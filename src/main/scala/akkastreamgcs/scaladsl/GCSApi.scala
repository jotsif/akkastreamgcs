package akkastreamsgcs.scaladsl

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Source
import akka.http.scaladsl.model.{FormData}
import akka.http.scaladsl.model.Uri.Query
import akka.util.ByteString
import akkastreamsgcs.BucketObject

case class ListBucketRequest(
  bucket: String,
  delimiter: Option[String],
  prefix: Option[String]
)

object ListBucketRequest {
  def requestToFormData(
    request: ListBucketRequest,
    access_token: String,
    continuation_token: Option[String]
  ) : FormData = {
    val uri_builder = Query.newBuilder
    if(!request.delimiter.isEmpty)
      uri_builder += (("delimiter", request.delimiter.get))
    if(!request.prefix.isEmpty)
      uri_builder += (("prefix", request.prefix.get))
    if(!continuation_token.isEmpty)
      uri_builder += (("pageToken", continuation_token.get))
    uri_builder += (("access_token", access_token))
    FormData(uri_builder.result())
  }
}


object GCSAPI {
  /** list call lists the contents in a gcs bucket
    * 
    * 
    * @param bucket Name of the google bucket
    * @param prefix prefix to use to filter the result
    * @param delimiter delimiter to use
    */
  def list(
    bucket: String,
    prefix: Option[String] = None,
    delimiter: Option[String] = None
  )  (
    implicit system: ActorSystem, mat: ActorMaterializer
  ) : Source[BucketObject, akka.NotUsed] = {
    ListBucket.list(ListBucketRequest(
      bucket = bucket,
      prefix = prefix,
      delimiter = delimiter
    ))
  }
  /** get gets a GCS object */
  def get(
    file: String
  )  (
    implicit system: ActorSystem, mat: ActorMaterializer
  ) : Source[ByteString, akka.NotUsed] = {
    null
  }
}
