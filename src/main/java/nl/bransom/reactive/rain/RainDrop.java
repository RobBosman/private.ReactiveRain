package nl.bransom.reactive.rain;

import io.vertx.core.json.JsonObject;

class RainDrop implements RainConstants {

  private final float x;
  private final float y;

  RainDrop() {
    x = RANDOM.nextFloat();
    y = RANDOM.nextFloat();
  }

  JsonObject toJson() {
    return new JsonObject()
        .put("x", x)
        .put("y", y);
  }
}
