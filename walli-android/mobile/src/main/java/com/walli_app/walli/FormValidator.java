package com.walli_app.walli;

import android.util.Patterns;
import android.widget.EditText;

/**
 * Created by dado on 24/06/2016.
 */
public class FormValidator {

    //convalida i campi inseriti per la registrazione di un nuovo utente
    public static String signUpValidator(CustomAppCompatActivity activity, String username, String name, String surname, String mail, String cell, String pwd, String confirm_pwd){
        String error = "";
        //controllo che siano validi
        if(username.equals("") || username.length() < 4){                           //username
            error += activity.getString(R.string.signup_nick_error)+"\n";
        }
        error += changeUserDataValidator(activity,name,surname,cell,mail,pwd,confirm_pwd,true);
        return error;
    }

    //controlla se i dati inseriti per la modifica dei dati utente sono validi
    public static String changeUserDataValidator(CustomAppCompatActivity activity, String name, String surname, String cell, String mail, String pwd, String confirm_pwd, boolean checkPwd){
        String error = "";
        if(name.equals("")){                                                        //nome
            error += activity.getString(R.string.signup_name_error)+"\n";
        }
        if(surname.equals("")){                                                     //cognome
            error += activity.getString(R.string.signup_surname_error)+"\n";
        }
        if(mail.equals("") || !Patterns.EMAIL_ADDRESS.matcher(mail).matches()){      //mail
            error += activity.getString(R.string.signup_mail_error)+"\n";
        }
        if(cell.equals("") || !Patterns.PHONE.matcher(cell).matches()){             //cell
            error += activity.getString(R.string.signup_cell_error)+"\n";
        }
        if(checkPwd){
            if (pwd.equals("") || pwd.length() < 8){                                    //password
                error += activity.getString(R.string.signup_pwd_error)+"\n";
            }
            if(!confirm_pwd.equals(pwd)){                                               //confirm password
                error += activity.getString(R.string.signup_confirmpwd_error)+"\n";
            }
        }
        return error;
    }

    //controlla se i dati inseriti per creare una nuova spesa sono validi
    public static String createNewShop(CustomAppCompatActivity activity,String description, String value){
        String error = "";
        error += createNewNotify(activity,description);
        if(value.equals("") || Double.parseDouble(value) <= 0){
            error += activity.getString(R.string.shop_value_error)+"\n";
        }
        return error;
    }

    //controlla se i dati inseriti per creare una nuova notifica di spesa sono validi
    public static String createNewNotify(CustomAppCompatActivity activity, String description){
        String error = "";
        if(description.equals("")){
            error += activity.getString(R.string.shop_description_error)+"\n";
        }
        return error;
    }

    //controlla se è stato modificato qualcosa fra i dati dell'utente e quelli in memoria nella GUI
    public static boolean profileDataChanged(EditText[] info,User user) {
        String name = info[0].getText().toString();
        String surname = info[1].getText().toString();
        String phone = info[2].getText().toString();
        String mail = info[3].getText().toString();
        String pwd = info[4].getText().toString();
        String confirm_pwd = info[5].getText().toString();
        return !name.equals(user.getName()) || !surname.equals(user.getSurname()) || !phone.equals(user.getPhone()) || !mail.equals(user.getMail()) || !pwd.equals("") || !confirm_pwd.equals("");
    }

    public static String checkValidMail(String mail,CustomAppCompatActivity activity) {
        String error = "";
        if (mail.equals("") || !Patterns.EMAIL_ADDRESS.matcher(mail).matches()) {      //mail
            error += activity.getString(R.string.signup_mail_error) + "\n";
        }
        return error;
    }
}
