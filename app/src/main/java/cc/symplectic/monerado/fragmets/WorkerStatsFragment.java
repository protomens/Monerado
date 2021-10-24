package cc.symplectic.monerado.fragmets;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;


import com.anychart.APIlib;
import com.anychart.AnyChart;
import com.anychart.AnyChartView;
import com.anychart.chart.common.dataentry.DataEntry;
import com.anychart.chart.common.dataentry.ValueDataEntry;
import com.anychart.charts.Cartesian;
import com.anychart.core.axismarkers.Line;
import com.anychart.core.axismarkers.Text;
import com.anychart.core.cartesian.series.SplineArea;
import com.anychart.data.Mapping;
import com.anychart.data.Set;
import com.anychart.enums.Anchor;
import com.anychart.enums.HoverMode;
import com.anychart.enums.Position;
import com.anychart.enums.TooltipPositionMode;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;


import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import cc.symplectic.monerado.R;
import cc.symplectic.monerado.ReadWriteGUID;
import cc.symplectic.monerado.RemrigWorker;
import cc.symplectic.monerado.RetroRemrig;

import retrofit2.Call;
import retrofit2.Callback;

public class WorkerStatsFragment extends  Fragment {
    JSONObject workerObject;
    String WorkerName;
    List<DataEntry> seriesData = new ArrayList<>();
    ArrayList<Double> WorkerHashChart = new ArrayList<>();
    Double avgHR = 0.0;
    Boolean Remrig = true;
    String RemrigURL, RemrigUSER, RemrigPASS;

    public WorkerStatsFragment() {}

    public WorkerStatsFragment(JSONObject workerObject, String al, ArrayList<Double> WorkerHashChart) {
        this.workerObject = workerObject;
        this.WorkerName = al;
        this.WorkerHashChart = WorkerHashChart;
    }

    public WorkerStatsFragment(JSONObject workerObject, String al) {
        this.workerObject = workerObject;
        this.WorkerName = al;
        //this.WorkerHashChart = WorkerHashChart;
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        // Defines the xml file for the fragment
        return inflater.inflate(R.layout.fragment_worker, parent, false);


    }

   @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        Double Hash2;
        NumberFormat format = NumberFormat.getInstance(Locale.US);

        addBackFragListener(view);
        addRemrigListener(view);

        DecimalFormat df = new DecimalFormat("###,###.###");

        TextView Worker = view.findViewById(R.id.tv_WorkerName);
        Worker.setText(this.WorkerName);
        try {
            String Hash = this.workerObject.getString("hash");

            if (Double.parseDouble(Hash) > 1000) {
                Hash2 = Double.parseDouble(Hash) / (double) 1000;
            } else {
                Hash2 = Double.valueOf(Hash);
            }
            format.format(Hash2);
            Worker = view.findViewById(R.id.tv_HashRateRaw);
            Worker.setText(String.valueOf(df.format(Hash2)));
            Hash = this.workerObject.getString("hash2");
            if (Double.parseDouble(Hash) > 1000) {
                Hash2 = Double.parseDouble(Hash) / (double) 1000;
            } else {
                Hash2 = Double.valueOf(Hash);
            }
            Worker = view.findViewById(R.id.tv_HashRatePay);
            Worker.setText(String.valueOf(df.format(Hash2)));
            Hash = this.workerObject.getString("totalHash");
            Worker = view.findViewById(R.id.tv_totalhash);
            Worker.setText(String.valueOf(df.format(Double.parseDouble(Hash))));
            Hash = this.workerObject.getString("validShares");
            Worker = view.findViewById(R.id.tv_validshares);
            Worker.setText(String.valueOf(df.format(Double.parseDouble(Hash))));
            Hash = this.workerObject.getString("invalidShares");
            Worker = view.findViewById(R.id.tv_invalidshares);
            Worker.setText(String.valueOf(df.format(Double.parseDouble(Hash))));

        } catch (JSONException e) {
            e.printStackTrace();
        }

