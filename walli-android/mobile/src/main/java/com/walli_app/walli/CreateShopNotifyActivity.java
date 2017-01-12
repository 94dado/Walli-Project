package com.walli_app.walli;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.EditText;

/**
 * Created by dado on 29/06/2016.
 */
public class CreateShopNotifyActivity extends CustomAppCompatActivity {
    private int g_id;
    private CustomAppCompatActivity me;
    private boolean created;
    private boolean mod = false;
    private int n_id = -1;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_shop_notify);
        me = this;      //per referenziarmi all'oggetto dentro agli handler
        //setto toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar ab = getSupportActionBar();
        if(ab!=null){
            ab.setDisplayHomeAsUpEnabled(true);
            ab.setDisplayShowHomeEnabled(true);
        }
        //preparo grafica
        EditText desc = (EditText) findViewById(R.id.new_shop_description);
        if(desc!=null) desc.setSelected(true);
        //recupero dati
        Intent i = getIntent();
        dbm = new DBManager(i.getStringExtra("key"),i.getIntExtra("user_id",-1));
        g_id = i.getIntExtra("group_id",-1);
        if(i.hasExtra("desc")){
            String desc1 = i.getStringExtra("desc");
            if(desc!=null) desc.setText(desc1);
            n_id = i.getIntExtra("n_id",-1);
            mod = true;
        }
    }

    @Override
    public  boolean onCreateOptionsMenu ( Menu menu )  {
        MenuInflater inflater =  getMenuInflater () ;
        inflater.inflate(R.menu.next_menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            //avanzo per la creazione della notifica di spesa
            case R.id.next:
                EditText desc = (EditText) findViewById(R.id.new_shop_description);
                String description;
                if(desc!=null) description = desc.getText().toString();
                else description = "";
                String error = FormValidator.createNewNotify(me,description);
                if (error.equals("")) {
                    //tutto ok. Posso generare la spesa
                    if (isConnectionAvailable()) {
                        if(!mod){
                            InsertNotificaSpesaTask task = new InsertNotificaSpesaTask(description,g_id);
                            task.execute();
                        }else{
                            UpdateNotificaSpesaTask task = new UpdateNotificaSpesaTask(description,n_id);
                            task.execute();
                        }
                        return true;
                    } else {
                        alert(getString(R.string.network_error));
                        return false;
                    }
                }else{
                    alert(error);
                }
        }
        return false;
    }

    @Override
    protected void onDestroy() {
        //se si chiude l'activity senza aver generato nulla
        if(!created){
            Intent ret = new Intent();
            setResult(RESULT_CANCELED,ret);
        }
        super.onDestroy();
    }

    //classe per il processo secondario per creare la notifica di spesa
    public class InsertNotificaSpesaTask extends AsyncTask<Void, Void, Boolean> {
        private String description;
        private int g_id;

        public InsertNotificaSpesaTask(String description, int g_id) {
            this.description = description;
            this.g_id = g_id;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            //tento il login
            return dbm.insertNotify(description,g_id);
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            if (success) {
                toast(getString(R.string.shop_notify_saved),false);
                Intent ret = new Intent();
                setResult(RESULT_OK,ret);
                created = true;
                finish();
            } else {
                alert(getString(R.string.error_insert_notify));
            }
        }
    }

    //classe per il processo secondario per aggiornare una notifica di spesa già esistente
    public class UpdateNotificaSpesaTask extends AsyncTask<Void, Void, Boolean> {
        private String description;
        private int n_id;

        public UpdateNotificaSpesaTask(String description, int n_id) {
            this.description = description;
            this.n_id = n_id;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            //tento il login
            return dbm.updateNotify(description,n_id);
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            if (success) {
                toast(getString(R.string.shop_notify_updated),false);
                Intent ret = new Intent();
                setResult(RESULT_OK,ret);
                created = true;
                finish();
            } else {
                alert(getString(R.string.error_update_notify));
            }
        }
    }

}
