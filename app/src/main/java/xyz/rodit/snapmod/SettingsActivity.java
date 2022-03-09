package xyz.rodit.snapmod;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.InputType;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.EditTextPreference;
import androidx.preference.PreferenceFragmentCompat;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import xyz.rodit.snapmod.utils.StreamUtils;

public class SettingsActivity extends AppCompatActivity {

    static final int STORAGE_PERM_REQUEST = 38372;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);
        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.settings, new SettingsFragment())
                    .commit();
        }
        setSupportActionBar(findViewById(R.id.toolbar));

        findViewById(R.id.btn_save).setOnClickListener(v -> savePrefs(this));

        if (isStoragePermissionGranted()) {
            initStorage();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        if (resultCode == RESULT_OK) {
            if (requestCode == STORAGE_PERM_REQUEST) {
                initStorage();
            }
        }
    }

    private void initStorage() {
        File mappingsPath = new File(Shared.SNAPMOD_MAPPINGS_DIR);
        if (!mappingsPath.exists()) {
            try {
                if (!mappingsPath.mkdirs()) {
                    throw new IOException("Failed to create directories.");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            long updateTime = getPackageManager().getPackageInfo(getPackageName(), 0).lastUpdateTime;
            for (String asset : getAssets().list("mappings")) {
                if (asset.endsWith(".json")) {
                    File outputFile = new File(Shared.SNAPMOD_MAPPINGS_DIR, asset);
                    if (outputFile.lastModified() <= updateTime) {
                        try (InputStream in = getAssets().open("mappings/" + asset);
                             OutputStream out = new FileOutputStream(new File(Shared.SNAPMOD_MAPPINGS_DIR, asset))) {
                            StreamUtils.copyTo(in, out);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        } catch (IOException | PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }

    private static void savePrefs(Context context) {
        try {
            File configFile = new File(Shared.SNAPMOD_CONFIG);
            if (configFile.exists()) {
                configFile.delete();
            }

            InputStream in = new FileInputStream(new File(context.getFilesDir().getParent(), "shared_prefs/xyz.rodit.snapmod_preferences.xml"));
            StreamUtils.copyTo(in, configFile);
            in.close();
            Toast.makeText(context, context.getString(R.string.prefs_saved), Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean isStoragePermissionGranted() {
        if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, STORAGE_PERM_REQUEST);
            return false;
        }
    }

    public static class SettingsFragment extends PreferenceFragmentCompat {

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey);

            findPreference("save_prefs").setOnPreferenceClickListener(p -> {
                savePrefs(getContext());
                return true;
            });

            ((EditTextPreference) findPreference("override_snap_timer")).setOnBindEditTextListener(t -> t.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL));
            ((EditTextPreference) findPreference("public_dp_resolution")).setOnBindEditTextListener(t -> t.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL));
            ((EditTextPreference) findPreference("location_share_lat")).setOnBindEditTextListener(t -> t.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL));
            ((EditTextPreference) findPreference("location_share_long")).setOnBindEditTextListener(t -> t.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL));
        }
    }
}
