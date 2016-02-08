package si.uni_lj.fri.taskyapp.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import si.uni_lj.fri.taskyapp.R;

/**
 * Created by urgas9 on 8. 02. 2016.
 *
 */
public class SimpleTextArrayAdapter extends ArrayAdapter<String> {

    String[] texts;

    public SimpleTextArrayAdapter(Context context, int resource, String[] objects) {
        super(context, resource, objects);
        texts = objects;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = super.getView(position, convertView, parent);
        ((TextView)v.findViewById(R.id.text_item)).setText(texts[position]);
        return v;
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        View v = super.getDropDownView(position, convertView, parent);
        //v.setBackgroundResource(R.drawable.spinner_bg);

        ((TextView)v.findViewById(R.id.text_item)).setText(texts[position]);
        return v;
    }
}
