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

  public static void startRaining(final Vertx vertx, final Future<Void> result) {
    vertx.eventBus().send("RainMaker.intervalMillis", RainMaker.START, Main.handleFuture(result));
  }

  public static void stopRaining(final Vertx vertx) {
    vertx.eventBus().publish("RainMaker.intervalMillis", RainMaker.STOP);
  }

  public static final int START = 1;
  public static final int STOP = 0;

  @Override
  public void start() {
    final Random random = new Random();
    final List<RainDrop> rainDrops = new ArrayList<>();

    final Subscriber<RainDrop> rds = Subscribers.create(rainDrop -> LOG.info("\t" + rainDrop));

    vertx.eventBus()
        .<Integer>consumer("RainMaker.intervalMillis")
        .toObservable()
        .doOnNext(message -> message.reply("OK"))
        .map(Message::body)
        .subscribe(intervalMillis -> {
          LOG.debug("RainMaker.intervalMillis = " + intervalMillis);
          if (intervalMillis > 0) {
            Observable.<RainDrop>create(subscriber ->
                vertx.setPeriodic(intervalMillis, timerId -> {
                  if (subscriber.isUnsubscribed()) {
                    vertx.cancelTimer(timerId);
                  } else {
                    subscriber.onNext(new RainDrop(random));
                  }
                }))
                .doOnNext(rainDrops::add)
                .subscribe(rds);
          } else {
            rds.unsubscribe();
            LOG.info("Total RainDrops: " + rainDrops.size());
          }
        });
  }
}
