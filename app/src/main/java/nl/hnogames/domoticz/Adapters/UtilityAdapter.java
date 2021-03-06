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

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.like.LikeButton;
import com.like.OnLikeListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import github.nisrulz.recyclerviewhelper.RVHAdapter;
import github.nisrulz.recyclerviewhelper.RVHViewHolder;
import nl.hnogames.domoticz.Interfaces.UtilityClickListener;
import nl.hnogames.domoticz.R;
import nl.hnogames.domoticz.Utils.SharedPrefUtil;
import nl.hnogames.domoticzapi.Containers.UtilitiesInfo;
import nl.hnogames.domoticzapi.Domoticz;
import nl.hnogames.domoticzapi.DomoticzIcons;
import nl.hnogames.domoticzapi.DomoticzValues;

@SuppressWarnings("unused")
public class UtilityAdapter extends RecyclerView.Adapter<UtilityAdapter.DataObjectHolder> implements RVHAdapter {

    private static final String TAG = UtilityAdapter.class.getSimpleName();
    public static List<String> mCustomSorting;
    private final UtilityClickListener listener;
    public ArrayList<UtilitiesInfo> filteredData = null;
    private Context context;
    private ArrayList<UtilitiesInfo> data = null;
    private Domoticz domoticz;
    private ItemFilter mFilter = new ItemFilter();
    private SharedPrefUtil mSharedPrefs;

    public UtilityAdapter(Context context,
                          Domoticz mDomoticz,
                          ArrayList<UtilitiesInfo> data,
                          UtilityClickListener listener) {
        super();

        this.context = context;
        mSharedPrefs = new SharedPrefUtil(context);
        domoticz = mDomoticz;

        if (mCustomSorting == null)
            mCustomSorting = mSharedPrefs.getSortingList("utilities");
        setData(data);
        this.listener = listener;
    }

    public void setData(ArrayList<UtilitiesInfo> data) {
        ArrayList<UtilitiesInfo> sortedData = SortData(data);
        this.data = sortedData;
        this.filteredData = sortedData;
    }

    private ArrayList<UtilitiesInfo> SortData(ArrayList<UtilitiesInfo> data) {
        ArrayList<UtilitiesInfo> customdata = new ArrayList<>();
        if (mSharedPrefs.enableCustomSorting() && mCustomSorting != null) {
            for (String s : mCustomSorting) {
                for (UtilitiesInfo d : data) {
                    if (s.equals(String.valueOf(d.getIdx())))
                        customdata.add(d);
                }
            }
            for (UtilitiesInfo d : data) {
                if (!customdata.contains(d))
                    customdata.add(d);
            }
        } else
            customdata = data;
        return customdata;
    }

    private void SaveSorting() {
        List<String> ids = new ArrayList<>();
        for (UtilitiesInfo d : filteredData) {
            ids.add(String.valueOf(d.getIdx()));
        }
        mCustomSorting = ids;
        mSharedPrefs.saveSortingList("utilities", ids);
    }

    public Filter getFilter() {
        return mFilter;
    }

