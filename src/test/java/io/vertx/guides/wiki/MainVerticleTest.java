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

package io.vertx.guides.wiki;




import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import com.vertx.qui.first.MyFirstVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.WebClient;
@RunWith(VertxUnitRunner.class)
public class MainVerticleTest {
  
  

  private Vertx vertx;
  private int port=8089;
  @Before
  public void setUp(TestContext context) {
    DeploymentOptions options = new DeploymentOptions()
        .setConfig(new JsonObject().put("http.port", port)
    );
    vertx = Vertx.vertx();
    vertx.deployVerticle(MyFirstVerticle.class.getName(), options, context.asyncAssertSuccess());
  }

  @After
  public void tearDown(TestContext context) {
    vertx.close(context.asyncAssertSuccess());
  }

  @Test
  public void testMyApplication(TestContext context) {
    Async async = context.async();

    vertx
      .createHttpServer().requestHandler(req ->
      req.response().putHeader("Content-Type", "text/plain").end("Ok"))
      .listen(port, context.asyncAssertSuccess(server -> {

        WebClient webClient = WebClient.create(vertx);

        webClient.get(port, "localhost", "/").send(ar -> {
          if (ar.succeeded()) {
            HttpResponse<Buffer> response = ar.result();
            context.assertTrue(response.headers().contains("Content-Type"));
            context.assertEquals("text/plain", response.getHeader("Content-Type"));
            context.assertEquals("Ok", response.body().toString());
            async.complete();
          } else {
            async.resolve(Future.failedFuture(ar.cause()));
          }
        });
      }));

  }
 
  @Test /*(timeout=5000)*/  
  public void async_behavior(TestContext context) { 
    Vertx vertx = Vertx.vertx();  
    context.assertEquals("foo", "foo");  
    Async a1 = context.async();   
    Async a2 = context.async(3);  
    vertx.setTimer(100, n -> a1.complete());  
    vertx.setPeriodic(100, n -> a2.countDown());  
  }
}