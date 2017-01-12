var mysql = require('mysql');
var crypto = require('crypto');
var queries = require('./query.js');
var request = require('request');
var fs = require('fs');
var pwd_generator = require('randomstring');
//per firebase
var firebase_key = 'AIzaSyBLp_6xXajVALwW2PuJmsHXjNrtgw2zWV0';

//var per le operazioni di conversioni valute
var fx = require('money');
var exchange_key = "54b82e7334ed4cb78e8406a93b8d227c";	//https://openexchangerates.org
var interval_exchange;
//var per le mail
var exec = require('child_process').exec;	//esecuzione comandi su shell
var mail_command = 'echo "%DATA%" | mail -s "%SUBJ%" -a "MIME-Version: 1.0" -a "Content-Type: text/html" -a "From: noreply@walli.ddns.net" %DST%';
var day_in_ms = 1000*60*60*24;
var CONFIRMATION_MAIL_URL = '/home/ubuntu/lam/mail/confirmationMail.html';
var FORGOT_PWD_MAIL_URL = '/home/ubuntu/lam/mail/forgotPasswordMail.html';
//variabile di connessione col db
var connection;
var timerConnection = undefined;
var timeoutConnection = 5*1000*60;
var isConnected = false;

//variabile contenente le chiavi delle sessioni aperte
var login_keys = [];
//percorso dove fare backup delle chiavi
var login_path = '/home/ubuntu/lam/data/login.txt';
//variabile contenente le notifiche eseguite
var notification_sent = {};
//variabile contenente gli utenti non ancora confermati per mail
var userNotConfirmed = {};

//backup dei login eseguiti
function backupLogin(){
	try{
		var data = JSON.stringify(login_keys);
		fs.writeFileSync(login_path,data);
	}catch(e){
		console.log("errore nel salvataggio delle chiavi per il login");
	}
}

//ripristino i login in memoria da backup
function restoreLogin(){
	try{
		var data = fs.readFileSync(login_path,'utf-8');
		login_keys = JSON.parse(data);
	}catch(e){
		console.log("errore nel ripristino delle chiavi per il login");
	}
}
//imposto aggiornamento giornaliero delle richieste
interval_exchange = setInterval(function(){
		updateExchange();
	},day_in_ms);

function updateExchange(){
	var url = 'https://openexchangerates.org/api/latest.json?app_id='+exchange_key;
	request(url,function(error,response,body){
		var data = JSON.parse(body);
        fx.rates = data.rates;
        fx.base = data.base;
	});
}
//aggiorno valori di conversione
updateExchange();

//funzione privata per inizializzare il collegamento col db
function setConnection(){
	if(!isConnected){
		connection = mysql.createConnection({
	   host     : 'localhost',
	   user     : 'Walli',
	   password : 'W4Ll1_$erv3r',
	   database : 'WalliDB'
	});
		connection.connect();
		isConnected = true;
		timerConnection = setTimeout(function(){
			timerConnection = undefined;
			closeConnection();
		},timeoutConnection);
	}
}
//funzione privata per chiudere la connessione col db
function closeConnection(){
	if(isConnected){
		connection.end();
		if(timerConnection){
			clearTimeout(timerConnection);
			timerConnection = undefined;
		}
		isConnected = false;
	}
}

function end(response,code,toret){
	response.statusCode = code;
	response.end(toret);
	//closeConnection();
}

//funzione per inviare una mail
function sendMail(response,to,subject,data){
	//catturo tutte le virgolette all'interno del soggetto e nel corpo della mail
	var regex = new RegExp('"','g');
	var replacer = '\\"';
	data = data.replace(regex,replacer);
	//catturo anche gli a capo altrimenti la bash la prende male
	data = data.replace(/\n/g,"");
	subject = subject.replace(regex,replacer);
	//genero il comando
	var cmd = mail_command.replace(/%DATA%/,data).replace(/%SUBJ%/,subject).replace(/%DST%/,to);
	exec(cmd, function (error,output,outerr){
		if(!error){
			toret = {response:"ok"};
			end(response,200,JSON.stringify(toret));
		}
	});
}

//funzione per eseguire le query e gestire la callback in un try
function execQuery(response,query,callback){
	connection.query(query,function(err,rows,fields){
		try{
			callback(err,rows,fields);
		}catch(e){
			console.log(e);
			end(response,500,"");
		}
	});
}

