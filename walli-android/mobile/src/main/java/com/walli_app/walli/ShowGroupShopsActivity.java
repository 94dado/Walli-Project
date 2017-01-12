package com.walli_app.walli;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

public class ShowGroupShopsActivity extends CustomAppCompatActivity {

    private Group group;
    private ArrayList<Shop> spese;
    private ArrayList<User> utenti;
    private ArrayList<CreditDebit> crediti;
    private ArrayList<ShopNotify> notifiche_spese;
    private SwipeRefreshLayout swiper;
    private ShowGroupShopsActivity me;
    private ImageView group_image;
    private boolean changed = false;

    //handlers per il fab

    private View.OnClickListener fab_addShop = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Intent intent = new Intent(getBaseContext(),CreateShopActivity.class);
            intent.putExtra("key",dbm.toString());
            intent.putExtra("user_id",dbm.getUserId());
            intent.putExtra("group_id",group.getId());
            intent.putExtra("valuta",group.getEvaluate());
            startActivityForResult(intent,ACTIVITY_CODE);
        }
    };

    private View.OnClickListener fab_addNotify = new View.OnClickListener(){
        @Override
        public void onClick(View view) {
            Intent intent = new Intent(getBaseContext(),CreateShopNotifyActivity.class);
            intent.putExtra("key",dbm.toString());
            intent.putExtra("user_id",dbm.getUserId());
            intent.putExtra("group_id",group.getId());
            startActivityForResult(intent,ACTIVITY_CODE);
        }
    };

    private View.OnClickListener fab_addUser = new View.OnClickListener(){
        @Override
        public void onClick(View view) {
            Intent intent = new Intent(getBaseContext(),AddUserActivity.class);
            //noinspection unchecked
            ArrayList<User> us = (ArrayList<User>) utenti.clone();
            us.remove(us.size()-1);
            for(int i =0;i<us.size();i++){
                User u = us.get(i);
                if(u.getId() == dbm.getUserId()){
                    us.remove(i);
                    break;
                }
            }
            String usersJson = User.arrayListToJson(us);
            intent.putExtra("users",usersJson);
            intent.putExtra("key",dbm.toString());
            intent.putExtra("user_id",dbm.getUserId());
            intent.putExtra("group_id",group.getId());
            startActivityForResult(intent,ACTIVITY_CODE);
        }
    };

    //controlla se l'utente loggato è l'amministratore del gruppo
    public boolean isAdmin(){
        return group.getUser_id() == dbm.getUserId();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_group);
        fab = (FloatingActionButton) findViewById(R.id.fab);
        if(fab!=null) fab.setVisibility(View.INVISIBLE);
        SharedPreferences pref = getSharedPreferences("showGroup", MODE_PRIVATE);
        //per questioni di scoping
        me = this;
        //preparo la toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_shops);
        setSupportActionBar(toolbar);
        ActionBar ab = getSupportActionBar();
        if(ab!=null){
            ab.setDisplayHomeAsUpEnabled(true);
            ab.setDisplayShowHomeEnabled(true);
            ab.setDisplayShowTitleEnabled(false); //rimuovo il nome dell'activity dall'intestazione
        }
        //preparo il viewPager
        setupViewPager();
        //preparo lo swype to refresh
        swiper = (SwipeRefreshLayout) findViewById(R.id.swiper);
        swiper.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refresh();
            }
        });
        setSwiperColor(swiper);
        //recupero i dati del gruppo e la key per il server
        String key,json;
        int id;
        Intent i = getIntent();
        if(i.hasExtra("key")){
            key = i.getStringExtra("key");
            id = i.getIntExtra("user_id",-1);
            json = i.getStringExtra("group");
            //salvo i dati per i prossimi accessi alla schermata
            SharedPreferences.Editor edit = pref.edit();
            edit.putInt("user_id",id);
            edit.putString("key",key);
            edit.putString("group",json);
            edit.apply();
        }else{
            key = pref.getString("key","");
            id = pref.getInt("user_id",-1);
            json = pref.getString("group","");
        }

        dbm = new DBManager(key,id);
        group = new Group().updateFromJson(json);
        //preparo interfaccia
        TextView nome_gruppo = (TextView)findViewById(R.id.toolbar_title);
        if(nome_gruppo!=null) nome_gruppo.setText(group.getName());
        //preparo immagine
        group_image = (ImageView) findViewById(R.id.toolbar_img);
        ImageManager.setImageToView(this,group_image, ImageManager.GROUP,group.getId());
        //recupero le spese del gruppo
        refresh();

    }

    @Override
    protected void onDestroy() {
        if(changed)setResult(RESULT_OK);
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == ACTIVITY_CODE && resultCode == RESULT_OK){
            changed = true;
            refresh();
        }else if(requestCode == ACTIVITY_CODE+1 && resultCode == RESULT_OK){
            if(data.hasExtra("valuta")){
                group.setEvaluate(data.getStringExtra("valuta"));
                refresh();
                changed = true;
                TextView nome_gruppo = (TextView)findViewById(R.id.toolbar_title);
                if(nome_gruppo!=null) nome_gruppo.setText(group.getName());
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater =  getMenuInflater () ;
        inflater.inflate(R.menu.show_group_menu,menu);
        if(!isAdmin()) menu.findItem(R.id.mod_group).setVisible(false);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.delete_group:
                String text;
                if(isAdmin()) text = getString(R.string.ask_delete_group);
                else text = getString(R.string.ask_exit_group);
                AlertDialog.Builder d = prompt(text);
                d.setPositiveButton(getString(android.R.string.yes), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        deleteGroup();
                    }
                });
                d.setNegativeButton(getString(android.R.string.no), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                d.show();
                return true;
            case R.id.mod_group:
                //avvio intent per modifica del gruppo
                Intent i = new Intent(getBaseContext(),ModGroupActivity.class);
                i.putExtra("group",group.toJson());
                i.putExtra("key",dbm.toString());
                i.putExtra("user_id",dbm.getUserId());
                startActivityForResult(i,ACTIVITY_CODE+1);
                return true;
        }
        return false;
    }

    //per impostare il viewPager
    private void setupViewPager() {
        ViewPager viewPager = (ViewPager) findViewById(R.id.viewpager);
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(new CreditDebitFragment(),getString(R.string.tab_pay));
        adapter.addFragment(new ShopsListFragment(),getString(R.string.tab_shops));
        adapter.addFragment(new NotifyListFragment(),getString(R.string.tab_notify));
        adapter.addFragment(new UserListFragment(),getString(R.string.tab_users));
        if(viewPager!=null){
            viewPager.setOffscreenPageLimit(3);
            viewPager.setAdapter(adapter);
        }
        //sistemo il tablayout
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tab);
        if(tabLayout!=null) tabLayout.setupWithViewPager(viewPager);
        //preparo handler degli eventi sul viewpager
        if(viewPager!=null) viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                //cambio listenere del fab a seconda della pagina visualizzata
                switch(position){
                    case 0:     //crediti/debiti
                        fab.setVisibility(View.INVISIBLE);
                        break;
                    case 1:     //aggiungi spesa
                        fab.setVisibility(View.VISIBLE);
                        changeFloatingIcon(android.R.drawable.ic_input_add);
                        fab.setOnClickListener(fab_addShop);
                        break;
                    case 2:     //aggiungi notifica spese
                        fab.setVisibility(View.VISIBLE);
                        changeFloatingIcon(android.R.drawable.ic_input_add);
                        fab.setOnClickListener(fab_addNotify);
                        break;
                    case 3:     //addUser
                        if(isAdmin()){
                            fab.setVisibility(View.VISIBLE);
                            changeFloatingIcon(R.drawable.pencil_icon);
                            fab.setOnClickListener(fab_addUser);
                        }else{
                            fab.setVisibility(View.INVISIBLE);
                        }
                        break;
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                if(state == ViewPager.SCROLL_STATE_DRAGGING){
                    //utente sta cambiando schermata. Blocco il refresh
                    swiper.setEnabled(false);
                }else{
                    //fine cambio schermata. Riabilito refresh
                    swiper.setEnabled(true);
                }
            }
        });
    }

    //aggiorna i dati locali sul gruppo
    private void refresh(){
        swiper.setRefreshing(true);
        if(isConnectionAvailable()){
            //aggiorno gruppi
            GetShopsTask task = new GetShopsTask();
            task.execute();
            GetUsersTask tk = new GetUsersTask();
            tk.execute();
            GetCreditDebitTask ttk = new GetCreditDebitTask();
            ttk.execute();
            GetShopsNotifyTask tsk = new GetShopsNotifyTask();
            tsk.execute();
            ImageManager.setImageToView(me,group_image,ImageManager.GROUP,group.getId());
        }else{
            alert(getString(R.string.network_error));
            swiper.setRefreshing(false);
        }
    }

    //controlla se una spesa è modificabile oppure no
    public void isChangeableShop(Shop shop,ActionMode mode){
        if(isConnectionAvailable()){
            IsChangeableShopTask task = new IsChangeableShopTask(shop,mode);
            task.execute();

        }else{
            alert(getString(R.string.network_error));
        }
    }

    //elimina una spesa
    public void deleteShop(int s_id){
        if(isConnectionAvailable()){
            DeleteShopTask task = new DeleteShopTask(s_id);
            task.execute();
        }else{
            alert(getString(R.string.network_error));
        }

    }


    //elimina il gruppo
    public void deleteGroup(){
        if(isConnectionAvailable()) {
            DeleteGroupTask task = new DeleteGroupTask(group.getId());
            task.execute();
        }else{
            alert(getString(R.string.network_error));
        }
    }

    //aggiorna le notifiche di spese da fare
    public void updateNotify(ShopNotify n){
        Intent i = new Intent(getBaseContext(),CreateShopNotifyActivity.class);
        i.putExtra("key",dbm.toString());
        i.putExtra("user_id",dbm.getUserId());
        i.putExtra("g_id",group.getId());
        i.putExtra("desc",n.getDescription());
        i.putExtra("n_id",n.getId());
        startActivityForResult(i,ACTIVITY_CODE);
    }

    //cancella una notifica di spesa
    public void deleteNotify(int n_id){
        DeleteNotificaSpesaTask task = new DeleteNotificaSpesaTask(n_id);
        task.execute();
    }

    //segna come pagato un debito
    @Override
    protected void setAsPaid(CreditDebit debt) {
        SetAsPaidTask task = new SetAsPaidTask(debt.getWith().getId(),group.getId());
        task.execute();
    }

    //aggiorna i dati che ho sul gruppo
    @Override
    protected void refresh_debt() {
        refresh();
    }

    //classe per il processo secondario che gestisce il recupero dei gruppi
    public class GetShopsTask extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Void... params) {
            //recupero i dati
            spese = dbm.getSpese(group.getId());
            if (spese != null){
                //aggiungo una spesa vuota alla fine per motivi di layout
                spese.add(new Shop());
                return true;
            }
            return false;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            if (!success) {
                alert(getString(R.string.error_get_shops));
                System.err.println("alert in onPostExecute()");
            }else{
                TextView noShops = (TextView) findViewById(R.id.no_shops);
                if(noShops!=null)
                        if(spese.size() == 1){
                        //ho solo lo spazio bianco
                        noShops.setVisibility(View.VISIBLE);
                    }else {
                        noShops.setVisibility(View.INVISIBLE);
                    }
                RecyclerView sList  = (RecyclerView) findViewById(R.id.shops_list);
                ShopAdapter adapter = new ShopAdapter(spese,me,group,dbm.getUserId(),getString(R.string.you));
                LinearLayoutManager lm = new LinearLayoutManager(getBaseContext());
                if(sList!=null){
                    sList.setAdapter(adapter);
                    sList.setLayoutManager(lm);
                }
                //in caso di referesh, fermo anche il movimento dell'animazione
                swiper.setRefreshing(false);
            }
        }

        @Override
        protected void onCancelled() {
            alert(getString(R.string.error_get_shops));
        }
    }

    //classe per il processo secondario che gestisce il recupero dei gruppi
    public class GetUsersTask extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Void... params) {
            //recupero i dati
             utenti = dbm.getUtenti(group.getId());
            if(utenti != null){
                //aggiungo un'utente voto alla fine per motivi di layout
                utenti.add(new User());
                return true;
            }else{
                return false;
            }
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            if (!success) {
                alert(getString(R.string.error_get_users));
                System.err.println("alert in onPostExecute()");
            }else{
                RecyclerView uList  = (RecyclerView) findViewById(R.id.user_list);
                UserAdapter adapter = new UserAdapter(me,utenti,group.getUser_id());
                LinearLayoutManager lm = new LinearLayoutManager(getBaseContext());
                if(uList!=null){
                    uList.setAdapter(adapter);
                    uList.setLayoutManager(lm);
                }
                //in caso di referesh, fermo anche il movimento dell'animazione
                swiper.setRefreshing(false);
            }
        }

        @Override
        protected void onCancelled() {
            alert(getString(R.string.error_get_users));
        }
    }

    //classe per il processo secondario che gestisce il recupero dei debiti/crediti
    public class GetCreditDebitTask extends AsyncTask<Void, Void, Boolean>{
        @Override
        protected Boolean doInBackground(Void... params) {
            //recupero i dati
            crediti = dbm.getCreditDebit(group.getId(),null);
            if(crediti != null){
                //aggiungo un'utente vuoto alla fine per motivi di layout
                crediti.add(new CreditDebit());
                return true;
            }
            return false;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            if (!success) {
                alert(getString(R.string.error_get_credit));
                System.err.println("alert in onPostExecute()");
            }else{
                RecyclerView uList  = (RecyclerView) findViewById(R.id.credit_debit_list);
                CreditDebitAdapter adapter = new CreditDebitAdapter(crediti,group.getEvaluate(),me);
                LinearLayoutManager lm = new LinearLayoutManager(getBaseContext());
                if(uList!=null){
                    uList.setAdapter(adapter);
                    uList.setLayoutManager(lm);
                }
                //in caso di referesh, fermo anche il movimento dell'animazione
                swiper.setRefreshing(false);
            }
        }

        @Override
        protected void onCancelled() {
            alert(getString(R.string.error_get_credit));
        }
    }

    //classe per il processo secondario che gestisce il recupero delle spese da fare
    public class GetShopsNotifyTask extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Void... params) {
            //recupero i dati
            notifiche_spese = dbm.getNotify(group.getId());
            if (notifiche_spese != null){
                //aggiungo una spesa vuota alla fine per motivi di layout
                notifiche_spese.add(new ShopNotify());
                return true;
            }
            return false;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            if (!success) {
                alert(getString(R.string.error_get_shops_notify));
                System.err.println("alert in onPostExecute()");
            }else{
                TextView noShops = (TextView) findViewById(R.id.no_notify);
                if(noShops!=null)
                    if(notifiche_spese.size() == 1){
                        //ho solo lo spazio bianco
                        noShops.setVisibility(View.VISIBLE);
                    }else{
                        noShops.setVisibility(View.INVISIBLE);
                    }
                RecyclerView sList  = (RecyclerView) findViewById(R.id.notify_list);
                ShopNotifyAdapter adapter = new ShopNotifyAdapter(notifiche_spese,me,group,dbm.getUserId(),getString(R.string.you));
                LinearLayoutManager lm = new LinearLayoutManager(getBaseContext());
                if(sList!=null){
                    sList.setAdapter(adapter);
                    sList.setLayoutManager(lm);
                }
                //in caso di referesh, fermo anche il movimento dell'animazione
                swiper.setRefreshing(false);
            }
        }

        @Override
        protected void onCancelled() {
            alert(getString(R.string.error_get_shops_notify));
        }
    }

    //classe per il processo secondario che cancella una spesa
    public class DeleteShopTask extends AsyncTask<Void, Void, Integer> {
        private int s_id;

        public DeleteShopTask(int s_id) {
            this.s_id = s_id;
        }

        @Override
        protected Integer doInBackground(Void... params) {
            return dbm.deleteShop(s_id);
        }

        @Override
        protected void onPostExecute(final Integer val) {
            switch (val) {
                case -1:
                    alert(getString(R.string.error_delete_shop));
                    System.err.println("alert in onPostExecute()");
                    break;
                case 0:
                    alert(getString(R.string.error_delete_shop_payed));
                    break;
                case 1:
                    toast(getString(R.string.shop_removed),false);
                    refresh();
                    break;
            }
        }

        @Override
        protected void onCancelled() {
            alert(getString(R.string.error_delete_shop));
        }
    }

    //classe per il processo secondario che cancella un gruppo
    public class DeleteGroupTask extends AsyncTask<Void, Void, Integer> {
        private int g_id;

        public DeleteGroupTask(int g_id) {
            this.g_id = g_id;
        }

        @Override
        protected Integer doInBackground(Void... params) {
            return dbm.deleteGroup(g_id);
        }

        @Override
        protected void onPostExecute(final Integer val) {
            switch (val) {
                case -1:
                    alert(getString(R.string.error_delete_group));
                    System.err.println("alert in onPostExecute()");
                    break;
                case 0:
                    alert(getString(R.string.error_delete_group_notpayed));
                    break;
                case 1:
                    toast(getString(R.string.group_removed),false);
                    setResult(RESULT_OK);
                    finish();
                    break;
            }
        }

        @Override
        protected void onCancelled() {
            alert(getString(R.string.error_delete_group));
        }
    }

    //classe per il processo secondario che controlla se una spesa è modificabile
    public class IsChangeableShopTask extends AsyncTask<Void, Void, Boolean> {
        private Shop shop;
        private ActionMode mode;

        public IsChangeableShopTask(Shop shop, ActionMode mode) {
            this.shop = shop;
            this.mode = mode;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            return dbm.isChangeableShop(shop.getId());
        }

        @Override
        protected void onPostExecute(final Boolean val) {
            if(val) {
                Intent i = new Intent(getBaseContext(),CreateShopActivity.class);
                i.putExtra("key",dbm.toString());
                i.putExtra("user_id",dbm.getUserId());
                i.putExtra("desc",shop.getDescription());
                i.putExtra("value",shop.getValue());
                i.putExtra("id",shop.getId());
                i.putExtra("valuta",group.getEvaluate());
                startActivityForResult(i,ACTIVITY_CODE);
            }else{
                alert(getString(R.string.not_changeable_shop));
            }
            mode.finish();
        }

        @Override
        protected void onCancelled() {
            alert(getString(R.string.error_delete_group));
        }
    }

    //classe per il processo secondario che cancella una notifica di spesa
    public class DeleteNotificaSpesaTask extends AsyncTask<Void, Void, Boolean> {
        private int n_id;

        public DeleteNotificaSpesaTask(int n_id) {
            this.n_id = n_id;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            //tento il login
            return dbm.updateNotify(null,n_id);
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            if (success) {
                toast(getString(R.string.shop_notify_deleted),false);
                refresh();
            } else {
                alert(getString(R.string.error_delete_shop_notify));
            }
        }
    }
}
