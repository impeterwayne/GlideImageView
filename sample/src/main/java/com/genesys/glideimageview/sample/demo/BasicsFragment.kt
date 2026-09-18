package com.genesys.glideimageview.sample.demo

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.genesys.glideimageview.GlideImageView
import com.genesys.glideimageview.OnLoadListener
import com.genesys.glideimageview.sample.R
import com.genesys.glideimageview.sample.databinding.FragmentBasicsBinding

class BasicsFragment : Fragment(R.layout.fragment_basics) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val binding = FragmentBasicsBinding.bind(view)

        trackStatus(binding.imgAssetDirect, binding.statusAssetDirect)
        trackStatus(binding.imgDrawable, binding.statusDrawable)
        trackStatus(binding.imgDrawableCircle, binding.statusDrawableCircle)
        trackStatus(binding.imgDrawableRadius, binding.statusDrawableRadius)
        trackStatus(binding.imgRemote, binding.statusRemote)
        trackStatus(binding.imgError, binding.statusError)
        trackStatus(binding.imgCache, binding.statusCache)
    }

    private fun trackStatus(image: GlideImageView, statusView: TextView) {
        image.addOnLoadListener(object : OnLoadListener {
            override fun onLoadStarted(view: GlideImageView) {
                statusView.setText(R.string.status_loading)
            }

            override fun onResourceReady(
                view: GlideImageView,
                resource: Drawable,
                dataSource: DataSource
            ) {
                statusView.text = getString(R.string.status_success, dataSource.name)
            }

            override fun onLoadFailed(view: GlideImageView, error: GlideException?) {
                statusView.setText(R.string.status_failed)
            }

            override fun onCleared(view: GlideImageView) {
                statusView.setText(R.string.status_cleared)
            }
        })
    }
}
