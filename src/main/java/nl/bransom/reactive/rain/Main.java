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
    final Future<String> whenRainMakerIsDeployed = Future.future();
    final Future<String> whenRainIntensityMonitorIsDeployed = Future.future();
    final Future<String> whenRainServerIsListening = Future.future();

//    vertx.deployVerticle(RainMaker.class.getName());
//    vertx.deployVerticle(RainMaker.class.getName());
//    vertx.deployVerticle(RainMaker.class.getName());
//    vertx.deployVerticle(RainMaker.class.getName());

    vertx.deployVerticle(RainMaker.class.getName(), whenRainMakerIsDeployed.completer());
    vertx.deployVerticle(RainIntensityMonitor.class.getName(), whenRainIntensityMonitorIsDeployed.completer());
    vertx.deployVerticle(RainServer.class.getName(), whenRainServerIsListening.completer());

    CompositeFuture.all(whenRainMakerIsDeployed, whenRainIntensityMonitorIsDeployed, whenRainServerIsListening)
        .setHandler(result -> {
          if (result.succeeded()) {
            vertx.eventBus().publish(RainConstants.RAIN_INTENSITY_SET_MSG,
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
}
