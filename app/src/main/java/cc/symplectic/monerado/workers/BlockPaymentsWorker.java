package cc.symplectic.monerado.workers;

import android.app.ProgressDialog;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.ListIterator;

import cc.symplectic.monerado.MainActivity;
import cc.symplectic.monerado.ReadWriteGUID;
import cc.symplectic.monerado.fragmets.MenuFragment;
import cc.symplectic.monerado.fragmets.PaymentFragment;
import cc.symplectic.monerado.fragmets.PaymentsListFragment;
import cc.symplectic.monerado.fragmets.StartupFragment;

public class BlockPaymentsWorker extends Worker {
    private String APIHOST = "https://api.moneroocean.stream/";
    private String MOADDY = "x";
    private String BlockPaymentURL = APIHOST + "miner/"+ MOADDY + "/block_payments?limit=100";
    private String PaymentURL = APIHOST + "miner/" + MOADDY + "/stats";
    private String PaymentsURL = APIHOST + "miner/" + MOADDY + "/payments";
    private String PaymentString;
    private ArrayList<HashMap<String, String>> blockPayments = new ArrayList<HashMap<String, String>>();
    private ArrayList<HashMap<String, String>> LastPayment = new ArrayList<>();
    private final String BLOCKPAYMENTFILE = "bpx-";
    private final String WORKERTAG = "BPW";

