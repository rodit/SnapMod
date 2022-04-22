package xyz.rodit.snapmod;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.text.InputType;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.view.inputmethod.EditorInfo;

import androidx.appcompat.app.AlertDialog;
import androidx.preference.EditTextPreference;
import androidx.preference.Preference;

import java.io.File;
import java.util.List;
import java.util.Optional;

import xyz.rodit.xposed.utils.PathUtils;

public class SettingsActivity extends xyz.rodit.xposed.SettingsActivity {

    public SettingsActivity() {
        super(R.xml.root_preferences);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        updates.checkForUpdates("rodit", "SnapMod");
    }

    @Override
    public void onCreatePreferences(xyz.rodit.xposed.SettingsActivity.SettingsFragment fragment) {
        Preference statusPreference = fragment.findPreference("installation_status");
        statusPreference.setSummary(getInstallationSummary(false));
        statusPreference.setOnPreferenceClickListener(p -> {
            showInfoDialog();
            return true;
        });

        ((EditTextPreference) fragment.findPreference("override_snap_timer")).setOnBindEditTextListener(t -> t.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL));
        ((EditTextPreference) fragment.findPreference("public_dp_resolution")).setOnBindEditTextListener(t -> t.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL));
        ((EditTextPreference) fragment.findPreference("location_share_lat")).setOnBindEditTextListener(t -> t.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL));
        ((EditTextPreference) fragment.findPreference("location_share_long")).setOnBindEditTextListener(t -> t.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL));
        ((EditTextPreference) fragment.findPreference("hidden_friends")).setOnBindEditTextListener(t -> {
            t.setImeOptions(EditorInfo.IME_ACTION_NONE);
            t.setInputType(InputType.TYPE_TEXT_FLAG_MULTI_LINE);
            t.setSingleLine(false);
            t.setSelection(t.getText().length());
        });
    }

    private void showInfoDialog() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.installation_status_title)
                .setMessage(getInstallationSummary(true))
                .show();
    }

    private Spannable getInstallationSummary(boolean detailed) {
        SpannableStringBuilder builder = new SpannableStringBuilder();
        try {
            PackageInfo info = getPackageManager().getPackageInfo(Shared.SNAPCHAT_PACKAGE, 0);
            List<File> possible = PathUtils.getPossibleMappingFiles(this, info.versionCode + ".json");
            Optional<File> mappings = possible.stream().filter(File::exists).findFirst();
            boolean supported = mappings.isPresent();
            builder.append("Snapchat Version: ")
                    .append(info.versionName + " (" + info.versionCode + ")", new ForegroundColorSpan(supported ? Color.GREEN : Color.RED), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                    .append('\n');
            if (supported) {
                builder.append("Supported", new ForegroundColorSpan(Color.GREEN), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                if (detailed) {
                    builder.append("\n\nMappings found at ").append(String.valueOf(mappings.get()));
                }
            } else {
                builder.append("Unsupported", new ForegroundColorSpan(Color.RED), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                if (detailed) {
                    builder.append("\n\nNo mappings found at ");
                    possible.forEach(f -> builder.append(f.getPath()).append('\n'));
                }
            }
        } catch (PackageManager.NameNotFoundException e) {
            builder.append("Error getting Snapchat package info.", new ForegroundColorSpan(Color.RED), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        if (!detailed) {
            builder.append("\nTap for more info.");
        }

        return builder;
    }
}
