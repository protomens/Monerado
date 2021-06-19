package cc.symplectic.monerado.fragmets;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import cc.symplectic.monerado.Monerado;
import cc.symplectic.monerado.R;
import cc.symplectic.monerado.ReadWriteGUID;
import cc.symplectic.monerado.RemrigWorker;
import cc.symplectic.monerado.RetroRemrig;
import retrofit2.Call;
import retrofit2.Callback;


public class RemrigFragment extends Fragment {
    private Boolean Remrig = false;
    private String WorkerName;
    public String RemrigURL;
    public String RemrigUSER;
    public String RemrigPASS;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        // Defines the xml file for the fragment
        return inflater.inflate(R.layout.fragment_remrig, parent, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        addRemrigListener(view);
    }

    public void addRemrigListener(View view) {
        ImageView actionRemrig = view.findViewById(R.id.imageViewActionRemrig);

        actionRemrig.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(Remrig) {
                    ActionRemrig(view);
                    actionRemrig.setImageResource(R.drawable.start);
                    //actionRemrig.setImageDrawable(ContextCompat.getDrawable(v.getContext(), R.drawable.start));
                }
                else {
                    ActionRemrig(view);
                    actionRemrig.setImageResource(R.drawable.stop);
                    //actionRemrig.setImageDrawable(ContextCompat.getDrawable(v.getContext(), R.drawable.stop));
                }

            }
        });
    }
    public void ActionRemrig(View v) {
        EditText remriget = v.findViewById(R.id.et_remrig);
        String remrigdata = remriget.getEditableText().toString();
        RemrigURL = remrigdata;

        remriget = v.findViewById(R.id.et_username);
        remrigdata = remriget.getEditableText().toString();
        RemrigUSER = remrigdata;
        remriget = v.findViewById(R.id.et_password);
        remrigdata = remriget.getEditableText().toString();
        RemrigPASS = remrigdata;
        remriget = v.findViewById(R.id.et_workername);
        this.WorkerName = remriget.getEditableText().toString();


        if(Remrig) {
            RemrigWorker remrig = new RemrigWorker(RemrigURL, RemrigUSER, RemrigPASS, false);
            SaveRemrigJSON(v, remrig);
            RetroRemrigAction("stop", v);
            Remrig = false;
        }
        else {
            RemrigWorker remrig = new RemrigWorker(RemrigURL, RemrigUSER, RemrigPASS, true);
            SaveRemrigJSON(v, remrig);
            RetroRemrigAction("start", v);
            Remrig = true;
        }

    }

    private void SaveRemrigJSON(View view, RemrigWorker remrig) {
        HashMap<String, RemrigWorker> Moneradoremrig = new HashMap<String, RemrigWorker>();

        ReadWriteGUID remrigFILE = new ReadWriteGUID("remrigs.json");
        String remrigJSON = remrigFILE.readFromFile(view.getContext());

        try {
            Moneradoremrig = parseRemrigJSONFile(remrigJSON);
            Moneradoremrig.put(WorkerName, remrig);
            Log.d("RemFrag", "Worker: " + WorkerName);
            Log.d("RemFrag", "Moneradoremrig: " + Moneradoremrig.toString());
        }
        catch (JSONException e) { e.printStackTrace(); }

        Gson gson = new Gson();
        remrigJSON = gson.toJson(Moneradoremrig);
        Log.d("RemFrag", "REMRIGJSON: " + remrigJSON);

        remrigFILE.writeToFile(remrigJSON, view.getContext());

    }

    private HashMap<String, RemrigWorker> parseRemrigJSONFile(String jsonString) throws JSONException {
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

    public void RetroRemrigAction(String action, View view) {
        Call<String> call  = RetroRemrig.getRetrofitInstance(this.WorkerName, view)
                .create(RetroRemrig.ApiInterface.class)
                .setRemrigAction(RetroRemrig.getAuthToken(), action);

        call.enqueue(new Callback<String>() {

            @Override
            public void onResponse(Call<String> call, retrofit2.Response<String> response) {
                String retCodeList = response.body();
                try {
                    String rc = retCodeList;
                    if (rc.toUpperCase().equals("START")) {
                        Log.d("Retro", "xmrig STARTED");
                        Toast.makeText(view.getContext(), "XMRig STARTED", Toast.LENGTH_LONG).show();
                    } else if (rc.toUpperCase().equals("STOP")) {
                        Toast.makeText(view.getContext(), "XMRig STOPPED", Toast.LENGTH_LONG).show();
                        Log.d("Retro", "xmrig STOPPED");
                    }
                    else {
                        Toast.makeText(view.    getContext(), "ERROR ID10T", Toast.LENGTH_LONG).show();
                        Log.d("Retro", "ID10T ERROR");
                    }
                }
                catch (Exception e) { Log.d("Retro", "Failed: "  + e.getMessage() + e.getStackTrace()); }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                Log.d("Retro", "Failed: " + t.getMessage() + t.getStackTrace() + t.getCause() + t.getSuppressed() + t.getLocalizedMessage());
            }
        });
    }

}