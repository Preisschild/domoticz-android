/*
 * Copyright (C) 2015 Domoticz - Mark Heinis
 *
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package nl.hnogames.domoticz.Adapters;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;

import androidx.core.content.ContextCompat;
import nl.hnogames.domoticz.Containers.NFCInfo;
import nl.hnogames.domoticz.Interfaces.NFCClickListener;
import nl.hnogames.domoticz.R;
import nl.hnogames.domoticz.Utils.SharedPrefUtil;
import nl.hnogames.domoticz.Utils.UsefulBits;

public class NFCAdapter extends BaseAdapter {

    @SuppressWarnings("unused")
    private static final String TAG = NFCAdapter.class.getSimpleName();
    public ArrayList<NFCInfo> data = null;
    private Context context;
    private NFCClickListener listener;

    private SharedPrefUtil mSharedPrefs;

    public NFCAdapter(Context context,
                      ArrayList<NFCInfo> data,
                      NFCClickListener l) {
        super();

        mSharedPrefs = new SharedPrefUtil(context);
        this.context = context;
        this.data = data;
        this.listener = l;
    }

    @Override
    public int getCount() {
        if (data == null)
            return 0;

        return data.size();
    }

    @Override
    public Object getItem(int i) {
        return data.get(i);
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }


    @SuppressLint({"ViewHolder", "SetTextI18n"})
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;
        int layoutResourceId;

        final NFCInfo mNFCInfo = data.get(position);
        holder = new ViewHolder();

        layoutResourceId = R.layout.nfc_row;
        LayoutInflater inflater = ((Activity) context).getLayoutInflater();
        convertView = inflater.inflate(layoutResourceId, parent, false);

        if (mSharedPrefs.darkThemeEnabled()) {
            if ((convertView.findViewById(R.id.card_global_wrapper)) != null)
                convertView.findViewById(R.id.card_global_wrapper).setBackgroundColor(ContextCompat.getColor(context, R.color.card_background_dark));
            if ((convertView.findViewById(R.id.row_wrapper)) != null)
                (convertView.findViewById(R.id.row_wrapper)).setBackground(ContextCompat.getDrawable(context, R.color.card_background_dark));
            if ((convertView.findViewById(R.id.row_global_wrapper)) != null)
                (convertView.findViewById(R.id.row_global_wrapper)).setBackgroundColor(ContextCompat.getColor(context, R.color.card_background_dark));

            if ((convertView.findViewById(R.id.remove_button)) != null)
                ((MaterialButton) convertView.findViewById(R.id.remove_button)).setTextColor(ContextCompat.getColor(context, R.color.white));
        }

        holder.enable = convertView.findViewById(R.id.enableNFC);
        holder.nfc_name = convertView.findViewById(R.id.nfc_name);
        holder.nfc_tag_id = convertView.findViewById(R.id.nfc_tag_id);
        holder.nfc_switch_idx = convertView.findViewById(R.id.nfc_switchidx);
        holder.remove = convertView.findViewById(R.id.remove_button);

        holder.nfc_name.setText(mNFCInfo.getName());
        holder.nfc_tag_id.setText(mNFCInfo.getId());

        if (!UsefulBits.isEmpty(mNFCInfo.getSwitchName())) {
            holder.nfc_switch_idx.setText(context.getString(R.string.connectedSwitch) + ": " + mNFCInfo.getSwitchName());
        } else if (mNFCInfo.getSwitchIdx() > 0) {
            holder.nfc_switch_idx.setText(context.getString(R.string.connectedSwitch) + ": " + mNFCInfo.getSwitchIdx());
        } else {
            holder.nfc_switch_idx.setText(context.getString(R.string.connectedSwitch)
                    + ": " + context.getString(R.string.not_available));
        }

        if (!UsefulBits.isEmpty(mNFCInfo.getValue()))
            holder.nfc_switch_idx.setText(holder.nfc_switch_idx.getText() + " - " + mNFCInfo.getValue());

        holder.remove.setId(position);
        holder.remove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                handleRemoveButtonClick(data.get(v.getId()));
            }
        });

        holder.enable.setId(position);
        holder.enable.setChecked(mNFCInfo.isEnabled());
        holder.enable.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                handleEnableChanged(data.get(buttonView.getId()), isChecked);
            }
        });

        convertView.setTag(holder);
        return convertView;
    }

    private void handleRemoveButtonClick(NFCInfo nfc) {
        listener.onRemoveClick(nfc);
    }

    private boolean handleEnableChanged(NFCInfo nfc, boolean enabled) {
        return listener.onEnableClick(nfc, enabled);
    }

    static class ViewHolder {
        TextView nfc_name;
        TextView nfc_tag_id;
        TextView nfc_switch_idx;
        CheckBox enable;
        Button remove;
    }
}