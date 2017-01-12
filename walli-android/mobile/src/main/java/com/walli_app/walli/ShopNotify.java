package com.walli_app.walli;

/**
 * Created by dado on 28/06/2016.
 */
public class ShopNotify {
    private int id;
    private String description;
    private int user_id;
    private String user_nick;
    private int g_id;

    public ShopNotify(){
        id = user_id = g_id = -1;
        description = user_nick = null;
    }

    public ShopNotify(int id, String description, int user_id, String user_nick, int g_id) {
        this.id = id;
        this.description = description;
        this.user_id = user_id;
        this.user_nick = user_nick;
        this.g_id = g_id;
    }

    public boolean isNull(){
        return (id == -1 && description == null && user_id == -1 && user_nick == null && g_id == -1);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public int getUser_id() {
        return user_id;
    }

    public void setUser_id(int user_id) {
        this.user_id = user_id;
    }

    public String getUser_nick() {
        return user_nick;
    }
}
