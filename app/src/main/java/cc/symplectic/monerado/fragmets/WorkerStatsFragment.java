package cc.symplectic.monerado.fragmets;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.anychart.APIlib;
import com.anychart.AnyChart;
import com.anychart.AnyChartView;
 import com.anychart.chart.common.dataentry.DataEntry;
 import com.anychart.chart.common.dataentry.ValueDataEntry;
 import com.anychart.charts.Cartesian;
import com.anychart.core.axismarkers.Line;
import com.anychart.core.axismarkers.Text;
import com.anychart.core.cartesian.series.Marker;
import com.anychart.core.cartesian.series.SplineArea;
import com.anychart.data.Mapping;
import com.anychart.data.Set;
import com.anychart.enums.Anchor;
import com.anychart.enums.HoverMode;
import com.anychart.enums.Position;
import com.anychart.enums.TooltipPositionMode;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import cc.symplectic.monerado.MainActivity;
import cc.symplectic.monerado.Monerado;
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
    String ChartURL = "https://api.moneroocean.stream/miner/" + MainActivity.MOADDY + "/chart/hashrate/allWorkers";
    HashMap<String, ArrayList<Double>> WorkerHashChartMap = new HashMap<String, ArrayList<Double>>();
    ProgressDialog dialog;
    Double avgHR = 0.0;
    Boolean Remrig = true;



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

    // This event is triggered soon after onCreateView().
    // Any view setup should occur here.  E.g., view lookups and attaching view listeners.
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        // Setup any handles to view objects here
        // EditText etFoo = (EditText) view.findViewById(R.id.etFoo);
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

            //Not needed: (deprecated)
            //GetInfos(ChartURL, view);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        SetupChartHashRate(WorkerHashChart, WorkerName, view);

        AddRemrigInfos(view);

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
            etremrig.setText(Moneradoremrig.get(WorkerName).getURL());

            etremrig = v.findViewById(R.id.et_username);
            etremrig.setText(Moneradoremrig.get(WorkerName).getUsername());

            etremrig = v.findViewById(R.id.et_password);
            etremrig.setText(Moneradoremrig.get(WorkerName).getPassword());

            Boolean remrigstate = Moneradoremrig.get(WorkerName).getState();

            if (remrigstate) {
                IVremrig.setImageResource(R.drawable.stop);
            } else {
                IVremrig.setImageResource(R.drawable.start);
            }
        }
        catch (NullPointerException e) { Log.w("Remrig", "No remrig data."); }

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
            //SaveRemrigJSON(remrig, al.get(k).toString());
            Moneradoremrig.put(al.get(k).toString(), remrig);

            k++;
        }
        return Moneradoremrig;

    }

