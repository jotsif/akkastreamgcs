package akkastreamsgcs.scaladsl

import scala.concurrent.Future

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Source, Sink}
import akka.http.scaladsl.model.HttpResponse
import akka.util.ByteString
import akkastreamsgcs.BucketObject
import akkastreamsgcs.impl.{ListBucket, ListBucketRequest, ObjectSource, ObjectSink}

object GCSAPI {
  /** list call lists the contents in a gcs bucket
    * 
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
    * @param bucket Name of the google bucket
    * @param file Name of the file
    * @param token Oauth2 token
    * 
    */
  def upload(
    bucket: String,
    file: String,
    token: String
  ) (
    implicit system: ActorSystem, mat: ActorMaterializer
  ) : Sink[ByteString, Future[HttpResponse]] = {
    ObjectSink.create(bucket, file, token)
  }
}
