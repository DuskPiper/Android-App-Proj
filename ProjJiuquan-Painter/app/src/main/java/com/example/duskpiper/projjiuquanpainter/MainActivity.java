package com.example.duskpiper.projjiuquanpainter;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.media.Image;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {
    private Button btnSave;
    private Button btnDrawOrStop;
    private Button btnClear;
    private ImageView ivCanvas;
    private TextView statusBar;

    private Paint paint;
    private Bitmap baseBitmap;
    private Canvas canvas;
    private String paintStatus; // "show", "draw"

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // BINDINGS
        btnClear = (Button)findViewById(R.id.main_btn_clear);
        btnDrawOrStop = (Button)findViewById(R.id.main_btn_draw_or_stop);
        btnSave = (Button)findViewById(R.id.main_btn_save);
        ivCanvas = (ImageView)findViewById(R.id.main_iv_canvas);
        statusBar = (TextView)findViewById(R.id.main_tv_pointer_status);

        // INITIALIZE
        paint = new Paint();
        paint.setStrokeWidth(5);
        paint.setColor(Color.CYAN);
        paintStatus = "draw";

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
                            baseBitmap = Bitmap.createBitmap(ivCanvas.getWidth(),
                                    ivCanvas.getHeight(), Bitmap.Config.ARGB_8888);
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

        /*
        View.OnTouchListener touch = new View.OnTouchListener() {
            float startX;
            float startY; // Finger touch starting positions

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        Log.d("Draw", "Case = touch down");
                        // INITIALIZE FOR FIRST DRAW
                        if (baseBitmap == null) {
                            baseBitmap = Bitmap.createBitmap(ivCanvas.getWidth(),
                                    ivCanvas.getHeight(), Bitmap.Config.ARGB_8888);
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
        };
        */
    }

    private void renewStatusBar(int pointerId, float x, float y) {
        String statusText = "PointerID:" + Integer.toString(pointerId) + "  X:" + Float.toString(x) + "  Y:" + Float.toString(y);
        statusBar.setText(statusText);
    }
}
