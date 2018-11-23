package nl.bransom.reactive.rain;

import io.vertx.core.json.JsonObject;
import io.vertx.rxjava.core.AbstractVerticle;
import io.vertx.rxjava.core.eventbus.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;
import rx.Subscriber;

public class RainMaker extends AbstractVerticle {

  private static final Logger LOG = LoggerFactory.getLogger(RainMaker.class);

  private static long intensityToIntervalMillis(final double intensity) {
    final var effectiveIntensity = Math.min(Math.max(0.0, intensity), 1.0);
    LOG.debug("intensity: {}", effectiveIntensity);
    return Math.round(Math.pow(Math.E, Math.log(RainConstants.MAX_INTERVAL_MILLIS) * (1.0 - effectiveIntensity)));
  }

  private static long sampleDelayMillis(final long intervalMillis) {
    return Math.max(1, Math.round(2.0 * RainConstants.RANDOM.nextDouble() * intervalMillis));
  }

  @Override
  public void start() {
    vertx.eventBus()
        .<JsonObject>consumer(RainConstants.RAIN_INTENSITY_SET_ADDRESS)
        .toObservable()
        .map(Message::body)
        .map(jsonObject -> jsonObject.getDouble(RainConstants.VALUE_KEY))
        .map(RainMaker::intensityToIntervalMillis)
        .switchMap(this::createRainDropObservable)
        .map(RainDrop::toJson)
        .subscribe(
            rainDropJson -> vertx.eventBus().publish(RainConstants.RAIN_DROP_NOTIFY_ADDRESS, rainDropJson),
            throwable -> LOG.error("Error making rain.", throwable));
  }

  private Observable<? extends RainDrop> createRainDropObservable(final long intervalMillis) {
    LOG.debug("intervalMillis: {}", intervalMillis);
    if (intervalMillis < RainConstants.MAX_INTERVAL_MILLIS) {
      return Observable.unsafeCreate(subscriber -> createDelayedRainDrop(intervalMillis, subscriber));
    } else {
      return Observable.never();
    }
  }

  private void createDelayedRainDrop(final long intervalMillis, final Subscriber<? super RainDrop> subscriber) {
    final var delayMillis = sampleDelayMillis(intervalMillis);
    vertx.setTimer(delayMillis, timerId -> {
      if (!subscriber.isUnsubscribed()) {
        subscriber.onNext(new RainDrop());
        createDelayedRainDrop(intervalMillis, subscriber);
      }
    });
  }
}
