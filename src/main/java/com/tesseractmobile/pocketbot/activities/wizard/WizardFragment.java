package com.tesseractmobile.pocketbot.activities.wizard;

import android.content.Context;
import android.app.Fragment;

import com.tesseractmobile.pocketbot.R;
import com.tesseractmobile.pocketbot.activities.LauncherActivity;

/**
 * Created by josh on 4/17/17.
 */

public class WizardFragment extends Fragment {
    private ConfigWizard configWizard;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        configWizard = (ConfigWizard) context;
    }

    protected ConfigWizard getConfigWizard(){
        return configWizard;
    }

    /**
     * Begins a fragment transaction
     * @param fragment
     */
    protected void nextFragment(final Fragment fragment){
        getFragmentManager().beginTransaction().replace(R.id.fragment_placeholder, fragment).commit();
        ((ConfigWizard) getActivity()).nextStep();
    }

    protected void done(){
        getConfigWizard().applyConfig(getActivity());
        //Restart the app by launching the Launcher Activity
        getActivity().startActivity(LauncherActivity.getLaunchIntent(getActivity()));
        getActivity().finish();
    }
}
