package nl.bransom.vertex;

import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.shareddata.AsyncMap;
import io.vertx.core.shareddata.LocalMap;
import io.vertx.core.shareddata.SharedData;
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
          final Vertx vertx = res.result();

          doVertex(vertx);

          putCvJsonOnClusterWideMap(vertx, "JSON DATA CLUSTERED");
          vertx.setTimer(100, timerId -> getCvJsonFromClusterWideMap(vertx));
        } else {
          LOG.error("Error getting clustered vertx.", res.cause());
        }
      });
    } else {
      doVertex(Vertx.vertx());
    }
  }

  public static void doVertex(final Vertx vertx) {
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

    vertx.deployVerticle(MyVerticle.class.getName(), res -> {
      if (res.succeeded()) {
        final String deploymentId = res.result();
        vertx.eventBus().publish("deployed verticle", deploymentId);
        LOG.info("Deployment id is: " + deploymentId);
      } else {
        LOG.error("Deployment failed.", res.cause());
      }
    });

    vertx.eventBus().consumer("deployed verticle", res -> {
      final String deploymentId = (String) res.body();
      LOG.debug("Deployment id from result is: " + deploymentId);
      vertx.setTimer(6000, timerId ->
          vertx.undeploy(deploymentId, res2 -> {
            if (res2.succeeded()) {
              LOG.info("shut down " + deploymentId + " OK");
            } else {
              LOG.error("shut down " + deploymentId + " failed", res2.cause());
            }
          })
      );
    });


    putCvJsonOnLocalSharedMap(vertx, "JSON DATA LOCAL");
    String jsonData = getCvJsonFromLocalSharedMap(vertx);
    dealWithCvJson(jsonData);
  }

  private static void putCvJsonOnLocalSharedMap(final Vertx vertx, final String jsonData) {
    final SharedData sd = vertx.sharedData();
    final LocalMap<String, String> map = sd.<String, String>getLocalMap("cvtool");
    map.put("cv", jsonData);
  }

  private static String getCvJsonFromLocalSharedMap(final Vertx vertx) {
    final SharedData sd = vertx.sharedData();
    final LocalMap<String, String> map = sd.<String, String>getLocalMap("cvtool");
    return map.get("cv");
  }


  private static void putCvJsonOnClusterWideMap(final Vertx vertx, final String jsonData) {
    vertx.sharedData().<String, String>getClusterWideMap("cvtool", res -> {
      if (res.succeeded()) {
        final AsyncMap<String, String> map = res.result();
        map.put("cv", jsonData, h -> {
          if (h.succeeded()) {
            LOG.info("Data was put on cluster wide map");
          } else {
            LOG.error("Putting data on cluster wide map failed!", res.cause());
          }
        });
      } else {
        LOG.error("Getting cluster wide map failed!", res.cause());
      }
    });
  }

  private static void getCvJsonFromClusterWideMap(final Vertx vertx) {
    vertx.sharedData().<String, String>getClusterWideMap("cvtool", res -> {
      if (res.succeeded()) {
        getCvJsonFromMap(res.result());
      } else {
        LOG.error("Getting clustered shared data failed!", res.cause());
      }
    });
  }

  private static void getCvJsonFromMap(final AsyncMap<String, String> map) {
    map.get("cv", res -> {
      if (res.succeeded()) {
        dealWithCvJson(res.result());
      } else {
        LOG.error("Getting data from clustered shared data failed!", res.cause());
      }
    });
  }

  private static void dealWithCvJson(final String cvJson) {
    LOG.info("Got it: " + cvJson);
  }

  public static <T> void handleThat(final T t, final Handler<T> handler) {
    handler.handle(t);
  }

  @FunctionalInterface
  public interface Handler<E> {
    void handle(E var1);
  }
}
