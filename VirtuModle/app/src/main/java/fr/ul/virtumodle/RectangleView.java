package fr.ul.virtumodle;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.view.View;

public class RectangleView extends View {

    private final Paint paint = new Paint();
    private final RectF rect = new RectF();

    public RectangleView(Context context, float left, float top, float right, float bottom) {
        super(context);


        rect.set(left, top, right, bottom);


        paint.setColor(0xFFFF0000);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(10);
        paint.setAntiAlias(true); 
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawRect(rect, paint);
    }
}
