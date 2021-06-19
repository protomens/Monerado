package cc.symplectic.monerado;


import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.View;
import android.view.Menu;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.navigation.NavigationView;
import com.google.gson.Gson;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;

import cc.symplectic.monerado.databinding.ActivityMainBinding;
import cc.symplectic.monerado.fragmets.MenuFragment;
import cc.symplectic.monerado.fragmets.PoolListFragment;
import cc.symplectic.monerado.fragmets.StartupFragment;
import cc.symplectic.monerado.fragmets.WorkerFragment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration mAppBarConfiguration;
    private ActivityMainBinding binding;
    //ProgressDialog dialog;
    public static ArrayList<String> MainMenu = new ArrayList<>();
    public static String MOADDY;


    //HashMap<String, RemrigWorker> WorkersRemrig = new HashMap<String, RemrigWorker>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.appBarMain.toolbar);
        binding.appBarMain.fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addPool(view);
                //Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                //        .setAction("Action", null).show();
            }
        });


        DrawerLayout drawer = binding.drawerLayout;
        NavigationView navigationView = binding.navView;
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_gallery, R.id.nav_slideshow)
                .setDrawerLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);
        // Begin the transaction

        MainMenu.add("General & Payment Info");
        MainMenu.add("Worker Stats");
        MainMenu.add("Remrig");
        //MainMenu.add("Genral Pool Info");
        //MainMenu.add("Genral Pool Stats");

        ReadWriteGUID poolfiles = new ReadWriteGUID("pools.txt");
        String pools = poolfiles.readFromFile(getApplicationContext());

        ReadWriteGUID workerRemrigs = new ReadWriteGUID("remrigs.json");
        String remrigs = workerRemrigs.readFromFile(getApplicationContext());

        try { parseRemrigJSONFile(remrigs); }
        catch (JSONException e) { e.printStackTrace();}


        ReadWriteGUID moaddyfile = new ReadWriteGUID("moaddy.pls");
        MOADDY = moaddyfile.readFromFile(getApplicationContext());
        if (MOADDY.isEmpty()) {
            Fragment fragment = new StartupFragment();
            RunFragment(fragment);
        }
        else {
            Fragment fragment = new MenuFragment(MainMenu);
            RunFragment(fragment);
        }







/*
        String jsonWorkers = "{\"global\":{\"lts\":1622999209,\"identifer\":\"global\",\"hash\":11571.02724995931,\"hash2\":9519.358333333334,\"totalHash\":52354896461.504,\"validShares\":575636,\"invalidShares\":10},\"Sisyphus\":{\"lts\":1622999166,\"identifer\":\"Sisyphus\",\"hash\":4258.111302083334,\"hash2\":2478.3566666666666,\"totalHash\":18900368372.71924,\"validShares\":118387,\"invalidShares\":2},\"Sisyphus 2\":{\"lts\":1622999197,\"identifer\":\"Sisyphus 2\",\"hash\":1792.555625,\"hash2\":1653.6666666666667,\"totalHash\":5660132155.131348,\"validShares\":128596,\"invalidShares\":0},\"Sisyphus 3\":{\"lts\":1622999127,\"identifer\":\"Sisyphus 3\",\"hash\":2912.313463541667,\"hash2\":2199.8216666666667,\"totalHash\":9890909287.618652,\"validShares\":128402,\"invalidShares\":0},\"Sisyphus 5\":{\"lts\":1622999173,\"identifer\":\"Sisyphus 5\",\"hash\":2521.575208333333,\"hash2\":1905.7416666666666,\"totalHash\":7452058264.650391,\"validShares\":100773,\"invalidShares\":0},\"Sisyphus Pixel\":{\"lts\":1622999114,\"identifer\":\"Sisyphus Pixel\",\"hash\":11.836880289713541,\"hash2\":205.005,\"totalHash\":10477517.74118042,\"validShares\":13249,\"invalidShares\":0},\"Sisyphus Pixel 3a\":{\"lts\":1622999059,\"identifer\":\"Sisyphus Pixel 3a\",\"hash\":59.13292805989583,\"hash2\":1028.68,\"totalHash\":6425297.545654297,\"validShares\":4965,\"invalidShares\":0},\"Sisyphus Ryzen\":{\"lts\":1622998568,\"identifer\":\"Sisyphus Ryzen\",\"hash\":30.980504557291667,\"hash2\":511.8466666666667,\"totalHash\":10105827009.134277,\"validShares\":41777,\"invalidShares\":0},\"web_miner\":{\"lts\":1622999209,\"identifer\":\"web_miner\",\"hash\":15.501842651367188,\"hash2\":48.086666666666666,\"totalHash\":10009929.22151184,\"validShares\":22468,\"invalidShares\":7}}";


        parseJsonData(jsonWorkers);
        */

    }

    private void parseRemrigJSONFile(String jsonString) throws JSONException {

        JSONObject object = new JSONObject(jsonString);

        ArrayList al = new ArrayList();
        ArrayList<JSONObject> workerObjects = new ArrayList<>();

        @SuppressWarnings("unchecked")
        Iterator<String> keys = (Iterator<String>) object.keys();
        while (keys.hasNext()) {
            String key = keys.next();
            al.add(key);
            workerObjects.add(object.getJSONObject(key));
        }
        int k=0;
        for (JSONObject obj : workerObjects) {
            RemrigWorker remrig = new RemrigWorker(obj.getString("URL"), obj.getString("Username"), obj.getString("Password"), obj.getBoolean("state"));
            SaveRemrigJSON(remrig, al.get(k).toString());
            k++;
        }

    }




    public void SaveRemrigJSON(RemrigWorker remrig, String WorkerName) {
        HashMap<String, RemrigWorker> Moneradoremrig = new HashMap<String, RemrigWorker>();


        //Monerado monerado = Monerado.getInstance();
        //Moneradoremrig = monerado.getWorkersRemrig();
        Moneradoremrig.put(WorkerName, remrig);
        //monerado.setWorkersRemrig(Moneradoremrig);

        ReadWriteGUID remrigFILE = new ReadWriteGUID("remrigs.json");
        Gson gson = new Gson();
        String remrigJSON = gson.toJson(Moneradoremrig);

        remrigFILE.writeToFile(remrigJSON, this);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    private void RunFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
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
        getSupportFragmentManager().beginTransaction()
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