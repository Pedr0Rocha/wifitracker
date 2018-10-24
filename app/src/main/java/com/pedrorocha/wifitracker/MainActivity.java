package com.pedrorocha.wifitracker;

import android.Manifest;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.pedrorocha.wifitracker.adapters.AdapterListWifi;
import com.pedrorocha.wifitracker.models.Scan;
import com.pedrorocha.wifitracker.models.Wifi;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    private TextView tabWelcomeMsg;

    private Button btnScan;
    private Button btnForceStop;
    private TextView txtScanStatus;
    private LinearLayout llScanInfos;
    private TextView txtTotalFoundSession;
    private TextView txtWifiCount;
    private TextView txtScanCount;
    private TextView txtLastScan;

    private boolean isScanning;

    private WifiReceiver wifiReceiver;
    private WifiManager wifiManager;
    private List<ScanResult> resultList;
    private List<Wifi> wifiList;
    private HashSet<String> wifiIds;

    private RecyclerView rvWifi;

    private AdapterListWifi adapterWifi;

    DatabaseReference mDatabase;
    DatabaseReference scansReference;
    DatabaseReference wifiReference;

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

        scansReference = FirebaseDatabase.getInstance().getReference("data").child("scans");
        wifiReference = FirebaseDatabase.getInstance().getReference("data").child("wifi");

        scansReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                long total = dataSnapshot.getChildrenCount();
                String lastScan = Utils.getParsedTimestamp(System.currentTimeMillis());
                mDatabase.child("stats").child("scanCount").setValue(total);
                mDatabase.child("stats").child("lastScan").setValue(lastScan);
                txtScanCount.setText(getResources().getString(R.string.scan_count, total));
                txtLastScan.setText(getResources().getString(R.string.last_scan, lastScan));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        wifiReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                long total = dataSnapshot.getChildrenCount();
                mDatabase.child("stats").child("wifiCount").setValue(total);
                txtWifiCount.setText(getResources().getString(R.string.wifi_count, total));
                txtWifiCount.setTextColor(getResources().getColor(R.color.colorAccent));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void bindAll() {
        tabWelcomeMsg = findViewById(R.id.tabWelcomeMsg);

        btnScan = findViewById(R.id.btnScan);
        btnForceStop = findViewById(R.id.btnForceStop);
        txtScanStatus = findViewById(R.id.txtScanStatus);
        txtTotalFoundSession = findViewById(R.id.txtTotalFoundSession);
        txtWifiCount = findViewById(R.id.txtWifiCount);
        txtScanCount = findViewById(R.id.txtScanCount);
        txtLastScan = findViewById(R.id.txtLastScan);

        rvWifi = findViewById(R.id.rvWifi);
        llScanInfos = findViewById(R.id.llScanInfos);

        BottomNavigationView navigation = findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
    }

    private void setupLayout() {
        txtScanStatus.setText(R.string.txt_not_scanning);
        tabWelcomeMsg.setText(getResources().getString(R.string.app_name));

        llScanInfos.setVisibility(View.GONE);
        txtTotalFoundSession.setText(getResources().getString(R.string.total_wifi_found_session, 0));
        txtWifiCount.setText(getResources().getString(R.string.stats_sync));
        txtWifiCount.setTextColor(getResources().getColor(R.color.yellow));
        txtScanCount.setText("");
        txtLastScan.setText("");
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
        wifiIds = new HashSet<>();
        wifiReceiver = new WifiReceiver();
        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
    }

    private void stopScanning() {
        isScanning = false;

        try {
            unregisterReceiver(wifiReceiver);
        } catch (IllegalArgumentException ex) {
            ex.printStackTrace();
        }
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

        Scan scan = new Scan(
                UUID.randomUUID().toString(),
                System.currentTimeMillis(),
                resultList.size()
        );
        mDatabase.child("data").child("scans").child(scan.getId()).setValue(scan);

        manageResults(scan);
    }

    private void manageResults(Scan scan) {

        for (ScanResult result : resultList) {
            if (!wifiIds.contains(result.BSSID)) {
                wifiIds.add(result.BSSID);

                Wifi wifi = new Wifi(
                        scan.getId(),
                        result.SSID,
                        result.BSSID,
                        result.capabilities,
                        System.currentTimeMillis()
                );
                wifiList.add(wifi);
                mDatabase.child("data").child("wifi").child(result.BSSID).setValue(wifi);
            }
        }

        if (!wifiList.isEmpty()) {
            txtTotalFoundSession.setText(getResources().getString(R.string.total_wifi_found_session, wifiList.size()));

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

