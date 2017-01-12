var login_query = '\
SELECT u_id, u_nick, u_mail, u_nome, u_cognome, u_cell \
FROM Utente \
WHERE u_nick LIKE "%USER%" AND u_password LIKE "%PWD%";';

var getGroupPartecipant_query = "\
SELECT G.g_nome, P.u_id \
FROM Gruppo G JOIN Partecipazione P ON G.g_id = P.g_id \
WHERE G.g_id = %g_id% AND P.u_id <> %u_id%;";

var getUsernameById_query = "\
SELECT u_nick \
FROM Utente \
WHERE u_id = %id%;";

var checkMail_query = "\
SELECT u_id \
FROM Utente \
WHERE u_mail LIKE '%mail%';";

var updatePwd_query = "\
UPDATE Utente \
SET u_password = '%pwd%' \
WHERE u_id = %id%;";

var getGroups_query = '\
SELECT DISTINCT G.g_id, G.u_id, G.g_nome, G.g_valuta, \
(SELECT SUM(totale) \
FROM Credito \
WHERE creditore = %ID% AND gruppo = G.g_id) as credito, \
(SELECT SUM(totale) \
FROM Credito \
WHERE debitore = %ID% AND gruppo = G.g_id) as debito, \
(SELECT s_data \
FROM Spesa \
WHERE g_id = G.g_id \
ORDER BY s_data DESC \
LIMIT 1 )as g_lastUpdate, \
(SELECT COUNT(n_id) \
FROM NotificaSpesa \
WHERE g_id = G.g_id) as notifiche \
FROM Gruppo G JOIN Partecipazione P ON G.g_id = P.g_id WHERE P.u_id = %ID% \
ORDER BY g_lastUpdate DESC;';

var getGroupsExtraInfo_query = '\
SELECT G.u_id as u_id, COUNT(P.u_id) as count \
FROM Gruppo G JOIN Partecipazione P ON G.g_id = P.g_id \
WHERE G.g_id = %g_id%;';

var getUsersByGroup_query = '\
SELECT U.u_id, U.u_nick, U.u_mail, U.u_nome, U.u_cognome, U.u_cell \
FROM Utente U JOIN Partecipazione P ON P.u_id = U.u_id \
WHERE g_id = %ID% \
ORDER BY U.u_nick;';

var getUsersByHint_query = "\
SELECT U.u_id, U.u_nick, U.u_nome, U.u_cognome \
FROM Utente U  \
WHERE U.u_nick LIKE '%*hint*%' \
ORDER BY U.u_nick \
LIMIT 15;";

var getAllKnownUsers_query = "\
SELECT DISTINCT U.u_id, U.u_nick, U.u_mail, U.u_nome, U.u_cognome, U.u_cell \
FROM Utente U JOIN Partecipazione P ON P.u_id = U.u_id JOIN( \
	SELECT Pa.g_id  \
	FROM Partecipazione Pa \
	WHERE Pa.u_id = %ID% ) as T ON P.g_id = T.g_id \
ORDER BY U.u_nick;";

var checkExistentUser_query = '\
SELECT u_id \
FROM Utente \
WHERE u_nick LIKE "%NICK%" OR u_mail LIKE "%MAIL%";';

var getSpeseByGroup_query = '\
SELECT S.s_id, S.s_valore, S.s_desc, S.s_data, S.s_pagato, S.u_id, U.u_nick \
FROM Spesa S JOIN Gruppo G ON S.g_id = G.g_id JOIN Utente U ON U.u_id = S.u_id \
WHERE S.g_id = %ID% \
ORDER BY S.s_data DESC;';

