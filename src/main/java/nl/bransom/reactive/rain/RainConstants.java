package nl.bransom.reactive.rain;

import java.security.SecureRandom;
import java.util.Random;

interface RainConstants {

  int SERVER_PORT = 8080;

  boolean CLUSTERED = false;

  Random RANDOM = new SecureRandom();

  long MAX_INTERVAL_MILLIS = 3000;

  String RAIN_INTENSITY_SET_MSG = "rain.intensity.set";
  String RAIN_INTENSITY_GET_MSG = "rain.intensity.get";
  String RAIN_DROP_NOTIFY_MSG = "rain.drop.notify";

  String VALUE_KEY = "value";
}
