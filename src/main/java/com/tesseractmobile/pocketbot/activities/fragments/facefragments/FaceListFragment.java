package com.tesseractmobile.pocketbot.activities.fragments.facefragments;

import android.app.Activity;
import android.app.DialogFragment;
import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.tesseractmobile.pocketbot.R;
import com.tesseractmobile.pocketbot.activities.PocketBotSettings;

import java.util.List;

/**
 * Created by josh on 1/24/2016.
 */
public class FaceListFragment extends DialogFragment{

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.face_selector, container);
        final RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.rvFaces);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setHasFixedSize(false);

        recyclerView.setAdapter(new FaceListAdapter());

        return view;
    }

    private class FaceListAdapter extends RecyclerView.Adapter<FaceListAdapter.ViewHolder>{

        private List<FaceInfo> faceInfoList = FaceFragmentFactory.getFaceInfoList();

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            //Inflate the view
            final View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.face_list_item, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, int position) {
            final FaceInfo faceInfo = faceInfoList.get(position);
            holder.tvName.setText(faceInfo.name);
            holder.tvInfo.setText(faceInfo.info);
            holder.ivIcon.setImageResource(faceInfo.icon);
            //holder.ivIcon.setVisibility(View.INVISIBLE);
            //Lock if needed
            if(faceInfo.locked){
                holder.llLock.setVisibility(View.VISIBLE);
            } else {
                holder.llLock.setVisibility(View.GONE);
                holder.view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        final Activity activity = getActivity();
                        if(activity != null){
                            PocketBotSettings.setSelectedFace(activity, faceInfo.id);
                            dismiss();
                        }
                    }
                });
            }
        }

        @Override
        public int getItemCount() {
            faceInfoList = FaceFragmentFactory.getFaceInfoList();
            return faceInfoList.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder{
            final TextView tvName;
            final TextView tvInfo;
            final ImageView ivIcon;
            final LinearLayout llLock;
            final View view;

            public ViewHolder(final View view) {
                super(view);
                this.view = view;
                tvName = (TextView) view.findViewById(R.id.tvName);
                tvInfo = (TextView) view.findViewById(R.id.tvInfo);
                ivIcon = (ImageView) view.findViewById(R.id.ivFaceIcon);
                llLock = (LinearLayout) view.findViewById(R.id.llLock);
            }
        }
    }

}
