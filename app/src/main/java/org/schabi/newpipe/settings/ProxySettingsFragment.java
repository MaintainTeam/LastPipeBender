package org.schabi.newpipe.settings;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.widget.Toast;
import androidx.preference.Preference;
import androidx.preference.ListPreference;
import androidx.preference.SwitchPreferenceCompat;
import androidx.preference.PreferenceManager;

import org.schabi.newpipe.App;
import org.schabi.newpipe.R;

public class ProxySettingsFragment extends BasePreferenceFragment {

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
            // Сообщаем пользователю, что требуется перезапуск
            Toast.makeText(getContext(),
                getString(R.string.proxy_restart_required), Toast.LENGTH_LONG).show();
            return true;
        });

        // Настройка адреса прокси
        final Preference proxyAddressPref = findPreference(
            App.getApp().getString(R.string.proxy_address_key)
        );
        assert proxyAddressPref != null;

        proxyAddressPref.setOnPreferenceChangeListener((preference, newValue) -> {
            // Валидация IP-адреса
            if (!isValidIpAddress(newValue.toString())) {
                Toast.makeText(getContext(),
                    getString(R.string.invalid_ip_address), Toast.LENGTH_SHORT).show();
                return false; // Не сохраняем изменение
            }
            Log.d("ProxySettings", "Read proxy_address_key: " + newValue);
            // Сохраняем новое значение IP-адреса
            sharedPreferences.edit().putString(
                App.getApp().getString(R.string.proxy_address_key),
                newValue.toString()).apply();
            // Обновляем summary с новым адресом
            proxyAddressPref.setSummary(
                getString(R.string.proxy_address_summary, newValue)
            );
            // Сообщаем пользователю, что требуется перезапуск
            Toast.makeText(getContext(),
                getString(R.string.proxy_restart_required), Toast.LENGTH_LONG).show();
            return true;
        });
        // Устанавливаем текущее значение в summary при загрузке настроек
        final String currentAddress = sharedPreferences.getString(
            App.getApp().getString(R.string.proxy_address_key), "192.168.1.1"
        );
        proxyAddressPref.setSummary(
            getString(R.string.proxy_address_summary, currentAddress)
        );

        // Настройка порта прокси
        final Preference proxyPortPref = findPreference(
            App.getApp().getString(R.string.proxy_port_key)
        );
        assert proxyPortPref != null;

        proxyPortPref.setOnPreferenceChangeListener((preference, newValue) -> {
            // Валидация порта
            if (!isValidPort(newValue.toString())) {
                Toast.makeText(getContext(),
                    getString(R.string.invalid_port), Toast.LENGTH_SHORT).show();
                return false; // Не сохраняем изменение
            }
            Log.d("ProxySettings", "Read proxy_port_key: " + newValue);
            // Сохраняем новое значение порта
            sharedPreferences.edit().putString(
                App.getApp().getString(R.string.proxy_port_key),
                newValue.toString()).apply();
            // Обновляем summary с новым портом
            proxyPortPref.setSummary(
                getString(R.string.proxy_port_summary, newValue)
            );
            // Сообщаем пользователю, что требуется перезапуск
            Toast.makeText(getContext(),
                getString(R.string.proxy_restart_required), Toast.LENGTH_LONG).show();
            return true;
        });
        // Устанавливаем текущее значение в summary при загрузке настроек
        final String currentPort = sharedPreferences.getString(
            App.getApp().getString(R.string.proxy_port_key), "1080"
        );
        proxyPortPref.setSummary(
            getString(R.string.proxy_port_summary, currentPort)
        );
        // Настройка типа прокси
        final ListPreference proxyTypePref = findPreference(
            App.getApp().getString(R.string.proxy_type_key)
        );
        assert proxyTypePref != null;

        proxyTypePref.setOnPreferenceChangeListener((preference, newValue) -> {
            final String proxyType = newValue.toString();
            Log.d("ProxySettings", "Read proxy_type_key: " + proxyType);
            // Сохраняем новое значение типа прокси
            sharedPreferences.edit().putString(
                App.getApp().getString(R.string.proxy_type_key),
                proxyType).apply();
            // Обновляем summary
            proxyTypePref.setSummary(
                getString(R.string.proxy_type_summary) + ": " + proxyType.toUpperCase()
            );
            // Сообщаем пользователю, что требуется перезапуск
            Toast.makeText(getContext(),
                getString(R.string.proxy_restart_required), Toast.LENGTH_LONG).show();
            return true;
        });
        // Устанавливаем текущее значение в summary при загрузке настроек
        final String currentProxyType = sharedPreferences.getString(
            App.getApp().getString(R.string.proxy_type_key), "socks"
        );
        proxyTypePref.setSummary(
            getString(R.string.proxy_type_summary) + ": " + currentProxyType.toUpperCase()
        );
    }
    // Метод для валидации IP-адреса
    public boolean isValidIpAddress(final String ipAddress) {
        return Patterns.IP_ADDRESS.matcher(ipAddress).matches();
    }
    // Метод для валидации порта
    public boolean isValidPort(final String port) {
        try {
            final int portNumber = Integer.parseInt(port);
            return portNumber >= 1 && portNumber <= 65535;
        } catch (final NumberFormatException e) {
            return false;
        }
    }
}

