"use strict";

var SVG_NS = "http://www.w3.org/2000/svg";
var DROP_TIME_TO_LIVE = 2000;

var eventBus = new EventBus('/eventbus');

var isEventBusOpen = false;
var isSliderReady = false;
var isUpdatingSlider = false;
var numRainDrops = 0;
var numWithinRadius = 0;
var sumX = 0.0;
var sumY = 0.0;
var sumDeviationXY = 0.0;
var sumSquaredDeviationX = 0.0;
var sumSquaredDeviationY = 0.0;

window.onload = initSlider;

eventBus.onopen = function() {
  eventBus.registerHandler('rain.drop.notify', drawRainDrop);
  eventBus.registerHandler('rain.drop.notify', updateDropCounter);
  eventBus.registerHandler('rain.drop.notify', updateAverageX);
  eventBus.registerHandler('rain.drop.notify', updateAverageY);
  eventBus.registerHandler('rain.drop.notify', updateCorrelation);
  eventBus.registerHandler('rain.drop.notify', updatePi);
  eventBus.registerHandler('rain.intensity.set', '', updateRainIntensity);

  eventBus.send('rain.intensity.get', '', updateRainIntensity);
  isEventBusOpen = true;
};

function drawRainDrop(err, msg) {
  var tileSvg = document.getElementById("tile");
  var drop = document.createElementNS(SVG_NS, "circle");
  drop.setAttribute("r", "2%");
  drop.setAttribute("cx", 100.0 * msg.body.x + "%");
  drop.setAttribute("cy", 100.0 * msg.body.y + "%");
  tileSvg.appendChild(drop);

  setTimeout(function () {
    tileSvg.removeChild(drop);
  }, DROP_TIME_TO_LIVE);
}

function updateDropCounter(err, msg) {
  numRainDrops++;
}

function updateAverageX(err, msg) {
  sumX += msg.body.x;
  document.getElementById('average-x').innerHTML = (sumX / numRainDrops).toFixed(16);
}

function updateAverageY(err, msg) {
  sumY += msg.body.y;
  document.getElementById('average-y').innerHTML = (sumY / numRainDrops).toFixed(16);
}

function updateCorrelation(err, msg) {
  var averageX = sumX / numRainDrops;
  var averageY = sumY / numRainDrops;
  var deviationX = msg.body.x - averageX;
  var deviationY = msg.body.y - averageY;
  sumDeviationXY += deviationX * deviationY;
  sumSquaredDeviationX += deviationX * deviationX;
  sumSquaredDeviationY += deviationY * deviationY;
  if (sumSquaredDeviationX * sumSquaredDeviationY != 0.0) {
    var correlationCoefficient = sumDeviationXY / Math.sqrt(sumSquaredDeviationX * sumSquaredDeviationY);
    document.getElementById('correlation-coefficient').innerHTML = correlationCoefficient.toFixed(16);
  }
}

function updatePi(err, msg) {
  var rainDrop = msg.body;
  if (rainDrop.x * rainDrop.x + rainDrop.y * rainDrop.y <= 1.0) {
    numWithinRadius++;
  }
  document.getElementById('num-drops').innerHTML = numRainDrops.toFixed(0);
  document.getElementById('num-within-radius').innerHTML = numWithinRadius.toFixed(0);
  document.getElementById('pi').innerHTML = (4.0 * numWithinRadius / numRainDrops).toFixed(16);
}

function initSlider() {
  var rainIntensitySlider = document.getElementById('rain-intensity');
  noUiSlider.create(rainIntensitySlider, {
    start: [0],
    orientation: 'vertical',
    direction: 'rtl',
    range: {
      'min': 0,
      'max': 100
    },
    pips: {
      mode: 'positions',
      values: [0,25,50,75,100],
      density: 4
    }
  });
  rainIntensitySlider.noUiSlider.on('update', function(values, handle) {
    if (isSliderReady && isEventBusOpen && !isUpdatingSlider) {
      eventBus.publish('rain.intensity.set', {intensity: (values[handle] / 100.0)});
    }
  });
  isSliderReady = true;
}

function updateRainIntensity(err, msg) {
  var intensityPercentage = 100.0 * msg.body.intensity;
  document.getElementById('intensity').innerHTML = intensityPercentage.toFixed(0) + "%";
  isUpdatingSlider = true;
  document.getElementById('rain-intensity').noUiSlider.set(intensityPercentage);
  isUpdatingSlider = false;
}
