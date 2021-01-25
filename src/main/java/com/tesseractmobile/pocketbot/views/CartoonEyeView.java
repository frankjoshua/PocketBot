package com.tesseractmobile.pocketbot.views;

import android.content.Context;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.util.AttributeSet;

import com.tesseractmobile.pocketbot.robot.faces.EyeAttributes;

/**
 * Created by josh on 1/27/2016.
 */
public class CartoonEyeView extends EyeView {
    public CartoonEyeView(Context context, AttributeSet attrs) {
        super(context, attrs);
        EyeAttributes eyeAttributes = new EyeAttributes();
        eyeAttributes.eyelidColor = Color.parseColor("#7A5B78");
        eyeAttributes.irisColor = Color.parseColor("#A8D4A9");
        eyeAttributes.eyeBallGradientStartColor = Color.parseColor("#CdB6B6");
        eyeAttributes.eyeBallGradientEndColor = Color.parseColor("#ffffff");
        eyeAttributes.eyeOuterGradientStartColor = Color.parseColor("#9BC7A0");
        eyeAttributes.eyeOuterGradientEndColor = Color.parseColor("#9BC7AC");
        setEyeAttributes(eyeAttributes);
    }

}
