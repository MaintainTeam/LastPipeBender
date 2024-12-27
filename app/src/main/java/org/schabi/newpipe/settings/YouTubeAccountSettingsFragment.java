package org.schabi.newpipe.settings;

import android.content.Intent;
import android.os.Bundle;
import androidx.preference.Preference;
import org.schabi.newpipe.R;
import org.schabi.newpipe.views.YouTubeLoginWebViewActivity;

import static android.app.Activity.RESULT_OK;

public class YouTubeAccountSettingsFragment extends BasePreferenceFragment {
    private static final int REQUEST_LOGIN = 1;

    @Override
    public void onCreatePreferences(final Bundle savedInstanceState, final String rootKey) {
        addPreferencesFromResource(R.xml.account_settings_youtube);
        Preference login = findPreference(getString(R.string.login_key));
        Preference logout = findPreference(getString(R.string.logout_key));
        login.setOnPreferenceClickListener(preference -> {
            // Open a webview to login and then get cookies
            // and save them to the shared preferences
            Intent intent = new Intent(this.getContext(), YouTubeLoginWebViewActivity.class);
            startActivityForResult(intent, REQUEST_LOGIN);
            return true;
        });
        logout.setOnPreferenceClickListener(preference -> {
            // Clear cookies
            defaultPreferences.edit().putString(getString(R.string.youtube_cookies_key), "").apply();
//            defaultPreferences.edit().putString(getString(R.string.youtube_po_token), "").apply();
            return true;
        });
        if (defaultPreferences.getString(getString(R.string.youtube_cookies_key), "").equals("")) {
            logout.setEnabled(false);
        } else {
            login.setEnabled(false);
        }

//        Preference override_cookies_niconico_value = findPreference(getString(R.string.override_cookies_youtube_value_key));
//        override_cookies_niconico_value.setEnabled(defaultPreferences.getBoolean(getString(R.string.override_cookies_youtube_key), false));
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_LOGIN && resultCode == RESULT_OK) {
            String cookies = data.getStringExtra("cookies");
            String pot = data.getStringExtra("pot");
            // save cookies to shared preferences
            defaultPreferences.edit().putString(getString(R.string.youtube_cookies_key), cookies).apply();
//            defaultPreferences.edit().putString(getString(R.string.youtube_po_token), pot).apply();
        }
    }

}
