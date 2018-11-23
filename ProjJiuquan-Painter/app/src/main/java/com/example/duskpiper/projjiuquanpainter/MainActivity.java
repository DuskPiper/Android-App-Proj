package com.example.duskpiper.projjiuquanpainter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.media.Image;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {
    private Button btnDrawOrStop;
    private Button btnClear;
    private ImageView ivCanvas;
    private TextView statusBar;

    private Paint paint;
    private Paint circlePaint;
    private Bitmap baseBitmap;
    private Canvas canvas;
    private String paintStatus; // "show", "draw"
    private float circleRadius = 100;


    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final RelativeLayout layout = (RelativeLayout)findViewById(R.id.main_layout_grandview);
        final View touchCircleView = new TouchCircleView(MainActivity.this);
        layout.addView(touchCircleView);
        //layout.removeView(touchCircleView);

        // BINDINGS
        btnClear = (Button)findViewById(R.id.main_btn_clear);
        btnDrawOrStop = (Button)findViewById(R.id.main_btn_draw_or_stop);
        ivCanvas = (ImageView)findViewById(R.id.main_iv_canvas);
        statusBar = (TextView)findViewById(R.id.main_tv_pointer_status);

        // INITIALIZE
        paint = new Paint();
        paint.setStrokeWidth(7);
        paint.setColor(Color.CYAN);
        circlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        circlePaint.setColor(Color.RED);
        circlePaint.setStyle(Paint.Style.FILL);
        paintStatus = "show";

        // SET LISTENERS
        ivCanvas.setOnTouchListener(new View.OnTouchListener() {
            float startX;
            float startY; // Finger touch starting positions

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        Log.d("Draw", "Case = touch down");
                        // INITIALIZE FOR FIRST DRAW
                        if (baseBitmap == null) {
                            baseBitmap = Bitmap.createBitmap(ivCanvas.getWidth(), ivCanvas.getHeight(), Bitmap.Config.ARGB_8888);
                            canvas = new Canvas(baseBitmap);
                            canvas.drawColor(Color.WHITE);
                        }
                        // RECORD START POSITION
                        startX = event.getX();
                        startY = event.getY();
                        renewStatusBar(0, startX, startY);
                        break;
                    // FINGER MOVING
                    case MotionEvent.ACTION_MOVE:
                        Log.d("Draw", "Case = touch move");
                        // RECORD STOP POSITION
                        float stopX = event.getX();
                        float stopY = event.getY();

                        // CREATE LINK BETWEEN START AND STOP SPOTS
                        if (paintStatus.equals("draw")) {
                            canvas.drawLine(startX, startY, stopX, stopY, paint);
                        }

                        // RENEW START POSITION
                        startX = event.getX();
                        startY = event.getY();
                        renewStatusBar(0, startX, startY);

                        // SHOW IMAGE
                        if (paintStatus.equals("draw")) {
                            ivCanvas.setImageBitmap(baseBitmap);
                        }
                        break;
                    case MotionEvent.ACTION_UP:
                        Log.d("Draw", "Case = touch up");
                        startX = event.getX();
                        startY = event.getY();
                        renewStatusBar(0, startX, startY);
                        break;
                    default:
                        startX = event.getX();
                        startY = event.getY();
                        renewStatusBar(0, startX, startY);
                        break;
                }
                return true;
            }

        });

        btnDrawOrStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (paintStatus.equals("show")) {
                    paintStatus = "draw";
                    btnDrawOrStop.setText("STOP DRAW");
                    layout.removeView(touchCircleView);
                    Log.d("Mode Button", "Switched to mode: DRAW");
                } else {
                    paintStatus = "show";
                    btnDrawOrStop.setText("DRAW");
                    layout.addView(touchCircleView);
                    Log.d("Mode Button", "Switched to mode: SHOW");
                }
            }
        });

        btnClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                baseBitmap = Bitmap.createBitmap(ivCanvas.getWidth(),
                        ivCanvas.getHeight(), Bitmap.Config.ARGB_8888);
                canvas = new Canvas(baseBitmap);
                canvas.drawColor(Color.WHITE);
                ivCanvas.setImageBitmap(baseBitmap);
            }
        });
    }

    class TouchCircleView extends SurfaceView {
        // VIEW THAT SHOW TOUCH FINGERPRINTS
        private final SurfaceHolder surfaceHolder;
        private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private Canvas canvas;

        public TouchCircleView(Context context) {
            super(context);
            surfaceHolder = getHolder();
            paint.setColor(Color.RED);
            paint.setStyle(Paint.Style.FILL);
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {
            //Canvas canvas;
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    if (surfaceHolder.getSurface().isValid()) {
                        canvas = surfaceHolder.lockCanvas();
                        canvas.drawColor(Color.BLACK);
                        canvas.drawCircle(event.getX(), event.getY(), circleRadius, paint);
                        surfaceHolder.unlockCanvasAndPost(canvas);
                    }
                    break;
                case MotionEvent.ACTION_MOVE:
                    if (surfaceHolder.getSurface().isValid()) {
                        canvas = surfaceHolder.lockCanvas();
                        canvas.drawColor(Color.BLACK);
                        canvas.drawCircle(event.getX(), event.getY(), circleRadius, paint);
                        surfaceHolder.unlockCanvasAndPost(canvas);
                    }
                    break;
                case MotionEvent.ACTION_UP:
                    if (surfaceHolder.getSurface().isValid()) {
                        canvas = surfaceHolder.lockCanvas();
                        canvas.drawColor(Color.BLACK, PorterDuff.Mode.CLEAR);
                        //canvas.drawCircle(event.getX(), event.getY(), circleRadius, paint);
                        surfaceHolder.unlockCanvasAndPost(canvas);
                    }
                default:
                    break;
            }
            return true;
        }
    }

    private void renewStatusBar(int pointerId, float x, float y) {
        String statusText = "PointerID:" + Integer.toString(pointerId) + "  X:" + Float.toString(x) + "  Y:" + Float.toString(y);
        statusBar.setText(statusText);
    }
}
