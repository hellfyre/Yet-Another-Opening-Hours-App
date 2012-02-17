package org.yaoha;

import java.util.ArrayList;
import java.util.TreeSet;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class TextAdapter extends BaseAdapter {
    private Context context;
    private ArrayList<HourRange> hourRanges;
    
    public TextAdapter(Context ctx, TreeSet<HourRange> hourRanges) {
        this.context = ctx;
        this.hourRanges = new ArrayList<HourRange>();
        for (HourRange hourRange : hourRanges) {
            this.hourRanges.add(hourRange);
        }
    }
    
    @Override
    public int getCount() {
        return hourRanges.size();
    }

    @Override
    public HourRange getItem(int position) {
        return hourRanges.get(position);
    }

    @Override
    public long getItemId(int position) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        RelativeLayout view;
        
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = (RelativeLayout) inflater.inflate(R.layout.hour_range_text_view, null);
        }
        else {
            view = (RelativeLayout) convertView;
        }
        
        TextView textView = (TextView) view.findViewById(R.id.hourRangeTextView);
        textView.setText(hourRanges.get(position).toString());
        
        return view;
    }

}
