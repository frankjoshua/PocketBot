package com.tesseractmobile.pocketbot.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.RectF;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by josh on 10/25/2015.
 */
public class JoystickView extends View {

    //Main circle
    private TouchPad mainPad;

    private TouchCircle[] mTouchCircles;
    private final static int CIRCLE_TOUCH = 0;
    private final static int CIRCLE_TOGGLE = 1;
    private final static int CIRCLE_A = 2;
    private final static int CIRCLE_B = 3;

    private boolean mButtonA;
    private boolean mButtonB;

    //Button colors
    private static final int COLOR_ON = Color.WHITE;
    private static final int COLOR_OFF = Color.DKGRAY;

    /** true if the user is touching */
    private boolean mHasFocus;
    /** if true joystick will hold position */
    private boolean mSticky = false;

    private boolean mChangingState = false;

    private JoystickListener mJoystickListener;

    public JoystickView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mainPad = new TouchPad(Color.parseColor("#bcb9e5"), 150);
        mTouchCircles = new TouchCircle[4];
        mTouchCircles[0] = new TouchCircle(Color.parseColor("#e59100"), Color.parseColor("#e59100"), 200);
        mTouchCircles[CIRCLE_TOGGLE] = new TouchCircle(Color.BLUE, Color.RED, 100);
        mTouchCircles[CIRCLE_A] = new TouchCircle(COLOR_OFF, COLOR_ON, 170);
        mTouchCircles[CIRCLE_B] = new TouchCircle(COLOR_OFF, COLOR_ON, 170);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        final int size = Math.min(w, h) / 10;
        //Set locations
        mainPad.set(w / 2, h / 2);
        //Center
        mTouchCircles[CIRCLE_TOUCH].set(w / 2, h / 2);
        //Bottom right
        mTouchCircles[CIRCLE_TOGGLE].set(size, h - size);
        //Top left
        mTouchCircles[CIRCLE_A].set(size, size);
        //Top right
        mTouchCircles[CIRCLE_B].set(w - size, size);

        //Set sizes
        mainPad.setSize(Math.min(w, h) / 2);
        mTouchCircles[CIRCLE_TOUCH].setSize(Math.round(size * 1.5f));
        mTouchCircles[CIRCLE_TOGGLE].setSize(size);
        mTouchCircles[CIRCLE_A].setSize(size);
        mTouchCircles[CIRCLE_B].setSize(size);


        int[] circleColors = new int[]{
                Color.parseColor("#bcb9e5"),
                Color.parseColor("#dddbe5"),
                Color.parseColor("#b3a2c1")
        };
        final Shader circleShader = new LinearGradient(0, 0, w, h, circleColors, null, Shader.TileMode.CLAMP);
        mainPad.setShader(circleShader);

        int[] touchColors = new int[]{
                Color.parseColor("#e59100"),
                Color.parseColor("#f2d7a8"),
                Color.parseColor("#e09900")
        };
        final Shader touchShader = new LinearGradient(0, 0, w, h, touchColors, null, Shader.TileMode.CLAMP);
        mTouchCircles[CIRCLE_TOUCH].setShader(touchShader);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        mainPad.draw(canvas);
        //Draw Circles
        for(int i = mTouchCircles.length - 1; i >= 0; i--){
            mTouchCircles[i].draw(canvas);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if(event.getAction() == MotionEvent.ACTION_DOWN){
            int touchedCircle = touchedCircle(event.getX(), event.getY());
            switch (touchedCircle){
                case CIRCLE_TOUCH:
                    mChangingState = false;
                    break;
                case CIRCLE_TOGGLE:
                    mChangingState = true;
                    break;
                case CIRCLE_A:
                    mButtonA = !mButtonA;
                    mTouchCircles[CIRCLE_A].toggle(mButtonA);
                    mChangingState = true;
                    break;
                case CIRCLE_B:
                    mButtonB = !mButtonB;
                    mTouchCircles[CIRCLE_B].toggle(mButtonB);
                    mChangingState = true;
                    break;
            }
            update();
        } else if(mChangingState == false && event.getAction() == MotionEvent.ACTION_MOVE){
            mTouchCircles[CIRCLE_TOUCH].set(Math.round(event.getX()), Math.round(event.getY()));
            update();
            setHasFocus(true);
        } else if (event.getAction() == MotionEvent.ACTION_UP){
            int touchedCircle = touchedCircle(event.getX(), event.getY());
            switch (touchedCircle){
                case CIRCLE_TOGGLE:
                    mSticky = !mSticky;
                    mTouchCircles[CIRCLE_TOGGLE].toggle(mSticky);
                    break;
                case CIRCLE_TOUCH:
                    break;
                case CIRCLE_A:
                    mButtonA = !mButtonA;
                    mTouchCircles[CIRCLE_A].toggle(mButtonA);
                    break;
                case CIRCLE_B:
                    mButtonB = !mButtonB;
                    mTouchCircles[CIRCLE_B].toggle(mButtonB);
                    break;
            }
            if(!mSticky) {
                mTouchCircles[CIRCLE_TOUCH].set(mainPad.point.x, mainPad.point.y);
                setHasFocus(false);
            }
            mChangingState = false;
            update();
        }
        invalidate();
        return true;
    }

