/*SERVER NODEJS DI WALLI. Author: Davide Quadrelli.
All rights reserved*/

//require
var https = require("https");					//connessioni https
var db = require('./include/db.js');				//query col database
var qs = require('querystring');				//per leggere i parametri in post
var img = require('./include/img.js');				//gestione immagini
var fs = require("fs");						//accesso filesystem
//content-type dei file
var mmm = require('mmmagic');

//variabili per il server
var port = 443;							//porta utilizzata dal server
var privateKey = fs.readFileSync('/home/ubuntu/cert/key.pem');
var certificate = fs.readFileSync('/home/ubuntu/cert/CA/2_walli.ddns.net.crt');
var ca = fs.readFileSync('/home/ubuntu/cert/CA/root.crt');
var intermediate = fs.readFileSync('/home/ubuntu/cert/CA/1_Intermediate.crt');
var passphrase = "mi piace la banana";

var credentials ={
	key:privateKey,
	cert:certificate,
	passphrase:passphrase,
	ca:intermediate,
	ciphers: 'ECDHE-RSA-AES256-SHA:AES256-SHA:RC4-SHA:RC4:HIGH:!MD5:!aNULL:!EDH:!AESGCM',
    	honorCipherOrder: true};
//funzione che fa parsing dei parametri in post
function getParam(data){
	return qs.parse(data);
}


//funzione che recupera i parametri dalla request e riparte con il flusso di esecuzione della callback
function execCall(request,response,callback){
	var data="";
	//recupero i parametri
	request.on('data',function(chunk){
		data += chunk.toString();
	});
	//eseguo la callback
	request.on('end',function(){
		try{
			db.setConnection();
			var param = getParam(data);
			callback(param);
		}catch(e){
			console.log(e);
			response.statusCode = 500;
			response.end("");
			db.closeConnection();
		}
	});
}

//ripristino da backup i dati
db.restoreLogin();
db.restoreTokens();

