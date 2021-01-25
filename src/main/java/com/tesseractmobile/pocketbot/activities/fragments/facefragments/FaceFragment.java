package com.tesseractmobile.pocketbot.activities.fragments.facefragments;

import com.tesseractmobile.pocketbot.activities.fragments.CallbackFragment;
import com.tesseractmobile.pocketbot.robot.faces.RobotFace;
import com.tesseractmobile.pocketbot.robot.faces.RobotInterface;
import com.tesseractmobile.pocketbot.robot.model.Face;

/**
 * Created by josh on 10/18/2015.
 */
abstract public class FaceFragment extends CallbackFragment {

    private RobotFace face;
    /**
     * True if using face tracing
     * @return
     */
    abstract public boolean isUseFaceTracking();

    final protected void setFace(final RobotFace face){
        this.face = face;
    }

    @Override
    public void onDestroyView() {
        final RobotFace face = this.face;
        if(face != null){
            face.onDestroy();
        }
        super.onDestroyView();
    }
}
