package akkastreamsgcs.impl

import java.net.URLEncoder

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpRequest, Uri}
import akka.http.scaladsl.model.HttpMethods.GET
import akka.http.scaladsl.model.headers.{Authorization, OAuth2BearerToken}
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Source
import akka.util.ByteString
import akkastreamsgcs.GoogleProtocols

object ObjectSource extends GoogleAPI with GoogleProtocols {

  private def getObjectRequest(
    bucket: String,
    file: String,
    token: String
  ) = {
    HttpRequest(
      GET,
      uri = Uri.from(scheme = scheme, host = host, path = storageuri + bucket + "/o/" + URLEncoder.encode(file))
        .withQuery(Uri.Query("alt"-> "media")),
      headers = List(Authorization(OAuth2BearerToken(token)))
    )
  }
  /** create creates a source from a GCS object
    * 
    * TODO: Add materialisation to handle when object is not found
    * 
    */
  def create(
    bucket: String,
    file: String,
    token: String
  ) (
    implicit system: ActorSystem, mat: ActorMaterializer
  ) : Source[ByteString, akka.NotUsed] = {
    import mat.executionContext
    val objectrequest = getObjectRequest(bucket, file, token)
    Source.fromFuture(
      Http().singleRequest(objectrequest)
        .map(_.entity.dataBytes)
    )
      .flatMapConcat(identity)
  }
}
