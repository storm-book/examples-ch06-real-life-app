pkill -9 node
SERVER_NAME=$(ec2-metadata --public-hostname |cut -c 18-)

mkdir ../logs

# Start the Web Server
node ../node-js-server/node-drpc-server.js $SERVER_NAME 1000 1000 8080 1> ../logs/full_web_server.log 2>../logs/full_web_server.err &

# Start the Index Server
node ../node-js-server/node-drpc-server.js $SERVER_NAME 1000 1000 9090 1> ../logs/full_index_server.log 2>../logs/full_index_server.err &
