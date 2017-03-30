package akkastreamsgcs.impl

import akka.http.scaladsl.Http

import akkastreamsgcs.GoogleProtocols
import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Source
import akka.http.scaladsl.model.HttpMethods.GET
import akka.http.scaladsl.model.{HttpRequest, Uri}
import akka.http.scaladsl.model.headers.{Authorization, OAuth2BearerToken}
import akka.util.ByteString

object ObjectSource extends GoogleAPI with GoogleProtocols {

  def getObjectRequest(
    bucket: String,
    file: String,
    token: String
  ) = {
    val req = HttpRequest(
      GET,
      uri = Uri.from(scheme = scheme, host = host, path = storageuri + "/" + bucket + "/o/" + file, queryString = Some("alt=media")),
      headers = List(Authorization(OAuth2BearerToken(token)))
    )
    println(req)
    req
  }

  def create(
    bucket: String,
    file: String,
    token: String
  ) (
    implicit system: ActorSystem, mat: ActorMaterializer
  ) : Source[ByteString, akka.NotUsed] = {
    import mat.executionContext    
    Source.fromFuture(
      Http().singleRequest(getObjectRequest(bucket, file, token))
        .map(_.entity.dataBytes)
    )
      .flatMapConcat(identity)
  }
}
