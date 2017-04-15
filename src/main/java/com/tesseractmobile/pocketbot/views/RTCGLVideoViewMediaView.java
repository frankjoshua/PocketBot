package com.tesseractmobile.pocketbot.views;

import android.content.Context;
import android.util.AttributeSet;

import com.quickblox.videochat.webrtc.view.RTCGLVideoView;

/**
 * Created by josh on 8/2/16.
 */
public class RTCGLVideoViewMediaView extends RTCGLVideoView {

    public RTCGLVideoViewMediaView(Context c, AttributeSet attr) {
        super(c, attr);
        setZOrderMediaOverlay(true);
    }

}