//inzializzo il server
var server = https.createServer(credentials,function(request,response){	
	var url = request.url;
	try{
		//login account
		if (url.indexOf("login") > -1){
			execCall(request,response,function(param){
				db.login(response,param.user,param.pwd,param.token,param.platform);
			});
		}
		//logout account
		else if(url.indexOf("logout") > -1){
			execCall(request,response,function(param){
				db.logout(response,param.id,param.key,param.token);
			});
		}
		//creazione nuovo account
		else if(url.indexOf("signUp") > -1){
			execCall(request,response, function(param){
				db.createNewAccount(response,param.nick,param.mail,param.name,param.surname,param.cell,param.pwd);
			});
		}
		//conferma iscrizione nuovo account
		else if(url.indexOf("confirmSubscription") > -1){
			db.setConnection();
			var mail = url.split('?')[1].split('=')[1];
			mail = unescape(mail);
			db.confirmSubscription(response,mail);
		}
		//elenco dei gruppi di un utente
		else if (url.indexOf("getGroupsByUser") > -1) {
			execCall(request,response,function(param){
				db.getGroups(response,param.id,param.key);
			});
		}
		//elenco degli utenti all'interno di un gruppo
		else if (url.indexOf("getUsersByGroup") > -1){
			execCall(request,response,function(param){
				db.getUsersByGroup(response,param.id,param.key,param.g_id);
			});
		}
		//elenco di tutti gli utenti con cui ho a che fare
		else if(url.indexOf("getAllMyUsers") > -1){
			execCall(request,response,function(param){
				db.getAllMyUsers(response,param.id,param.key);
			});
		}
		//elenco delle spese di un gruppo
		else if (url.indexOf("getSpeseByGroup") > -1){
			execCall(request,response,function(param){
				db.getSpeseByGroup(response,param.id,param.key,param.g_id);
			});
		}
		//elenco spese di un utente (per generare grafici)
		else if(url.indexOf("getSpeseForCharts") > -1){
			execCall(request,response,function(param){
				db.getSpeseForCharts(response,param.id,param.key,param.date1,param.date2,param.valuta);
			});
		}
		//crediti e debiti di un utente (per grafico)
		else if(url.indexOf("getCreditDebtForCharts") > -1){
			execCall(request,response,function(param){
				db.getCreditDebtForCharts(response,param.id,param.key,param.valuta);
			});
		}
		//inserisce una nuova spesa in un gruppo
		else if(url.indexOf("insertSpesa") > -1){
			execCall(request,response,function(param){
				db.insertSpesa(response,param.id, param.key, param.g_id, param.description, param.value, param.n_id);
			});
		}
		//elenco debiti/crediti verso utenti di un gruppo
		else if(url.indexOf("getCreditDebitByGroup") > -1){
			execCall(request,response, function(param){
				db.getCreditDebitByGroup(response,param.id,param.key,param.g_id);
			});
		}
		//elenco debiti/crediti verso chiunque
		else if(url.indexOf("getCreditDebit") > -1){
			execCall(request,response, function(param){
				db.getCreditDebit(response,param.id,param.key,param.valuta);
			});
		}
		//elenco delle notifiche di spesa
		else if(url.indexOf("getNotificheByGroup") > -1){
			execCall(request,response, function(param){
				db.getNotificheByGroup(response,param.id,param.key,param.g_id);
			});
		}
		//inserisce una notifica di spesa in un gruppo
		else if(url.indexOf("insertNotifica") > -1){
			execCall(request,response, function(param){
				db.insertNotifica(response,param.id,param.key,param.g_id,param.description);
			});
		}
		//aggiorna i dati del profilo utente
		else if(url.indexOf("updateProfileData") > -1){
			execCall(request,response,function(param){
				db.updateProfileData(response,param.id,param.key,param.name,param.surname,param.mail,param.phone,param.pwd);
			});
		}
		//richiesta per la password dimenticata
		else if(url.indexOf("restorePassword") > -1){
			execCall(request,response,function(param){
				db.restorePassword(response,param.mail);
			});
		}
		// richiedo suggerimenti sugli utenti da aggiungere
		else if(url.indexOf("getHints") > -1){
			execCall(request,response,function(param){
				db.getHints(response,param.id,param.key,param.text);
			});
		}
		//aggiornamento/creazione di un gruppo
		else if(url.indexOf("updateGroup") > -1){
			execCall(request,response,function(param){
				db.updateGroup(response,param.id,param.key,param.g_id,param.user_ids,param.g_name,param.valuta);
			});
		}
		//controlla se è una spesa modificabile
		else if(url.indexOf("isChangeableShop") > -1){
			execCall(request,response, function(param){
				db.isChangeableShop(response,param.id,param.key,param.s_id);
			});
		}
		//elimina spesa
		else if(url.indexOf("deleteShop") > -1){
			execCall(request,response,function(param){
				db.deleteShop(response,param.id,param.key,param.s_id);
			});
		}
		//esce dal gruppo
		else if(url.indexOf("deleteGroup") > -1){
			execCall(request,response,function(param){
				db.deleteGroup(response,param.id,param.key,param.g_id);
			});
		}
		//aggiorna una spesa
		else if(url.indexOf("changeShop") > -1){
			execCall(request,response,function(param){
				db.changeShop(response,param.id,param.key,param.s_id,param.desc,param.value);
			});
		}
		//aggiorna/cancella una notifica di spesa
		else if(url.indexOf("updateNotify") > -1){
			execCall(request,response,function(param){
				db.updateNotify(response,param.id,param.key,param.n_id,param.desc);
			});
		}
		//segna come pagato un debito
		else if(url.indexOf("setAsPaid") > -1){
			execCall(request,response,function(param){
				db.setAsPaid(response,param.id,param.key,param.u_id,param.g_id);
			});
		}
		//salva immagine
		else if(url.indexOf("saveImage") > -1){
			execCall(request,response,function(param){
				img.saveImage(response,param.id,param.key,param.type,param.img_id,param.img_ext,param.raw_img);
			});
		}
		//recupera immagine
		else if(url.indexOf("getImage") > -1){
			execCall(request,response,function(param){
				img.getImage(response,param.id,param.key,param.type,param.img_id);
			});
		}
		//salva immagine
		else if(url.indexOf("checkImage") > -1){
			execCall(request,response,function(param){
				img.checkImage(response,param.id,param.key,param.type,param.img_id,param.timestamp);
			});
		}
		//manda notifica ad un altro utente di pagare i suoi debiti
		else if(url.indexOf("askForPayment") > -1){
			execCall(request,response,function(param){
				db.askForPayment(response,param.id,param.key,param.u_id);
			});
		}
		else{
			//mi comporto come un web server
			try{
				var baseurl = "/home/ubuntu/lam/Sito_Walli";
				if(url=="/" || url=="") url = "/index.html";
				var typer = new mmm.Magic(mmm.MAGIC_MIME_TYPE);
				typer.detectFile(baseurl+url,function(err,result){
					if(err) throw err;
					try{
						if(url.split('.').pop() == "css") result = "text/css";
						var file = fs.readFileSync(baseurl+url);
						response.writeHead(200,{'Content-type':result});
						response.end(file);
					}catch(e){
						console.log(e);
						response.statusCode = 404;
						response.end("Page Not Found");
					}	
				});
			}catch(e){
				console.log(e);
				response.statusCode = 404;
				response.end("Page Not Found");
			}
		}
		//richiesta palesemente scazzata. Io mando a fanculo a tutti e tutto
		// else {
		// 	response.writeHead(403);
		// 	response.end("FORBIDDEN: uri not found");
		// 	db.closeConnection();
		// 	return;
		// }
	}catch(e){
		console.log(e);
	}
});
server.listen(port);
server.maxHeadersCount = 0;	//per non far andare in timeout il server con richieste lunghe, come l'invio di immagini

process.on('SIGTERM',function(){
	console.log('server in chiusura. Salvo token e chiavi ..');
	db.backupLogin();
	db.backupTokens();
	console.log("...done. Bye");
	process.exit();
});

console.log('http started on port '+ port);

