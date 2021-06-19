package cc.symplectic.monerado.fragmets;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.fragment.app.Fragment;

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

        EditText MoAddyET = (EditText) view.findViewById(R.id.et_MOADDY);

        MainActivity.MOADDY = MoAddyET.getEditableText().toString();
        ReadWriteGUID moaddyfile = new ReadWriteGUID("moaddy.pls");
        moaddyfile.writeToFile(MainActivity.MOADDY, view.getContext());

        Fragment fragment = new MenuFragment(MainActivity.MainMenu);
        RunFragment(fragment);
        dialog.dismiss();

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