var getCreditDebitByGroup_query = '\
SELECT DISTINCT U.u_id, U.u_nick, U.u_mail, U.u_nome, U.u_cognome, U.u_cell, \
(SELECT totale \
	FROM Credito \
	WHERE creditore = %ID% AND gruppo = %G_ID% AND debitore = U.u_id)as credito, \
(SELECT totale \
	FROM Credito \
	WHERE debitore = %ID% AND gruppo = %G_ID% AND creditore = U.u_id) as debito \
FROM Utente U JOIN Partecipazione P ON U.u_id = P.u_id \
WHERE P.g_id = %G_ID% AND U.u_id <> %ID% \
ORDER BY U.u_nick;';

var getCreditDebit_query = '\
SELECT DISTINCT U.u_id, U.u_nick, U.u_mail, U.u_nome, U.u_cognome, U.u_cell,G.g_valuta, \
(SELECT totale \
	FROM Credito \
	WHERE creditore = %u_id% AND gruppo = G.g_id AND debitore = U.u_id)as credito, \
(SELECT totale \
	FROM Credito \
	WHERE debitore = %u_id% AND gruppo = G.g_id AND creditore = U.u_id) as debito \
FROM Utente U JOIN Partecipazione P ON U.u_id = P.u_id JOIN Gruppo G ON G.g_id = P.g_id \
WHERE U.u_id <> %u_id% \
ORDER BY U.u_nick;';

var getSpeseForCharts_query = "\
SELECT data_entrata AS data, valore, g_valuta AS valuta, 'entrata' AS tipo \
FROM Entrata \
WHERE u_id = %id% AND data_entrata BETWEEN '%d1%' AND '%d2%' \
UNION \
SELECT data_uscita AS data, valore, g_valuta AS valuta, 'uscita' AS tipo \
FROM Uscita \
WHERE u_id = %id% AND data_uscita BETWEEN '%d1%' AND '%d2%' \
ORDER BY data;";

var getCreditForCharts_query = "\
SELECT SUM(totale) as totale, g_valuta \
from Credito JOIN Gruppo ON gruppo = g_id \
WHERE creditore = %id% \
GROUP BY gruppo,creditore;";

var getDebtForCharts_query = "\
SELECT SUM(totale) as totale, g_valuta \
from Credito JOIN Gruppo ON gruppo = g_id \
WHERE debitore = %id% \
GROUP BY gruppo,debitore;";

var getNotificheByGroup_query = '\
SELECT N.n_id, N.n_desc, N.u_id, U.u_nick \
FROM NotificaSpesa N JOIN Utente U ON N.u_id = U.u_id \
WHERE g_id = %G_ID% \
ORDER BY N.n_id DESC;';

var insertSpesa_query = "\
INSERT INTO Spesa (s_valore, s_desc, s_data, s_pagato, g_id, u_id) \
VALUES (%valore%, '%descrizione%', '%data%', %pagato%, %idGruppo%, %idUtente%);";

var getGroupDim_query = '\
SELECT u_id, number_user AS n \
FROM Partecipazione P JOIN DimensioneGruppo G ON P.g_id = G.g_id \
WHERE G.g_id = %g_id%;';

var insertQuotaStart_query = "\
INSERT INTO Quota (q_valore, q_pagato, q_data, u_id, s_id) \
VALUES ";

var insertQuotaMiddle_query = "(%valore%, %pagato%, '%data%', %idUtente%, %idSpesa%),";

var insertQuotaEnd_query = ";";


var insertGroup_query = "\
INSERT INTO Gruppo (g_nome, g_valuta, u_id) \
VALUES ('%nome%', '%valuta%', %u_id%);";

var insertPartecipazioneStart_query = "\
INSERT INTO Partecipazione (u_id, g_id) \
VALUES ";

var insertPartecipazioneMiddle_query = "(%u_id%, %g_id%),";

var insertPartecipazioneEnd_query = ";";

var insertUser_query = "\
INSERT INTO Utente (u_nick, u_mail, u_nome, u_cognome, u_cell, u_password) \
VALUES ('%NICK%', '%MAIL%', '%NOME%', '%COGNOME%', '%CELL%', '%PWD%');";

var insertNotifica_query = "\
INSERT INTO NotificaSpesa (n_desc, u_id, g_id) \
VALUES ('%desc%', %id%, %g_id%);";