    /**
     * Returns id of touched cirle or -1 if none
     * @param x
     * @param y
     * @return
     */
    private int touchedCircle(final float x, final float y){
        final int X = Math.round(x);
        final int Y = Math.round(y);
        for(int i = mTouchCircles.length - 1; i >= 0; i--){
            if(mTouchCircles[i].isTouched(X, Y)){
                return i;
            }
        }
        return -1;
    }

    private void update() {
        float cx = mTouchCircles[CIRCLE_TOUCH].point.x / (float) getWidth();
        float cy = mTouchCircles[CIRCLE_TOUCH].point.y / (float) getHeight();

        float x = cx * 2 - 1;
        float y = -(cy * 2 - 1);

        //Constrain values between -1 and 1
        x = Math.max(-1.0f, Math.min(1.0f, x));
        y = Math.max(-1.0f, Math.min(1.0f, y));

        mJoystickListener.onPositionChange(this, x, y, mHasFocus ? 1.0f : 0.0f, mButtonA, mButtonB);
    }

    public void setJoystickListener(final JoystickListener joystickListener){
        mJoystickListener = joystickListener;
    }

    private void setHasFocus(boolean hasFocus) {
        if(this.mHasFocus != hasFocus){
            this.mHasFocus = hasFocus;
            mJoystickListener.onFocusChange(this, hasFocus);
        }
    }

    private static class TouchCircle {
        public Paint paint = new Paint();
        public Point point = new Point();
        public int circleSize;
        private int mColorNormal;
        private int mColorSelected;

        public TouchCircle(final int color, final int colorSelected, final int alpha){
            paint.setColor(color);
            paint.setAlpha(alpha);
            mColorNormal = color;
            mColorSelected = colorSelected;
        }

        public void set(int x, int y) {
            point.set(x, y);
        }

        public void setSize(int size) {
            circleSize = size;
        }

        public void setShader(Shader shader) {
            paint.setShader(shader);
        }

        public void draw(Canvas canvas) {
            canvas.drawCircle(point.x, point.y, circleSize, paint);
        }

        public boolean isTouched(int x, int y) {
            final double distance = Math.sqrt(Math.pow((x - point.x), 2) + Math.pow((y - point.y), 2));
            return distance <= circleSize;
        }

        public void toggle(final boolean sticky){
            int alpha = paint.getAlpha();
            if(sticky){
                paint.setColor(mColorSelected);
            } else {
                paint.setColor(mColorNormal);
            }
            paint.setAlpha(alpha);
        }
    }

    public interface JoystickListener {
        /**
         * Called with the position of the joystick is updated
         * @param x -1.0 is far left, 1.0 is far right, 0.0 is center
         * @param y -1.0 is down, 1.0 is up, 0.0 is center
         */
        void onPositionChange(final JoystickView joystickView, float x, float y, float z, boolean a, boolean b);

        /**
         * True when user is touching false when they let go
         * @param hasFocus
         */
        void onFocusChange(final JoystickView joystickView, final boolean hasFocus);
    }

    private class TouchPad {
        public Paint paint = new Paint();
        public RectF rect = new RectF();
        public Point point = new Point();

        public TouchPad(final int color, final int alpha) {
            paint.setColor(color);
            paint.setAlpha(alpha);
        }

        public void set(final int x, final int y) {
            point.set(x, y);
        }

        public void setSize(final int size) {
            rect.set(point.x - size, point.y - size, point.x + size, point.y + size);
        }

        public void setShader(final Shader circleShader) {
            paint.setShader(circleShader);
        }

        public void draw(final Canvas canvas) {
            canvas.drawRoundRect(rect, rect.width() / 10, rect.width() / 10, paint);
        }
    }
}
