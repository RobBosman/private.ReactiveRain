package nl.bransom.reactive;

import io.vertx.core.Vertx;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(VertxUnitRunner.class)
public class RainServer4Test {

  private Vertx vertx;

  @Before
  public void setUp(final TestContext testContext) {
    vertx = Vertx.vertx();
    vertx
        .exceptionHandler(testContext.exceptionHandler())
        .deployVerticle(RainServer.class.getName(), testContext.asyncAssertSuccess());
  }

  @After
  public void tearDown(final TestContext testContext) {
    vertx.close(testContext.asyncAssertSuccess());
  }

  @Test
  public void test(final TestContext testContext) {
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
    async.awaitSuccess();
  }
}
