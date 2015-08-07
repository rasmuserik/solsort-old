export PATH=/usr/local/bin:$PATH 
install -d logs
npm install >& logs/`hostname`-`date +%y%m%d-%H%M%S`.install

while true
do
  node solsort.js dev-server >& logs/`hostname`-`date +%y%m%d-%H%M%S`.console &
  NODE_PID=$!
  echo $NODE_PID > solsort.pid
  sleep 5
  wait $NODE_PID
done
