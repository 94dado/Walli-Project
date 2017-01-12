package com.walli_app.walli;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;

public class ModGroupActivity extends CustomAppCompatActivity {
    private Group gruppo;
    private boolean created = false;
    private ImageView img;
    private Bitmap bmp;
    private CustomAppCompatActivity me;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        me = this;
        currency_pref = getSharedPreferences("currency",MODE_PRIVATE);
        setContentView(R.layout.activity_create_group);
        Toolbar t = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(t);
        ActionBar ab = getSupportActionBar();
        if(ab!=null){
            ab.setDisplayHomeAsUpEnabled(true);
            ab.setDisplayShowHomeEnabled(true);
        }
        Intent i = getIntent();
        int user_id = i.getIntExtra("user_id",-1);
        dbm = new DBManager(i.getStringExtra("key"),user_id);
        gruppo = new Group().updateFromJson(i.getStringExtra("group"));
        //preimposto i campi
        EditText group_edit = (EditText) findViewById(R.id.new_group_name);
        if(group_edit!=null) group_edit.setText(gruppo.getName());
        //setto lo spinner
        spinner = (Spinner) findViewById(R.id.spinner);
        setSpinner();
        //setto immagine
        img = (ImageView) findViewById(R.id.group_image);
        ImageManager.setImageToView(this,img,ImageManager.GROUP,gruppo.getId());
        ImageManager.setChangeImageListener(this,img);
    }

    @Override
    protected void onDestroy() {
        if (!created) {
            setResult(RESULT_CANCELED);
        }
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu ( Menu menu )  {
        MenuInflater inflater =  getMenuInflater () ;
        inflater.inflate(R.menu.next_menu,menu);
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == PIC_CODE && resultCode == RESULT_OK){
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
            ImageManager.setTempImageToView(img,bmp);
        }
    }

    //selezione di una voce dal menu contestuale
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.next:
                EditText group =(EditText) findViewById(R.id.new_group_name);
                String group_name;
                if(group!=null)group_name = group.getText().toString();
                else group_name = "";
                if(group_name.equals("")){
                    alert(getString(R.string.error_new_group_name));
                }else{
                    String currency = spinner.getSelectedItem().toString();
                    String [] parts = currency.split(" ");
                    currency = parts[0];
                    if(isConnectionAvailable()){
                        UpdateGroupTask task = new UpdateGroupTask(group_name,currency);
                        task.execute();
                    }else{
                        alert(getString(R.string.network_error));
                        return true;
                    }
                }
        }
        return false;
    }

    //classe per processo secondario che aggiorna un gruppo
    public class UpdateGroupTask extends AsyncTask<Void, Void, Boolean> {
        private String name;
        private String valuta;
        private int id;

        public UpdateGroupTask(String name, String valuta) {
            this.name = name;
            this.valuta = valuta;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            id = dbm.updateGroup(null,gruppo.getId(),name,valuta);
            return id != -1;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            if (!success) {
                alert(getString(R.string.error_update_group));
                System.err.println("alert in onPostExecute()");
            }else{
                Intent i = new Intent();
                i.putExtra("valuta",valuta);
                setResult(RESULT_OK,i);
                String message = getString(R.string.group_updated);
                toast(message,false);
                created = true;
                if(bmp != null) ImageManager.setNewImageToView(me,null,bmp,ImageManager.GROUP,id,true);
            }
        }

        @Override
        protected void onCancelled() {
            alert(getString(R.string.error_update_group));
        }
    }

}
