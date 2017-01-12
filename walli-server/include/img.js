/*SERVER NODEJS DI WALLI. Author: Davide Quadrelli.
All rights reserved*/

//require
var lwip = require('lwip');			// https://github.com/EyalAr/lwip
var db = require("./db.js");
var fs = require("fs");	
var crypto = require('crypto');

//attributi globali
var user_path = "/home/ubuntu/lam/image/user/";
var group_path = "/home/ubuntu/lam/image/group/";
var ext = "png";

var time_path = "/home/ubuntu/lam/image/timestamp.json";

//funzioni ausiliarie
function getImageName(type,img_id,img_ext){
	var img_name;
	if(type == "user") img_name = user_path;
	else img_name = group_path;
	img_name += img_id+"."+img_ext;
	return img_name;
}

function setTimestamp(type,id,timestamp){
	id = parseInt(id);
	var dict;
	try{
		var txt = fs.readFileSync(time_path);
		dict = JSON.parse(txt);
	}catch(e){
		dict = {};
	}
	if(!dict[type]) dict[type]={};
	dict[type][id] = timestamp;
	var txt = JSON.stringify(dict);
	fs.writeFileSync(time_path,txt);
}

function getTimestamp(type,id){
	try{
		var txt = fs.readFileSync(time_path);
		var dict = JSON.parse(txt);
		if(dict[type]) return dict[type][id]+"";
	}catch(e){
		return null;
	}
}


//richieste esterne

//salva un'immagine
function saveImage(response,id,key,type,img_id,img_ext,raw_img){
	if(db.isLogged(id,key)){
		//genero nome dell'immagine
		var img_name = getImageName(type,img_id,img_ext);
		//creo immagine
		buff = new Buffer(raw_img,"base64");
		fs.writeFileSync(img_name,buff,null);
		//posso iniziare a manipolare l'immagine
		lwip.open(img_name,function(err, image){
			if(err) throw err;
  			image.batch()
		    .writeFile(img_name,ext,function(err){
		      if(err) throw err;
		      //immagine salvata. Genero timestamp
		      var timestamp = new Date().getTime();
		      setTimestamp(type,img_id,timestamp);
		      var toret = {response:timestamp};
		      db.end(response,200,JSON.stringify(toret));
		    });
		});
	}else db.notLogged(response);
}

//recupera un'immagine
function getImage(response,id,key,type,img_id){
	if(db.isLogged(id,key)){
		//cerco l'immagine se esiste
		var img_name = getImageName(type,img_id,ext);
		var img_data,toret;
		try{
			img_data = fs.readFileSync(img_name,null);
			//immagine esiste!
			img_data = img_data.toString('base64');
			toret = JSON.stringify({response:img_data,timestamp:getTimestamp(type,img_id)});
		}catch(e){
			//immagine non esiste!
			toret = JSON.stringify({response:"default"});
		}
		db.end(response,200,toret);
	}else db.notLogged(response);
}

//controlla se un'immagine in cache è aggiornata
function checkImage(response,id,key,type,img_id,timestamp){
	if(db.isLogged(id,key)){
		var time = getTimestamp(type,img_id);
		if(time == null || time != timestamp) toret = JSON.stringify({response:"to-update"});
		else toret = JSON.stringify({response:"up-to-date"});
		db.end(response,200,toret);
	}else db.notLogged(response);
}

//funzioni esportate
module.exports = {
	saveImage:saveImage,
	getImage:getImage,
	checkImage:checkImage
}
