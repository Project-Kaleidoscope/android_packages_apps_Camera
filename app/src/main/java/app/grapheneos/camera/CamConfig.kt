package app.grapheneos.camera

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.util.Log
import android.util.Size
import android.view.View
import android.widget.Toast
import androidx.camera.core.*
import androidx.camera.extensions.ExtensionMode
import androidx.camera.extensions.ExtensionsManager
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import app.grapheneos.camera.ui.activities.MainActivity
import java.io.File
import java.util.concurrent.ExecutionException
import java.util.concurrent.TimeUnit
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.Animation

import android.view.animation.AlphaAnimation
import app.grapheneos.camera.analyzer.QRAnalyzer
import java.util.concurrent.Executors
import kotlin.math.roundToInt


class CamConfig(private val mActivity: MainActivity) {

    var camera: Camera? = null

    private var cameraProvider: ProcessCameraProvider? = null

    var imageCapture: ImageCapture? = null
        private set

    var preview: Preview? = null
        private set

    private var lensFacing = CameraSelector.LENS_FACING_BACK

    private var cameraMode = ExtensionMode.NONE

    private lateinit var cameraSelector: CameraSelector

    private val extensionsManager by lazy {
        ExtensionsManager.getInstance(mActivity).get()
    }

    private val cameraExecutor by lazy {
        Executors.newSingleThreadExecutor()
    }

    var isVideoMode = false
        private set

    var isQRMode = false
        private set

    var videoCapture: VideoCapture? = null

    var qrAnalyzer: QRAnalyzer? = null

    var iAnalyzer: ImageAnalysis? = null

    private var aspectRatio = AspectRatio.RATIO_16_9

    private var latestFile: File? = null

    private var flashMode: Int
        get() = if (imageCapture != null) imageCapture!!.flashMode else ImageCapture.FLASH_MODE_OFF
        set(flashMode) {
            imageCapture!!.flashMode = flashMode
        }
    val parentDirPath: String
        get() = parentDir!!.absolutePath

    private val parentDir: File?
        get() {
            val dirs = mActivity.externalMediaDirs
            var parentDir: File? = null
            for (dir in dirs) {
                if (dir != null) {
                    parentDir = dir
                    break
                }
            }
            if (parentDir != null) {
                parentDir = File(
                    parentDir.absolutePath,
                    mActivity.resources.getString(R.string.app_name)
                )
                if (parentDir.mkdirs()) {
                    Log.i(TAG, "Parent directory was successfully created")
                }
            }
            return parentDir
        }

    val latestPreview: Bitmap?
        get() {
            val lastModifiedFile = latestMediaFile ?: return null
            return if (getExtension(lastModifiedFile) == "mp4") {
                try {
                    getVideoThumbnail(lastModifiedFile.absolutePath)
                } catch (throwable: Throwable) {
                    throwable.printStackTrace()
                    null
                }
            } else BitmapFactory.decodeFile(lastModifiedFile.absolutePath)
        }

    fun setLatestFile(latestFile: File?) {
        this.latestFile = latestFile
    }

    val latestMediaFile: File?
        get() {
            if (latestFile != null) return latestFile
            val dir = parentDir
            val files = dir!!.listFiles { file: File ->
                if (!file.isFile) return@listFiles false
                val ext = getExtension(file)
                ext == "jpg" || ext == "png" || ext == "mp4"
            }
            if (files == null || files.isEmpty()) return null
            var lastModifiedFile = files[0]
            for (file in files) {
                if (lastModifiedFile.lastModified() < file.lastModified()) lastModifiedFile = file
            }
            return lastModifiedFile
        }

    fun switchCameraMode() {
        isVideoMode = !isVideoMode
        startCamera(true)
    }

    // Tells whether flash is available for the current mode
    private val isFlashAvailable: Boolean
        get() = camera!!.cameraInfo.hasFlashUnit()

