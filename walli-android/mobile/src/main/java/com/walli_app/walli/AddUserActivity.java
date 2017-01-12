package com.walli_app.walli;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;

import java.util.ArrayList;

public class AddUserActivity extends CustomAppCompatActivity {
    private ArrayList<User> users;
    private ArrayList<User> hints;
    private ArrayList<String> users_list;
    private AutoCompleteTextView actv;
    private ArrayAdapter<String> string_adapter;
    private AddUserActivity me;
    private String groupName;
    private String valuta;
    private boolean created;
    private Bitmap bmp;
    private int group_id;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        me = this;      //per poter refernziarmi a questo oggetto all'interno degli handler
        setContentView(R.layout.activity_add_user);
        Intent i = getIntent();
        //recupero i dati dall'intent
        dbm = new DBManager(i.getStringExtra("key"),i.getIntExtra("user_id",-1));
        group_id = i.getIntExtra("group_id",-1);
        groupName = i.getStringExtra("group_name");
        bmp = getBitmapFromIntent();
        valuta = i.getStringExtra("valuta");
        //se sto modificando, recupero gli utenti che gia' ho
        if(i.hasExtra("users")){
            String json = i.getStringExtra("users");
            users = User.jsonToArrayList(json);
        }else{
            if(savedInstanceState!= null && savedInstanceState.containsKey("users")){
                users = User.jsonToArrayList(savedInstanceState.getString("user"));
            }
            else users = new ArrayList<>();
        }
        //imposto toolbar
        Toolbar t = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(t);
        ActionBar act_bar = getSupportActionBar();
        if(act_bar!= null){
            act_bar.setDisplayHomeAsUpEnabled(true);
            act_bar.setDisplayShowHomeEnabled(true);
        }
        //imposto recycler view
        RecyclerView rv = (RecyclerView) findViewById(R.id.add_user_recycler);
        final AddUserAdapter adapter = new AddUserAdapter(users);
        LinearLayoutManager lm = new LinearLayoutManager(getBaseContext());
        if(rv!=null){
            rv.setAdapter(adapter);
            rv.setLayoutManager(lm);
        }
        //imposto autocomplete
        users_list = new ArrayList<>();
        actv = (AutoCompleteTextView) findViewById(R.id.add_user_autocomplete);
        string_adapter = new ArrayAdapter<>(this,android.R.layout.simple_dropdown_item_1line,users_list);
        actv.setAdapter(string_adapter);
        actv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //recupero utente selezionato
                User sel_user = hints.get(position);
                //lo aggiungo alla lista degli utenti
                users.add(sel_user);
                //svuoto la text view
                actv.setText("");
                //aggiorno la lista degli utenti
                adapter.notifyDataSetChanged();
            }
        });
        //listener per l'inserimento da tastiera virtuale del nome
        actv.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String text = s.toString();
                //se sono state scritti abbastanza caratteri
                if(actv.enoughToFilter()){
                    //chiedo utenti al server
                    if(isConnectionAvailable()){
                        GetHintsTask task = new GetHintsTask(text);
                        task.execute();
                    }else{
                        alert(getString(R.string.network_error));
                    }
                }
            }
        });
    }

    //restituisce true se sto creando un nuovo gruppo o ne sto modificando uno esistente
    private boolean isCreatingNewGroup(){
        return groupName != null;
    }


    @Override
    public  boolean onCreateOptionsMenu ( Menu menu )  {
        MenuInflater inflater =  getMenuInflater () ;
        inflater.inflate(R.menu.next_menu,menu);
        return true;
    }

    //salvo gli utenti per un "refactoring" della schermata
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        if(users.size() > 0) outState.putString("users",User.arrayListToJson(users));
        super.onSaveInstanceState(outState);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Freccia indietro: gestita qui perchè questa activity ha più activity che la possono chiamare
            case android.R.id.home:     //https://developer.android.com/training/implementing-navigation/ancestral.html
                    finish();
                return true;
            case R.id.next:
                //avanzo per aggiornare/creare il gruppo
                if(users.size()!=0){
                    if(isConnectionAvailable()) {
                        SaveUserTask task = new SaveUserTask(users);
                        task.execute();
                    }else{
                        alert(getString(R.string.network_error));
                    }
                }else{
                    alert(getString(R.string.insert_users));
                }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        if(!created){
            Intent i = new Intent();
            setResult(RESULT_CANCELED,i);
        }
        super.onDestroy();
    }

    public class GetHintsTask extends AsyncTask<Void, Void, Boolean> {
        private String partial;

        public GetHintsTask(String partial) {
            this.partial = partial;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            //recupero i dati
            hints = dbm.getHints(partial);
            //elimino gli hints di utenti che sono gia' nel gruppo
            if(hints == null) return false;
            for(int i=0;i<hints.size();i++){
                if(hints.get(i).getId() == dbm.getUserId()){
                    hints.remove(i);
                    break;
                }
                for(int j=0; j<users.size();j++){
                    if(users.get(j).getId() == hints.get(i).getId()){
                        hints.remove(i);
                        break;
                    }
                }
            }
            return true;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            if (!success) {
                alert(getString(R.string.error_get_users));
                System.err.println("alert in onPostExecute()");
            }else{
                users_list = User.getNicknames(hints);
                string_adapter = new ArrayAdapter<>(me,android.R.layout.simple_dropdown_item_1line,users_list);
                actv.setAdapter(string_adapter);
                actv.showDropDown();
            }
        }

        @Override
        protected void onCancelled() {
            alert(getString(R.string.error_get_users));
        }
    }

    public class SaveUserTask extends AsyncTask<Void, Void, Boolean> {
        private ArrayList<User> users;
        private int id;

        public SaveUserTask(ArrayList<User> users) {
            this.users = users;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            //minimizzo i dati al solo id utente
            int[] ids = new int[users.size()];
            for(int i=0;i<users.size();i++){
                ids[i] = users.get(i).getId();
            }
            id = dbm.updateGroup(ids,group_id,groupName,valuta);
            return id != -1;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            if (!success) {
                alert(getString(R.string.error_update_group));
                System.err.println("alert in onPostExecute()");
            }else{
                Intent i = new Intent();
                setResult(RESULT_OK,i);
                String message;
                if(isCreatingNewGroup()){
                    message = getString(R.string.new_group_created);
                }else{
                    message = getString(R.string.group_users_updated);
                }
                toast(message,false);
                created = true;
                if(bmp !=null) ImageManager.setNewImageToView(me,null,bmp,ImageManager.GROUP,id,true);
                else finish();
            }
        }

        @Override
        protected void onCancelled() {
            alert(getString(R.string.error_update_group));
        }
    }
}
