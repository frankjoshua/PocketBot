package com.tesseractmobile.pocketbot.views;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;

/**
 * Created by josh on 1/26/2016.
 */
public class CartoonMouthView extends MouthView{
    public CartoonMouthView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void drawTeeth(Canvas canvas) {
        //Don't draw the teeth
    }
}