//funzione per "Password Dimenticata"
function restorePassword(response,mail){
	var query = queries.checkMail.replace("%mail%",mail);
	execQuery(response,query,function(err,rows,fields){
		if(err) throw err;
		if(rows.length == 1){
			//ok. genero nuova password
			var pwd = pwd_generator.generate(8);
			var clear_pwd = pwd;
			//genero md5
			var md5 = crypto.createHash('md5').update(pwd).digest("hex");
			md5 = crypto.createHash('md5').update(md5).digest("hex");
			//salvo sul db
			var query = queries.updatePwd.replace("%id%",rows[0].u_id).replace("%pwd%",md5);
			execQuery(response,query,function(err,rows,fields){
				if(err) throw err;
				//end(response,200,JSON.stringify(response:"done"));
				sendRestoreMail(response,mail,clear_pwd);
			});
		}else{
			//email inesistente
			var toret = {response:"denied"};
			end(response,200,JSON.stringify(toret));
		}
	});
}

function sendRestoreMail(response,mail,pwd){
	var data = fs.readFileSync(FORGOT_PWD_MAIL_URL,'utf-8');
	data = data.replace("%Password%",pwd);
	var subj = "Restore password for Walli";
	sendMail(response,mail,subj,data);
}

//funzione per tentare il login sul database
function login(response,username, password,token,platform){
	password = crypto.createHash('md5').update(password).digest("hex");
	//eseguo query
	var query = queries.login.replace("%USER%",username).replace("%PWD%",password);
	execQuery(response,query,function(err,rows,fields){
		if (err) {
			throw err;
		}
		//login eseguito correttamente
		response.setHeader('Content-Type', 'text/json');
		//inserisco una chiave per autenticare il client alle prossime comunicazioni
		var key = new Date().getTime();
		key = crypto.createHash('md5').update(String(key)).digest("hex");
		var id = rows[0].u_id;
		//memorizzo la chiave legata all'id
		if(login_keys[id]){
			login_keys[id].push(key);
		}else{
			login_keys[id]=[key];
		}
		//START: FIREBASE CODE
		if(token!=undefined) setToken(id,token,platform);
		//END: FIREBASE CODE

		//inserisco la chiave generata nella risposta
		rows[0].key = key;
		result = JSON.stringify(rows);
		//rispondo 
		end(response,200,result);
	});
}

//funzione che esegue il logout, ovvero, rimuovo la chiave di accesso per la sessione del login terminata
function logout(response,id,key,token){
	id = parseInt(id);
	var i = login_keys[id].indexOf(key);
	var statusCode;
	if(i > -1){
		delete login_keys[id][i];
		statusCode = 200;
	}else{
		statusCode = 401;
	}
	//START: FIREBASE CODE
	if(token!=undefined) removeToken(id,token);
	//END: FIREBASE CODE

	toret = {response:"ok"};
	end(response,statusCode,JSON.stringify(toret));
}

//funzione che controlla se si è loggati
function isLogged(id,key){
	return login_keys[id].indexOf(key) > -1;
}

//funzione per la creazione di un nuovo account
function createNewAccount(response,nick,mail,name,surname,cell,pwd){
	var query = queries.checkExistentUser.replace("%NICK%",nick).replace("%MAIL%",mail);
	execQuery(response,query,function(err,rows,fields){
		if(err){
			throw err;
		}
		//controllo se l'utente esiste gia'
		if(rows.length > 0){
			var toret = {response:"user_taken"};
			end(response,401,JSON.stringify(toret));
		}else{
			//utente creabile.
			pwd = crypto.createHash('md5').update(pwd).digest("hex");
			userNotConfirmed[mail] = {
				nick:nick,
				name:name,
				surname:surname,
				cell:cell,
				pwd:pwd,
				timer:setTimeout(function(){
					delete userNotConfirmed[mail];
				},day_in_ms)
			};
			//memorizzo sul server l'utente solo fino alla conferma per mail
			sendConfirmationEmail(response,mail,nick);
		}
	});
}

//funzione per confermare l'avvenuta registrazione
function confirmSubscription(response,mail){
	var user = userNotConfirmed[mail];
	//annullo il timer di cancellazione dati
	clearTimeout(user.timer);
	delete userNotConfirmed[mail];
	var query = queries.insertUser.replace("%NICK%",user.nick).replace("%MAIL%",mail).replace("%NOME%",user.name);
	query = query.replace("%COGNOME%",user.surname).replace("%CELL%",user.cell).replace("%PWD%",user.pwd);
	execQuery(response,query,function(err,result){
		if(err){
			throw err;
		}
		if(result.affectedRows == 1){
			end(response,200,"Subscription confirmed.");
		}
	});
}

//funzione per inviare mail di conferma creazione account
function sendConfirmationEmail(response,mail,nick){
	fs.readFile(CONFIRMATION_MAIL_URL,'utf-8',function (err,data){
		if(err){
			throw err;
		}
		var mailBody = data.replace(/%NICK%/g,nick).replace(/%MAIL%/g,mail);
		var subj = "Subscription request for Walli";
		sendMail(response,mail,subj,mailBody);
	});
}

