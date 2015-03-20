package com.bonovo.colortools;

import android.view.View;
import android.graphics.Paint;
import android.content.Context;
import android.util.AttributeSet;
import android.graphics.Canvas;

public class MyViewRectMID extends View {
    Paint paint = new Paint();
    
    public MyViewRectMID(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        paint.setAntiAlias(true);
        paint.setStrokeWidth(3.0f);
        canvas.drawRect(0.0f, 0.0f, 300.0f, 0.0f, this);
    }
    
    public void setARGB(int A, int R, int G, int B) {
        paint.setARGB(A, R, G, B);
        invalidate();
    }
}
