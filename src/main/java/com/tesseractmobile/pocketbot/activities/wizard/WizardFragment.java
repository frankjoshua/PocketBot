package com.tesseractmobile.pocketbot.activities.wizard;

import android.content.Context;
import android.support.v4.app.Fragment;

import com.tesseractmobile.pocketbot.R;

/**
 * Created by josh on 4/17/17.
 */

abstract class WizardFragment extends Fragment {
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
        ((WizardActivity) getActivity()).done();
    }
}
