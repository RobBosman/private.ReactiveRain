package nl.bransom.reactive;

import io.vertx.core.json.JsonObject;
import io.vertx.rxjava.core.AbstractVerticle;
import io.vertx.rxjava.core.eventbus.Message;

public class RainIntensityMonitor extends AbstractVerticle {

  @Override
  public void start() {
    vertx.eventBus()
        .<JsonObject>consumer(RainConstants.RAIN_INTENSITY_GET_MSG)
        .toObservable()
        .withLatestFrom(vertx.eventBus()
                .<JsonObject>consumer(RainConstants.RAIN_INTENSITY_SET_MSG)
                .toObservable()
                .map(Message::body)
                .map(jsonObject -> jsonObject.getDouble(RainConstants.VALUE_KEY)),
            (requestIntensityMessage, actualIntensity) -> {
              requestIntensityMessage.reply(new JsonObject().put(RainConstants.VALUE_KEY, actualIntensity));
              return null;
            })
        .subscribe();
  }
}
