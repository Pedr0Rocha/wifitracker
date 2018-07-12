package com.pedrorocha.wifitracker.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.pedrorocha.wifitracker.R;
import com.pedrorocha.wifitracker.models.Wifi;

import java.util.List;

public class AdapterListWifi extends RecyclerView.Adapter<AdapterListWifi.CustomViewHolder> {

    private List<Wifi> itens;
    private Context context;

    public AdapterListWifi(List<Wifi> itens) {
        this.itens = itens;
    }

    @NonNull
    @Override
    public CustomViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        this.context = parent.getContext();
        LayoutInflater mInflater = LayoutInflater.from(context);
        View view = mInflater.inflate(R.layout.itemlist_wifi, parent, false);
        return new CustomViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final CustomViewHolder holder, int position) {
        final Wifi wifi = itens.get(position);

        holder.txtSsid.setText(wifi.getSsid());
        holder.txtBssid.setText(wifi.getBssid());
        holder.txtCapabilities.setText(wifi.getCapabilities());
        holder.txtSecurityLevel.setText("Security level: " + wifi.getSecurityLevel());
        holder.txtTimestamp.setText(wifi.getParsedTimestamp());

        holder.txtSecurityLevel.setTextColor(context.getResources().getColor(wifi.getColor()));
    }

    @Override
    public int getItemCount() {
        return (itens != null ? itens.size() : 0);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    class CustomViewHolder extends RecyclerView.ViewHolder {
        protected TextView txtSsid;
        protected TextView txtBssid;
        protected TextView txtCapabilities;
        protected TextView txtSecurityLevel;
        protected TextView txtTimestamp;


        private CustomViewHolder(View view) {
            super(view);
            this.txtSsid = view.findViewById(R.id.txtSsid);
            this.txtBssid = view.findViewById(R.id.txtBssid);
            this.txtCapabilities = view.findViewById(R.id.txtCapabilities);
            this.txtSecurityLevel = view.findViewById(R.id.txtSecurityLevel);
            this.txtTimestamp = view.findViewById(R.id.txtTimestamp);
        }
    }
}
