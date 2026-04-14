package com.example.myapplication.activities;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.example.myapplication.models.PDSession;

import java.util.ArrayList;
import java.util.List;

public class MotorTaskView extends View {

    private Paint paint;
    private Path drawPath;
    private List<PDSession.DataPoint> collectedPoints = new ArrayList<>();
    
    private String taskType = "Spiral";
    private Paint targetPaint;

    public MotorTaskView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setupDrawing();
    }

    private void setupDrawing() {
        paint = new Paint();
        paint.setColor(Color.BLUE);
        paint.setAntiAlias(true);
        paint.setStrokeWidth(8f);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setStrokeCap(Paint.Cap.ROUND);

        targetPaint = new Paint();
        targetPaint.setColor(Color.LTGRAY);
        targetPaint.setAntiAlias(true);
        targetPaint.setStrokeWidth(4f);
        targetPaint.setStyle(Paint.Style.STROKE);

        drawPath = new Path();
    }

    public void setTaskType(String type) {
        this.taskType = type;
        reset();
    }

    public void reset() {
        drawPath.reset();
        collectedPoints.clear();
        invalidate();
    }

    public List<PDSession.DataPoint> getCollectedPoints() {
        return new ArrayList<>(collectedPoints);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        
        // Draw Task Template (e.g., a guide spiral)
        if ("Spiral".equalsIgnoreCase(taskType)) {
            drawGuideSpiral(canvas);
        } else if ("Maze".equalsIgnoreCase(taskType)) {
            drawGuideMaze(canvas);
        }

        // Draw User Path
        canvas.drawPath(drawPath, paint);
    }

    private void drawGuideSpiral(Canvas canvas) {
        float centerX = getWidth() / 2f;
        float centerY = getHeight() / 2f;
        Path spiral = new Path();
        float radius = 0;
        float angle = 0;
        spiral.moveTo(centerX, centerY);
        for (int i = 0; i < 720; i++) {
            angle = (float) (i * Math.PI / 180);
            radius = i * 0.5f;
            float x = centerX + (float) (radius * Math.cos(angle));
            float y = centerY + (float) (radius * Math.sin(angle));
            spiral.lineTo(x, y);
        }
        canvas.drawPath(spiral, targetPaint);
    }

    private void drawGuideMaze(Canvas canvas) {
        // Simple Zig-Zag Maze guide
        float w = getWidth();
        float h = getHeight();
        Path maze = new Path();
        maze.moveTo(w * 0.1f, h * 0.1f);
        maze.lineTo(w * 0.9f, h * 0.1f);
        maze.lineTo(w * 0.1f, h * 0.3f);
        maze.lineTo(w * 0.9f, h * 0.3f);
        maze.lineTo(w * 0.1f, h * 0.5f);
        canvas.drawPath(maze, targetPaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float touchX = event.getX();
        float touchY = event.getY();
        long ts = System.currentTimeMillis();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                drawPath.moveTo(touchX, touchY);
                break;
            case MotionEvent.ACTION_MOVE:
                drawPath.lineTo(touchX, touchY);
                break;
            case MotionEvent.ACTION_UP:
                break;
            default:
                return false;
        }

        collectedPoints.add(new PDSession.DataPoint(touchX, touchY, event.getPressure(), ts));
        invalidate();
        return true;
    }
}
