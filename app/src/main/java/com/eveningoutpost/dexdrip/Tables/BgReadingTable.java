package com.eveningoutpost.dexdrip.Tables;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListAdapter;
import android.widget.TextView;

import com.eveningoutpost.dexdrip.Models.BgReading;
import com.eveningoutpost.dexdrip.NavigationDrawerFragment;
import com.eveningoutpost.dexdrip.R;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class BgReadingTable extends ListActivity implements NavigationDrawerFragment.NavigationDrawerCallbacks {
    private String menu_name = "BG Data Table";
    private NavigationDrawerFragment mNavigationDrawerFragment;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.raw_data_list);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mNavigationDrawerFragment = (NavigationDrawerFragment) getFragmentManager().findFragmentById(R.id.navigation_drawer);
        mNavigationDrawerFragment.setUp(R.id.navigation_drawer, (DrawerLayout) findViewById(R.id.drawer_layout), menu_name, this);

        getData();
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        mNavigationDrawerFragment.swapContext(position);
    }

    private void getData() {
        final List<BgReading> latest = BgReading.latest(5000);
        ListAdapter adapter = new BgReadingAdapter(this, latest);

        this.setListAdapter(adapter);
    }

    public static class BgReadingCursorAdapterViewHolder {
        TextView calculated_value;
        TextView filtered_calculated_value;
        TextView age_adjusted_raw_value;
        TextView raw_data;
        TextView raw_data_timestamp;

        public BgReadingCursorAdapterViewHolder(View root) {
        	calculated_value = (TextView) root.findViewById(R.id.calculated_value);
        	filtered_calculated_value = (TextView) root.findViewById(R.id.filtered_calculated_value);
        	age_adjusted_raw_value = (TextView) root.findViewById(R.id.age_adjusted_raw_value);
        	raw_data = (TextView) root.findViewById(R.id.raw_data);
            raw_data_timestamp = (TextView) root.findViewById(R.id.raw_data_timestamp);
        }
    }

    public static class BgReadingAdapter extends BaseAdapter {
        private final Context         context;
        private final List<BgReading> readings;

        public BgReadingAdapter(Context context, List<BgReading> readings) {
            this.context = context;
            if(readings == null)
                readings = new ArrayList<>();

            this.readings = readings;
        }

        public View newView(Context context, ViewGroup parent) {
            final View view = LayoutInflater.from(context).inflate(R.layout.bg_table_list_item, parent, false);

            final BgReadingCursorAdapterViewHolder holder = new BgReadingCursorAdapterViewHolder(view);
            view.setTag(holder);

            return view;
        }

        public void bindView(View view, final Context context, final BgReading bgReading) {
            final BgReadingCursorAdapterViewHolder tag = (BgReadingCursorAdapterViewHolder) view.getTag();
            tag.calculated_value.setText(Double.toString(bgReading.calculated_value));
            tag.filtered_calculated_value.setText(Double.toString(bgReading.filtered_calculated_value));
            tag.age_adjusted_raw_value.setText(Double.toString(bgReading.age_adjusted_raw_value));
            tag.raw_data.setText(Double.toString(bgReading.raw_data));
            tag.raw_data_timestamp.setText(new Date(bgReading.timestamp).toString());
            view.setLongClickable(true);
            view.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            switch (which){
                                case DialogInterface.BUTTON_POSITIVE:
                                    bgReading.ignoreForStats = true;
                                    bgReading.save();
                                    break;

                                case DialogInterface.BUTTON_NEGATIVE:
                                    bgReading.ignoreForStats = false;
                                    bgReading.save();
                                    break;
                            }
                        }
                    };

                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setMessage("Flag reading as \"bad\".\n Flagged readings have no impact on the statistics.").setPositiveButton("Yes", dialogClickListener)
                            .setNegativeButton("No", dialogClickListener).show();
                    return true;
                }
            });
        }

        @Override
        public int getCount() {
            return readings.size();
        }

        @Override
        public BgReading getItem(int position) {
            return readings.get(position);
        }

        @Override
        public long getItemId(int position) {
            return getItem(position).getId();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null)
                convertView = newView(context, parent);

            bindView(convertView, context, getItem(position));
            return convertView;
        }
    }
}
