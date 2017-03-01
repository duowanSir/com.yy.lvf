package com.android.lvf.demo.draw;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.text.style.ReplacementSpan;

import com.android.lvf.LLog;

/**
 * Created by slowergun on 2017.02.28.
 */

public class MyReplacementSpan extends ReplacementSpan {

    @Override
    public int getSize(Paint paint, CharSequence text, int start, int end, Paint.FontMetricsInt fm) {

        LLog.printThreadStacks("MyReplacementSpan", "getSize|" + text+"|"+start+"|"+end+"|"+fm);

        return 0;
    }

    @Override
    public void draw(Canvas canvas, CharSequence text, int start, int end, float x, int top, int y, int bottom, Paint paint) {
        LLog.printThreadStacks("MyReplacementSpan", "draw|" + text+"|"+start+"|"+end+"|"+x+"|"+y+"|"+top+"|"+bottom);
    }
}
