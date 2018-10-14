package com.example.myapp.coolmediaplayer.fragment

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.renderscript.Allocation
import android.renderscript.Element
import android.renderscript.RenderScript
import android.renderscript.ScriptIntrinsicBlur
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView

import com.example.myapp.coolmediaplayer.R
import com.example.myapp.coolmediaplayer.exoplayer.FtpDataSource
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.ExoPlayerFactory
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.ExtractorMediaSource
import com.google.android.exoplayer2.source.ads.AdsMediaSource
import com.google.android.exoplayer2.source.dash.DashMediaSource
import com.google.android.exoplayer2.ui.PlayerControlView
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.upstream.*
import com.google.android.exoplayer2.util.Util
import kotlinx.coroutines.experimental.Dispatchers
import kotlinx.coroutines.experimental.GlobalScope
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.launch
import org.apache.commons.net.ftp.FTPClient
import java.io.EOFException
import java.io.IOException
import java.io.InputStream


/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * [TopMenuFragment.OnFragmentInteractionListener] interface
 * to handle interaction events.
 * Use the [TopMenuFragment.newInstance] factory method to
 * create an instance of this fragment.
 *
 */

class TopMenuFragment : Fragment() {
    private var exoPlayer: SimpleExoPlayer? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_top_menu, container, false)
        initMainMenuBackground(view)
        initExoPlayer(context, view, Uri.parse("yoshiki/Music/alteration/01.mp3"))
        return view
    }

    private fun initExoPlayer(context: Context?, view: View, uri: Uri) {
        val dataSourceFactory = FtpDataSource.Factory()

        val mediaSource = ExtractorMediaSource.Factory(dataSourceFactory)
                .createMediaSource(uri)

        exoPlayer = ExoPlayerFactory.newSimpleInstance(context).apply {
            prepare(mediaSource)
            playWhenReady = true
        }

        view.findViewById<PlayerControlView>(R.id.playerControlView).apply {
            player = exoPlayer
            showTimeoutMs = 0
        }

        view.findViewById<PlayerView>(R.id.playerView).apply {
            player = exoPlayer
            defaultArtwork = context?.getDrawable(R.drawable.splatoon_wallpaper)
        }

    }

    private fun initMainMenuBackground(view: View) {
        GlobalScope.launch(Dispatchers.Main) {
            val splatoonBitmap = BitmapFactory.decodeResource(view.resources, R.drawable.splatoon_wallpaper)
            val blurBitmap = async { blur(context, splatoonBitmap, 25f) }
            view.findViewById<ImageView>(R.id.image_view_main_menu_background).run {
                setImageBitmap(blurBitmap.await())
            }
        }
    }

    private fun blur(context: Context?, input: Bitmap, blurRadius: Float): Bitmap {
        if (blurRadius == 0f) return input
        if (blurRadius < 1) return blur(context, input, 1f)
        if (blurRadius > 25) return blur(context, input, 25f)

        val output = Bitmap.createBitmap(input)
        val renderScript = RenderScript.create(context)
        val inAllocation = Allocation.createFromBitmap(renderScript, input)
        val outAllocation = Allocation.createFromBitmap(renderScript, output)
        ScriptIntrinsicBlur.create(renderScript, Element.U8_4(renderScript)).run {
            setInput(inAllocation)
            setRadius(blurRadius)
            forEach(outAllocation)
        }
        outAllocation.copyTo(output)
        return output
    }

    //
    companion object {
        private val TAG = this::class::simpleName.get()
    }
}