//funzione per ottenere elenco dei gruppi dell'utente
function getGroups(response,u_id,key){
	if (isLogged(u_id,key)){
		var query = queries.getGroups.replace(/%ID%/g,u_id);
		response_back(response,query);
	}else notLogged(response);
}

//funzione per ottenere elenco degli utenti di un gruppo
function getUsersByGroup(response,u_id,key,g_id){
	if (isLogged(u_id,key)){
		var query = queries.getUsersByGroup.replace("%ID%",g_id);
		response_back(response,query);
	}else notLogged(response);
}

//funzione per ottenere elenco utenti con cui ho a che fare
function getAllMyUsers(response,id,key){
	if(isLogged(id,key)){
		var query = queries.getAllKnownUsers.replace("%ID%",id);
		response_back(response,query);
	}else notLogged(response);
}

//funzione per restituire nickname che fanno match con quello che l'utente hascritto
function getHints(response,id,key,hint){
	if(isLogged(id,key)){
		var query = queries.getUsersByHint.replace("*hint*",hint);
		response_back(response,query);
	}else notLogged(response);
}

//funzione per ottenere le spese di un gruppo
function getSpeseByGroup(response,id,key,g_id){
	if (isLogged(id,key)){
		var query = queries.getSpeseByGroup.replace("%ID%",g_id);
		response_back(response,query);
	}else notLogged(response);
}

//funzione per ottenere situazione di crediti e debiti di un utente in un gruppo
function getCreditDebitByGroup(response,id,key,g_id){
	if(isLogged(id,key)){
		var query = queries.getCreditDebitByGroup.replace(/%ID%/g,id).replace(/%G_ID%/g,g_id);
		response_back(response,query);
	}else notLogged(response);
}

////funzione per ottenere situazione di crediti e debiti di un utente
function getCreditDebit(response,id,key,valuta){
	if(isLogged(id,key)){
		var query = queries.getCreditDebit.replace(/%u_id%/g,id);
		execQuery(response,query,function(err,rows,fields){
			var inserted = [];
			var now;
			for (i in rows){
				var actual = rows[i];
				if(actual.credito == null) actual.credito = 0;
				if(actual.debito == null) actual.debito = 0;
				if(valuta != actual.g_valuta)
					actual.credito = fx.convert(actual.credito,{from:actual.g_valuta,to:valuta});
					actual.debito = fx.convert(actual.debito,{from:actual.g_valuta,to:valuta});
				if(i==0){
					now = actual;
					continue;
				}
				if(now.u_id == actual.u_id){
					now.credito += actual.credito;
					now.debito += actual.debito;
				}else{
					delete now.g_valuta;
					inserted.push(now);
					now = actual;
				}
			}
			delete now.g_valuta;
			inserted.push(now);
			result = JSON.stringify(inserted);
			//rispondo 
			end(response,200,result);
		});
	}else notLogged(response);
}

//funzione per ottenere le notifiche di spesa da fare
function getNotificheByGroup(response,id,key,g_id){
	if(isLogged(id,key)){
		var query = queries.getNotificheByGroup.replace("%G_ID%",g_id);
		response_back(response,query);
	}else notLogged(response);
}

//funzione per inserire una nuova spesa
function insertSpesa(response,id,key,g_id,desc,value,n_id){
	if(isLogged(id,key)){
		//se parto da una notifica, prima di tutto la rimuovo
		if(n_id != undefined){
			var query = queries.deleteNotificaSpesa.replace("%ID%",n_id);
			execQuery(response,query,function(err,result){
				if(err){
					throw err;
				}
			});
		}
		//rimuovo caratteri rompi azzo dalla descrizione
		desc = desc.replace("'","\\'");
		var date = new Date().toISOString();
		query = queries.insertSpesa.replace("%valore%",value).replace("%descrizione%",desc);
		query = query.replace("%data%",date).replace("%pagato%",0).replace("%idGruppo%",g_id).replace("%idUtente%",id);
		execQuery(response,query,function(err,result){
		if(err){
			throw err;
		}
		if(result.affectedRows == 1){
			//la spesa è stata inserita correttamente. Ora devo generare le quote.
			var fbm_push = {g_id:g_id,desc:desc,value:value,u_id:id};
			insertQuote(response,date,result.insertId,id,g_id,value,fbm_push);
		}else{
			//si è verificato un errore
			var toret = {s_id:-1,date:""};
			end(response,200,JSON.stringify(toret));
		}
	});
	}else notLogged(response);
}

