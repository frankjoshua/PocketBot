package com.tesseractmobile.pocketbot.views;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.media.audiofx.Visualizer;
import android.media.audiofx.Visualizer.OnDataCaptureListener;
import android.os.Build;
import android.os.Handler;
import android.os.SystemClock;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.speech.tts.UtteranceProgressListener;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.Gravity;
import android.widget.TextView;

import com.tesseractmobile.pocketbot.R;
import com.tesseractmobile.pocketbot.activities.SpeechState;
import com.tesseractmobile.pocketbot.robot.Robot;
import com.tesseractmobile.pocketbot.robot.SpeechStateListener;

import java.util.HashMap;

public class MouthView extends TextView implements OnInitListener, OnDataCaptureListener, SpeechStateListener{

    final Handler handler = new Handler();
    private final TextToSpeech mTts;
    private boolean isTalkReady;
    private State mState;
    private final Paint mMouthPaint;
    private SpeechCompleteListener mSpeechCompleteListener;

    static final private RectF DEST_RECT = new RectF();
    private HashMap<String, Boolean> mActiveUtterance = new HashMap<String, Boolean>();

    private Bitmap[] mMouthBitmaps;
    private Bitmap[] mMouthStaticBitmaps;
    private Bitmap mMicrophoneBitmap;
    private Paint mMicrophonePaint;
    private int mCurrentBitmap = 0;
    private long mLastChange;
    private Path mTeethPath;
    private Paint mTeethPaint;
    private int mMouthDx = 100;
    private int mMouthDy = 100;
    private boolean mListening = false;

    public MouthView(final Context context, final AttributeSet attrs) {
        super(context, attrs);
        setTextColor(Color.rgb(100, 0, 255));
        setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);

        mTts = new TextToSpeech(context, this);
        if(Build.VERSION.SDK_INT >= 15) {
            mTts.setOnUtteranceProgressListener(new UtteranceProgressListener() {

                @Override
                public void onStart(final String utteranceId) {
                    setmState(State.TALKING);
                    //Add to active utterance list
                    synchronized (MouthView.this) {
                        mActiveUtterance.put(utteranceId, true);
                    }
                }

                @Override
                public void onError(final String utteranceId) {
                    // TODO Auto-generated method stub

                }

                @Override
                public void onDone(final String utteranceId) {
                    synchronized (MouthView.this) {
                        mActiveUtterance.remove(utteranceId);
                        if (mActiveUtterance.size() == 0) {
                            setmState(State.NOT_TALKING);
                        }
                    }
                }
            });

        }

        mMouthPaint = new Paint();
        mMouthPaint.setColor(Color.WHITE);

        mTeethPaint = new Paint();
        mTeethPaint.setStrokeWidth(10);
        mTeethPaint.setStyle(Paint.Style.STROKE);
        mTeethPaint.setColor(Color.BLACK);

        mMouthStaticBitmaps = new Bitmap[3];
        mMouthStaticBitmaps[0] = BitmapFactory.decodeResource(getResources(), R.drawable.staticmouth);
        mMouthStaticBitmaps[1] = BitmapFactory.decodeResource(getResources(), R.drawable.staticmouthhappy);
        mMouthStaticBitmaps[2] = BitmapFactory.decodeResource(getResources(), R.drawable.staticmouthsad);

        mMouthBitmaps = new Bitmap[4];
        mMouthBitmaps[0] = mMouthStaticBitmaps[1];
        mMouthBitmaps[1] = BitmapFactory.decodeResource(getResources(), R.drawable.normalmouth1);
        mMouthBitmaps[2] = BitmapFactory.decodeResource(getResources(), R.drawable.normalmouth2);
        mMouthBitmaps[3] = BitmapFactory.decodeResource(getResources(), R.drawable.normalmouth3);

        mMicrophoneBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.microphone);
        mMicrophonePaint = new Paint();
        mMicrophonePaint.setAlpha(100);

        //Listen for speech state changes
        Robot.get().registerSpeechChangeListener(this);
    }

    
    @Override
    protected void onDraw(final Canvas canvas) {
        DEST_RECT.set(0, 0, canvas.getWidth(), canvas.getHeight());
        if(mState == State.TALKING){
            invalidate();
            if(SystemClock.uptimeMillis() - mLastChange > 75){
                mLastChange = SystemClock.uptimeMillis();
                mCurrentBitmap++;
                if(mCurrentBitmap > 3){
                    mCurrentBitmap = 0;
                }
            }
            if(mMouthDx < canvas.getWidth()){
                mMouthDx += 30;
            }
            canvas.drawRoundRect(DEST_RECT, mMouthDx, mMouthDy, mMouthPaint);
        } else {
            if(mMouthDx > 100){
                mMouthDx -= 30;
                invalidate();
            }
            canvas.drawRoundRect(DEST_RECT, mMouthDx, mMouthDy, mMouthPaint);
        }

        canvas.drawBitmap(mMouthBitmaps[mCurrentBitmap], null, DEST_RECT, null);
        //Draw teeth
        drawTeeth(canvas);
        //Draw microphone if listening
        if(mListening){
            canvas.drawBitmap(mMicrophoneBitmap, canvas.getWidth() / 2 - mMicrophoneBitmap.getWidth() / 2, canvas.getHeight() / 2 - mMicrophoneBitmap.getHeight() / 2, mMicrophonePaint);
        }
        //Draw text
        //super.onDraw(canvas);
    }

    protected void drawTeeth(Canvas canvas) {
        canvas.drawPath(mTeethPath, mTeethPaint);
    }


    @Override
    protected void onSizeChanged(final int w, final int h, final int oldw, final int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        //Check for audio permission
        int permissionCheck = ContextCompat.checkSelfPermission(getContext(),
                Manifest.permission.RECORD_AUDIO);
        if(permissionCheck == PackageManager.PERMISSION_GRANTED) {
            //Start the visualizer to animated the mouth
            final Visualizer mVisualizer = new Visualizer(0);
            mVisualizer.setDataCaptureListener(MouthView.this, Visualizer.getMaxCaptureRate() / 2, true, false);
            mVisualizer.setEnabled(true);
        }

        //Create path for teeth
        mTeethPath = new Path();
        final int teeth = 6;
        final int toothWidth = Math.round(w * 1.2f / teeth);
        for(int i = 1; i < teeth; i++){
            int x = Math.round(toothWidth * i - w * .1f);
            mTeethPath.moveTo(x, 0);
            mTeethPath.lineTo(x, h);
        }

    }



    @Override
    public void setText(final CharSequence text, final BufferType type) {
        if (handler != null) {
            if(text == null){
                super.setText("Error null text!", type);
                return;
            }
            //Speak if ready
            if(isTalkReady){
                final HashMap<String, String> map = new HashMap<String, String>();
                map.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "UniqueID");
                mTts.speak(text.toString(), TextToSpeech.QUEUE_FLUSH, map);
                
            }
        }
        super.setText(text, type);
    }

    @Override
    public void onInit(final int status) {
        isTalkReady = true;
    }

    @Override
    protected void onDetachedFromWindow() {
        mTts.stop();
        mTts.shutdown();
        super.onDetachedFromWindow();
    }

    /**
     * @return the mState
     */
    private State getmState() {
        return mState;
    }

    /**
     * @param state the mState to set
     */
    private void setmState(final State state) {
        this.mState = state;
        if(state == State.NOT_TALKING){
            mCurrentBitmap = 0;
            //Let the listener know the the speech is complete
            final SpeechCompleteListener speechCompleteListener = mSpeechCompleteListener;
            if(speechCompleteListener != null){
                speechCompleteListener.onSpeechComplete();
                //Listeners only get informed once
                //mSpeechCompleteListener = null;
            }
        }
        postInvalidate();
    }

    public void smile() {
        mMouthBitmaps[0] = mMouthStaticBitmaps[1];
        invalidate();
    }

    public void frown() {
        mMouthBitmaps[0] = mMouthStaticBitmaps[2];
        invalidate();
    }

    public void nuetral(){
        mMouthBitmaps[0] = mMouthStaticBitmaps[0];
        invalidate();
    }

    @Override
    public void onSpeechStateChange(SpeechState speechState) {
        if(speechState == SpeechState.LISTENING){
            mListening = true;
        } else {
            mListening = false;
        }
        postInvalidate();
    }

    private enum State {
        TALKING, NOT_TALKING
    }

    @Override
    public void onWaveFormDataCapture(final Visualizer visualizer, final byte[] waveform, final int samplingRate) {
        //Log.d("wave", Arrays.toString(waveform));
        updateWave(waveform);
    }


    /**
     * @param waveform
     */
    public void updateWave(final byte[] waveform) {
        invalidate();
    }

    public void setOnSpeechCompleteListener(final SpeechCompleteListener speechCompleteListener){
        this.mSpeechCompleteListener = speechCompleteListener;
    }

    @Override
    public void onFftDataCapture(final Visualizer visualizer, final byte[] fft, final int samplingRate) {
        updateWave(fft);
    }



    public interface SpeechCompleteListener {
        public void onSpeechComplete();
    }
}
