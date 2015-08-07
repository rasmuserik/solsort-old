npm install

killall java
lein cljsbuild auto solsort > lein.log &

kill `cat tail.pid`
tail -f lein.log &
echo $! > tail.pid


while true
do
  node solsort.js ${1:-dev-server} &
  NODE_PID=$!
#  cp solsort.js ../webroot/solsort.js
#  echo CACHE\ MANIFEST\ > ../webroot/solsort.appcache
#  echo "#" `date`  >> ../webroot/solsort.appcache
#  echo /solsort.js  >> ../webroot/solsort.appcache
#  echo /style.css >> ../webroot/solsort.appcache
#  echo /font/ubuntu-latin1.ttf >> ../webroot/solsort.appcache
#  echo NETWORK:  >> ../webroot/solsort.appcache
#  echo "*" >> ../webroot/solsort.appcache
#  sleep 5
#  curl -s http://localhost:9999/index.html > ../webroot/index.html
  wait $NODE_PID
done
