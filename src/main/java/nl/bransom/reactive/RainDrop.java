package nl.bransom.reactive;

import io.vertx.core.json.JsonObject;

class RainDrop implements RainConstants {

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
