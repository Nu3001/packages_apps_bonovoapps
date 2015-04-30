package com.bonovo.colortools;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

public class MyViewRect extends View{
	
	Paint paint = new Paint();
	
	public MyViewRect(Context context, AttributeSet attrs){
		super(context,attrs);
	}
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		
		canvas.drawColor(Color.WHITE);
		
		paint.setAntiAlias(true);
		paint.setStrokeWidth(3);
//		paint.setStyle(Style.MORPH);
//		paint.setColor(Color.BLUE);
		canvas.drawRect(0, 0, 50, 50, paint);
//		RectF rectf_body = new RectF(0,0,50,50);
//		canvas.drawRoundRect(rectf_body, 10, 10, paint);
	}
	
//	public void setColor(int color) {
//		// TODO Auto-generated method stub
//		paint.setColor(color);
//		invalidate();
//	}
	
	public void setARGB(int A, int R, int G, int B) {
		// TODO Auto-generated method stub
		paint.setARGB(A, R, G, B);
		invalidate();
	}
	
}




