pkill -9 node
mkdir ../logs
nohup node ../src/test/node/mock-api-server.js 1> ../logs/mock-items-api-server.log 2> ../logs/mock-items-api-server.err &
