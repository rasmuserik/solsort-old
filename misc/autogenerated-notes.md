
## solsort/solsort backlog
### 0.5

- bibapp
  - use reagent
  - √single /bib function
  - √css uncapitalise->dash
  - basic bib webapp w/ ui
  - design/choose pages
- test+deploy
  - automatic test with comparison of site
  - automatic browser test
  - travis-ok -> auto git pull / deploy

### Soon

- network graph
- update routing
- reimplement kvdb on indexeddb

- version id (checksum of unchecked .min.js)
### Later

- webworkers
- solsort slide show
- uccorg watcher
- distributed DB

## solsort/solsort changelog
### 0.4 /notes

- update changelog/backlog
- canonize string for notes and in general
- notes (migrate notes)

### 0.3 /index, /rasmuserik, mboxes, websockets, render jsonhtml

- mboxes, erlang inspired message system
- /index, /rasmuserik
- use websockets for communication with clients
- render jsonhtml

### 0.2 autorestarting, webserver, routes

- webserver+router
- autorestarting dev-server

### 0.1 travis, uccorg-restarter, nginx

- travis test
- basic uccorg restarter
- nginx
- logging

### 0.0 kvdb, related-webservice

- kvdb - leveldb/indexeddb wrapper
- related webservice


## solsort/solsort notes

- process
  - has one mbox
  - either worker(webworker), browser(ui+indexeddb), nodejs(w/ public http server)
- types
  - handler: msg -> 
  - msg: pid, mbox, info[reply-to] , data/args
- api
  - low-level
    - handle (mbox, handler) -> mbox | nil
    - handle (handler) -> mbox
    - unhandle (mbox) -> success
    - handled? mbox -> bool
    - handled -> mbox list
    - send msg -> nil
    - msg (pid, mbox, data, info) -> msg
  - high-level
    - call-timeout (timeout, pid, mbox, args..) -> result chan
    - call (pid, mbox, args..) -> result chan
    - route (mbox, f args... -> result chan) -> nil
    - local pid
    - browser pid
    - random-worker () -> pid

### old notes

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
## Brunch

Checklist fo brunch

- røræg
- boller/brød
  - marmelade
  - ost
  - honning
  - smør
- yoghurt
  - "græsk" yoghurt
  - müsli
  - ahornsirup
- tilbehør
  - oliven m. mandel
  - tomat/hvidløg-mix
  - agurke-stænger
  - gulerodsstænger
- drikkevarer
  - danskvand
  - hyldblomstsaft
  - te
  - kaffe
- dessert
  - frugt
  - smeltet chokolade

## Bass

Personal notes for practising/learning bass

- repetoire
  - must
    - jazz/blues bass 32043252323403203252425230231200234030232340302
    - canon 3 3 0 0 1 1 1 3 3232030310101032
    - fever 313130330310
    - tango/piazolla-like 032032 232232232232032032
    - seven nation army 3313143 3313141431
    - pink panther 401222104333401240542054 020221040+320224010+22022 
    - aint no sunshine 22202 22202 ain
    - stand by me 21 2221221444244200422122212
    - sixteen tons 44122412 2032 2032 2032
    - pink floyd money 24422020244220202
    - alex foley 31333113533653 33131313 3333 3123
    - in the summertime 24224242340 24224242340 32000000000 02220000
    - sweet dreams |: 33031303 44133301 :|
    - cant touch it 3203 03 20
  - maybe
    - satisfaction
    - I feel good
    - beatles come together
    - clayton brothers walking bass
    - mission impossible
    - when a man loves a woman
    - heartbreak hotel
    - I can't get no satisfaction
    - another one bites the dust
    - cream sunshine of your love
    - minnie the moocher
    - beat it
    - peter gunn
    - bubble bobble
    - mario
    - walking bass
    - bare necessities
    - aristocats everybody wants to be cat
    - another brick in the wall
    - I'll be missing you
- practise plan
  - learn repetoire
    - in all keys
  - practise intonation with tuner and drone
  - walking bass
- youtube
  - Discover Double Bass
  - Chris Fitzgerald
  - John Clayton Jazz
  - ArtistWorks John Patitucci

## d6 simple roleplaying system

Notes about simple roleplaying system

