package cc.symplectic.monerado.fragmets;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import java.util.ArrayList;
import java.util.HashMap;

import cc.symplectic.monerado.R;

public class BlockPaymentFragment extends Fragment {
    ArrayList<HashMap<String, String>> BlockPayments;
    String TotalBlockPayment;

    public BlockPaymentFragment(ArrayList<HashMap<String, String>> BlockPayments, String TotalBlockPayment) {
        this.BlockPayments = BlockPayments;
        this.TotalBlockPayment = TotalBlockPayment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        // Defines the xml file for the fragment
        return inflater.inflate(R.layout.fragment_payments_list, parent, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        ImageView paymentImg = view.findViewById(R.id.imageView3);
        paymentImg.setImageResource(R.drawable.blockpayments);
        TextView lastBlockTV = view.findViewById(R.id.tv_blockcount);
        lastBlockTV.setVisibility(View.VISIBLE);
        lastBlockTV = view.findViewById(R.id.tv_totalpaid);
        lastBlockTV.setText((TotalBlockPayment));
        ListView paymentsList = view.findViewById(R.id.paymentsList);
        SimpleAdapter adapter = new SimpleAdapter(
                getActivity(),
                BlockPayments,
                R.layout.custom_block_payment_row,
                new String[] {"date", "datefound", "pct", "xmr"},
                new int[] { R.id.tv_Date, R.id.tv_datefound, R.id.tv_pct, R.id.tv_xmr});
        paymentsList.setAdapter(adapter);

    }
}
