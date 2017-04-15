package com.tesseractmobile.pocketbot.activities.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.tesseractmobile.pocketbot.R;
import com.tesseractmobile.pocketbot.activities.PocketBotSettings;

import java.util.ArrayList;

/**
 * Created by josh on 12/7/2015.
 */
public class BluetoothDialog extends android.support.v4.app.DialogFragment {

    private ArrayList<BluetoothDevice> mDevices;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final Context ctx = getActivity();
        LayoutInflater inflater = getActivity().getLayoutInflater();

        final View rootView = inflater.inflate(R.layout.bluetooth_selector, null, false);

        final ListView listView = (ListView) rootView.findViewById(R.id.lvBluetoothDevices);
        listView.setAdapter(new BluetoothListAdapter(getActivity(), mDevices));

        return new AlertDialog.Builder(ctx)
                .setTitle("Bluetooth Selection")
                .setView(rootView)
                .create();
    }

    public void setData(ArrayList<BluetoothDevice> deviceList) {
        mDevices = deviceList;
    }


    private class BluetoothListAdapter extends ArrayAdapter<BluetoothDevice>{

        private String mSelectedDevice;

        public BluetoothListAdapter(final Context context, final ArrayList<BluetoothDevice> devices){
            super(context, 0, devices);
            mSelectedDevice = PocketBotSettings.getBluetoothDevice(context);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final View view;
            if(convertView == null){
                view = LayoutInflater.from(getContext()).inflate(R.layout.bluetooth_row, parent, false);
            } else {
                view = convertView;
            }
            final TextView tvMac = (TextView) view.findViewById(R.id.tvMac);
            final BluetoothDevice device = getItem(position);
            final String address = device.getAddress();
            tvMac.setText(address);
            final TextView tvName = (TextView) view.findViewById(R.id.tvName);
            final String name = device.getName();
            tvName.setText(name);
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    PocketBotSettings.setBluetoothDevice(getContext(), address);
                    dismiss();
                    Toast.makeText(getContext(), "Selected device " + address, Toast.LENGTH_LONG).show();
                }
            });
            if(address.equals(mSelectedDevice)){
                view.setBackgroundColor(Color.BLUE);
            } else {
                view.setBackgroundColor(Color.BLACK);
            }
            return view;
        }
    }
}
