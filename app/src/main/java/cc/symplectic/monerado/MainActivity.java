package cc.symplectic.monerado;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Menu;

import com.google.android.material.navigation.NavigationView;
import com.google.common.util.concurrent.ListenableFuture;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.work.BackoffPolicy;
import androidx.work.Constraints;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.ExistingWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.Operation;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkContinuation;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;
import androidx.work.WorkQuery;
import androidx.work.WorkRequest;

import org.jetbrains.annotations.NotNull;

import cc.symplectic.monerado.databinding.ActivityMainBinding;
import cc.symplectic.monerado.fragmets.MenuFragment;
import cc.symplectic.monerado.fragmets.PoolListFragment;
import cc.symplectic.monerado.fragmets.StartupFragment;

import java.util.ArrayList;

import java.io.File;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;


public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration mAppBarConfiguration;
    private ActivityMainBinding binding;
    public static ArrayList<String> MainMenu = new ArrayList<>();
    public static String MOADDY;
    private final static String WORKER_STATUS = "WORKER_STATUS";
    private final static String WORKER_NAME = "WORKER_IDS_WORKER";
    public final static String NOTIFICATION_NAME = "workerStatus";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //String remrigs;

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.appBarMain.toolbar);

        /*
        binding.appBarMain.fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addPool(view);
                //Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                //        .setAction("Action", null).show();
            }
        });
*/

        DrawerLayout drawer = binding.drawerLayout;
        NavigationView navigationView = binding.navView;
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_mo, R.id.nav_slideshow)
                .setDrawerLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);
        // Begin the transaction

        createNotificationChannel();
        createWorker();

        MainMenu.add("General & Payment Info");
        MainMenu.add("Worker Stats");
        MainMenu.add("Block Payments");
        MainMenu.add("Remrig");
        //MainMenu.add("Genral Pool Info");
        //MainMenu.add("Genral Pool Stats");

        /* Will be needed in future releases
         ReadWriteGUID poolfiles = new ReadWriteGUID("pools.txt");
         String pools = poolfiles.readFromFile(getApplicationContext());
         */

        File file = new File(getApplicationContext().getFilesDir().getPath() + "/remrigs.json");
        File idfile = new File(getApplicationContext().getFilesDir().getPath() + "/identifiers.mo");
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

        // No need to read the file here.
        // Could be useful in a Global Variable environment.
        // Global variables might contain too much data slowing everything down and making
        // the app unstable. Best to read from remrigs.json each time it's needed.
        // Even better would be database operations. But that's a lot of code and
        // at this point not needed until future releases.
        //try { parseRemrigJSONFile(remrigs); }
        //catch (JSONException e) { e.printStackTrace();}


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

    private void createWorker() {


        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();

        PeriodicWorkRequest MinerIDs =
                new PeriodicWorkRequest.Builder(MinerIdentifiersWorker.class, 15, TimeUnit.MINUTES)
                        .setConstraints(constraints)
                        .setBackoffCriteria(BackoffPolicy.LINEAR, PeriodicWorkRequest.MIN_BACKOFF_MILLIS, TimeUnit.MILLISECONDS)
                        .addTag(WORKER_STATUS)
                        .build();

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(WORKER_NAME, ExistingPeriodicWorkPolicy.KEEP, MinerIDs);


    }

    // Will be useful in future releases

}