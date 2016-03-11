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
public class MyVerticleTest {

  private Vertx vertx;

  @Before
  public void setUp(final TestContext context) {
    vertx = Vertx.vertx();
    vertx.deployVerticle(MyVerticle.class.getName(), context.asyncAssertSuccess());
  }

  @After
  public void tearDown(final TestContext context) {
    vertx.close(context.asyncAssertSuccess());
  }

  @Test
  public void testStart(final TestContext context) {
    final Async async = context.async();

    vertx.createHttpClient()
        .get(8080, "localhost", "/", response ->
            response.handler(body -> {
              context.assertTrue(body.toString().startsWith("Hello"));
              async.complete();
            }))
        .end();
  }
}
