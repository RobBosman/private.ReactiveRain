package nl.bransom.reactive;

import io.vertx.core.Future;
import io.vertx.ext.web.handler.sockjs.BridgeOptions;
import io.vertx.ext.web.handler.sockjs.PermittedOptions;
import io.vertx.rxjava.core.AbstractVerticle;
import io.vertx.rxjava.ext.web.Router;
import io.vertx.rxjava.ext.web.handler.sockjs.SockJSHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RainServer extends AbstractVerticle implements RainConstants {

  private static final Logger LOG = LoggerFactory.getLogger(RainServer.class);

  @Override
  public void start(final Future<Void> startFuture) {

    final Router router = Router.router(vertx);
    router.route("/")
        .handler(routingContext -> routingContext.reroute("/rain"));
    router.route("/rain")
        .handler(routingContext -> routingContext.response().sendFile("webroot/index.html"));
    router.route("/rain/js/*")
        .handler(routingContext -> routingContext.response().sendFile("webroot/js/vertx-eventbus-3.2.1.js"));

    final SockJSHandler sockJSHandler = SockJSHandler.create(vertx);
    sockJSHandler.bridge(new BridgeOptions()
        .addInboundPermitted(new PermittedOptions().setAddress(RAIN_MAKER_ADDRESS))
        .addOutboundPermitted(new PermittedOptions().setAddress(RAIN_DROP_ADDRESS)));
    router.route("/rain/eventbus/*").handler(sockJSHandler);

    vertx.createHttpServer()
        .requestHandler(router::accept)
        .listen(8080, res -> {
          if (res.succeeded()) {
            LOG.info("Server is now listening on http://localhost:8080/");
            startFuture.complete();
          } else {
            startFuture.fail(res.cause());
          }
        });
  }
}
