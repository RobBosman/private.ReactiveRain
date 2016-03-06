package nl.bransom.vertx;

import io.vertx.core.Future;
import io.vertx.rxjava.core.AbstractVerticle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RainServer extends AbstractVerticle {

  private static final Logger LOG = LoggerFactory.getLogger(RainServer.class);

  @Override
  public void start(final Future<Void> startFuture) {
    vertx.createHttpServer()
        .requestHandler(request -> request
            .handler(buffer -> LOG.debug("received some bytes: " + buffer.toString()))
            .bodyHandler(totalBuffer -> LOG.debug("Full body received, path = " + request.path() + ", length = " + totalBuffer.length()))
            .response().end("Hello world"))
        .listen(8080, "localhost", res -> {
          if (res.succeeded()) {
            LOG.info("Server is now listening on http://localhost:8080/");
            startFuture.complete();
          } else {
            startFuture.fail(res.cause());
          }
        });
  }
}
