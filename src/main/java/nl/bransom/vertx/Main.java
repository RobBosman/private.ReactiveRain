package nl.bransom.vertx;

import io.vertx.core.Future;
import io.vertx.core.VertxOptions;
import io.vertx.rxjava.core.Vertx;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {

  private static final Logger LOG = LoggerFactory.getLogger(Main.class);
  private static final boolean CLUSTERED = false;

  public static void main(String[] args) {
    if (CLUSTERED) {
      final VertxOptions options = new VertxOptions();
      Vertx.clusteredVertx(options, res -> {
        if (res.succeeded()) {
          goForIt(res.result());
        } else {
          LOG.error("Error getting clustered vertx.", res.cause());
        }
      });
    } else {
      goForIt(Vertx.vertx());
    }
  }

  public static void goForIt(final Vertx vertx) {
    vertx.setTimer(5000, timerId -> {
      vertx.close();
      LOG.info("And... it's gone!");
    });

    final Future<String> startRainMakerResult = Future.<String>future();
    vertx.deployVerticle(RainMaker.class.getName(), res -> {
      if (res.succeeded()) {
        startRainMakerResult.complete();
      } else {
        startRainMakerResult.fail(res.cause());
      }
    });
    vertx.deployVerticleObservable(RainServer.class.getName())
        .subscribe();
    startRainMakerResult.setHandler(res -> {
      if (res.succeeded()) {
        vertx.setTimer(3000, timerId -> vertx.eventBus().send(RainMaker.INTENSITY_TAG, 0.85));
      } else {
        LOG.error("Error starting rain: ", res.cause());
      }
    });
  }
}
