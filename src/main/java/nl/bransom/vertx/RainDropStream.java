package nl.bransom.vertx;

import io.vertx.core.Handler;
import io.vertx.core.streams.ReadStream;

public class RainDropStream implements ReadStream<RainDrop> {

  @Override
  public ReadStream<RainDrop> exceptionHandler(Handler<Throwable> handler) {
    return this;
  }

  @Override
  public ReadStream<RainDrop> handler(Handler<RainDrop> handler) {
    return this;
  }

  @Override
  public ReadStream<RainDrop> pause() {
    return this;
  }

  @Override
  public ReadStream<RainDrop> resume() {
    return this;
  }

  @Override
  public ReadStream<RainDrop> endHandler(Handler<Void> handler) {
    return this;
  }

  public ReadStream<RainDrop> add(RainDrop rainDrop) {
    return this;
  }
}
