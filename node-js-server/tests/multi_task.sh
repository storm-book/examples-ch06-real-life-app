#!/bin/bash
CONTENT='Everything is OK!'
curl --silent localhost:8080/ipod > search_result1 &
curl --silent localhost:8080/dvd > search_result2 &
curl --silent localhost:8080/sony > search_result3 &
curl --silent localhost:8080/notebook > search_result4 &

sleep 1

CANT=$(curl --silent localhost:8081/?max=100 |wc -l)

if [ $CANT -ge 9 ];then
	echo "Passed!"
else
	echo "TESTS FAIL! [$CANT] received"
fi

pkill curl
