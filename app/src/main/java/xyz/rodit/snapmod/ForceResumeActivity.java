package xyz.rodit.snapmod;

import android.app.Activity;
import android.os.Bundle;

import androidx.annotation.Nullable;

public class ForceResumeActivity extends Activity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        finish();
    }
}
