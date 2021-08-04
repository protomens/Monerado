package cc.symplectic.monerado;

import android.util.Base64;
import android.view.View;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;

import retrofit2.http.Header;
import retrofit2.http.Query;
import retrofit2.http.Url;

public class RetroRemrig  {

    private static Retrofit retrofit;
    private static  String BASE_URL;
    private static  String username;
    private static  String password;

    public static Retrofit getRetrofitInstance(String WorkerName, View view) {
        RemrigWorker remrig;
        HashMap<String, RemrigWorker> Moneradoremrig = new HashMap<String, RemrigWorker>();

        ReadWriteGUID workerRemrigs = new ReadWriteGUID("remrigs.json");
        String remrigs = workerRemrigs.readFromFile(view.getContext());

        try { Moneradoremrig = parseRemrigJSONFile(remrigs); }
        catch (JSONException e) { e.printStackTrace();}

        BASE_URL = Moneradoremrig.get(WorkerName).getURL();
        username = Moneradoremrig.get(WorkerName).getUsername();
        password = Moneradoremrig.get(WorkerName).getPassword();

        if (retrofit == null) {
            retrofit = new retrofit2.Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }
    static private HashMap<String, RemrigWorker> parseRemrigJSONFile(String jsonString) throws JSONException {
        HashMap<String, RemrigWorker> Moneradoremrig = new HashMap<String, RemrigWorker>();
        JSONObject object = new JSONObject(jsonString);

        ArrayList al = new ArrayList();
        ArrayList<JSONObject> workerObjects = new ArrayList<>();

        @SuppressWarnings("unchecked")
        Iterator<String> keys = (Iterator<String>) object.keys();
        while (keys.hasNext()) {
            String key = keys.next();
            al.add(key);
            workerObjects.add(object.getJSONObject(key));
        }
        int k=0;
        for (JSONObject obj : workerObjects) {
            RemrigWorker remrig = new RemrigWorker(obj.getString("URL"), obj.getString("Username"), obj.getString("Password"), obj.getBoolean("state"));
            //SaveRemrigJSON(remrig, al.get(k).toString());
            Moneradoremrig.put(al.get(k).toString(), remrig);

            k++;
        }
        return Moneradoremrig;

    }
    public interface ApiInterface {


        @GET()
        Call<String> setRemrigAction(@Url String url, @Header("Authorization") String authKey, @Query("action") String action);
        @GET()
        Call<String> getCPUSensors(@Url String url, @Header("Authorization") String authKey);
        @GET()
        Call<String> getLastCoinMined(@Url String url, @Header("Authorization") String authKey);
        @GET()
        Call<String> getMemInfo(@Url String url, @Header("Authorization") String authKey);
    }
    public static String getAuthToken(String username, String password) {
        byte[] data = new byte[0];
        try {
            data = (username + ":" + password).getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return "Basic " + Base64.encodeToString(data, Base64.NO_WRAP);
    }
}