    public BlockPaymentsWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @Override
    public Result doWork() {

        // Load Pool Infos JSON FILE
        Boolean sanity = LoadPoolJSON();

        // Do the work here--in this case, upload the images.
        if (sanity) {
            GetInfos(BlockPaymentURL, false, false);
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
                BlockPaymentURL = APIHOST + "miner/"+ MOADDY + "/block_payments?limit=100";
                PaymentURL = APIHOST + "miner/" + MOADDY + "/stats";
                PaymentsURL = APIHOST + "miner/" + MOADDY + "/payments";

            }
            catch (JSONException e) {
                Log.w("BPW", "ERROR ON JSON");
                MOADDY = PoolInfosJSON;
                BlockPaymentURL = APIHOST + "miner/"+ MOADDY + "/block_payments?limit=100";
                PaymentURL = APIHOST + "miner/" + MOADDY + "/stats";
                PaymentsURL = APIHOST + "miner/" + MOADDY + "/payments";
            }
        }
        return true;
    }

    private void GetInfos(String URL, Boolean PaymentInfo, Boolean Payments) {

        StringRequest request = new StringRequest(URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String string) {
                try {
                    if (! PaymentInfo ) {
                        blockPayments = MenuFragment.parseBlockPayments(string, true);
                        GetInfos(PaymentURL, true, false);
                    }
                    else if (PaymentInfo && ! Payments) {
                        PaymentString = string;
                        GetInfos(PaymentsURL, true, true );
                    }
                    else {
                        LastPayment = PaymentFragment.parseJsonData(string, true);
                        try { CompareBlockPaymentsOnRecord(blockPayments, LastPayment,  PaymentString); }
                        catch (JSONException e) { e.printStackTrace();}
                    }
                }
                catch (JSONException e) { e.printStackTrace(); }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                Log.w("BPW", "Error in retrieving Block Payemtns");
            }
        });

        RequestQueue rQueue = Volley.newRequestQueue(super.getApplicationContext());
        rQueue.add(request);
    }
    private Boolean CompareBlockPaymentsOnRecord( ArrayList<HashMap<String, String>> NewBlockPayments, ArrayList<HashMap<String, String>> LastPayment, String jsonString) throws JSONException {
        DateFormat formatter =  new SimpleDateFormat("EEE, d MMM yyyy hh:mm:ss aaa");
        Date date ;
        Long longDate;

        ArrayList<HashMap<String, String>> bpx_payments = new ArrayList<HashMap<String, String>>();
        LinkedHashSet<HashMap<String, String>> AllBPXPayments = new LinkedHashSet<HashMap<String, String>>();
        HashMap<String, String> nextElement = new HashMap<String, String>();
        JSONObject object = new JSONObject(jsonString);

        HashMap<String, String> PaymentInfos = new HashMap<String, String>();
        HashMap<String, String> lpay = LastPayment.get(0);

        Long lpayts = Long.parseLong(lpay.get("date"));

        PaymentInfos = MenuFragment.parsePaymentInfos(object);
        String PayCount = PaymentInfos.get("PayCount");
        Integer currentTXN = Integer.parseInt(PayCount);
        currentTXN++;

        File file = new File(getApplicationContext().getFilesDir().getPath() + "/" + BLOCKPAYMENTFILE + String.valueOf(currentTXN) + ".json");
        ReadWriteGUID CTX_FILE = new ReadWriteGUID(BLOCKPAYMENTFILE + String.valueOf(currentTXN) + ".json");

        if (file.exists()) {
            Log.d(WORKERTAG, "BLOCK PAYMENT FILE EXISTS... COMPUTING...");
            String bpx_string = CTX_FILE.readFromFile(super.getApplicationContext());
            bpx_payments = MenuFragment.parseBlockPayments(bpx_string, false);

            ListIterator<HashMap<String, String>> bpxIterator = bpx_payments.listIterator();
            int k = 0;
            while (bpxIterator.hasNext()) {
                //Log.d("BPW", "Block #: " + k);
                nextElement = bpxIterator.next();
                //Log.d("BPW", "Next Block: " + nextElement);
                try {
                    date = (Date)formatter.parse(nextElement.get("ts"));
                    longDate = date.getTime();
                }
                catch (ParseException e){
                    longDate = Long.parseLong(nextElement.get("ts"));
                }
                if (longDate > lpayts) {
                    AllBPXPayments.add(nextElement);
                }
                k++;
            }

            bpx_payments = null;
            bpxIterator = null;
            bpx_payments = ReverseBlockPaymentOrder(NewBlockPayments, lpayts);
            bpxIterator = bpx_payments.listIterator();
            k=0;
            while (bpxIterator.hasNext()) {
                //Log.d("BPW", "Block #: " + k);
                nextElement = bpxIterator.next();
                try {
                    date = (Date)formatter.parse(nextElement.get("ts"));
                    longDate = date.getTime();
                }
                catch (ParseException e){
                    longDate = Long.parseLong(nextElement.get("ts"));
                }
                if (longDate > lpayts) {
                    AllBPXPayments.add(nextElement);
                }
                //Log.d("BPW", "Next Block: " + nextElement);
                //AllBPXPayments.add(nextElement);
                k++;
            }
            k=0;
            Log.d("BPW", "SIZE: " + AllBPXPayments.size());
            WriteBlockPaymentFile(CTX_FILE, null, AllBPXPayments, null);

        }
        else {
            // Do JSON write blockPayments ArrayList GSON
            // Be sure to write file on LIFO of NewBlockPayments sequence
            WriteBlockPaymentFile(CTX_FILE, NewBlockPayments, null, lpayts);
        }

        return true;
    }


    private void WriteBlockPaymentFile(ReadWriteGUID FILE, @Nullable  ArrayList<HashMap<String, String>> NewBlockPayments, @Nullable  LinkedHashSet<HashMap<String, String>> AllBPXPayments, @Nullable Long LastTSPayment) {
        ArrayList<HashMap<String, String>> RevBlockPayments = new ArrayList<HashMap<String, String>>();
        Gson gson = new Gson();
        if (AllBPXPayments == null) {
            RevBlockPayments = ReverseBlockPaymentOrder(NewBlockPayments, LastTSPayment);
            String bpxJSON = gson.toJson(RevBlockPayments);
            //Log.d(WORKERTAG, "BPX JSON: " + bpxJSON);
            FILE.writeToFile(bpxJSON, super.getApplicationContext());
        }
        else {
            String bpxJSON = gson.toJson(AllBPXPayments);
            //Log.d(WORKERTAG, "ALL BPX JSON: " + bpxJSON);
            FILE.writeToFile(bpxJSON, super.getApplicationContext());
        }
    }

    //LIFO
    private ArrayList<HashMap<String, String>> ReverseBlockPaymentOrder( ArrayList<HashMap<String, String>> NewBlockPayments, Long ts) {
        ArrayList<HashMap<String, String>> RevBlockPayments = new ArrayList<HashMap<String, String>>();
        HashMap<String, String> prevBlock = new HashMap<>();
        ListIterator<HashMap<String, String>> bpxIterator = NewBlockPayments.listIterator(NewBlockPayments.size());
        Log.d(WORKERTAG, "BlockPayment Size: " + NewBlockPayments.size());
        while (bpxIterator.hasPrevious()) {
            prevBlock = bpxIterator.previous();
            if (Long.parseLong(prevBlock.get("ts")) > ts) {
                RevBlockPayments.add(prevBlock);
            }
            //Log.d(WORKERTAG, "BlocK #: "+ bpxIterator.previousIndex());
        }
        return RevBlockPayments;
    }

}
