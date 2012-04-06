/**
 ********************************
 * Node.JS -> Storm DRPC Server *
 ********************************
 * 
 * This server listens in 3 ports
 * ------------------------------
 *
 * Port 8080 Synchronous task execution requests.
 * Port 8081 Pull pending tasks.
 * Port 8082 Push ready tasks results.
 *
 **/

var http = require('http');
var parser = require('url');
var os = require('os');

/**
 * Add the FIFO functionality to Array class.
 **/
Array.prototype.store = function (info) {
  this.push(info);
}

Array.prototype.fetch = function() {
  if (this.length <= 0) { return ''; }  // nothing in array
  return this.shift(); 
}

Array.prototype.display = function() {
  return this.join('\n');
}

// Parameters
var topology_timeout= 20000;
var claim_timeout= 20000;
var base_port = 8080;
var content_type = 'application/json; charset=utf-8';

// Metrics
var total_active_tasks = 0;
var total_requests_made = 0;
var total_requests_answered = 0;

// Error metrics
var not_claimed_tasks = 0;
var not_answered_tasks = 0;

// Tasks FIFO
var pending_tasks= new Array();

// Waiting workers FIFO
var waiting_workers = new Array();

// Current active tasks (Assigned to a worker).
var active_tasks = {};

// This RPC Server Request ID
var global_task_id = 0;

// My IP, to be sent in the origin
var local_ip= null;

function get_local_ip() {
	if(local_ip==null || local_ip=="undefined") {
		var interfaces= os.networkInterfaces();
		for(var interf_name in interfaces) {
			var addresses= interfaces[interf_name];
			for(var addr_name in addresses) {
				var addr= addresses[addr_name];
				if(addr.family=="IPv4" && !addr.internal && (/en\d/.test(interf_name) || /eth\d/.test(interf_name))) {
					local_ip= addr.address;
					return local_ip;
				}	
			}
		}
	}
	return local_ip;
}

if(process.argv.length>2) {
	local_ip = process.argv[2];
}
else {
	console.log("No local IP provided, looking for one...");
	get_local_ip();
	console.log("Using IP: ["+local_ip+"]");
}

if(process.argv.length>3) {
	topology_timeout = parseInt(process.argv[3]);
} else {
	console.log("No topology timeout provided, using default "+topology_timeout);
}

if(process.argv.length>4) {
	claim_timeout = parseInt(process.argv[4]);
} else {
	console.log("No claim timeout provided, using default "+claim_timeout);
}


if(process.argv.length>5) {
	base_port = parseInt(process.argv[5]);
} else {
	console.log("No baseport provided, using default "+base_port);
}

if(process.argv.length>6) {
	content_type = process.argv[6];
} else {
	console.log("No content encoding provided, using default "+content_type);
}


var report = function () {
	var d= new Date();
	console.log("********************************");
	console.log("* Date: "+ d.getFullYear() +"/"+(d.getMonth()+1)+"/"+d.getDate()+" "+d.getHours()+":"+d.getMinutes()+":"+d.getSeconds());
	console.log("* Local IP: "+ local_ip);
	console.log("* Topology TO: "+ topology_timeout);
	console.log("* Claim TO: "+ claim_timeout);
	console.log("* Base Port: " + base_port);
	console.log("* Content-type: " + content_type);
	console.log("************* WORK *************");
	console.log("* Total requests made: "+ total_requests_made);
	console.log("* Total requests answered: "+  total_requests_answered);
	console.log("************ STATUS ************");
	console.log("* Waiting workers: "+ waiting_workers.length);
	console.log("* Pending tasks: "+  pending_tasks.length);
	console.log("* Active tasks: "+ total_active_tasks);
	console.log("************ ERRORS ************");
	console.log("* Tasks not claimed: " + not_claimed_tasks);
	console.log("* Tasks never answered: " + not_answered_tasks);
	console.log("********************************");
}

report();


