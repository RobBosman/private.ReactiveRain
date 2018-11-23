package nl.bransom.reactive.rain;

import io.vertx.core.Future;
import io.vertx.ext.bridge.PermittedOptions;
import io.vertx.ext.web.handler.sockjs.BridgeOptions;
import io.vertx.rxjava.core.AbstractVerticle;
import io.vertx.rxjava.ext.web.Router;
import io.vertx.rxjava.ext.web.handler.StaticHandler;
import io.vertx.rxjava.ext.web.handler.sockjs.SockJSHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RainServer extends AbstractVerticle {

  private static final Logger LOG = LoggerFactory.getLogger(RainServer.class);

  @Override
  public void start(final Future<Void> futureResult) {

    final Router router = Router.router(vertx);

    router.route("/eventbus/*")
        .handler(SockJSHandler.create(vertx)
            .bridge(new BridgeOptions()
                .addInboundPermitted(new PermittedOptions().setAddress(RainConstants.RAIN_INTENSITY_GET_ADDRESS))
                .addInboundPermitted(new PermittedOptions().setAddress(RainConstants.RAIN_INTENSITY_SET_ADDRESS))
                .addOutboundPermitted(new PermittedOptions().setAddress(RainConstants.RAIN_DROP_NOTIFY_ADDRESS))
                .addOutboundPermitted(new PermittedOptions().setAddress(RainConstants.RAIN_INTENSITY_SET_ADDRESS))));

    router.route()
        .handler(StaticHandler.create("www").setIndexPage("rain.html"));

    vertx.createHttpServer()
        .requestHandler(router::accept)
        .listen(RainConstants.SERVER_PORT, result -> {
          if (result.succeeded()) {
            LOG.info("Server is now listening on http://localhost:{}/", RainConstants.SERVER_PORT);
            futureResult.complete();
          } else {
            futureResult.fail(result.cause());
          }
        });
  }
}
