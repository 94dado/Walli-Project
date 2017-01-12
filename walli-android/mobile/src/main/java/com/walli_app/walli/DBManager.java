package com.walli_app.walli;

import android.graphics.Bitmap;
import android.text.format.DateUtils;
import android.util.Base64;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.ByteArrayOutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;

/**
 * Created by dado on 15/06/16.
 */
//classe per la gestione di tutte le query da fare al db
public class DBManager {
    private ConnectionManager connection;
    private final String location = "https://walli.ddns.net/";
    private String key = null;
    private int user_id = -1;

    public DBManager() {
        connection = new ConnectionManager();
    }

    public DBManager(String key, int user_id) {
        this();
        this.key = key;
        this.user_id = user_id;
    }

    public int getUserId() {
        return user_id;
    }

    public String toString() {
        return key;
    }

    //funzione per controllare se l'immagine che ho in cache è aggiornata o meno
    private Map<String, String> getImageHeaders(int type, int img_id) {
        Map<String, String> params = new HashMap<>();
        params.put("id", Integer.toString(user_id));
        params.put("key", key);
        String tipo;
        switch (type) {
            case ImageManager.GROUP:
                tipo = "group";
                break;
            case ImageManager.USER:
                tipo = "user";
                break;
            default:
                tipo = null;
        }
        params.put("type", tipo);
        params.put("img_id", Integer.toString(img_id));
        return params;
    }

