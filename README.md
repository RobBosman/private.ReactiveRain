# Reactive Rain
### a demo application using the Rx-ified API of Vert.x

This repo contains a fairly simple Java server application and an HTML client to demonstrate the usage of
* bridged event bus ([Vert.x](http://vertx.io/))
* RxObservables ([RxJava](http://reactivex.io/))
* Rx-ified API of Vert.x (`io.vertx.rxjava.*`)

A `RainMaker` component on the server periodically generates 'rain drops' at a given interval. Each rain drop consists of two coordinates (x, y). The interval can be adjusted by setting the 'rain intensity'. All `RainDrops` are displayed in the browser. You can set the rain intensity with a slider.

Each individual `RainDrop` is published as an event on the Vert.x event bus, which has been extended (bridged) to the browser via a web socket connection. Setting the rain intensity is also done via events on the event bus. All events are visible by all connected browsers, so if one user changes the rain intensity, the sliders of all other connected browsers are adjusted automatically.

The server is 100% stateless, but a special verticle (`RainIntervalMonitor`) allows newly connected browsers to query the actual rain intensity value.

See also [my presentation](http://slides.com/robbosman/reactive-programming-3#/) about reactive programming.
