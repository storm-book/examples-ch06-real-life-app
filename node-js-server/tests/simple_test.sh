#!/bin/bash
echo "Simple Test..."
CONTENT='Everything is OK!'
rm search_result
curl --silent localhost:8080/ipod 1> search_result 2>/dev/null & 
PID=$!
ID=$(curl --silent localhost:8081/?max=1 2>/dev/null | head -2 |tail -1)
curl --silent -X 'POST' --data "$CONTENT" localhost:8082/?id=$ID 1> post_result 2>/dev/null
sleep 1
TSTS=$(cat search_result)
if [ "$CONTENT" == "$TSTS" ]; then
	echo "Passed!"
else
	echo "TESTS FAIL!"
	echo "[$CONTENT] != [$TSTS]"
fi

kill -9 $PID 1>/dev/null 2>/dev/null

rm search_result
rm post_result