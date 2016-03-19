package nl.bransom.reactive;

import io.vertx.core.json.JsonObject;
import io.vertx.rxjava.core.AbstractVerticle;
import io.vertx.rxjava.core.eventbus.Message;

public class RainIntensityMonitor extends AbstractVerticle {

  @Override
  public void start() {
    vertx.eventBus()
        .<JsonObject>consumer(RainConstants.MSG_RAIN_INTENSITY_GET)
        .toObservable()
        .withLatestFrom(vertx.eventBus()
                .<JsonObject>consumer(RainConstants.MSG_RAIN_INTENSITY_SET)
                .toObservable()
                .map(Message::body)
                .map(jsonObject -> jsonObject.getDouble(RainConstants.INTENSITY_KEY)),
            (message, intensity) -> {
              message.reply(new JsonObject().put(RainConstants.INTENSITY_KEY, intensity));
              return null;
            })
        .subscribe();
  }
}
