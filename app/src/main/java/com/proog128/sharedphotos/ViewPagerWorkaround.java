package com.proog128.sharedphotos;

import android.content.Context;
import androidx.viewpager.widget.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

// http://stackoverflow.com/questions/16459196/java-lang-illegalargumentexception-pointerindex-out-of-range-exception-dispat
public class ViewPagerWorkaround extends ViewPager {
    public ViewPagerWorkaround(Context context) {
        super(context);
    }

    public ViewPagerWorkaround(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        boolean r = false;

        try {
            r = super.onInterceptTouchEvent(ev);
        }
        catch(Exception e) {

        }

        return r;
    }
}
