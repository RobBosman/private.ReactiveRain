"use strict";

var SVG_NS = "http://www.w3.org/2000/svg";
var DROP_TIME_TO_LIVE = 2000;

var eventBus = new EventBus('/eventbus');
var rainIntensity = 0.0;
var numRainDrops = 0;
var totalProductX = 1.0;
var totalProductY = 1.0;
var varianceX = 0.0;
var varianceY = 0.0;
var correlationCoefficient = 0.0;

eventBus.onopen = function() {
  var tileSvg = document.createElementNS(SVG_NS, "svg");
  document.getElementById("tile").appendChild(tileSvg);

  eventBus.registerHandler('RainDrop', function(err, msg) {
    var rainDrop = msg.body;
    var drop = document.createElementNS(SVG_NS, "circle");
    drop.setAttribute("cx", 100.0 * rainDrop.x + "%");
    drop.setAttribute("cy", 100.0 * rainDrop.y + "%");
    tileSvg.appendChild(drop);
    setTimeout(function() { tileSvg.removeChild(drop); }, DROP_TIME_TO_LIVE);
  });

  eventBus.registerHandler('RainDrop', updateDropCounter);
}

function setRainIntensity(newIntensity) {
  rainIntensity = Math.min(Math.max(0.0, newIntensity), 1.0);
  document.getElementById('rain-intensity').innerHTML = "Intensity: " + Math.round(100.0 * rainIntensity) + "%";
  eventBus.send('RainMaker', {intensity: rainIntensity});
}


function updateDropCounter(err, msg) {
  var rainDrop = msg.body;

  // TODO fix computations
  numRainDrops++;
  totalProductX *= rainDrop.x;
  totalProductY *= rainDrop.y;
  var deltaX = Math.abs(rainDrop.x - totalProductX);
  var deltaY = Math.abs(rainDrop.y - totalProductY);
  varianceX += deltaX * deltaX / numRainDrops;
  varianceY += deltaY * deltaY / numRainDrops;
  if (varianceX * varianceY != 0.0) {
    correlationCoefficient += deltaX * deltaY / Math.sqrt(varianceX * varianceY);
  }

  document.getElementById('drop-x').innerHTML = rainDrop.x;
  document.getElementById('drop-y').innerHTML = rainDrop.y;
  document.getElementById('num-drops').innerHTML = numRainDrops;
  document.getElementById('total-product-x').innerHTML = totalProductX;
  document.getElementById('total-product-y').innerHTML = totalProductY;
  document.getElementById('variance-x').innerHTML = varianceX;
  document.getElementById('variance-y').innerHTML = varianceY;
  document.getElementById('correlation-coefficient').innerHTML = correlationCoefficient;
}