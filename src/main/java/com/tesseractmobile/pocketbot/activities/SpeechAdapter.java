package com.tesseractmobile.pocketbot.activities;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.tesseractmobile.pocketbot.R;

import java.util.ArrayList;

/**
 * Created by josh on 9/27/2015.
 */
public class SpeechAdapter extends BaseAdapter {

    private ArrayList<Speech> mSpeechList = new ArrayList<Speech>();
    private Context mContext;

    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            mSpeechList.add(mNewSpeech);
            notifyDataSetChanged();
        }
    };
    private Speech mNewSpeech;

    public SpeechAdapter(final Context context){
        mContext = context;
        mSpeechList.add(new Speech("Ready...", true));
    }

    @Override
    public int getCount() {
        return mSpeechList.size();
    }

    @Override
    public Object getItem(int position) {
        return mSpeechList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final View view;
        final ViewHolder viewHolder;
        if(convertView == null){
            //Create new view
            view = LayoutInflater.from(mContext).inflate(R.layout.speech_row, parent, false);
            viewHolder = new ViewHolder();
            view.setTag(viewHolder);
            viewHolder.tvText = (TextView) view.findViewById(R.id.tvText);
            viewHolder.ivIcon = (ImageView) view.findViewById(R.id.rowIcon);
        } else {
            view = convertView;
            viewHolder = (ViewHolder) view.getTag();
        }

        viewHolder.tvText.setText(mSpeechList.get(position).getText());
        if(mSpeechList.get(position).mIsPocketBot){
            viewHolder.ivIcon.setVisibility(View.VISIBLE);
        } else {
            viewHolder.ivIcon.setVisibility(View.GONE);
        }
        return view;
    }

    public void addText(String text, final boolean isPocketBot) {
        //Save the speech
        mNewSpeech = new Speech(text, isPocketBot);
        //Update the list
        mHandler.sendEmptyMessage(0);
    }

    static private class ViewHolder {
        public TextView tvText;
        public ImageView ivIcon;
    }

    static private class Speech {
        private String mText;
        private boolean mIsPocketBot;

        public Speech(final String text, final boolean isPocketBot){
            mText = text;
            mIsPocketBot = isPocketBot;
        }

        public String getText(){
            return mText;
        }
    }
}
