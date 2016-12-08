package com.android.lvf;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

/**
 * Created by slowergun on 2016/12/8.
 */
public class HorizontalSlideActivity extends Activity {
    private ListView mlistView;
    String[] films = new String[]{
            "煎饼侠",
            "猎妖记",
            "大圣归来",
            "道士下山",
            "王朝的女人·杨贵妃",
            "栀子花开",
            "太平轮(下) ",};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_item_horizental_slideable);
        mlistView = (ListView) findViewById(R.id.listView);
        final TitleAdapter titleAdapter = new TitleAdapter(LayoutInflater.from(this), films);
        mlistView.setAdapter(titleAdapter);
    }

    private class TitleAdapter extends BaseAdapter {

        String[]       itemNames;
        LayoutInflater inflater;

        public TitleAdapter(LayoutInflater _inflater, String[] names) {
            inflater = _inflater;
            itemNames = names.clone();
        }

        @Override
        public Object getItem(int i) {
            return itemNames[i];
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public int getCount() {
            return itemNames.length;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            try {
                ViewHolder holder = new ViewHolder();
                if (view == null) {
                    view = inflater.inflate(R.layout.horizental_slide_layout, viewGroup, false);
                    holder.title = (TextView) view.findViewById(R.id.tv);
                    view.setTag(holder);
                } else {
                    holder = (ViewHolder) view.getTag();
                }

                if (holder != null && holder.title != null) {
                    TextView tv = holder.title;
                    tv.setText(itemNames[i]);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return view;
        }

        private class ViewHolder {
            TextView title;
        }
    }
}
