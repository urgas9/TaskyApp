package si.uni_lj.fri.taskyapp.adapter;

import android.animation.ArgbEvaluator;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import butterknife.Bind;
import butterknife.ButterKnife;
import si.uni_lj.fri.taskyapp.LabelTaskActivity;
import si.uni_lj.fri.taskyapp.R;
import si.uni_lj.fri.taskyapp.data.SensorReadingDataWithSections;
import si.uni_lj.fri.taskyapp.data.db.SensorReadingRecord;
import si.uni_lj.fri.taskyapp.sensor.Constants;

/**
 * Created by urgas9 on 7. 02. 2016.
 */
public class ListDataRecyclerAdapter extends RecyclerView.Adapter {

    private final static String TAG = "ListDataRecyclerAdapter";
    private final static int SECTION_HEADER_ITEM = -1;
    private final static int NORMAL_ITEM = 0;
    // Must be ordered by timestamp!!
    ArrayList<SensorReadingRecord> dataList;
    int numDays;
    Activity mActivity;

    SimpleDateFormat formatFullDate = new SimpleDateFormat(Constants.DATE_FORMAT_TO_SHOW_FULL);
    SimpleDateFormat formatDailyDate = new SimpleDateFormat(Constants.DATE_FORMAT_TO_SHOW_DAY);

    public ListDataRecyclerAdapter(Activity mActivity) {
        super();
        this.mActivity = mActivity;
        dataList = new ArrayList<>();
    }

    public void setAdapterData(SensorReadingDataWithSections sensorReadingDataWithSections) {
        this.dataList = sensorReadingDataWithSections.getDataList();
        this.numDays = sensorReadingDataWithSections.getNumSections();
        notifyDataSetChanged();
    }

    public void updateDatabaseRecord(long dbId, int action) {
        int index = -1, count = 0;
        for (SensorReadingRecord srr : dataList) {
            if (srr != null && srr.getId() == dbId) {
                index = count;
                break;
            }
            count++;
        }

        if (index > 0) {
            // Object was removed if action == 0, or updated action == 1
            if (action == 0) {
                notifyItemRemoved(index);
            } else if (action == 1) {
                SensorReadingRecord srr = SensorReadingRecord.findById(SensorReadingRecord.class, dbId);
                dataList.set(index, srr);
                notifyItemChanged(index);
            }

        }
    }

    public void addNewSensorReadingRecord(SensorReadingRecord srr) {
        dataList.add(srr);
        notifyItemInserted(dataList.size() - 1);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mActivity);
        switch (viewType) {
            case SECTION_HEADER_ITEM:
                return new SectionHeaderViewHolder(inflater.inflate(R.layout.list_data_header, parent, false));
            case NORMAL_ITEM:
                return new NormalItemViewHolder(inflater.inflate(R.layout.list_data_item, parent, false));
            default:
                Log.e(TAG, "viewType in RecyclerView adapter not handled!!");
        }

        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        final SensorReadingRecord srr = dataList.get(position);
        if (holder instanceof NormalItemViewHolder) {
            ((NormalItemViewHolder) holder).contentView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(mActivity, LabelTaskActivity.class);
                    intent.putExtra("db_record_id", srr.getId());
                    mActivity.startActivityForResult(intent, Constants.LABEL_TASK_REQUEST_CODE);
                }
            });
            if (srr.getDetectedActivity() != null) {
                ((NormalItemViewHolder) holder).mActivityTv.setText(srr.getDetectedActivity());
            }
            ((NormalItemViewHolder) holder).mDateTv.setText(formatFullDate.format(new Date(srr.getTimeStartedSensing())));

            if (srr.getLocationLat() > 0 && srr.getLocationLng() > 0) {
                if (srr.getAddress() != null) {
                    ((NormalItemViewHolder) holder).mLocationTv.setText(srr.getAddress());
                } else {
                    ((NormalItemViewHolder) holder).mLocationTv.setText(String.format("%.3f, %.3f", srr.getLocationLat(), srr.getLocationLng()));
                }
            }


            if (srr.getLabel() != null && srr.getLabel() > 0) {
                ((GradientDrawable) ((NormalItemViewHolder) holder).mLabelCircle.getBackground())
                        .setColor((Integer) new ArgbEvaluator()
                                .evaluate(srr.getLabel() / 7.f, Color.GREEN, Color.RED));
                ((NormalItemViewHolder) holder).mLabelCircle.setVisibility(View.VISIBLE);
            } else {
                ((NormalItemViewHolder) holder).mLabelCircle.setVisibility(View.GONE);
            }

        } else if (holder instanceof SectionHeaderViewHolder) {
            ((SectionHeaderViewHolder) holder).mHeader.setText(formatDailyDate.format(dataList.get(position + 1).getTimeStartedSensing()));
        } else {
            Log.e(TAG, "Holder was instance of " + holder.getClass().getSimpleName());
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0 || dataList.get(position) == null) {
            return SECTION_HEADER_ITEM;
        } else {
            return NORMAL_ITEM;
        }
    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }

    class NormalItemViewHolder extends RecyclerView.ViewHolder {

        @Bind(R.id.item_date_tv)
        TextView mDateTv;
        @Bind(R.id.item_location_tv)
        TextView mLocationTv;
        @Bind(R.id.item_activity_tv)
        TextView mActivityTv;
        @Bind(R.id.label_circle)
        View mLabelCircle;

        View contentView;

        public NormalItemViewHolder(View itemView) {
            super(itemView);
            contentView = itemView;
            ButterKnife.bind(this, itemView);
        }
    }

    class SectionHeaderViewHolder extends RecyclerView.ViewHolder {
        @Bind(R.id.list_section_header)
        TextView mHeader;

        public SectionHeaderViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

    }
}
