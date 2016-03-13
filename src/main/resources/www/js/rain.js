"use strict";

var eb = new EventBus(window.location + '/eventbus');
eb.onopen = function() {
  eb.registerHandler('RainDrop', function(error, msg) {
    var rainDrop = msg.body;
    document.getElementById('raindrop-count').innerHTML
        = "RainDrop[" + rainDrop.count + "] (" + rainDrop.x + ", " + rainDrop.y + ")";
    drawRainDrop(rainDrop);
  });
}

function drawRainDrop(rainDrop) {
  var tile = document.getElementById("tile-svg");
  var drop = document.createElementNS("http://www.w3.org/2000/svg", "circle");
  drop.id = rainDrop.count;
  drop.setAttribute("cx", Math.round(tile.clientWidth * rainDrop.x));
  drop.setAttribute("cy", Math.round(tile.clientHeight * rainDrop.y));
  drop.setAttribute("r", 5);
  drop.setAttribute("fill", "#00f");
  tile.appendChild(drop);

  setTimeout(function() { tile.removeChild(drop); }, 2000);
}


var rainIntensity = 0.0;

function addRain(deltaIntensity) {
  rainIntensity += deltaIntensity;
  eb.send('RainMaker', {intensity: rainIntensity});
}