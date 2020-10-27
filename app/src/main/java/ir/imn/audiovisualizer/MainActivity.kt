package ir.imn.audiovisualizer

import android.Manifest
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import ir.imn.audiovisualizer.utils.showToast
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private lateinit var mediaPlayer: MediaPlayer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mediaPlayer = MediaPlayer.create(this, R.raw.test1)
            .apply { isLooping = true }

        playControlButton.setOnClickListener {
            togglePlayBack()
        }
    }

    private fun togglePlayBack() {
        if (mediaPlayer.isPlaying) {
            mediaPlayer.pause()
            playControlButton.text = getString(R.string.play)
        } else {
            checkAudioRecordPermission {
                mediaPlayer.start()
                visualizerView.link(mediaPlayer)
                playControlButton.text = getString(R.string.pause)
            }
        }
    }

    private fun checkAudioRecordPermission(block: () -> Unit) {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    this,
                    Manifest.permission.RECORD_AUDIO
                )
            ) {
                showToast(R.string.no_permission)
            } else {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.RECORD_AUDIO),
                    PERMISSION_REQUEST_CODE
                )
            }
        } else {
            block()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>, grantResults: IntArray
    ) {
        when (requestCode) {
            PERMISSION_REQUEST_CODE -> {
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    togglePlayBack()
                } else {
                    showToast(R.string.no_permission)
                }
                return
            }
        }
    }

    override fun onStop() {
        super.onStop()
        mediaPlayer.release()
    }

    companion object {
        private const val PERMISSION_REQUEST_CODE = 123
    }
}
