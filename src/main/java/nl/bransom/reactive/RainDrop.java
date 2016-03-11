package nl.bransom.reactive;

import io.vertx.core.json.JsonObject;

import java.util.Random;

public class RainDrop {

  private static int count;

  private final float x;
  private final float y;

  public RainDrop() {
    final Random random = new Random();
    x = random.nextFloat();
    y = random.nextFloat();
    count++;
  }

  public JsonObject toJson() {
    return new JsonObject()
        .put("x", x)
        .put("y", y)
        .put("count", count);
  }
}
