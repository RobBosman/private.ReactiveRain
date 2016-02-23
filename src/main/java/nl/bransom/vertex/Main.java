package nl.bransom.vertex;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.EventBus;

/**
 * Created by robbo on 21-02-2016.
 */
public class Main {

  public static void main(String[] args) {
    Vertx vertx = Vertx.vertx();

    vertx.setTimer(10000, timerId -> {
      vertx.close();
      System.out.println("...and it's gone");
    });

    vertx.setPeriodic(2000, timerId -> {
      final EventBus eb = vertx.eventBus();
      eb.publish("news.uk.sport", "Yay! Someone kicked a ball");
      eb.send("news.uk.sport", "Yay! Someone kicked a ball across a patch of grass", ar -> {
        if (ar.succeeded()) {
          System.out.println("received reply: " + ar.result().body());
        }
      });
    });

    final Future<String> deploymentId = Future.future();
    deploymentId.compose(s -> {
      System.out.println("This id the future, man!");

      vertx.setTimer(6000, timerId ->
          vertx.undeploy(deploymentId.result(), res2 -> {
            if (res2.succeeded()) {
              System.out.println("shut down OK");
            } else {
              System.out.println("shut down failed");
            }
          })
      );
    }, null);

    vertx.deployVerticle(MyVerticle.class.getName(), res1 -> {
      if (res1.succeeded()) {
        deploymentId.complete(res1.result());
        System.out.println("Deployment id is: " + deploymentId);
      } else {
        System.out.println("Deployment failed!");
      }
    });
  }
}
