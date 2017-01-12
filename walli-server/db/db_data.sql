INSERT INTO Utente (u_nick,u_mail,u_nome,u_cognome,u_cell,u_password) VALUES
('lucci','daniele.piergigli@gmail.com','Daniele','Piergigli','+393401770381','4b888228a94ce27f09f2724825f0935e'),
('dado','ildado94@gmail.com','Davide','Quadrelli','+393935272253','f4e3fd967ae7a301640f009c43f80ce7'),
('pino','pino@pinguino.it','Pino','dei Pini','+393330333010','5cd5b9e4b18a9c0f1e3a5f01acbc6aff')
;

INSERT INTO Gruppo (g_nome,g_valuta,u_id) VALUES 
('Surf Bahamas',"EUR",1),
('DiscoSTU',"USD",3)
;

INSERT INTO Partecipazione (u_id,g_id) VALUES
(1,1),
(2,1),
(3,2),
(1,2),
(2,2)
;

INSERT INTO Spesa (s_valore,s_desc,s_data,s_pagato,g_id,u_id) VALUES
(12.50,"Sambuca",'2015-12-31 01:02:03',0,1,1),
(13,"Droga","2015-12-30 00:05:00",0,1,2),
(20,"Preservativi","2015-12-30 00:05:00",0,2,3),
(16,"Dildo Gigante","2015-12-30 00:05:00",1,1,2),
(80.98,"Flashlight","2015-12-30 00:05:00",0,2,1)
;

INSERT INTO NotificaSpesa (n_desc,u_id,g_id) VALUES
('Bisogna andare a troie',2,1)
;

INSERT INTO Quota (q_valore,q_pagato,u_id,s_id, q_data) VALUES
(6.25,1,1,1,'2016-07-05 19:44:22'),
(6.25,0,2,1,null),
(6.50,0,1,2,null),
(6.50,1,2,2,'2016-07-05 19:44:22'),
(6.66,1,3,3,'2016-03-06 19:44:22'),
(6.66,0,1,3,null),
(6.66,0,2,3,null),
(8,1,2,4,'2016-07-01 19:44:22'),
(8,1,1,4,'2016-06-03 19:44:22'),
(26.99,1,1,5,'2015-09-21 19:44:22'),
(26.99,0,2,5,null),
(26.99,0,3,5,null)
;

