package com.dj.dot.playback

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Size
import androidx.annotation.OptIn
import androidx.media3.common.util.BitmapLoader
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import com.dj.dot.DotApp
import com.dj.dot.MainActivity
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import java.util.concurrent.Callable
import java.util.concurrent.Executors

@OptIn(UnstableApi::class)
class DotService : MediaSessionService() {
    private var session: MediaSession? = null
    private var thumbnailLoader: ThumbnailLoader? = null

    override fun onCreate() {
        super.onCreate()
        val engine = DotApp.instance.engine
        val open = PendingIntent.getActivity(
            this, 0, Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
        val loader = ThumbnailLoader(this)
        thumbnailLoader = loader
        session = MediaSession.Builder(this, engine.sessionPlayer)
            .setSessionActivity(open)
            .setBitmapLoader(loader)
            .build()
        engine.onSessionPlayerChanged = { session?.player = it }
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo) = session

    /** Swiping Dot away from recents stops the music and clears the notification. */
    override fun onTaskRemoved(rootIntent: Intent?) {
        val engine = DotApp.instance.engine
        engine.pause(fade = false)
        engine.save()
        stopSelf()
    }

    override fun onDestroy() {
        DotApp.instance.engine.onSessionPlayerChanged = null
        session?.release()
        session = null
        thumbnailLoader?.release()
        thumbnailLoader = null
        super.onDestroy()
    }
}

/** Notification and lock-screen art straight from the file's embedded cover. */
@OptIn(UnstableApi::class)
private class ThumbnailLoader(private val context: Context) : BitmapLoader {
    private val executor = MoreExecutors.listeningDecorator(Executors.newSingleThreadExecutor())

    override fun supportsMimeType(mimeType: String) = true

    override fun decodeBitmap(data: ByteArray): ListenableFuture<Bitmap> = executor.submit(Callable {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeByteArray(data, 0, data.size, bounds)

        val targetSize = 512
        var sampleSize = 1
        val width = bounds.outWidth
        val height = bounds.outHeight
        if (width > 0 && height > 0) {
            while ((width / (sampleSize * 2)) >= targetSize && (height / (sampleSize * 2)) >= targetSize) {
                sampleSize *= 2
            }
        }

        val decodeOptions = BitmapFactory.Options().apply { inSampleSize = sampleSize }
        BitmapFactory.decodeByteArray(data, 0, data.size, decodeOptions) ?: error("Could not decode artwork")
    })

    override fun loadBitmap(uri: Uri): ListenableFuture<Bitmap> = executor.submit(Callable {
        context.contentResolver.loadThumbnail(uri, Size(512, 512), null)
    })

    fun release() {
        executor.shutdown()
    }
}
