package nl.bransom.reactive;

import io.vertx.core.VertxOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.rxjava.core.CompositeFuture;
import io.vertx.rxjava.core.Future;
import io.vertx.rxjava.core.Vertx;
import io.vertx.rxjava.core.eventbus.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main implements RainConstants {

  private static final Logger LOG = LoggerFactory.getLogger(Main.class);

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
//    vertx.setTimer(12000, timerId -> {
//      vertx.close();
//      LOG.info("And... it's gone!");
//    });

    final Future<String> atRainMakerStart = Future.future();
    final Future<String> atRainServerStart = Future.future();

    vertx.deployVerticle(RainMaker.class.getName(), atRainMakerStart.completer());
    vertx.deployVerticle(RainServer.class.getName(), atRainServerStart.completer());

    CompositeFuture.all(atRainMakerStart, atRainServerStart).setHandler(res -> {
      if (res.succeeded()) {
        vertx.eventBus().send(RAIN_MAKER_ADDRESS, new JsonObject().put(INTENSITY_KEY, 0.5));
      } else {
        LOG.error("There won't be any rain today...", res.cause());
      }
    });

    vertx.eventBus()
        .<JsonObject>consumer(RAIN_DROP_ADDRESS)
        .toObservable()
        .map(Message::body)
        .subscribe(rainDropJson -> LOG.debug("\t{}", rainDropJson));
  }
}
