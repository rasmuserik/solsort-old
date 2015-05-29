[![Build Status](https://travis-ci.org/solsort/solsort.svg?branch=master)](https://travis-ci.org/solsort/solsort)

solsort.com platform, site, widgets and apps

# backlog

- prune backlog/changelog/notes/readme
- plan next tasks

## 0.5

- √mbox reimplementation
- √fix dispatch for multiparam browser hashes
- √ajax (on both node and browser)
- bibapp
  - √use reagent
  - √single /bib function
  - √css uncapitalise->dash
  - basic bib webapp w/ ui
  - design/choose pages
- test+deploy
  - √automatic test with comparison of site
  - √automatic browser test
  - √travis-ok -> auto git pull / deploy

## Soon

- kvdb should handle binary data (typed arrays), and clojure data structures
- network graph
- update routing
- reimplement kvdb on indexeddb

- version id (checksum of unchecked .min.js)

## Later

- webworkers
- solsort slide show
- uccorg watcher
- distributed DB
- package as real clojure modules
- loader
- on-need loading of javascript modules (through cdn or distributed db)

- platforms
  - √node.js server
  - √html5 client
  - firefox addon client
  - chrome addon client
  - cordova client
  - cordova kiosk/
  - nashorn server

## Roadmap

- apps/widgets
  - ddb-cms module - new version of recommendation/visualisation
  - tinkuy widget - event signup
  - wordpress widget - compbigame
  - facebook friend graph interconnects

# changelog

- new implementation of kvdb

## 0.4 /notes

- update changelog/backlog
- canonize string for notes and in general
- notes (migrate notes)

## 0.3 /index, /rasmuserik, mboxes, websockets, render jsonhtml

- mboxes, erlang inspired message system
- /index, /rasmuserik
- use websockets for communication with clients
- render jsonhtml

## 0.2 autorestarting, webserver, routes

- webserver+router
- autorestarting dev-server

## 0.1 travis, uccorg-restarter, nginx

- travis test
- basic uccorg restarter
- nginx 
- logging

## 0.0 kvdb, related-webservice

- kvdb - leveldb/indexeddb wrapper
- related webservice


# notes

- sys
  - mbox - process messaging
  - platform - platform tests - platform abstraction - (some file system code)
  - test - asynchronous unit test
  - util
- lib
  - log
  - test_runner
  - kvdb
  - net - ws+ajax
  - dispatch
  - css
  - html
  - webserver
- apps
  - index
  - rasmuserik
  - notes
  - bib_main
  - bib_process
  - bib_related
  - dev_server
  - uccorg_monitor
  - example

## mbox
  - process
    - has one mbox
    - either worker(webworker), browser(ui+indexeddb), nodejs(w/ public http server)
  - types
    - handler: msg ->
    - msg: pid, mbox, info[reply-to] , data/args
  - api
    - low-level
      - msg (pid, mbox, data, info) -> msg
      - post msg -> nil
      - handle (mbox, handler) -> mbox | nil
      - handle (handler) -> mbox
      - unhandle (mbox) -> success
      - handled? mbox -> bool
      - handled -> mbox list
    - high-level
      - call-timeout (timeout, pid, mbox, args..) -> result chan
      - call (pid, mbox, args..) -> result chan
      - route (mbox, f args... -> result chan) -> nil
      - local pid
      - browser pid
      - random-worker () -> pid

## old notes

View Types
- html
- releated graph - d3-force
- map - Leaflet
- canvas
- webgl

Feature Levels
- insufficient - IE9-, android4.3-, iOS6- - javascript,Content-Encoding:gzip
- basic - Blackberry-10,IE10+,iOS6+,Android4.4+ - js+cors-ajax+html,canvas,atob/btoa,appcache,geolocation,history/location,hashchange-event,navigator.onLine,websockets,(server-sent except ie)
- client - IE11+, iOS8+, Android5+, ff34,chrome37,opera24 - webgl, indexeddb, webworkers, video, web-cryptography, file+filereader-api, page visibility, requestAnimationFrame, (device-orientation,fullscreen,touch,clipboard,performance.now)
- peer - recent chrome/firefox/opera/android5+ - webrtc-peer-data, camera, generational-garbage-collection, webworker transferables, (indexeddb-binary-blob-builder, battery-status, simd.js, presentation-api, storageInfo, filesystem+drag, touch, vibration, web audio api)
- app - arbitrary http-access, ie. crosswalk, node-webkit, ...
- server - connection to couchbase/elastic-search/postgresql, http-server available from internet

- mobile - avoid computation, only little memory
    - nb: keep-awake-hack: play a video (can only start on touch-interaction)


Platforms

- current
  - pure webapp
  - node.js
- future
  - node-webkit
  - firefox-addon - https://developer.mozilla.org/en-US/Add-ons/SDK/Low-Level_APIs - child_process, read-file, ... inkl. mobile
  - android-app https://crosswalk-project.org/
    - possibility also to run a thread on rhino - to have access to android api
  - cordova iOS etc.  - chrome extension
  - opera extension

Repository plan

- `solsort` - source code for cloud
  - `notes.md` - various writings: notes and release-status, will be automatically extracted and transformed from markdown
- `webroot` - static files for the webserver
- `gateway` - gateway server, which starts webrtc-server, connects nodes, and forward http to nodes, proxies couch-base-access etc.
- `loader` - boot-javascript that caches code in localstorage, and autoupdates etc.
- repositories for building individual projects

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


