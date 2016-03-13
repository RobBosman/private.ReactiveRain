"use strict";
var SVG_NS = "http://www.w3.org/2000/svg";

var eb = new EventBus(window.location + '/eventbus');
eb.onopen = function() {
  var tileSvg = createSvg("tile");
  eb.registerHandler('RainDrop', function(error, msg) {
    var rainDrop = msg.body;
    document.getElementById('raindrop-count').innerHTML
        = "RainDrop[" + rainDrop.count + "] (" + rainDrop.x + ", " + rainDrop.y + ")";
    drawRainDrop(rainDrop, tileSvg);
  });
}

function createSvg(parentId) {
  var tileSvg = document.createElementNS(SVG_NS, "svg");
  document.getElementById(parentId).appendChild(tileSvg);
  return tileSvg;
}

function drawRainDrop(rainDrop, tileSvg) {
  var drop = document.createElementNS(SVG_NS, "circle");
  drop.id = rainDrop.count;
  drop.setAttribute("cx", 100.0 * rainDrop.x + "%");
  drop.setAttribute("cy", 100.0 * rainDrop.y + "%");
  tileSvg.appendChild(drop);

  setTimeout(function() {
    tileSvg.removeChild(drop);
  },
  2000);
}


var rainIntensity = 0.0;

function addRain(deltaIntensity) {
  rainIntensity = Math.min(Math.max(0.0, rainIntensity + deltaIntensity), 1.0);
  eb.send('RainMaker', {intensity: rainIntensity});
}