        SetupChartHashRate(WorkerHashChart, WorkerName, view);
        AddRemrigInfos(view);


    }

    public void SetupChartHashRate(List<Double> MatchSP1, String Worker, View view) {
        AnyChartView anyChartView = view.findViewById(R.id.any_chart_view);
        anyChartView.setProgressBar(view.findViewById(R.id.anyChartProgressBar));
        APIlib.getInstance().setActiveAnyChartView(anyChartView);

        Cartesian area3d = AnyChart.area();
        area3d.xAxis(0).labels().format("{%Value}");

        area3d.animation(true);



        area3d.xAxis(0).ticks(true);
        area3d.xAxis(0).minorTicks(false);
        area3d.xAxis(0).title("Time Elapsed (m)");
        area3d.xAxis(true);
        area3d.xGrid(false);
        area3d.title("Pay Hash Rate of:  " + Worker + " (20hrs)");
        area3d.title().fontColor("#FFB908");



        try {
            addSeriesData(MatchSP1);
        }
          catch (ArrayIndexOutOfBoundsException e) {
            Log.w("AnyChart", "No Data for worker: " + e.getMessage());
            return;
        }

        Line marker = area3d.lineMarker(0).value(avgHR);
        marker.stroke("#FFFFFF", 1.5, "2 12","bevel", "round");
        Text marker2 = area3d.textMarker(0).value(avgHR);

        DecimalFormat df = new DecimalFormat("###,###.00");
        marker2.text(String.valueOf(df.format(avgHR)));
        marker2.fontColor("#FFFFFF");


        Set set = Set.instantiate();
        set.data(seriesData);

        Mapping series1Data = set.mapAs("{ x: 'x', value: 'value' }");

        SplineArea series1 = area3d.splineArea(series1Data, "x,value");

        //Log.d("S1", "Series 2: " + series1.getPoint(5));

        series1.normal().fill("#FFB908", 0.3);
        series1.hovered().fill("#FFB908", 0.1);
        series1.selected().fill("#FFB908", 0.5);

        series1.hovered().markers(true);
        series1.normal().markers(true);
        series1.color("#FFB908");
        series1.markers(false);
        series1.minLabels(true);
        series1.maxLabels(true);
        series1.name("H/s");

        area3d.tooltip()
                .displayMode("Union")
                .position(Position.CENTER_TOP)
                .positionMode(TooltipPositionMode.POINT)
                .anchor(Anchor.LEFT_BOTTOM)
                .offsetX(5d)
                .offsetY(5d);

        area3d.interactivity().hoverMode(HoverMode.BY_X);
        area3d.legend(false);
        area3d.background().fill("#1b0222");
        //area3d.zAspect(10);

        anyChartView.setChart(area3d);


    }

    public void addSeriesData(List<Double> M1) {
        int k = 0;
        Double hraccum, total;
        hraccum = 0.0;
        total = 0.0;

        for (Double sp : M1) {
            if (k % 20 == 0) {
                if (k == 0) {
                    hraccum = sp;
                    seriesData.add(new CustomDataEntry(k, hraccum));
                }
                else {
                    seriesData.add(new CustomDataEntry(k, (hraccum / 20)));
                }

                hraccum = 0.0;
                //Log.d("SD", "K: " + seriesData.get(k));
            }
            else {
                hraccum += sp;
                total += sp;
            }
             k++;
        }
        avgHR = total / k;
    }

    private class CustomDataEntry extends ValueDataEntry {
        CustomDataEntry(Integer x, Double value) {
            super(x, value);
            //setValue("value2", value2);

        }
    }

    private void AddRemrigInfos(View v) {
        RemrigWorker remrig;
        EditText etremrig;
        ImageView IVremrig = v.findViewById(R.id.imageViewActionRemrig);
        HashMap<String, RemrigWorker> Moneradoremrig = new HashMap<String, RemrigWorker>();

        ReadWriteGUID workerRemrigs = new ReadWriteGUID("remrigs.json");
        String remrigs = workerRemrigs.readFromFile(v.getContext());
        Log.d("Remrig", "Remrigs.json: " + remrigs);

        try { Moneradoremrig = parseRemrigJSONFile(remrigs); }
        catch (JSONException e) { e.printStackTrace();}


        try {
            etremrig = v.findViewById(R.id.et_remrig);
            RemrigURL = Moneradoremrig.get(WorkerName).getURL();
            etremrig.setText(RemrigURL);

            etremrig = v.findViewById(R.id.et_username);
            RemrigUSER = Moneradoremrig.get(WorkerName).getUsername();
            etremrig.setText(RemrigUSER);

            etremrig = v.findViewById(R.id.et_password);
            RemrigPASS = Moneradoremrig.get(WorkerName).getPassword();
            etremrig.setText(RemrigPASS);

            Boolean remrigstate = Moneradoremrig.get(WorkerName).getState();

            if (remrigstate) {
                IVremrig.setImageResource(R.drawable.stop);
            } else {
                IVremrig.setImageResource(R.drawable.start);
            }
            RetroRemrigSensors(v);
            RetroRemrigCoin(v);
            RetroRemrigMem(v);
        }
        catch (NullPointerException e) { Log.w("Remrig", "No remrig data."); }

    }


    public void addBackFragListener(View view) {
        FloatingActionButton button = view.findViewById(R.id.floatingActionBackButton);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager fm = getParentFragmentManager();
                if (fm.getBackStackEntryCount() > 0) {
                    Log.i("StatsFrag", "popping backstack");
                    fm.popBackStack();
                } else {
                    Log.i("StatsFrag", "nothing on backstack, calling super");
                }
            }
        });
    }

    public void addRemrigListener(View view) {
        ImageView actionRemrig = view.findViewById(R.id.imageViewActionRemrig);

        actionRemrig.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(Remrig) {
                    ActionRemrig(view);
                    actionRemrig.setImageResource(R.drawable.start);
                    //actionRemrig.setImageDrawable(ContextCompat.getDrawable(v.getContext(), R.drawable.start));
                }
                else {
                    ActionRemrig(view);
                    actionRemrig.setImageResource(R.drawable.stop);
                    //actionRemrig.setImageDrawable(ContextCompat.getDrawable(v.getContext(), R.drawable.stop));
                }

            }
        });
    }

    public void ActionRemrig(View v) {
        EditText remriget = v.findViewById(R.id.et_remrig);
        String remrigdata = remriget.getEditableText().toString();
        RemrigURL = remrigdata;
        remriget = v.findViewById(R.id.et_username);
        remrigdata = remriget.getEditableText().toString();
        RemrigUSER = remrigdata;
        remriget = v.findViewById(R.id.et_password);
        remrigdata = remriget.getEditableText().toString();
        RemrigPASS = remrigdata;

        Log.d("Remrig", "Worker: " + WorkerName + " URL: " + RemrigURL + " User: " + RemrigUSER + " Password: " + RemrigPASS);
        if(Remrig) {

            RetroRemrigAction("stop", v);
            RemrigWorker remrig = new RemrigWorker(RemrigURL, RemrigUSER, RemrigPASS, false);
            SaveRemrigJSON(v, remrig);

            Remrig = false;
        }
        else {
            RetroRemrigAction("start", v);
            RemrigWorker remrig = new RemrigWorker(RemrigURL, RemrigUSER, RemrigPASS, true);
            SaveRemrigJSON(v, remrig);
            Remrig = true;
        }


    }

    private void SaveRemrigJSON(View view, RemrigWorker remrig) {
        HashMap<String, RemrigWorker> Moneradoremrig = new HashMap<String, RemrigWorker>();

        ReadWriteGUID remrigFILE = new ReadWriteGUID("remrigs.json");
        String remrigJSON = remrigFILE.readFromFile(view.getContext());

        try {
            Moneradoremrig = parseRemrigJSONFile(remrigJSON);
            Moneradoremrig.put(WorkerName, remrig);
            //Log.d("RemFrag", "Worker: " + WorkerName);
            //Log.d("RemFrag", "Moneradoremrig: " + Moneradoremrig.toString());
        }
        catch (JSONException e) { e.printStackTrace(); }

        Gson gson = new Gson();
        remrigJSON = gson.toJson(Moneradoremrig);
        Log.d("RemFrag", "REMRIGJSON: " + remrigJSON);

        remrigFILE.writeToFile(remrigJSON, view.getContext());

    }


    private HashMap<String, RemrigWorker> parseRemrigJSONFile(String jsonString) throws JSONException {
        HashMap<String, RemrigWorker> Moneradoremrig = new HashMap<String, RemrigWorker>();
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
            Moneradoremrig.put(al.get(k).toString(), remrig);

            k++;
        }
        return Moneradoremrig;

    }

    public void RetroRemrigAction(String action, View view) {
        Call<String> call  = RetroRemrig.getRetrofitInstance(this.WorkerName, view)
                .create(RetroRemrig.ApiInterface.class)
                .setRemrigAction(RemrigURL + "/api/xmrig", RetroRemrig.getAuthToken(RemrigUSER, RemrigPASS), action);

        call.enqueue(new Callback<String>() {

            @Override
            public void onResponse(Call<String> call, retrofit2.Response<String> response) {
                String retCodeList = response.body();
                try {
                    String rc = retCodeList;
                    if (rc.toUpperCase().equals("START")) {
                        Log.d("Retro", "xmrig STARTED");
                        Toast.makeText(view.getContext(), "XMRig STARTED", Toast.LENGTH_LONG).show();
                    } else if (rc.toUpperCase().equals("STOP")) {
                        Toast.makeText(view.getContext(), "XMRig STOPPED", Toast.LENGTH_LONG).show();
                        Log.d("Retro", "xmrig STOPPED");
                    }
                    else {
                        Toast.makeText(view.    getContext(), "ERROR ID10T", Toast.LENGTH_LONG).show();
                        Log.d("Retro", "ID10T ERROR");
                    }
                }
                catch (Exception e) { Log.d("Retro", "Failed: "  + e.getMessage() + e.getStackTrace()); }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                Log.d("Retro", "Failed: " + t.getMessage() + t.getStackTrace() + t.getCause() + t.getSuppressed() + t.getLocalizedMessage());
            }
        });
    }

    // to get CPU temp if remrig is enabled
    public void RetroRemrigSensors(View view) {
        Call<String> call  = RetroRemrig.getRetrofitInstance(this.WorkerName, view)
                .create(RetroRemrig.ApiInterface.class)
                .getCPUSensors(RemrigURL + "/api/sensors", RetroRemrig.getAuthToken(RemrigUSER, RemrigPASS));

        call.enqueue(new Callback<String>() {

            @Override
            public void onResponse(Call<String> call, retrofit2.Response<String> response) {
                String retCodeList = response.body();
                try {
                    String rc = retCodeList;
                    TextView cputemp = view.findViewById(R.id.tv_cputemp);
                    if (! rc.isEmpty()) {
                        cputemp.setText(rc);
                        Log.d("RetRem", "CPUTEMP: " + rc);

                    }
                    else {
                        cputemp.setText("null");
                        Log.d("RetRem", "ID10T CPU TEMP ERROR");
                    }
                }
                catch (Exception e) { Log.d("Retro", "Failed: "  + e.getMessage() + e.getStackTrace()); }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                Log.d("Retro", "Failed: " + t.getMessage() + t.getStackTrace() + t.getCause() + t.getSuppressed() + t.getLocalizedMessage());
            }
        });
    }

    public void RetroRemrigCoin(View view) {
        Call<String> call  = RetroRemrig.getRetrofitInstance(this.WorkerName, view)
                .create(RetroRemrig.ApiInterface.class)
                .getLastCoinMined(RemrigURL + "/api/coin", RetroRemrig.getAuthToken(RemrigUSER, RemrigPASS));

        call.enqueue(new Callback<String>() {

            @Override
            public void onResponse(Call<String> call, retrofit2.Response<String> response) {
                String retCodeList = response.body();
                try {
                    String rc = retCodeList;
                    TextView coin = view.findViewById(R.id.tv_coin);
                    if (! rc.isEmpty()) {
                        coin.setText(rc);
                        Log.d("RetRem", "COIN: " + rc);

                    }
                    else {
                        coin.setText("null");
                        Log.d("RetRem", "ID10T COIN ERROR");
                    }
                }
                catch (Exception e) { Log.d("Retro", "Failed: "  + e.getMessage() + e.getStackTrace()); }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                Log.d("Retro", "Failed: " + t.getMessage() + t.getStackTrace() + t.getCause() + t.getSuppressed() + t.getLocalizedMessage());
            }
        });
    }

    public void RetroRemrigMem(View view) {
        Call<String> call  = RetroRemrig.getRetrofitInstance(this.WorkerName, view)
                .create(RetroRemrig.ApiInterface.class)
                .getMemInfo(RemrigURL + "/api/mem", RetroRemrig.getAuthToken(RemrigUSER, RemrigPASS));

        call.enqueue(new Callback<String>() {

            @Override
            public void onResponse(Call<String> call, retrofit2.Response<String> response) {
                String retCodeList = response.body();
                try {
                    String rc = retCodeList;
                    TextView mem = view.findViewById(R.id.tv_mem);
                    if (! rc.isEmpty()) {
                        mem.setText(rc + "MiB");
                        Log.d("RetRem", "COIN: " + rc);

                    }
                    else {
                        mem.setText("null");
                        Log.d("RetRem", "ID10T COIN ERROR");
                    }
                }
                catch (Exception e) { Log.d("Retro", "Failed: "  + e.getMessage() + e.getStackTrace()); }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                Log.d("Retro", "Failed: " + t.getMessage() + t.getStackTrace() + t.getCause() + t.getSuppressed() + t.getLocalizedMessage());
            }
        });
    }

}
