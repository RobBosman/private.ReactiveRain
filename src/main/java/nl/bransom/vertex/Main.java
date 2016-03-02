package nl.bransom.vertex;

import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.eventbus.EventBus;
import io.vertx.rx.java.ObservableFuture;
import io.vertx.rx.java.RxHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {

  private static final boolean CLUSTERED = false;

  private static final Logger LOG = LoggerFactory.getLogger(Main.class);

  public static void main(String[] args) {
    handleThat(42, i -> LOG.info("" + i));

    if (CLUSTERED) {
      final VertxOptions options = new VertxOptions();
      Vertx.clusteredVertx(options, res -> {
        if (res.succeeded()) {
          doSomething(res.result());
        } else {
          LOG.error("Error getting clustered vertx.", res.cause());
        }
      });
    } else {
      doSomething(Vertx.vertx());
    }
  }

  public static void doSomething(final Vertx vertx) {
    vertx.setTimer(10000, timerId -> {
      vertx.close();
      LOG.info("...and it's gone");
    });

    vertx.setPeriodic(2000, timerId -> {
      final EventBus eb = vertx.eventBus();
      eb.publish("news.uk.sport", "Yay! Someone kicked a ball");
      eb.send("news.uk.sport", "Yay! Someone kicked a ball across a patch of grass", res -> {
        if (res.succeeded()) {
          LOG.info("received reply: " + res.result().body());
        } else {
          LOG.warn("Error sending message: " + res.cause().getMessage());
        }
      });
    });

    final ObservableFuture<String> observableDeploymentId = RxHelper.observableFuture();

    observableDeploymentId.subscribe(value -> {
      final String deploymentId = value;
      LOG.debug("Deployment id from result is: " + deploymentId);
      vertx.setTimer(5000, timerId ->
          vertx.undeploy(deploymentId, res2 -> {
            if (res2.succeeded()) {
              LOG.info("shut down " + deploymentId + " OK");
            } else {
              LOG.error("shut down " + deploymentId + " failed", res2.cause());
            }
          })
      );
    });

    vertx.deployVerticle(MyVerticle.class.getName(), res -> {
      if (res.succeeded()) {
        observableDeploymentId.toHandler().handle(res);
        final String deploymentId = res.result();
        LOG.info("Deployment id is: " + deploymentId);
      } else {
        LOG.error("Deployment failed.", res.cause());
      }
    });
  }

  public static <T> void handleThat(final T t, final Handler<T> handler) {
    handler.handle(t);
  }

  @FunctionalInterface
  public interface Handler<E> {
    void handle(E var1);
  }
}
