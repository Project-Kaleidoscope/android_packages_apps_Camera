// Generated by view binder compiler. Do not edit!
package app.grapheneos.camera.databinding;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewbinding.ViewBinding;
import androidx.viewbinding.ViewBindings;
import app.grapheneos.camera.R;
import app.grapheneos.camera.ui.ZoomableImageView;
import java.lang.NullPointerException;
import java.lang.Override;
import java.lang.String;

public final class GallerySlideBinding implements ViewBinding {
  @NonNull
  private final FrameLayout rootView;

  @NonNull
  public final GalleryPlaceholderBinding placeholderText;

  @NonNull
  public final ImageView playButton;

  @NonNull
  public final FrameLayout root;

  @NonNull
  public final ZoomableImageView slidePreview;

  private GallerySlideBinding(@NonNull FrameLayout rootView,
      @NonNull GalleryPlaceholderBinding placeholderText, @NonNull ImageView playButton,
      @NonNull FrameLayout root, @NonNull ZoomableImageView slidePreview) {
    this.rootView = rootView;
    this.placeholderText = placeholderText;
    this.playButton = playButton;
    this.root = root;
    this.slidePreview = slidePreview;
  }

  @Override
  @NonNull
  public FrameLayout getRoot() {
    return rootView;
  }

  @NonNull
  public static GallerySlideBinding inflate(@NonNull LayoutInflater inflater) {
    return inflate(inflater, null, false);
  }

  @NonNull
  public static GallerySlideBinding inflate(@NonNull LayoutInflater inflater,
      @Nullable ViewGroup parent, boolean attachToParent) {
    View root = inflater.inflate(R.layout.gallery_slide, parent, false);
    if (attachToParent) {
      parent.addView(root);
    }
    return bind(root);
  }

  @NonNull
  public static GallerySlideBinding bind(@NonNull View rootView) {
    // The body of this method is generated in a way you would not otherwise write.
    // This is done to optimize the compiled bytecode for size and performance.
    int id;
    missingId: {
      id = R.id.placeholder_text;
      View placeholderText = ViewBindings.findChildViewById(rootView, id);
      if (placeholderText == null) {
        break missingId;
      }
      GalleryPlaceholderBinding binding_placeholderText = GalleryPlaceholderBinding.bind(placeholderText);

      id = R.id.play_button;
      ImageView playButton = ViewBindings.findChildViewById(rootView, id);
      if (playButton == null) {
        break missingId;
      }

      FrameLayout root = (FrameLayout) rootView;

      id = R.id.slide_preview;
      ZoomableImageView slidePreview = ViewBindings.findChildViewById(rootView, id);
      if (slidePreview == null) {
        break missingId;
      }

      return new GallerySlideBinding((FrameLayout) rootView, binding_placeholderText, playButton,
          root, slidePreview);
    }
    String missingId = rootView.getResources().getResourceName(id);
    throw new NullPointerException("Missing required view with ID: ".concat(missingId));
  }
}