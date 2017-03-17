package com.android.lvf.demo.event;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.lvf.R;

/**
 * Created by slowergun on 2016/12/8.
 */
public class ActivityVideoList1 extends Activity implements View.OnClickListener {
    public static class Holder {
        public int      mPosition;
        public TextView mTv;
    }

    public static final String TAG = ActivityVideoList1.class.getSimpleName();

    private FrameLayout fl;
    private Button      btn;
    private ListView    mLv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_list1);
        fl = (FrameLayout) findViewById(R.id.fl);
        fl.setOnTouchListener(new View.OnTouchListener() {
            private float adX, adY;
            private float preX, preY;
            private float curX, curY;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        adX = preX = event.getX();
                        adY = preY = event.getY();
                        return true;
                    case MotionEvent.ACTION_MOVE:
                        curX = event.getX();
                        curY = event.getY();
                        if (Math.abs(curX - preX) < Math.abs(curY - preY) && Math.abs(curX - adX) < Math.abs(curY - adY)) {
                            fl.setVisibility(View.GONE);
                            return false;
                        }
                        return true;
                }
                return false;
            }
        });
        btn = (Button) findViewById(R.id.btn);
        mLv = (ListView) findViewById(R.id.lv);

        btn.setVisibility(View.GONE);
        mLv.setAdapter(new BaseAdapter() {

            @Override
            public int getCount() {
                return 20;
            }

            @Override
            public Object getItem(int position) {
                return String.valueOf(position);
            }

            @Override
            public long getItemId(int position) {
                return position;
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                Holder holder;
                if (convertView == null) {
                    convertView = LayoutInflater.from(ActivityVideoList1.this).inflate(R.layout.base_list_item, parent, false);
                    holder = new Holder();
                    holder.mTv = (TextView) convertView.findViewById(R.id.tv);
                    convertView.setTag(holder);
                } else {
                    holder = (Holder) convertView.getTag();
                }
                holder.mPosition = position;
                holder.mTv.setText((String) getItem(position));
                holder.mTv.setTag(holder);
                return convertView;
            }
        });
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.video_view) {
            Toast.makeText(this, "child", Toast.LENGTH_SHORT).show();
        } else if (v.getId() == R.id.video_container_layout) {
            Toast.makeText(this, "parent", Toast.LENGTH_SHORT).show();
        }
    }

}
