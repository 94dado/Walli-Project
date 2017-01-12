package com.walli_app.walli;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.ActionMode;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;

/**
 * Created by dado on 16/06/16.
 */
public class CustomAppCompatActivity extends AppCompatActivity {

    //codici per intent
    protected static int ACTIVITY_CODE = 10;
    public static int PIC_CODE =1;

    protected DBManager dbm;
    protected FloatingActionButton fab;
    protected int icon;

    public DBManager getDbm(){
        return dbm;
    }

    protected SharedPreferences currency_pref;

    public ActionMode notifyActionMode;
    public ActionMode shopActionMode;
    public ActionMode debitActionMode;

    protected ArrayList<String> currencies;
    protected ArrayAdapter<String> adapter;
    protected Spinner spinner;

    //cambia icona del pulsante in basso in base alle API disponibili
    protected void changeFloatingIcon(int id){
        icon = id;
        if(Build.VERSION.SDK_INT <21){
            switch(id) {
                case R.drawable.pencil_icon:
                    fab.setImageResource(R.mipmap.ic_action_pencil);
                    break;
                case R.drawable.save_icon:
                    fab.setImageResource(R.mipmap.ic_action_content_save);
                    break;
                default:
                    fab.setImageDrawable(ContextCompat.getDrawable(this,id));
            }
        }else fab.setImageDrawable(ContextCompat.getDrawable(this,id));
    }

    //imposta le possibili scelte di valute nello spinner
    public void setSpinner(){
        spinner = (Spinner) findViewById(R.id.spinner);
        currencies = CurrencyManager.getCurrencies(currency_pref);
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, currencies);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
    }

    //funzione per mostrare un dialog di errore
    protected void alert(String message){
        String title = getString(R.string.error);
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        //esecuzione del pulsante ok
                        dialog.cancel();
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    //funzione per mostrare un dialog di domanda
    protected AlertDialog.Builder prompt(String message){
        String title = getString(R.string.confirm);
        AlertDialog.Builder d =new AlertDialog.Builder(this);
        d.setTitle(title);
        d.setMessage(message);
        return d;
    }

    //funzione per controllare se il dispositivo è connesso
    protected boolean isConnectionAvailable(){
        ConnectivityManager conn = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo network = conn.getActiveNetworkInfo();
        return network != null && network.isConnected();
    }

    //imposto i colori dello "swype to refresh"
    protected void setSwiperColor(SwipeRefreshLayout swiper){
        swiper.setColorSchemeResources(
                android.R.color.holo_blue_dark,
                android.R.color.holo_green_dark,
                android.R.color.holo_red_light,
                android.R.color.holo_orange_light
        );
        //imposto anche distanza di swiping per migliorare la user-experience
        swiper.setDistanceToTriggerSync(500);
    }

    //stampa un toast
    protected void toast(String message, boolean longtime){
        int length = longtime?Toast.LENGTH_LONG : Toast.LENGTH_SHORT;
        Toast.makeText(this, message, length).show();
    }

    //ShowGroupsShopsActivity e MainActivity faranno override di tali metodi.
    protected void setAsPaid(CreditDebit debt){}
    protected void refresh_debt(){}

    //salva un'immagine come parametro di un intent
    protected void putBitmapIntoIntent(Intent i, Bitmap bmp){
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.PNG, 100, stream);
        byte[] byteArray = stream.toByteArray();
        i.putExtra("img",byteArray);
    }

    //recupera un immagine dall'intent
    protected Bitmap getBitmapFromIntent(){
        byte[] byteArray = getIntent().getByteArrayExtra("img");
        if(byteArray != null)
            return BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
        else return null;
    }

    //metodo per mandare una notifica per ricordare di pagare un debito
    public void sendPaymentNotification(CreditDebit debt) {
        SendPaymentNotificationTask task = new SendPaymentNotificationTask(debt.getWith().getId());
        task.execute();
    }

    //classe per il processo secondario che segna come pagato un debito
    public class SetAsPaidTask extends AsyncTask<Void, Void, Boolean> {
        private int u_id;
        private int g_id;

        public SetAsPaidTask(int u_id, int g_id) {
            this.u_id = u_id;
            this.g_id = g_id;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            //tento il login
            return dbm.setAsPaid(u_id,g_id);
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            if (success) {
                toast(getString(R.string.debt_paid),false);
                refresh_debt();
            } else {
                alert(getString(R.string.error_pay_debt));
            }
        }


    }

    //funzione che controlla se i permessi di scrittura sono disponibili
    public boolean checkPermissions(){
        boolean permissionCheck = ContextCompat.checkSelfPermission(this,Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        //se non ho i permessi, li richiedo
        if(!permissionCheck){
            String permissions[] = {Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.READ_EXTERNAL_STORAGE};
            ActivityCompat.requestPermissions(this,permissions,ACTIVITY_CODE);
        }
        return permissionCheck;
    }

    //codice che gestisce l'ottenimento dei permessi.
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    //classe per il processo secondario che manda una notifica per richiedere il pagamento
    public class SendPaymentNotificationTask extends AsyncTask<Void, Void, Boolean> {
        private int user_id;

        public SendPaymentNotificationTask(int user_id) {
            this.user_id = user_id;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            return dbm.sendPaymentNotification(user_id);
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            if (success) {
                toast(getString(R.string.push_notification_sent),false);
            } else {
                alert(getString(R.string.error_push_notfication));
            }
        }
    }
}
