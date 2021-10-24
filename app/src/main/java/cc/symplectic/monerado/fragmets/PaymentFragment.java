package cc.symplectic.monerado.fragmets;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.ImageView;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

import cc.symplectic.monerado.MainActivity;
import cc.symplectic.monerado.R;
import cc.symplectic.monerado.WorkerNameObj;

public class PaymentFragment extends Fragment {
    HashMap<String, String> PaymentInfos = new HashMap<String, String>();
    public static BigDecimal Satoshi = new BigDecimal("1000000000000");
    ProgressDialog dialog;
    String PaymentsURL = MainActivity.APIHOST + "miner/" + MainActivity.MOADDY + "/payments";

    public PaymentFragment() {}

    public PaymentFragment(HashMap<String, String> PaymentInfos) {
        this.PaymentInfos = PaymentInfos;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        // Defines the xml file for the fragment
        return inflater.inflate(R.layout.fragment_payment, parent, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {

        Double Hash2;
        ProgressDialog dialog;
        NumberFormat format = NumberFormat.getInstance(Locale.US);

        addBackFragListener(view);
        addViewPaymentsOnClickListener(view);

        dialog = new ProgressDialog(view.getContext());
        dialog.setMessage("Loading....");
        dialog.show();

        DecimalFormat df = new DecimalFormat("###,###.###");

        TextView Worker;

        String Hash = this.PaymentInfos.get("RawHash");

        if (Double.parseDouble(Hash) > 1000) {
            Hash2 = Double.parseDouble(Hash) / (double) 1000;
        } else {
            Hash2 = Double.valueOf(Hash);
        }
        format.format(Hash2);
        Worker = view.findViewById(R.id.tv_HashRateRaw);
        Worker.setText(String.valueOf(df.format(Hash2)));
        Hash = this.PaymentInfos.get("PayHash");
        if (Double.parseDouble(Hash) > 1000) {
            Hash2 = Double.parseDouble(Hash) / (double) 1000;
        } else {
            Hash2 = Double.valueOf(Hash);
        }
        Worker = view.findViewById(R.id.tv_HashRatePay);
        Worker.setText(String.valueOf(df.format(Hash2)));
        Hash = this.PaymentInfos.get("TotalHash");
        Worker = view.findViewById(R.id.tv_totalhash);
        Worker.setText(String.valueOf(df.format(Double.parseDouble(Hash))));
        Hash = this.PaymentInfos.get("Vshares");
        Worker = view.findViewById(R.id.tv_validshares);
        Worker.setText(String.valueOf(df.format(Double.parseDouble(Hash))));
        Hash = this.PaymentInfos.get("Ishares");
        Worker = view.findViewById(R.id.tv_invalidshares);
        Worker.setText(String.valueOf(df.format(Double.parseDouble(Hash))));
        String Paid = this.PaymentInfos.get("AmtPaid");
        BigDecimal XMR = new BigDecimal(Paid);
        XMR = XMR.divide(Satoshi);
        String SXMR = String.valueOf(XMR);
        Worker = view.findViewById(R.id.tv_amtpaid);
        Worker.setText(SXMR);
        Paid = this.PaymentInfos.get("AmtDue");
        XMR = new BigDecimal(Paid);
        XMR = XMR.divide(Satoshi);
        SXMR = String.valueOf(XMR);
        Worker = view.findViewById(R.id.tv_amtdue);
        Worker.setText(SXMR);
        Hash = this.PaymentInfos.get("PayCount");
        Worker = view.findViewById(R.id.tv_txcount);
        Worker.setText(String.valueOf(df.format(Double.parseDouble(Hash))));


        dialog.dismiss();
    }

    public void addViewPaymentsOnClickListener(View view) {
        ImageView viewPayments = view.findViewById(R.id.imageViewPayments);

        viewPayments.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GetInfos(PaymentsURL);

            }
        });
    }

    public void addBackFragListener(View view) {
        FloatingActionButton button = view.findViewById(R.id.floatingActionBackButton);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager fm = getParentFragmentManager();
                if (fm.getBackStackEntryCount() > 0) {
                    Log.i("StatsFrag", "popping backstack");
                    fm.popBackStack();
                } else {
                    Log.i("StatsFrag", "nothing on backstack, calling super");
                }
            }
        });

    }

    private void GetInfos(String url) {
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
                    ArrayList<HashMap<String, String>> GettingPaid = new ArrayList<HashMap<String, String>>();
                    GettingPaid = parseJsonData(string, false);
                    Fragment fragment = new PaymentsListFragment(GettingPaid);
                    RunFragment(fragment);
                    dialog.dismiss();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
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

    public static ArrayList<HashMap<String, String>> parseJsonData(String jsonString, Boolean BlockWorker) throws JSONException {

        JSONArray array = new JSONArray(jsonString);
        //ArrayList<String> TimeStamps = new ArrayList<>();
        //ArrayList<String> AmtPaid = new ArrayList<>();
        ArrayList<HashMap<String, String>> GettingPaid = new ArrayList<HashMap<String, String>>();
        DecimalFormat df = new DecimalFormat("###.############");
        df.setMinimumFractionDigits(12);

        String Paid;

        int len_array = array.length();
        if (BlockWorker) {
            HashMap<String,String> TXDateAmt = new HashMap<>();
            JSONObject obj = array.getJSONObject(0);
            //TimeStamps.add();
            BigDecimal XMR = new BigDecimal(obj.getString("amount"));
            XMR = XMR.divide(PaymentFragment.Satoshi);
            //AmtPaid.add();
            //Timestamp ts = new Timestamp(Long.parseLong(obj.getString("ts")));
            //Date d = new Date((long)(Long.parseLong(obj.getString("ts"))*1000));
            //SimpleDateFormat DateFor = new SimpleDateFormat("EEE, d MMM yyyy hh:mm:ss aaa");
            //String dateString = DateFor.format(d);
            TXDateAmt.put("date", obj.getString("ts"));
            TXDateAmt.put("xmr", df.format(XMR));
            //Log.d("Pay", "Date: " + dateString + " XMR: " + String.valueOf(XMR));
            GettingPaid.add(TXDateAmt);
            TXDateAmt = null;
        }
        else {
            for (int k = 0; k < len_array; k++) {
                HashMap<String, String> TXDateAmt = new HashMap<>();
                JSONObject obj = array.getJSONObject(k);
                //TimeStamps.add();
                BigDecimal XMR = new BigDecimal(obj.getString("amount"));
                XMR = XMR.divide(PaymentFragment.Satoshi);
                //AmtPaid.add();
                //Timestamp ts = new Timestamp(Long.parseLong(obj.getString("ts")));
                Date d = new Date((long) (Long.parseLong(obj.getString("ts")) * 1000));
                SimpleDateFormat DateFor = new SimpleDateFormat("EEE, d MMM yyyy hh:mm:ss aaa");
                String dateString = DateFor.format(d);
                TXDateAmt.put("date", dateString);
                TXDateAmt.put("xmr", df.format(XMR));
                //Log.d("Pay", "Date: " + dateString + " XMR: " + String.valueOf(XMR));
                GettingPaid.add(TXDateAmt);
                TXDateAmt = null;
            }
        }
        return GettingPaid;

    }
    private void RunFragment(Fragment fragment) {
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

    }

}
