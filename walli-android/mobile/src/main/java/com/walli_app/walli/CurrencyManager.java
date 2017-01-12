package com.walli_app.walli;

import android.content.SharedPreferences;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Currency;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

/**
 * Created by dado on 02/07/2016.
 */
public class CurrencyManager {

    //restituisce tutte le valute conosciute dal sistema
    private static Set<Currency> getAllCurrencies(){
        Set<Currency> toret = new HashSet<>();
        Locale[] locs = Locale.getAvailableLocales();

        for(Locale loc : locs) {
            try {
                toret.add( Currency.getInstance( loc ) );
            } catch(Exception exc) {
                System.err.println(exc.toString());
            }
        }

        return toret;
    }

    //restituisce tutte le currency conosciute, mettendo in testa quella preferita
    public static ArrayList<String> getCurrencies(SharedPreferences pref){
        Set<Currency> currencies = getAllCurrencies();
        Currency current = getUserCurrencyDefault(pref);
        ArrayList<String> curr_string = new ArrayList<>();
        for(Currency c:currencies){
            if(!c.getCurrencyCode().equals(current.getCurrencyCode())){
                String val = fromCurrencyToString(c);
                curr_string.add(val);
            }
        }
        Collections.sort(curr_string);
        curr_string.add(0,fromCurrencyToString(current));
        return curr_string;
    }

    //data la sigla della valuta, restituisce una stringa rappresentante la valuta e il simbolo ad essa legato (se esiste)
    private static String fromCurrencyToString(Currency c){
        String code = c.getCurrencyCode();
        String symbol = c.getSymbol();
        return code.equals(symbol)?code:code+" "+symbol;
    }

    //restituisce il codice della valuta preferita dall'utente
    public static String getUserCurrencyCodeDefault(SharedPreferences pref){
        //se è stata data una preferenza, la recupero
        if(pref.contains("currency")){
            return pref.getString("currency",null);
        }else{
            //altrimenti recupero quella di default dal sistema
            String curr = Currency.getInstance(Locale.getDefault()).getCurrencyCode();
            SharedPreferences.Editor edit = pref.edit();
            edit.putString("currency",curr);
            edit.apply();
            return curr;
        }
    }

    //restituisce la valuta preferita dell'utente
    private static Currency getUserCurrencyDefault(SharedPreferences pref){
        //se esiste una preferenza, la restituisco
        if(pref.contains("currency")) {
            return Currency.getInstance(pref.getString("currency", ""));
            //altrimenti uso quella di default del sistema
        }else{
            Currency c = Currency.getInstance(Locale.getDefault());
            String curr = c.getCurrencyCode();
            SharedPreferences.Editor edit = pref.edit();
            edit.putString("currency",curr);
            edit.apply();
            return c;
        }
    }

    //salva la scelta della valuta preferita dell'utente
    public static void setUserCurrencyDefault(SharedPreferences pref, String currency){
        //se mi viene passata una valuta non valida, salvo quella di default del sistema
        if(currency== null || currency.equals("") || currency.length()!=3){
            currency = Currency.getInstance(Locale.getDefault()).getCurrencyCode();
        }
        //salvo
        SharedPreferences.Editor edit = pref.edit();
        edit.putString("currency",currency);
        edit.apply();
    }
}
