# solsort.com api server notes

Applications

- relvis related server
- watch/driftsovervågning uccorg

# TODO

- refactor relvis related server
- create repository for static code
- autoreload on dev-machines
- set up nginx
- services: - `/api/log` - `_logger.js` - `_*` - `/_s/..`
- writings
- slides

# JavaScript
## FeatureLevels

- insufficient - IE9-, android4.3-, iOS6- - javascript,Content-Encoding:gzip
- basic - IE10+,iOS6+,Android4.4+ - js+cors-ajax+html,canvas,atob/btoa,appcache,geolocation,history/location,hashchange-event,navigator.onLine,websockets,(server-sent except ie)- client - IE11+, iOS8+, Android5+, ff34,chrome37,opera24 - webgl, indexeddb, webworkers, video, web-cryptography, file+filereader-api, page visibility, requestAnimationFrame, (device-orientation,fullscreen,touch,clipboard,performance.now)
- peer - recent chrome/firefox/opera/android5+ - webrtc-peer-data, camera, generational-garbage-collection, webworker transferables, (indexeddb-binary-blob-builder, battery-status, simd.js, presentation-api, storageInfo, filesystem+drag, touch, vibration, web audio api)
- server - connection to couchbase/elastic-search/postgresql, http-server/node-api

- mobile - avoid computation, only little memory

## Platforms

- current
  - pure webapp
  - node-webkit
- future
  - firefox-addon - https://developer.mozilla.org/en-US/Add-ons/SDK/Low-Level_APIs - child_process, read-file, ... inkl. mobile
  - android-app https://crosswalk-project.org/
  - cordova iOS etc.  - chrome extension
  - opera extension

# Release log
