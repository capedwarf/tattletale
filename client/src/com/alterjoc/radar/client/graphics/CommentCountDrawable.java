package com.alterjoc.radar.client.graphics;

import android.graphics.*;
import android.graphics.drawable.BitmapDrawable;

public class CommentCountDrawable extends BitmapDrawable {

    private int commentCount;
    private int x;
    private boolean isRead;

    public CommentCountDrawable(Bitmap original, boolean isEventRead, int commentCount) {
        super(original);
        this.commentCount = commentCount;
        x = commentCount < 10 ? 5 : 2;
        isRead = isEventRead;
    }


    @Override
    public void draw(Canvas canvas) {
        Paint paint = new Paint();
        if (!isRead){
            setColorFilter(Color.YELLOW, PorterDuff.Mode.MULTIPLY);
            paint.setColor(Color.WHITE);
        } else {
            paint.setColor(Color.LTGRAY);
        }
        super.draw(canvas);
        clearColorFilter();


        paint.setTextSize(10);
        paint.setAntiAlias(true);
        paint.setFakeBoldText(true);
        paint.setStrokeWidth(2f);
        canvas.drawText(commentCount + "", x, 12, paint);
    }
}