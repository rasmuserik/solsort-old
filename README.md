# Solsort

# Backlog

- √running in node.js

- abstract all system-specific into system.cljs library
- database-abstraction backed on leveldb and indexeddb

- testing
- improve key-value-database
- also compile + test node version
- related-server up and running
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
## 0.2.0 

- refacter general utilities from related-server into utility library
- statistic calculation from library data
- clean up dead code
- related-info

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

# Notes
## View types

- html
- releated graph - d3-force
- map - Leaflet
- canvas
- webgl

## JavaScript
### FeatureLevels

- insufficient - IE9-, android4.3-, iOS6- - javascript,Content-Encoding:gzip
- basic - Blackberry-10,IE10+,iOS6+,Android4.4+ - js+cors-ajax+html,canvas,atob/btoa,appcache,geolocation,history/location,hashchange-event,navigator.onLine,websockets,(server-sent except ie)
- client - IE11+, iOS8+, Android5+, ff34,chrome37,opera24 - webgl, indexeddb, webworkers, video, web-cryptography, file+filereader-api, page visibility, requestAnimationFrame, (device-orientation,fullscreen,touch,clipboard,performance.now)
- peer - recent chrome/firefox/opera/android5+ - webrtc-peer-data, camera, generational-garbage-collection, webworker transferables, (indexeddb-binary-blob-builder, battery-status, simd.js, presentation-api, storageInfo, filesystem+drag, touch, vibration, web audio api)
- app - arbitrary http-access, ie. crosswalk, node-webkit, ...
- server - connection to couchbase/elastic-search/postgresql, http-server available from internet

- mobile - avoid computation, only little memory
    - nb: keep-awake-hack: play a video (can only start on touch-interaction)


### Platforms

- current
  - pure webapp
  - node-webkit
- future
  - firefox-addon - https://developer.mozilla.org/en-US/Add-ons/SDK/Low-Level_APIs - child_process, read-file, ... inkl. mobile
  - android-app https://crosswalk-project.org/
    - possibility also to run a thread on rhino - to have access to android api
  - cordova iOS etc.  - chrome extension
  - opera extension

## Repository Plan

Main repositories

- `solsort` - source code for cloud
  - `notes.md` - various writings: notes and release-status, will be automatically extracted and transformed from markdown
- `webroot` - static files for the webserver
- `gateway` - gateway server, which starts webrtc-server, connects nodes, and forward http to nodes, proxies couch-base-access etc.
- `loader` - boot-javascript that caches code in localstorage, and autoupdates etc.
- repositories for building individual projects

## Distributed System
Distributed system
- node
  - DB-thread
  - UI-thread
  - worker threads
- everything coordinated via distributed database, 
  - all state saved there
  - coordination 
    - idle-workers are listening on (wait-get :local :next-task) and (wait-get :local worker-id), and then compare-and-swap to take job
    - communication are done via changes in storage, - most cached in memory, and messages send between threads immediately when db-change
- database: key-value stores, each store distributed in a number of buckets each belonging to a set of nodes
- storage
  - owner/manager manages bucket-placements, and timestamp for entire db
  - owners
  - buckets
  - redundency level
  - bucket-fn (key `->` bucket it belongs to)
- bucket
  - owner/manager - timestamps commits
  - owners
  - key-values
  - key-subscriber-tree-of-trees
- initial stores: 
  - `local` - single bucket on local computer, no replication
  - `topology` - contains network information, 1 bucket per key corresponding and hosted on each host, no replication
- topology levels
  - process (containing ui-thread, db-thread and worker-threads)
  - processes on same machine/same-internal-ip
  - same external ip
  - same zone
- computer type
  - peer
    - indexeddb
    - cryptography
    - webrtc-peer
    - webworkers/thread-pool
  - pseudo-peer
    - as peer, but no webrtc-peer, so proxied through peer
    - cannot be bucket-owner except for local/non-replicated
    - either no-webrtc, often working offline, or running on battery
  - client
    - essentially only ui-thread running
- commit-levels
  - locally sent
  - committed in local db-thread
  - committed in bucket-member
  - committed in bucket-owner
  - committed in all buckets
- client-api storage-keys
  - get key [max-age] `->` value (possibly cached)
  - (get-and-lock storage keys [timeout]) `->` result-map, put-function
  - wait-get key (similar to subscribe, channel returning non-nil value if possible or waiting for write)
  - subscribe key 
  - unsubscribe key
  - local-fn - applies function on key/val in bucket, ie. inc, dec, push, pop, compare-and-swap, set-if-nil, ...
- client-api storage
  - open
  - map - run named function w/ optional data along
  - reduce incl.  - get-all - get-all-keys - get-all-values
  - subscribe
