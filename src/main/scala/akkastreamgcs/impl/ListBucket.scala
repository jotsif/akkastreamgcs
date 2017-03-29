package akkastreamsgcs.scaladsl


import scala.concurrent.Future

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.HttpMethods.GET
import akka.http.scaladsl.model.HttpRequest
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Source
import akkastreamsgcs.{BucketListResponse, GoogleProtocols, BucketObject}
import spray.json._

object ListBucket extends GoogleProtocols {
  private val storageuri = "https://www.googleapis.com/storage/v1/b"

  private def listBucketRequest(
    request: ListBucketRequest,
    continuation_token: Option[String]
  ) : HttpRequest = {
    val token = "testtoken"
    HttpRequest(
      GET,
      uri = storageuri + "/" + request.bucket + "/o",
      entity = ListBucketRequest.requestToFormData(request, token, continuation_token).toEntity
    )
  }

  /** list lists contents in a Google bucket */
  def list(
    request: ListBucketRequest
  ) (
    implicit system: ActorSystem, mat: ActorMaterializer
  ) : Source[BucketObject, akka.NotUsed] = {
    import mat.executionContext
    def listBucketCall(
      continuation_token: Option[String] = None
    ) : Future[BucketListResponse] =
      Http()
        .singleRequest(listBucketRequest(request, continuation_token))
        .flatMap(_.entity.dataBytes.runReduce((a, b) => a++b))
        .map(_.utf8String.parseJson.convertTo[BucketListResponse])

    def fileSourceFromFuture(
      f: Future[BucketListResponse]
    ): Source[BucketObject, akka.NotUsed] =
      Source
        .fromFuture(f)
        .flatMapConcat(res => {
          val keys = Source.fromIterator(() => res.items.toIterator)
          if (!res.nextPageToken.isEmpty) {
            keys.concat(fileSourceFromFuture(listBucketCall(res.nextPageToken)))
          } else
            keys
        })
    fileSourceFromFuture(listBucketCall())
  }
}
