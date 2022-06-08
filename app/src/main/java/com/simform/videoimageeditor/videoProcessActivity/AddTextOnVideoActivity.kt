package com.simform.videoimageeditor.videoProcessActivity

import android.annotation.SuppressLint
import android.database.Cursor
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.text.TextUtils
import android.view.View
import android.widget.Toast
import com.jaiselrahman.filepicker.model.MediaFile
import com.simform.videoimageeditor.BaseActivity
import com.simform.videoimageeditor.R
import com.simform.videooperations.CallBackOfQuery
import com.simform.videooperations.Common
import com.simform.videooperations.Common.getFileFromAssets
import com.simform.videooperations.FFmpegCallBack
import com.simform.videooperations.LogMessage
import kotlinx.android.synthetic.main.activity_add_text_on_video.*
import java.util.concurrent.CompletableFuture.runAsync

class AddTextOnVideoActivity : BaseActivity(R.layout.activity_add_text_on_video, R.string.add_text_on_video) {
    private var isInputVideoSelected = false
    override fun initialization() {
        btnVideoPath.setOnClickListener(this)
        btnAdd.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btnVideoPath -> {
                Common.selectFile(this, maxSelection = 1, isImageSelection = false, isAudioSelection = false)
//                val intent = Intent(Intent.ACTION_PICK, MediaStore.Video.Media.EXTERNAL_CONTENT_URI)
//                intent.type = "video/*";
//                startActivityForResult(intent, 5)
//
//
//                    if (mediaFiles != null && mediaFiles.isNotEmpty()) {
//                        tvInputPathVideo.text = mediaFiles[0].path
//                        isInputVideoSelected = true
//                        runAsync {
//                            retriever = MediaMetadataRetriever()
//                            retriever?.setDataSource(tvInputPathVideo.text.toString())
//                            val bit = retriever?.frameAtTime
//                            width = bit?.width
//                            height = bit?.height
//                        }
//                    } else {
//                        Toast.makeText(this, getString(R.string.video_not_selected_toast_message), Toast.LENGTH_SHORT).show()
//                    }

            }
            R.id.btnAdd -> {
                when {
                    !isInputVideoSelected -> {
                        Toast.makeText(this, getString(R.string.input_video_validate_message), Toast.LENGTH_SHORT).show()
                    }
                    TextUtils.isEmpty(edtText.text.toString()) -> {
                        Toast.makeText(this, getString(R.string.please_add_text_validation), Toast.LENGTH_SHORT).show()
                    }
                    TextUtils.isEmpty(edtXPos.text.toString()) -> {
                        Toast.makeText(this, getString(R.string.x_position_validation), Toast.LENGTH_SHORT).show()
                    }
                    edtXPos.text.toString().toFloat() > 100 || edtXPos.text.toString().toFloat() <= 0 -> {
                        Toast.makeText(this, getString(R.string.x_validation_invalid), Toast.LENGTH_SHORT).show()
                    }
                    TextUtils.isEmpty(edtYPos.text.toString()) -> {
                        Toast.makeText(this, getString(R.string.y_position_validation), Toast.LENGTH_SHORT).show()
                    }
                    edtYPos.text.toString().toFloat() > 100 || edtYPos.text.toString().toFloat() <= 0 -> {
                        Toast.makeText(this, getString(R.string.y_validation_invalid), Toast.LENGTH_SHORT).show()
                    }
                    else -> {
                        processStart()
                        addTextProcess()
                    }
                }
            }
        }
    }

    private fun addTextProcess() {
        val outputPath = Common.getFilePath(this, Common.VIDEO)
        val xPos = width?.let {
            (edtXPos.text.toString().toFloat().times(it)).div(100)
        }
        val yPos = height?.let {
            (edtYPos.text.toString().toFloat().times(it)).div(100)
        }
        val fontPath = getFileFromAssets(this, "little_lord.ttf").absolutePath
        val query = ffmpegQueryExtension.addTextOnVideo(tvInputPathVideo.text.toString(), edtText.text.toString(), xPos, yPos, fontPath = fontPath, isTextBackgroundDisplay = true, fontSize = 28, fontcolor = "red", output = outputPath)
        CallBackOfQuery().callQuery(query, object : FFmpegCallBack {
            override fun process(logMessage: LogMessage) {
                tvOutputPath.text = logMessage.text
            }

            override fun success() {
                tvOutputPath.text = String.format(getString(R.string.output_path), outputPath)
                processStop()
            }

            override fun cancel() {
                processStop()
            }

            override fun failed() {
                processStop()
            }
        })
    }

    private fun processStop() {
        btnVideoPath.isEnabled = true
        btnAdd.isEnabled = true
        mProgressView.visibility = View.GONE
    }

    private fun processStart() {
        btnVideoPath.isEnabled = false
        btnAdd.isEnabled = false
        mProgressView.visibility = View.VISIBLE
    }

    fun parsePath(uri: Uri?): String? {
        val projection = arrayOf(MediaStore.Video.Media.DATA)
        val cursor: Cursor? = contentResolver.query(uri!!, projection, null, null, null)
        return if (cursor != null) {
            val columnIndex: Int = cursor
                .getColumnIndexOrThrow(MediaStore.Video.Media.DATA)
            cursor.moveToFirst()
            cursor.getString(columnIndex)
        } else null
    }
//
//    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        super.onActivityResult(requestCode, resultCode, data)
//        if (resultCode == RESULT_OK) {
//            when (requestCode) {
//                5 -> {
//                    val videoUri: Uri = data?.data!!
//                    val videoPath = parsePath(videoUri)
//                }
//            }
//        }
//    }

    @Throws(IllegalArgumentException::class)
    private fun getFileName(urim: Uri): String? {
        val cursor = contentResolver.query(urim, null, null, null, null)
        if (cursor!!.count <= 0) {
            cursor.close()
            throw IllegalArgumentException("Can't obtain file name, cursor is empty")
        }
        cursor.moveToFirst()
        val fileName = cursor.getString(cursor.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME))
        cursor.close()
        return fileName
    }

    @SuppressLint("NewApi")
    override fun selectedFiles(mediaFiles: List<MediaFile>?, requestCode: Int) {
        when (requestCode) {
            Common.VIDEO_FILE_REQUEST_CODE -> {
                if (mediaFiles != null && mediaFiles.isNotEmpty()) {
                    tvInputPathVideo.text = mediaFiles[0].path
                    isInputVideoSelected = true
                    runAsync {
                        retriever = MediaMetadataRetriever()
                        retriever?.setDataSource(tvInputPathVideo.text.toString())
                        val bit = retriever?.frameAtTime
                        width = bit?.width
                        height = bit?.height
                    }
                } else {
                    Toast.makeText(this, getString(R.string.video_not_selected_toast_message), Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

}