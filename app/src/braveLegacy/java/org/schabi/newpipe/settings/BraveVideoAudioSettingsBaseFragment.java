package org.schabi.newpipe.settings;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;

import org.schabi.newpipe.R;

import androidx.preference.ListPreference;
import androidx.preference.PreferenceManager;


public abstract class BraveVideoAudioSettingsBaseFragment extends BraveBasePreferenceFragment {

    public static void makeConfigOptionsSuitableForFlavor(final Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            return;
        }

        final SharedPreferences prefs =
                PreferenceManager.getDefaultSharedPreferences(context);

        // make sure seekbar thumbnail stuff is *not* set to high quality as it
        // consumes to much memory which gives OutOfMemoryError Exception on Kitkat
        // -> so default to low quality
        // -> TODO long run fix the bug of the seekbar_preview_thumbnail implementation
        final String seekBarOption = prefs.getString(context.getString(
                R.string.seekbar_preview_thumbnail_key), null
        );
        if ((null == seekBarOption) || (seekBarOption.equals(
                context.getString(R.string.seekbar_preview_thumbnail_high_quality)))) {
            prefs.edit().putString(
                    context.getString(R.string.seekbar_preview_thumbnail_key),
                    context.getString(R.string.seekbar_preview_thumbnail_low_quality)).apply();
        }
    }

    @Override
    protected void manipulateCreatedPreferenceOptions() {
        super.manipulateCreatedPreferenceOptions();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            return;
        }

        setListPreferenceData();
    }

    private void setListPreferenceData() {
        final ListPreference lp = (ListPreference) findPreference(
                getString(R.string.seekbar_preview_thumbnail_key));

        final CharSequence[] entries = {
                getString(R.string.low_quality_smaller),
                getString(R.string.dont_show)
        };

        final CharSequence[] entryValues = {
                getString(R.string.seekbar_preview_thumbnail_low_quality),
                getString(R.string.seekbar_preview_thumbnail_none)
        };

        lp.setEntries(entries);
        lp.setEntryValues(entryValues);
        // default value has to be set in BraveApp via the static
        // method makeConfigOptionsSuitableForFlavor()
        //lp.setDefaultValue(getString(R.string.seekbar_preview_thumbnail_low_quality));
        //lp.setValueIndex(0);
    }
}
