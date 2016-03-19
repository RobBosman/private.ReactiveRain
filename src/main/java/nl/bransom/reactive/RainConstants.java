package nl.bransom.reactive;

import java.util.Random;

interface RainConstants {

  int SERVER_PORT = 8080;

  boolean CLUSTERED = false;

  Random RANDOM = new Random();

  long MAX_INTERVAL_MILLIS = 3000;

  String MSG_RAIN_INTENSITY_SET = "rain.intensity.set";
  String MSG_RAIN_INTENSITY_GET = "rain.intensity.get";
  String MSG_RAIN_DROP_NOTIFY = "rain.drop.notify";

  String INTENSITY_KEY = "intensity";
}
