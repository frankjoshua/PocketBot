package com.tesseractmobile.pocketbot.views;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.tesseractmobile.pocketbot.R;

/**
 * Created by josh on 12/29/2015.
 */
public class RobotInfoViewHolder extends RecyclerView.ViewHolder {
    final public TextView robotName;
    final public TextView robotStatus;
    final public LinearLayout mainLayout;
    final public Button btnDelete;

    public RobotInfoViewHolder(final View itemView) {
        super(itemView);
        robotName = (TextView) itemView.findViewById(R.id.tvRobotName);
        robotStatus = (TextView) itemView.findViewById(R.id.tvRobotStatus);
        mainLayout = (LinearLayout) itemView.findViewById(R.id.llListItem);
        btnDelete = (Button) itemView.findViewById(R.id.btnDelete);
    }
}
