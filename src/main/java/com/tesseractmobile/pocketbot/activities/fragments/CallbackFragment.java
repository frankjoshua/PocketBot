package com.tesseractmobile.pocketbot.activities.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by josh on 10/18/2015.
 */
abstract public class CallbackFragment extends Fragment {

    public interface OnCompleteListener {
        void onComplete();
    }

    private OnCompleteListener mOnCompleteListener;

    @Override
    final public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = createView(inflater, container, savedInstanceState);
        if(mOnCompleteListener != null){
            mOnCompleteListener.onComplete();
        }
        return view;
    }

    protected abstract View createView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState);

    public void setOnCompleteListener(OnCompleteListener mOnCompleteListener) {
        this.mOnCompleteListener = mOnCompleteListener;
    }
}
