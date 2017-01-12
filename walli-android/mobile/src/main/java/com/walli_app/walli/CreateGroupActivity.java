package com.walli_app.walli;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
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

public class CreateGroupActivity extends CustomAppCompatActivity {
    private String user;
    private boolean created;
    private ImageView img;
    private Bitmap bmp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        currency_pref = getSharedPreferences("currency",MODE_PRIVATE);
        setContentView(R.layout.activity_create_group);
        Toolbar t = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(t);
        ActionBar ab = getSupportActionBar();
        if(ab!= null){
            ab.setDisplayHomeAsUpEnabled(true);
            ab.setDisplayShowHomeEnabled(true);
        }
        Intent i = getIntent();
        user = i.getStringExtra("user");
        //setto lo spinner
        spinner = (Spinner) findViewById(R.id.spinner);
        setSpinner();
        img = (ImageView) findViewById(R.id.group_image);
        ImageManager.setChangeImageListener(this,img);

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
            case R.id.next:
                EditText group =(EditText) findViewById(R.id.new_group_name);
                String group_name;
                if(group!=null)
                group_name = group.getText().toString();
                else group_name = "";
                if(group_name.equals("")){
                    alert(getString(R.string.error_new_group_name));
                }else{
                    String currency = spinner.getSelectedItem().toString();
                    String [] parts = currency.split(" ");
                    currency = parts[0];
                    Intent i = new Intent(getBaseContext(),AddUserActivity.class);
                    i.putExtra("user",user);
                    i.putExtra("group_name",group_name);
                    Intent caller = getIntent();
                    i.putExtra("key",caller.getStringExtra("key"));
                    i.putExtra("user_id",caller.getIntExtra("user_id",-1));
                    i.putExtra("valuta",currency);
                    if(bmp != null){
                        putBitmapIntoIntent(i,bmp);
                    }
                    startActivityForResult(i,ACTIVITY_CODE);
                }
        }
        return false;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == ACTIVITY_CODE && resultCode == RESULT_OK){
            created = true;
            Intent ret = new Intent();
            setResult(RESULT_OK,ret);
            finish();
        }if(requestCode == PIC_CODE && resultCode == RESULT_OK){
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
            //mi è tornata l'immagine tagliata. La recupero
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
            //la salvo in locale
            ImageManager.setTempImageToView(img,bmp);
        }
    }

    @Override
    protected void onDestroy() {
        //se viene distrutta l'activity e non si è generato nulla
        if(!created){
            Intent ret = new Intent();
            setResult(RESULT_CANCELED,ret);
        }
        super.onDestroy();
    }
}
