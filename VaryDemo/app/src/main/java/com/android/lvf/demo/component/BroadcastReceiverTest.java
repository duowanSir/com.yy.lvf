package com.android.lvf.demo.component;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

/**
 * Created by 烽 on 2017/1/4.
 */

public class BroadcastReceiverTest extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Toast.makeText(context,"",Toast.LENGTH_SHORT).show();
    }

}
