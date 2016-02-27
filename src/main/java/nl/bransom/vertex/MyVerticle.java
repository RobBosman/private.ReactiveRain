package nl.bransom.vertex;

import io.vertx.core.AbstractVerticle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MyVerticle extends AbstractVerticle {
  
  private static final Logger LOG = LoggerFactory.getLogger(MyVerticle.class);

  @Override
  public void start() {
    LOG.info(getClass().getName() + " - deployed");

    vertx.eventBus().consumer("news.uk.sport")
        .handler(message -> {
          LOG.info("received message: " + message.body());
          message.reply("how interesting!");
        });

    vertx.createHttpServer()
        .requestHandler(request -> {
          request.handler(buffer -> LOG.debug("received some bytes: " + buffer.toString()));

          request.bodyHandler(totalBuffer -> LOG.debug("Full body received, path = " + request.path() + ", length = " + totalBuffer.length()));

          request.response().end("Hello world");

        })
        .listen(8080, "localhost", res -> {
          if (res.succeeded()) {
            LOG.info("Server is now listening on http://localhost:8080/");
          } else {
            LOG.error("Failed to bind!", res.cause());
          }
        });
  }

  @Override
  public void stop() {
    LOG.info(getClass().getName() + " - undeployed");
  }
}