var deleteGroup_query = "\
DELETE FROM Gruppo \
WHERE g_id = %g_id%;";

var checkQuoteNonPagateGruppo_query = "\
SELECT q_id \
FROM Quota Q JOIN Spesa S ON S.s_id = Q.s_id \
WHERE Q.q_pagato = 0 AND S.g_id = %g_id%;";

var checkQuoteNonPagateUtenteGruppo_query = '\
SELECT q_id \
FROM Quota Q JOIN Spesa S ON S.s_id = Q.s_id \
WHERE Q.q_pagato = 0 AND S.g_id = %g_id% AND Q.u_id = %u_id%;';

var deleteUtente_query = "\
DELETE FROM Partecipazione \
WHERE g_id = %g_id% AND u_id =%u_id% ;";

var deleteUtenteGruppo_query = '\
DELETE FROM Partecipazione \
WHERE g_id = %g_id%;';

var checkQuoteNonPagateUtente_query = "\
SELECT q_id \
FROM Quota Q JOIN Spesa S ON S.s_id = Q.s_id \
WHERE Q.q_pagato = 0 AND S.u_id = %idUtente%;";

var deleteNotificaSpesa_query = "\
DELETE FROM NotificaSpesa \
WHERE n_id = %ID%;";

var deleteSpesa_query = "\
DELETE FROM Spesa \
WHERE s_id = %ID%;";

var checkQuotePagateSpesa_query = "\
SELECT q_id \
FROM Quota \
WHERE q_pagato = 1 AND s_id = %s_id%;";

var checkQuoteNonPagateSpesa_query = "\
SELECT q_id \
FROM Quota \
WHERE q_pagato = 0 AND s_id = %idSpesa%;";

var getQuoteCountBySpesa_query = '\
SELECT COUNT(q_id) as n \
FROM Quota \
WHERE s_id = %s_id%;';

var updateSpesa_query = "\
UPDATE Spesa \
SET s_valore = %val%, s_desc = '%desc%', s_data = '%data%', s_pagato = %pagato% \
WHERE s_id = %s_id%;";

var updateGruppo_query = "\
UPDATE Gruppo \
SET g_nome = '%g_nome%', g_valuta = '%g_valuta%' \
WHERE g_id = %g_id%;";

var updateQuotaPagataGruppo_query = "\
UPDATE Quota Q JOIN Spesa S ON Q.s_id = S.s_id \
SET Q.q_pagato = 1, Q.q_data = '%date%' \
WHERE S.g_id = %g_id% AND (S.u_id = %id1% OR S.u_id = %id2%) AND (Q.u_id = %id1% OR Q.u_id = %id2%);";

var updateQuotaPagata_query = "\
UPDATE Quota Q JOIN Spesa S ON Q.s_id = S.s_id \
SET q_pagato = 1, q_data = '%date%' \
WHERE (Q.u_id = %id1% OR Q.u_id = %id2%) AND(S.u_id = %id1% OR S.u_id = %id2%) AND q_pagato = 0;";

var getSpesaToSetPaid_query = "\
SELECT s_id \
FROM Spesa \
WHERE (u_id = %id1% OR u_id = %id2%) AND s_pagato = 0 AND s_id NOT IN \
(SELECT DISTINCT S.s_id \
FROM Spesa S JOIN Quota Q ON Q.s_id = S.s_id \
WHERE S.s_pagato = 0 AND (S.u_id = %id1% OR S.u_id = %id2%) AND Q.q_pagato = 0);";

var updateQuotaValore_query = "\
UPDATE Quota \
SET q_valore = %valore% \
WHERE s_id = %s_id%;";

var updateUtente_query = "\
UPDATE Utente \
SET u_mail = '%mail%', u_nome = '%name%', u_cognome = '%surname%', u_cell = '%phone%', u_password = '%pwd%' \
WHERE u_id = %id%;";

