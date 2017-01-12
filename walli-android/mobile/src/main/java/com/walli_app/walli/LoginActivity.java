package com.walli_app.walli;
import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.firebase.iid.FirebaseInstanceId;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends CustomAppCompatActivity{

    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    private EditText edit_username;
    private EditText edit_password;
    private int user_id;
    private User us;
    private CustomAppCompatActivity me;
    private String key = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        me = this;
        int saved_id;
        User us = null;
        //recupero dati su eventuali precedenti login
        SharedPreferences pref = getSharedPreferences("login",MODE_PRIVATE);
        if(!getIntent().hasExtra("delete_user")) {
            if (pref.contains("user")){
                //c'è già un login in memoria. recupero i dati
                String json = pref.getString("user",null);
                us = new User().updateFromJson(json);
                saved_id = us.getId();
                key = pref.getString("key",null);
            }else{
                //no dati in memoria
                saved_id = -1;
            }
        }else{
            //cancello le shared preferences perchè è stato eseguito logout
            SharedPreferences.Editor editor = pref.edit();
            editor.clear();
            editor.apply();
            saved_id = -1;
        }
        user_id = saved_id;
        if(user_id != -1){
            //bypasso il login perchè ho dati in memoria
            Intent intent = new Intent(getBaseContext(),MainActivity.class);
            intent.putExtra("user",us.toJson());
            intent.putExtra("key",key);
            startActivity(intent);
            finish();
        }else{
            //mostro schermata di login
            setContentView(R.layout.activity_login);
            //rimuovo focus dall'editText
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
            Button signIn = (Button) findViewById(R.id.sign_in);
            if(signIn!=null)  signIn.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    attemptLogin();
                }
            });
            Button signUp = (Button) findViewById(R.id.sign_up);
            if(signUp != null) signUp.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(getBaseContext(),SignUpActivity.class);
                    startActivity(intent);
                }
            });
            edit_username = (EditText)findViewById(R.id.username);
            edit_password = (EditText)findViewById(R.id.password);
            //preparo pulsante password dimenticata
            setForgottenPassword();
        }
    }

    private void setForgottenPassword() {
        TextView label = (TextView) findViewById(R.id.restore_pwd);
        final LayoutInflater inflater = getLayoutInflater();
        if(label !=null) label.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = prompt(getString(R.string.insert_email));
                @SuppressLint("InflateParams")
                final LinearLayout layout = (LinearLayout) inflater.inflate(R.layout.restore_password,null);
                builder.setView(layout);
                builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //todo invio al server la richiesta
                        EditText edit = (EditText) layout.findViewById(R.id.restore_pwd_email);
                        String mail;
                        if(edit!=null) mail = edit.getText().toString();
                        else mail = "";
                        String error = FormValidator.checkValidMail(mail,me);
                        if(error.equals("")){
                            //tutto ok. procedo
                            RestorePasswordTask task = new RestorePasswordTask(mail);
                            task.execute();
                            dialog.dismiss();
                        }else{
                            alert(error);
                        }
                    }
                });
                builder.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                builder.show();
            }
        });
    }


    @Override
    protected void onStop(){
        //se è stato fatto un login, salvo le credenziali prima di chiudere l'activity
        if(user_id != -1){
            SharedPreferences pref = getSharedPreferences("login",MODE_PRIVATE);
            SharedPreferences.Editor editor = pref.edit();
            editor.putString("user",us.toJson());
            editor.putString("key",key);
            editor.apply();
        }
        super.onStop();
    }

    //prova ad eseguire il login
    private void attemptLogin() {
        String username = edit_username.getText().toString();
        String password = edit_password.getText().toString();
        if(isConnectionAvailable()){
            //recupero token di firebase
            String token = FirebaseInstanceId.getInstance().getToken();
            //tento login
            UserLoginTask task = new UserLoginTask(username, password,token);
            task.execute();
        }else{
            alert(getString(R.string.network_error));
        }

    }

    //classe per il processo secondario che esegue il login
    public class UserLoginTask extends AsyncTask<Void, Void, Boolean> {

        private final String mUsername;
        private final String mPassword;
        private final String token;
        private final DBManager manager = new DBManager();

        UserLoginTask(String username, String password,String token) {
            mUsername = username;
            mPassword = password;
            this.token = token;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            //tento il login
            us = manager.login(mUsername,mPassword,token);
            if(us != null){
                user_id = us.getId();
                return true;
            }else{
                user_id = -1;
                return false;
            }
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            if (success) {
                key = manager.toString();
                finish();
                Intent intent = new Intent(getBaseContext(),MainActivity.class);
                intent.putExtra("user",us.toJson());
                intent.putExtra("key",key);
                startActivity(intent);

            } else {
                alert(getString(R.string.error_incorrect_password));
            }
        }
    }

    //classe per il processo secondario che ripristina la password dimenticata
    public class RestorePasswordTask extends AsyncTask<Void, Void, Integer>{
        private String mail;

        public RestorePasswordTask(String mail) {
            this.mail = mail;
        }

        @Override
        protected Integer doInBackground(Void... params) {
            DBManager dbm = new DBManager();    //non ho bisogno della chiave per la chiamata, non inizializzo il dbm della classe
            return dbm.restorePassword(mail);
        }

        @Override
        protected void onPostExecute(Integer result) {
            int val;
            if(result == 1) val = R.string.password_changed;
            else if(result == 0) val = R.string.password_invalid_mail;
            else val = R.string.error_forgot_password;
            String text = getString(val);
            alert(text);
        }
    }
}

