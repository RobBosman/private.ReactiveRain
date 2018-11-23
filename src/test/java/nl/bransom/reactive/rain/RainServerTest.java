package nl.bransom.reactive.rain;

import io.vertx.core.Vertx;
import io.vertx.ext.unit.TestSuite;
import org.junit.jupiter.api.Test;

class RainServerTest {

  private Vertx vertx;

  @Test
  void test() {
    vertx = Vertx.vertx();

    TestSuite
        .create("testSuite")
        .before(testContext ->
            vertx
                .exceptionHandler(testContext.exceptionHandler())
                .deployVerticle(RainServer.class.getName(), testContext.asyncAssertSuccess()))
        .test("testStart", testContext -> {
          final var async = testContext.async();
          vertx
              .exceptionHandler(testContext.exceptionHandler())
              .createHttpClient()
              .get(RainConstants.SERVER_PORT, "localhost", "/", response -> {
                testContext.assertEquals(200, response.statusCode());
                response.bodyHandler(body -> {
                  testContext.assertTrue(body.toString().contains("Reactive Rain in Monte Carlo"));
                  async.complete();
                });
              })
              .end();
        })
        .run()
        .awaitSuccess();
  }
}
