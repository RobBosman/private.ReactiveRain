package nl.bransom.reactive;

import io.vertx.core.Vertx;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestSuite;
import org.junit.jupiter.api.Test;

class RainServerTest {

  private Vertx vertx;

  @Test
  void test() {
    vertx = Vertx.vertx();

    TestSuite
        .create("testSuite")
        .before(testContext -> {
          final Async async = testContext.async();
          vertx
              .exceptionHandler(testContext.exceptionHandler())
              .deployVerticle(RainServer.class.getName(), testContext.asyncAssertSuccess(x ->
                  async.complete()));
        })
        .test("testStart", testContext -> {
          final Async async = testContext.async();
          vertx
              .exceptionHandler(testContext.exceptionHandler())
              .createHttpClient()
              .get(8080, "localhost", "/", response -> {
                testContext.assertEquals(200, response.statusCode());
                response.bodyHandler(body -> {
                  testContext.assertTrue(body.toString().contains("Reactive rain in Monte Carlo"));
                  async.complete();
                });
              })
              .end();
        })
        .run()
        .awaitSuccess();
  }
}
