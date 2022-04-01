package xyz.rodit.snapmod;

import android.text.InputType;

import androidx.preference.EditTextPreference;

public class SettingsActivity extends xyz.rodit.xposed.SettingsActivity {

    public SettingsActivity() {
        super(R.xml.root_preferences);
    }

    @Override
    public void onCreatePreferences(xyz.rodit.xposed.SettingsActivity.SettingsFragment fragment) {
        ((EditTextPreference) fragment.findPreference("override_snap_timer")).setOnBindEditTextListener(t -> t.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL));
        ((EditTextPreference) fragment.findPreference("public_dp_resolution")).setOnBindEditTextListener(t -> t.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL));
        ((EditTextPreference) fragment.findPreference("location_share_lat")).setOnBindEditTextListener(t -> t.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL));
        ((EditTextPreference) fragment.findPreference("location_share_long")).setOnBindEditTextListener(t -> t.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL));
    }
}
