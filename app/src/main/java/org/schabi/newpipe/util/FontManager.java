package org.schabi.newpipe.util;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.preference.PreferenceManager;
import com.marcinorlowski.fonty.Fonty;
import org.schabi.newpipe.R;


public final class FontManager {
    private FontManager() { }

    public static void init(final Context context) {
        // Apply the preferred font globally
        final String preferredFont = getPreferredFont(context);

        setUpFont(preferredFont, context);
    }

    public static String getPreferredFont(final Context context) {
        final SharedPreferences preferences = PreferenceManager
                .getDefaultSharedPreferences(context);
        return preferences.getString("preferred_font", (getString(R.string.default_font_key)));
    }

    // build the relevant font TypeFace
    public static void setUpFont(final String preferredFont, final Context context) {
        switch (preferredFont) {
            case "Arial":
                Fonty.context(context)
                        .normalTypeface("arial.ttf")
                        .build();
                break;
            case "Broadway":
                Fonty.context(context)
                        .normalTypeface("BROADW.TTF")
                        .build();
                break;
            case "Algerian":
                Fonty.context(context)
                        .normalTypeface("Algerian.TTF")
                        .build();
                break;
            case "Bell MT":
                Fonty.context(context)
                        .normalTypeface("BELL.TTF")
                        .build();
                break;
            case "Calibri":
                Fonty.context(context)
                        .normalTypeface("calibrii.ttf")
                        .build();
                break;
            case "Time New Roman":
                Fonty.context(context)
                        .normalTypeface("times.ttf")
                        .build();
                break;
            default:
                // do nothing
                break;
        }

    }
}
