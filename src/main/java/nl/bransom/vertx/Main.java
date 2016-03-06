package nl.bransom.vertx;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
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

    final Future<String> startRainMakerResult = Future.future();
    vertx.deployVerticle(RainMaker.class.getName(), handleFuture(startRainMakerResult));

    final Future<Void> startRainingResult = Future.future();
    vertx.deployVerticleObservable(RainServer.class.getName())
        .subscribe(deploymentId -> startRaining(vertx, startRainingResult));

    startRainingResult.setHandler(res -> {
      if (res.succeeded()) {
        LOG.info("Started raining");
        vertx.setTimer(3000, timerId -> stopRaining(vertx));
      } else {
        LOG.error("Error starting rain: ", res.cause());
      }
    });
  }

  private static void startRaining(final Vertx vertx, final Future<Void> result) {
    vertx.eventBus().send("RainMaker", RainMaker.START, handleFuture(result));
  }

  private static void stopRaining(final Vertx vertx) {
    vertx.eventBus().publish("RainMaker", RainMaker.STOP);
  }

  public static <T,R> Handler<AsyncResult<R>> handleFuture(final Future<T> futureResult) {
    return res -> {
      if (res.succeeded()) {
        futureResult.complete();
      } else {
        futureResult.fail(res.cause());
      }
    };
  }
}
