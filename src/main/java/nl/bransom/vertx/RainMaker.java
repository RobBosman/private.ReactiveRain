package nl.bransom.vertx;

import io.vertx.core.Future;
import io.vertx.rxjava.core.AbstractVerticle;
import io.vertx.rxjava.core.Vertx;
import io.vertx.rxjava.core.eventbus.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;
import rx.Subscriber;
import rx.observers.Subscribers;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class RainMaker extends AbstractVerticle {

  private static final Logger LOG = LoggerFactory.getLogger(RainMaker.class);
  private static final String INTENSITY_TAG = "RainMaker.intensity";
  private static final long MAX_INTERVAL_MILLIS = 3000;

  public static void startRaining(final Vertx vertx, final Future<Void> result) {
    vertx.eventBus().send(INTENSITY_TAG, 1.0, Main.handleFuture(result));
  }

  public static void stopRaining(final Vertx vertx) {
    vertx.eventBus().publish(INTENSITY_TAG, 0.0);
  }

  private final Random random;
  private Long timerID;

  public RainMaker() {
    random = new Random();
  }

  @Override
  public void start() {
    final List<RainDrop> rainDrops = new ArrayList<>();

    final Subscriber<RainDrop> rds = Subscribers.create(rainDrop -> LOG.info("\t" + rainDrop));

    vertx.eventBus()
        .<Double>consumer(INTENSITY_TAG)
        .toObservable()
        .doOnNext(message -> message.reply("OK"))
        .map(Message::body)
        .map(intensity -> (long) ((1.0 - intensity) * MAX_INTERVAL_MILLIS))
        .flatMap(this::createRainDropObservable)
        .doOnNext(rainDrops::add)
        .subscribe(rds);
  }

  private Observable<RainDrop> createRainDropObservable(final long intervalMillis) {
    LOG.info("intervalMillis: " + intervalMillis);
    if (intervalMillis < 0 || intervalMillis >= MAX_INTERVAL_MILLIS) {
      if (timerID != null) {
        vertx.cancelTimer(timerID);
        timerID = null;
      }
      return Observable.empty();
    } else {
      return Observable.<RainDrop>create(subscriber -> {
        final long delay = (intervalMillis > 0 ? intervalMillis : 1L) * 250;
        timerID = vertx.setPeriodic(delay, timerId -> {
          if (subscriber.isUnsubscribed()) {
            vertx.cancelTimer(timerId);
          } else {
            subscriber.onNext(new RainDrop(random));
          }
        });
      });
    }
  }
}
