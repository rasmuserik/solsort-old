# solsort.com api server notes
## Configuration
- nginx in front, serving `www`, and forward the following to the server
  - `/_.*`

## DONE

- fork app repositories into solsort organisation
  - 360
  - art-quiz
  - blobshot
  - circle-flow-graph
  - cute-engine
  - danske-byer
  - dragimation
  - eventyrheksen
  - frie-sange
  - hack4dk
  - kbh-parking
  - lightscript
  - morse-code
  - notescore
  - single-touch-snake
  - speeding
  - tsartnoc
  - writings
- clojurescript server hello-world


## TODO

- get basic server running

###

- slides
- writings

- api
  - `/api/log`
  - `_logger.js`
  - `_*`
  - `/_s/..`

- `www`
  - autoimport solsort repositories
    - https://api.github.com/orgs/solsort/repos?page=1 ..
  - static: robots.txt favicon.js /font/ubuntu-latin1.ttf solsort.js /img/logicon.png
  - autogen `/icons/` from repos