var updatePwdDimenticata_query = "\
UPDATE Utente \
SET u_password = %password% \
WHERE u_id = %idUtente%;";

var updateNotificaSpesa_query = "\
UPDATE NotificaSpesa \
SET n_desc = '%desc%' \
WHERE n_id = %n_id%;";

// var updateQuotaPagata_query = "\
// UPDATE Quota \
// SET s_pagato = %pagato%, q_data = '%data%' \
// WHERE s_id = %idSpesa% AND u_id = %idUtente%;";

var updateSpesaPagataStart_query = "\
UPDATE Spesa \
SET s_pagato = 1 \
WHERE s_id = %s_id% ";

var updateSpesaPagataMiddle_query = "\
OR s_id = %s_id% ";

var updateSpesaPagataEnd_query = ";";

module.exports = {
	login:login_query,
	getGroupPartecipant:getGroupPartecipant_query,
	getUsernameById:getUsernameById_query,
	checkMail:checkMail_query,
	updatePwd:updatePwd_query,
	getGroups:getGroups_query,
	getGroupsExtraInfo:getGroupsExtraInfo_query,
	getUsersByGroup:getUsersByGroup_query,
	getUsersByHint:getUsersByHint_query,
	getAllKnownUsers:getAllKnownUsers_query,
	checkExistentUser:checkExistentUser_query,
	getSpeseByGroup:getSpeseByGroup_query,
	getCreditDebitByGroup:getCreditDebitByGroup_query,
	getCreditDebit:getCreditDebit_query,
	getSpeseForCharts:getSpeseForCharts_query,
	getCreditForCharts:getCreditForCharts_query,
	getDebtForCharts:getDebtForCharts_query,
	getNotificheByGroup:getNotificheByGroup_query,
	insertSpesa:insertSpesa_query,
	getGroupDim:getGroupDim_query,
	insertQuotaStart:insertQuotaStart_query,
	insertQuotaMiddle:insertQuotaMiddle_query,
	insertQuotaEnd:insertQuotaEnd_query,
	insertGroup:insertGroup_query,
	insertPartecipazioneStart:insertPartecipazioneStart_query,
	insertPartecipazioneMiddle:insertPartecipazioneMiddle_query,
	insertPartecipazioneEnd:insertPartecipazioneEnd_query,
	insertUser:insertUser_query,
	insertNotifica:insertNotifica_query,
	deleteGroup:deleteGroup_query,
	checkQuoteNonPagateGruppo:checkQuoteNonPagateGruppo_query,
	checkQuoteNonPagateUtenteGruppo:checkQuoteNonPagateUtenteGruppo_query,
	deleteUtente:deleteUtente_query,
	deleteUtenteGruppo:deleteUtenteGruppo_query,
	checkQuoteNonPagateUtente:checkQuoteNonPagateUtente_query,
	deleteNotificaSpesa:deleteNotificaSpesa_query,
	deleteSpesa:deleteSpesa_query,
	checkQuotePagateSpesa:checkQuotePagateSpesa_query,
	checkQuoteNonPagateSpesa:checkQuoteNonPagateSpesa_query,
	getQuoteCountBySpesa:getQuoteCountBySpesa_query,
	updateSpesa:updateSpesa_query,
	updateGruppo:updateGruppo_query,
	updateQuotaPagataGruppo:updateQuotaPagataGruppo_query,
	updateQuotaPagata:updateQuotaPagata_query,
	getSpesaToSetPaid:getSpesaToSetPaid_query,
	updateQuotaValore:updateQuotaValore_query,
	updateUtente:updateUtente_query,
	updatePwdDimenticata:updatePwdDimenticata_query,
	updateNotificaSpesa:updateNotificaSpesa_query,
	updateSpesaPagataStart:updateSpesaPagataStart_query,
	updateSpesaPagataMiddle:updateSpesaPagataMiddle_query,
	updateSpesaPagataEnd:updateSpesaPagataEnd_query
};