### Ability scores

How to calculate ability score:

- start out with 2 in every ability
- add race modifier
- add class modifier
- add one to an ability of choice

total: 6+2+1, ie 621 441 333 225 234

#### STR - strength/physics

- 1 
- 2
- 3 
- 4
- 5
- 6

#### INT - intellect/mind

- 1
- 2
- 3 
- 4
- 5
- 6

#### FIN - finesse/awareness

- 1
- 2
- 3 
- 4
- 5
- 6


### power
#### Health

- `(STR+1) * 10`
- restore STR health per day of rest

#### Mana

- `(INT+1) * 10`
- restore INT mana per hour of rest

### Races/classes
#### Races

##### Humans ...
##### Gnomes .+-
##### Goblins .-+
##### Orcs +-.
##### Elves -+.
##### Dwarfs +.-
##### Hobbits -.+
#### Classes

##### Warrior 2..
- 3x fight

##### Wizard .2.
- 3x spells

##### Rogue ..2
- 3x tricks

##### Cleric ++.
- Heal
- 2x fight or spell

##### Ranger +.+
- Hunting
- 2x warrior

##### Bard .++
- 2x trick or spell

### Actions

Turn consist of two actions and one step

#### Move - move max(1d6, str) steps

#### Attack - use str-to-hit

#### Defend/hide - add FIN to defence

#### Search/spot

#### Talk

 2 
 3 
 4 
 5 
 6 
 7 6 
 8 5 15 42%
 9 4 10 27%
10 3 6 17%
11 2 3 8%
12 1 1 3%
### Skills

- choose 6 skills in the beginning, get 1 skill per level
- limited to the ability level per category

#### Battle - STR

##### Knighthood

- shield fighting

##### Berserker

- two weapon fighting

##### Unarmed fighting

- pin
- disarm
- pressure-points
- dead-touch

#### Spiritual - min(STR,INT)

##### Life

- self-healing
- healing
- curing
- raise dead

##### Spirit

#### Magic (INT)

##### Elemental magic

- fireball
- wind walk
- ice wall

##### Nature Magic

- animal companion
- conjure animal

##### Planar Magic

- blink
- teleport

##### Necromancy

#### Bard - min(INT,FIN)
##### Inspire

- Best of two
- Offer reroll

##### Charisma

- distraction
- sympathy/disguise
- convince
- illusion

#### Rogue - FIN

##### Stealth

- pick lock and pockets
- hide in plain sight

##### Acrobatics

##### Backstabbing

- use finesse for dagger attack
- poison

#### Ranger min(STR,FIN)

##### Archery

##### Hunting, tracking and nature knowledge

## Dance

- contact impro
  - use energy/healing/qi in the dance
  - within personal space without touching
  - connection with the ground/earth
  - stillness as movement
- debora hay
  - each of the trillion cells in the human body focusses on the action
  - sense of direction and extension in the movement
- lindy hop
  - remember small steps
  - louie
    - triple-step vs. slow-slow
    - barbie
    - swing towards/apart-exercise
    - lead on 1. vs. lead on 2.
  - lindymyday
    - move top body and rear, 
    - samme figur med forskellige hænder, ie. tuck-turn under lindy-my-day
    - exit angle
- tango

## Anbefalinger


- Dagligt forbrug
  - Københavns Fødevarefællesskab - ideologisk lokal økologiske madvarer non-profit
  - Coop - medlemsejede supermarkeder, overskud går til forbrugerne/medlemmerne, og ikke store coorporations
  - cykel frem for bil eller offentlig transport
  - lejebolig - fsb og lignende, fleksibilitet
- Special Forbrug
  - non-profit genbrugsbutikker - røde kors, folkekirkens nødhjælp, etc. minimalt miljømæssigt impact, og pengene går til godt formål
  - thewritingdesk.co.uk - skriveredskaber
  - Mimers cykler - lokal og god kvalitet/service, 
  - kontrabas.com - lokal og god kvalitet/service
  - naturlig fod - lokal og god kvalitet/service
  - trævarer - 
