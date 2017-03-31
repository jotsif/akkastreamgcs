package akkastreamsgcs.impl

trait GoogleAPI {
  protected val scheme = "https"
  protected val host = "www.googleapis.com"
  protected val storageuri = "/storage/v1/b/"
  protected val uploaduri = "/upload/storage/v1/b/"
}
