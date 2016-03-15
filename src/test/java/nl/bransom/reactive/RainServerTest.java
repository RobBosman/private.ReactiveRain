package nl.bransom.reactive;

import io.vertx.core.Vertx;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(VertxUnitRunner.class)
public class RainServerTest {

  private Vertx vertx;

  @Before
  public void setUp(final TestContext testContext) {
    vertx = Vertx.vertx();
    vertx.deployVerticle(RainServer.class.getName(), testContext.asyncAssertSuccess());
  }

  @After
  public void tearDown(final TestContext testContext) {
    vertx.close(testContext.asyncAssertSuccess());
  }

  @Test
  public void testStart(final TestContext testContext) {
    vertx.createHttpClient()
        .get(8080, "localhost", "/", response ->
            response.handler(body -> {
              testContext.assertTrue(body.toString().contains("Reactive rain in Monte Carlo"));
              testContext.async().complete();
            }))
        .end();
  }
}
