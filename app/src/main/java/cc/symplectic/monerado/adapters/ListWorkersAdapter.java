package cc.symplectic.monerado.adapters;


import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;


import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import cc.symplectic.monerado.R;


public class ListWorkersAdapter extends RecyclerView.Adapter<ListWorkersAdapter.WorkerViewHolder> {

    private ArrayList<String> WorkerNames;
    private ArrayList<JSONObject> WorkerObjects;

    private static ClickListener clickListener;

    public ListWorkersAdapter(ArrayList<String> WorkerNames, ArrayList<JSONObject> WorkerObjects) {
        this.WorkerNames = WorkerNames;
        this.WorkerObjects = WorkerObjects;
    }

    @Override
    public int getItemCount() { return WorkerNames.size(); }

    @Override
    public void onBindViewHolder(WorkerViewHolder contactViewHolder, int i) {
        NumberFormat format = NumberFormat.getInstance(Locale.US);
        DecimalFormat df = new DecimalFormat("###,###.000");
        Double Hash2;

        String wName = WorkerNames.get(i);
        JSONObject wObj = WorkerObjects.get(i);
        String Hash = null;

        contactViewHolder.workerName.setText(wName);

        try { Hash = wObj.getString("hash"); }
        catch (JSONException e) { e.printStackTrace();  }
        if (Double.parseDouble(Hash) > 1000) { Hash2 = Double.parseDouble(Hash) / (double) 1000; }
        else { Hash2 = Double.valueOf(Hash); }
        format.format(Hash2);
        contactViewHolder.rawHash.setText(df.format(Hash2));

        try { Hash = wObj.getString("hash2");}
        catch (JSONException e) { e.printStackTrace(); }
        if (Double.parseDouble(Hash) > 1000) { Hash2 = Double.parseDouble(Hash) / (double) 1000; }
        else { Hash2 = Double.valueOf(Hash); }
        format.format(Hash2);
        contactViewHolder.payHash.setText(df.format(Hash2));


        try {
            contactViewHolder.validShares.setText(String.valueOf(format.format(Double.parseDouble(wObj.getString("validShares")))));
        }
        catch (JSONException e) { e.printStackTrace(); }

    }

    @Override
    public WorkerViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View itemView = LayoutInflater.
                from(viewGroup.getContext()).
                inflate(R.layout.worker_cardview, viewGroup, false);

        return new WorkerViewHolder(itemView);
    }

    public static class WorkerViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        protected TextView workerName;
        protected TextView rawHash;
        protected TextView payHash;
        protected TextView validShares;


        public WorkerViewHolder(View v) {
            super(v);
            workerName = (TextView) v.findViewById(R.id.workerName);
            rawHash = (TextView) v.findViewById(R.id.rawHash);
            payHash = (TextView) v.findViewById(R.id.payHash);
            validShares = (TextView) v.findViewById(R.id.validShares);

            v.setOnClickListener(this);
            v.setOnLongClickListener(this);

        }
        @Override
        public void onClick(View v) {
            if (clickListener == null) {
                v.setClickable(false);
            }
            else {
                clickListener.onItemClick(getAdapterPosition(), v);
            }

        }
        @Override
        public boolean onLongClick(View v) {
            clickListener.onItemLongClick(getAdapterPosition(), v);
            return false;
        }

    }

    public void setOnItemClickListener(ClickListener clickListener) {
        ListWorkersAdapter.clickListener = clickListener;
    }
    public interface ClickListener {
        void onItemClick(int position, View v);
        void onItemLongClick(int position, View v);
    }

}