package nl.bransom.reactive;

import io.vertx.core.VertxOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.rxjava.core.CompositeFuture;
import io.vertx.rxjava.core.Future;
import io.vertx.rxjava.core.Vertx;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {

  private static final Logger LOG = LoggerFactory.getLogger(Main.class);

  public static void main(String[] args) {
    if (RainConstants.CLUSTERED) {
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

  private static void goForIt(final Vertx vertx) {
    final Future<String> whenRainMakerIsDeployed = Future.future();
    final Future<String> whenRainIntensityMonitorIsDeployed = Future.future();
    final Future<String> whenRainServerIsListening = Future.future();

    vertx.deployVerticle(RainMaker.class.getName(), whenRainMakerIsDeployed.completer());
    vertx.deployVerticle(RainIntensityMonitor.class.getName(), whenRainIntensityMonitorIsDeployed.completer());
    vertx.deployVerticle(RainServer.class.getName(), whenRainServerIsListening.completer());

    CompositeFuture.all(whenRainMakerIsDeployed, whenRainIntensityMonitorIsDeployed, whenRainServerIsListening)
        .setHandler(result -> {
          if (result.succeeded()) {
            vertx.eventBus().publish(RainConstants.MSG_RAIN_INTENSITY_SET,
                new JsonObject().put(RainConstants.INTENSITY_KEY, 0.0));
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
