# JavaScript
## FeatureLevels

- insufficient - IE9-, android4.3-, iOS6- - javascript,Content-Encoding:gzip
- basic - Blackberry-10,IE10+,iOS6+,Android4.4+ - js+cors-ajax+html,canvas,atob/btoa,appcache,geolocation,history/location,hashchange-event,navigator.onLine,websockets,(server-sent except ie)
- client - IE11+, iOS8+, Android5+, ff34,chrome37,opera24 - webgl, indexeddb, webworkers, video, web-cryptography, file+filereader-api, page visibility, requestAnimationFrame, (device-orientation,fullscreen,touch,clipboard,performance.now)
- peer - recent chrome/firefox/opera/android5+ - webrtc-peer-data, camera, generational-garbage-collection, webworker transferables, (indexeddb-binary-blob-builder, battery-status, simd.js, presentation-api, storageInfo, filesystem+drag, touch, vibration, web audio api)
- app - arbitrary http-access, ie. crosswalk, node-webkit, ...
- server - connection to couchbase/elastic-search/postgresql, http-server available from internet

- mobile - avoid computation, only little memory
    - nb: keep-awake-hack: play a video (can only start on touch-interaction)


## Platforms

- current
  - pure webapp
  - node-webkit
- future
  - firefox-addon - https://developer.mozilla.org/en-US/Add-ons/SDK/Low-Level_APIs - child_process, read-file, ... inkl. mobile
  - android-app https://crosswalk-project.org/
    - possibility also to run a thread on rhino - to have access to android api
  - cordova iOS etc.  - chrome extension
  - opera extension

# View types

- html
- releated graph - d3-force
- map - Leaflet
- canvas
- webgl

# Backlog

- refacter general utilities from related-server into utility library
- clean up dead code
- related-server up and running
- related-info
- restructure repositories

## Later

### Website

- services: - `/api/log` - `_logger.js` - `_*` - `/_s/..`
- writings
- set up nginx
- slides

### Platform
- autoreload on dev-machines
- semi-distributed database
- communication

# Release log
## 0.1.0 

- recommendation engine - `/relvis-related/related`
  - calculation of related materials
  - fix weighting for recommendation
  - implemented using/learning transducers
- keyval-db
  - locking for better stability
  - code for clearing a "database"

## 0.0.0 

- keyval-db - simple db-key-value storage
- simple web-server

# Repository Plan

Main repositories

- `solsort` - source code for cloud
- `notes` - various writings: notes and release-status
- `webroot` - static files for the webserver
- `gateway` - gateway server, which starts webrtc-server, connects nodes, and forward http to nodes, proxies couch-base-access etc.
- `loader` - boot-javascript that caches code in localstorage, and autoupdates etc.
- repositories for building individual projects
