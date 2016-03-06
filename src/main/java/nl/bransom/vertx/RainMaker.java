package nl.bransom.vertx;

import io.vertx.core.Future;
import io.vertx.rxjava.core.AbstractVerticle;
import io.vertx.rxjava.core.Vertx;
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
    vertx.eventBus().send("RainMaker", RainMaker.START, Main.handleFuture(result));
  }

  public static void stopRaining(final Vertx vertx) {
    vertx.eventBus().publish("RainMaker", RainMaker.STOP);
  }

  public static final String START = "start";
  public static final String STOP = "stop";

  @Override
  public void start() {
    final Random random = new Random();
    final List<RainDrop> rainDrops = new ArrayList<>();

    final Observable<RainDrop> observableRainDrop = vertx.periodicStream(100)
        .toObservable()
        .map(timerId -> new RainDrop(random))
        .doOnNext(rainDrops::add);

    final Subscriber<RainDrop> rds = Subscribers.create(rainDrop -> LOG.info("\t" + rainDrop));

    vertx.eventBus()
        .consumer("RainMaker", message -> {
          if (message.body().equals(START)) {
            LOG.info("received message: " + message.body());
            observableRainDrop.subscribe(rds);
            message.reply("OK");
          } else if (message.body().equals(STOP)) {
            LOG.info("received message: " + message.body());
            rds.unsubscribe();
            LOG.info("Total RainDrops: " + rainDrops.size());
            message.reply("OK");
          }
        });
  }
}
