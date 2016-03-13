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
  drawRainDropCanvas(rainDrop);
}

function drawRainDropCanvas(rainDrop) {
  var tile = document.getElementById("tile-canvas");
  var context = tile.getContext("2d");
  context.moveTo(tile.width * rainDrop.x, tile.height * (1.0 - rainDrop.y));
  context.lineTo(0, tile.height);
  context.stroke();
}

function drawRainDropCss(rainDrop) {
  var tile = document.getElementById("tile-css");
  var drop = document.createElement("div");
  drop.className = "circle";
  drop.setAttribute("style",
    "left:" + Math.round(tile.clientWidth * rainDrop.x - drop.clientWidth / 2.0) + "px;" +
    "top:" + Math.round(tile.clientHeight * (1.0 - rainDrop.y) - drop.clientHeight / 2.0) + "px;");
  tile.appendChild(drop);
}


var rainIntensity = 0.0;

function addRain(deltaIntensity) {
  rainIntensity += deltaIntensity;
  eb.send('RainMaker', {intensity: rainIntensity});
}