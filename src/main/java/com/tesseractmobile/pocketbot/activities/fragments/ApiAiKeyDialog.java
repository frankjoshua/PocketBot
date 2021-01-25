package com.tesseractmobile.pocketbot.activities.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import com.tesseractmobile.pocketbot.R;
import com.tesseractmobile.pocketbot.activities.PocketBotSettings;

/**
 * Created by josh on 11/21/2015.
 */
public class ApiAiKeyDialog extends DialogFragment {
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final Context ctx = getActivity();
        LayoutInflater inflater = getActivity().getLayoutInflater();

        View rootView = inflater.inflate(R.layout.api_ai_key_widget, null, false);

        final EditText edToken = (EditText) rootView.findViewById(R.id.edToken);
        final EditText edKey = (EditText) rootView.findViewById(R.id.edKey);

        //Set intial fields
        edToken.setText(PocketBotSettings.getApiAiToken(ctx));
        edKey.setText(PocketBotSettings.getApiAiKey(ctx));

        rootView.findViewById(R.id.btnSave).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PocketBotSettings.setApiAiKey(ctx, edKey.getText().toString());
                PocketBotSettings.setApiAiToken(ctx, edKey.getText().toString());
                dismiss();
            }
        });

        rootView.findViewById(R.id.btnDefault).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PocketBotSettings.setApiAiKey(ctx, PocketBotSettings.DEFAULT_API_AI_KEY);
                PocketBotSettings.setApiAiToken(ctx, PocketBotSettings.DEFAULT_API_AI_TOKEN);
                dismiss();
            }
        });

        return new AlertDialog.Builder(ctx)
                .setTitle("api.ai settings")
                .setView(rootView)
                .create();
    }
}
