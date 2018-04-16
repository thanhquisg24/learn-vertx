/*
 *  Copyright (c) 2017 Red Hat, Inc. and/or its affiliates.
 *  Copyright (c) 2017 INSA Lyon, CITI Laboratory.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package step7.io.vertx.guides.wiki.database;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.asyncsql.MySQLClient;

import io.vertx.ext.sql.SQLClient;
import io.vertx.serviceproxy.ProxyHelper;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Properties;

/**
 * @author <a href="https://julien.ponge.org/">Julien Ponge</a>
 */
// tag::dbverticle[]
public class WikiDatabaseVerticle extends AbstractVerticle {


  public static final String CONFIG_WIKIDB_JDBC_HOST = "wikidb.jdbc.host";
  public static final String CONFIG_WIKIDB_JDBC_PORT = "wikidb.jdbc.port";
  public static final String CONFIG_WIKIDB_JDBC_DATABSAE = "wikidb.jdbc.database";
  public static final String CONFIG_WIKIDB_JDBC_USERNAME = "wikidb.jdbc.username";
  public static final String CONFIG_WIKIDB_JDBC_PASSWORD = "wikidb.jdbc.password";
  public static final String CONFIG_WIKIDB_JDBC_MAX_POOL_SIZE = "wikidb.jdbc.max_pool_size";
  public static final String CONFIG_WIKIDB_SQL_QUERIES_RESOURCE_FILE = "wikidb.sqlqueries.resource.file";

  public static final String CONFIG_WIKIDB_QUEUE = "wikidb.queue";

  @Override
  public JsonObject config() {
  
      return new JsonObject(
        vertx.fileSystem()
          .readFileBlocking("../../src/main/resources/conf/application-conf.json"));
   
  }
  
  @Override
  public void start(Future<Void> startFuture) throws Exception {

    HashMap<String, String> sqlQueries = loadSqlQueries();

    JsonObject mySQLClientConfig = new JsonObject().put("host",  config().getString(CONFIG_WIKIDB_JDBC_HOST))
        .put("port", config().getInteger(CONFIG_WIKIDB_JDBC_PORT))   // 
        .put("database", config().getString(CONFIG_WIKIDB_JDBC_DATABSAE))   // 
         .put("username", config().getString(CONFIG_WIKIDB_JDBC_USERNAME)) 
        .put("password", config().getString(CONFIG_WIKIDB_JDBC_PASSWORD))
        .put("max_pool_size", config().getInteger(CONFIG_WIKIDB_JDBC_MAX_POOL_SIZE)); 
    /*JDBCClient dbClient = JDBCClient.createShared(vertx, new JsonObject()
      .put("url", config().getString(CONFIG_WIKIDB_JDBC_URL, "jdbc:hsqldb:file:db/wiki"))
      .put("driver_class", config().getString(CONFIG_WIKIDB_JDBC_DRIVER_CLASS, "org.hsqldb.jdbcDriver"))
      .put("max_pool_size", config().getInteger(CONFIG_WIKIDB_JDBC_MAX_POOL_SIZE, 30)));*/
 
    SQLClient dbClient= MySQLClient.createNonShared(vertx, mySQLClientConfig);
    WikiDatabaseService.create(dbClient, sqlQueries, ready -> {
      if (ready.succeeded()) {
        ProxyHelper.registerService(WikiDatabaseService.class, vertx, ready.result(), config().getString(CONFIG_WIKIDB_QUEUE)); // <1>
        startFuture.complete();
      } else {
        startFuture.fail(ready.cause());
      }
    });
  }

  /*
   * Note: this uses blocking APIs, but data is small...
   */
  private HashMap<String, String> loadSqlQueries() throws IOException {

    String queriesFile = config().getString(CONFIG_WIKIDB_SQL_QUERIES_RESOURCE_FILE);
    InputStream queriesInputStream;
    if (queriesFile != null) {
      queriesInputStream = new FileInputStream(queriesFile);
    } else {
      queriesInputStream = getClass().getResourceAsStream("/db-queries.properties");
    }

    Properties queriesProps = new Properties();
    queriesProps.load(queriesInputStream);
    queriesInputStream.close();

    HashMap<String, String> sqlQueries = new HashMap<>();
    sqlQueries.put(SqlQueryVar.CREATE_PAGES_TABLE, queriesProps.getProperty("create-pages-table"));
    sqlQueries.put(SqlQueryVar.ALL_PAGES, queriesProps.getProperty("all-pages"));
    sqlQueries.put(SqlQueryVar.GET_PAGE, queriesProps.getProperty("get-page"));
    sqlQueries.put(SqlQueryVar.CREATE_PAGE, queriesProps.getProperty("create-page"));
    sqlQueries.put(SqlQueryVar.SAVE_PAGE, queriesProps.getProperty("save-page"));
    sqlQueries.put(SqlQueryVar.DELETE_PAGE, queriesProps.getProperty("delete-page"));
    sqlQueries.put(SqlQueryVar.GET_PAGE_BY_ID, queriesProps.getProperty("get-page-by-id"));
    sqlQueries.put(SqlQueryVar.ALL_PAGES_DATA, queriesProps.getProperty("all-pages-data"));
    
    return sqlQueries;
  }
}
// end::dbverticle[]
