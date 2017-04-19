package com.tesseractmobile.pocketbot.activities.wizard;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.tesseractmobile.pocketbot.R;

/**
 * Created by josh on 4/17/17.
 */

public class WizardStepRos extends WizardFragment implements View.OnClickListener, TextWatcher {

    private EditText editTextMasterUri;
    private TextView tvMasterUri;
    private Button btnOk;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.wizard_step_ros, container, false);
        view.findViewById(R.id.btnOk).setOnClickListener(this);
        tvMasterUri = (TextView) view.findViewById(R.id.tvMasterUri);
        btnOk = (Button) view.findViewById(R.id.btnOk);
        editTextMasterUri = (EditText) view.findViewById(R.id.edMasterUri);
        editTextMasterUri.setText("10.10.10.1");
        editTextMasterUri.addTextChangedListener(this);
        return view;
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.btnOk){
            getConfigWizard().setRosMasterUri(tvMasterUri.getText().toString());
            done();
        }
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        final String url = "http://" + s + ":11311";
        tvMasterUri.setText(url);
        if(Patterns.WEB_URL.matcher(url).matches()){
            btnOk.setEnabled(true);
        } else {
            btnOk.setEnabled(false);
        }
    }

    @Override
    public void afterTextChanged(Editable s) {

    }
}
