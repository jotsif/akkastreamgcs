package akkastreamsgcs.auth

import scala.concurrent.Future

import io.igl.jwt._
import java.util.Date
import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.http.scaladsl.model.HttpMethods.{POST}
import akka.http.scaladsl.model.{FormData, HttpRequest}
import akka.http.scaladsl.Http
import akkastreamsgcs.{GoogleToken, ErrorMessage, GoogleRequestResponse, GoogleProtocols}
import org.apache.commons.codec.binary.Base64
import spray.json._

object Auth extends GoogleProtocols {
  // URI for oauth2 token request
  private val tokenuri = "https://www.googleapis.com/oauth2/v4/token"
  // Permission for reading and writing to gcs
  private val gcsreadwritescope = "https://www.googleapis.com/auth/devstorage.read_write"

  // Get secrets from environment
  lazy private val client_email = sys.env("GCS_READERWRITER_EMAIL")
  lazy private val privatekey = sys.env("GCS_PRIVATE_PRIVATEKEY")

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
      Seq(Iss(client_email), Aud("https://www.googleapis.com/oauth2/v4/token"), Scope(gcsreadwritescope), Exp(expiresAt), Iat(now))
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

  /** getToken gets a token by sending a token request to Google
    * 
    * @client_email email for the service account
    * @privatekey PKCS8 formatted private RSA key
    */
  def getToken(
    client_email: String,
    privatekey: String
  ) (
    implicit system: ActorSystem, mat: ActorMaterializer
  ): Future[GoogleRequestResponse] = {
    import mat.executionContext
    Http()
      .singleRequest(tokenRequest(client_email, privatekey))
      .flatMap(response => {
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
      })
  }
}
