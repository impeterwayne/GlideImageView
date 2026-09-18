package com.genesys.glideimageview.sample.demo

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.genesys.glideimageview.sample.R
import com.genesys.glideimageview.sample.SampleImages
import com.genesys.glideimageview.sample.databinding.FragmentScrollListBinding

class BasicsFragment : Fragment(R.layout.fragment_scroll_list) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        FragmentScrollListBinding.bind(view).container.bindDemoCards(cards())
    }

    private fun cards() = listOf(
        DemoCard(
            title = getString(R.string.basics_remote_title),
            subtitle = "image.load(\"https://…\")",
            source = SampleImages.REMOTE
        ),
        DemoCard(
            title = getString(R.string.basics_asset_title),
            subtitle = "image.load(\"images/sample_banner.webp\")",
            source = SampleImages.BANNER
        ),
        DemoCard(
            title = getString(R.string.basics_resource_title),
            subtitle = "image.load(R.drawable.placeholder_image)",
            source = R.drawable.placeholder_image
        ),
        DemoCard(
            title = getString(R.string.basics_error_title),
            subtitle = "glideError drawable, from GlideImageViewConfig.defaults",
            source = SampleImages.BROKEN
        ),
        DemoCard(
            title = getString(R.string.basics_fallback_title),
            subtitle = "image.load(null) with options.fallback set",
            source = null,
            configure = { image ->
                image.updateOptions { copy(fallback = R.drawable.error_image) }
            }
        )
    )
}
