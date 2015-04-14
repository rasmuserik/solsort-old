npm install

killall java
lein cljsbuild auto solsort > lein.log &

kill `cat tail.pid`
tail -f lein.log &
echo $! > tail.pid

while true
do
  node solsort.js dev-server &
  NODE_PID=$!
  cp solsort.js ../webroot/solsort.js
  sleep 5
  curl -s http://localhost:9999/index.html > build/webroot/index.html
  wait $NODE_PID
done
