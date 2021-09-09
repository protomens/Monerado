package cc.symplectic.monerado.fragmets;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.math.MathContext;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;

import cc.symplectic.monerado.MainActivity;
import cc.symplectic.monerado.R;
import cc.symplectic.monerado.ReadWriteGUID;
import cc.symplectic.monerado.WorkerNameObj;

public class MenuFragment extends Fragment {
    ProgressDialog dialog;
    String WorkerURL = MainActivity.APIHOST + "miner/" + MainActivity.MOADDY + "/stats/allWorkers";
    String PaymentURL = MainActivity.APIHOST + "miner/" + MainActivity.MOADDY + "/stats";
    String BlockPayURL = MainActivity.APIHOST + "miner/"+ MainActivity.MOADDY + "/block_payments?limit=100";
    String PoolStatsURL = MainActivity.APIHOST + "pool/stats";
    HashMap<String, String> PaymentInfo = new HashMap<>();
    HashMap<String, String> PoolStatsInfo = new HashMap<>();
    View MenuView;
    private static BigDecimal TotalBlockPayment = new BigDecimal(0.0);

    public MenuFragment() {

    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        // Defines the xml file for the fragment
        return inflater.inflate(R.layout.fragment_main_menu, parent, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        MenuView = view;
        setMenuIconsOnClickListener(view);
        onBackKeyPressed(view);
        GetInfos(PaymentURL, 420);


    }

    private void setMenuIconsOnClickListener(View v) {
        ImageView Payments = v.findViewById(R.id.imageView4);
        ImageView WorkerStats = v.findViewById(R.id.imageView5);
        ImageView BlockPayments = v.findViewById(R.id.imageView8);
        ImageView Remrig = v.findViewById(R.id.imageView10);
        ImageView WarriorV = v.findViewById(R.id.imageView7);
        FloatingActionButton refreshButton = v.findViewById(R.id.floatingRefreshButton);

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

        WarriorV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Fragment WarriorVMinerFrag = new WarriorVMinerFragment();
                RunMenuFragment(WarriorVMinerFrag, "XMRIG");
            }
        });

        refreshButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GetInfos(PaymentURL, 420);

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

    private void parseJsonData(String jsonString, int position) throws JSONException {
        JSONObject object = null;
        WorkerNameObj WNObj = new WorkerNameObj();
        JSONArray jsonArray;
        ArrayList<HashMap<String, String>> blockPayments = new ArrayList<HashMap<String, String>>();
        MathContext mc = new MathContext(12);



        if (position != 2) {
            object = new JSONObject(jsonString);
        }


        switch(position) {
            case 0:
                HashMap<String, String> PaymentInfos = new HashMap<String, String>();
                try { PaymentInfos = parsePaymentInfos(object); }
                catch (JSONException e) { e.printStackTrace();}
                Fragment fragment = new PaymentFragment(PaymentInfos);
                RunMenuFragment(fragment, "PAYMENTS");
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
                RunMenuFragment(fragment2, "WORKERS");
                break;
            case 2:
                /*try {
                    TotalBlockPayment = new BigDecimal(0.0);
                    blockPayments = parseBlockPayments(jsonString, true);
                }
                catch (JSONException e) { e.printStackTrace(); }

                 */
                Fragment fragment0 = new BlockPaymentFragment();
                RunMenuFragment(fragment0, "BLOCK_PAYMENTS");
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
                RunMenuFragment(fragment3, "REMRIG");
                break;
            case 314:
                try {
                    PoolStatsInfo = parsePoolStats(object);
                    SetQuickStats(position);
                }
                catch (JSONException e) {e.printStackTrace();}
                break;
            case 420:
                try {
                    PaymentInfo = parsePaymentInfos(object);
                    SetQuickStats(position);
                    dialog.dismiss();
                    GetInfos(PoolStatsURL, 314);
                    dialog.dismiss();
                }
                catch (JSONException e) { e.printStackTrace();}
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + position);

        }
    }

    private void SetQuickStats(Integer position) throws JSONException {
        switch(position) {
            case 314:
                TextView textView = MenuView.findViewById(R.id.tv_noOfMiner);
                textView.setText(PoolStatsInfo.get("miners"));
                textView = MenuView.findViewById(R.id.tv_xmrprice);
                textView.setText(PoolStatsInfo.get("price"));
                break;
            case 420:
                Double Hash2;
                DecimalFormat df = new DecimalFormat("###,###.000");
                TextView tv = MenuView.findViewById(R.id.tv_myhashrate);

                String Hash = PaymentInfo.get("RawHash");

                if (Double.parseDouble(Hash) > 1000) {
                    Hash2 = Double.parseDouble(Hash) / (double) 1000;
                } else {
                    Hash2 = Double.valueOf(Hash);
                }

                String myHash = df.format(Hash2);

                Hash = PaymentInfo.get("PayHash");
                ComputeDailyMiningRevenue(Double.parseDouble(Hash));
                if (Double.parseDouble(Hash) > 1000) {
                    Hash2 = Double.parseDouble(Hash) / (double) 1000;
                } else {
                    Hash2 = Double.valueOf(Hash);
                }

                myHash = myHash + " / " + df.format(Hash2) + " Kh/s";
                tv.setText(myHash);

                BigDecimal XMR = new BigDecimal(PaymentInfo.get("AmtPaid"));
                XMR = XMR.divide(PaymentFragment.Satoshi);
                tv = MenuView.findViewById(R.id.tv_totalxmr);
                tv.setText(String.valueOf(XMR));
                tv = MenuView.findViewById(R.id.tv_pendingxmr);
                XMR = new BigDecimal(PaymentInfo.get("AmtDue"));
                XMR = XMR.divide(PaymentFragment.Satoshi);
                tv.setText(String.valueOf(XMR));
                break;

        }

    }

    private void ComputeDailyMiningRevenue(Double payHash) throws JSONException {
        ArrayList<HashMap<String, Double>> JSONPayArray = new ArrayList<HashMap<String, Double>>();
        Double AvgPayHashRate = 0.0;

        ReadWriteGUID payFile = new ReadWriteGUID("avgpayhash.json");
        String avgPayHashJSON = payFile.readFromFile(getContext());
        Log.d("MF", "Pay Hash JSON: " + avgPayHashJSON);
        JSONArray jsonArray = new JSONArray(avgPayHashJSON);
        Double payhash;
        int k;
        Integer payRateArrayLen = jsonArray.length();
        Log.d("MF", "Json Length: " + payRateArrayLen);
        if (payRateArrayLen >= 50) {
            for (k = payRateArrayLen - 50; k < jsonArray.length(); k++) {
                HashMap<String, Double> JSONPayHash = new HashMap<>();
                JSONObject object = jsonArray.getJSONObject(k);
                payhash = Double.parseDouble(object.getString("hashrate"));
                JSONPayHash.put("hashrate", payhash);
                JSONPayArray.add(JSONPayHash);
                JSONPayHash = null;
                AvgPayHashRate += payhash;

            }
        }
        else {
            for (k = 0; k < jsonArray.length(); k++) {
                HashMap<String, Double> JSONPayHash = new HashMap<>();
                JSONObject object = jsonArray.getJSONObject(k);
                payhash = Double.parseDouble(object.getString("hashrate"));
                JSONPayHash.put("hashrate", payhash);
                JSONPayArray.add(JSONPayHash);
                JSONPayHash = null;
                AvgPayHashRate += payhash;
            }
        }
        HashMap<String, Double> JSONPayHash = new HashMap<>();
        JSONPayHash.put("hashrate", payHash);
        JSONPayArray.add(JSONPayHash);

        AvgPayHashRate += payHash;
        AvgPayHashRate = AvgPayHashRate / (k + 1);

        Integer intAvgHashRate = (int) Math.round(AvgPayHashRate);

        Log.d("MF", "Average Hash Rate: " + intAvgHashRate);


        // Get CoinCalculators XMR Daily pay rate, parse JSON, and set TextView
        GetMiningRevenue(intAvgHashRate);

        // Write newest payrate
        Gson gson = new Gson();
        String avgpaystring = gson.toJson(JSONPayArray);
        payFile.writeToFile(avgpaystring, getContext());

    }

    private void GetMiningRevenue(Integer HashRate) {
        String url = "https://www.coincalculators.io/api?hashrate=" + String.valueOf(HashRate) + "&name=Monero";

        StringRequest request = new StringRequest(url, new Response.Listener<String>() {
            @Override
            public void onResponse(String string) {
                try {
                    String DailyPayRate = "$" + parseMiningRevenueJson(string);
                    Log.d("MF", "Daily XMR Payout: " + DailyPayRate);
                    TextView tv_dpr = MenuView.findViewById(R.id.tv_dailyxmrpayout);
                    tv_dpr.setText(DailyPayRate);
                }
                catch (JSONException e) { e.printStackTrace(); }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                Log.d("GMR", "ERROR IN GETTING XMR MINING INFOS");
                Toast.makeText(getContext(), "Could not get XMR Revenue from CoinCalculators!", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            }
        });

        RequestQueue rQueue = Volley.newRequestQueue(getContext());
        rQueue.add(request);
    }

    private String parseMiningRevenueJson(String jsonString) throws JSONException {
        JSONObject object = new JSONObject(jsonString);
        Double DailyPayRate = Double.parseDouble(object.getString("revenueInDayUSD"));
        Log.d("MFparse", "Daily Pay Rate: " + DailyPayRate);
        DecimalFormat df = new DecimalFormat("###.00");
        return String.valueOf(df.format(DailyPayRate));

    }

    private HashMap<String,String> parsePoolStats(JSONObject object) throws JSONException {
        HashMap<String,String> PoolStatsMap = new HashMap<>();
        DecimalFormat df = new DecimalFormat("###,###.###");

        JSONObject pool_stats = object.getJSONObject("pool_statistics");
        PoolStatsMap.put("miners", pool_stats.getString("miners"));
        pool_stats = pool_stats.getJSONObject("price");
        PoolStatsMap.put("price", df.format(Double.parseDouble(pool_stats.getString("usd"))));

        return PoolStatsMap;

    }

    public static ArrayList<HashMap<String, String>>  parseBlockPayments(String jsonString, Boolean NewData) throws JSONException {
        JSONArray jsonArray;
        ArrayList<HashMap<String, String>> blockPayments = new ArrayList<HashMap<String, String>>();
        DecimalFormat df = new DecimalFormat("#.#####################");
        SimpleDateFormat DateFor = new SimpleDateFormat("EEE, d MMM yyyy hh:mm:ss aaa");
        jsonArray = new JSONArray(jsonString);

        if (NewData) {
            for (int k = 0; k < jsonArray.length(); k++) {
                HashMap<String, String> bpay = new HashMap<String, String>();
                JSONObject jsonObject = jsonArray.getJSONObject(k);
                //Date d = new Date((long) (Long.parseLong(jsonObject.getString("ts")) * 1000));
                //bpay.put("ts", DateFor.format(d));
                bpay.put("ts", jsonObject.getString("ts"));
                //d = new Date((long) (Long.parseLong(jsonObject.getString("ts_found")) * 1000));
                //bpay.put("ts_found", "Date Found: " + DateFor.format(d));
                bpay.put("ts_found", jsonObject.getString("ts_found"));
                BigDecimal bigDecimal = new BigDecimal(jsonObject.get("value_percent").toString());
                //bpay.put("value_percent", df.format(bigDecimal) + "%");
                bpay.put("value_percent", df.format(bigDecimal));
                bigDecimal = new BigDecimal(jsonObject.get("value").toString());
                //bpay.put("value", df.format(bigDecimal) + " XMR");
                bpay.put("value", df.format(bigDecimal));
                //TotalBlockPayment = TotalBlockPayment.add(bigDecimal);
                blockPayments.add(bpay);
                bpay = null;
            }
        }
        else {
            for (int k = 0; k < jsonArray.length(); k++) {
                HashMap<String, String> bpay = new HashMap<String, String>();
                JSONObject jsonObject = jsonArray.getJSONObject(k);
                bpay.put("ts", jsonObject.getString("ts"));
                bpay.put("ts_found", jsonObject.getString("ts_found"));
                bpay.put("value_percent", jsonObject.get("value_percent").toString());
                bpay.put("value", jsonObject.get("value").toString());
                //TotalBlockPayment = TotalBlockPayment.add(bigDecimal);
                blockPayments.add(bpay);
                bpay = null;
            }
        }
        return blockPayments;
    }

    public static HashMap<String, String> parsePaymentInfos(JSONObject object) throws JSONException {
        HashMap<String, String> PaymentInfos = new HashMap<String, String>();

        PaymentInfos.put("RawHash", object.getString("hash"));
        PaymentInfos.put("PayHash", object.getString("hash2"));
        PaymentInfos.put("TotalHash", object.getString("totalHashes"));
        PaymentInfos.put("Vshares", object.getString("validShares"));
        PaymentInfos.put("Ishares", object.getString("invalidShares"));
        PaymentInfos.put("AmtPaid", object.getString("amtPaid"));
        PaymentInfos.put("AmtDue", object.getString("amtDue"));
        PaymentInfos.put("PayCount", object.getString("txnCount"));
        return PaymentInfos;
    }

    private void RunMenuFragment(Fragment fragment, String TAG) {
        getParentFragmentManager().beginTransaction()
                .setCustomAnimations(
                        R.anim.slide_in,  // enter
                        R.anim.fade_out,  // exit
                        R.anim.fade_in,   // popEnter
                        R.anim.slide_out  // popExit
                )
                .replace(R.id.monerado_main_frame, fragment)
                .addToBackStack(TAG)
                .commit();

        try {
            dialog.dismiss();
        }
        catch (NullPointerException e) { Log.w("Menu", "Did not fetch data... no dialog"); }

    }

    private void onBackKeyPressed(View view) {
        //You need to add the following line for this solution to work; thanks skayred
        view.setFocusableInTouchMode(true);
        view.requestFocus();
        view.setOnKeyListener( new View.OnKeyListener()
        {
            @Override
            public boolean onKey( View v, int keyCode, KeyEvent event )
            {
                if( keyCode == KeyEvent.KEYCODE_BACK )
                {
                    return true;
                }
                else {
                    return false;
                }
            }
        } );
    }
}
