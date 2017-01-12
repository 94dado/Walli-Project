package com.walli_app.walli;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.ViewFlipper;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.google.firebase.iid.FirebaseInstanceId;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Currency;
import java.util.Map;

public class MainActivity extends CustomAppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private ViewFlipper flipper;
    private NavigationView menu;
    private SwipeRefreshLayout swiper;
    private ImageView profile_img;
    private ArrayList<Group> gruppi = null;
    private ArrayList<CreditDebit> crediti = null;
    private LineChart line_chart;
    private PieChart pie_chart;
    private Spinner spinner;
    private final int GROUPS = 0;
    private EditText[] profile_info;
    private final int[] profile_info_id = {R.id.profile_name_edit,R.id.profile_surname_edit,R.id.profile_phone_edit,R.id.profile_email_edit,R.id.profile_pass_edit,R.id.profile_pass_confirm_edit};
    private final int n_profile_info = profile_info_id.length;
    private User us;
    private int user_id = -1;
    private final Context context=this;
    private  MainActivity me;
    private TextView from_date;
    private TextView to_date;
    private Bitmap bmp;
    private View header;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        me = this;
        super.onCreate(savedInstanceState);
        currency_pref = getSharedPreferences("currency",MODE_PRIVATE);
        SharedPreferences pref = getSharedPreferences("main",MODE_PRIVATE);
        //inizializzo dbm e user_id
        if (pref.contains("key")){
            String key = pref.getString("key","");
            us = new User().updateFromJson(pref.getString("user",null));
            user_id = us.getId();
            dbm = new DBManager(key,user_id);
        }
        else if(getIntent().hasExtra("user")){
            //login appena eseguito
            String json = getIntent().getStringExtra("user");
            us = new User().updateFromJson(json);
            user_id = us.getId();
            String key = getIntent().getStringExtra("key");
            dbm = new DBManager(key,user_id);
            //memorizzo dati per i prossimi accessi
            SharedPreferences.Editor edit = pref.edit();
            edit.putString("user",json);
            edit.putString("key",key);
            edit.apply();
        }else{
            //non deve MAI succedere. Torno alla schermata di login
            logout();
        }
        //setto layout
        setContentView(R.layout.activity_main);
        //recupero interfaccia
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        icon = android.R.drawable.ic_input_add;
        flipper = (ViewFlipper) findViewById(R.id.flipper);
        menu = (NavigationView) findViewById(R.id.nav_view);
        fab = (FloatingActionButton) findViewById(R.id.fab);
        //setto lo swype to refresh
        swiper = (SwipeRefreshLayout) findViewById(R.id.swiper);
        swiper.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                //aggiorno i gruppi
                updateGroups();
                updateDebit();
                setCharts();
                updateImage();
            }
        });
        setSwiperColor(swiper);
        //check sull'istanza della schermata
        if (savedInstanceState != null) {
            //se non è il primo avvio dell'istanza corrente, ripristino la schermata precedente
            int flipperPosition = savedInstanceState.getInt("TAB_NUMBER");
            flipper.setDisplayedChild(flipperPosition);
            onNavigationItemSelected(menu.getMenu().getItem(flipperPosition));
            menu.getMenu().getItem(flipperPosition).setChecked(true);
        }else{
            //seleziono di default la prima entry del menu
            menu.getMenu().getItem(0).setChecked(true);
            onNavigationItemSelected(menu.getMenu().getItem(0));
        }
        //setto la toolbar
        setSupportActionBar(toolbar);
        //codice per il layout e per il menu laterale
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        if(drawer!= null) drawer.addDrawerListener(toggle);
        toggle.syncState();
        //mostro mail e nome e cognome dell'utente nella barra laterale
        header = menu.getHeaderView(0);
        TextView name = (TextView) header.findViewById(R.id.lateral_profile_name);
        TextView mail = (TextView) header.findViewById(R.id.lateral_profile_email);
        String real_name = us.getName()+" "+us.getSurname();
        name.setText(real_name);
        mail.setText(us.getMail());
        //setto il listener del cambio della voce del menu a questa classe (metodo: onNavigationItemSelected)
        menu.setNavigationItemSelectedListener(this);
        //preparo i campi del profilo utente
        updateProfileForm();
        //aggiorno label della scelta della currency di default
        TextView currency =(TextView) findViewById(R.id.label_valuta);
        if(currency!=null) currency.setText(getString(R.string.set_default_currency));
        //rimuovo padding dalla list_view dei debiti totali
        RelativeLayout credit = (RelativeLayout) findViewById(R.id.layout_credit_debit);
        if(credit!=null) credit.setPadding(0,0,0,0);
        //setto lo spinner per la scelta della currency preferita
        spinner = (Spinner) findViewById(R.id.spinner);
        setSpinner();
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                String currency = spinner.getSelectedItem().toString();
                currency = currency.split(" ")[0];
                CurrencyManager.setUserCurrencyDefault(currency_pref,currency);
                updateDebit();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parentView) {

            }

        });
        //setto handler immagine profilo
        profile_img = (ImageView) findViewById(R.id.profile_image);
        ImageManager.setChangeImageListener(this,profile_img);
        //aggiorno immagine laterale del profilo
        updateImage();
    }

    private void updateImage() {
        ImageView i = (ImageView) header.findViewById(R.id.lateral_profile_image);
        ImageManager.setImageToView(me,i,ImageManager.USER,dbm.getUserId());
        ImageManager.setImageToView(me,profile_img,ImageManager.USER,dbm.getUserId());
    }

    @Override
    //metodo per la gestione del pulsante back dell'app
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if(drawer!=null){
            if (drawer.isDrawerOpen(GravityCompat.START)) {
                drawer.closeDrawer(GravityCompat.START);
            } else {
                if (flipper.getDisplayedChild()!=GROUPS){
                    menu.getMenu().getItem(GROUPS).setChecked(true);
                    onNavigationItemSelected(menu.getMenu().getItem(GROUPS));
                }else{
                    super.onBackPressed();
                }
            }
        }
    }

    @Override
    protected void onStop() {
        //salvo l'utente loggato
        SharedPreferences pref = getSharedPreferences("main",MODE_PRIVATE);
        SharedPreferences.Editor edit = pref.edit();
        edit.putString("user",us.toJson());
        edit.apply();
        super.onStop();
    }

    //serve ad aggiungere informazioni al bundle tra un refresh della schermata e l'altra senza chiudere l'app
    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        int position = flipper.getDisplayedChild();
        savedInstanceState.putInt("TAB_NUMBER", position);
    }

    @Override
    //dove gestire il click sulle icone del menu laterale
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        switch (id){
            case R.id.nav_groups:
                //elenco gruppi
                flipper.setDisplayedChild(GROUPS);
                fab.setVisibility(View.VISIBLE);
                swiper.setEnabled(true);
                changeFloatingIcon(android.R.drawable.ic_input_add);
                fab_addGroup();
                //popolo gruppi, se necessario
                if(gruppi == null){
                    updateGroups();
                }
                break;
            case R.id.nav_profile:
                //gestione profilo
                int PROFILE = 1;
                flipper.setDisplayedChild(PROFILE);
                fab.setVisibility(View.VISIBLE);
                swiper.setEnabled(false);
                changeFloatingIcon(R.drawable.pencil_icon);
                profile_img.setClickable(false);
                fab_modProfile();
                break;
            case R.id.nav_charts:
                //grafici
                int CHARTS = 2;
                flipper.setDisplayedChild(CHARTS);
                swiper.setEnabled(false);
                fab.setVisibility(View.INVISIBLE);
                if(pie_chart==null && line_chart==null){
                    setCharts();
                }
                break;
            case R.id.nav_debit:
                //debiti
                int DEBT = 3;
                flipper.setDisplayedChild(DEBT);
                swiper.setEnabled(true);
                fab.setVisibility(View.INVISIBLE);
                if(crediti == null){
                    updateDebit();
                }
                break;
            case R.id.nav_settings:
                //impostazioni
                int SETTINGS = 4;
                flipper.setDisplayedChild(SETTINGS);
                swiper.setEnabled(false);
                fab.setVisibility(View.INVISIBLE);
                break;
            case R.id.nav_logout:
                //devo eseguire il logout
                logout();
                break;
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if(drawer!=null) drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    //funzione per aggiornare elenco gruppi
    private void updateGroups(){
        swiper.setRefreshing(true);
        if(isConnectionAvailable()){
            GetGroupsTask task = new GetGroupsTask();
            task.execute();
        }else{
            alert(getString(R.string.network_error));
            swiper.setRefreshing(false);
        }
    }

    private void updateProfileImage(){
        ImageManager.setNewImageToView(me,(ImageView) findViewById(R.id.lateral_profile_image),bmp,ImageManager.USER,user_id,false);
    }

    //funzione per aggiornare crediti debiti rispetto a chiunque
    private void updateDebit(){
        swiper.setRefreshing(true);
        if(isConnectionAvailable()){
            GetCreditDebitTask task = new GetCreditDebitTask();
            task.execute();
        }else{
            alert(getString(R.string.network_error));
            swiper.setRefreshing(false);
        }
    }

    //aggiorna, se necessario, informazioni del profilo dopo modifica da parte dell'utente
    private void updateProfileInfo(){
        if(FormValidator.profileDataChanged(profile_info,us)){
            String[] values = new String[profile_info.length];
            for(int i=0;i<profile_info.length;i++){
                values[i] = profile_info[i].getText().toString();
            }
            boolean checkPwd = !(values[4].equals("") && values[5].equals(""));
            String error = FormValidator.changeUserDataValidator(me,values[0],values[1],values[2],values[3],values[4],values[5],checkPwd);
            if(error.equals("")){
                //tutto ok. Posso aggiornare i dati se ho connessione
                if(isConnectionAvailable()){
                    UpdateProfileTask task = new UpdateProfileTask(values,checkPwd);
                    task.execute();
                }else{
                    alert(getString(R.string.network_error));
                }

            }else{
                alert(error);
            }
        }
    }

    //mette nella form del profilo i dati dell'utente
    private void updateProfileForm(){
        TextView nick= (TextView) findViewById(R.id.profile_nickname);
        if(nick!=null) nick.setText(us.getNick());
        EditText edit = (EditText) findViewById(profile_info_id[0]);
        if(edit!=null) edit.setText(us.getName());
        edit = (EditText) findViewById(profile_info_id[1]);
        if(edit!=null) edit.setText(us.getSurname());
        edit = (EditText) findViewById(profile_info_id[2]);
        if(edit!=null) edit.setText(us.getPhone());
        edit = (EditText) findViewById(profile_info_id[3]);
        if(edit!=null) edit.setText(us.getMail());
    }



    //handler del fab per la schermata dei gruppi
    private void fab_addGroup(){
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //avvio activity per generazione del gruppo
                Intent intent = new Intent(getBaseContext(),CreateGroupActivity.class);
                intent.putExtra("user",us.toJson());
                intent.putExtra("key",dbm.toString());
                intent.putExtra("user_id",dbm.getUserId());
                startActivityForResult(intent,ACTIVITY_CODE);
            }

        });
    }

    //handler del fab per la schermata del profilo
    private void fab_modProfile(){
        profile_info = new EditText[n_profile_info];
        //imposto correttamente gli edit text del profilo
        for (int i =0;i<n_profile_info;i++){
            profile_info[i]= (EditText) findViewById(profile_info_id[i]);
            profile_info[i].setEnabled(false);
            profile_info[i].setClickable(false);
        }
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(icon == R.drawable.pencil_icon){
                    //entro in modifica
                    changeFloatingIcon(R.drawable.save_icon);
                }else{
                    //esco dalla modifica, quindi cerco di salvare
                    changeFloatingIcon(R.drawable.pencil_icon);
                    updateProfileInfo();
                    updateProfileImage();
                }
                for (int i =0;i<n_profile_info;i++){
                    boolean val = profile_info[i].isEnabled();
                    profile_info[i].setEnabled(!val);
                    profile_info[i].setClickable(!val);
                }
                profile_img.setClickable(!profile_img.isClickable());
            }
        });
    }

    //funzione per lo show del grafo.
    private void setCharts(){
        from_date = (TextView) findViewById(R.id.from_date);
        to_date = (TextView) findViewById(R.id.to_date);
        Calendar from = Calendar.getInstance();
        Calendar to = (Calendar) from.clone();
        from.add(Calendar.DATE,-7);
        from_date.setOnClickListener(new DateOnClickListener(from_date, from,me));
        to_date.setOnClickListener(new DateOnClickListener(to_date, to,me));
        if(isConnectionAvailable()){
            refresh_charts(true);
        }else{
            alert(getString(R.string.network_error));
            swiper.setRefreshing(false);
        }
    }

    private void logout(){
        //logout dal server per chiudere la sessione
        logoutTask task = new logoutTask();
        task.execute();
        //elimino le sharedPreferencies
        SharedPreferences pref = getSharedPreferences("main",MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.clear();
        editor.apply();
        Intent intent = new Intent(getBaseContext(),LoginActivity.class);
        intent.putExtra("delete_user","1");
        startActivity(intent);
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == ACTIVITY_CODE && resultCode == RESULT_OK){
            updateGroups();
            updateDebit();
            //return dall'intent dell'immagine
        }else if(requestCode == PIC_CODE && resultCode == RESULT_OK){
            //mi è tornata l'immagine!!! la taglio
            Intent intent = new Intent("com.android.camera.action.CROP");
            Uri uri = data.getData();
            intent.setDataAndType(uri, "image/*");
            intent.putExtra("crop", "true");
            intent.putExtra("aspectX", 1);
            intent.putExtra("aspectY", 1);
            intent.putExtra("outputX", 500);
            intent.putExtra("outputY", 500);
            intent.putExtra("return-data", true);
            startActivityForResult(intent,PIC_CODE+1);
        }else if(requestCode == PIC_CODE+1 && resultCode == RESULT_OK){
            Uri uri = data.getData();
            if (uri == null) {
                bmp = (Bitmap) data.getExtras().get("data");
            }else{
                try{
                    bmp = MediaStore.Images.Media.getBitmap(this.getContentResolver(),uri);
                }catch(Exception e){
                    System.err.println("immagine non esiste. fottiti");
                    finish();
                }
            }
            ImageManager.setTempImageToView(profile_img,bmp);
        }
    }

    @Override
    protected void setAsPaid(CreditDebit debt) {
        SetAsPaidTask task = new SetAsPaidTask(debt.getWith().getId(),-1);
        task.execute();
    }

    @Override
    protected void refresh_debt() {
        GetCreditDebitTask task = new GetCreditDebitTask();
        task.execute();
        GetGroupsTask task1 = new GetGroupsTask();
        task1.execute();
    }

    public void refresh_charts(boolean both){
        String d1 = from_date.getText().toString();
        String d2 = to_date.getText().toString();
        GetChartsDataTask task = new GetChartsDataTask(both,d1,d2);
        task.execute();
    }

    //classe per il processo secondario che gestisce il recupero dei gruppi
    public class GetGroupsTask extends AsyncTask<Void, Void, Boolean> {
        @Override
        protected Boolean doInBackground(Void... params) {
            //recupero i dati
            gruppi = dbm.getGruppi();
            if(gruppi != null){
                //aggiungo un gruppo vuoto alla fine per mostrare uno spazio vuoto per inserire uno spazio bianco in fondo
                gruppi.add(new Group());
                return true;
            }
            return false;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            if (!success) {
                alert(getString(R.string.error_get_groups));
            }else{
                TextView noGroups = (TextView) findViewById(R.id.no_groups);
                if(gruppi.size() == 1){
                    //non ci sono gruppi ma solo lo spazio bianco
                    if(noGroups!= null) noGroups.setVisibility(View.VISIBLE);
                }else {
                    if(noGroups!=null) noGroups.setVisibility(View.INVISIBLE);
                }
                RecyclerView gList  = (RecyclerView) findViewById(R.id.groups_list);
                GroupAdapter adapter = new GroupAdapter(me,gruppi,dbm.toString(),dbm.getUserId());
                LinearLayoutManager lm = new LinearLayoutManager(context);
                if(gList!=null){
                    gList.setAdapter(adapter);
                    gList.setLayoutManager(lm);
                }
                //in caso di referesh, fermo anche il movimento dell'animazione
                swiper.setRefreshing(false);
            }
        }

        @Override
        protected void onCancelled() {
            alert(getString(R.string.error_get_groups));
        }
    }

    //classe per il processo secondario che esegue il logout
    public class logoutTask extends AsyncTask<Void,Void,Void>{

        @Override
        protected Void doInBackground(Void... params) {
            dbm.logout(user_id,FirebaseInstanceId.getInstance().getToken());
            return null;
        }
    }

    //classe per il processo secondario che aggiorna i dati dell'utente
    public class UpdateProfileTask extends AsyncTask<Void,Void,Boolean>{
        String [] values;
        boolean checkPwd;

        UpdateProfileTask(String [] values, boolean checkPwd) {
            this.values = values;
            this.checkPwd = checkPwd;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            //recupero i dati
            return dbm.updateProfileData(us,values,checkPwd);
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            if (!success) {
                alert(getString(R.string.error_update_user_profile));
            }else{
                toast(getString(R.string.user_update_succeded),false);
            }
        }

        @Override
        protected void onCancelled() {
            alert(getString(R.string.error_update_user_profile));
        }
    }

    //classe per il processo secondario che gestisce il recupero dei debiti/crediti
    public class GetCreditDebitTask extends AsyncTask<Void, Void, Boolean>{
        private final String currency = CurrencyManager.getUserCurrencyCodeDefault(currency_pref);
        @Override
        protected Boolean doInBackground(Void... params) {
            //recupero i dati
            int NO_GROUP = -1;
            crediti = dbm.getCreditDebit(NO_GROUP,currency);
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
                CreditDebitAdapter adapter = new CreditDebitAdapter(crediti, currency, me);
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

    //classe per il processo secondario che gestisce il recupero dei dati per i grafici
    public class GetChartsDataTask extends AsyncTask<Void, Void, Boolean>{

        private final String currency = CurrencyManager.getUserCurrencyCodeDefault(currency_pref);
        private ArrayList<ChartPoint> line_data;
        private Map<String,Float>  pie_data;
        private String d1;
        private String d2;
        private boolean both;

        public GetChartsDataTask(boolean both, String d1, String d2) {
            this.both = both;
            this.d1 = d1;
            this.d2 = d2;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            //recupero dati
            line_data = dbm.getLineChartData(currency,d1,d2);
            if(line_data == null) return false;
            if(both){
                pie_data = dbm.getPieChartData(currency,d1,d2);
                return pie_data != null;
            }else{
                return true;
            }
        }

        @SuppressWarnings("deprecation")
        @Override
        protected void onPostExecute(final Boolean success) {
            if (!success) {
                alert(getString(R.string.error_get_charts));
                System.err.println("alert in onPostExecute()");
            }else{
                //setto linechart
                boolean refresh = line_chart != null;
                line_chart = (LineChart) findViewById(R.id.line_chart);
                ArrayList <Entry> data = new ArrayList<>();
                ArrayList<String> labels = new ArrayList<>();
                for(int i = 0; i < line_data.size();i++){
                    ChartPoint p = line_data.get(i);
                    data.add(new Entry(p.getValue(),i));
                    labels.add(p.getLabel());
                }
                LineDataSet line_dataset = new LineDataSet(data,getString(R.string.in_out));
                line_dataset.setColor(getResources().getColor(R.color.colorAccent));
                line_dataset.setValueTextSize(12);
                LineData graph = new LineData(labels,line_dataset);
                line_chart.setData(graph);
                if(refresh){
                    //ho aggiornato i dati del grafo. Se non lo clicco non li mostra, quindi genero un click
                    long downTime = SystemClock.uptimeMillis();
                    long eventTime = SystemClock.uptimeMillis() + 100;
                    float x = 0.0f;
                    float y = 0.0f;
                    int metaState = 0;
                    MotionEvent motionEvent = MotionEvent.obtain(downTime, eventTime, MotionEvent.ACTION_UP, x, y, metaState);
                    line_chart.dispatchTouchEvent(motionEvent);
                    motionEvent.recycle();
                }else{
                    //prima visualizzazione
                    line_chart.setDescriptionTextSize(12);
                    line_chart.getAxisLeft().setGridLineWidth(0f);
                    line_chart.getXAxis().setGridLineWidth(0f);
                    line_chart.setDescription(getString(R.string.finances_description));
                    line_chart.animateXY(1500,1500);
                }
                //setto piechart
                if(both){
                    pie_chart = (PieChart) findViewById(R.id.pie_chart);
                    pie_chart.setDescription(getString(R.string.piechart_name));
                    pie_chart.setRotationEnabled(true);
                    pie_chart.setDrawCenterText(true);
                    pie_chart.setDrawHoleEnabled(false);
                    data = new ArrayList<>();
                    data.add(new Entry(pie_data.get("credit"),0));
                    data.add(new Entry(pie_data.get("debt"),1));
                    ArrayList<Integer> colors = new ArrayList<>();
                    PieDataSet pie_dataset = new PieDataSet(data, Currency.getInstance(currency).getSymbol());
                    colors.add(getResources().getColor(R.color.chartGreen));
                    colors.add(getResources().getColor(R.color.chartRed));
                    pie_dataset.setColors(colors);
                    pie_dataset.setValueTextColor(getResources().getColor(R.color.whiteTextColor));
                    pie_dataset.setValueTextSize(12);
                    pie_dataset.setSliceSpace(3);
                    pie_dataset.setSelectionShift(7);
                    labels = new ArrayList<>();
                    labels.add(getString(R.string.credit));
                    labels.add(getString(R.string.debt));
                    PieData piedata = new PieData(labels,pie_dataset);
                    pie_chart.setData(piedata);
                    pie_chart.animateXY(1500,1500);
                }
            }
        }

        @Override
        protected void onCancelled() {
            alert(getString(R.string.error_get_charts));
        }
    }
}
