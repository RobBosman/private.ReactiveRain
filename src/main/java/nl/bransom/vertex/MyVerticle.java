package nl.bransom.vertex;

import io.vertx.core.AbstractVerticle;

/**
 * Created by robbo on 21-02-2016.
 */
public class MyVerticle extends AbstractVerticle {

  @Override
  public void start() {
    System.out.println(getClass().getName() + " - deployed");

    vertx.eventBus().consumer("news.uk.sport")
        .handler(message -> {
          System.out.println("received message: " + message.body());
          message.reply("how interesting!");
        });

    vertx.createHttpServer()
        .requestHandler(request -> {
          request.response().end("Hello world");
//          request.handler(buffer -> System.out.println("received some bytes: " + buffer.toString()));
        })
        .listen(8080, "localhost", res -> {
          if (res.succeeded()) {
            System.out.println("Server is now listening on http://localhost:8080/");
          } else {
            System.out.println("Failed to bind!");
          }
        });
  }

  @Override
  public void stop() {
    System.out.println(getClass().getName() + " - undeployed");
  }
}
