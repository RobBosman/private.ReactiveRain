package nl.bransom.reactive;

import io.vertx.core.json.JsonObject;

import java.util.Random;

class RainDrop {

  private static final Random RANDOM = new Random();

  private final float x;
  private final float y;

  public RainDrop() {
    x = RANDOM.nextFloat();
    y = RANDOM.nextFloat();
  }

  public float getX() {
    return x;
  }

  public float getY() {
    return y;
  }

  public JsonObject toJson() {
    return new JsonObject()
        .put("x", getX())
        .put("y", getY());
  }
}
