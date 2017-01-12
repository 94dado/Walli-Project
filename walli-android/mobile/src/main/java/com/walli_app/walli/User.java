package com.walli_app.walli;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by dado on 23/06/2016.
 */
public class User {
    private int id;
    private String nick;
    private String mail;
    private String name;
    private String surname;
    private String phone;

    public User(){
        nick = mail = name = surname = phone = null;
    }

    public User(int id, String nick, String mail, String name, String surname, String phone) {
        this.id = id;
        this.nick = nick;
        this.mail = mail;
        this.name = name;
        this.surname = surname;
        this.phone = phone;
    }

    public boolean isNull(){
        return nick == null && mail == null && name == null && surname == null && phone == null;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNick() {
        return nick;
    }

    public String getMail() {
        return mail;
    }

    public void setMail(String mail) {
        this.mail = mail;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    //aggiorna l'utente con i dati da un json
    public User updateFromJson(String json){
        try{
            JSONObject obj = new JSONObject(json);
            id = obj.getInt("id");
            nick = obj.getString("nick");
            mail = obj.getString("mail");
            name = obj.getString("name");
            surname = obj.getString("surname");
            phone = obj.getString("phone");
            return this;
        }catch (Exception e){
            return null;
        }
    }

    //converte l'utente in un json
    public String toJson(){
        try{
            JSONObject obj = new JSONObject();
            obj.put("id",id);
            obj.put("nick",nick);
            obj.put("mail",mail);
            obj.put("name",name);
            obj.put("surname",surname);
            obj.put("phone",phone);
            return obj.toString();
        }catch (Exception e){
            return null;
        }
    }

    //static method

    //trasforma un array di utenti in un json
    public static String arrayListToJson(ArrayList<User> users){
        try{
            JSONArray array = new JSONArray();
            for(int i=0;i<users.size();i++){
                User u = users.get(i);
                String json = u.toJson();
                JSONObject obj = new JSONObject(json);
                array.put(obj);
            }
            return array.toString();
        }catch (Exception e){
            System.err.println(e.toString());
            return null;
        }
    }

    //recupera un array di utenti da un json
    public static ArrayList<User> jsonToArrayList(String json){
        try{
            ArrayList<User> users = new ArrayList<>();
            JSONArray array = new JSONArray(json);
            for(int i=0;i<array.length();i++){
                JSONObject obj = array.getJSONObject(i);
                User u = new User().updateFromJson(obj.toString());
                users.add(u);
            }
            return users;
        }catch (Exception e){
            System.err.println(e.toString());
            return null;
        }
    }

    //restituisce una lista di nickname da una lista di utenti
    public static ArrayList<String> getNicknames(ArrayList<User> users){
        ArrayList<String> list = new ArrayList<>();
        for (int i = 0; i< users.size(); i++){
            User u = users.get(i);
            String nick = u.getNick();
            list.add(nick);
        }
        return list;
    }

}
