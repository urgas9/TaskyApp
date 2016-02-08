package si.uni_lj.fri.taskyapp.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.afollestad.sectionedrecyclerview.SectionedRecyclerViewAdapter;

import java.util.ArrayList;

import si.uni_lj.fri.taskyapp.R;
import si.uni_lj.fri.taskyapp.data.SensorReadingData;

/**
 * Created by urgas9 on 7. 02. 2016.
 *
 */
public class ListDataAdapter extends SectionedRecyclerViewAdapter<ListDataAdapter.MainVH> {

    // Must be ordered by timestamp!!
    ArrayList<SensorReadingData> sectionsList;
    @Override
    public int getSectionCount() {
        return 20; // number of sections.
    }

    @Override
    public int getItemCount(int section) {
        return 8; // number of items in section (section index is parameter).
    }

    @Override
    public void onBindHeaderViewHolder(MainVH holder, int section) {
        // Setup header view.
    }

    @Override
    public void onBindViewHolder(MainVH holder, int section, int relativePosition, int absolutePosition) {
        // Setup non-header view.
        // 'section' is section index.
        // 'relativePosition' is index in this section.
        // 'absolutePosition' is index out of all non-header items.
        // See sample project for a visual of how these indices work.
    }

    @Override
    public MainVH onCreateViewHolder(ViewGroup parent, int viewType) {
        // Change inflated layout based on 'header'.
        View v = LayoutInflater.from(parent.getContext())
                .inflate(viewType == VIEW_TYPE_HEADER ? R.layout.list_data_header : R.layout.list_data_item, parent, false);
        return new MainVH(v);
    }

    public static class MainVH extends RecyclerView.ViewHolder {

        public MainVH(View itemView) {
            super(itemView);
            // Setup view holder.
            // You'd want some views to be optional, e.g. for header vs. normal.
        }
    }
}
