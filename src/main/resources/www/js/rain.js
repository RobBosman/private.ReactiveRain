"use strict";

var SVG_NS = "http://www.w3.org/2000/svg";
var DROP_TIME_TO_LIVE = 2000;

var intensitySlider;
var tileSvg;
var isDOMReady = false;
window.onload = function() {
  tileSvg = document.getElementById("tile");
  intensitySlider = document.getElementById('rain-intensity');
  noUiSlider.create(intensitySlider, {
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
  intensitySlider.noUiSlider.on('update', function(values, handle) {
    if (isDOMReady && isEventBusOpen) {
      eventBus.send('RainMaker', {intensity: (values[handle] / 100.0)});
    }
  });
  isDOMReady = true;
}

var eventBus = new EventBus('/eventbus');
var isEventBusOpen = false;
eventBus.onopen = function() {
  eventBus.registerHandler('RainDrop', drawRainDrop);
  eventBus.registerHandler('RainDrop', updateDropCounter);
  eventBus.registerHandler('RainDrop', updatePi);
  eventBus.registerHandler('RainDrop', updateAverageX);
  eventBus.registerHandler('RainDrop', updateAverageY);
  eventBus.registerHandler('RainDrop', updateCorrelation);
  isEventBusOpen = true;
}

function drawRainDrop(err, msg) {
  var drop = document.createElementNS(SVG_NS, "circle");
  drop.setAttribute("r", "2%");
  drop.setAttribute("cx", 100.0 * msg.body.x + "%");
  drop.setAttribute("cy", 100.0 * msg.body.y + "%");
  tileSvg.appendChild(drop);

  setTimeout(function () {
    tileSvg.removeChild(drop);
  }, DROP_TIME_TO_LIVE);
}

var numRainDrops = 0;
function updateDropCounter(err, msg) {
  numRainDrops++;
}

var numWithinRadius = 0;
function updatePi(err, msg) {
  var rainDrop = msg.body;
  if (rainDrop.x * rainDrop.x + rainDrop.y * rainDrop.y <= 1.0) {
    numWithinRadius++;
  }
  document.getElementById('num-drops').innerHTML = numRainDrops;
  document.getElementById('num-within-radius').innerHTML = numWithinRadius;
  document.getElementById('pi').innerHTML = (4.0 * numWithinRadius / numRainDrops).toFixed(16);
}

var sumX = 0.0;
function updateAverageX(err, msg) {
  sumX += msg.body.x;
  document.getElementById('average-x').innerHTML = (sumX / numRainDrops).toFixed(16);
}

var sumY = 0.0;
function updateAverageY(err, msg) {
  sumY += msg.body.x;
  document.getElementById('average-y').innerHTML = (sumY / numRainDrops).toFixed(16);
}

var sumDeviationXY = 0.0;
var sumSquaredDeviationX = 0.0;
var sumSquaredDeviationY = 0.0;
function updateCorrelation(err, msg) {
  var rainDrop = msg.body;
  var averageX = sumX / numRainDrops;
  var averageY = sumY / numRainDrops;
  var deviationX = rainDrop.x - averageX;
  var deviationY = rainDrop.y - averageY;
  sumDeviationXY += deviationX * deviationY;
  sumSquaredDeviationX += deviationX * deviationX;
  sumSquaredDeviationY += deviationY * deviationY;
  if (sumSquaredDeviationX * sumSquaredDeviationY != 0.0) {
    var correlationCoefficient = sumDeviationXY / Math.sqrt(sumSquaredDeviationX * sumSquaredDeviationY);
    document.getElementById('correlation-coefficient').innerHTML = correlationCoefficient.toFixed(16);
  }
}
