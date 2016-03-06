package nl.bransom.vertx;

import java.util.Random;

public class RainDrop {

  private final float x;
  private final float y;

  public RainDrop(final Random random) {
    x = random.nextFloat();
    y = random.nextFloat();
  }

  public float getX() {
    return x;
  }

  public float getY() {
    return y;
  }

  @Override
  public String toString() {
    return "RainDrop(" + x + ", " + y + ")";
  }
}