    //converte un bitmap in una stringa
    private String bitmapToString(Bitmap img) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        img.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] b = baos.toByteArray();
        return Base64.encodeToString(b, Base64.DEFAULT);
    }

    //funzione per l'md5 della password
    private String md5(final String s) {
        final String MD5 = "MD5";
        try {
            // Create MD5 Hash
            MessageDigest digest = java.security.MessageDigest
                    .getInstance(MD5);
            digest.update(s.getBytes());
            byte messageDigest[] = digest.digest();

            // Create Hex String
            StringBuilder hexString = new StringBuilder();
            for (byte aMessageDigest : messageDigest) {
                String h = Integer.toHexString(0xFF & aMessageDigest);
                while (h.length() < 2)
                    h = "0" + h;
                hexString.append(h);
            }
            return hexString.toString();

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "";
    }

    //funzione per eseguire il login
    public User login(String username, String password,String token) {
        try {
            //prima di tutto, calcolo md5 della password
            password = md5(password);
            //genero parametri e url
            String url = location + "login";
            Map<String, String> params = new HashMap<>();
            params.put("user", username);
            params.put("pwd", password);
            //inserisco token di firebase se ce l'ho
            if(token != null){
                params.put("token",token);
            }
            //eseguo la richiesta
            String answer = connection.POST(url, params);
            //gestisco la risposta
            JSONArray response = new JSONArray(answer);
            JSONObject obj = response.getJSONObject(0);
            int id = obj.getInt("u_id");
            String nick = obj.getString("u_nick");
            String mail = obj.getString("u_mail");
            String name = obj.getString("u_nome");
            String surname = obj.getString("u_cognome");
            String phone = obj.getString("u_cell");
            User entry = new User(id, nick, mail, name, surname, phone);
            user_id = id;
            key = obj.getString("key");
            return entry;
        } catch (Exception e) {
            System.err.println(e.toString());
            return null;
        }
    }

    //funzione per eseguire il logout
    public void logout(int id,String token) {
        //genero parametri e url
        String url = location + "logout";
        Map<String, String> params = new HashMap<>();
        params.put("id", Integer.toString(id));
        params.put("key", key);
        if(token != null) params.put("token",token);
        //eseguo la richiesta
        connection.POST(url, params);
        //ignoro la risposta, non mi interessa clientside
    }

    //funzione per registrare un nuovo account. Restituisco statusCode
    public int signUp(String username, String name, String surname, String mail, String cell, String pwd) {
        try {
            //prima di tutto, calcolo md5 della password
            pwd = md5(pwd);
            String url = location + "signUp";
            Map<String, String> params = new HashMap<>();
            params.put("nick", username);
            params.put("mail", mail);
            params.put("name", name);
            params.put("surname", surname);
            params.put("cell", cell);
            params.put("pwd", pwd);
            //eseguo la richiesta
            String answer = connection.POST(url, params);
            JSONObject obj = new JSONObject(answer);
            //gestisco la risposta
            if (obj.getString("response").equals("ok")) return 200;
            else return Integer.parseInt(answer);
        } catch (Exception e) {
            System.err.println(e.toString());
            return 500;
        }
    }

    //aggiorno i dati del profilo di un utente
    public boolean updateProfileData(User user, String[] values, boolean checkPwd) {
        try {
            String url = location + "updateProfileData";
            Map<String, String> params = new HashMap<>();
            params.put("id", Integer.toString(user_id));
            params.put("key", key);
            params.put("name", values[0]);
            params.put("surname", values[1]);
            params.put("phone", values[2]);
            params.put("mail", values[3]);
            if (checkPwd) {
                String pwd = values[4];
                pwd = md5(pwd);
                params.put("pwd", pwd);
            }
            //eseguo la richiesta
            String answer = connection.POST(url, params);
            JSONObject obj = new JSONObject(answer);
            //gestisco la risposta
            if (obj.getString("response").equals("ok")) {
                user.setName(values[0]);
                user.setSurname(values[1]);
                user.setPhone(values[2]);
                user.setMail(values[3]);
                return true;
            } else return false;
        } catch (Exception e) {
            System.err.println(e.toString());
            return false;
        }
    }

    //funzione per ottenere l'elenco dei gruppi dal server
    public ArrayList<Group> getGruppi() {
        ArrayList<Group> gruppi = new ArrayList<>();
        try {
            //genero parametri e url
            String url = location + "getGroupsByUser";
            Map<String, String> params = new HashMap<>();
            params.put("id", Integer.toString(user_id));
            params.put("key", key);
            //eseguo la richiesta
            String answer = connection.POST(url, params);
            //gestisco la risposta
            JSONArray response = new JSONArray(answer);
            for (int i = 0; i < response.length(); i++) {
                //per ogni gruppo che ho ricevuto, genero l'oggetto della classe corrispondente
                JSONObject obj = response.getJSONObject(i);
                int id = obj.getInt("g_id");
                String nome = obj.getString("g_nome");
                String valuta = obj.getString("g_valuta");
                double credito = obj.getDouble("credito");
                double debito = obj.getDouble("debito");
                int user_id = obj.getInt("u_id");
                int notifiche = obj.getInt("notifiche");
                String lastUpdate;
                try {
                    lastUpdate = obj.getString("g_lastUpdate");
                } catch (Exception e) {
                    lastUpdate = "";
                }
                credito = credito - debito;
                String cash = String.format(Locale.getDefault(), "%.02f", credito);
                Group g = new Group(id, nome, cash, valuta, lastUpdate, user_id, notifiche);
                gruppi.add(g);
            }
        } catch (Exception e) {
            System.err.println(e.toString());
            return null;
        }

        return gruppi;
    }

    //funzione per ottenere l'elenco delle spese di un gruppo dal server
    public ArrayList<Shop> getSpese(int group_id) {
        ArrayList<Shop> spese = new ArrayList<>();
        try {
            String url = location + "getSpeseByGroup";
            Map<String, String> params = new HashMap<>();
            params.put("g_id", Integer.toString(group_id));
            params.put("id", Integer.toString(user_id));
            params.put("key", key);
            //eseguo la richiesta
            String answer = connection.POST(url, params);
            //gestisco la risposta
            JSONArray response = new JSONArray(answer);
            for (int i = 0; i < response.length(); i++) {
                JSONObject obj = response.getJSONObject(i);
                int id = obj.getInt("s_id");
                double valore = obj.getDouble("s_valore");
                String val = String.format(Locale.getDefault(), "%.02f", valore);
                String desc = obj.getString("s_desc");
                String user_name = obj.getString("u_nick");
                String date = obj.getString("s_data");
                boolean pagato = obj.getInt("s_pagato") == 1;
                int user_id = obj.getInt("u_id");
                Shop entry = new Shop(id, val, desc, date, user_name, pagato, user_id);
                spese.add(entry);
            }
        } catch (Exception e) {
            System.err.println(e.toString());
            return null;
        }
        return spese;
    }

    //funzione per ottenere crediti e debiti verso altri utenti
    public ArrayList<CreditDebit> getCreditDebit(int group_id, String valuta) {
        ArrayList<CreditDebit> credit_debit = new ArrayList<>();
        boolean group = group_id != -1;
        try {
            String url = location;
            //se ho il gruppo, chiedo i crediti/debiti dei gruppi di un solo gruppo
            if (group) url += "getCreditDebitByGroup";
            //altrimenti rispetto a tutti gli utenti
            else url += "getCreditDebit";
            Map<String, String> params = new HashMap<>();
            params.put("id", Integer.toString(user_id));
            params.put("key", key);
            //invio id del gruppo, se interessato
            if (group) params.put("g_id", Integer.toString(group_id));
            //altrimenti la valuta per la conversione di tutti i valori in essa
            else params.put("valuta", valuta);
            //eseguo la richiesta
            String answer = connection.POST(url, params);
            //gestisco la risposta
            JSONArray response = new JSONArray(answer);
            for (int i = 0; i < response.length(); i++) {
                JSONObject obj = response.getJSONObject(i);
                int u_id = obj.getInt("u_id");
                String nick = obj.getString("u_nick");
                String mail = obj.getString("u_mail");
                String name = obj.getString("u_nome");
                String surname = obj.getString("u_cognome");
                String cell = obj.getString("u_cell");
                double credito = obj.getDouble("credito");
                double debito = obj.getDouble("debito");
                User u = new User(u_id, nick, mail, name, surname, cell);
                String val = String.format(Locale.getDefault(), "%.02f", credito - debito);
                CreditDebit credit = new CreditDebit(val, u);
                credit_debit.add(credit);
            }
        } catch (Exception e) {
            System.err.println(e.toString());
            return null;
        }
        return credit_debit;
    }

    //funzione per ottenere tutte le notifiche di spesa di un gruppo
    public ArrayList<ShopNotify> getNotify(int group_id) {
        ArrayList<ShopNotify> notifiche = new ArrayList<>();
        try {
            String url = location + "getNotificheByGroup";
            Map<String, String> params = new HashMap<>();
            params.put("g_id", Integer.toString(group_id));
            params.put("id", Integer.toString(user_id));
            params.put("key", key);
            //eseguo la richiesta
            String answer = connection.POST(url, params);
            //gestisco la risposta
            JSONArray response = new JSONArray(answer);
            for (int i = 0; i < response.length(); i++) {
                JSONObject obj = response.getJSONObject(i);
                String description = obj.getString("n_desc");
                int id = obj.getInt("n_id");
                int u_id = obj.getInt("u_id");
                String nick = obj.getString("u_nick");
                ShopNotify notifica = new ShopNotify(id, description, u_id, nick, group_id);
                notifiche.add(notifica);
            }
        } catch (Exception e) {
            System.err.println(e.toString());
            return null;
        }
        return notifiche;
    }

    //funzione per ottenere l'elenco degli utenti di un gruppo
    public ArrayList<User> getUtenti(int group_id) {
        ArrayList<User> utenti = new ArrayList<>();
        try {
            String url = location + "getUsersByGroup";
            Map<String, String> params = new HashMap<>();
            params.put("g_id", Integer.toString(group_id));
            params.put("id", Integer.toString(user_id));
            params.put("key", key);
            //eseguo la richiesta
            String answer = connection.POST(url, params);
            //gestisco la risposta
            JSONArray response = new JSONArray(answer);
            for (int i = 0; i < response.length(); i++) {
                JSONObject obj = response.getJSONObject(i);
                int id = obj.getInt("u_id");
                String nick = obj.getString("u_nick");
                String mail = obj.getString("u_mail");
                String name = obj.getString("u_nome");
                String surname = obj.getString("u_cognome");
                String phone = obj.getString("u_cell");
                User entry = new User(id, nick, mail, name, surname, phone);
                utenti.add(entry);
            }
        } catch (Exception e) {
            System.err.println(e.toString());
            return null;
        }
        return utenti;
    }

    //funzione per inserire una spesa in un gruppo
    public boolean insertSpesa(String desc, String value, int group_id, int n_id) {
        try {
            //genero parametri
            String url = location + "insertSpesa";
            Map<String, String> params = new HashMap<>();
            params.put("g_id", Integer.toString(group_id));
            params.put("id", Integer.toString(user_id));
            params.put("key", key);
            params.put("description", desc);
            params.put("value", value);
            if (n_id != -1) { //la spesa è stata generata da una notifica spesa
                params.put("n_id", Integer.toString(n_id));
            }
            //eseguo richiesta
            String answer = connection.POST(url, params);
            //gestisco risposta
            JSONObject obj = new JSONObject(answer);
            return obj.getInt("s_id")!=-1;
        } catch (Exception e) {
            System.err.println(e.toString());
            return false;
        }
    }

    //funzione per inserire una notifica di spesa in un gruppo
    public boolean insertNotify(String desc, int group_id) {
        try {
            //genero parametri
            String url = location + "insertNotifica";
            Map<String, String> params = new HashMap<>();
            params.put("g_id", Integer.toString(group_id));
            params.put("id", Integer.toString(user_id));
            params.put("key", key);
            params.put("description", desc);
            //eseguo la richiesta
            String answer = connection.POST(url, params);
            //gestisco la risposta
            JSONObject obj = new JSONObject(answer);
            return obj.getInt("n_id") != -1;
        } catch (Exception e) {
            System.err.println(e.toString());
            return false;
        }
    }

    //funzione per ottenere gli hint per gli username disponibili da inserire nel gruppo
    public ArrayList<User> getHints(String text) {
        try {
            ArrayList<User> hints = new ArrayList<>();
            String url = location + "getHints";
            Map<String, String> params = new HashMap<>();
            params.put("id", Integer.toString(user_id));
            params.put("key", key);
            params.put("text", text);
            //eseguo la richiesta
            String answer = connection.POST(url, params);
            //gestisco la risposta
            JSONArray response = new JSONArray(answer);
            for (int i = 0; i < response.length(); i++) {
                JSONObject obj = response.getJSONObject(i);
                int id = obj.getInt("u_id");
                String nick = obj.getString("u_nick");
                String mail = "";
                String name = obj.getString("u_nome");
                String surname = obj.getString("u_cognome");
                String phone = "";
                User entry = new User(id, nick, mail, name, surname, phone);
                hints.add(entry);
            }
            return hints;
        } catch (Exception e) {
            System.err.println(e.toString());
            return null;
        }
    }

    //funzione per aggiornare i dati di un gruppo
    public int updateGroup(int[] user_ids, int g_id, String g_name, String valuta) {
        try {
            String url = location + "updateGroup";
            Map<String, String> params = new HashMap<>();
            params.put("id", Integer.toString(user_id));
            params.put("key", key);
            JSONArray ids = new JSONArray();
            //se devo aggiornare la lista di utenti, la mando
            if (user_ids != null && user_ids.length > 0) {
                for (int id : user_ids) {
                    ids.put(id);
                }
                params.put("user_ids", ids.toString());
            }
            //se sto modificando un gruppo e non creando, invio il suo id
            if (g_id != -1) {
                params.put("g_id", Integer.toString(g_id));
            }
            //se ho modificato anche il nome/ creato un nuovo gruppo, mando nome e valuta
            if (g_name != null) {
                params.put("g_name", g_name);
                params.put("valuta", valuta);
            }
            //eseguo la richiesta
            String answer = connection.POST(url, params);
            //gestisco la risposta
            JSONObject response = new JSONObject(answer);
            return response.getInt("g_id");
        } catch (Exception e) {
            System.err.println(e.toString());
            return -1;
        }
    }

    public int deleteShop(int s_id) {
        int toret = -1;
        try {
            String url = location + "deleteShop";
            Map<String, String> params = new HashMap<>();
            params.put("id", Integer.toString(user_id));
            params.put("key", key);
            params.put("s_id", Integer.toString(s_id));
            //eseguo la richiesta
            String answer = connection.POST(url, params);
            //gestisco la risposta
            JSONObject response = new JSONObject(answer);
            if (response.getString("response").equals("ok")) toret = 1;
            else toret = 0;
            return toret;
        } catch (Exception e) {
            System.err.println(e.toString());
            return toret;
        }
    }

    //funzione per cancellare un gruppo
    public int deleteGroup(int g_id) {
        int toret = -1;
        try {
            String url = location + "deleteGroup";
            Map<String, String> params = new HashMap<>();
            params.put("id", Integer.toString(user_id));
            params.put("key", key);
            params.put("g_id", Integer.toString(g_id));
            //eseguo la richiesta
            String answer = connection.POST(url, params);
            //gestisco la risposta
            JSONObject response = new JSONObject(answer);
            if (response.getString("response").equals("ok"))
                toret = 1;
            else toret = 0;
            return toret;
        } catch (Exception e) {
            System.err.println(e.toString());
            return toret;
        }
    }

    //funzione che controlla se una spesa è modificabile o meno
    public boolean isChangeableShop(int s_id) {
        try {
            String url = location + "isChangeableShop";
            Map<String, String> params = new HashMap<>();
            params.put("id", Integer.toString(user_id));
            params.put("key", key);
            params.put("s_id", Integer.toString(s_id));
            //eseguo la richiesta
            String answer = connection.POST(url, params);
            //gestisco la risposta
            JSONObject response = new JSONObject(answer);
            return response.getString("response").equals("ok");
        } catch (Exception e) {
            System.err.println(e.toString());
            return false;
        }
    }

    //funzione per modificare una spesa
    public boolean changeShop(int s_id, String description, String value) {
        try {
            String url = location + "changeShop";
            Map<String, String> params = new HashMap<>();
            params.put("id", Integer.toString(user_id));
            params.put("key", key);
            params.put("s_id", Integer.toString(s_id));
            params.put("desc", description);
            params.put("value", value);
            //eseguo la richiesta
            String answer = connection.POST(url, params);
            //gestisco la risposta
            JSONObject response = new JSONObject(answer);
            return response.getString("response").equals("ok");
        } catch (Exception e) {
            System.err.println(e.toString());
            return false;
        }
    }

    //funzione per aggiornare una notifica di spesa
    public boolean updateNotify(String desc, int n_id) {
        try {
            String url = location + "updateNotify";
            Map<String, String> params = new HashMap<>();
            params.put("id", Integer.toString(user_id));
            params.put("key", key);
            params.put("n_id", Integer.toString(n_id));
            if (desc != null) params.put("desc", desc);
            //eseguo la richiesta
            String answer = connection.POST(url, params);
            //gestisco la risposta
            JSONObject response = new JSONObject(answer);
            return response.getString("response").equals("ok");
        } catch (Exception e) {
            System.err.println(e.toString());
            return false;
        }
    }

    //funzione per segnare come pagato un debito
    public boolean setAsPaid(int u_id, int g_id) {
        try {
            String url = location + "setAsPaid";
            Map<String, String> params = new HashMap<>();
            params.put("id", Integer.toString(user_id));
            params.put("key", key);
            params.put("u_id", Integer.toString(u_id));
            //se il debito è rispetto solo a un gruppo, ne invio l'id
            if (g_id != -1) params.put("g_id", Integer.toString(g_id));
            //eseguo la richiesta
            String answer = connection.POST(url, params);
            //gestisco la risposta
            JSONObject response = new JSONObject(answer);
            return response.getString("response").equals("ok");
        } catch (Exception e) {
            System.err.println(e.toString());
            return false;
        }
    }

    //funzione per ottenere i dati da mostrare nel linechart
    public ArrayList<ChartPoint> getLineChartData(String valuta, String d1, String d2) {
        try {
            ArrayList<ChartPoint> values = new ArrayList<>();
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            long now = System.currentTimeMillis();
            String url = location + "getSpeseForCharts";
            Map<String, String> params = new HashMap<>();
            params.put("id", Integer.toString(user_id));
            params.put("key", key);
            params.put("valuta", valuta);
            params.put("date1", d1);
            params.put("date2", d2);
            //eseguo la richiesta
            String answer = connection.POST(url, params);
            //gestisco la risposta
            JSONObject obj = new JSONObject(answer);
            Iterator<?> keys = obj.keys();
            while (keys.hasNext()) {
                String key = (String) keys.next();
                float value = Float.parseFloat(String.format(Locale.US, "%.2f", obj.getDouble(key)));
                Date d = format.parse(key);
                String data = (String) DateUtils.getRelativeTimeSpanString(d.getTime(), now, DateUtils.DAY_IN_MILLIS);
                ChartPoint p = new ChartPoint(data, value);
                values.add(p);
            }
            return values;
        } catch (Exception e) {
            System.err.println(e.toString());
            return null;
        }
    }

    //funzione per ottenere i dati per il piechart
    public Map<String, Float> getPieChartData(String valuta, String d1, String d2) {
        try {
            Map<String, Float> values = new HashMap<>();
            String url = location + "getCreditDebtForCharts";
            Map<String, String> params = new HashMap<>();
            params.put("id", Integer.toString(user_id));
            params.put("key", key);
            params.put("valuta", valuta);
            //eseguo la richiesta
            String answer = connection.POST(url, params);
            //gestisco la risposta
            JSONObject obj = new JSONObject(answer);
            double d_credito = obj.getDouble("credito");
            double d_debito = obj.getDouble("debito");
            float credito = Float.parseFloat(String.format(Locale.US, "%.2f", d_credito));
            float debito = Float.parseFloat(String.format(Locale.US, "%.2f", d_debito));
            values.put("credit", credito);
            values.put("debt", debito);
            return values;
        } catch (Exception e) {
            System.err.println(e.toString());
            return null;
        }
    }

    //funzione per ottenere un immagine profilo/gruppo dal server
    public JSONObject getImageFromServer(int type, int img_id) {
        try {
            String url = location + "getImage";
            Map<String, String> params = getImageHeaders(type, img_id);
            //eseguo la richiesta
            String answer = connection.POST(url, params);
            return new JSONObject(answer);

        } catch (Exception e) {
            System.err.println(e.toString());
            return null;
        }
    }

    //funzione per salvare una nuova immagine sul server
    public String saveImageToServer(int type, int img_id, Bitmap img) {
        try {
            String url = location + "saveImage";
            Map<String, String> params = getImageHeaders(type, img_id);
            params.put("img_ext","png");
            params.put("raw_img", bitmapToString(img));
            //eseguo connessione
            String answer = connection.POST(url, params);
            //gestisco risposta
            JSONObject obj = new JSONObject(answer);
            return obj.getString("response");
        } catch (Exception e) {
            System.err.println(e.toString());
            return null;
        }
    }

    //funzione per controllare se l'immagine in cache è aggiornata
    public boolean checkCachedImage(int type, int id,String timestamp) {
        try {
            String url = location + "checkImage";
            Map<String, String> params = getImageHeaders(type, id);
            params.put("timestamp", timestamp);
            String answer = connection.POST(url, params);
            //gestisco risposta
            JSONObject obj = new JSONObject(answer);
            return obj.getString("response").equals("up-to-date");
        } catch (Exception e) {
            System.err.println(e.toString());
            return false;
        }
    }

    //funzione per inviare una notifica push per richiedere un pagamento
    public Boolean sendPaymentNotification(int user_id) {
        try{
            String url = location + "askForPayment";
            Map<String, String> params = new HashMap<>();
            params.put("id", Integer.toString(this.user_id));
            params.put("key", key);
            params.put("u_id",Integer.toString(user_id));
            //eseguo richiesta
            String answer = connection.POST(url, params);
            //gestisco risposta
            JSONObject obj = new JSONObject(answer);
            return obj.get("response").equals("sent");
        }catch (Exception e){
            System.err.println(e.toString());
            return false;
        }
    }

    //funzione per ripristinare la password dell'account
    public Integer restorePassword(String mail) {
        try{
            String url = location + "restorePassword";
            Map<String, String> params = new HashMap<>();
            params.put("mail",mail);
            //eseguo richiesta
            String answer = connection.POST(url,params);
            //gestisco risposta
            JSONObject obj = new JSONObject(answer);
            String result = obj.getString("response");
            switch (result) {
                case "ok":
                    return 1;
                case "denied":
                    return 0;
                default:
                    return -1;
            }
        }catch (Exception e){
            System.err.println(e.toString());
            return -1;
        }
    }
}
