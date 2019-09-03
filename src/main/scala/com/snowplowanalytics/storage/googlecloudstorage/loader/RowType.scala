package com.snowplowanalytics.storage.googlecloudstorage.loader

import com.snowplowanalytics.iglu.core.SchemaKey

/** Type of row which determined according to schema of self describing data */
sealed trait RowType extends Product with Serializable {
  def getName(): String
}

object RowType {

  /** Represents cases where row type could not be determined
    * since either row is not valid json or it is not self
    * describing json
    */
  case class PartitionError(errorDir: String) extends RowType {
    override def getName(): String = errorDir
  }

  /** Represents cases where type of row can be determined successfully
    * e.g. does have proper schema key
    */
  case class SelfDescribing(schemaKey: SchemaKey) extends RowType {
    override def getName(): String = s"${schemaKey.vendor}.${schemaKey.name}"
  }
}
