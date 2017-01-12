package com.walli_app.walli;

/**
 * Created by dado on 28/06/2016.
 */

public class CreditDebit {
    private String value;
    private User with;

    public CreditDebit(String value, User with) {
        this.value = value;
        this.with = with;
    }

    public CreditDebit(){
        value = null;
        with = null;
    }

    public Boolean isNull(){
        return (value == null && with == null);
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public User getWith() {
        return with;
    }

    public void setWith(User with) {
        this.with = with;
    }
}
