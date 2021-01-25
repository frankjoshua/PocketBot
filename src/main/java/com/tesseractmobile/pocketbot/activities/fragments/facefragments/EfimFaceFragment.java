package com.tesseractmobile.pocketbot.activities.fragments.facefragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.tesseractmobile.pocketbot.R;
import com.tesseractmobile.pocketbot.robot.RemoteControl;
import com.tesseractmobile.pocketbot.robot.Robot;
import com.tesseractmobile.pocketbot.robot.SensorData;
import com.tesseractmobile.pocketbot.robot.faces.EfimFace;
import com.tesseractmobile.pocketbot.robot.faces.RobotFace;
import com.tesseractmobile.pocketbot.robot.faces.RobotInterface;

import io.reactivex.Observer;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;

/**
 * Created by josh on 10/18/2015.
 */
public class EfimFaceFragment extends FaceFragment{

    private Disposable mControlDisposable;

    @Override
    public void onDestroy() {
        final Disposable controlDisposable = mControlDisposable;
        if(controlDisposable != null && !controlDisposable.isDisposed()){
            controlDisposable.dispose();
        }
        super.onDestroy();
    }

    @Override
    public boolean isUseFaceTracking() {
        return true;
    }

    @Override
    protected View createView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.robot_face, null);
        final EfimFace face = new EfimFace(view, Robot.get());
        setFace(face);
        RemoteControl.get().getControlSubject().subscribe(new Observer<SensorData.Control>() {
            @Override
            public void onSubscribe(@NonNull Disposable d) {
                mControlDisposable = d;
            }

            @Override
            public void onNext(@NonNull SensorData.Control control) {
                ((EfimFace) face).onControlReceived(control);
            }

            @Override
            public void onError(@NonNull Throwable e) {
                ((EfimFace) face).onControlReceived(new SensorData.Control());
            }

            @Override
            public void onComplete() {

            }
        });
        return view;
    }


}