package nl.bransom.reactive;

import io.vertx.core.json.Json;
import io.vertx.rxjava.core.AbstractVerticle;
import io.vertx.rxjava.core.eventbus.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;
import rx.Subscriber;

import java.util.Random;

public class RainMaker extends AbstractVerticle {

  private static final Logger LOG = LoggerFactory.getLogger(RainMaker.class);
  private static final long MAX_INTERVAL_MILLIS = 3000;

  public static final String INTENSITY_MSG = "RainMaker.intensity";
  public static final String RAIN_DROP_MSG = "RainDrop";

  @Override
  public void start() {
    vertx.eventBus()
        .<Double>consumer(INTENSITY_MSG)
        .toObservable()
        .doOnNext(message -> message.reply("OK"))
        .map(Message::body)
        .map(this::intensityToIntervalMillis)
        .switchMap(this::createRainDrops)
        .subscribe(rainDrop -> vertx.eventBus().send(RAIN_DROP_MSG, Json.encode(rainDrop)));
  }

  private long intensityToIntervalMillis(final double intensity) {
    LOG.debug("intensity: {}", intensity);
    return Math.round((1.0 - intensity) * MAX_INTERVAL_MILLIS);
  }

  private long sampleDelayMillis(final long intervalMillis) {
    final Random random = new Random();
    final long delayMillis = Math.max(1, Math.round(2.0 * random.nextDouble() * intervalMillis));
    LOG.debug("delayMillis: {}", delayMillis);
    return delayMillis;
  }

  private Observable<? extends RainDrop> createRainDrops(final long intervalMillis) {
    LOG.debug("intervalMillis: {}", intervalMillis);
    if (intervalMillis < MAX_INTERVAL_MILLIS) {
      return Observable.<RainDrop>create(subscriber -> createDelayedRainDrop(intervalMillis, subscriber));
    } else {
      return Observable.empty();
    }
  }

  private void createDelayedRainDrop(final long intervalMillis, final Subscriber<? super RainDrop> subscriber) {
    vertx.setTimer(sampleDelayMillis(intervalMillis), timerId -> {
      if (!subscriber.isUnsubscribed()) {
        subscriber.onNext(new RainDrop());
        createDelayedRainDrop(intervalMillis, subscriber);
      }
    });
  }
}
