package app.grapheneos.camera.ui.fragment

import androidx.recyclerview.widget.RecyclerView
import ink.kscope.camera.databinding.GallerySlideBinding

class GallerySlide(val binding: GallerySlideBinding) : RecyclerView.ViewHolder(binding.root) {
    @Volatile var currentPostion = 0
}
