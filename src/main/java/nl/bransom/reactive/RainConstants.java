package nl.bransom.reactive;

import java.util.Random;

interface RainConstants {

  Random RANDOM = new Random();

  String RAIN_MAKER_ADDRESS = "RainMaker";

  String RAIN_DROP_ADDRESS = "RainDrop";

  String INTENSITY_KEY = "intensity";

  int SERVER_PORT = 8080;

  long MAX_INTERVAL_MILLIS = 3000;

  boolean CLUSTERED = false;
}
