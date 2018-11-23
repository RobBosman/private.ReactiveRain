package nl.bransom.reactive.rain;

import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {

  private static final Logger LOG = LoggerFactory.getLogger(Main.class);

  public static void main(String[] args) {
    if (RainConstants.CLUSTERED) {
      Vertx.clusteredVertx(new VertxOptions(), res -> {
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

  private static void goForIt(final Vertx vertx) {
    CompositeFuture
        .all(
//            deployVerticle(vertx, RainMaker.class.getName()),
//            deployVerticle(vertx, RainMaker.class.getName()),
//            deployVerticle(vertx, RainMaker.class.getName()),
            deployVerticle(vertx, RainMaker.class.getName()),
            deployVerticle(vertx, RainIntensityMonitor.class.getName()),
            deployVerticle(vertx, RainServer.class.getName())
        )
        .setHandler(result -> {
          if (result.succeeded()) {
            vertx.eventBus().publish(RainConstants.RAIN_INTENSITY_SET_ADDRESS,
                new JsonObject().put(RainConstants.VALUE_KEY, 0.0));
          } else {
            LOG.error("There won't be any rain today...", result.cause());
          }
        });

//    vertx.eventBus().addInterceptor(context -> LOG.debug("EVENT '{}' = {}", context.message().address(), context.message().body()));

//    vertx.setTimer(30000, timerId -> {
//      vertx.close();
//      LOG.info("And... it's gone!");
//    });
  }

  private static Future<Void> deployVerticle(final Vertx vertx, final String verticleName) {
    final Future<Void> result = Future.future();
    vertx.deployVerticle(verticleName, deployResult -> {
      if (deployResult.succeeded()) {
        result.complete();
      } else {
        result.fail(deployResult.cause());
      }
    });
    return result;
  }
}
