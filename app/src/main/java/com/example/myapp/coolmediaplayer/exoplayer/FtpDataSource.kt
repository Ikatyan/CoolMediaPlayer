package com.example.myapp.coolmediaplayer.exoplayer

import android.net.Uri
import android.util.Log
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.upstream.BaseDataSource
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DataSpec
import com.google.android.exoplayer2.upstream.FileDataSource
import org.apache.commons.net.ftp.FTP
import org.apache.commons.net.ftp.FTPClient
import java.io.EOFException
import java.io.IOException
import java.io.InputStream

class FtpDataSource : BaseDataSource(true) {
    private val hostName = "192.168.1.1"
    private val userName = "yoshiki"
    private val password = "ikatyan4649"
    private var uri: Uri? = null
    private var remainingBytes = 0L
    private var isOpened = false
    private val ftpClient = FTPClient()
    private var inputStream: InputStream? = null

    companion object {
        private val TAG = FtpDataSource::class::simpleName.get()
    }

    override fun open(dataSpec: DataSpec?): Long {
        if (!isOpened) {
            ftpClient.let {
                it.connect(hostName)
                Log.d(TAG, it.replyString)
                it.login(userName, password)
                Log.d(TAG, it.replyString)
                it.enterLocalPassiveMode()
                it.setFileType(FTP.BINARY_FILE_TYPE)
            }
        }
        dataSpec?.let {
            this.uri = it.uri
            val file = ftpClient.mlistFile(it.uri.path)
            this.remainingBytes = if (it.length == C.LENGTH_UNSET.toLong()) file.size - it.position else it.length
            if (remainingBytes < 0) throw EOFException()
            this.inputStream = ftpClient.retrieveFileStream(it.uri.path)
            this.inputStream!!.skip(it.position)
        }
        isOpened = true
        transferStarted(dataSpec)
        return this.remainingBytes
    }

    override fun getUri(): Uri? {
        return uri
    }

    override fun close() {
        try {
            inputStream?.close()
            ftpClient.logout()
            ftpClient.disconnect()
        } catch (e: IOException) {
            throw FileDataSource.FileDataSourceException(e)
        } finally {
            if (isOpened) {
                isOpened = false
                transferEnded()
            }

        }
    }

    override fun read(buffer: ByteArray?, offset: Int, readLength: Int): Int {
        if (inputStream == null) return C.RESULT_NOTHING_READ
        if (readLength == 0) return 0
        if (remainingBytes == 0L) return C.RESULT_END_OF_INPUT

        val readBytes = inputStream!!.read(buffer, offset, Math.min(remainingBytes, readLength.toLong()).toInt())
        if (readBytes > 0) {
            remainingBytes -= readBytes
            bytesTransferred(readBytes)
        }
        return readBytes
    }

    class Factory : DataSource.Factory {
        override fun createDataSource(): DataSource {
            return FtpDataSource()
        }

    }

}