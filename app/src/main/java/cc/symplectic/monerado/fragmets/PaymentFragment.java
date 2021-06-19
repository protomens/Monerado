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
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONException;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Locale;

import cc.symplectic.monerado.R;

public class PaymentFragment extends Fragment {
    HashMap<String, String> PaymentInfos = new HashMap<String,String>();
    BigDecimal Satoshi = new BigDecimal("1000000000000");

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

        dialog = new ProgressDialog(view.getContext());
        dialog.setMessage("Loading....");
        dialog.show();

        DecimalFormat df = new DecimalFormat("###,###.###");

        TextView Worker;

        String Hash = this.PaymentInfos.get("RawHash");

        if (Double.parseDouble(Hash) > 1000) {
            Hash2 = Double.parseDouble(Hash) / (double) 1000;
        }
        else { Hash2 = Double.valueOf(Hash); }
        format.format(Hash2);
        Worker = view.findViewById(R.id.tv_HashRateRaw);
        Worker.setText(String.valueOf(df.format(Hash2)));
        Hash = this.PaymentInfos.get("PayHash");
        if (Double.parseDouble(Hash) > 1000) {
            Hash2 = Double.parseDouble(Hash) / (double) 1000;
        }
        else { Hash2 = Double.valueOf(Hash); }
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
}
