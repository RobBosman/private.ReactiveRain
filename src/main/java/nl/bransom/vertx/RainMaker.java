package nl.bransom.vertx;

import io.vertx.rxjava.core.AbstractVerticle;
import io.vertx.rxjava.core.eventbus.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;
import rx.Subscriber;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class RainMaker extends AbstractVerticle {

  public static final String INTENSITY_TAG = "RainMaker.intensity";

  private static final Logger LOG = LoggerFactory.getLogger(RainMaker.class);
  private static final long MAX_INTERVAL_MILLIS = 3000;

  private final Random random;
  private Long delayTimerID;
  private Long intervalMillis;

  public RainMaker() {
    random = new Random();
  }

  @Override
  public void start() {
    final List<RainDrop> rainDrops = new ArrayList<>();

    vertx.eventBus()
        .<Double>consumer(INTENSITY_TAG)
        .toObservable()
        .doOnNext(message -> message.reply("OK"))
        .map(Message::body)
        .map(RainMaker::intensityToAverageIntervalMillis)
        .doOnNext(intervalMillis -> {
          if (delayTimerID != null) {
            vertx.cancelTimer(delayTimerID);
            delayTimerID = null;
          }
        })
        .doOnNext(intervalMillis -> {
          this.intervalMillis = intervalMillis;
          LOG.info("intervalMillis: " + intervalMillis);
        })
        .flatMap(intervalMillis -> Observable.<RainDrop>create(this::createDelayedRainDrop))
        .doOnNext(rainDrops::add)
        .subscribe(rainDrop -> LOG.info("\t" + rainDrop));
  }

  private static long intensityToAverageIntervalMillis(final double intensity) {
    return Math.round((1.0 - intensity) * MAX_INTERVAL_MILLIS);
  }

  private long sampleDelayMillis() {
    return  Math.max(1, Math.round(random.nextGaussian() * intervalMillis));
  }

  private void createDelayedRainDrop(final Subscriber<? super RainDrop> subscriber) {
    if (intervalMillis < MAX_INTERVAL_MILLIS) {
      delayTimerID = vertx.setTimer(sampleDelayMillis(), timerId -> {
        if (!subscriber.isUnsubscribed()) {
          subscriber.onNext(new RainDrop(random));
          createDelayedRainDrop(subscriber);
        }
      });
    }
  }
}
