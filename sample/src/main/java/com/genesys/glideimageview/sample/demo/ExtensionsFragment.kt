package com.genesys.glideimageview.sample.demo

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.Priority
import com.genesys.glideimageview.RequestManagerFactory
import com.genesys.glideimageview.sample.R
import com.genesys.glideimageview.sample.SampleImages
import com.genesys.glideimageview.sample.databinding.FragmentScrollListBinding
import com.genesys.glideimageview.sample.ext.Avatar
import com.genesys.glideimageview.sample.ext.AuthHeaderResolver
import com.genesys.glideimageview.sample.ext.BorderShape
import com.genesys.glideimageview.sample.ext.ExactSizeDecorator
import com.genesys.glideimageview.sample.ext.SquircleShape
import com.genesys.glideimageview.sample.ext.priorityDecorator
import com.genesys.glideimageview.sample.ext.thumbnailDecorator

class ExtensionsFragment : Fragment(R.layout.fragment_scroll_list) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        FragmentScrollListBinding.bind(view).container.bindDemoCards(cards())
    }

    private fun cards(): List<DemoCard> {
        val density = resources.displayMetrics.density
        return listOf(
            DemoCard(
                title = getString(R.string.ext_model_title),
                subtitle = "image.load(Avatar(userId = 42))",
                source = Avatar(userId = 42, sizePx = 600)
            ),
            DemoCard(
                title = getString(R.string.ext_headers_title),
                subtitle = "image.modelResolvers += AuthHeaderResolver { token }",
                source = SampleImages.REMOTE_SQUARE,
                configure = { image ->
                    image.modelResolvers += AuthHeaderResolver { "demo-access-token" }
                }
            ),
            DemoCard(
                title = getString(R.string.ext_shape_title),
                subtitle = "image.shapes = listOf(SquircleShape(), BorderShape(...))",
                source = SampleImages.REMOTE,
                configure = { image ->
                    image.shapes = listOf(
                        SquircleShape(curvature = 4f),
                        BorderShape(3 * density, 0xFF2563EB.toInt())
                    )
                }
            ),
            DemoCard(
                title = getString(R.string.ext_decorator_title),
                subtitle = "image.decorators += thumbnail(0.05f), override(size), priority(LOW)",
                source = "https://picsum.photos/seed/decorated/1200/1200",
                configure = { image ->
                    image.decorators += thumbnailDecorator(0.05f)
                    image.decorators += ExactSizeDecorator
                    image.decorators += priorityDecorator(Priority.LOW)
                }
            ),
            DemoCard(
                title = getString(R.string.ext_manager_title),
                subtitle = "image.requestManagerFactory = { Glide.with(it.context) }",
                source = SampleImages.ICON,
                configure = { image ->
                    image.requestManagerFactory = RequestManagerFactory {
                        Glide.with(it.context.applicationContext)
                    }
                }
            )
        )
    }
}
