package cc.symplectic.monerado.fragmets;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;

import cc.symplectic.monerado.R;

public class PaymentsListFragment extends Fragment {
    ArrayList<HashMap<String, String>> PaymentInfos = new ArrayList<HashMap<String, String>>();

    public PaymentsListFragment() {}
    public PaymentsListFragment(ArrayList<HashMap<String, String>> PaymentInfos) {
        this.PaymentInfos = PaymentInfos;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        // Defines the xml file for the fragment
        return inflater.inflate(R.layout.fragment_payments_list, parent, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        HashMap<String, String> obj = new HashMap<>();
        BigDecimal TotalXMR = new BigDecimal("000000000000");
        TextView TotalPaidTV = view.findViewById(R.id.tv_totalpaid);
        for (int k=0; k < PaymentInfos.size(); k++) {
            obj = PaymentInfos.get(k);
            //Log.d("PayFrag", "Date: " +  obj.get("date") + "      XMR: " + obj.get("xmr"));
            BigDecimal xmr = new BigDecimal(obj.get("xmr"));
            TotalXMR = TotalXMR.add(xmr);
        }

        ListView paymentsList = view.findViewById(R.id.paymentsList);
        SimpleAdapter adapter = new SimpleAdapter(
                getActivity(),
                PaymentInfos,
                R.layout.custom_payment_row,
                new String[] {"date", "xmr"},
                new int[] { R.id.tv_Date, R.id.tv_xmr});
        paymentsList.setAdapter(adapter);

        TotalPaidTV.setText(String.valueOf(TotalXMR));


    }
}
