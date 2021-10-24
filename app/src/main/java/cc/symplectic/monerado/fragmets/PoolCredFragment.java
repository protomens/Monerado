package cc.symplectic.monerado.fragmets;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import cc.symplectic.monerado.R;

public class PoolCredFragment extends Fragment {

    String pool;

    public PoolCredFragment() {}
    public PoolCredFragment(String pool)
    {
        this.pool = pool;
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        // Defines the xml file for the fragment
        return inflater.inflate(R.layout.fragment_poolcred, parent, false);
    }

}