function insertNotifica(response,id,key,g_id,desc){
	if(isLogged(id,key)){
		//rimuovo caratteri rompi azzo dalla descrizione
		desc = desc.replace("'","\\'");
		var query = queries.insertNotifica.replace("%desc%",desc).replace("%id%",id).replace("%g_id%",g_id);
		execQuery(response,query,function(err,result){
			if(err){
				throw err;
			}
			if(result.affectedRows == 1){
				//notifica correttamente inserita
				var toret = {n_id:result.insertId};
				end(response,200,JSON.stringify(toret));
				//gestisco notifica push
				var fbm_push = {desc:desc,g_id:g_id};
				newNotifyShop_push(id,fbm_push);
			}else{
				//si è verificato un errore
			var toret = {n_id:-1};
			end(response,200,JSON.stringify(toret));
			}
		});
	}else notLogged(response);
}

//funzione per generare le quote di una spesa
function insertQuote(response,date,s_id,u_id,g_id,value,fbm_push){
	var n;
	var query = queries.getGroupDim.replace('%g_id%',g_id);
	execQuery(response,query, function (err,rows,fields){
		if(err){
			throw err;
		}
		n = rows[0].n;
		value = parseFloat(value);
		var q_value = value/n;	//valore singola quota
		query = queries.insertQuotaStart;
		for(var i = 0; i<n; i++){
			//recupero id
			var id = rows[i].u_id;
			//setto valore pagato a 0 per tutti gli utenti tranne per chi inserisce la spesa
			var pagato = 0;
			//non genero la data per tutti tranne che per chi inserisce la spesa (ha gia' pagato la sua quota)
			var data = "NULL";
			if(id == u_id){
				pagato = 1;
				data = new Date().toISOString();
			}
			var to_add = "\n"+queries.insertQuotaMiddle.replace('%valore%',q_value).replace('%pagato%',pagato);
			to_add = to_add.replace('%data%',data).replace("%idUtente%",id).replace('%idSpesa%',s_id);
			query += to_add;
		}
		//rimuovo ultima virgola
		query = query.slice(0, -1);
		//finisco query
		query += queries.insertQuotaEnd;
		//eseguo la query
		execQuery(response,query,function(err,result){
			if(err){
				throw err;
			}
			if(result.affectedRows == n){
				var toret = {date:date,s_id:s_id};
				end(response,200,JSON.stringify(toret));
				//gestisco notifica push
				var id = fbm_push.u_id;
				delete fbm_push.u_id;
				newShop_push(u_id,fbm_push);
			}else{
				throw "Non tutte le quote sono state salvate. OMG!!";
			}
		});
	});
}

//funzione per aggiungere/modificare un gruppo
function updateGroup(response,id,key,g_id,user_ids,g_name,valuta){
	if(isLogged(id,key)){
		var query = "";
		var push_notification;
		if(user_ids) user_ids = JSON.parse(user_ids);
		if(g_id != undefined){
			//il gruppo esiste già ed è da modificare. Elimino utenti esistenti se ne ho di nuovi
			if(user_ids && user_ids.length > 0)query = queries.deleteUtenteGruppo.replace("%g_id%",g_id);
			else if(g_name && valuta){
					query = queries.updateGruppo.replace("%g_nome%",g_name).replace("%g_valuta%",valuta).replace("%g_id%",g_id);
					execQuery(response,query,function(err,result){
						if(err) throw err;
						var toret = {g_id:g_id};
						end(response,200,JSON.stringify(toret));
						return;
					});
				}
		}else{
			// il gruppo è da creare
			query = queries.insertGroup.replace("%nome%",g_name).replace("%valuta%",valuta).replace("%u_id%",id);
		}
		//eseguo la prima query preliminare
		execQuery(response,query,function(err,result){
			if(err) throw err;
			if(g_id == undefined){
				g_id = result.insertId+"";
				var fbm_push = {name:g_name,id:g_id};
			}
			if(user_ids && user_ids.length > 0) addUsersToGroup(response,id,g_id,user_ids,fbm_push);
			else{
				var toret = {g_id:g_id};
				end(response,200,JSON.stringify(toret));
			}
	});
	}else notLogged(response);
}

// funzione per mettere gli utenti dentro utenti al gruppo
function addUsersToGroup(response,id,g_id,user_ids,fbm_push){
	var query = queries.insertPartecipazioneStart;
	var ids = user_ids.slice(0);
	ids.push(id);
	for(var i in ids){
		query+= queries.insertPartecipazioneMiddle.replace("%u_id%",ids[i]).replace("%g_id%",g_id)+"\n";
	}
	query = query.substring(0,query.length-2)+queries.insertPartecipazioneEnd;
	execQuery(response,query,function(err,result){
		if(err) throw err;
		if(result.affectedRows == ids.length){
			end(response,200,JSON.stringify({g_id:g_id}));
			newGroup_push(user_ids,fbm_push);
		}else{
			end(response,200,JSON.stringify({g_id:-1}));
		}
	});
}

