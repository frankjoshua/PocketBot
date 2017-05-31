package com.tesseractmobile.pocketbot.robot;

import com.tesseractmobile.pocketbot.robot.model.TextInput;

import io.reactivex.Observer;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.subjects.BehaviorSubject;

/**
 * Created by josh on 11/16/2015.
 */
abstract public class AIRobot extends BaseRobot {

    private BehaviorSubject<TextInput> mTextInputSubject = BehaviorSubject.create();
    private Disposable mTextInputDisposable;

    public AIRobot(final DataStore dataStore) {
        super(dataStore);
    }

    /**
     * Set the AI service to use
     * @param ai
     */
    public void setAI(final AI ai){
        //Unsubscribe from old ai
        if(mTextInputDisposable != null && !mTextInputDisposable.isDisposed()){
            mTextInputDisposable.dispose();
        }
        //Pass text to AI
        mTextInputSubject.subscribe(new Observer<TextInput>() {
           @Override
           public void onSubscribe(@NonNull Disposable d) {
                mTextInputDisposable = d;
           }

           @Override
           public void onNext(@NonNull TextInput textInput) {
                ai.input(textInput.text, null);
           }

           @Override
           public void onError(@NonNull Throwable e) {

           }

           @Override
           public void onComplete() {

           }
       });
    }


    @Override
    public void onTextInput(final String text) {
        //Report to the SpeechListener
        mTextInputSubject.onNext(new TextInput(text));
    }

    @Override
    public BehaviorSubject<TextInput> getTextInputSubject() {
        return mTextInputSubject;
    }

}