    @Override
    public DataObjectHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.utilities_row_default, parent, false);

        if (mSharedPrefs.darkThemeEnabled()) {
            if ((view.findViewById(R.id.card_global_wrapper)) != null)
                view.findViewById(R.id.card_global_wrapper).setBackgroundColor(ContextCompat.getColor(context, R.color.card_background_dark));
            if ((view.findViewById(R.id.row_wrapper)) != null)
                (view.findViewById(R.id.row_wrapper)).setBackground(ContextCompat.getDrawable(context, R.color.card_background_dark));
            if ((view.findViewById(R.id.row_global_wrapper)) != null)
                (view.findViewById(R.id.row_global_wrapper)).setBackgroundColor(ContextCompat.getColor(context, R.color.card_background_dark));
            if ((view.findViewById(R.id.on_button)) != null)
                ((MaterialButton) view.findViewById(R.id.on_button)).setTextColor(ContextCompat.getColor(context, R.color.white));
            if ((view.findViewById(R.id.off_button)) != null)
                ((MaterialButton) view.findViewById(R.id.off_button)).setTextColor(ContextCompat.getColor(context, R.color.white));
            if ((view.findViewById(R.id.color_button)) != null)
                ((MaterialButton) view.findViewById(R.id.color_button)).setTextColor(ContextCompat.getColor(context, R.color.white));
            if ((view.findViewById(R.id.set_button)) != null)
                ((MaterialButton) view.findViewById(R.id.set_button)).setTextColor(ContextCompat.getColor(context, R.color.white));
        }

        return new DataObjectHolder(view);
    }

    @Override
    public void onBindViewHolder(final DataObjectHolder holder, final int position) {
        if (filteredData != null && filteredData.size() > 0) {
            final UtilitiesInfo mUtilitiesInfo = filteredData.get(position);
            final double setPoint = mUtilitiesInfo.getSetPoint();

            if ((mUtilitiesInfo.getType() != null && DomoticzValues.Device.Utility.Type.THERMOSTAT.equalsIgnoreCase(mUtilitiesInfo.getType())) ||
                    (mUtilitiesInfo.getSubType() != null && DomoticzValues.Device.Utility.SubType.SMARTWARES.equalsIgnoreCase(mUtilitiesInfo.getSubType()))) {
                setButtons(holder, Buttons.THERMOSTAT);
                CreateThermostatRow(holder, mUtilitiesInfo, setPoint);
            } else {
                if (DomoticzValues.Device.Utility.SubType.TEXT.equalsIgnoreCase(mUtilitiesInfo.getSubType()) || DomoticzValues.Device.Utility.SubType.ALERT.equalsIgnoreCase(mUtilitiesInfo.getSubType())) {
                    CreateTextRow(holder, mUtilitiesInfo);
                    setButtons(holder, Buttons.TEXT);
                } else {
                    CreateDefaultRow(holder, mUtilitiesInfo);
                    setButtons(holder, Buttons.DEFAULT);
                }
            }

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onItemClicked(v, position);
                }
            });

            holder.infoIcon.setTag(mUtilitiesInfo.getIdx());
            holder.infoIcon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onItemLongClicked((int) v.getTag());
                }
            });
        }
    }

    private void CreateTextRow(DataObjectHolder holder, UtilitiesInfo mUtilitiesInfo) {
        holder.isProtected = mUtilitiesInfo.isProtected();

        holder.name.setText(mUtilitiesInfo.getName());
        holder.data.setText(context.getString(R.string.data) + ": " + mUtilitiesInfo.getData());
        holder.hardware.setText(context.getString(R.string.hardware) + ": " + mUtilitiesInfo.getHardwareName());
        if (mUtilitiesInfo.getUsage() != null && mUtilitiesInfo.getUsage().length() > 0)
            holder.data.setText(context.getString(R.string.usage) + ": " + mUtilitiesInfo.getUsage());

        String text = "";
        if (mUtilitiesInfo.getUsage() != null && mUtilitiesInfo.getUsage().length() > 0) {
            try {
                int usage = Integer.parseInt(mUtilitiesInfo.getUsage().replace("Watt", "").trim());
                if (mUtilitiesInfo.getUsageDeliv() != null && mUtilitiesInfo.getUsageDeliv().length() > 0) {
                    int usagedel = Integer.parseInt(mUtilitiesInfo.getUsageDeliv().replace("Watt", "").trim());
                    text = context.getString(R.string.usage) + ": " + (usage - usagedel) + " Watt";
                    holder.data.setText(text);
                } else {
                    text = context.getString(R.string.usage) + ": " + mUtilitiesInfo.getUsage();
                    holder.data.setText(text);
                }
            } catch (Exception ex) {
                text = context.getString(R.string.usage) + ": " + mUtilitiesInfo.getUsage();
                holder.data.setText(text);
            }
        }

        if (mUtilitiesInfo.getCounterToday() != null && mUtilitiesInfo.getCounterToday().length() > 0)
            holder.data.append(" " + context.getString(R.string.today) + ": " + mUtilitiesInfo.getCounterToday());
        if (mUtilitiesInfo.getCounter() != null && mUtilitiesInfo.getCounter().length() > 0 && !mUtilitiesInfo.getCounter().equals(mUtilitiesInfo.getData()))
            holder.data.append(" " + context.getString(R.string.total) + ": " + mUtilitiesInfo.getCounter());

        if (holder.likeButton != null) {
            holder.likeButton.setId(mUtilitiesInfo.getIdx());
            holder.likeButton.setLiked(mUtilitiesInfo.getFavoriteBoolean());
            holder.likeButton.setOnLikeListener(new OnLikeListener() {
                @Override
                public void liked(LikeButton likeButton) {
                    handleLikeButtonClick(likeButton.getId(), true);
                }

                @Override
                public void unLiked(LikeButton likeButton) {
                    handleLikeButtonClick(likeButton.getId(), false);
                }
            });
        }

        holder.buttonLog.setId(mUtilitiesInfo.getIdx());
        holder.buttonLog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleLogButtonClick(v.getId());
            }
        });

        Picasso.get().load(DomoticzIcons.getDrawableIcon(mUtilitiesInfo.getTypeImg(), mUtilitiesInfo.getType(), mUtilitiesInfo.getSubType(), false, false, null)).into(holder.iconRow);
    }

    private void handleLogButtonClick(int idx) {
        listener.onLogButtonClick(idx);
    }

    private void CreateDefaultRow(DataObjectHolder holder, UtilitiesInfo mUtilitiesInfo) {
        holder.isProtected = mUtilitiesInfo.isProtected();

        holder.name.setText(mUtilitiesInfo.getName());
        holder.data.setText(context.getString(R.string.data) + ": " + mUtilitiesInfo.getData());
        holder.hardware.setText(context.getString(R.string.hardware) + ": " + mUtilitiesInfo.getHardwareName());

        String text = "";
        if (mUtilitiesInfo.getUsage() != null && mUtilitiesInfo.getUsage().length() > 0) {
            try {
                int usage = Integer.parseInt(mUtilitiesInfo.getUsage().replace("Watt", "").trim());
                if (mUtilitiesInfo.getUsageDeliv() != null && mUtilitiesInfo.getUsageDeliv().length() > 0) {
                    int usagedel = Integer.parseInt(mUtilitiesInfo.getUsageDeliv().replace("Watt", "").trim());
                    text = context.getString(R.string.usage) + ": " + (usage - usagedel) + " Watt";
                    holder.data.setText(text);
                } else {
                    text = context.getString(R.string.usage) + ": " + mUtilitiesInfo.getUsage();
                    holder.data.setText(text);
                }
            } catch (Exception ex) {
                text = context.getString(R.string.usage) + ": " + mUtilitiesInfo.getUsage();
                holder.data.setText(text);
            }
        }
        if (mUtilitiesInfo.getCounterToday() != null && mUtilitiesInfo.getCounterToday().length() > 0)
            holder.data.append(" " + context.getString(R.string.today) + ": " + mUtilitiesInfo.getCounterToday());
        if (mUtilitiesInfo.getCounter() != null && mUtilitiesInfo.getCounter().length() > 0 &&
                !mUtilitiesInfo.getCounter().equals(mUtilitiesInfo.getData()))
            holder.data.append(" " + context.getString(R.string.total) + ": " + mUtilitiesInfo.getCounter());

        holder.dayButton.setId(mUtilitiesInfo.getIdx());
        holder.dayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for (UtilitiesInfo t : filteredData) {
                    if (t.getIdx() == v.getId())
                        listener.onLogClick(t, DomoticzValues.Graph.Range.DAY);
                }
            }
        });
        holder.monthButton.setId(mUtilitiesInfo.getIdx());
        holder.monthButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for (UtilitiesInfo t : filteredData) {
                    if (t.getIdx() == v.getId())
                        listener.onLogClick(t, DomoticzValues.Graph.Range.MONTH);
                }
            }
        });

        holder.weekButton.setVisibility(View.GONE);
        holder.weekButton.setId(mUtilitiesInfo.getIdx());
        holder.weekButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for (UtilitiesInfo t : filteredData) {
                    if (t.getIdx() == v.getId())
                        listener.onLogClick(t, DomoticzValues.Graph.Range.WEEK);
                }
            }
        });

        holder.yearButton.setId(mUtilitiesInfo.getIdx());
        holder.yearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for (UtilitiesInfo t : filteredData) {
                    if (t.getIdx() == v.getId())
                        listener.onLogClick(t, DomoticzValues.Graph.Range.YEAR);
                }
            }
        });

        if (holder.likeButton != null) {
            holder.likeButton.setId(mUtilitiesInfo.getIdx());
            holder.likeButton.setLiked(mUtilitiesInfo.getFavoriteBoolean());
            holder.likeButton.setOnLikeListener(new OnLikeListener() {
                @Override
                public void liked(LikeButton likeButton) {
                    handleLikeButtonClick(likeButton.getId(), true);
                }

                @Override
                public void unLiked(LikeButton likeButton) {
                    handleLikeButtonClick(likeButton.getId(), false);
                }
            });
        }

        Picasso.get().load(DomoticzIcons.getDrawableIcon(mUtilitiesInfo.getTypeImg(), mUtilitiesInfo.getType(), mUtilitiesInfo.getSubType(), false, false, null)).into(holder.iconRow);
    }

    private void CreateThermostatRow(DataObjectHolder holder, UtilitiesInfo mUtilitiesInfo, final double setPoint) {
        int layoutResourceId;
        holder.isProtected = mUtilitiesInfo.isProtected();
        if (holder.isProtected)
            holder.on_button.setEnabled(false);

        holder.on_button.setText(context.getString(R.string.set_temperature));
        holder.on_button.setId(mUtilitiesInfo.getIdx());
        holder.on_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleThermostatClick(v.getId());
            }
        });

        holder.dayButton.setId(mUtilitiesInfo.getIdx());
        holder.dayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for (UtilitiesInfo t : filteredData) {
                    if (t.getIdx() == v.getId())
                        listener.onLogClick(t, DomoticzValues.Graph.Range.DAY);
                }
            }
        });
        holder.monthButton.setId(mUtilitiesInfo.getIdx());
        holder.monthButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for (UtilitiesInfo t : filteredData) {
                    if (t.getIdx() == v.getId())
                        listener.onLogClick(t, DomoticzValues.Graph.Range.MONTH);
                }
            }
        });

        holder.weekButton.setVisibility(View.GONE);
        holder.weekButton.setId(mUtilitiesInfo.getIdx());
        holder.weekButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for (UtilitiesInfo t : filteredData) {
                    if (t.getIdx() == v.getId())
                        listener.onLogClick(t, DomoticzValues.Graph.Range.WEEK);
                }
            }
        });

        if (mUtilitiesInfo.getSubType()
                .replace("Electric", "counter")
                .replace("kWh", "counter")
                .replace("Gas", "counter")
                .replace("Energy", "counter")
                .replace("Voltcraft", "counter")
                .replace("SetPoint", "temp")
                .replace("YouLess counter", "counter").contains("counter"))
            holder.weekButton.setVisibility(View.VISIBLE);

        holder.yearButton.setId(mUtilitiesInfo.getIdx());
        holder.yearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for (UtilitiesInfo t : filteredData) {
                    if (t.getIdx() == v.getId())
                        listener.onLogClick(t, DomoticzValues.Graph.Range.YEAR);
                }
            }
        });
        holder.name.setText(mUtilitiesInfo.getName());
        holder.data.setText(mUtilitiesInfo.getLastUpdate());
        holder.hardware.setText(context.getString(R.string.set_point) + ": " + String.valueOf(setPoint));
        Picasso.get().load(DomoticzIcons.getDrawableIcon(mUtilitiesInfo.getTypeImg(), mUtilitiesInfo.getType(), mUtilitiesInfo.getSubType(), false, false, null)).into(holder.iconRow);
    }

    public void handleThermostatClick(int idx) {
        listener.onThermostatClick(idx);
    }

    @Override
    public int getItemCount() {
        return filteredData.size();
    }

    public void setButtons(DataObjectHolder holder, int button) {

        if (holder.buttonLog != null) {
            holder.buttonLog.setVisibility(View.GONE);
        }
        if (holder.dayButton != null) {
            holder.dayButton.setVisibility(View.GONE);
        }
        if (holder.monthButton != null) {
            holder.monthButton.setVisibility(View.GONE);
        }
        if (holder.yearButton != null) {
            holder.yearButton.setVisibility(View.GONE);
        }
        if (holder.weekButton != null) {
            holder.weekButton.setVisibility(View.GONE);
        }
        if (holder.on_button != null) {
            holder.on_button.setVisibility(View.GONE);
        }

        switch (button) {
            case Buttons.DEFAULT:
                holder.dayButton.setVisibility(View.VISIBLE);
                holder.monthButton.setVisibility(View.VISIBLE);
                holder.weekButton.setVisibility(View.VISIBLE);
                holder.yearButton.setVisibility(View.VISIBLE);
                break;
            case Buttons.TEXT:
                holder.buttonLog.setVisibility(View.VISIBLE);
                break;
            case Buttons.THERMOSTAT:
                holder.on_button.setVisibility(View.VISIBLE);
                holder.dayButton.setVisibility(View.VISIBLE);
                holder.monthButton.setVisibility(View.VISIBLE);
                holder.weekButton.setVisibility(View.VISIBLE);
                holder.yearButton.setVisibility(View.VISIBLE);
                break;
        }
    }

    private void handleLikeButtonClick(int idx, boolean checked) {
        listener.onLikeButtonClick(idx, checked);
    }

    @Override
    public boolean onItemMove(int fromPosition, int toPosition) {
        swap(fromPosition, toPosition);
        return true;
    }

    @Override
    public void onItemDismiss(int position, int direction) {
        remove(position);
    }

    private void remove(int position) {
        filteredData.remove(position);
        notifyItemRemoved(position);
    }

    private void swap(int firstPosition, int secondPosition) {
        Collections.swap(filteredData, firstPosition, secondPosition);
        notifyItemMoved(firstPosition, secondPosition);
        SaveSorting();
    }

    interface Buttons {
        int DEFAULT = 0;
        int TEXT = 1;
        int THERMOSTAT = 2;
    }

    public static class DataObjectHolder extends RecyclerView.ViewHolder implements RVHViewHolder {
        TextView name;
        TextView data;
        TextView hardware;
        ImageView iconRow;
        Boolean isProtected;

        Chip dayButton;
        Chip monthButton;
        Chip yearButton;
        Chip weekButton;
        Chip buttonLog;
        Button on_button;
        ImageView infoIcon;
        LikeButton likeButton;
        LinearLayout extraPanel;

        public DataObjectHolder(View itemView) {
            super(itemView);

            dayButton = itemView.findViewById(R.id.day_button);
            monthButton = itemView.findViewById(R.id.month_button);
            yearButton = itemView.findViewById(R.id.year_button);
            weekButton = itemView.findViewById(R.id.week_button);
            likeButton = itemView.findViewById(R.id.fav_button);

            infoIcon = itemView.findViewById(R.id.widget_info_icon);
            on_button = itemView.findViewById(R.id.on_button);
            name = itemView.findViewById(R.id.utilities_name);
            iconRow = itemView.findViewById(R.id.rowIcon);
            buttonLog = itemView.findViewById(R.id.log_button);
            data = itemView.findViewById(R.id.utilities_data);
            hardware = itemView.findViewById(R.id.utilities_hardware);

            extraPanel = itemView.findViewById(R.id.extra_panel);
            if (extraPanel != null)
                extraPanel.setVisibility(View.GONE);
        }

        @Override
        public void onItemSelected(int actionstate) {
            System.out.println("Item is selected");
        }

        @Override
        public void onItemClear() {
            System.out.println("Item is unselected");
        }
    }

    private class ItemFilter extends Filter {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {

            String filterString = constraint.toString().toLowerCase();

            FilterResults results = new FilterResults();

            final ArrayList<UtilitiesInfo> list = data;

            int count = list.size();
            final ArrayList<UtilitiesInfo> nlist = new ArrayList<UtilitiesInfo>(count);

            UtilitiesInfo filterableObject;

            for (int i = 0; i < count; i++) {
                filterableObject = list.get(i);
                if (filterableObject.getName().toLowerCase().contains(filterString)) {
                    nlist.add(filterableObject);
                }
            }

            results.values = nlist;
            results.count = nlist.size();

            return results;
        }

        @SuppressWarnings("unchecked")
        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            filteredData = (ArrayList<UtilitiesInfo>) results.values;
            notifyDataSetChanged();
        }
    }
}