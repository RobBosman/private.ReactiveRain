package nl.bransom.reactive;

import io.vertx.core.json.JsonObject;
import io.vertx.rxjava.core.AbstractVerticle;
import io.vertx.rxjava.core.eventbus.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;

public class RainIntensityMonitor extends AbstractVerticle {

  private static final Logger LOG = LoggerFactory.getLogger(RainIntensityMonitor.class);

  @Override
  public void start() {
    final Observable<Double> intensityObservable = vertx.eventBus()
        .<JsonObject>consumer(RainConstants.MSG_RAIN_INTENSITY_SET)
        .toObservable()
        .map(Message::body)
        .map(jsonObject -> jsonObject.getDouble(RainConstants.INTENSITY_KEY));

    vertx.eventBus()
        .<JsonObject>consumer(RainConstants.MSG_RAIN_INTENSITY_GET)
        .toObservable()
        .withLatestFrom(intensityObservable, (message, intensity) -> {
          message.reply(new JsonObject().put(RainConstants.INTENSITY_KEY, intensity));
          return null;
        })
        .subscribe();
  }
}
