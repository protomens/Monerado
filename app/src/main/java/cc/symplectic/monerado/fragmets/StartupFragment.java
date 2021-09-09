package cc.symplectic.monerado.fragmets;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;

import androidx.fragment.app.Fragment;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.HashMap;

import cc.symplectic.monerado.MainActivity;
import cc.symplectic.monerado.R;
import cc.symplectic.monerado.ReadWriteGUID;

public class StartupFragment extends Fragment {
    ProgressDialog dialog;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        // Defines the xml file for the fragment
        return inflater.inflate(R.layout.fragment_startup, parent, false);


    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        FloatingActionButton fab = view.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addPool(view);
                //Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                //        .setAction("Action", null).show();
            }
        });

        ImageButton moaddybtn = view.findViewById(R.id.imageButton);
        moaddybtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                writeMoAddy(view);
            }
        });


    }

    public void writeMoAddy(View view) {
        dialog = new ProgressDialog(view.getContext());
        dialog.setMessage("Loading....");
        dialog.show();
        HashMap<String, String> PoolInfos = new HashMap<>();

        EditText MoAddyET = (EditText) view.findViewById(R.id.et_MOADDY);
        Spinner PoolSpinner = view.findViewById(R.id.poolspinner);
        String Pool = PoolSpinner.getSelectedItem().toString();
        MainActivity.MOADDY = MoAddyET.getEditableText().toString();
        PoolInfos.put("pool", Pool);
        PoolInfos.put("address", MainActivity.MOADDY);
        if (Pool.equals("C3pool")) { MainActivity.APIHOST = "https://api.c3pool.com/"; }
        Gson gson = new Gson();
        String PoolInfosJSON = gson.toJson(PoolInfos);
        ReadWriteGUID moaddyfile = new ReadWriteGUID("moaddy.pls");
        moaddyfile.writeToFile(PoolInfosJSON, view.getContext());

        Fragment fragment = new MenuFragment();
        RunFragment(fragment);
        dialog.dismiss();

    }

    public void addPool (View view) {
        ArrayList<String> PoolList = new ArrayList<>();

        PoolList.add("Monero Ocean");
        PoolList.add("Minexmr");
        PoolList.add("SupportXMR");
        PoolList.add("XMRPool");
        PoolList.add("F2Pool");
        PoolList.add("Hashcity");
        PoolList.add("Hashvault");
        PoolList.add("MoneroHash");
        PoolList.add("Kryptex");
        PoolList.add("C3Pool");
        PoolList.add("XMRvsBeast");
        PoolList.add("null");
        PoolList.add("null");
        PoolList.add("null");
        PoolList.add("null");
        PoolList.add("null");
        PoolList.add("null");
        PoolList.add("null");
        PoolList.add("null");
        PoolList.add("null");
        PoolList.add("null");
        PoolList.add("null");
        PoolList.add("null");

        Fragment fragment = new PoolListFragment(PoolList);
        RunFragment(fragment);

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
