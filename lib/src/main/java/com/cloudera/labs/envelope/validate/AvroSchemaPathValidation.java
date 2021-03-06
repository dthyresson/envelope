/*
 * Copyright (c) 2015-2018, Cloudera, Inc. All Rights Reserved.
 *
 * Cloudera, Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"). You may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * This software is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied. See the License for
 * the specific language governing permissions and limitations under the
 * License.
 */

package com.cloudera.labs.envelope.validate;

import com.cloudera.labs.envelope.utils.AvroUtils;
import com.cloudera.labs.envelope.utils.FilesystemUtils;
import com.google.common.collect.Sets;
import com.typesafe.config.Config;
import org.apache.avro.Schema;

import java.util.Set;

public class AvroSchemaPathValidation implements Validation {

  private String path;

  public AvroSchemaPathValidation(String path) {
    this.path = path;
  }

  @Override
  public ValidationResult validate(Config config) {
    String literal;
    try {
      literal = FilesystemUtils.filesystemPathContents(config.getString(path));
    }
    catch (Exception e) {
      return new ValidationResult(this, Validity.INVALID,
          "Avro schema could not be retrieved from path. " +
              "See stack trace below for more information.", e);
    }

    Schema schema;
    try {
      schema = new Schema.Parser().parse(literal);
    }
    catch (Exception e) {
      return new ValidationResult(this, Validity.INVALID,
          "Avro schema from path could not be parsed. " +
              "See stack trace below for more information.", e);
    }

    try {
      AvroUtils.structTypeFor(schema);
    }
    catch (Exception e) {
      return new ValidationResult(this, Validity.INVALID,
          "Avro schema from path could be parsed, but could not be converted " +
              "to a Spark SQL StructType. See stack trace below for more information.", e);
    }

    return new ValidationResult(this, Validity.VALID,
        "Avro schema from path could be parsed and converted to a Spark SQL StructType");
  }

  @Override
  public Set<String> getKnownPaths() {
    return Sets.newHashSet(path);
  }

}