//funzione per ottenere le spese di un utente in una finestra di tempo
function getSpeseForCharts(response,id,key,d1,d2,valuta){
	if(isLogged(id,key)){
		var query = queries.getSpeseForCharts.replace(/%id%/g,id).replace(/%d1%/g,d1).replace(/%d2%/g,d2);
		execQuery(response,query,function(err,rows,fields){
			if(err) throw err;
			var toret = {};
			for(i in rows){
				var row = rows[i];
				var valore = fx.convert(row.valore,{from:row.valuta,to:valuta});
				var date = row.data.toISOString().substring(0,10);
				if(!toret[date]) toret[date] = 0;	//inizializzo
				if(row.tipo == "entrata")
					toret[date] += valore;
				else
					toret[date] -= valore;
			}
			toret = JSON.stringify(toret);
			end(response,200,toret);
		});
	}else notLogged(response);
}

//funzione per sapere situazione globale
function getCreditDebtForCharts(response,id,key,valuta){
	if(isLogged(id,key)){
		var credito = 0;
		var debito = 0;
		var query = queries.getCreditForCharts.replace("%id%",id);
		execQuery(response,query,function(err,rows,fields){
			if(err) throw err;
			for(i in rows){
				var row = rows[i];
				credito += fx.convert(row.totale,{from:row.g_valuta,to:valuta});
			}
			var query = queries.getDebtForCharts.replace("%id%",id);
			execQuery(response,query,function(err,rows,fields){
				if(err) throw err;
				for(i in rows){
					var row = rows[i];
					debito += fx.convert(row.totale,{from:row.g_valuta,to:valuta});
				}
				var toret = {credito:credito,debito:debito};
				end(response,200,JSON.stringify(toret));
			});
		});
	}else notLogged(response);
}

//funzione per aggiornare i dati di un utente
function updateProfileData(response,id,key,name,surname,mail,phone,pwd){
	if(isLogged(id,key)){
		var query = queries.updateUtente.replace("%mail%",mail).replace("%name%",name);
		query = query.replace("%surname%",surname).replace("%phone%",phone).replace("%id%",id);
		if(pwd){
			pwd = crypto.createHash('md5').update(pwd).digest("hex"); 
			query = query.replace("%pwd%",pwd);
		}
		else query = query.replace(", u_password = '%pwd%'","");
		execQuery(response,query,function(err,result){
			if(err){
				throw err;
			}
			if(result.affectedRows == 1){
				//tutto a buon fine
				var toret = {response:"ok"};
				end(response,200,JSON.stringify(toret));
			}else{
				//errore
				var toret = {response:"error"};
				end(response,200,JSON.stringify(toret));
			}
		});
	}else notLogged(response);
}

//funzione per cancellare una spesa
function deleteShop(response,id,key,s_id){
	if(isLogged(id,key)){
		var query = queries.checkQuotePagateSpesa.replace("%s_id%",s_id);
		execQuery(response,query,function(err,rows,fields){
			if(err) throw err;
			if(rows.length == 1){
				var query = queries.deleteSpesa.replace("%ID%",s_id);
				execQuery(response,query,function(err,result){
					if(err) throw err;
					var toret = {response:"ok"};
					end(response,200,JSON.stringify(toret));
				});
			}else{
				var toret = {response:"denied"};
				end(response,200,JSON.stringify(toret));
			}
		});
	}else notLogged(response);
}

//funzione per controllare se una spesa è modificabile
function isChangeableShop(response,id,key,s_id){
	if(isLogged(id,key)){
		var query = queries.checkQuotePagateSpesa.replace("%s_id%",s_id);
		execQuery(response,query,function(err,rows,fields){
			if(err) throw err;
			var toret = {};
			toret.response = rows.length == 1?"ok":"denied";
			end(response,200,JSON.stringify(toret));
		});
	}else notLogged(response);
}

//funzione per cambiare una spesa
function changeShop(response,id,key,s_id,desc,value){
	if(isLogged(id,key)){
		//prima controllo di poterlo fare, che non si sa mai
		var query = queries.checkQuotePagateSpesa.replace("%s_id%",s_id);
		execQuery(response,query,function(err,rows,fields){
			if(err) throw err;
			if(rows.length ==1){
				//posso. Eseguo la modifica
				desc = desc.replace("'","\\'");
				var date = new Date().toISOString();
				var query = queries.updateSpesa.replace("%val%",value).replace("%desc%",desc);
				query = query.replace("%data%",date).replace("%pagato%",0).replace("%s_id%",s_id);
				execQuery(response,query,function(err,result){
					if(err) throw err;
					//ora devo aggiornare le quote. Prima scopro quante sono
					var n;
					var query = queries.getQuoteCountBySpesa.replace("%s_id%",s_id);
					execQuery(response,query,function(err,rows,fields){
						if(err) throw err;
						n = rows[0].n;
						var quota = parseFloat(value)/n;
						var query = queries.updateQuotaValore.replace("%s_id%",s_id).replace("%valore%",quota);
						execQuery(response,query,function(err,result){
							if(err) throw err;
							var toret = {response:"ok"};
							end(response,200,JSON.stringify(toret));
						});
					});
				});
			}else{
				var toret = {response:"denied"};
				end(response,200,JSON.stringify(toret));
			}
		});
	}else notLogged(response);
}

