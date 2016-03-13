package nl.bransom.reactive;

import io.vertx.core.json.JsonObject;

import java.util.Random;

class RainDrop {

  private final float x;
  private final float y;

  public RainDrop() {
    final Random random = new Random();
    x = random.nextFloat();
    y = random.nextFloat();
  }

  public JsonObject toJson() {
    return new JsonObject()
        .put("x", x)
        .put("y", y);
  }
}
