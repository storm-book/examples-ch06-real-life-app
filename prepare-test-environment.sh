#!/bin/bash

mkdir ./logs/

pkill -9 node

# Start the Mock Items API
nohup node ./src/test/node/mock-api-server.js 1> ./logs/mock-items-api-server.log 2> ./logs/mock-items-api-server.err &

# Start the NewsFeed DRPC Server
# Timeouts are set to 10 seconds so, because indexing can take longer.
nohup node ./node-js-server/node-drpc-server.js localhost 10000 10000 9090 text 1> ./logs/news-feed-drpc-server.log 2>./logs/news-feed-drpc-server.err &

# Start the queries DRPC Server
# Timeouts are set to 2 seconds, because queries are expected to return fast!.
nohup node ./node-js-server/node-drpc-server.js localhost 2000 2000 8080 1> ./logs/queries-drpc-server.log 2>./logs/queries-drpc-server.err &

echo Environment ready!

