node ./solsort.js test-server &
NODE_PID=$!

sleep 5


cd test && rm -rf result && mkdir result &&
cd result &&
cat ../paths.lst | sed 's/^/http:\/\/localhost:2222\//' | xargs wget || exit 1
../../node_modules/.bin/html-beautify -r *.html
../../node_modules/.bin/css-beautify -r *.css
../../node_modules/.bin/js-beautify -r *.js *.json
cd ../../
diff -r test/expected test/result || exit 1

firefox http://localhost:2222/index#test-client &
xvfb-run firefox http://localhost:2222/index#test-client &

wait $NODE_PID