    fun toggleFlashMode() {
        if (camera!!.cameraInfo.hasFlashUnit()) {
            flashMode =
                if (flashMode == ImageCapture.FLASH_MODE_OFF) ImageCapture.FLASH_MODE_AUTO else imageCapture!!.flashMode + 1
            mActivity.flashPager.currentItem = flashMode
        } else {
            Toast.makeText(
                mActivity, "Flash is unavailable" +
                        " for the current mode.",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    fun toggleCameraSelector() {
        lensFacing =
            if (lensFacing == CameraSelector.LENS_FACING_BACK) CameraSelector.LENS_FACING_FRONT else CameraSelector.LENS_FACING_BACK
        startCamera(true)
    }

    fun initializeCamera() {
        if (cameraProvider != null) {
            startCamera()
            return
        }
        val cameraProviderFuture = ProcessCameraProvider.getInstance(
            mActivity
        )
        cameraProviderFuture.addListener({
            try {
                cameraProvider = cameraProviderFuture.get()
                startCamera()
            } catch (e: ExecutionException) {
                // No errors need to be handled for this Future.
                // This should never be reached.
            } catch (e: InterruptedException) {
            }
        }, ContextCompat.getMainExecutor(mActivity))
    }

    // Start the camera with latest hard configuration
    @SuppressLint("RestrictedApi")
    @JvmOverloads
    fun startCamera(forced: Boolean = false) {
        if (!forced && camera != null) return

        if (mActivity.isDestroyed || mActivity.isFinishing) return

        preview = Preview.Builder()
            .setTargetRotation(mActivity.windowManager.defaultDisplay.rotation)
            .setTargetAspectRatio(aspectRatio)
            .build()

        cameraSelector = CameraSelector.Builder()
            .requireLensFacing(lensFacing)
            .build()

        val builder = ImageCapture.Builder()

        imageCapture = builder
            .setTargetRotation(mActivity.windowManager.defaultDisplay.rotation)
            .setTargetAspectRatio(aspectRatio)
            .setFlashMode(flashMode)
            .build()

        preview!!.setSurfaceProvider(mActivity.previewView.surfaceProvider)

        // To use the last frame instead of showing a blank screen when
        // the camera that is being currently used gets unbing
        mActivity.updateLastFrame()

        // Unbind/close all other camera(s) [if any]
        cameraProvider!!.unbindAll()

        if(extensionsManager.isExtensionAvailable(cameraProvider!!, cameraSelector,
                cameraMode)){
            cameraSelector = extensionsManager.getExtensionEnabledCameraSelector(
                cameraProvider!!, cameraSelector, cameraMode
            )
        } else {
            Log.i(TAG, "Auto mode isn't available for this device")
        }

        val useCaseGroupBuilder = UseCaseGroup.Builder()

        useCaseGroupBuilder.addUseCase(preview!!)

        if(isQRMode){
            qrAnalyzer = QRAnalyzer(mActivity)
            iAnalyzer =
                ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .setTargetResolution(Size(mActivity.previewView.width,
                        mActivity.previewView.height))
                    .build()
            iAnalyzer!!.setAnalyzer(cameraExecutor, qrAnalyzer!!)
            useCaseGroupBuilder.addUseCase(iAnalyzer!!)

        } else {
            if(isVideoMode){
                videoCapture = VideoCapture
                    .Builder()
                    .setTargetAspectRatio(aspectRatio)
                    .build()

                useCaseGroupBuilder.addUseCase(videoCapture!!)
            }
            useCaseGroupBuilder.addUseCase(imageCapture!!)
        }

        camera = cameraProvider!!.bindToLifecycle(
            mActivity, cameraSelector,
            useCaseGroupBuilder.build()
        )

        loadTabs()

        camera!!.cameraInfo.zoomState.observe(mActivity, {
            if(it.linearZoom!=0f){
                mActivity.zoomBar.updateThumb()
            }
        })

        mActivity.exposureBar.setExposureConfig(camera!!.cameraInfo.exposureState)

        // Focus camera on touch/tap
        mActivity.previewView.setOnTouchListener(mActivity)
        startAutoFocus()
        if (!isFlashAvailable) {
            mActivity.flashPager.currentItem = ImageCapture.FLASH_MODE_OFF
            flashMode = ImageCapture.FLASH_MODE_OFF
        }
    }

    fun snapPreview(){
        val animation: Animation = AlphaAnimation(1f, 0f)
        animation.duration = 100
        animation.interpolator = AccelerateDecelerateInterpolator()
        animation.repeatMode = Animation.REVERSE
        mActivity.imagePreview.startAnimation(animation)
    }

    private fun startAutoFocus() {
        val autoFocusPoint = SurfaceOrientedMeteringPointFactory(1f, 1f)
            .createPoint(.5f, .5f)
        val autoFocusAction = FocusMeteringAction.Builder(
            autoFocusPoint,
            FocusMeteringAction.FLAG_AF
        ).setAutoCancelDuration(AUTO_FOCUS_INTERVAL_IN_SECONDS.toLong(), TimeUnit.SECONDS).build()
        camera!!.cameraControl.startFocusAndMetering(autoFocusAction).addListener(
            { Log.i(TAG, "Auto-focusing every $AUTO_FOCUS_INTERVAL_IN_SECONDS seconds...") },
            ContextCompat.getMainExecutor(mActivity)
        )
    }

    private fun loadTabs(){

        val modes = getAvailableModes()
        val cModes = mActivity.tabLayout.getAllModes()

        var mae = true

        if(modes.size==cModes.size){
            for(index in 0 until modes.size){
                if(modes[index]!=cModes[index]){
                    mae = false
                    break
                }
            }
        } else mae = false

        if(mae) return

        Log.i(TAG, "Refreshing tabs...")

        mActivity.tabLayout.removeAllTabs()

        modes.forEach { mode ->
            mActivity.tabLayout.newTab().let {
                mActivity.tabLayout.addTab(it.setText(mode), false)
                if(mode=="CAMERA"){
                    it.select()
                }
            }
        }
    }

    private fun getAvailableModes(): ArrayList<String> {
        val modes = arrayListOf<String>()

        if(extensionsManager.isExtensionAvailable(cameraProvider!!, cameraSelector,
                ExtensionMode.BOKEH)){
            modes.add("NIGHT LIGHT")
        }

        if(extensionsManager.isExtensionAvailable(cameraProvider!!, cameraSelector,
                ExtensionMode.BOKEH)){
            modes.add("PORTRAIT")
        }

        if(extensionsManager.isExtensionAvailable(cameraProvider!!, cameraSelector,
                ExtensionMode.HDR)){
            modes.add("HDR")
        }

        if(extensionsManager.isExtensionAvailable(cameraProvider!!, cameraSelector,
                ExtensionMode.BEAUTY)){
            modes.add("BEAUTY")
        }

        if(!isVideoMode){
            modes.add("QR SCAN")
        }

        val mid = (modes.size/2f).roundToInt()
        modes.add(mid, "CAMERA")

        return modes
    }

    fun switchMode(modeText: String){

        cameraMode = ExtensionMode.NONE

        if(modeText == "CAMERA"){
            if(extensionsManager.isExtensionAvailable(cameraProvider!!, cameraSelector,
                    ExtensionMode.AUTO)){
                cameraMode = ExtensionMode.AUTO
            }
        } else {
            cameraMode = extensionModes.indexOf(modeText)
        }

        isQRMode = modeText=="QR SCAN"

        if(isQRMode){
            mActivity.qrOverlay.visibility = View.VISIBLE
            mActivity.threeButtons.visibility = View.INVISIBLE
            mActivity.flashPager.visibility = View.INVISIBLE
            mActivity.captureModeView.visibility = View.INVISIBLE
            mActivity.settingsIcon.visibility = View.INVISIBLE
        } else {
            mActivity.qrOverlay.visibility = View.INVISIBLE
            mActivity.threeButtons.visibility = View.VISIBLE
            mActivity.flashPager.visibility = View.VISIBLE
            mActivity.captureModeView.visibility = View.VISIBLE
            mActivity.settingsIcon.visibility = View.VISIBLE
        }

        startCamera(true)
    }

    companion object {
        private const val TAG = "CamConfig"
        const val AUTO_FOCUS_INTERVAL_IN_SECONDS = 2
        private val extensionModes = arrayOf("CAMERA", "PORTRAIT", "HDR", "NIGHT LIGHT",
            "BEAUTY", "AUTO")

        @JvmStatic
        @Throws(Throwable::class)
        fun getVideoThumbnail(p_videoPath: String?): Bitmap {

            val mBitmap: Bitmap
            var mMediaMetadataRetriever: MediaMetadataRetriever? = null

            try {
                mMediaMetadataRetriever = MediaMetadataRetriever()
                mMediaMetadataRetriever.setDataSource(p_videoPath)
                mBitmap = mMediaMetadataRetriever.frameAtTime!!
            } catch (m_e: Exception) {
                throw Throwable(
                    "Exception in retrieveVideoFrameFromVideo(String p_videoPath)"
                            + m_e.message
                )
            } finally {
                if (mMediaMetadataRetriever != null) {
                    mMediaMetadataRetriever.release()
                    mMediaMetadataRetriever.close()
                }
            }
            return mBitmap
        }

        @JvmStatic
        fun getExtension(file: File): String {
            val fileName = file.name
            val lastIndexOf = fileName.lastIndexOf(".")
            return if (lastIndexOf == -1) "" else fileName.substring(lastIndexOf + 1)
        }
    }
}