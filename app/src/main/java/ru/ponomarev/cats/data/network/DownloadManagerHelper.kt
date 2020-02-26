package ru.ponomarev.cats.data.network

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Environment
import android.os.Looper
import android.webkit.URLUtil
import androidx.core.content.ContextCompat
import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.disposables.Disposables
import io.reactivex.schedulers.Schedulers
import ru.ponomarev.cats.di.qualifiers.AppContext
import java.io.File
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject


class DownloadManagerHelper @Inject constructor(@AppContext private val context: Context) {

    private val parentPath =
        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
    private val downloadManager: DownloadManager by lazy {
        ContextCompat.getSystemService(
            context,
            DownloadManager::class.java
        )!!
    }
    private var downloadID = 0L

    fun downloadFile(url: String): Single<File> {
        return Single
            .fromCallable { startDownload(url) }
            .flatMap { checkFileDownloaded() }
            .filter { it }
            .map { createFile(url) }
            .toSingle()
    }

    private fun createFile(link: String) = File(parentPath, createFileName(link))

    private fun createFileName(link: String) = URLUtil.guessFileName(link, null, null)

    private fun startDownload(link: String) {
        val fileName = createFileName(link)
        DownloadManager.Request(Uri.parse(link)).apply {
            setAllowedNetworkTypes(DownloadManager.Request.NETWORK_MOBILE or DownloadManager.Request.NETWORK_WIFI)
            setTitle(fileName)
            setDescription(fileName)
            setVisibleInDownloadsUi(true)
            setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)

            downloadID = downloadManager.enqueue(this)
        }
    }

    private fun checkFileDownloaded(): Single<Boolean> =
        DownloadReceiver(context)
            .subscribeOn(AndroidSchedulers.mainThread())
            .observeOn(Schedulers.io())
            .map { it.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1) }
            .filter { it == downloadID }
            .map { id -> checkDownloadStatus(downloadManager, id) }
            .firstOrError()

    private fun checkDownloadStatus(dm: DownloadManager, downloadId: Long): Boolean {
        val query = DownloadManager.Query().setFilterById(downloadId)
        val cursor = dm.query(query)
        cursor?.use {
            if (it.moveToFirst()) {
                val statusColumnIndex = it.getColumnIndex(DownloadManager.COLUMN_STATUS)
                return it.getInt(statusColumnIndex) == DownloadManager.STATUS_SUCCESSFUL
            }
        }
        return false
    }

}

class DownloadReceiver(private val context: Context) : Observable<Intent>() {

    private lateinit var receiver: Receiver

    override fun subscribeActual(observer: Observer<in Intent>) {
        if (checkLooperThread(observer).not()) {
            return
        }
        receiver = Receiver(
            onNext = observer::onNext,
            onDisposed = { context.unregisterReceiver(receiver) }
        )
        if (Looper.myLooper() == Looper.getMainLooper()) {
            context.registerReceiver(
                receiver,
                IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE)
            )
        }
        observer.onSubscribe(receiver)
    }

    private fun checkLooperThread(observer: Observer<*>): Boolean {
        return if (Looper.myLooper() == null) {
            observer.onSubscribe(Disposables.empty())
            observer.onError(IllegalStateException("Calling thread is not associated with Looper"))
            false
        } else {
            true
        }
    }

    class Receiver(
        private val onNext: (Intent) -> Unit,
        private val onDisposed: () -> Unit
    ) : BroadcastReceiver(), Disposable {

        private val disposed = AtomicBoolean(false)

        override fun dispose() {
            if (disposed.compareAndSet(false, true)) {
                onDisposed()
            }
        }

        override fun isDisposed(): Boolean {
            return disposed.get()
        }

        override fun onReceive(context: Context, intent: Intent) {
            if (!isDisposed) {
                onNext(intent)
            }
        }
    }
}