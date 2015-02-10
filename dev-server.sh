killall java
lein cljsbuild auto > lein.log &

kill `cat tail.pid`
tail -f lein.log &
echo $! > tail.pid

while true
do
  node resources/build/solsort/solsort.js dev-server &
  NODE_PID=$!
  sleep 10
  wait $NODE_PID
done
