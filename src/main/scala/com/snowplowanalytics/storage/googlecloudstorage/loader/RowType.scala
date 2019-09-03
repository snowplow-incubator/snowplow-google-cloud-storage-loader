/*
 * Copyright (c) 2018-2019 Snowplow Analytics Ltd. All rights reserved.
 *
 * This program is licensed to you under the Apache License Version 2.0,
 * and you may not use this file except in compliance with the Apache License Version 2.0.
 * You may obtain a copy of the Apache License Version 2.0 at
 * http://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the Apache License Version 2.0 is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Apache License Version 2.0 for the specific language governing permissions and
 * limitations there under.
 */
package com.snowplowanalytics.storage.googlecloudstorage.loader

import com.snowplowanalytics.iglu.core.SchemaKey

/** Type of row determined according to schema of self describing data */
sealed trait RowType extends Product with Serializable {
  def prefix: String
}

object RowType {

  /** Represents cases where row type could not be determined
    * since either row is not valid json or it is not self
    * describing json
    */
  case class Unpartitioned(reason: String) extends RowType {
    override def prefix: String = reason
  }

  /** Represents cases where type of row can be determined successfully
    * e.g. does have proper schema key
    */
  case class SelfDescribing(vendor: String, name: String, format: String, model: Int) extends RowType {
    def prefix: String = s"$vendor.$name/$format-$model"
  }
  object SelfDescribing {
    def apply(schemaKey: SchemaKey) = new SelfDescribing(schemaKey.vendor, schemaKey.name, schemaKey.format, schemaKey.version.model)
  }
}
