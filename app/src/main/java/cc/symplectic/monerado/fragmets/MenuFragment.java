package cc.symplectic.monerado.fragmets;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.math.MathContext;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;

import cc.symplectic.monerado.MainActivity;
import cc.symplectic.monerado.R;
import cc.symplectic.monerado.ReadWriteGUID;
import cc.symplectic.monerado.WorkerNameObj;

public class MenuFragment extends Fragment {
    ArrayList<String> MenuOptions;
    ProgressDialog dialog;
    String WorkerURL = "https://api.moneroocean.stream/miner/" + MainActivity.MOADDY + "/stats/allWorkers";
    String PaymentURL = "https://api.moneroocean.stream/miner/" + MainActivity.MOADDY + "/stats";
    String BlockPayURL = "https://api.moneroocean.stream/miner/"+ MainActivity.MOADDY + "/block_payments?limit=100";

    public MenuFragment(ArrayList<String> MenuOptions) {
        this.MenuOptions = MenuOptions;
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        // Defines the xml file for the fragment
        return inflater.inflate(R.layout.fragment_main_menu, parent, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {

        setMenuIconsOnClickListener(view);
    }

    private void setMenuIconsOnClickListener(View v) {
        ImageView Payments = v.findViewById(R.id.imageView4);
        ImageView WorkerStats = v.findViewById(R.id.imageView5);
        ImageView BlockPayments = v.findViewById(R.id.imageView8);
        ImageView Remrig = v.findViewById(R.id.imageView10);

        Payments.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GetInfos(PaymentURL, 0);

            }
        });

        WorkerStats.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GetInfos(WorkerURL, 1);
            }
        });

        BlockPayments.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GetInfos(BlockPayURL, 2);
            }
        });

        Remrig.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ReadWriteGUID remrigFILE = new ReadWriteGUID("remrigs.json");
                String remrigJSON = remrigFILE.readFromFile(v.getContext());
                try {
                    parseJsonData(remrigJSON, 3);
                }
                catch (JSONException e) { e.printStackTrace(); }
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
        JSONObject object = null;
        WorkerNameObj WNObj = new WorkerNameObj();
        JSONArray jsonArray;

        DecimalFormat df = new DecimalFormat("#.#####################");

        if (position != 2) {
            object = new JSONObject(jsonString);
        }


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
                //ArrayList al = new ArrayList();
                //ArrayList<JSONObject> workerObjects = new ArrayList<>();

                @SuppressWarnings("unchecked")
                Iterator<String> keys = (Iterator<String>) object.keys();
                while (keys.hasNext()) {
                    String key = keys.next();
                    WNObj.al.add(key);
                    WNObj.workerObjects.add(object.getJSONObject(key));
                }
                Fragment fragment2 = new WorkerFragment(WNObj.al, WNObj.workerObjects);
                RunMenuFragment(fragment2);
                break;
            case 2:
                ArrayList<HashMap<String, String>> blockPayments = new ArrayList<HashMap<String, String>>();
                jsonArray = new JSONArray(jsonString);
                BigDecimal TotalBlockPayment = new BigDecimal(0.0);
                MathContext mc = new MathContext(12);
                for (int k = 0; k < jsonArray.length(); k++) {
                    HashMap<String, String> bpay = new HashMap<String, String>();
                    JSONObject jsonObject = jsonArray.getJSONObject(k);
                    Date d = new Date((long) (Long.parseLong(jsonObject.getString("ts")) * 1000));
                    SimpleDateFormat DateFor = new SimpleDateFormat("EEE, d MMM yyyy hh:mm:ss aaa");
                    bpay.put("date", DateFor.format(d));
                    d = new Date((long) (Long.parseLong(jsonObject.getString("ts_found")) * 1000));
                    bpay.put("datefound", "Date Found: " + DateFor.format(d));
                    BigDecimal bigDecimal = new BigDecimal(jsonObject.get("value_percent").toString());
                    bpay.put("pct", df.format(bigDecimal) + "%");
                    bigDecimal = new BigDecimal(jsonObject.get("value").toString());
                    bpay.put("xmr", df.format(bigDecimal) + " XMR");
                    TotalBlockPayment = TotalBlockPayment.add(bigDecimal);
                    blockPayments.add(bpay);
                    //Log.d("BPAY", "date: " + dateString + " pct: " + blockPayments.get(k).get("pct").toString() + " amt: " +blockPayments.get(k).get("xmr").toString());
                    bpay = null;
                }
                Fragment fragment0 = new BlockPaymentFragment(blockPayments, String.valueOf(TotalBlockPayment.round(mc)));
                RunMenuFragment(fragment0);
                break;
            case 3:
                //ArrayList al2 = new ArrayList();
                //ArrayList<JSONObject> workerObjects2 = new ArrayList<>();
                try {
                    @SuppressWarnings("unchecked")
                    Iterator<String> keys2 = (Iterator<String>) object.keys();
                

                    while (keys2.hasNext()) {
                        String key = keys2.next();
                        WNObj.al.add(key);
                        //WNObj.workerObjects.add(object.getJSONObject(key));
                    }
                }
                catch (Exception e)  { e.printStackTrace(); }
                Fragment fragment3 = new RemrigFragment(WNObj.al);
                RunMenuFragment(fragment3);
        }
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
