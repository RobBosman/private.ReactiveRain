"use strict";

function RainIntensity() {

  var ID = Math.random().toFixed(10);
  var isSliderInitialized = false;
  var isUpdatingSlider = false;
  var rainIntensitySlider;

  this.initializeSlider = function() {
    rainIntensitySlider = document.getElementById('rain-intensity');
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
      if (isSliderInitialized && isEventBusOpen && !isUpdatingSlider) {
        eventBus.publish('rain.intensity.set', {
            'intensity': (values[handle] / 100.0)
          },
          {'id': ID});
      }
    });

    isSliderInitialized = true;
  };

  this.updateRainIntensity = function(err, msg) {
    var intensityPercentage = 100.0 * msg.body.intensity;
    document.getElementById('rain-intensity-percentage').innerHTML = intensityPercentage.toFixed(0) + "%";
    if (msg.headers == null || msg.headers.id != ID) {
      isUpdatingSlider = true;
      rainIntensitySlider.noUiSlider.set(intensityPercentage);
      isUpdatingSlider = false;
    }
  };
}