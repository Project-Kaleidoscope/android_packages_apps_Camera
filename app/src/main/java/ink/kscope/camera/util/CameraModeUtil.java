package ink.kscope.camera.util;

import android.content.Context;
import android.content.Intent;

import app.grapheneos.camera.CameraMode;
import app.grapheneos.camera.ui.activities.MainActivity;

public class CameraModeUtil {
    public static void launchCameraWithMode(Context context, CameraMode mode) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.putExtra("mode", mode);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }
}