//funzione per aggiornare/cancellare una notifica di spesa
function updateNotify(response,id,key,n_id,desc){
	if(isLogged(id,key)){
		var query;
		if(desc){
			//aggiorno
			query = queries.updateNotificaSpesa.replace("%desc%",desc).replace("%n_id%",n_id);
		}else{
			//elimino
			query = queries.deleteNotificaSpesa.replace("%ID%",n_id);
		}
		execQuery(response,query,function(err,result){
			if(err) throw err;
			var toret = {response:"ok"};
			end(response,200,JSON.stringify(toret));	
		});
		
	}else notLogged(response);
}

//funzione per uscire da /cancellare un gruppo
function deleteGroup(response,id,key,g_id){
	if (isLogged(id,key)){
		//prima scopro chi è l'admin del gruppo e quante persone ha
		var query = queries.getGroupsExtraInfo.replace("%g_id%",g_id);
		execQuery(response,query,function(err,rows,fields){
			if(err) throw err;
			var info = rows[0];
			if(info.u_id == id || info.count-1 == 1){
				//sono l'admin o lascierei il gruppo vuoto. voglio cancellare il gruppo
				piallaGruppo(response,id,key,g_id);
			}else{
				//sono utente normale. voglio solo uscire dal gruppo
				var query = queries.checkQuoteNonPagateUtenteGruppo.replace("%g_id%",g_id).replace("%u_id%",id);
				execQuery(response,query,function(err,rows,fields){
					if (err) throw err;
					if(rows.length == 0){
						//non ho debiti, posso uscire.
						var finalquery = queries.deleteUtente.replace("%g_id%",g_id).replace("%u_id%",id);
						execQuery(response,finalquery,function(err,result){
							if(err) throw err;
							var toret = {};
							if(result.affectedRows == 1){
								toret.response = "ok";
							}else{
								toret.response = "WTF?";
							}
							end(response,200,JSON.stringify(toret));
						});
					}else{
						var toret = {response:"denied"};
						end(response,200,JSON.stringify(toret));
					}
				});
			}
		});
	}else notLogged(response);
}

//cancella effettivamente un gruppo
function piallaGruppo(response,id,key,g_id){
	var query = queries.checkQuoteNonPagateGruppo.replace("%g_id%",g_id);
	execQuery(result,query,function(err,rows,fields){
		if(err) throw err;
		if(rows.length == 0){
			//posso eliminare gruppo
			var finalquery = queries.deleteGroup.replace("%g_id%",g_id);
			execQuery(response,finalquery,function(err,result){
				if(err) throw err;
				var toret = {};
				if(result.affectedRows == 1){
					toret.response = "ok";
				}else{
					toret.response = "WTF?";
				}
				end(response,200,JSON.stringify(toret));
			});
		}else{
			//ci sono ancora quote non pagate, non posso eliminare il gruppo
			var toret = {response:"denied"};
			end(response,200,JSON.stringify(toret));
		}
	});
}

//funzione per la risposta del db secca all'app cosi' com'e'
function response_back(response,query){
	var result;
	execQuery(response,query,function(err,rows,fields){
		if (err) {
			throw err;
		}
		response.setHeader('Content-Type', 'text/json');
		result = JSON.stringify(rows);
		//miglioro il risultato
		result = result.replace(/:null,/g,":0,").replace(/:null}/g,":0}");
		result = result.replace(/"g_lastUpdate":0/g,'"g_last_update":""');
		end(response,200,result);
	});
}

