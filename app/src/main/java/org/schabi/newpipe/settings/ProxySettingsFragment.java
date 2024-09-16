package org.schabi.newpipe.settings;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SwitchPreferenceCompat;
import androidx.preference.PreferenceManager;

import org.schabi.newpipe.App;
import org.schabi.newpipe.R;

public class ProxySettingsFragment extends PreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(final Bundle savedInstanceState, final String rootKey) {
        // Подключаем preferences файл для Proxy
        addPreferencesFromResource(R.xml.proxy_settings);

        // Получаем SharedPreferences
        final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(
            App.getApp()
        );

        // Настройка включения прокси
        final SwitchPreferenceCompat proxyEnablePref = findPreference(
            App.getApp().getString(R.string.proxy_enabled_key)
        );
        assert proxyEnablePref != null;

        proxyEnablePref.setOnPreferenceChangeListener((preference, newValue) -> {
            final boolean isEnabled = (Boolean) newValue;
            Log.d("ProxySettings", "Read proxy_enabled_key: " + isEnabled);

            // Сохраняем новое значение proxy_enabled_key
            sharedPreferences.edit().putBoolean(
                App.getApp().getString(R.string.proxy_enabled_key),
                isEnabled).apply();

            Log.d("ProxySettings",
                "Saved proxy_enabled_key: " + sharedPreferences.getBoolean(
                  App.getApp().getString(R.string.proxy_enabled_key), false));

            return true;
        });

        // Настройка адреса прокси
        final Preference proxyAddressPref = findPreference(
            App.getApp().getString(R.string.proxy_address_key)
        );
        assert proxyAddressPref != null;

        proxyAddressPref.setOnPreferenceChangeListener((preference, newValue) -> {
            Log.d("ProxySettings", "Read proxy_address_key: " + newValue);
            // Сохраняем новое значение IP-адреса
            sharedPreferences.edit().putString(
                App.getApp().getString(R.string.proxy_address_key),
                newValue.toString()).apply();
            return true;
        });

        // Настройка порта прокси
        final Preference proxyPortPref = findPreference(
            App.getApp().getString(R.string.proxy_port_key)
        );
        assert proxyPortPref != null;

        proxyPortPref.setOnPreferenceChangeListener((preference, newValue) -> {
            Log.d("ProxySettings", "Read proxy_port_key: " + newValue);
            // Сохраняем новое значение порта
            sharedPreferences.edit().putString(
                App.getApp().getString(R.string.proxy_port_key),
                newValue.toString()).apply();
            return true;
        });
    }
}

