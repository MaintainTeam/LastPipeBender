package org.schabi.newpipe.settings;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;


/**
 * Inherit from this class instead of {@link BasePreferenceFragment} to manipulate config options.
 * <p>
 * If you have a fork and flavors and want to alter some config options use this class especially
 * overwrite the {@link #manipulateCreatedPreferenceOptions()} in which you can manipulate
 */
public abstract class BraveBasePreferenceFragment extends BasePreferenceFragment {

    /**
     * After creation of this settings fragment you may want to manipulate
     * some settings.
     * <p>
     * Eg. if you've some flavor's and want them to have different options
     * here is a good place to manipulate them programmatically.
     */
    protected void manipulateCreatedPreferenceOptions() {
    }

    @Override
    public void onViewCreated(
            @NonNull final View rootView,
            @Nullable final Bundle savedInstanceState) {
        super.onViewCreated(rootView, savedInstanceState);
        manipulateCreatedPreferenceOptions();
    }
}
