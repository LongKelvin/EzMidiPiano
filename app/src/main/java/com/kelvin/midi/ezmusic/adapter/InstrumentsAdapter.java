package com.kelvin.midi.ezmusic.adapter;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.kelvin.midi.ezmusic.R;
import com.kelvin.midi.ezmusic.object.Instruments;

import java.util.List;



public class InstrumentsAdapter extends ArrayAdapter<Instruments> {
    private final List<Instruments>  list;
    private final Activity context;

    public InstrumentsAdapter(Activity context,List<Instruments> list) {
        super(context, R.layout.activity_instruments_adapter, list);
        this.list = list;
        this.context = context;
    }

    static class ViewHolder {
        protected TextView instrumentName;
        protected ImageView instrumentImage;
    }



    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view;

        if (convertView == null) {
            LayoutInflater inflater = context.getLayoutInflater();
            view = inflater.inflate(R.layout.activity_instruments_adapter, null);
            final ViewHolder viewHolder = new ViewHolder();
            viewHolder.instrumentName = view.findViewById(R.id.name);
            viewHolder.instrumentImage = view.findViewById(R.id.image);
            view.setTag(viewHolder);
        } else {
            view = convertView;
        }

        ViewHolder holder = (ViewHolder) view.getTag();
        holder.instrumentName.setText(list.get(position).getInstrumentsName());
        holder.instrumentImage.setImageDrawable(list.get(position).getInstrumentsImage());
        return view;
    }
}
