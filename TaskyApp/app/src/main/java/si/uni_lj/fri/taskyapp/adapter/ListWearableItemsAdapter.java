package si.uni_lj.fri.taskyapp.adapter;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import si.uni_lj.fri.taskyapp.R;
import si.uni_lj.fri.taskyapp.sensor.Constants;

/**
 * Created by veljko on 17/05/16.
 */


public class ListWearableItemsAdapter extends ArrayAdapter<ListWearableItem> {

    private final ArrayList<ListWearableItem> mItems;
    private final Context mContext;

    public static final String TAG = "ListWearableItemsAdpter";

    public ListWearableItemsAdapter(Context context, int textViewResourceId) {
        super(context, textViewResourceId);
        this.mContext = context;
        this.mItems = new ArrayList<>();
    }


    public void addItem(ListWearableItem item) {
        Log.d(TAG, "adding "+item.getItemName());
        mItems.add(item);
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View view = convertView;
        if (view == null) {
            try {
                view = getInflator().inflate(R.layout.wearable_item, parent, false);
            } catch (Exception e) {
                return null; //TODO: handle exception
            }
        }

        if (mItems.size() == 0) {
            return view;
        }
        ListWearableItem item = mItems.get(position);
        if (item != null) {

            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
            String wearableMac = prefs.getString(Constants.PREFS_CHOSEN_WEARABLE_MAC, "");
            ImageView itemSelected = (ImageView) view.findViewById(R.id.wearable_selected);
            Log.d(TAG, "Item key: " + item.getItemKey() + " saved mac: " + wearableMac);
            if(item.getItemKey().equals(wearableMac)) {
                Log.d(TAG, "Making it visible.");
                itemSelected.setVisibility(View.VISIBLE);
            }
            else{
                itemSelected.setVisibility(View.GONE);
            }
            TextView itemNameView = (TextView) view.findViewById(R.id.wearable_name);
            if (itemNameView != null) {
                itemNameView.setText(item.getItemName());
            }

            TextView itemKeyView = (TextView) view.findViewById(R.id.wearable_mac);
            if (itemKeyView != null) {
                itemKeyView.setText(item.getItemKey());
            }
        }

        return view;
    }


    private LayoutInflater getInflator() {
        return (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }
}