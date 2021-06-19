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
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import cc.symplectic.monerado.MainActivity;
import cc.symplectic.monerado.R;

public class MenuFragment extends Fragment {
    ArrayList<String> MenuOptions;
    ProgressDialog dialog;
    String WorkerURL = "https://api.moneroocean.stream/miner/" + MainActivity.MOADDY + "/stats/allWorkers";
    String PaymentURL = "https://api.moneroocean.stream/miner/" + MainActivity.MOADDY + "/stats";

    public MenuFragment(ArrayList<String> MenuOptions) {
        this.MenuOptions = MenuOptions;
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        // Defines the xml file for the fragment
        return inflater.inflate(R.layout.fragment_workers, parent, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {

        ListView menuList = view.findViewById(R.id.workerList);
        ArrayAdapter adapter = new ArrayAdapter(getActivity(), R.layout.list_white_text, MenuOptions);
        menuList.setAdapter(adapter);
        menuList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch(position) {
                    case 0:
                        GetInfos(PaymentURL, position);

                        break;
                    case 1:
                        GetInfos(WorkerURL, position);
                        break;
                    case 2:
                        Fragment fragment = new RemrigFragment();
                        RunMenuFragment(fragment);

                }
            }
        });
    }

    private void GetInfos(String url, int position) {
        //Runs volley string request and then executes parseJsonData()
        //which starts the WorkerStats fragment
        //
        //Should probably Generalize parseJsonData() at some pointe.

        dialog = new ProgressDialog(getContext());
        dialog.setMessage("Loading....");
        dialog.show();

        StringRequest request = new StringRequest(url, new Response.Listener<String>() {
            @Override
            public void onResponse(String string) {
                try {
                    parseJsonData(string, position);
                }
                catch (JSONException e) { e.printStackTrace(); }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                Toast.makeText(getContext(), "Some error occurred!!", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            }
        });

        RequestQueue rQueue = Volley.newRequestQueue(getContext());
        rQueue.add(request);
    }

    void parseJsonData(String jsonString, int position) throws JSONException {

        JSONObject object = new JSONObject(jsonString);


        switch(position) {
            case 0:
                HashMap<String, String> PaymentInfos = new HashMap<String, String>();
                PaymentInfos.put("RawHash", object.getString("hash"));
                PaymentInfos.put("PayHash", object.getString("hash2"));
                PaymentInfos.put("TotalHash", object.getString("totalHashes"));
                PaymentInfos.put("Vshares", object.getString("validShares"));
                PaymentInfos.put("Ishares", object.getString("invalidShares"));
                PaymentInfos.put("AmtPaid", object.getString("amtPaid"));
                PaymentInfos.put("AmtDue", object.getString("amtDue"));
                PaymentInfos.put("PayCount", object.getString("txnCount"));
                Fragment fragment = new PaymentFragment(PaymentInfos);
                RunMenuFragment(fragment);
                break;
            case 1:
                ArrayList al = new ArrayList();
                ArrayList<JSONObject> workerObjects = new ArrayList<>();

                @SuppressWarnings("unchecked")
                Iterator<String> keys = (Iterator<String>) object.keys();
                while (keys.hasNext()) {
                    String key = keys.next();
                    al.add(key);
                    workerObjects.add(object.getJSONObject(key));
                }
                Fragment fragment2 = new WorkerFragment(al, workerObjects);
                RunMenuFragment(fragment2);
                break;
        }



        /*
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



        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        // Replace the contents of the container with the new fragment
        ft.replace(R.id.monerado_main_frame, new WorkerFragment(al, workerObjects));
        ft.setCustomAnimations(
                R.anim.slide_in,  // enter
                R.anim.fade_out,  // exit
                R.anim.fade_in,   // popEnter
                R.anim.slide_out  // popExit
        );
        ft.addToBackStack("Main");
        // or ft.add(R.id.your_placeholder, new FooFragment());
        // Complete the changes added above
        ft.commit();
        */



    }
    private void RunMenuFragment(Fragment fragment) {
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

        try {
            dialog.dismiss();
        }
        catch (NullPointerException e) { Log.w("Menu", "Did not fetch data... no dialog"); }

    }
}
