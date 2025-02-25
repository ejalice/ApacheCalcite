/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to you under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.calcite.adapter.mongodb;

import org.apache.calcite.schema.Schema;
import org.apache.calcite.schema.SchemaFactory;
import org.apache.calcite.schema.SchemaPlus;

import com.mongodb.AuthenticationMechanism;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.MongoCredential;

import java.util.Map;

/**
 * Factory that creates a {@link MongoSchema}.
 *
 * <p>Allows a custom schema to be included in a model.json file.
 */
public class MongoSchemaFactory implements SchemaFactory {
  // public constructor, per factory contract
  public MongoSchemaFactory() {
  }

  @Override public Schema create(SchemaPlus parentSchema, String name,
      Map<String, Object> operand) {
    final String host = (String) operand.get("host");
    final String port = (String) operand.get("port"); // 연결 port 추가
    final String database = (String) operand.get("database");
    final String authMechanismName = (String) operand.get("authMechanism");

    final MongoClientSettings.Builder settings =
        MongoClientSettings
            .builder()
            .applyConnectionString(new ConnectionString("mongodb://" + host + ":" + port)); // mongodb 연결
//            .applyConnectionString(new ConnectionString(host));

    if (authMechanismName != null) {
      settings.credential(createCredential(operand));
    }

    return new MongoSchema(settings.build(), database);
  }

  private static MongoCredential createCredential(Map<String, Object> map) {
    final String authMechanismName = (String) map.get("authMechanism");
    final AuthenticationMechanism authenticationMechanism =
        AuthenticationMechanism.fromMechanismName(authMechanismName);
    final String username = (String) map.get("username");
    final String authDatabase = (String) map.get("authDatabase");
    final String password = (String) map.get("password");

    switch (authenticationMechanism) {
    case PLAIN:
      return MongoCredential.createPlainCredential(username, authDatabase,
          password.toCharArray());
    case SCRAM_SHA_1:
      return MongoCredential.createScramSha1Credential(username, authDatabase,
          password.toCharArray());
    case SCRAM_SHA_256:
      return MongoCredential.createScramSha256Credential(username, authDatabase,
          password.toCharArray());
    case GSSAPI:
      return MongoCredential.createGSSAPICredential(username);
    case MONGODB_X509:
      return MongoCredential.createMongoX509Credential(username);
    default:
      break;
    }
    throw new IllegalArgumentException("Unsupported authentication mechanism "
        + authMechanismName);
  }
}