//funzione per pagare i propri debiti verso un altro utente
function setAsPaid(response,id,key,u_id,g_id){
	if(isLogged(id,key)){
		//prima controllo se pago i debiti totali o solo di un gruppo
		var query;
		var date = new Date().toISOString();
		if(g_id == undefined){
			query = queries.updateQuotaPagata.replace("%date%",date).replace(/%id1%/g,id).replace(/%id2%/g,u_id);
		}else{
			query = queries.updateQuotaPagataGruppo.replace("%g_id%",g_id).replace("%date%",date).replace(/%id1%/g,id).replace(/%id2%/g,u_id)
		}
		execQuery(response,query,function(err,result){
			if(err) throw err;
			//quote pagate.
			//gestisco notifica push
			var fbm_push = {u_id:id};
			setAsPaid_push(u_id,fbm_push);
			//recupero le spese che hanno tutte le quote pagate ma non sono segnate come tali
			var query = queries.getSpesaToSetPaid.replace(/%id1%/g,id).replace(/%id2%/g,u_id);
			execQuery(response,query,function(err,rows,fields){
				if(err) throw err;
				var finalQuery = "";
				for(i in rows){
					if(i==0)
						finalQuery += queries.updateSpesaPagataStart.replace("%s_id%",rows[i].s_id);
					else 
						finalQuery += queries.updateSpesaPagataMiddle.replace("%s_id%",rows[i].s_id);
				}
				if(finalQuery!= ""){
					//ci sono spese pagate da aggiornare
					finalQuery += queries.updateSpesaPagataEnd;
					execQuery(response,finalQuery,function(err,result){
						if(err) throw err;
						var toret = {response:"ok"};
						end(response,200,JSON.stringify(toret));
					});
				}else{
					//nessuna spesa è totalmente pagata con questo pagamento
					var toret = {response:"ok"};
					end(response,200,JSON.stringify(toret));
				}

			});
		});
	}else notLogged(response);
}

//invia notifica push per pagare un debito, se possibile
function askForPayment(response,id,key,u_id){
	if(isLogged(id,key)){
		if(notification_sent[id]==undefined) notification_sent[id] = {};
		if(notification_sent[id][u_id] == undefined){
			//posso mandare la notifica
			end(response,200,JSON.stringify({response:"sent"}));
			var fbm_push = {u_id:id};
			newPaymentPush(u_id,fbm_push);
			notification_sent[id][u_id] = setTimeout(function(){
				delete notification_sent[id][u_id];
			},day_in_ms);
		}else{
			//notifica già mandata.
			var toret = JSON.stringify({response:"denied"});
			end(response,200,toret);
		}
	}else notLogged(response);
}

function notLogged(response){
	end(response,401,"UNAUTHORIZED");
}

//variabili e funzioni per firebase
var tokens = {};
var token_path = '/home/ubuntu/lam/data/tokens.txt';

//funzione per ripristinare da disco i token in memoria
function restoreTokens(){
	try{
			var data = fs.readFileSync(token_path,'utf-8');
			tokens = JSON.parse(data);
	}catch(e){
		console.log("errore nel ripristino dei token");
		tokens = {};
	}
}

//funzione per salvare su disco i token in memoria
function backupTokens(){
	try{
		var data = JSON.stringify(tokens);
		fs.writeFileSync(token_path,data);
	}catch(e){
		console.log("errore nel salvataggio dei token");
	}
}

//salva il token di firebase in memoria
function setToken(id,token,platform){
	id = parseInt(id);
	var obj = {token:token,platform:platform};
	if(tokens[id]) tokens[id].push(obj);
	else tokens[id] = [obj];
}

//rimuove il token di firebase in memoria (se esiste)
function removeToken(id,token){
	id = parseInt(id);
	if(tokens[id]){
		for(i in tokens[id]){
			var device = tokens[id][i];
			if(device.token == token){
				delete tokens[id][i];
				break;
			}
		}
	}
}

//ottiene nome e id dei gruppi
function getGroupData(g_id,u_id,callback){
	var toret = {};
	var query = queries.getGroupPartecipant.replace("%g_id%",g_id).replace("%u_id%",u_id);
	execQuery(undefined,query,function(err,rows,fields){
		if(err) throw err;
		var name = rows[0].g_nome;
		var ids = [];
		for(i in rows){
			ids.push(rows[i].u_id);
		}
		callback(name,ids);
	});
}

//ottiene token a cui mandare messaggio firebase
function getDests(ids){
	var dests = [];
	for(i in ids){
		var id = ids[i];
		for(j in tokens[id]){
			var token = tokens[id][j];
			dests.push(token);
		}
	}
	return dests;
}

function getUsernameById(id,callback){
	var query = queries.getUsernameById.replace("%id%",id);
	execQuery(undefined,query,function(err,rows,fields){
		if(err) throw err;
		var u_name = rows[0].u_nick;
		callback(u_name);
	});
}

//genera una notifica per una richiesta di pagamento
function newPaymentPush(id,data){
	getUsernameById(data.u_id,function(name,ids){
		var push_ids = getDests([id]);
		data.u_name = name;
		pushNotify(push_ids,"paymentRequest",data);
	});
}

//genera notifica del nuovo gruppo
function newGroup_push(ids,data){
	var push_ids = getDests(ids);
	pushNotify(push_ids,"newGroup",data);
}

