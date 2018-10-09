package com.example.myapp.coolmediaplayer.fragment

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.example.myapp.coolmediaplayer.R


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

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_top_menu, container, false)
    }

//
}
