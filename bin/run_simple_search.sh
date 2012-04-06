#!/bin/bash

# Kill any running node.js server
pkill -9 node

# Start a default instance of the server
node ../node-js-server/node-drpc-server.js 1> ../logs/simple_search_node.log 2>../logs/simple_search_node.err &

java -cp ../target/storm-search-0.0.1-jar-with-dependencies.jar simplesearch.SimpleSearchEngineTopologyStarter SimpleSearchTest localhost:8081 100

pkill -9 node
