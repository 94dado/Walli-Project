package com.walli_app.walli;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;

import java.util.Currency;

public class CreateShopActivity extends CustomAppCompatActivity {

    private int g_id;
    private boolean created;
    private int n_id = -1;
    private boolean mod;
    private int s_id = -1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //preparo layout
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_shop);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar ab = getSupportActionBar();
        if(ab!=null){
            ab.setDisplayHomeAsUpEnabled(true);
            ab.setDisplayShowHomeEnabled(true);
        }
        EditText desc = (EditText) findViewById(R.id.new_shop_description);
        if(desc!=null) desc.setSelected(true);
        //recupero dati
        Intent i = getIntent();
        dbm = new DBManager(i.getStringExtra("key"),i.getIntExtra("user_id",-1));
        g_id = i.getIntExtra("group_id",-1);
        String g_valuta = i.getStringExtra("valuta");
        //metto il simbolo della valuta
        TextView v = (TextView) findViewById(R.id.valuta);
        String valuta = Currency.getInstance(g_valuta).getSymbol();
        if(v!=null) v.setText(valuta);
        //se questa schermata viene aperta da una notifica di spesa, preparo direttamente la descrizione
        if(i.hasExtra("desc")){
            if(desc!=null) desc.setText(i.getStringExtra("desc"));
            n_id = i.getIntExtra("n_id",-1);
        }
        //se si sta modificando una spesa già inserita
        if(i.hasExtra("value")){
            mod = true;
            TextView val = (TextView) findViewById(R.id.new_shop_value);
            String value = i.getStringExtra("value");
            if(val!=null) val.setText(value);
            s_id = i.getIntExtra("id",-1);
        }else{
            mod = false;
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
            //creo la spesa
            case R.id.next:
                //recupero i dati
                EditText desc = (EditText) findViewById(R.id.new_shop_description);
                EditText val = (EditText) findViewById(R.id.new_shop_value);
                String description,value;
                if(desc!=null) description = desc.getText().toString();
                else description = "";
                if(val!=null)value = val.getText().toString();
                else value = "";
                //controllo se i dati sono validi
                String error = FormValidator.createNewShop(this, description, value);
                if (error.equals("")) {
                    //tutto ok. Posso generare/modificare la spesa
                    if (isConnectionAvailable()) {
                        if(!mod){
                            InsertSpesaTask task = new InsertSpesaTask(description,value,g_id,n_id);
                            task.execute();
                        }else{
                            ModSpesaTask task = new ModSpesaTask(description,value,s_id);
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
        //se non ho creato nulla, restituisco all'activity chiamante
        if(!created){
            Intent ret = new Intent();
            setResult(RESULT_CANCELED,ret);
        }
        super.onDestroy();
    }

    //classe per processo secondario che salva la spesa
    public class InsertSpesaTask extends AsyncTask<Void, Void, Boolean> {
        private String description;
        private String value;
        private int g_id;
        private int n_id;

        public InsertSpesaTask(String description, String value, int g_id, int n_id) {
            this.description = description;
            this.value = value;
            this.g_id = g_id;
            this.n_id = n_id;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            //tento il login
           return dbm.insertSpesa(description,value,g_id,n_id);
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            if (success) {
                toast(getString(R.string.shop_saved),false);
                Intent ret = new Intent();
                setResult(RESULT_OK,ret);
                created = true;
                finish();
            } else {
                alert(getString(R.string.error_insert_shop));
            }
        }
    }

    public class ModSpesaTask extends AsyncTask<Void, Void, Boolean> {
        private String description;
        private String value;
        private int s_id;

        public ModSpesaTask(String description, String value, int s_id) {
            this.description = description;
            this.value = value;
            this.s_id = s_id;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            //tento il login
            return  dbm.changeShop(s_id,description,value);
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            if (success) {
                toast(getString(R.string.shop_updated),false);
                Intent ret = new Intent();
                setResult(RESULT_OK,ret);
                created = true;
                finish();
            } else {
                alert(getString(R.string.error_update_shop));
            }
        }
    }
}
