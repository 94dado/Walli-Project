package com.walli_app.walli;

import java.sql.Date;
import java.text.SimpleDateFormat;

/**
 * Created by dado on 22/06/2016.
 */
public class Shop {
    private int id;
    private String value;
    private String description;
    private Date date;
    private String user_name;
    private boolean paid;
    private int user_id;

    public Shop(){
        user_id = id = -1;
        description =  user_name =  value = null;
        date = null;
    }

    public Shop(int id, String value, String description, String date, String user_name, boolean paid, int user_id) {
        this.id = id;
        this.value = value;
        this.description = description;
        setDate(date);
        this.user_name = user_name;
        this.paid = paid;
        this.user_id = user_id;
    }

    public boolean isNull(){
        return id == -1 && value == null && description == null && date == null  && user_name == null;
    }
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getUser_name() {
        return user_name;
    }

    public void setUser_name(String user_name) {
        this.user_name = user_name;
    }

    public String getDescription() {
        return description;
    }


    public Date getDate() {
        return date;
    }


    public void setDate(String date){
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        try{
            java.util.Date d = format.parse(date);
            this.date = new Date(d.getTime());
        }catch(Exception e){
            this.date = null;
            System.out.println("Errore inizializzazione della data. Che cazzo di stringa mi hai passato?");
        }
    }

    public boolean isPaid() {
        return paid;
    }

    public int getUser_id() {
        return user_id;
    }

    public void setUser_id(int user_id) {
        this.user_id = user_id;
    }
}
