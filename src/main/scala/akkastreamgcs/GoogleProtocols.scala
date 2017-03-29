package akkastreamsgcs

import spray.json._

case class BucketObject(
  kind: String,
  id: String,
  name: String,
  crc32c: String,
  md5Hash: String,
  size: String
)

case class BucketListResponse(
  kind: String,
  nextPageToken: Option[String],
  prefixes: Seq[String],
  items: Seq[BucketObject]
)

sealed trait GoogleRequestResponse

final case class GoogleToken(
  access_token: String,
  token_type: String,
  expires_in: Int
) extends GoogleRequestResponse

final case class ErrorMessage(
  error: String,
  error_description: Option[String]
) extends GoogleRequestResponse

trait GoogleProtocols extends DefaultJsonProtocol {
  implicit val objectformat = jsonFormat6(BucketObject.apply)
  implicit val bucketlistformat = jsonFormat4(BucketListResponse.apply)
  implicit val tokenformat = jsonFormat3(GoogleToken.apply)
  implicit val errorformat = jsonFormat2(ErrorMessage.apply)
}
