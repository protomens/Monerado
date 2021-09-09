package cc.symplectic.monerado.workers;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

import cc.symplectic.monerado.MainActivity;
import cc.symplectic.monerado.R;
import cc.symplectic.monerado.ReadWriteGUID;
import cc.symplectic.monerado.receivers.NotificationReceiver;

public class MinerIdentifiersWorker extends Worker {
    private String APIHOST = "https://api.moneroocean.stream/";
    private String MOADDY = "x";
    private String IdentifiersURL = APIHOST + "miner/" + MOADDY  + "/identifiers";

    public MinerIdentifiersWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }
    @Override
    public Result doWork() {

        Boolean sanity = LoadPoolJSON();

        // Do the work here--in this case, upload the images.
        if(sanity) {
            GetIdentifiers(IdentifiersURL);
        }

        // Indicate whether the work finished successfully with the Result
        return Result.success();
    }

    private Boolean LoadPoolJSON() {
        ReadWriteGUID moaddyfile = new ReadWriteGUID("moaddy.pls");
        String PoolInfosJSON = moaddyfile.readFromFile(getApplicationContext());
        if (PoolInfosJSON.isEmpty()) {
            return false;
        } else {
            try {
                JSONObject poolobj = new JSONObject(PoolInfosJSON);
                String pool = poolobj.getString("pool");
                if (pool.equals("C3pool")) {
                    APIHOST = "https://api.c3pool.com/";
                }
                MOADDY = poolobj.getString("address");
                IdentifiersURL = APIHOST + "miner/" + MOADDY  + "/identifiers";

            }
            catch (JSONException e) {
                Log.w("BPW", "ERROR ON JSON");
                MOADDY = PoolInfosJSON;
                IdentifiersURL = APIHOST + "miner/" + MOADDY  + "/identifiers";
            }
        }
        return true;
    }

    private void GetIdentifiers(String url) {
        StringRequest request = new StringRequest(url, new Response.Listener<String>() {
            @Override
            public void onResponse(String string) {
                try {
                    ArrayList<String> Identifiers = new ArrayList<>();
                    Identifiers = parseJsonData(string);
                    CompareIdentifiers(Identifiers, string);
                }
                catch (JSONException e) { e.printStackTrace(); }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                Log.w("MIW", "ERROR GETTING IDENTIFIERS: " + volleyError.getMessage());
            }
        });

        RequestQueue rQueue = Volley.newRequestQueue(super.getApplicationContext());
        rQueue.add(request);
    }

    private ArrayList<String> parseJsonData(String jsonString) throws JSONException {
        Log.d("MIW", "JsonArray: " + jsonString);
        JSONArray jsonIDArray = new JSONArray(jsonString);
        ArrayList<String> Identifiers = new ArrayList<>();
        if (jsonIDArray.length() > 0) {
            for (int k = 0; k < jsonIDArray.length(); k++) {
                Identifiers.add(jsonIDArray.get(k).toString());
                //Log.d("MIW", "ID " + String.valueOf(k) + ": " + Identifiers.get(k));
            }
        }
        return Identifiers;
    }

    private void CompareIdentifiers(ArrayList<String> NewIdentifiers, String JSONString) {
        ArrayList<String> OldIdentifiers = new ArrayList<>();
        ReadWriteGUID identifiersFile = new ReadWriteGUID("identifiers.mo");
        String identifiersJSON = identifiersFile.readFromFile(super.getApplicationContext());
        try {
            OldIdentifiers = parseJsonData(identifiersJSON);
        }
        catch (JSONException e) { e.printStackTrace(); }
        Collection<String> SameWorkers = new HashSet<String>( OldIdentifiers );
        Collection<String> DeadWorkers = new HashSet<String>();
        Collection<String> NewWorkers = new HashSet<String>();

        //different.addAll(OldIdentifiers);
        //different.addAll(NewIdentifiers);
        //different.removeAll()


        SameWorkers.retainAll(NewIdentifiers);

        for (String worker : NewIdentifiers) {
            if (SameWorkers.contains(worker)) {
                //Log.v("MIW", worker + " is not Fresh");
            }
            else {
                NewWorkers.add(worker);
            }
        }
        for (String worker : OldIdentifiers) {
            if (SameWorkers.contains(worker)) {
                //Log.v("MIW", worker + " is not Dead");
            }
            else {
                DeadWorkers.add(worker);
            }
        }
        if (NewWorkers.isEmpty()) {
            Log.d("MIW", "NewWorkers is empty");
        }
        else {
            SendPushNotification(NewWorkers, true);
        }
        if (DeadWorkers.isEmpty()) {
            Log.d("MIW", "DeadWorkers is empty");
        }
        else {
            SendPushNotification(DeadWorkers, false);
        }
        identifiersFile.writeToFile(JSONString, super.getApplicationContext());
    }

    private void SendPushNotification(Collection<String> Workers, Boolean NewWorkers) {
        String MessagePreface = "";
        if (NewWorkers) {
            MessagePreface = MessagePreface + "New Workers: ";
        }
        else {
            MessagePreface = MessagePreface + "Dead Workers: ";
        }
        int k = 0;
        String NewMessage = "";
        for (String worker : Workers) {
            if (k==0) {
                NewMessage = NewMessage + worker;
            }
            else {
                NewMessage = NewMessage + ", " + worker;
            }
            k++;
        }

        String Message = MessagePreface + NewMessage;
        String Title = "Worker Status";

        /*
        NotificationManager notif=(NotificationManager) super.getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        Notification notify=new Notification.Builder
                (getApplicationContext()).setContentTitle(Title)
                .setContentText(Message)
                .setSmallIcon(R.drawable.monerado_mountain_small)
                .build();

        notify.flags |= Notification.FLAG_AUTO_CANCEL;
        notif.notify(0, notify);
        */


        Intent intent = new Intent(super.getApplicationContext(), NotificationReceiver.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.putExtra("Title", Title);
        intent.putExtra("ContentText", MessagePreface);
        intent.putExtra("Message", Message);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(super.getApplicationContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        AlarmManager alarmManager = (AlarmManager) super.getApplicationContext().getSystemService(Context.ALARM_SERVICE);
        alarmManager.set(AlarmManager.RTC_WAKEUP, 1000, pendingIntent);

    }


}