//genera notifica della nuova spesa
function newShop_push(id,data){
	getGroupData(data.g_id,id,function(name,ids){
		var push_ids = getDests(ids);
		data.g_name = name;
		pushNotify(push_ids,"newShop",data);
	});
}

//genera notifica push per la notifica spesa da fare
function newNotifyShop_push(id,data){
	getGroupData(data.g_id,id,function(name,ids){
		var push_ids = getDests(ids);
		data.g_name = name;
		pushNotify(push_ids,"newNotify",data);
	});
}

//genera notifica push per un debito saldato
function setAsPaid_push(id,data){
	getUsernameById(id,function(name){
		data.u_name = name;
		var dests = getDests([id]);
		pushNotify(dests,"paid",data);
	});
}

function pushNotify(dests,type,message){
	if(dests.length > 0){
		var ios_dests = [];
		var android_dests = [];
		for (i in dests){
			var dest = dests[i];
			if(dest!=null){
				if(dest.platform == "IOS"){
					ios_dests.push(dest.token)
				}else android_dests.push(dest.token);
			}
		}
		//mi assicuro che i token non siano presenti più volte per evitare notifiche doppie
		ios_dests = ios_dests.filter(function(elem, pos) {
    			return ios_dests.indexOf(elem) == pos;
		});
		android_dests = android_dests.filter(function(elem, pos) {
                        return android_dests.indexOf(elem) == pos;
                });
		console.log("invio notifiche a \n"+ios_dests+"\ne a \n"+android_dests);
		//preparo le notifiche in base alla piattaforma
		var body = iosMerda(type,message);
		var ios_json = {
			registration_ids:ios_dests,
			notification:{
				title:body.title,
				badge:1,
				icon:"ic_launcher",
				sound:"default",
				collapse_key:"do_not_collapse",
				body:body.body
			}
		};
		var android_json = {
			registration_ids:android_dests,
			data:{
				type:type,
				message:message
			}
		};

		var ios_body = JSON.stringify(ios_json);
		var android_body = JSON.stringify(android_json);
		if(ios_dests.length > 0)request({
			url:'https://fcm.googleapis.com/fcm/send',
			method:"POST",
			headers: { //We can define headers too
	        'Content-Type': 'application/json',
	        'Authorization': 'key='+firebase_key,
	    	},
	    	body:ios_body
		}, function(err,response,body){
			if(err) throw err;
		});
		if(android_dests.length > 0) request({
			url:'https://fcm.googleapis.com/fcm/send',
			method:"POST",
			headers: { //We can define headers too
	        'Content-Type': 'application/json',
	        'Authorization': 'key='+firebase_key,
	    	},
	    	body:android_body
		}, function(err,response,body){
			if(err) throw err;
		});
	}
}

function iosMerda(type,message){
	switch(type){
		case "newGroup":
			return {body:"Created new group: "+message.name,title:"Walli"};
		case "newShop":
			return {body:"Added new expense: "+message.desc,title:message.g_name};
		case "newNotify":
			return {body:"Added new expense to do: "+message.desc,title:message.g_name};
		case "paid":
			return {body:"Your debt was paid",title:message.u_name};
		case "paymentRequest":
			return {body:"Remember to pay your debt",title:message.u_name};
		default:
		return "Base notification";
	}
}

//cio' che e' raggiungibile del modulo dall'esterno
module.exports = {
	'setConnection':setConnection,
	'closeConnection':closeConnection,
	'login':login,
	'logout':logout,
	'isLogged':isLogged,
	'restorePassword':restorePassword,
	'createNewAccount':createNewAccount,
	'confirmSubscription':confirmSubscription,
	'getGroups':getGroups,
	'getUsersByGroup':getUsersByGroup,
	'getAllMyUsers':getAllMyUsers,
	'getSpeseByGroup':getSpeseByGroup,
	'getSpeseForCharts':getSpeseForCharts,
	'insertSpesa':insertSpesa,
	'getCreditDebitByGroup':getCreditDebitByGroup,
	'getCreditDebit':getCreditDebit,
	'getNotificheByGroup':getNotificheByGroup,
	'insertNotifica':insertNotifica,
	'updateProfileData':updateProfileData,
	'getHints':getHints,
	'updateGroup':updateGroup,
	'deleteShop':deleteShop,
	'deleteGroup':deleteGroup,
	'isChangeableShop':isChangeableShop,
	'changeShop':changeShop,
	'updateNotify':updateNotify,
	'setAsPaid':setAsPaid,
	'getCreditDebtForCharts':getCreditDebtForCharts,
	'end':end,
	'askForPayment':askForPayment,
	'backupLogin':backupLogin,
	'restoreLogin':restoreLogin,
	'restoreTokens':restoreTokens,
	'backupTokens':backupTokens
};

