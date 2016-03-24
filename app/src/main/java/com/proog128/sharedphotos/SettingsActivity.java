package com.proog128.sharedphotos;

import android.app.Activity;
import android.os.Bundle;
import android.preference.PreferenceFragment;

public class SettingsActivity extends Activity {
    public static final String KEY_PREF_AUTO_ROTATE = "pref_auto_rotate";
    public static final String KEY_PREF_SUBTITLE = "pref_subtitle";
    public static final String KEY_PREF_THUMBNAIL_SIZE = "pref_thumbnail_size";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getFragmentManager().beginTransaction()
            .replace(android.R.id.content, new SettingsFragment())
            .commit();
    }

    public static class SettingsFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            addPreferencesFromResource(R.xml.preferences);
        }
    }
}
