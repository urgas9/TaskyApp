package si.uni_lj.fri.taskyapp.adapter;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import si.uni_lj.fri.taskyapp.R;

/**
 * Created by urgas9 on 8. 02. 2016.
 */
public class SimpleTextArrayAdapter extends ArrayAdapter<String> {

    String[] texts;
    Context mContext;

    public SimpleTextArrayAdapter(Context context, int resource, String[] objects) {
        super(context, resource, objects);
        this.texts = objects;
        this.mContext = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = super.getView(position, convertView, parent);
        ((TextView) v.findViewById(R.id.text_item)).setText(texts[position]);
        return v;
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        View v = super.getDropDownView(position, convertView, parent);
        //v.setBackgroundResource(R.drawable.spinner_bg);

        TextView textView = (TextView) v.findViewById(R.id.text_item);
        textView.setText(texts[position]);
        textView.setTextColor(ContextCompat.getColor(mContext, R.color.black));

        return v;
    }
}
