package nl.bransom.reactive;

import io.vertx.core.VertxOptions;
import io.vertx.rxjava.core.CompositeFuture;
import io.vertx.rxjava.core.Future;
import io.vertx.rxjava.core.Vertx;
import io.vertx.rxjava.core.eventbus.Message;
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
    vertx.setTimer(15000, timerId -> {
      vertx.close();
      LOG.info("And... it's gone!");
    });

    final Future<String> atRainMakerStart = Future.future();
    final Future<String> atRainServerStart = Future.future();

    vertx.deployVerticle(RainMaker.class.getName(), atRainMakerStart.completer());
    vertx.deployVerticle(RainServer.class.getName(), atRainServerStart.completer());

    CompositeFuture.all(atRainMakerStart, atRainServerStart).setHandler(res -> {
      if (res.succeeded()) {
        vertx.eventBus().send(RainMaker.INTENSITY_MSG, 0.9);
        vertx.setTimer(4000, timerId -> vertx.eventBus().send(RainMaker.INTENSITY_MSG, 0.4));
        vertx.setTimer(8000, timerId -> vertx.eventBus().send(RainMaker.INTENSITY_MSG, 0.0));
      } else {
        LOG.error("There won't be any rain today...", res.cause());
      }
    });

    vertx.eventBus()
        .<String>consumer(RainMaker.RAIN_DROP_MSG)
        .toObservable()
        .map(Message::body)
        .subscribe(rainDropJson -> LOG.debug("\t{}", rainDropJson));
  }
}
