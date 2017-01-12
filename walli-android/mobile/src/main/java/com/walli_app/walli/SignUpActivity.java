package com.walli_app.walli;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;

/**
 * Created by dado on 20/06/16.
 */
public class SignUpActivity extends CustomAppCompatActivity {
    private Menu menu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        //rimuovo focus da EditText
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        //setto toolbar
        Toolbar t = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(t);
        ActionBar ab = getSupportActionBar();
        if(ab!=null){
            ab.setDisplayHomeAsUpEnabled(true);
            ab.setDisplayShowHomeEnabled(true);
        }
    }

    @Override
    public  boolean onCreateOptionsMenu (Menu menu )  {
        MenuInflater inflater =  getMenuInflater () ;
        inflater.inflate(R.menu.next_menu,menu);
        this.menu = menu;
        return  true ;
    }

    //registrazione completata. mostra schermata con informazioni sulla conferma account
    private void subscriptionComplete(String username, String email){
        //preparo la schermata da mostrare
        TextView name = (TextView) findViewById(R.id.user_name_confirm);
        if(name!=null) name.setText(username);
        TextView description = (TextView) findViewById(R.id.new_user_description);
        String desc = getString(R.string.registration_confirmed_description);
        desc = desc.replace("FUCKINGMAIL",email);
        if(description!=null) description.setText(desc);
        //eseguo lo scambio dei layout mostrati
        View first = findViewById(R.id.subscribe_first);
        View second = findViewById(R.id.subscribe_second);
        if(first!=null && second != null) {
            first.setVisibility(View.INVISIBLE);
            second.setVisibility(View.VISIBLE);
        }
        //nascondo pulsanti da toolbar
        menu.findItem(R.id.next).setVisible(false);

    }

    //ottiene i dati da un campo della form
    private String getFormValue(EditText edit){
        if(edit!=null) return edit.getText().toString();
        else return "";
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if(id == R.id.next){
            //recupero i dati
            String username,name,surname,cell,mail,pwd,confirm_pwd;
            username = getFormValue((EditText) findViewById(R.id.username));
            name = getFormValue((EditText) findViewById(R.id.profile_name_edit));
            surname = getFormValue((EditText) findViewById(R.id.profile_surname_edit));
            cell = getFormValue((EditText) findViewById(R.id.profile_phone_edit));
            mail = getFormValue((EditText) findViewById(R.id.profile_email_edit));
            pwd = getFormValue((EditText) findViewById(R.id.profile_pass_edit));
            confirm_pwd = getFormValue((EditText) findViewById(R.id.profile_pass_confirm_edit));
            //contorllo che siano validi
            String error = FormValidator.signUpValidator(this,username,name,surname,mail,cell,pwd,confirm_pwd);
            //Se tutto ok, procedo
            if(error.equals("")){
                //tento la registrazione se c'è connessione
                if(isConnectionAvailable()) {
                    SignUpTask task = new SignUpTask(username, name, surname, mail, cell, pwd);
                    task.execute();
                }else{
                    alert(getString(R.string.network_error));
                }
            }else{
                alert(error);
            }
            return true;
        }
        return false;
    }

    //classe del processo secondario che esegue la registrazione
    public class SignUpTask extends AsyncTask<Void, Void, Boolean> {

        private String username,name,surname,mail,cell,pwd;
        private final DBManager manager = new DBManager();

        public SignUpTask(String username, String name, String surname, String mail, String cell, String pwd) {
            this.username = username;
            this.name = name;
            this.surname = surname;
            this.mail = mail;
            this.cell = cell;
            this.pwd = pwd;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            //tento il login
            int code = manager.signUp(username,name,surname,mail,cell,pwd);
            return (code == 200);
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            if (success) {
                subscriptionComplete(username,mail);
            } else {
                alert(getString(R.string.username_already_taken));
            }
        }
    }


}
