package com.genesys.glideimageview.sample.demo

import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.LinearLayout
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.genesys.glideimageview.GlideImageView
import com.genesys.glideimageview.OnLoadListener
import com.genesys.glideimageview.sample.R
import com.genesys.glideimageview.sample.databinding.ItemDemoCardBinding

data class DemoCard(
    val title: String,
    val subtitle: String,
    val source: Any?,
    val configure: (GlideImageView) -> Unit = {}
)

fun LinearLayout.bindDemoCards(cards: List<DemoCard>) {
    removeAllViews()
    val inflater = LayoutInflater.from(context)
    cards.forEach { card -> addView(inflate(inflater, this, card)) }
}

private fun inflate(
    inflater: LayoutInflater,
    parent: ViewGroup,
    card: DemoCard
): ViewGroup {
    val binding = ItemDemoCardBinding.inflate(inflater, parent, false)
    binding.tvTitle.text = card.title
    binding.tvSubtitle.text = card.subtitle

    binding.image.addOnLoadListener(object : OnLoadListener {
        override fun onLoadStarted(view: GlideImageView) {
            binding.tvStatus.setText(R.string.status_loading)
        }

        override fun onResourceReady(
            view: GlideImageView,
            resource: Drawable,
            dataSource: DataSource
        ) {
            binding.tvStatus.text =
                binding.root.context.getString(R.string.status_success, dataSource.name)
        }

        override fun onLoadFailed(view: GlideImageView, error: GlideException?) {
            binding.tvStatus.setText(R.string.status_failed)
        }

        override fun onCleared(view: GlideImageView) {
            binding.tvStatus.setText(R.string.status_cleared)
        }
    })

    card.configure(binding.image)
    binding.image.load(card.source)
    return binding.root
}