- Finans
  - Merkur bank - ideologisk bank
  - lsb - fagforeningsejet, ekstremt god rente på indlån op til 50K.
  - ¿AP-pension? ¿ideologisk pension? kundeejet kigger på at skifte til dette ASAP
  - Triodos investeringsforeninger - ideologisk placering/investering af ens midlere
  - lærestandens brandforsikring - medlemsejet, bedste forsikringstilbud
- IT
  - Mozilla / firefox - ideologisk bedste (og teknisk bedste sammen med blink(chrome/opera)) webbrowser
  - fsf og openknowledge - kæmper for open source og åbne data
  - github - central infrastruktur for open source
  - laptops.dk - god erfaring med kundeservice, leverance og pris, I need http://xkcd.com/243/
  - gratisdns - gode erfaringer med denne leverandør
- Outgoing
  - De danske biblioteker
  - Tinkuy
  - cafe retro
  - studenterhuset - lindy hop
  - det kongelige teater - tango
  - ci-cph
  - falaffelkælderen
  - morgenstedet
  - bewelcome
  - toastmasters

## Berlin

- raise a smile berlin
- dans
  - wed tango 20:00 Strandbar Mitte Monbijoustr.  Berlin
  - wed lindy 22:00 Clärchens Ballhaus, Auguststr. 24, Berlin-Mitte
  - thu tango 18:30 Himmelbeet. Ruheplatzstr. 12.
  - thu lindy 21:30 Frannz, Schönhauser Alle 36 · 10435 Berlin
- food
  - baharet falaffel Winterfeldtstr. 37, Nollendorplatz 
  - lucky leek Kollwitzstraße 54 10405 Berlin
  - yoyo Gartnerstrasse 27 Berlin, Germany 10245
  - ohlala pastery Mainzer Strasse 18 (Frankfurter Allee) 
  - vegetarian club
- museum
  - naturkunde Invalidenstraße 43 10115 Berlin
  - technik Trebbiner Straße 9 D-10963 Berlin-Kreuzberg
  - musikinstrumenten   Tiergartenstr. 1 10785 Berlin 
  - design panoptikum Torstraße 201, Berlin, 10115, 
- city
  - buy tea at large eco-store
  - escape game
  - betahaus thursday breakfast 9:30 filmtruppe berlin, tightops, and fattelo
  - east side gallery
  - holocaust-memorial, east-side gallery
  - alternative tour 11,13,15 Our Free day tours meet at Alexanderplatz TV TOWER next to entrance of Star.uck’s
  - Universitet?
  - (Berlin exploratorium 24.07.2014 um 19:30 - 21:30 Nichts und alles Impro-Treff, Saal 2 ) ???
  - musikbutik - ukulele
- nature
  - grünewald - ¿ddr?
  - swim
  - ¿treptower park?
  - boat - wannsee Bootsverleih Wannsee Wannseebadweg 25
  - botanische garden
  - wannsee island


## CombiGame
CombiGame is a mobile app, web game and card game. Try it:

