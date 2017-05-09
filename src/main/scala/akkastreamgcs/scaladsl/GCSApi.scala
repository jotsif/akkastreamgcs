package akkastreamsgcs.scaladsl

import scala.concurrent.Future

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Sink, Source}
import akka.util.ByteString
import akkastreamsgcs.{BucketObject, Oauth2RequestResponse, InsertRequestResponse}
import akkastreamsgcs.impl.{ListBucket, ListBucketRequest, ObjectSink, ObjectSource}
import akkastreamsgcs.auth.Auth
import io.igl.jwt.Scope

object GCSAPI {
  /** token gets a OAuth2 Bearer token needed for the other API Calls
    *
    * @param client_email email for the service account
    * @param privatekey PKCS8 formatted private RSA key
    */
  def token(
    client_email: String,
    privatekey: String,
    scopes: Seq[Scope]
  ) (
    implicit system: ActorSystem, mat: ActorMaterializer
  ): Future[Oauth2RequestResponse] = {
    Auth.getToken(client_email, privatekey, scopes)
  }
  /** list call lists the contents in a gcs bucket
    * 
    * It returns the directory as well, with size 0
    * 
    * @param bucket Name of the google bucket
    * @param prefix prefix to use to filter the result
    * @param delimiter delimiter to use
    * @param token Oauth2 token
    */
  def list(
    bucket: String,
    prefix: Option[String] = None,
    delimiter: Option[String] = None,
    token: String
  ) (
    implicit system: ActorSystem, mat: ActorMaterializer
  ) : Source[BucketObject, akka.NotUsed] = {
    ListBucket.list(ListBucketRequest(
      bucket = bucket,
      prefix = prefix,
      delimiter = delimiter,
      token = token
    ))
  }
  /** get gets a GCS object as a ByteString source
    * 
    * @param bucket Name of the google bucket
    * @param file Name of the file
    * @param token Oauth2 token
    * 
    */
  def get(
    bucket: String,
    file: String,
    token: String
  )  (
    implicit system: ActorSystem, mat: ActorMaterializer
  ) : Source[ByteString, akka.NotUsed] = {
    ObjectSource.create(bucket, file, token)
  }
  /** upload a file to GCS using chunks 
    * 
    * Uploading to an already existing object requires delete permissions
    * 
    * @param bucket Name of the google bucket
    * @param file Name of the file
    * @param token Oauth2 token
    * @param chunksize Parameter needed for deciding chunk size for file upload
    * 
    */
  def upload(
    bucket: String,
    file: String,
    token: String,
    chunksize: Int = 1024
  ) (
    implicit system: ActorSystem, mat: ActorMaterializer
  ) : Sink[ByteString, Future[InsertRequestResponse]] = {
    ObjectSink.create(bucket, file, token, chunksize)
  }
}
