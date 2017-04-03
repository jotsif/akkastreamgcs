package akkastreamsgcs.impl

import scala.concurrent.Future

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, HttpRequest, HttpResponse, Uri}
import akka.http.scaladsl.model.HttpMethods.POST
import akka.http.scaladsl.model.Uri.Query
import akka.http.scaladsl.model.headers.{Authorization, OAuth2BearerToken}
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Flow, Keep, Sink}
import akka.util.ByteString
import akkastreamsgcs.GoogleProtocols

object ObjectSink extends GoogleAPI with GoogleProtocols {

  private def uploadObjectRequest(
    bucket: String,
    file: String,
    token: String
  ) = {
    val query = Query(Map("uploadType" -> "media", "name" -> file))
    HttpRequest(
      POST,
      uri = Uri.from(scheme = scheme, host = host, path = uploaduri + bucket + "/o").withQuery(query),
      headers = List(Authorization(OAuth2BearerToken(token)))
    )
  }

  /** create creates a Sink uploading the ByteString stream as chunks to GCS
    * 
    * How do we regulate the input ByteString sizes?
    * 
    */
  def create(
    bucket: String,
    file: String,
    token: String
  ) (
    implicit system: ActorSystem, mat: ActorMaterializer
  ) : Sink[ByteString, Future[HttpResponse]] = {
    Flow[ByteString]
      .grouped(1024) // How large are the input ByteStrings ?
      .map(seq => seq.reduce(_ ++ _))
      .prefixAndTail(0)
      .map{case (_, source) => {
        val req = uploadObjectRequest(bucket, file, token).withEntity(HttpEntity(ContentTypes.`application/octet-stream`, source))
        println(req)
        req
      }}
      .mapAsync(10)(request => Http().singleRequest(request))
      .toMat(Sink.head)(Keep.right)
  }
}
