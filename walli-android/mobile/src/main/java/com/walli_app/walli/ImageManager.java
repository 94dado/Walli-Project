package com.walli_app.walli;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Base64;
import android.view.View;
import android.widget.ImageView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by dado on 07/07/2016.
 */

public class ImageManager {
    public static final int GROUP = 0;
    public static final int USER = 1;
    private static final String EXTENSION = ".png";
    private static final String cache = "/Walli/cache.json";


    //metodo che restituisce il timestamp dell'immagine della cache
    private static String getTimeStamp(int type, int id){
        try {
            String filepath = Environment.getExternalStorageDirectory().getPath() + cache;
            File f = new File(filepath);
            JSONArray dict;
            if(!f.exists()){
                return null;    //non ho il dizionario, che posso fare?
            }else{
                //leggo dizionario da filesystem
                InputStream is = new FileInputStream(f);
                int size = is.available();
                byte[] buffer = new byte[size];
                //noinspection ResultOfMethodCallIgnored
                is.read(buffer);
                is.close();
                dict = new JSONArray(new String(buffer, "UTF-8"));
            }
            return dict.getJSONArray(type).getString(id);
        }catch (Exception e){
            System.err.println(e.toString());
            return null;
        }
    }

    //salva il timestamp di un immagine appena scaricata dal server
    private static boolean saveTimeStamp(int type,int id, String timestamp){
        try{
            String filepath = Environment.getExternalStorageDirectory().getPath() + cache;
            File f = new File(filepath);
            JSONArray dict;
            if(!f.exists()){
                //creo il nuovo dizionario
                if(!f.createNewFile()) return false;
                dict = new JSONArray();
                dict.put(GROUP,new JSONArray());
                dict.put(USER,new JSONArray());
            }else{
                //leggo dizionario da filesystem
                InputStream is = new FileInputStream(f);
                int size = is.available();
                byte[] buffer = new byte[size];
                //noinspection ResultOfMethodCallIgnored
                is.read(buffer);
                is.close();
                dict = new JSONArray(new String(buffer, "UTF-8"));
            }
            dict.getJSONArray(type).put(id,timestamp);
            //aggiorno filesystem
            FileOutputStream stream = new FileOutputStream(f);
            stream.write(dict.toString().getBytes());
            stream.close();
            return true;
        }catch(Exception e){
            System.err.println(e.toString());
            return false;
        }
    }

    //metodi del client

    //metodo che restituisce il path dell'immagine in cache (o che avrebbe se ci fosse)
    private static String okToGo(CustomAppCompatActivity me, int type){
        try {
            if (!me.checkPermissions()) {
                return null; //non ho i permessi, ma sono stati gia' richiesti dal metodo stesso
            }
            String filepath = Environment.getExternalStorageDirectory().getPath() + "/Walli";
            switch (type) {
                case GROUP:
                    filepath += "/group";
                    break;
                case USER:
                    filepath += "/user";
                    break;
                default:
                    filepath = "";      //voglio generare un errore
            }
            return filepath;
        }catch (Exception e){
            System.err.println(e.toString());
            return null;
        }
    }

    //metodo per salvare l'immagine in lcoale
    private static boolean saveImage(CustomAppCompatActivity me,int type, int id_img, Bitmap img){
        try{
            String filepath = okToGo(me,type);
            File path;
            boolean ok;
            if(filepath != null){
                path = new File(filepath);
                //noinspection ResultOfMethodCallIgnored
                path.mkdirs();
                //cartelle generata
                File img_file = new File(path.getPath()+"/"+id_img+EXTENSION);
                if(!img_file.exists()){
                    ok = img_file.createNewFile();
                    if(!ok) return false;
                }
                OutputStream fw = new FileOutputStream(img_file);
                //img = getResizedBitmap(img,500,500);
                img.compress(Bitmap.CompressFormat.PNG,100,fw);
                fw.close();
                return true;
            }else return false;
        }catch (Exception e){
            System.err.println(e.toString());
            return false;
        }
    }

    //funzione che ridimensiona un bitmap
    //http://stackoverflow.com/questions/4837715/how-to-resize-a-bitmap-in-android
    public static Bitmap getResizedBitmap(Bitmap bm, int newWidth, int newHeight) {
        int width = bm.getWidth();
        int height = bm.getHeight();
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        float ratio = Math.min(scaleHeight,scaleWidth);
        // CREATE A MATRIX FOR THE MANIPULATION
        Matrix matrix = new Matrix();
        // RESIZE THE BIT MAP
        matrix.postScale(ratio,ratio);
        // "RECREATE" THE NEW BITMAP
        return Bitmap.createBitmap(bm, 0, 0, width, height, matrix, false);
    }

    //funzione che recupera un immagine e la imposta alla view
    private static void setImage(CustomAppCompatActivity me, ImageView view, int type, int img_id){
        try{
            String filepath = okToGo(me,type);
            if(filepath != null){
                filepath += "/"+img_id+EXTENSION;
                Bitmap img;
                if(new File(filepath).exists()){
                    img = BitmapFactory.decodeFile(filepath);
                    //prossimo if controlla che non si sia danneggiato il file
                    if(img!=null){
                        String ts = getTimeStamp(type,img_id);
                        cacheImage(type,img_id,me,view,img,ts);
                    }
                    else getImageFromServer(me, view, type, img_id);
                }else{
                    getImageFromServer(me,view,type,img_id);
                }
            }
        }catch (Exception e){
            System.err.println(e.toString());
        }
    }

    //funzione che salva in locale e remoto la nuova immagine
    private static void setNewImage(CustomAppCompatActivity me, ImageView view, Bitmap bmp, int type, int id){
        try{
            saveImage(me,type,id,bmp);
            saveImageToServer(bmp,me.dbm,type,id);
            view.setImageBitmap(bmp);
        }catch(Exception e){
            System.err.println(e.toString());
        }

    }


