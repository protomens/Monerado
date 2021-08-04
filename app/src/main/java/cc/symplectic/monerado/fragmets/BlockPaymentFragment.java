package cc.symplectic.monerado.fragmets;

import android.media.Image;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;

import cc.symplectic.monerado.MainActivity;
import cc.symplectic.monerado.R;
import cc.symplectic.monerado.ReadWriteGUID;

public class BlockPaymentFragment extends Fragment {
    private ArrayList<HashMap<String, String>> BlockPayments;
    private String TotalBlockPayment;
    public static  Boolean fragmentran = false;

    private String PaymentURL = "https://api.moneroocean.stream/miner/" + MainActivity.MOADDY + "/stats";
    private String PaymentsURL = "https://api.moneroocean.stream/miner/" + MainActivity.MOADDY + "/payments";
    private final String BLOCKPAYMENTFILE = "bpx-";
    private final String TAG = "BPF";

    private ArrayList<HashMap<String, String>> PaymentInfos = new ArrayList<>();
    private LinkedHashSet<ArrayList<HashMap<String, String>>> AllBlocksPaymentPeriods = new LinkedHashSet<>();

    public BlockPaymentFragment() { }

    /*
    public BlockPaymentFragment(ArrayList<HashMap<String, String>> BlockPayments, String TotalBlockPayment) {
        this.BlockPayments = BlockPayments;
        this.TotalBlockPayment = TotalBlockPayment;
    }

     */

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        // Defines the xml file for the fragment
        return inflater.inflate(R.layout.fragment_payments_list, parent, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        ImageView paymentImg = view.findViewById(R.id.imageView3);
        paymentImg.setImageResource(R.drawable.blockpayments);

        TextView blockMessage = view.findViewById(R.id.tv_blockcount);
        blockMessage.setText("Click on a Payment to see the block rewards for that period.");
        blockMessage.setVisibility(View.VISIBLE);

        GetInfos(PaymentURL, view, false);
        /*
        TextView lastBlockTV = view.findViewById(R.id.tv_blockcount);
        lastBlockTV.setVisibility(View.VISIBLE);
        lastBlockTV = view.findViewById(R.id.tv_totalpaid);
        lastBlockTV.setText((TotalBlockPayment));
        ListView paymentsList = view.findViewById(R.id.paymentsList);
        SimpleAdapter adapter = new SimpleAdapter(
                getActivity(),
                BlockPayments,
                R.layout.custom_block_payment_row,
                new String[] {"ts", "ts_found", "value_percent", "value"},
                new int[] { R.id.tv_Date, R.id.tv_datefound, R.id.tv_pct, R.id.tv_xmr});
        paymentsList.setAdapter(adapter);
    */
    }

    private void GetInfos(String URL, View view, Boolean payments) {

        StringRequest request = new StringRequest(URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String string) {
                try {
                    if (payments) {
                        PaymentInfos = PaymentFragment.parseJsonData(string, false);
                        SetupBlockView(view);
                    }
                    else {
                        JSONObject object = new JSONObject(string);
                        String PayCount = object.getString("txnCount");
                        Integer currentTXN = Integer.parseInt(PayCount);
                        currentTXN++;
                        ReadBlockFiles(currentTXN);
                        GetInfos(PaymentsURL, view, true);
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                Log.w("BPF", "Error in retrieving Data");
            }
        });

        RequestQueue rQueue = Volley.newRequestQueue(getContext());
        rQueue.add(request);
    }

    private void ReadBlockFiles(Integer txn) throws JSONException {
        ArrayList<HashMap<String, String>> bpx_payments = new ArrayList<HashMap<String, String>>();
        for (int i=txn; i > 0; i--) {
            File file = new File(getContext().getFilesDir().getPath() + "/" + BLOCKPAYMENTFILE + String.valueOf(i) + ".json");
            ReadWriteGUID CTX_FILE = new ReadWriteGUID(BLOCKPAYMENTFILE + String.valueOf(i) + ".json");

            if (file.exists()) {
                String bpx_string = CTX_FILE.readFromFile(getContext());
                bpx_payments = MenuFragment.parseBlockPayments(bpx_string, false);
                AllBlocksPaymentPeriods.add(bpx_payments);
            }
            else {
                Log.w(TAG, "No such File: bpx-" + String.valueOf(i) + ".json! Exiting...");
                if (i==txn) {
                    AllBlocksPaymentPeriods.add(null);
                }
            }
        }


    }

    private void SetupBlockView(View view) {
        //TextView lastBlockTV = view.findViewById(R.id.tv_blockcount);
        //lastBlockTV.setVisibility(View.VISIBLE);
        //lastBlockTV = view.findViewById(R.id.tv_totalpaid);
        //lastBlockTV.setText((TotalBlockPayment));
        ImageView currentPaymentIcon = view.findViewById(R.id.imageViewCurrentPayment);
        currentPaymentIcon.setVisibility(View.VISIBLE);
        currentPaymentIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Fragment fragment = new BlocksFragment(AllBlocksPaymentPeriods.iterator().next());
                    RunFragment(fragment, "PAYMENT_PERIOD_BLOCKS");
                    fragmentran = true;
                }
                catch (IndexOutOfBoundsException e) {
                    Log.w(TAG, "No block paymnets on file for that period");
                    Toast.makeText(view.getContext(), "No Block Payments on file for that payment period.", Toast.LENGTH_LONG);
                }
            }
        });
        ListView paymentsList = view.findViewById(R.id.paymentsList);
        SimpleAdapter adapter = new SimpleAdapter(
                getActivity(),
                PaymentInfos,
                R.layout.custom_payment_row,
                new String[] {"date", "xmr"},
                new int[] { R.id.tv_Date, R.id.tv_xmr});
        paymentsList.setAdapter(adapter);
        paymentsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view2, int position, long id) {
                try {
                    int index = -1;
                    for (ArrayList<HashMap<String, String>> item : AllBlocksPaymentPeriods) {
                        Log.d(TAG, "Position in list: " + position);
                        if (index == position) {
                            Fragment fragment = new BlocksFragment(item);
                            AllBlocksPaymentPeriods = new LinkedHashSet<>();
                            RunFragment(fragment, "PAYMENT_PERIOD_BLOCKS");
                            fragmentran = true;
                            break;
                        }
                        index++;
                    }
                    if (! fragmentran) {
                        Log.w(TAG, "No Block Payments file for that payment period.");
                        Toast.makeText(view.getContext(), "No Block Payments on file for that payment period", Toast.LENGTH_LONG).show();
                    }
                } catch (IndexOutOfBoundsException e) {
                    e.printStackTrace();
                    Log.w(TAG, "No Block Payments file for that payment period.");
                    Toast.makeText(view.getContext(), "No Block Payments on file for that payment period", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void RunFragment(Fragment fragment, String TAG) {
        getParentFragmentManager().beginTransaction()
                .setCustomAnimations(
                        R.anim.slide_in,  // enter
                        R.anim.fade_out,  // exit
                        R.anim.fade_in,   // popEnter
                        R.anim.slide_out  // popExit
                )
                .replace(R.id.monerado_main_frame, fragment, BlocksFragment.TAG)
                .addToBackStack(TAG)
                .commit();

    }
}
