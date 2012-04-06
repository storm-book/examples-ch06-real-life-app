#!/bin/bash
rm -rf ./logs/
mkdir ./logs/
cd ./logs/

node ../node-drpc-server.js 1> server.log 2> server.err &
NODE_PID=$!

echo "Node.Js Started: $NODE_PID"

sleep 1

for i in `ls ../tests/*.sh`
do
	echo "Running $i"
	$i
done

kill -9 $NODE_PID
