package com.example.myapplication.activities;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.Handler;
import android.os.Looper;
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
    private Paint movingTargetPaint;
    
    private float targetX, targetY;
    private long startTime;
    private boolean isAnimating = false;
    private boolean showTemplate = true;

    public MotorTaskView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setupDrawing();
    }

    private void setupDrawing() {
        paint = new Paint();
        paint.setColor(Color.BLUE);
        paint.setAntiAlias(true);
        paint.setStrokeWidth(10f);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setStrokeCap(Paint.Cap.ROUND);

        targetPaint = new Paint();
        targetPaint.setColor(Color.parseColor("#E0E0E0"));
        targetPaint.setAntiAlias(true);
        targetPaint.setStrokeWidth(6f);
        targetPaint.setStyle(Paint.Style.STROKE);
        
        movingTargetPaint = new Paint();
        movingTargetPaint.setColor(Color.RED);
        movingTargetPaint.setAntiAlias(true);
        movingTargetPaint.setStyle(Paint.Style.FILL);

        drawPath = new Path();
    }

    public void setTaskType(String type) {
        this.taskType = type;
        reset();
        if ("Orbit".equalsIgnoreCase(taskType)) {
            startAnimation();
        } else if ("Ghost".equalsIgnoreCase(taskType)) {
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                showTemplate = false;
                invalidate();
            }, 3000);
        }
    }

    public void reset() {
        drawPath.reset();
        collectedPoints.clear();
        isAnimating = false;
        showTemplate = true;
        invalidate();
    }

    private void startAnimation() {
        isAnimating = true;
        startTime = System.currentTimeMillis();
        invalidate();
    }

    public List<PDSession.DataPoint> getCollectedPoints() {
        return new ArrayList<>(collectedPoints);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        
        if (showTemplate) {
            if ("Spiral".equalsIgnoreCase(taskType)) drawGuideSpiral(canvas);
            else if ("ZigZag".equalsIgnoreCase(taskType)) drawGuideZigZag(canvas);
            else if ("Mirror".equalsIgnoreCase(taskType)) drawGuideStar(canvas);
            else if ("Ghost".equalsIgnoreCase(taskType)) drawGuideComplex(canvas);
            else if ("Writing".equalsIgnoreCase(taskType)) drawGuideWriting(canvas);
        }

        if ("Orbit".equalsIgnoreCase(taskType) && isAnimating) {
            updateMovingTarget();
            canvas.drawCircle(targetX, targetY, 40f, movingTargetPaint);
            invalidate();
        }

        canvas.drawPath(drawPath, paint);
    }

    private void updateMovingTarget() {
        long elapsed = System.currentTimeMillis() - startTime;
        float t = elapsed / 1000f;
        float centerX = getWidth() / 2f;
        float centerY = getHeight() / 2f;
        float radius = Math.min(getWidth(), getHeight()) * 0.35f;
        targetX = centerX + radius * (float) Math.sin(1.2 * t);
        targetY = centerY + radius * (float) Math.cos(1.5 * t);
    }

    private void drawGuideSpiral(Canvas canvas) {
        float centerX = getWidth() / 2f;
        float centerY = getHeight() / 2f;
        Path spiral = new Path();
        spiral.moveTo(centerX, centerY);
        for (int i = 0; i < 720; i++) {
            float angle = (float) (i * Math.PI / 180);
            float r = i * 0.5f;
            float x = centerX + (float) (r * Math.cos(angle));
            float y = centerY + (float) (r * Math.sin(angle));
            spiral.lineTo(x, y);
        }
        canvas.drawPath(spiral, targetPaint);
    }

    private void drawGuideZigZag(Canvas canvas) {
        float w = getWidth(), h = getHeight();
        Path maze = new Path();
        maze.moveTo(w * 0.1f, h * 0.1f);
        maze.lineTo(w * 0.9f, h * 0.1f);
        maze.lineTo(w * 0.1f, h * 0.3f);
        maze.lineTo(w * 0.9f, h * 0.3f);
        maze.lineTo(w * 0.1f, h * 0.5f);
        maze.lineTo(w * 0.9f, h * 0.5f);
        maze.lineTo(w * 0.1f, h * 0.7f);
        canvas.drawPath(maze, targetPaint);
    }

    private void drawGuideStar(Canvas canvas) {
        float centerX = getWidth() / 2f, centerY = getHeight() / 2f;
        float r = Math.min(getWidth(), getHeight()) * 0.4f;
        Path star = new Path();
        for (int i = 0; i < 5; i++) {
            double angle = Math.toRadians(-90 + i * 144);
            float x = centerX + (float) (r * Math.cos(angle));
            float y = centerY + (float) (r * Math.sin(angle));
            if (i == 0) star.moveTo(x, y); else star.lineTo(x, y);
        }
        star.close();
        canvas.drawPath(star, targetPaint);
    }

    private void drawGuideComplex(Canvas canvas) {
        float cx = getWidth()/2, cy = getHeight()/2;
        Path p = new Path();
        for (int i = 0; i < 360; i++) {
            double t = Math.toRadians(i);
            float x = cx + (float) (getWidth() * 0.4 * Math.sin(t));
            float y = cy + (float) (getHeight() * 0.2 * Math.sin(2 * t));
            if (i == 0) p.moveTo(x, y); else p.lineTo(x, y);
        }
        canvas.drawPath(p, targetPaint);
    }

    private void drawGuideWriting(Canvas canvas) {
        Paint textPaint = new Paint(targetPaint);
        textPaint.setTextSize(60f);
        textPaint.setStyle(Paint.Style.FILL);
        canvas.drawText("The quick brown fox", 50, getHeight() * 0.4f, textPaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX(), y = event.getY();
        float processedX = ("Mirror".equalsIgnoreCase(taskType)) ? getWidth() - x : x;

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                drawPath.moveTo(x, y);
                break;
            case MotionEvent.ACTION_MOVE:
                drawPath.lineTo(x, y);
                break;
        }

        collectedPoints.add(new PDSession.DataPoint(processedX, y, event.getPressure(), System.currentTimeMillis()));
        invalidate();
        return true;
    }
}
