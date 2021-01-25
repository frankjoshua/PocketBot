package com.tesseractmobile.pocketbot.views;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;

import com.tesseractmobile.pocketbot.robot.faces.EyeAttributes;

/**
 * Created by josh on 7/15/16.
 */
public class ScratchyEyeView extends EyeView {

    public ScratchyEyeView(Context context, AttributeSet attrs) {
        super(context, attrs);
        EyeAttributes eyeAttributes = new EyeAttributes();
        eyeAttributes.eyelidColor = Color.parseColor("#8c8d91");
        eyeAttributes.irisColor = Color.parseColor("#99a7d3");
        eyeAttributes.eyeBallGradientStartColor = Color.parseColor("#ececec");
        eyeAttributes.eyeBallGradientEndColor = Color.parseColor("#ffffff");
        eyeAttributes.eyeOuterGradientStartColor = Color.parseColor("#a25fa8");
        eyeAttributes.eyeOuterGradientEndColor = Color.parseColor("#855fa7");
        setEyeAttributes(eyeAttributes);
    }

}
