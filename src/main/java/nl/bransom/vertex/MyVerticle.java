package nl.bransom.vertex;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.shareddata.AsyncMap;
import io.vertx.core.shareddata.LocalMap;
import io.vertx.core.shareddata.SharedData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MyVerticle extends AbstractVerticle {
  
  private static final Logger LOG = LoggerFactory.getLogger(MyVerticle.class);

  @Override
  public void start(final Future<Void> startFuture) {
    LOG.info(getClass().getName() + " - deployed");

    if (vertx.isClustered()) {
      putCvJsonOnClusterWideMap(vertx, "JSON DATA CLUSTERED");
      vertx.setTimer(100, timerId -> getCvJsonFromClusterWideMap(vertx));
    }

    putCvJsonOnLocalSharedMap(vertx, "JSON DATA LOCAL");
    String jsonData = getCvJsonFromLocalSharedMap(vertx);
    dealWithCvJson(jsonData);

    vertx.eventBus()
        .consumer("news.uk.sport", message -> {
          LOG.info("received message: " + message.body());
          message.reply("how interesting!");
        });

    vertx.createHttpServer()
        .requestHandler(request -> request
              .handler(buffer -> LOG.debug("received some bytes: " + buffer.toString()))
              .bodyHandler(totalBuffer -> LOG.debug("Full body received, path = " + request.path() + ", length = " + totalBuffer.length()))
              .response().end("Hello world"))
        .listen(8080, "localhost", res -> {
          if (res.succeeded()) {
            LOG.info("Server is now listening on http://localhost:8080/");
            startFuture.complete();
          } else {
            LOG.error("Failed to bind!", res.cause());
            startFuture.fail(res.cause());
          }
        });
  }

  @Override
  public void stop() {
    LOG.info(getClass().getName() + " - undeployed");
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
}
