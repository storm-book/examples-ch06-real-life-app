
/**;
 * Module dependencies.
 */

var express = require('express')
  , routes = require('./routes')
  , redis = require("redis")
  , client = redis.createClient()
  , uuid = require('node-uuid');


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


var app = module.exports = express.createServer();

function populate_redis() {
	var test_products = [
		{title:"Dvd player with surround sound system", category:"Players", price: 100},
		{title:"Full HD Bluray and DVD player", category:"Players", price:130},
		{title:"Media player with USB 2.0 input", category:"Players", price:70},

		{title:"Full HD Camera", category:"Cameras", price:200},
		{title:"Waterproof HD Camera", category:"Cameras", price:300},
		{title:"ShockProof and Waterproof HD Camera", category:"Cameras", price:400},
		{title:"Reflex Camera", category:"Cameras", price:500},

		{title:"DualCore Android Smartphon with 64Gb SD card", category:"Phones", price:200},
		{title:"Regular Movile Phone", category:"Phones", price:20},
		{title:"Satellite phone", category:"Cameras", price:500},

		{title:"64Gb SD Card", category:"Memory", price:35},
		{title:"32Gb SD Card", category:"Memory", price:27},
		{title:"16Gb SD Card", category:"Memory", price:5},

		{title:"Pink smartphone cover", category:"Covers", price:20},
		{title:"Black smartphone cover", category:"Covers", price:20},
		{title:"Kids smartphone cover", category:"Covers", price:30},

		{title:"55 Inches LED TV", category:"TVs", price:800},
		{title:"50 Inches LED TV", category:"TVs", price:700},
		{title:"42 Inches LED TV", category:"TVs", price:600},
		{title:"32 Inches LED TV", category:"TVs", price:400},

		{title:"TV Wall mount bracket 32-42 Inches", category:"Mounts", price:50},
		{title:"TV Wall mount bracket 50-55 Inches", category:"Mounts", price:80}
	];

	for(var i=0; i < test_products.length ;i++) {
		var product = test_products[i];
		product['id'] = i;
		var str = JSON.stringify(product);
		console.log("Inserting test product: [" + i  +"]:"+str);
		client.set(i, str);
		client.sadd("categories", product.category);
		client.sadd(product.category, product.id);
		client.sadd("products", product.id);
	}
}

populate_redis();

// Configuration

app.configure(function(){
  app.set('views', __dirname + '/views');
  app.set('view engine', 'jade');
  app.use(express.cookieParser());
  app.use(express.bodyParser());
  app.use(express.methodOverride());
  app.use(app.router);
  app.use(express.static(__dirname + '/public'));
});

app.configure('development', function(){
  app.use(express.errorHandler({ dumpExceptions: true, showStack: true }));
});

app.configure('production', function(){
  app.use(express.errorHandler());
});

app.get('/product/:id/stats', function(req, resp) {
	var id = req.params.id;
	client.hgetall("prodcnt:"+id, function(err, res) {
		if(err!=null)
			resp.render('redis_error.jade', { title: "Error reading from redis:"+err});
		else {
			if(res!=null) {
				var title = "Stats for product:"+id;
				resp.render('stats.jade', {title: title, stats: res});
			} else {
				resp.render('not_found.jade', {title: "No stats available vor this product"});
			}
		}
	});
});


app.get('/product/:id', function(req, res) {
	var id= req.params.id;

    if(req.cookies.uid==null) {
        console.log("Unidentified User");
        var uid = uuid.v4();
        res.cookie("uid", uid,  { expires: new Date(Date.now() + 9000000000)});
    } else {
        var entry = JSON.stringify({user: req.cookies.uid, product: req.params.id, type: "PRODUCT"});
		client.lpush('navigation', entry);
    }

	client.get(id, function(err, reply) {
		if(err!=null) {
			res.render('redis_error.jade', {title: "Error reading from redis"+err});
			return ;
		}

		if(reply==null) {
			res.render('not_found.jade', {title: "Producto not found"});
			return ;
		}

		var product = JSON.parse(reply);
		res.render('product.jade', product);
	});

});


function get_all_data(fn) {
	var products = {};
    client.smembers('products', function(err, resp){
		var ids = resp;
		var readed = 0;
		for(var i = 0 ; i < ids.length ; i++) {
			client.get(ids[i], function(err, resp) {
				products[ids[readed]] = JSON.parse(resp);
				readed ++;
				if(readed == ids.length)
					fn(products);
			});
		}
    });
}

app.post('/news', function(req, res) {
	var body = "";
	console.log("NEWS!!");

	req.on('data', function (data) {
		body += data;
	});

	req.on('end', function () {
		console.log("NEWS!:"+ body);
		res.send("OK");
	});
});


app.get('/', function(req, res) {

	get_all_data(function(products){
		var data = {title: "Main page", products:products};
		console.log(JSON.stringify(data));
		res.render('index.jade', data);
	});
});

app.listen(3000, function(){
  console.log("Express server listening on port %d in %s mode", app.address().port, app.settings.env);
});
