package cc.symplectic.monerado.fragmets;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.R.layout;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import cc.symplectic.monerado.R;
import cc.symplectic.monerado.MainActivity;
public class WorkerFragment extends Fragment {
    ArrayList al;
    ArrayList<JSONObject> workerObjects;
    String ChartURL = "https://api.moneroocean.stream/miner/" + MainActivity.MOADDY + "/chart/hashrate/allWorkers";
    HashMap<String, ArrayList<Double>> WorkerHashChart = new HashMap<String, ArrayList<Double>>();
    ProgressDialog dialog;

    // The onCreateView method is called when Fragment should create its View object hierarchy,
    // either dynamically or via XML layout inflation.

    public WorkerFragment(ArrayList al, ArrayList<JSONObject> workerObjects)
    {
        this.al = al;
        this.workerObjects = workerObjects;
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        // Defines the xml file for the fragment
        return inflater.inflate(R.layout.fragment_workers, parent, false);
    }

    // This event is triggered soon after onCreateView().
    // Any view setup should occur here.  E.g., view lookups and attaching view listeners.
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        // Setup any handles to view objects here
        // EditText etFoo = (EditText) view.findViewById(R.id.etFoo);

        dialog = new ProgressDialog(getActivity());
        dialog.setMessage("Loading....");
        dialog.show();
        if (WorkerHashChart.isEmpty()) {
            GetInfos(ChartURL);
        }
        else {
            dialog.dismiss();
        }

        setAdapterWorkerStats(view);

    }

    private void GetInfos(String url) {
        //Runs volley string request and then executes parseJsonData()
        //which starts the WorkerStats fragment
        //
        //Should probably Generalize parseJsonData() at some pointe.


        StringRequest request = new StringRequest(url, new Response.Listener<String>() {
            @Override
            public void onResponse(String string) {
                try {
                    parseJsonChartData(string);
                }
                catch (JSONException e) { e.printStackTrace(); }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                Toast.makeText(getContext(), "Some error occurred!!", Toast.LENGTH_SHORT).show();
            }
        });

        RequestQueue rQueue = Volley.newRequestQueue(getContext());
        rQueue.add(request);
    }

    void parseJsonChartData(String jsonString) throws JSONException {

        JSONObject object = new JSONObject(jsonString);
        ArrayList worker = new ArrayList();
        ArrayList<JSONArray> workerObjectArray = new ArrayList<>();
        ArrayList<Double> HashRates = new ArrayList<>();

        ArrayList wts = new ArrayList();
        @SuppressWarnings("unchecked")
        Iterator<String> keys = (Iterator<String>) object.keys();
        while (keys.hasNext()) {
            String key = keys.next();
            worker.add(key);
            workerObjectArray.add(object.getJSONArray(key));
        }
        int k = 0;
        for (JSONArray array : workerObjectArray) {
            for (int j =0; j <  array.length(); j++) {
                JSONObject tsobj = array.getJSONObject(j);
                //Log.d("Worker", worker.get(k) + ": " + tsobj.getString("hs2"));
                HashRates.add(Double.valueOf(tsobj.getString("hs2")));
            }

            //Iterator<String> workerKeys = (Iterator<String>) obj.keys();
            //while (workerKeys.hasNext()) {
            //    JSONObject tsobj = obj.getJSONObject(workerKeys.next());
            //}
            WorkerHashChart.put(String.valueOf(worker.get(k)), HashRates);
            HashRates = new ArrayList<>();
            k++;
        }

        dialog.dismiss();



    }


    public void setAdapterWorkerStats(View view) {
        ListView workersList = (ListView) view.findViewById(R.id.workerList);
        ArrayAdapter adapter = new ArrayAdapter(getActivity(), R.layout.list_white_text, al);
        workersList.setAdapter(adapter);
        workersList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                // Replace the contents of the container with the new fragment
                Fragment fragment = new WorkerStatsFragment(workerObjects.get(position), (String) al.get(position),WorkerHashChart.get((String) al.get(position)));
                getParentFragmentManager().beginTransaction()
                        .setCustomAnimations(
                                R.anim.slide_in,  // enter
                                R.anim.fade_out,  // exit
                                R.anim.fade_in,   // popEnter
                                R.anim.slide_out  // popExit
                        )
                        .replace(R.id.monerado_main_frame, fragment)
                        .addToBackStack(null)
                        .commit();
                /*
                FragmentTransaction ft = getParentFragmentManager().beginTransaction();/
                ft.replace(R.id.monerado_main_frame, new WorkerStatsFragment(workerObjects.get(position), (String) al.get(position)));
                ft.setCustomAnimations(
                        R.anim.slide_in,  // enter
                        R.anim.fade_out,  // exit
                        R.anim.fade_in,   // popEnter
                        R.anim.slide_out  // popExit
                );
                ft.addToBackStack("Worker");
                // or ft.add(R.id.your_placeholder, new FooFragment());
                // Complete the changes added above
                ft.commit();
                */

            }
        });

    }


/*
    public void refreshMe(View v) {
        StringRequest request = new StringRequest(url, new Response.Listener<String>() {
            @Override
            public void onResponse(String string) {
                parseJsonData(string);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                Toast.makeText(getApplicationContext(), "Some error occurred!!", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            }
        });

        RequestQueue rQueue = Volley.newRequestQueue(MainActivity.this);
        rQueue.add(request);

    }

 */
}