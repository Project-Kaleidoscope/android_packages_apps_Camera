// Generated by view binder compiler. Do not edit!
package ink.kscope.camera.databinding;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewbinding.ViewBinding;
import androidx.viewbinding.ViewBindings;
import ink.kscope.camera.R;
import com.google.android.material.tabs.TabLayout;
import java.lang.NullPointerException;
import java.lang.Override;
import java.lang.String;

public final class ScanResultDialogBinding implements ViewBinding {
  @NonNull
  private final LinearLayout rootView;

  @NonNull
  public final ImageButton copyQrText;

  @NonNull
  public final TabLayout encodingTabs;

  @NonNull
  public final TextView scanResultText;

  @NonNull
  public final ImageButton shareQrText;

  private ScanResultDialogBinding(@NonNull LinearLayout rootView, @NonNull ImageButton copyQrText,
      @NonNull TabLayout encodingTabs, @NonNull TextView scanResultText,
      @NonNull ImageButton shareQrText) {
    this.rootView = rootView;
    this.copyQrText = copyQrText;
    this.encodingTabs = encodingTabs;
    this.scanResultText = scanResultText;
    this.shareQrText = shareQrText;
  }

  @Override
  @NonNull
  public LinearLayout getRoot() {
    return rootView;
  }

  @NonNull
  public static ScanResultDialogBinding inflate(@NonNull LayoutInflater inflater) {
    return inflate(inflater, null, false);
  }

  @NonNull
  public static ScanResultDialogBinding inflate(@NonNull LayoutInflater inflater,
      @Nullable ViewGroup parent, boolean attachToParent) {
    View root = inflater.inflate(R.layout.scan_result_dialog, parent, false);
    if (attachToParent) {
      parent.addView(root);
    }
    return bind(root);
  }

  @NonNull
  public static ScanResultDialogBinding bind(@NonNull View rootView) {
    // The body of this method is generated in a way you would not otherwise write.
    // This is done to optimize the compiled bytecode for size and performance.
    int id;
    missingId: {
      id = R.id.copy_qr_text;
      ImageButton copyQrText = ViewBindings.findChildViewById(rootView, id);
      if (copyQrText == null) {
        break missingId;
      }

      id = R.id.encoding_tabs;
      TabLayout encodingTabs = ViewBindings.findChildViewById(rootView, id);
      if (encodingTabs == null) {
        break missingId;
      }

      id = R.id.scan_result_text;
      TextView scanResultText = ViewBindings.findChildViewById(rootView, id);
      if (scanResultText == null) {
        break missingId;
      }

      id = R.id.share_qr_text;
      ImageButton shareQrText = ViewBindings.findChildViewById(rootView, id);
      if (shareQrText == null) {
        break missingId;
      }

      return new ScanResultDialogBinding((LinearLayout) rootView, copyQrText, encodingTabs,
          scanResultText, shareQrText);
    }
    String missingId = rootView.getResources().getResourceName(id);
    throw new NullPointerException("Missing required view with ID: ".concat(missingId));
  }
}
