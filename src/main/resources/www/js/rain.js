"use strict";

var SVG_NS = "http://www.w3.org/2000/svg";
var DROP_TIME_TO_LIVE = 2000;

var eventBus = new EventBus('/eventbus');
var rainIntensity = 0.0;
var numRainDrops = 0;
var numWithinRadius = 0;
var sumX = 0.0;
var sumY = 0.0;
var sumDeviationXY = 0.0;
var sumSquaredDeviationX = 0.0;
var sumSquaredDeviationY = 0.0;
var correlationCoefficient = 0.0;

eventBus.onopen = function() {
  var tileSvg = document.createElementNS(SVG_NS, "svg");
  document.getElementById("tile").appendChild(tileSvg);

  eventBus.registerHandler('RainDrop', function (err, msg) {
    var rainDrop = msg.body;
    var drop = document.createElementNS(SVG_NS, "circle");
    drop.setAttribute("r", "2%");
    drop.setAttribute("cx", 100.0 * rainDrop.x + "%");
    drop.setAttribute("cy", 100.0 * rainDrop.y + "%");
    tileSvg.appendChild(drop);

    setTimeout(function () {
      tileSvg.removeChild(drop);
    }, DROP_TIME_TO_LIVE);
  });

  eventBus.registerHandler('RainDrop', updateDropCounter);
}

function setRainIntensity(newIntensity) {
  rainIntensity = Math.min(Math.max(0.0, newIntensity), 1.0);
  document.getElementById('rain-intensity').innerHTML = Math.round(100.0 * rainIntensity) + "%";
  eventBus.send('RainMaker', {intensity: rainIntensity});
}


function updateDropCounter(err, msg) {
  var rainDrop = msg.body;

  numRainDrops++;
  if (rainDrop.x * rainDrop.x + rainDrop.y * rainDrop.y <= 1.0) {
    numWithinRadius++;
  }
  sumX += rainDrop.x;
  sumY += rainDrop.y;
  var averageX = sumX / numRainDrops;
  var averageY = sumY / numRainDrops;
  var deviationX = rainDrop.x - averageX;
  var deviationY = rainDrop.y - averageY;
  sumSquaredDeviationX += deviationX * deviationX;
  sumSquaredDeviationY += deviationY * deviationY;
  sumDeviationXY += deviationX * deviationY;
  if (sumSquaredDeviationX * sumSquaredDeviationY != 0.0) {
    correlationCoefficient = sumDeviationXY / Math.sqrt(sumSquaredDeviationX * sumSquaredDeviationY);
  }

  document.getElementById('num-drops').innerHTML = numRainDrops;
  document.getElementById('num-within-radius').innerHTML = numWithinRadius;
  document.getElementById('pi').innerHTML = 4.0 * numWithinRadius / numRainDrops;
  document.getElementById('average-x').innerHTML = averageX;
  document.getElementById('average-y').innerHTML = averageY;
  document.getElementById('correlation-coefficient').innerHTML = correlationCoefficient;
}
