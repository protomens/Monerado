package cc.symplectic.monerado.fragmets;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.fragment.app.Fragment;

import java.util.ArrayList;

import cc.symplectic.monerado.R;

public class PoolListFragment extends Fragment {
    ArrayList<String> PoolList;

    public PoolListFragment() {}

    public PoolListFragment(ArrayList<String> PoolList)
    {
        this.PoolList = PoolList;
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        // Defines the xml file for the fragment
        return inflater.inflate(R.layout.fragment_addpool, parent, false);
    }

    // This event is triggered soon after onCreateView().
    // Any view setup should occur here.  E.g., view lookups and attaching view listeners.
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        // Setup any handles to view objects here
        // EditText etFoo = (EditText) view.findViewById(R.id.etFoo);
        ProgressDialog dialog;
        ListView poolList = (ListView) view.findViewById(R.id.poolList);

        dialog = new ProgressDialog(getActivity());
        dialog.setMessage("Loading....");
        dialog.show();

        ArrayAdapter adapter = new ArrayAdapter(getActivity(), R.layout.list_white_text, PoolList);
        poolList.setAdapter(adapter);

        poolList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                // Replace the contents of the container with the new fragment
                Fragment fragment = new PoolCredFragment((String) PoolList.get(position));
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
        });
        dialog.dismiss();

    }
}
