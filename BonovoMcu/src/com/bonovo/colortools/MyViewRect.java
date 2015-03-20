package com.bonovo.colortools;

import android.view.View;
import android.graphics.Paint;
import android.content.Context;
import android.util.AttributeSet;
import android.graphics.Canvas;

public class MyViewRect extends View {
    Paint paint = new Paint();
    
    public MyViewRect(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawColor(-0x1);
        paint.setAntiAlias(true);
        paint.setStrokeWidth(3.0f);
        canvas.drawRect(0.0f, 0.0f, 0.0f, 0.0f, this);
    }
    
    public void setARGB(int A, int R, int G, int B) {
        paint.setARGB(A, R, G, B);
        invalidate();
    }
}
