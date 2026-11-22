package com.example.digitrecognizer;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

/**
 * Custom View that lets the user draw a digit with a finger.
 * Drawing is white on a black background, exactly like the MNIST dataset,
 * so the model receives data in the format it was trained on.
 */
public class DrawingView extends View {

    private Bitmap bitmap;        // off-screen bitmap that stores the finished strokes
    private Canvas bitmapCanvas;  // canvas used to draw into that bitmap
    private final Path path = new Path();   // stroke currently being drawn
    private final Paint paint = new Paint(); // brush settings

    public DrawingView(Context context, AttributeSet attrs) {
        super(context, attrs);
        // Thick white round brush: thick strokes look like MNIST digits after downscaling
        paint.setColor(Color.WHITE);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(28f);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setAntiAlias(true);
    }

    /** Called when the view gets its size: create the bitmap with a black background. */
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        bitmapCanvas = new Canvas(bitmap);
        bitmapCanvas.drawColor(Color.BLACK);
    }

    /** Draws the saved strokes and the stroke currently in progress. */
    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawBitmap(bitmap, 0, 0, null);
        canvas.drawPath(path, paint);
    }

    /** Handles finger touch events to build the stroke path. */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                path.moveTo(x, y);          // finger touched: start a new stroke
                break;
            case MotionEvent.ACTION_MOVE:
                path.lineTo(x, y);          // finger moving: extend the stroke
                break;
            case MotionEvent.ACTION_UP:
                bitmapCanvas.drawPath(path, paint); // finger lifted: save stroke into bitmap
                path.reset();
                break;
        }
        invalidate(); // request redraw
        return true;
    }

    /** Erases everything and restores the black background. */
    public void clear() {
        path.reset();
        bitmapCanvas.drawColor(Color.BLACK);
        invalidate();
    }

    /** Returns the drawing as a bitmap (used for classification). */
    public Bitmap getBitmap() {
        return bitmap;
    }
}