package cc.symplectic.monerado;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.material.navigation.NavigationView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.view.GravityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.work.BackoffPolicy;
import androidx.work.Constraints;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import cc.symplectic.monerado.databinding.ActivityMainBinding;
import cc.symplectic.monerado.fragmets.BlockPaymentFragment;
import cc.symplectic.monerado.fragmets.BlocksFragment;
import cc.symplectic.monerado.fragmets.MenuFragment;
import cc.symplectic.monerado.fragmets.StartupFragment;

import java.util.ArrayList;

import java.io.File;
import java.util.concurrent.TimeUnit;

import cc.symplectic.monerado.workers.BlockPaymentsWorker;
import cc.symplectic.monerado.workers.MinerIdentifiersWorker;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private AppBarConfiguration mAppBarConfiguration;
    private ActivityMainBinding binding;
    public static String MOADDY;
    private final String ID_WORKER_TAG = "IDWORKER1";
    private final String BLOCK_WORKER_TAG = "BLOCKWORKER1";
    private final String IDWORKERNAME = "ID_WORKER";
    private final String BLOCKWORKERNAME = "BLOCK_WORKER";
    public final static String NOTIFICATION_NAME = "workerStatus";
    public WorkManager minerWorker;
    public WorkManager blockWorker;
    public static String APIHOST = "https://api.moneroocean.stream/";

    public enum POOL { MO, C3};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Nav drawer setup
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.appBarMain.toolbar);


        DrawerLayout drawer = binding.drawerLayout;
        NavigationView navigationView = binding.navView;
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_mo, R.id.nav_c3)
                .setDrawerLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);
        setNavigationViewListener();

        // battery optimization white-listing
        // GitHub release only! Google will delist us if we use this code.
        /*

        ReadWriteGUID mBatteryFile = new ReadWriteGUID("batteryoptimization");
        File batfile = new File(getApplicationContext().getFilesDir().getPath() + "/batteryoptimization");
        int mBatOption;
        if (batfile.exists()) {
             mBatOption = Integer.parseInt(mBatteryFile.readFromFile(getApplicationContext()));
        }
        else {
            mBatteryFile.writeToFile("0", getApplicationContext());
            mBatOption = 0;
        }

        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
        {
            if (!pm.isIgnoringBatteryOptimizations(getPackageName()) && mBatOption != 2)
            {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);

                builder.setTitle("Battery Optimization");
                builder.setMessage("This app has background services to monitor block payments and miner workers. Please turn off battery optimization for Monerado to function as intended");
                builder.setPositiveButton("Let's Do it!", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Intent intent = new Intent();
                        //intent.setAction(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS);
                        intent.setAction(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                        intent.setData(Uri.parse("package:" + getPackageName()));
                        startActivity(intent);
                    }
                });
                builder.setNeutralButton("No. Don't ask again.", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mBatteryFile.writeToFile("2", getApplicationContext());
                        dialog.cancel();
                        dialog.cancel();
                    }
                });
                builder.setNegativeButton("Pass", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
                builder.show();

                //  Prompt the user to disable battery optimization
            }
        }

         */

        /* Will be needed in future releases
         ReadWriteGUID poolfiles = new ReadWriteGUID("pools.txt");
         String pools = poolfiles.readFromFile(getApplicationContext());
        */

        // Create Workers and Push Notifications
        createNotificationChannel();
        createWorkers();

        // Check Configs and Act Accordingly
        CheckIfOtherConfigsExist();
        ReadConfigAndDecide(null);

    }

    private void CheckIfOtherConfigsExist() {
        File file = new File(getApplicationContext().getFilesDir().getPath() + "/remrigs.json");
        File idfile = new File(getApplicationContext().getFilesDir().getPath() + "/identifiers.mo");
        File payfile = new File(getApplicationContext().getFilesDir().getPath() + "/avgpayhash.json");
        if (file.exists()) {
            Log.d("MA", "remrigs.json EXISTS!");
            //ReadWriteGUID workerRemrigs = new ReadWriteGUID("remrigs.json");
            //remrigs = workerRemrigs.readFromFile(getApplicationContext());
        }
        else {
            Log.d("MA", "remrigs.json does not exist! creating...");
            ReadWriteGUID workerRemrigs = new ReadWriteGUID("remrigs.json");
            workerRemrigs.writeToFile("{}", getApplicationContext());
            //remrigs = workerRemrigs.readFromFile(getApplicationContext());
        }

        if (idfile.exists()) {
            Log.d("MA", "Identifiers file EXISTS!");
        }
        else {
            Log.d("MA", "Identifiers FILE DOES NOT EXIST! Creating...");
            ReadWriteGUID identifiersFile = new ReadWriteGUID("identifiers.mo");
            identifiersFile.writeToFile("[]", getApplicationContext());
        }

        if(payfile.exists()) {
            Log.d("MA", "avgpayhash.json exists!");
        }
        else {
            Log.d("MA", "avgpayhash.json does not exist! creating...");
            ReadWriteGUID avgPayHash = new ReadWriteGUID("avgpayhash.json");
            avgPayHash.writeToFile("[]", getApplicationContext());
        }
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

    // Reusable code
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

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.channel_name);
            String description = getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(NOTIFICATION_NAME, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private void createWorkers() {

/*
        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();
*/
        PeriodicWorkRequest MinerIDs =
                new PeriodicWorkRequest.Builder(MinerIdentifiersWorker.class, 15, TimeUnit.MINUTES)
                        //.setConstraints(constraints)
                        .setBackoffCriteria(BackoffPolicy.LINEAR, PeriodicWorkRequest.MIN_BACKOFF_MILLIS, TimeUnit.MILLISECONDS)
                        .addTag(ID_WORKER_TAG)
                        .build();
        minerWorker = WorkManager.getInstance(getApplication());
        minerWorker.enqueueUniquePeriodicWork(IDWORKERNAME, ExistingPeriodicWorkPolicy.REPLACE, MinerIDs);
        //WorkManager.getInstance(this).enqueueUniquePeriodicWork(WORKER_NAME, ExistingPeriodicWorkPolicy.KEEP, MinerIDs);

        PeriodicWorkRequest BlockPayments =
                new PeriodicWorkRequest.Builder(BlockPaymentsWorker.class, 1, TimeUnit.HOURS)
                        //.setConstraints(constraints)

                        .setBackoffCriteria(BackoffPolicy.LINEAR, PeriodicWorkRequest.MIN_BACKOFF_MILLIS, TimeUnit.MILLISECONDS)
                        .addTag(BLOCK_WORKER_TAG)
                        .build();
        blockWorker = WorkManager.getInstance(getApplication());
        blockWorker.enqueueUniquePeriodicWork(BLOCKWORKERNAME, ExistingPeriodicWorkPolicy.REPLACE, BlockPayments);
    }

    private void ReadConfigAndDecide(@Nullable POOL MiningPool) {
        POOL configPool;
        ReadWriteGUID moaddyfile = new ReadWriteGUID("moaddy.pls");
        String PoolInfosJSON = moaddyfile.readFromFile(getApplicationContext());

        if (MiningPool != null) {
            switch (MiningPool) {
                case MO:
                    configPool = DeterminePool();
                    if (configPool == POOL.C3) {
                        ShowPoolAlert(POOL.C3);
                    }
                    break;
                case C3:
                    configPool = DeterminePool();
                    if (configPool == POOL.MO) {
                        ShowPoolAlert(POOL.MO);
                    }
                    break;
            }
            Fragment fragment = new MenuFragment();
            RunFragment(fragment);
            return;
        }

        if (PoolInfosJSON.isEmpty()) {
            Fragment fragment = new StartupFragment();
            RunFragment(fragment);
        }
        else {
            try {
                JSONObject poolobj = new JSONObject(PoolInfosJSON);
                String pool = poolobj.getString("pool");
                if (pool.equals("C3pool")) {
                    APIHOST = "https://api.c3pool.com/";

                }
                MOADDY = poolobj.getString("address");
            } catch (JSONException e) {
                MOADDY = PoolInfosJSON;
            }
            Fragment fragment = new MenuFragment();
            RunFragment(fragment);
        }
    }

    private POOL DeterminePool() {
        ReadWriteGUID moaddyfile = new ReadWriteGUID("moaddy.pls");
        String PoolInfosJSON = moaddyfile.readFromFile(getApplicationContext());

        try {
            JSONObject poolobj = new JSONObject(PoolInfosJSON);
            String pool = poolobj.getString("pool");
            if (pool.equals("C3pool")) {
                APIHOST = "https://api.c3pool.com/";
                MOADDY = poolobj.getString("address");
                return POOL.C3;
            }
            else {
                MOADDY = poolobj.getString("address");
                return POOL.MO;
            }

        } catch (JSONException e) {
            MOADDY = PoolInfosJSON;
            return POOL.MO;
        }

    }

    private void ShowPoolAlert(POOL pool) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle("Wrong Pool!");
        if (pool == POOL.MO) {
            builder.setMessage("You are currently configured for Monero Ocean and cannot view C3Pool pool stats. Pool stats for miners on both C3 and MO will be implemented in the near future.");
        }
        else {
            builder.setMessage("You are currently configured for C3Pool and cannot view Monero Ocean pool stats. Pool stats for miners on both C3 and MO will be implemented in the near future.");
        }
        builder.setPositiveButton("Close", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });
        builder.show();
    }

    // Extremely ugly code.
    // Will be used when we allow stats for miners mining on C3 & MO simultaneously.
    /*
    private void GetMOAddress(String json)  {
        String pool;
        JSONObject obj;
        try {
             obj = new JSONObject(json);
        }
        catch (JSONException e) { Log.w("MA", "JSON READ ERROR"); MOADDY = json; return; }
        try {
            pool = obj.getString("pool");
            if (! pool.equals("MoneroOcean")) {
                try {
                    pool = obj.getString("pool2");
                    if (pool.equals("MoneroOcean")) { MOADDY = obj.getString("address2"); }
                }
                catch (JSONException e) { Log.w("MA", "ERROR READING JSON. ONLY 1 POOL"); }
            }
        }
        catch (JSONException e) { Log.d("MA", "OLD POOL CONFIG FILE IN PLACE. SETTING XMR ADDRESS (MOADDY)"); MOADDY = json; }
    }

    private void GetC3Address(String json) {
        String pool;
        JSONObject obj;
        try {
            obj = new JSONObject(json);
        }
        catch (JSONException e) { Log.w("MA", "JSON READ ERROR"); MOADDY = json; return; }
        try {
            pool = obj.getString("pool");
            if (! pool.equals("C3pool")) {
                try {
                    pool = obj.getString("pool2");
                    if (pool.equals("C3pool")) { MOADDY = obj.getString("address2"); }

                }
                catch (JSONException e) { Log.w("MA", "ERROR READING JSON. ONLY 1 POOL."); }
            }
        }
        catch (JSONException e) { Log.d("MA", "OLD POOL CONFIG FILE IN PLACE. SETTING XMR ADDRESS (MOADDY)"); MOADDY = json; }
    }

     */

    @Override
    public boolean onNavigationItemSelected(@NonNull @NotNull MenuItem item) {
        // Handle navigation view item clicks here.
        switch (item.getItemId()) {

            case R.id.nav_mo: {
                CloseDrawer();
                ReadConfigAndDecide(POOL.MO);
                break;
            }
            case R.id.nav_c3: {
                CloseDrawer();
                ReadConfigAndDecide(POOL.C3);
                break;
            }
            case R.id.nav_home: {
                CloseDrawer();
                Fragment fragment = new MenuFragment();
                RunFragment(fragment);
                break;
            }
        }
        return true;
    }

    private void CloseDrawer() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);

    }

    private void setNavigationViewListener() {
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }


}