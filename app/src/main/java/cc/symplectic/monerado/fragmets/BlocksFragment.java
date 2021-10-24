package cc.symplectic.monerado.fragmets;

import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.fragment.app.FragmentManager;
import java.math.BigDecimal;
import java.math.MathContext;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Objects;

import cc.symplectic.monerado.R;

public class BlocksFragment extends Fragment {
    private ArrayList<HashMap<String, String>> BlockPayments = new ArrayList<>();
    public final static  String TAG = "BF";

    public BlocksFragment() {}

    public BlocksFragment(ArrayList<HashMap<String, String>> BlockPayments) {
        this.BlockPayments = BlockPayments;
        //this.TotalBlockPayment = TotalBlockPayment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        // Defines the xml file for the fragment
        return inflater.inflate(R.layout.fragment_payments_list, parent, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        MathContext mc = new MathContext(12);
        SimpleDateFormat DateFor = new SimpleDateFormat("EEE, d MMM yyyy hh:mm:ss aaa");
        ArrayList<HashMap<String, String>> ConvertedBlockPayments = new ArrayList<>();
        ImageView paymentImg = view.findViewById(R.id.imageView3);
        paymentImg.setImageResource(R.drawable.blockpayments);
        BigDecimal TotalAmtFromBlocks = new BigDecimal(0);
        TextView blockMessage;
        //TextView lastBlockTV = view.findViewById(R.id.tv_blockcount);
        //lastBlockTV.setVisibility(View.VISIBLE);
        //lastBlockTV = view.findViewById(R.id.tv_totalpaid);
        //lastBlockTV.setText((TotalBlockPayment));
        Long ts, ts_found;
        Date d;

        onBackKeyPressed(view);

        // Necessary conversions for OLDLY stored data
        try {
            for (HashMap<String, String> element : BlockPayments) {
                try {
                    d = new Date((long) (Long.parseLong(element.get("ts")) * 1000));
                    element.put("ts", DateFor.format(d));
                    d = new Date((long) (Long.parseLong(element.get("ts_found")) * 1000));
                    element.put("ts_found", DateFor.format(d));
                    element.put("value_percent", element.get("value_percent").replace("%", "") + "%");
                    element.put("value", element.get("value").replace(" XMR", "") + " XMR");
                } catch (Exception e) {
                    Log.w(TAG, "Exception Error: " + e.getMessage());
                }
                ConvertedBlockPayments.add(element);
            }
        }
        catch (NullPointerException e) {
            Toast.makeText(view.getContext(), "SOME ERROR OCCURED", Toast.LENGTH_SHORT).show();
            Log.w("BF", "NO BLOCK FRAGMENTS FOUND");
        }

        ListView paymentsList = view.findViewById(R.id.paymentsList);
        SimpleAdapter adapter = new SimpleAdapter(
                getActivity(),
                ConvertedBlockPayments,
                R.layout.custom_block_payment_row,
                new String[] {"ts", "ts_found", "value_percent", "value"},
                new int[] { R.id.tv_Date, R.id.tv_datefound, R.id.tv_pct, R.id.tv_xmr});
        paymentsList.setAdapter(adapter);

        try {
            for (HashMap<String, String> block : BlockPayments) {
                String value = block.get("value");
                value = value.replace(" XMR", "");
                BigDecimal TrueValue = new BigDecimal(value);
                //Log.d(TAG, "Value: " + String.valueOf(TrueValue));
                TotalAmtFromBlocks = TotalAmtFromBlocks.add(TrueValue);
                //Log.d(TAG, "Total Amt: " + String.valueOf(TotalAmtFromBlocks));
            }
        }
        catch (NullPointerException e) {
            Toast.makeText(view.getContext(), "SOME ERROR OCCURED", Toast.LENGTH_SHORT).show();
            Log.w("BF", "NO BLOCK FRAGMENTS FOUND");
        }
        TotalAmtFromBlocks = TotalAmtFromBlocks.round(mc);
        blockMessage = view.findViewById(R.id.tv_totalpaid);
        blockMessage.setText(String.valueOf(TotalAmtFromBlocks) + " XMR");

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
                    BlockPaymentFragment.fragmentran = false;
                    getParentFragmentManager().popBackStack("PAYMENT_PERIOD_BLOCK", FragmentManager.POP_BACK_STACK_INCLUSIVE);
                    Fragment frg = new BlockPaymentFragment();
                    getParentFragmentManager()
                    .beginTransaction()
                    .detach(frg)
                    .attach(frg)
                    .commit();
                }
                return false;
            }
        } );
    }

}
