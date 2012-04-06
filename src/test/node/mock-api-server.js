var http = require('http');

var port = 8888;

if(process.argv.length>2)
	port = process.argv[2];
else 
	console.log("No port provided, using: " + port);

var my_objects={}

//Create the http server
http.createServer(function (request, response) {
		if(request.method=="POST") {
			var data='';
			request.on("data", function(chunk) {
				data += chunk;
			});
			request.on("end", function() {
				my_objects[request.url] = data;
				response.writeHead(200);
				response.end();
			});
			
		} else if(request.method=="DELETE") {
			if(request.url == "/"){
				my_objects = {} //Clear the full collection
				response.writeHead(200);
				response.end();
			}
			else {
				delete my_objects[request.url]
				response.writeHead(200)
				response.end();
			}
		} else if(request.method=="GET") {
			var data = my_objects[request.url];

			if(data==null || typeof(data)=="undefined") {
				response.writeHead(404)
				response.end()
			}

			response.writeHead(200,{
    	      'Content-Type'  : 'application/json; charset=utf-8'
        	});

			response.end(data);

		}
}).listen(port);
