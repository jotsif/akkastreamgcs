package akkastreamsgcs

import spray.json._

case class BucketObject(
  kind: String,
  id: String,
  name: String,
  crc32c: String,
  md5Hash: String,
  size: String
) extends InsertRequestResponse // FIXME- should be a more general class

case class Error(
  domain: String,
  reason: String,
  message: String
)

case class ErrorList(
  errors: Seq[Error],
  code: Int,
  message: String
)

sealed trait InsertRequestResponse

final case class InsertErrorMessage(
  error: ErrorList
) extends InsertRequestResponse

case class BucketListResponse(
  kind: String,
  nextPageToken: Option[String],
  prefixes: Option[Seq[String]],
  items: Option[Seq[BucketObject]]
)

sealed trait Oauth2RequestResponse

final case class GoogleToken(
  access_token: String,
  token_type: String,
  expires_in: Int
) extends Oauth2RequestResponse

final case class ErrorMessage(
  error: String,
  error_description: Option[String]
) extends Oauth2RequestResponse

trait GoogleProtocols extends DefaultJsonProtocol {
  implicit val insertErrorFormat = jsonFormat3(Error.apply)
  implicit val inserterrorlistformat = jsonFormat3(ErrorList.apply)
  implicit val InsertErrorMessageFormat = jsonFormat1(InsertErrorMessage.apply)
  implicit val objectformat = jsonFormat6(BucketObject.apply)
  implicit val bucketlistformat = jsonFormat4(BucketListResponse.apply)
  implicit val tokenformat = jsonFormat3(GoogleToken.apply)
  implicit val errorformat = jsonFormat2(ErrorMessage.apply)
}
