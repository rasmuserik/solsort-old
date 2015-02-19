cd /home/server/solsort
#PATH=/home/server/local/bin:/usr/local/bin:$PATH forever start solsort.js server
PATH=/home/server/local/bin:/usr/local/bin:$PATH nohup /home/server/solsort/dev-server.sh &