// If there is a task for a worker, make them meet each other!
function check_queues() {
	if(waiting_workers.length > 0 && pending_tasks.length > 0) {
		var worker =	waiting_workers.fetch();
		var max= worker.max;
		var send = get_local_ip() +"\n";
		var issue_dt= new Date().getTime();

		for(var i=0; (i<max) && (pending_tasks.length > 0);i++){
			var task = pending_tasks.fetch();
			send = send + task.id +"\n";
			send = send + task.query +"\n";
			task.issued_dt= issue_dt;
			active_tasks[task.id] =  task;
			total_active_tasks = total_active_tasks + 1;
		}
		worker.response.end(send);
	}
}

// Server to be used to receive search querys (tasks) and answer them in a synchronous way.
http.createServer(function (request, response) {
	var query= request.url;
	if(query=="/isAlive")
		response.end("YES!");
	else {
		var task_entry = { "id": global_task_id, 
						   "query": query, 
						   "response": response, 
						   "issued_dt" : new Date().getTime() };
		global_task_id = global_task_id+1;
		pending_tasks.store(task_entry);
		total_requests_made++;
		check_queues();
	}
}).listen(base_port);

// Server to be used for requesting pending tasks
http.createServer(function (request, response) {
	var parsed_url= parser.parse(request.url, true);
	var query = request.url;
	if(query=="/isAlive")
		response.end("YES!");
	else {
		var max= parsed_url.query.max;
		if(max==null || typeof(max)=="undefined")
			max=1;
		var waiter = { "request": request, "response": response, "max": max };
		waiting_workers.store(waiter);
		check_queues();
	}
}).listen(base_port+1);

// Response receiver server
http.createServer(function (request, response) {
	var query = request.url;
	if(query=="/isAlive")
		response.end("YES!");
	else {
		if(request.method=="POST") {
	
			var parsed_url= parser.parse(request.url, true);
			var id= parsed_url.query.id;

	
			if(id==null || typeof(id)=="undefined" || 
				active_tasks[id]==null || 
				typeof(active_tasks[id])=="undefined") {
				
				response.writeHead(404);
				response.end("Error ["+id+"] is not a waiting task in this server");
				console.log("Error ["+id+"] is not a waiting task in this server");
			} else {
	
				var data='';
				request.on("data", function(chunk) {
					data += chunk;
				});
			
				request.on("end", function() {
					active_tasks[id].response.writeHead(200, {
					  'Content-Type'  : content_type
					});
	
					active_tasks[id].response.end(data);
					delete active_tasks[id]
					total_active_tasks = total_active_tasks - 1;
					response.end("OK!\n");
					total_requests_answered = total_requests_answered + 1;
				});
			}
		}
	}
}).listen(base_port+2);


// Fail opened requests.
setInterval(function () {
	var now = new Date().getTime();
	for(var active_id in active_tasks) {
		var active = active_tasks[active_id];
		var age= now - active.issued_dt;
		if(age>topology_timeout) {
			console.log("Timeout waiting for topology. ID:" + active_id +" Query:" +active.query + " Age:"+ age);
			active.response.writeHead(500);
			active.response.end("TIMEOUT");
			not_answered_tasks = not_answered_tasks + 1;

			delete active_tasks[active_id]
			total_active_tasks = total_active_tasks - 1;
		}
	}
	
	for(var i = pending_tasks.length-1; i >= 0; i--){ 
		var pending = pending_tasks[i];
		var age =  now - pending.issued_dt;
		if(age>claim_timeout) {
			console.log("Timeout, no one claimed for this task in time. ID:" + pending.task_id +" Query:" +pending.query + " Age:"+ age);
			pending.response.writeHead(500);
			pending.response.end("TIMEOUT");
			not_claimed_tasks = not_claimed_tasks + 1;

			pending_tasks.splice(i, 1);
		}
	}
}, 1000);



// Log status information each 10 seconds interval.
setInterval(report, 10000) ;
