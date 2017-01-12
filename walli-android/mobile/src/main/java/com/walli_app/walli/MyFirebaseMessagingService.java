package com.walli_app.walli;

/**
 * Created by dado on 08/07/2016.
 */

//https://github.com/firebase/quickstart-android/blob/master/messaging/app/src/main/java/com/google/firebase/quickstart/fcm/MyFirebaseMessagingService.java
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Environment;
import android.support.v4.app.NotificationCompat;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("deprecation")
public class MyFirebaseMessagingService extends FirebaseMessagingService {
    private final static String TAG = "NOTIFY ERROR: ";
    private static int count = 0;
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        String type = remoteMessage.getData().get("type");
        sendNotification(type,remoteMessage.getData().get("message"));
        // If the application is in the foreground handle both data and notification messages here.
        // Also if you intend on generating your own notifications as a result of a received FCM
        // message, here is where that should be initiated. See sendNotification method below.
    }
    // [END receive_message]


//    https://github.com/miskoajkula/Fcm/blob/Fcm/app/src/main/java/testing/fcm/FirebaseMessagingService.java

    private void sendNotification(String type, String json) {
        Map<String,String> info;
        String path;
        int default_image;
        //genero i dati per la notifica
        try{
            JSONObject data = new JSONObject(json);
            switch (type){
                case "newGroup":
                    info = createGroupText(data);
                    path = Environment.getExternalStorageDirectory().getPath() + "/Walli/group/"+data.getString("id");
                    default_image = R.mipmap.default_group_image;
                    break;
                case "newShop":
                    info = createShopText(R.string.push_new_shop,data);
                    path = Environment.getExternalStorageDirectory().getPath() + "/Walli/group/"+data.getString("g_id");
                    default_image = R.mipmap.default_group_image;
                    break;
                case "newNotify":
                    info = createShopText(R.string.push_new_notify,data);
                    path = Environment.getExternalStorageDirectory().getPath() + "/Walli/group/"+data.getString("g_id");
                    default_image = R.mipmap.default_group_image;
                    break;
                case "paid":
                    info = createPaidText(data);
                    path = Environment.getExternalStorageDirectory().getPath() + "/Walli/user/"+data.getString("u_id");
                    default_image = R.mipmap.default_user_image;
                    break;
                case "paymentRequest":
                    info = createPaymentRequestData(data);
                    path = Environment.getExternalStorageDirectory().getPath() + "/Walli/user/"+data.getString("u_id");
                    default_image = R.mipmap.default_user_image;
                    break;
                default:
                    info = null;
                    path = "";
                    default_image = R.mipmap.ic_launcher;
            }
        }catch (Exception e){
            System.err.println(TAG+e.toString());
            info = null;
            path = "";
            default_image = R.mipmap.ic_launcher;
        }
        Bitmap icon = ImageManager.getImageOnlyFromCache(path);
        if(icon == null) { //noinspection ConstantConditions
            icon = ((BitmapDrawable) getResources().getDrawable(default_image)).getBitmap();
        }
        String title = info!=null?info.get("title"):"Walli";
        String message = info!=null?info.get("message"):"";
        //mostro notifica
        showNotification(icon,title,message);
    }

    //mostra una notifica all'utente
    private void showNotification(Bitmap icon, String title,String message) {
        Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        Intent i = new Intent(this,LoginActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,count,i,PendingIntent.FLAG_UPDATE_CURRENT);
        System.out.println(TAG+pendingIntent.toString());
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setAutoCancel(true)
                .setContentTitle(title)
                .setContentText(message)
                .setLargeIcon(icon)
                .setSmallIcon(R.mipmap.ic_stat_white_launcher)
                .setSound(alarmSound)
                .setContentIntent(pendingIntent);
        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        manager.notify(++count,builder.build());
    }

    //metodi che generano un messaggio, un titolo e un'immagine da inserire nella notifica
    //a seconda del tipo di notifica che arriva

    private Map<String,String> createPaymentRequestData(JSONObject data) throws JSONException {
        Map<String,String> toret = new HashMap<>();
        String title = data.getString("u_name");
        String text = getString(R.string.push_payment_request).replace("%name%",title);
        toret.put("title",title);
        toret.put("message",text);
        return toret;
    }

    private Map<String,String> createPaidText(JSONObject data) throws JSONException {
        Map<String,String> toret = new HashMap<>();
        toret.put("title",data.getString("u_name"));
        toret.put("message",getString(R.string.push_debit_paid));
        return toret;
    }

    private Map<String,String> createShopText(int string, JSONObject data) throws JSONException {
        Map<String,String> toret = new HashMap<>();
        toret.put("title",data.getString("g_name"));
        String message = getString(string);
        message = message.replace("%desc%",data.getString("desc"));
        toret.put("message",message);
        return toret;
    }

    private Map<String,String> createGroupText(JSONObject data) throws JSONException {
        Map<String,String> toret = new HashMap<>();
        toret.put("title",data.getString("name"));
        toret.put("message",getString(R.string.push_new_group));
        return toret;
    }
}
