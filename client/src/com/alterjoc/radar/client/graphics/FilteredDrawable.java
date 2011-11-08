package com.alterjoc.radar.client.graphics;

import android.graphics.*;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

/**
 * User: Dejan
 * Date: 3.1.11
 * Time: 19:45
 */
public class FilteredDrawable extends BitmapDrawable {

    public FilteredDrawable(BitmapDrawable original) {
        super(original.getBitmap());
    }


    @Override
    public void draw(Canvas canvas) {
        setColorFilter(Color.YELLOW, PorterDuff.Mode.MULTIPLY);
        super.draw(canvas);
        clearColorFilter();
    }
}