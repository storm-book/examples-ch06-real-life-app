#!/bin/bash

for FILE in	`ls *.json`
do
	echo "Posting $FILE"
	curl -v -X 'POST' --data @$FILE localhost:8888/$FILE
	echo "indexing..."
done

for item_id in `ls *.json | cut -c 1-1`
do
	echo "Indexing.. $item_id"
	curl -v localhost:9090/$item_id
done