- [online](/solsort/combigame) (runs in a web browser)
    - [android version](https://play.google.com/store/apps/details?id=com.solsort.combigame)
- as a cardgame
    - [buy it online from TheGameCrafter](https://www.thegamecrafter.com/games/combigame)
    - catch me in person ;) and try it or buy it (DKK 120)

Game objectives: spot combinations of three figures where color, count, shape and fill, are either the same or all different.

For example the figures in:

![combigame combi](/img/combigame-combi.png)

are a valid combination, as they have different color, count and shape, and the same fill,
whereas:

![combigame invalid](/img/combigame-invalid.png)

are invalid as the color is neither all different or the same.

The card game goes as follows:

- put 12 cards on table face up
- when a player spots a valid combination, she tap on the table to indicate that she spotted it first, and the collect the combination
- when no valid combinations can be spotted, deal 3 more cards from the deck face up
- when all of the deck is dealt, and no more combinations can be spotted, the player who collected most combinations won.

When played on a phone/tablet/computer, the computer deals such that there is always at least one valid combination 12 figures on the screen.

In the online version, it is possible to click on the hint-button to see examples of valid combinations.

#### Credits

Rules are mostly the same as the [Set card game](http://en.wikipedia.org/wiki/Set_%28game%29), which is highly recommendable. 

The goal of the original version of CombiGame was just to play Set on the smartphone (with new graphics), - later on it expanded onto physical cards as well.

## EuroCards

![EuroCards](/img/IMG_2596-eurocards.jpg)

(English description, see below)

Kortspil til brug i geografi-undervisningen.

Foreløbigt blot en prototype / proof-of-concept.

Tænker måske at søge tipsmidler til at lave en dansk udgave af dette, der dækker hele verden i stedet for blot Europa. Kontakt mig gerne hvis du vil være med til at lave en sådan ansøgning, eller vil spilteste det.

Spillet kan købes [online](https://www.thegamecrafter.com/games/EuroCards), eller ved at fange mig personligt (DKK 100).

#### In English

EuroCards is a [Top Trumps](http://en.wikipedia.org/wiki/Top_Trumps) like geography game with cards with facts about the European countries.

You can buy it from [TheGameCrafter](https://www.thegamecrafter.com/games/EuroCards).

#### Credits

- Data from CIA
- Satellite image from NASA

## Fototips 

Forskellige tips, trick og ting at huske.

* There are no rules for good photographs, there are only good photographs.
* Den fotograferede fokuserer øjnene på filmen bag ved linsen.
* Der er altid to personer i et billede, fotografen og beskueren.
* The negative is the score, and the print is the performance.
* Blitz når det er lyst, lad være når det er mørkt.
* Øjnene går fra venstre op mod højre
* Farvernes komplement og effekt.
* Perfection of imperfection.
* Skyggerne skaber formen.
* Omgivelser som rammer.
* Udnyt dybdeuskarphed.
* Kalibrering af kamera.
* Øjne tiltrækkes af lys.
* f/8 and being there.
* Kunsten er at se.
* Det gyldne snit.
* Farvepsykologi.
* Negative space.
* Rules of thirds.
* Gråtoner.

----
Rasmus Erik, 2006

## JavaScript platform

Features:
- TypedArrays
- SIMD: mapPar etc.
- threads: WebWorkers
- network: webrtc, websocket, ajax
- graphics: canvas, webgl, dom(inkl. svg)
- input: touch, webrtc-video-capture, geo, accel
- performance: (hidden classes, generational GC, )

Major Compilers:
- C/C++ llvm emscripten
- Java gwt

Implementations:
- spidermonkey+gecko
- v8+blink|node
- JSC+webkit
- nashorn+java
- chakra(ie)

## MiniLD##36: Contrasts

Entry for [MiniLD36](http://www.ludumdare.com/compo/2012/07/01/mini-ld-36-contrasts/).

#### Try it in Chrome/Firefox at <http://solsort.com/tsar_tnoc/>.

Works in recent Chrome/Firefox/... will probably fail miserably with other browsers.

![screenshot](/img/tsartnoc-screenshot.jpg)


A simple game where you collect tokens and avoid monsters.

Goal:

- get to as high a level as possible before getting eaten

Features:

- game engine written from scratch in 4 days
- improvised jazzy music <http://solsort.com/tsar_tnoc/audio/jazzy.ogg>
- original odd graphics
- procedural level generation
- focus was more on prototyping experiment than actual game...

Source code at: <http://github.com/rasmuserik/contrast>


----
Rasmus Erik, July 2012

## Notes for my son

Various notes about life, 

### Thinking

- mind maps / hierachial lists
- personal log
- scrum and lean
- the conscious vs. the unconscious
  - taking a walk or a nap not consious on the thing, often opens up for the solution
  - problem solving
- 10x10

### Behaviour

- "give it forward"
- the other is you, everything is connected
- hvis event er kedeligt, se på hvad du selv gør/giver til det
- always honesty and truth

### Philosophy and religion

- There are limits of science and thoughts, things exists which can neither be proven nor disproven 
  - Gödel has even shown mathematically that there are parts of mathematics which are beyond proof. As far as I remember: ZFC consistency is a matter of belief

- The map is not the territory
  - my or your model of the world is not the world nor how it is
  - everything we know is through our fallable senses

- God cannot be fully described with words

### Media

- Books
  - Tao te King
  - Richard Bach - Illusioner
  - Hermann Hesse - Sidharta, steppeulven
  - The Bible - Særligt prædikeren, samt jesus egne ord or handlinger, "elsk gud og din næste"
  - Rumi digte, todo:muhl-bog-synkronisitet
  - C.G.Jung's autobiography - especially the ideas about synchronisity and the unconscious
- Audio
  - Alan Watts talks
  - Getting things done seminar
- Movies
  - Amelie
  - Ghandi
  - Casablanca
- Art
  - Escher
  - Dali

## Opskrift: Hummus
![Hummus](/img/IMG_1556-hummus.jpg)

- 500g kogte kikærter
- ½dl vand
- Et lille hvidløg
- Saften af 3-4 citroner
- Et drys spidskommen
- En lille skefuld tahin

Ingredienserne blendes

Pynt:
- Et drys paprika
- Basilikum

----
Rasmus Erik, July 2012

## Opskrift: Karry Dolmers
![Dolmers](/img/IMG_1535-dolmers.jpg)

Ingredienser:

- 4 almindelige løg
- 1 rødløg
- smør til stegning
- karry
- 4 fed hvidløg
- ½kg basmatiris
- 400g vinblade i saltlage

Salsa:

- 1 lille hvidløg
- ½dl olivenolie
- 140g tomatkoncentrat

Karry og løg steges i smør, og herefter tilsættes risen.
Dolmers formes: Først nippes stilken af vinbladet, og derefter lægges en klump ris på bladet, siderne foldes op omkring risen og den rulles til en dolmer. Dolmerne pakkes derefter tæt i en gryde, og vand til kogning hældes derefter i gryden. Til sidst koges de en halv time - tre kvarter.

----
Rasmus Erik, July 2012

## Opskrift: Kartoffelæggekage

![Tortilla](/img/IMG_1476-tortilla.jpg)

Ingredienser:

- Kartofler
    - 1½kg kartofler
    - Olivenolie
    - Krydderier (paprika, rosmarin, salt)
- Jævning
    - 4 æg
    - Mælk
    - Majsstivelse (Maizena)
- Pynt
    - Rød peber
    - Salat
    - Agurk
    - Forskellige tomater

Kartoflerne steges med krydderier og olivenolie. Jævningen piskes sammen og hældes ud over kartoflerne. Det hele steges til jævningen stivner, og tortillaen vendes derefter og steger videre. Til sidst anrettes med skåren grønt.

----

Rasmus Erik, 2012 July 8th.

## Opskrift: Ovnbagt laks med kartofler og persillesovs
![Ovnbagt laks med kartofler og persillesovs](/img/IMG_1560-laks-kartoffel-persillesovs.jpg)

Ingredienser:

- Laks
    - Laksestykker
    - ½ citron per laksestykke
    - Timian
    - 1 tsk. smør per laksestykke
    - Salt
- Persillesovs
    - Smør
    - Mel
    - Mælk
    - Persille
- Kartofler

Laksestykkerne indpakkes enkeltvis i staniol, og overhældes med citronsaft, timian, salt og smør. Lægges herefter i ovenen ved 180° i ca. en halv time. Kartoflerne koges. Smør til persillesovs smeltes, og mel piske i og når det begynder at blive stift tilsættes mælk og hakket persille under omrøring.

----
Rasmus Erik, July 2012

## Presentation evaluation

- preparation/intro
    - talk with speaker, what kind of feedback do you want?
    - manual / goal of this speech
    - read-up on evaluating, sharpen your saw
- parts to look at
    - effect
        - personal, enriching, inspiring
        - purpose: inform/persuade/entertain/inspire
        - audience reception, focus on audience
        - humor, enthusiasm, personality
    - content
        - concrete vs. abstract
        - past, future, present
        - rhetorical devices
    - tools
        - visuals and props
        - notes
        - supporting material / research
        - quotes
    - body language
        - eye contact - one complet thought
        - posture open/close, confidence, direction/focus
        - face/smile
        - gestures/hands
        - use of floor
    - vocal variety
        - speed
        - volume
        - pitch
        - pauses
        - articulation
    - language
        - words: simple/slang/jargon/technical
        - images/vividness
        - active vs. passive
        - grammar
    - structure
        - opening: approach, hook, clear intent
        - outline: chrono/spatial/causal/comparative/topical/problem-solving
        - transitions
        - closing - consise, summary, memorable/call to action
        - overall structure
- presentation
    - make a point from  the speech, teach a lesson to all
    - sandwich / CRC - commend recommend commend/conclude
    - intro
        - she/he showed us ..., thanks
        - purpose of the speech
        - summarize the speech
    - concrete points from the speech
    - focus on a few points in depth
    - talk about how it impacts the audience, rather than what you would do as speaker
    - personal opinion, personal dequalifiers, girafsprog, my point of view
    - presentation
        - notes - careful, not look in them much
        - 'and', not 'but'
        - avoid cliches
        - job: improve the speaker, not speaking yourself
    - start with the end in mind

----
Rasmus Erik 2012-03-18

## Pricing scale


Nice round numbers, both for Euros and DKK(Danish currency)

Price doubles approximately per two steps

Exponential scale, a la planning poker

Useful tool for estimating/finding price without sweating the details

    
       DKK         (+VAT)         Euro
    
         15            (3.75)         2
         30            (7.5)          4
         45           (11.25)         6
         60           (15)            8
         90           (22.5)         12
        120           (30)           16
        180           (45)           24
        240           (60)           32
        360           (90)           48
        480          (120)           64
        720          (180)           96
        960          (240)          128
       1440          (360)          192
       1920          (480)          256
       2880          (720)          384
       3600          (900)          480
       4800         (1200)          640
       7200         (1800)          960
       9600         (2400)         1280
      14400         (3600)         1920
      19200         (4800)         2560
      28800         (7200)         3840
      36000         (9000)         4800
      48000        (12000)         6400
      72000        (18000)         9600
      96000        (24000)        12800
     144000        (36000)        19200
     192000        (48000)        25600
     288000        (72000)        38400
     360000        (90000)        48000
     480000       (120000)        64000
     720000       (180000)        96000
     960000       (240000)       128000
    1440000       (360000)       192000
    1920000       (480000)       256000
    2880000       (720000)       384000
    3600000       (900000)       480000
    4800000      (1200000)       640000
    7200000      (1800000)       960000
    9600000      (2400000)      1280000

## Productivity hacks

Notes from presentation.

#### Tactical vs strategic thinking
Planning on different levels, frees up focus. Work more efficiently when not thinking about direction.

###### Daily review

Review previous day • Think once a day • Choose tasks

###### Exercise, food, sleep~dream

Physical prerequisite for persistent performance

###### Gratitude, reiterate direction

Daily reaffirm direction • Focus on gratitude

###### Pomodoros

25 minute timebox • Atomic units • Task estimation • Record activity • Small, easy to start • Work fit allocated time (¿Parkinsons? law)

###### Empty inbox

Frees mental space • clutter tolerance, same amount of work

###### Daylog

Todolist • getting things out of the head • moving to done

###### Backlog

Temporal backlog in daylog • Categorised backlog in separate file • Capture system

###### Review Biweekly and Quarterly

Overall direction • Look back • Review backlog and goals • Temporal backlog

#### Sources
###### Getting Things Done
Empty inbox • Capture system • Not keeping stuff in the head

###### Scrum (and Lean)
14 day sprints • Retrospective • Daily Scrum • Kanban - daylog • Measure and optimise

###### Junto
Pomodoros • Network peering

## Solsort Product

- Typical product
  - App, or advanced website-functionality (frontend+backend)
  - Deadline within weeks or months
  - Budget € 4.000-40.000 (DKK 30.000-300.000)

- Target customers
  - Companies/organisations - improve you workflow and quality by getting IT available in the field - expert systems, checklists, digital training tools
  - Decision makers and management - get data analysed, visualised, available in your hands in real time.
  - Media companies - I help you deliver, - solve HTML5 problems, special effects, customisable games, ...
  - Website owners - get the advanced functionality you want on your website
  - StartUps - get your MVP up and running
  - Enthusiasts - get your idea turned into an app
  - Small business - differentiate/profile yourself via an app that demonstrates your expertise

- Priorities
  - Local, collaborative development - direct communication with stake-holders
  - Working solution within weeks - rather than perhaps perfect solution someday-maybe
  - Value driven - strongly prefers open source/data/content, and humanitarian/educational impact
  - solsort.com technology stack - runs on phones, tablets, computers, web browsers, and future devices... 

- Delivered artifacts (some yet to be applied)
  - Web app
  - Android app
  - iPhone app
  - Windows Phone app
  - iPad app
  - Windows/Linux/OS-X application
  - Facebook app
  - node.js server
  - Web widget or component
  - Souce code
  - Addon for Chrome and Firefox
  - CMS plugin/module/app (WordPress, Drupal, Joomla, Wix,...)

- Distribution channels (some yet to be applied)
  - The World Wide Web
  - Firefox Marketplace
  - Google Play
  - Node Package Manager
  - Bower
  - GitHub
  - Windows Store
  - Apple App Store
  - Facebook App Center
  - Physical devices

----
Rasmus Erik, 2014

- create `index.html`, `$APP.coffee`, `package.json`, `icon.png`(512x512), 2xscreenshot
- `reputil build`, `git commit ...`, `git tag v0.?.?`, `git push --tags`
- bower register ...
- npm publish
- add entry in www/createStatic.coffee, run, and commit (todo: this should traverse `*/package.json` too)
- login to https://build.phonegap.com - add repos, choose certificate, build, download artifacts
- login to https://play.google.com/apps/publish/ - upload artifacts, fill out forms - same info as in package.json, click publish

## Skrivetips
#### Tommelfingerregler for skrivning

* Brug aktiv tale
* Brug navneord og udsagnsord
* Brug positive udsagn
* Et emne per afsnit
    * Start afsnittet med emnet
* Gentag, gentag, gentag - på forskellig måde
* Præsenter lignende ideer ens
* Hold forfatteren i baggrunden
* Vær forsigtig med "at være"
* Vær forsigtig med fremmedord
* Vær forsigtig med tillægsord
* Fjern overflødige ord
* Læs korrektur og omskriv
* Skriv klart, uden overflødige detaljer
* Brug ordrækkefølgen til at strukturere indholdet
* Brug både visuelt, auditivt, kinestetisk og auditivt-digitalt sprog.

Vær opmærksom på:

* Formål med kommunikationen
* Målgruppen
* Tilgængelighed
    * Resumé
    * Opslagsord til indeks
    * Nøgleord til søgning
    * Opbygning og overskrifter
    * Referencer og kilder

#### Tekststruktur for rapporter

Skabelon og huskeliste der kan anvendes ved udarbejdelse af naturvidenskabelige rapporter, artikler og lignende. 

* Pre-tekst 
    * _Titel, forfatter, organisation_
    * Abstract
    * Nøgleord, klassifikationskoder, emne
    * Indholdsfortegnelse
    * Forord (Formål. Målgruppe/forudsætninger. Læsevejledning. Årsag/baggrunden for dette dokument. Tak.)
* Start-tekst 
    * _Problemformulering_ (Hvilket problem. Hvorfor interessant. Afgrænsning.)
    * Introduktion til emnet/litteraturen
* Hoved-tekst
    * _Metode_ (Hvordan løses problemet. Antagelser. Model, algoritme, analysemetode, eksperimentbeskrivelse og opstilling af hypotese. Beskrevet så andre vil kunne udføre det samme. Ingen resulater endnu.)
    * _Resultater_ (Præsentation af resultaterne.)
    * _Diskussion_ (Hvad betyder resultaterne. Sætte i perspektiv. Ideer til videre studier.)
* Slut-tekst 
    * _Konklusion_ (Hvad er resultatet af argumentationen i hovedteksten. Ingen ny information.)
* Post-tekst 
    * _Litteraturliste_ (Kilder.)
    * Litteraturliste (Foreslag til videre læsning.)
    * Noter (Hvis de ikke er placeret som fodnoter, hvilket er bedre.)
    * Indeks
    * Bilag

----
Rasmus Erik, 2005

## Ubuntu setup notes


- `sudo apt-get install build-essential screen`
- firewall: `sudo ufw allow ssh/tcp; sudo ufw allow http/tcp; sudo ufw allow https/tcp; sudo ufw logging on; sudo ufw enable`
- `sudo update-alternatives --config editor`

Wifi access point
http://cberner.com/2013/02/03/using-hostapd-on-ubuntu-to-create-a-wifi-access-point/


