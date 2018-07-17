package com.pedrorocha.wifitracker;

import android.Manifest;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Pair;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.pedrorocha.wifitracker.adapters.AdapterListWifi;
import com.pedrorocha.wifitracker.models.Wifi;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    private TextView tabWelcomeMsg;

    private Button btnScan;
    private Button btnForceStop;
    private TextView txtScanStatus;
    private LinearLayout llScanInfos;
    private TextView txtTotalFound;

    private boolean isScanning;

    private WifiReceiver wifiReceiver;
    private WifiManager wifiManager;
    private List<ScanResult> resultList;
    private List<Wifi> wifiList;

    private RecyclerView rvWifi;

    private AdapterListWifi adapterWifi;

    DatabaseReference mDatabase;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    tabWelcomeMsg.setText(R.string.title_home);
                    return true;
                case R.id.navigation_dashboard:
                    tabWelcomeMsg.setText(R.string.title_dashboard);
                    return true;
                case R.id.navigation_notifications:
                    tabWelcomeMsg.setText(R.string.title_notifications);
                    return true;
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initDatabase();
        bindAll();
        setupLayout();
        setupActions();
        initWifiSettings();
    }

    private void initDatabase() {
        mDatabase = FirebaseDatabase.getInstance().getReference();
    }

    private void bindAll() {
        tabWelcomeMsg = findViewById(R.id.tabWelcomeMsg);

        btnScan = findViewById(R.id.btnScan);
        btnForceStop = findViewById(R.id.btnForceStop);
        txtScanStatus = findViewById(R.id.txtScanStatus);
        txtTotalFound = findViewById(R.id.txtTotalFound);

        rvWifi = findViewById(R.id.rvWifi);
        llScanInfos = findViewById(R.id.llScanInfos);

        BottomNavigationView navigation = findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
    }

    private void setupLayout() {
        txtScanStatus.setText(R.string.txt_not_scanning);
        tabWelcomeMsg.setText("pew pew pew!");

        llScanInfos.setVisibility(View.GONE);
        txtTotalFound.setText(getResources().getString(R.string.total_wifi_found, 0));
    }

    private void setupActions() {
        isScanning = false;

        btnScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isScanning) stopScanning();
                else scan();
            }
        });

        btnForceStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stopScanning();
            }
        });
    }

    private void initWifiSettings() {
        wifiList = new ArrayList<>();
        wifiReceiver = new WifiReceiver();
        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
    }

    private void stopScanning() {
        isScanning = false;

        unregisterReceiver(wifiReceiver);
        Toast.makeText(this, R.string.txt_scanning_stopped, Toast.LENGTH_SHORT).show();

        txtScanStatus.setText(R.string.txt_scanning_stopped);
        btnScan.setText(R.string.btn_scan);
    }

    private void scan() {
        if (wifiManager.isWifiEnabled()) {
            isScanning = true;

            registerReceiver(wifiReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
            wifiManager.startScan();
            Toast.makeText(this, R.string.txt_scanning, Toast.LENGTH_SHORT).show();
            txtScanStatus.setText(R.string.txt_scanning);
            btnScan.setText(R.string.btn_scan_stop);
            llScanInfos.setVisibility(View.VISIBLE);

        } else {

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Wifi is off");
            builder.setMessage("should I turn it on and scan?");
            builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    wifiManager.setWifiEnabled(true);
                    scan();
                }
            });
            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();
                }
            });

            builder.create();
            builder.show();
        }
    }

    private void doneScanning() {
        isScanning = false;

        Toast.makeText(this, R.string.txt_scanning_done, Toast.LENGTH_SHORT).show();

        txtScanStatus.setText(R.string.txt_scanning_done);
        btnScan.setText(R.string.btn_scan);

        manageResults();
    }

    private void manageResults() {
        String scanId = UUID.randomUUID().toString();
        mDatabase.child("scans").child(scanId)
                .setValue(Utils.getParsedTimestamp(System.currentTimeMillis()));

        for (ScanResult result : resultList) {
            boolean isNew = true;

            for (Wifi wifi : wifiList) {
                if (wifi.getBssid().equals(result.BSSID)) {
                    isNew = false;
                    break;
                }
            }

            if (isNew) {
                Wifi wifi = new Wifi(
                        scanId,
                        result.SSID,
                        result.BSSID,
                        result.capabilities,
                        System.currentTimeMillis()
                );
                wifiList.add(wifi);
                mDatabase.child("wifi").child(result.BSSID).setValue(wifi);
            }
        }

        if (!wifiList.isEmpty()) {
            txtTotalFound.setText(getResources().getString(R.string.total_wifi_found, wifiList.size()));

            if (adapterWifi != null) {
                adapterWifi.notifyDataSetChanged();
            } else {
                adapterWifi = new AdapterListWifi(wifiList);
                rvWifi.setAdapter(adapterWifi);
                rvWifi.setLayoutManager(new LinearLayoutManager(this));
            }
        }

    }


    @Override
    public void onResume() {
        super.onResume();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 87);
            }
        }
    }

    class WifiReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)) {
                resultList = wifiManager.getScanResults();
                doneScanning();
            }
        }
    }
}