// Was getting chart data every time. This code is deprecated as I get
// the Chart Data first so it's not making a bunch of requests to MO.
/*
    private void GetInfos(String url, View view) {
        //Runs volley string request and then executes parseJsonData()
        //which starts the WorkerStats fragment
        //
        //Should probably Generalize parseJsonData() at some pointe.


        StringRequest request = new StringRequest(url, new Response.Listener<String>() {
            @Override
            public void onResponse(String string) {
                try {
                    parseJsonChartData(string, view);

                    for (int k = 0; k < WorkerHashChartMap.get(WorkerName).size(); k++) {
                        Log.d("Hash", WorkerName + ": " + WorkerHashChartMap.get(WorkerName).get(k));
                    }



                }
                catch (JSONException e) { e.printStackTrace(); }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                Toast.makeText(getContext(), "Some error occurred!!", Toast.LENGTH_SHORT).show();
            }
        });

        RequestQueue rQueue = Volley.newRequestQueue(getContext());
        rQueue.add(request);
    }

    void parseJsonChartData(String jsonString, View view) throws JSONException {

        JSONObject object = new JSONObject(jsonString);
        ArrayList worker = new ArrayList();
        ArrayList<JSONArray> workerObjectArray = new ArrayList<>();
        ArrayList<Double> HashRates = new ArrayList<>();

        ArrayList wts = new ArrayList();
        @SuppressWarnings("unchecked")
        Iterator<String> keys = (Iterator<String>) object.keys();
        while (keys.hasNext()) {
            String key = keys.next();
            worker.add(key);
            workerObjectArray.add(object.getJSONArray(key));
        }
        int k = 0;
        for (JSONArray array : workerObjectArray) {
            for (int j =0; j <  array.length(); j++) {
                JSONObject tsobj = array.getJSONObject(j);
                //Log.d("Worker", worker.get(k) + ": " + tsobj.getString("hs2"));
                HashRates.add(Double.valueOf(tsobj.getString("hs2")));
            }

            //Iterator<String> workerKeys = (Iterator<String>) obj.keys();
            //while (workerKeys.hasNext()) {
            //    JSONObject tsobj = obj.getJSONObject(workerKeys.next());
            //}
            WorkerHashChartMap.put(String.valueOf(worker.get(k)), HashRates);
            HashRates = new ArrayList<>();
            k++;
        }
    }

*/


    public void SetupChartHashRate(List<Double> MatchSP1, String Worker, View view) {
        AnyChartView anyChartView = view.findViewById(R.id.any_chart_view);
        anyChartView.setProgressBar(view.findViewById(R.id.anyChartProgressBar));
        APIlib.getInstance().setActiveAnyChartView(anyChartView);

        Cartesian area3d = AnyChart.area();
        area3d.xAxis(0).labels().format("{%Value}");

        area3d.animation(true);


        //area3d.yAxis(0).title("Shooting %");
        area3d.xAxis(0).ticks(false);
        area3d.xAxis(false);
        //area3d.xAxis(0).labels().padding(5d, 5d, 0d, 5d);
        area3d.xGrid(false);
        area3d.title("Pay Hash Rate of:  " + Worker + " (20hrs)");
        area3d.title().fontColor("#FFFFFF");

        //area3d.title().useHtml(true);
        //area3d.title().padding(0d, 0d, 20d, 0d);


        //Integer lenMatchSP1 = MatchSP1.size();
        //Integer lenMatchSP2 = MatchSP2.size();
        try {
            addSeriesData(MatchSP1);
        }
          catch (ArrayIndexOutOfBoundsException e) {
            Log.w("AnyChart", "No Data for a player: " + e.getMessage());
            return;
        }

        Line marker = area3d.lineMarker(0).value(avgHR);
        marker.stroke("white", 1.3, "2 12","bevel", "round");
        Text marker2 = area3d.textMarker(0).value(avgHR);

        DecimalFormat df = new DecimalFormat("###,###.##");
        marker2.text(String.valueOf(df.format(avgHR)));
        marker2.fontColor("#FFFFFF");

        Set set = Set.instantiate();
        set.data(seriesData);

        Mapping series1Data = set.mapAs("{ x: 'x', value: 'value' }");
        //Mapping series2Data = set.mapAs("{ x: 'x', value: 'value2' }");

        //Area3d series1 = area3d.area(series1Data);

        //Area series1 = area3d.area(series1Data);
        SplineArea series1 = area3d.splineArea(series1Data, "x,value");
        Log.d("S1", "Series 2: " + series1.getPoint(5));

        //series1.name(WorkerName + " Hash Rate" );
        //series1.legendItem(WorkerName);
        series1.normal().fill("#01a393", 0.1);
        series1.hovered().fill("#01a393", 0.1);
        series1.selected().fill("#01a393", 0.5);

        series1.hovered().markers(true);
        series1.normal().markers(true);
        series1.color("#01a393");
        series1.markers(false);
        series1.minLabels(true);
        series1.maxLabels(true);
/*
        //Area3d series2 = area3d.area(series2Data);
        Area series2 = area3d.area(series2Data);
        //SplineArea series2 = area3d.splineArea(series2Data, "x,value2");
        Log.d("S2", "Series 2: " + series2.getPoint(5));
        if (lenMatchSP1 < lenMatchSP2) {
            series2.name(PlayerName1 + " Shooting %");
            series2.legendItem(PlayerName1);
            series2.normal().fill("#fd09bf", 0.1);
            series2.hovered().fill("#fd09bf", 0.1);
            series2.selected().fill("#fd09bf", 0.5);
        } else {
            series2.name(PlayerName2 + " Shooting %");
            series2.legendItem(PlayerName2);
            series2.normal().fill("#fd09bf", 0.1);
            series2.hovered().fill("#fd09bf", 0.1);
            series2.selected().fill("#fd09bf", 0.5);
        }
        series2.hovered().markers(true);
        series2.normal().markers(true);
        series2.color("#fd09bf");
*/
        area3d.tooltip()
                .displayMode("Union")
                .position(Position.CENTER_TOP)
                .positionMode(TooltipPositionMode.POINT)
                .anchor(Anchor.LEFT_BOTTOM)
                .offsetX(5d)
                .offsetY(5d);

        area3d.interactivity().hoverMode(HoverMode.BY_X);
        area3d.legend(false);
        area3d.background().fill("#2F3335");
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
            //seriesData.add(new CustomDataEntry(k, sp));
            //Log.d("SD", "K: " + seriesData.get(k));
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
        String RemrigURL = remrigdata;
        remriget = v.findViewById(R.id.et_username);
        remrigdata = remriget.getEditableText().toString();
        String RemrigUSER = remrigdata;
        remriget = v.findViewById(R.id.et_password);
        remrigdata = remriget.getEditableText().toString();
        String RemrigPASS = remrigdata;


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


        Monerado monerado = Monerado.getInstance();
        Moneradoremrig = monerado.getWorkersRemrig();
        Moneradoremrig.put(this.WorkerName, remrig);
        monerado.setWorkersRemrig(Moneradoremrig);

        ReadWriteGUID remrigFILE = new ReadWriteGUID("remrigs.json");
        Gson gson = new Gson();
        String remrigJSON = gson.toJson(Moneradoremrig);

        remrigFILE.writeToFile(remrigJSON, view.getContext());

    }

    public void RetroRemrigAction(String action, View view) {
        Call<String> call  = RetroRemrig.getRetrofitInstance(this.WorkerName, view)
                .create(RetroRemrig.ApiInterface.class)
                .setRemrigAction(RetroRemrig.getAuthToken(), action);

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

}
