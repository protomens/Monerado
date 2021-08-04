package cc.symplectic.monerado.fragmets;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import cc.symplectic.monerado.R;
import cc.symplectic.monerado.ReadWriteGUID;
import cc.symplectic.monerado.RemrigWorker;
import cc.symplectic.monerado.RetroRemrig;
import cc.symplectic.monerado.WorkerNameObj;
import retrofit2.Call;
import retrofit2.Callback;


public class RemrigFragment extends Fragment {
    private Boolean Remrig = false;
    private String WorkerName;
    public String RemrigURL;
    public String RemrigUSER;
    public String RemrigPASS;
    private ArrayList<String> WorkerNames;

    public RemrigFragment(ArrayList al) {
        this.WorkerNames = al;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        // Defines the xml file for the fragment
        return inflater.inflate(R.layout.fragment_remrig, parent, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        addRemrigListener(view);

        ListView workersList = (ListView) view.findViewById(R.id.workerList);
        ArrayAdapter adapter = new ArrayAdapter(getActivity(), R.layout.list_white_text, WorkerNames);
        workersList.setAdapter(adapter);

        addWorkersOnClickListener(workersList, view);
        addWorkersLongOnClickListener(workersList, view);
    }

    public void addWorkersOnClickListener(ListView workersList, View v) {
        workersList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                HashMap<String, RemrigWorker> Moneradoremrig = new HashMap<String, RemrigWorker>();

                ReadWriteGUID remrigFILE = new ReadWriteGUID("remrigs.json");
                String remrigJSON = remrigFILE.readFromFile(view.getContext());
                EditText et_worker = v.findViewById(R.id.et_workername);
                ImageView remrigActionButton = v.findViewById(R.id.imageViewActionRemrig);
                try {
                    Moneradoremrig = parseRemrigJSONFile(remrigJSON);
                    et_worker.setText(workersList.getItemAtPosition(position).toString());
                    et_worker = v.findViewById(R.id.et_remrig);
                    et_worker.setText(Moneradoremrig.get(workersList.getItemAtPosition(position).toString()).getURL());
                    et_worker = v.findViewById(R.id.et_username);
                    et_worker.setText(Moneradoremrig.get(workersList.getItemAtPosition(position).toString()).getUsername());
                    et_worker = v.findViewById(R.id.et_password);
                    et_worker.setText(Moneradoremrig.get(workersList.getItemAtPosition(position).toString()).getPassword());
                    Boolean actionValue = Moneradoremrig.get(workersList.getItemAtPosition(position).toString()).getState();
                    if (actionValue) {
                        remrigActionButton.setImageResource(R.drawable.stop);
                    }
                    else {
                        remrigActionButton.setImageResource(R.drawable.start);
                    }
                }
                catch (JSONException | NullPointerException e) {
                    et_worker = v.findViewById(R.id.et_remrig);
                    et_worker.setText("");
                    et_worker.setHint("https://address:port");
                    et_worker = v.findViewById(R.id.et_username);
                    et_worker.setText("");
                    et_worker.setHint("xmrig");
                    et_worker = v.findViewById(R.id.et_password);
                    et_worker.setText("");
                    et_worker.setHint("abc123");

                }
            }
        });
    }

    public void addWorkersLongOnClickListener(ListView workerList, View v) {
        workerList.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                return false;
            }
        });
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

        Log.d("Remrig", "Worker: " + WorkerName + " URL: " + RemrigURL + " User: " + RemrigUSER + " Password: " + RemrigPASS);

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
            try {
                UpdateAdapter(v);
            }
            catch (JSONException e) {}
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
            //Log.d("RemFrag", "Worker: " + WorkerName);
            //Log.d("RemFrag", "Moneradoremrig: " + Moneradoremrig.toString());
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
            Moneradoremrig.put(al.get(k).toString(), remrig);
            k++;
        }
        return Moneradoremrig;

    }

    private void UpdateAdapter(View view) throws JSONException {
        ReadWriteGUID remrigFILE = new ReadWriteGUID("remrigs.json");
        String remrigJSON = remrigFILE.readFromFile(view.getContext());
        WorkerNameObj WNObj = new WorkerNameObj();

        JSONObject object = new JSONObject(remrigJSON);
        Iterator<String> keys2 = (Iterator<String>) object.keys();

        while (keys2.hasNext()) {
            String key = keys2.next();
            WNObj.al.add(key);
            //WNObj.workerObjects.add(object.getJSONObject(key));
        }

        ListView workersList = (ListView) view.findViewById(R.id.workerList);
        ArrayAdapter adapter = new ArrayAdapter(getActivity(), R.layout.list_white_text, WNObj.al);
        workersList.setAdapter(adapter);

    }

    public void RetroRemrigAction(String action, View view) {
        Call<String> call  = RetroRemrig.getRetrofitInstance(this.WorkerName, view)
                .create(RetroRemrig.ApiInterface.class)
                .setRemrigAction(RemrigURL + "/api/xmrig" , RetroRemrig.getAuthToken(RemrigUSER, RemrigPASS), action);

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