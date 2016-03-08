package nl.bransom.vertx;

import io.vertx.core.Future;
import io.vertx.rxjava.core.AbstractVerticle;
import io.vertx.rxjava.ext.web.Router;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RainServer extends AbstractVerticle {

  private static final Logger LOG = LoggerFactory.getLogger(RainServer.class);

  @Override
  public void start(final Future<Void> startFuture) {

    final Router router = Router.router(vertx);

    router.route("/*")
        .handler(routingContext -> {
          routingContext.response()
              // enable chunked responses because we will be adding data as
              // we execute over other handlers. This is only required once and
              // only if several handlers do output.
              .setChunked(true)
              .putHeader("content-type", "text/plain")
              .write("Hello World via route1\n")
              .end();
          routingContext.next();
        });

    vertx.createHttpServer()
        .requestHandler(router::accept)
        .listen(8080, res -> {
          if (res.succeeded()) {
            LOG.info("Server is now listening on http://localhost:8080/");
            startFuture.complete();
          } else {
            startFuture.fail(res.cause());
          }
        });
  }
}
