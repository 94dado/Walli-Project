package com.walli_app.walli;

import org.json.JSONException;
import org.json.JSONObject;

import java.sql.Date;
import java.text.SimpleDateFormat;

/**
 * Created by dado on 22/04/16.
 */

//classe entry dei gruppi
public class Group {
    private int id;
    private String name;
    private String money;
    private String evaluate;
    private Date lastMod;
    private int n_notify;
    private int user_id;

    public Group(){
        name=money=evaluate=null;
        lastMod = null;
        n_notify = id = user_id = -1;
    }

    public Group(int id, String name, String money, String evaluate, String lastMod, int user_id, int n_notify) {
        this.name = name;
        this.money = money;
        this.evaluate = evaluate;
        setDate(lastMod);
        this.user_id = user_id;
        this.n_notify = n_notify;
        this.id = id;
    }

    //controlla se tutti i campi sono invalidi
    public boolean isNull(){
        return name == null && evaluate == null && money == null && lastMod == null && n_notify == -1;
    }
    //memorizza la data dell'ultima modifica
    public void setDate(String date){
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        try{
            java.util.Date d = format.parse(date);
            this.lastMod = new Date(d.getTime());
        }catch(Exception e){
            this.lastMod = null;
            System.out.println("Errore inizializzazione della data. Che cazzo di stringa mi hai passato?");
        }
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getN_notify() {
        return n_notify;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setEvaluate(String evaluate) {
        this.evaluate = evaluate;
    }


    public String getName() {
        return name;
    }

    public String getEvaluate() {
        return evaluate;
    }

    public String getMoney() {
        return money;
    }

    public Date getLastMod() {
        return lastMod;
    }

    public int getUser_id() {
        return user_id;
    }

    public void setUser_id(int user_id) {
        this.user_id = user_id;
    }

    //restituisce un json equivalente a un gruppo
    public String toJson(){
        JSONObject json = new JSONObject();
        try{
            json.put("id",id);
            json.put("name",name);
            json.put("money",money);
            json.put("evaluate",evaluate);
            json.put("lastMod",lastMod);
            json.put("user_id",user_id);
            json.put("n_notify",n_notify);
            return json.toString();
        }catch(JSONException e){
            return "";
        }

    }

    //aggiorna e restituisce il gruppo coi dati dal json
    public Group updateFromJson(String json){
        try{
            JSONObject js = new JSONObject(json);
            id = js.getInt("id");
            name = js.getString("name");
            money = js.getString("money");
            evaluate = js.getString("evaluate");
            try{
                String lastMod = js.getString("lastMod");
                setDate(lastMod);
            }catch(Exception e){
                System.out.println(e.toString());
                lastMod = null;
            }
            user_id = js.getInt("user_id");
            n_notify = js.getInt("n_notify");
        }catch(JSONException e){
            System.out.println(e.toString());
            return null;
        }
        return this;
    }
}
