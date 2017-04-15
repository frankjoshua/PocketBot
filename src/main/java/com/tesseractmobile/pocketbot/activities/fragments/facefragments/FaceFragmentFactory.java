package com.tesseractmobile.pocketbot.activities.fragments.facefragments;

import com.tesseractmobile.pocketbot.R;
import com.tesseractmobile.pocketbot.robot.Robot;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by josh on 1/24/2016.
 */
public class FaceFragmentFactory {

    public static final int ID_FACE_EFIM = 0;
    public static final int ID_FACE_CONTROL = 1;
    public static final int ID_FACE_TELEPRESENCE = 2;
    public static final int ID_FACE_TELEPRESENCE_EFIM = 3;
    public static final int ID_FACE_ALIEN = 4;
    public static final int ID_FACE_NEW = 5;

    public static FaceFragment getFaceFragment(final int faceId) {
        final FaceFragment faceFragment;
        switch (faceId){
            case ID_FACE_EFIM:
                faceFragment = new EfimFaceFragment();
                break;
            case ID_FACE_CONTROL:
                faceFragment = new ControlFaceFragment();
                break;
            case ID_FACE_TELEPRESENCE:
                faceFragment = new TelepresenceFaceFragment();
                break;
            case ID_FACE_TELEPRESENCE_EFIM:
                faceFragment = new EfimTelepresenceFaceFragment();
                break;
            case ID_FACE_ALIEN:
                faceFragment = new CartoonFaceFragment();
                break;
            case ID_FACE_NEW:
                faceFragment = new ScratchyFaceFragment();
                break;
            default:
                throw new UnsupportedOperationException("Unknown face id " + faceId);
        }
        return faceFragment;
    }

    public static ArrayList<FaceInfo> getFaceInfoList() {
        final ArrayList<FaceInfo> faceList = new ArrayList<>();
        faceList.add(new FaceInfo(ID_FACE_EFIM, R.drawable.ic_launcher, R.drawable.efim_background_texture, "Robot", "Face tracking is active and AI is in total control.", false));
        faceList.add(new FaceInfo(ID_FACE_ALIEN, R.drawable.ic_face_fuzzy, R.drawable.purple_fuzz, "Fuzzy", "Face tracking is active and AI is in total control.", false));
        faceList.add(new FaceInfo(ID_FACE_NEW, R.drawable.ic_face_scratchy, R.drawable.purple_fuzz, "Scratchy", "Face tracking is active and AI is in total control.", false));
        faceList.add(new FaceInfo(ID_FACE_CONTROL, R.drawable.ic_control, R.drawable.efim_background_texture, "Control", "Used for direct control of remote or local robot.", false));
        faceList.add(new FaceInfo(ID_FACE_TELEPRESENCE, R.drawable.ic_telepresence, R.drawable.efim_background_texture, "Telepresence", "Allow remote control and show remote video feed on screen.", false));
        faceList.add(new FaceInfo(ID_FACE_TELEPRESENCE_EFIM, R.drawable.ic_robot_telepresence, R.drawable.efim_background_texture, "Remote Robot", "Telepresence while displaying robot face instead of video.", false));
        return faceList;
    }

    public static FaceInfo getFaceInfo(final int faceId) {
        final ArrayList<FaceInfo> faceList = getFaceInfoList();
        for(FaceInfo faceInfo : faceList){
            if(faceInfo.id == faceId){
                return faceInfo;
            }
        }
        return null;
    }
}
