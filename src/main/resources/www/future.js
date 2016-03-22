"use strict";

function Future() {

  var callbackFunctions = [];
  var chainedFutures = [];
  var self = this;

  this.and = function(nextFuture) {
    chainedFutures[chainedFutures.length] = nextFuture;
    nextFuture.thenDo(this.futureCallback);
    return this;
  };

  this.thenDo = function(callbackFunction) {
    callbackFunctions[callbackFunctions.length] = callbackFunction;
    return this;
  };

  this.completed = function() {
    for (var i = 0; i < callbackFunctions.length; i++) {
      callbackFunctions[i](self);
    }
  };

  this.futureCallback = function(completedFuture) {
    var allCompleted = true;
    for (var i = 0; i < chainedFutures.length; i++) {
      if (chainedFutures[i] == completedFuture) {
        chainedFutures[i] = null;
      } else if (chainedFutures[i] != null) {
        allCompleted = false;
      }
    }
    if (allCompleted) {
      self.completed();
    }
  };
}


var whenDomIsReady = new Future();
var whenEventBusIsOpen = new Future();
var whenSliderIsReady = new Future();
