package akkastreamsgcs.auth

import scala.concurrent.Future

import io.igl.jwt._
import java.util.Date
import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.http.scaladsl.model.HttpMethods.{POST, GET}
import akka.http.scaladsl.model.{FormData, HttpRequest, HttpResponse}
import akka.http.scaladsl.model.headers.RawHeader
import akka.http.scaladsl.Http
import akkastreamsgcs.{GoogleToken, ErrorMessage, Oauth2RequestResponse, GoogleProtocols}
import org.apache.commons.codec.binary.Base64
import spray.json._

object Auth extends GoogleProtocols {
  // URI for oauth2 token request
  private val tokenuri = "https://www.googleapis.com/oauth2/v4/token"
  // URI for internal metadata token request
  private val internaltokenuri = "http://metadata.google.internal/computeMetadata/v1/instance/service-accounts/default/token"
  // Permission for reading and writing to gcs
  private val gcsreadwritescope = "https://www.googleapis.com/auth/devstorage.read_write"
  private val gcsfullcontrolscope = "https://www.googleapis.com/auth/devstorage.full_control"

  /** Create JWT for reading and writing to GCS 
    * 
    * @client_email email for the service account
    * @privatekey PKCS8 formatted private RSA key
    */
  private[auth] def createJWT(
    client_email: String,
    privatekey: String
  ) : String = {
    val now = (new Date()).getTime()/1000
    val expiresAt = now + 3600
    val jwt = new DecodedJwt(
      Seq(Alg(Algorithm.RS256), Typ("JWT")),
      Seq(Iss(client_email), Aud("https://www.googleapis.com/oauth2/v4/token"), Scope(gcsfullcontrolscope), Exp(expiresAt), Iat(now))
    )
    jwt.encodedAndSigned(Base64.decodeBase64(privatekey))
  }
  /** tokenRequest creates a HttpRequest for getting a oauth2 token from google 
    * 
    * @client_email email for the service account
    * @privatekey PKCS8 formatted private RSA key
    */
  private[auth] def tokenRequest(
    client_email: String,
    privatekey: String
  ) : HttpRequest = {
    HttpRequest(
      POST,
      uri = tokenuri,
      entity = FormData(Map(
        "grant_type" -> "urn:ietf:params:oauth:grant-type:jwt-bearer",
        "assertion" -> createJWT(client_email, privatekey)
      )).toEntity
    )
  }
  /** Get token from internal server */
  private[auth] def tokenRequest : HttpRequest = {
    HttpRequest(
      GET,
      uri = internaltokenuri,
      headers = List(RawHeader("Metadata-Flavor", "Google"))
    )
  }

  def tokenResponseToToken(
    response: HttpResponse
  ) (implicit mat: ActorMaterializer) : Future[Oauth2RequestResponse] = {
    import mat.executionContext
    response
      .entity.dataBytes
      .runReduce((a, b) => a++b)
      .map(_.utf8String.parseJson)
      .map(jsonobject => {
        if(jsonobject.asJsObject.fields.contains("error"))
          jsonobject.convertTo[ErrorMessage]
        else
          jsonobject.convertTo[GoogleToken]
      })
  }

  /** getToken gets a token by sending a token request to Google
    * 
    * FIXME: Controll access scope here
    * 
    * @client_email email for the service account
    * @privatekey PKCS8 formatted private RSA key
    */
  def getToken(
    client_email: String,
    privatekey: String
  ) (
    implicit system: ActorSystem, mat: ActorMaterializer
  ): Future[Oauth2RequestResponse] = {
    import mat.executionContext
    Http()
      .singleRequest(tokenRequest(client_email, privatekey))
      .flatMap(response => tokenResponseToToken(response))
  }
  /** getToken gets a token from internal metadata servers */
  def getToken(
    implicit system: ActorSystem, mat: ActorMaterializer
  ) : Future[Oauth2RequestResponse] = {
    import mat.executionContext
    Http()
      .singleRequest(tokenRequest)
      .flatMap(response => tokenResponseToToken(response))
  }
}
