package ink.kscope.camera.ui.activities;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import app.grapheneos.camera.CameraMode;
import ink.kscope.camera.util.CameraModeUtil;

public class HDRActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        CameraModeUtil.launchCameraWithMode(getApplicationContext(), CameraMode.HDR);
        finish();
    }
}
