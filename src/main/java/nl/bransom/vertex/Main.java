package nl.bransom.vertex;

import io.vertx.core.Vertx;

/**
 * Created by robbo on 21-02-2016.
 */
public class Main {

  public static void main(String[] args) {
    Vertx vertx = Vertx.vertx();

    vertx.setTimer(60000, timerId -> {
      vertx.close();
      System.out.println("...and it's gone");
    });

    vertx.deployVerticle("nl.bransom.vertex.MyVerticle", res1 -> {
      if (res1.succeeded()) {
        String deploymentId = res1.result();
        System.out.println("Deployment id is: " + deploymentId);

        vertx.setTimer(4000, timerId -> {
          vertx.undeploy(deploymentId, res2 -> {
            if (res2.succeeded()) {
              System.out.println("shut down OK");
            } else {
              System.out.println("shut down failed");
            }
          });
        });
      } else {
        System.out.println("Deployment failed!");
      }
    });
  }
}
