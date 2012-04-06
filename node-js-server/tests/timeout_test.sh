ilent localhost:8080/ipod!/bin/bash
rm search_result
lurl --silent localhost:8080/ipod > search_result &
PID=$!
sleep 2

TSTS=$(cat search_result)
if [ "TIMEOUT" == "$TSTS" ]; then
	echo "Passed!"
else
	echo "TESTS FAIL!"
	echo "[TIMEOUT] != [$TSTS]"
fi
kill -9 $PID