    //chiamate pubbliche

    //imposta un'immagine alla view
    public static void setImageToView(CustomAppCompatActivity me, ImageView view,int type, int img_id){
        setImage(me,view,type,img_id);
    }

    //imposta listenere per recuperare una nuova immagine dal server
    public static void setChangeImageListener(final CustomAppCompatActivity me,ImageView view){
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent getIntent = new Intent(Intent.ACTION_GET_CONTENT);
                getIntent.setType("image/*");
                Intent pickIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                pickIntent.setType("image/*");
                Intent chooserIntent = Intent.createChooser(getIntent, "Select Image");
                chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[] {pickIntent});
                me.startActivityForResult(chooserIntent, CustomAppCompatActivity.PIC_CODE);
            }
        });
    }

    //imposta una nuova immagine appena scelta e la salva
    public static void setNewImageToView(CustomAppCompatActivity me,ImageView view, Bitmap bmp, int type, int id,boolean finish){
        setNewImage(me,view,bmp,type,id);
        //se dopo questa operazione l'activity va chiusa, la chiudo
        if(finish) me.finish();
    }

    //imposta temporaneamente un immagine (non salva ne sul server ne in cache)
    public static void setTempImageToView(ImageView view, Bitmap img){
        try{
            img = getResizedBitmap(img,500,500);
            view.setImageBitmap(img);
        }catch (Exception e){
            System.err.println(e.toString());
        }
    }

    //recupera un immagine dalla cache, ignorando il timestamp
    public static Bitmap getImageOnlyFromCache(String path){
        try{
            path+=EXTENSION;
                Bitmap img;
                if(new File(path).exists()){
                    img = BitmapFactory.decodeFile(path);
                    //prossimo if controlla che non si sia danneggiato il file
                    if(img!=null){
                        return img;
                    }
                }
            return null;
        }catch (Exception e){
            System.err.println(e.toString());
            return null;
        }
    }

    //server method

    //controlla se la cache è aggiornata
    private static void cacheImage(int type,int id,CustomAppCompatActivity me,ImageView view,Bitmap img, String timestamp){
        ImageCachingTask task = new ImageCachingTask(type,id,me,view,img,timestamp);
        task.execute();
    }

    // recupera l'immagine dal server
    private static void getImageFromServer(CustomAppCompatActivity me,ImageView img, int type, int img_id){
        GetImageFromServerTask task = new GetImageFromServerTask(img,type,img_id,me);
        task.execute();
    }

    //salva l'immagine sul server
    private static void saveImageToServer(Bitmap img,DBManager dbm,int type, int img_id){
        SaveImageToServerTask task = new SaveImageToServerTask(type,img_id,dbm,img);
        task.execute();
    }

    //classi dei processi secondari delle chiamate


    private static class GetImageFromServerTask extends AsyncTask<Void, Void, Boolean> {
        private ImageView img;
        private int type;
        private int id;
        private CustomAppCompatActivity me;
        private Bitmap bmap;
        private String timestamp;

        public GetImageFromServerTask(ImageView img, int type, int id, CustomAppCompatActivity me) {
            this.img = img;
            this.type = type;
            this.id = id;
            this.me = me;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            //recupero i dati
            JSONObject obj = me.dbm.getImageFromServer(type,id);
            try{
                String img = obj.getString("response");
                byte[] encodeByte = Base64.decode(img, Base64.DEFAULT);
                bmap = BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.length);
                timestamp = obj.getString("timestamp");
            }catch (Exception e){
                return false;
            }
            return bmap != null;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            if(success){
                saveImage(me,type,id,bmap);
                saveTimeStamp(type,id,timestamp);
                img.setImageBitmap(bmap);
            }
        }

        @Override
        protected void onCancelled() {
            System.err.println("Errore nel recupero dell'immagine dal server");
        }
    }

    private static class SaveImageToServerTask extends AsyncTask<Void, Void, Boolean> {
        private int type;
        private int id;
        private DBManager dbm;
        private Bitmap bmap;
        private String timestamp;

        public SaveImageToServerTask(int type, int id, DBManager dbm, Bitmap bmap) {
            this.type = type;
            this.id = id;
            this.dbm = dbm;
            this.bmap = bmap;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            //recupero i dati
            timestamp = dbm.saveImageToServer(type,id,bmap);
            return timestamp != null;
        }

        @Override
        protected void onPostExecute(Boolean success) {
            if(success){
                saveTimeStamp(type,id,timestamp);
            }
        }

        @Override
        protected void onCancelled() {
            System.err.println("Errore nel salvataggio dell'immagine sul server");
        }
    }

    private static class ImageCachingTask extends AsyncTask<Void, Void, Boolean>{
        private int type;
        private int id;
        private CustomAppCompatActivity me;
        private Bitmap bmap;
        private ImageView view;
        private String timestamp;

        public ImageCachingTask(int type, int id, CustomAppCompatActivity me,ImageView view, Bitmap bmap,String timestamp) {
            this.type = type;
            this.id = id;
            this.me = me;
            this.bmap = bmap;
            this.view = view;
            this.timestamp = timestamp;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            //recupero i dati
            return me.dbm.checkCachedImage(type,id,timestamp);
        }

        @Override
        protected void onPostExecute(Boolean success) {
            if(success){
                view.setImageBitmap(bmap);
            }else{
                getImageFromServer(me,view,type,id);
            }
        }

        @Override
        protected void onCancelled() {
            System.err.println("Errore nel salvataggio dell'immagine sul server");
        }
    }
}
