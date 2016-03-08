package nl.bransom.vertx;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;

public class RainDrop {

  private static final Logger LOG = LoggerFactory.getLogger(RainDrop.class);

  private final float x;
  private final float y;

  public RainDrop(final Random random) {
    x = random.nextFloat();
    y = random.nextFloat();
    LOG.debug("Created " + this);
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
