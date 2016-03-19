"use strict";

var tileWithRainDrops = new TileWithRainDrops();
var rainIntensity = new RainIntensity();
var randomnessStatistics = new RandomnessStatistics();
var piApproximation = new PiApproximation();

window.onload = rainIntensity.initializeSlider;

var eventBus = new EventBus('/eventbus');
var isEventBusOpen = false;

eventBus.onopen = function() {
  eventBus.registerHandler('rain.drop.notify', tileWithRainDrops.drawRainDrop);

  eventBus.registerHandler('rain.drop.notify', randomnessStatistics.updateDropCounter);
  eventBus.registerHandler('rain.drop.notify', randomnessStatistics.updateAverageX);
  eventBus.registerHandler('rain.drop.notify', randomnessStatistics.updateAverageY);
  eventBus.registerHandler('rain.drop.notify', randomnessStatistics.updateCorrelation);

  eventBus.registerHandler('rain.drop.notify', piApproximation.updatePi);

  eventBus.registerHandler('rain.intensity.set', '', rainIntensity.updateRainIntensity);
  eventBus.send('rain.intensity.get', '', rainIntensity.updateRainIntensity);
  isEventBusOpen = true;
};