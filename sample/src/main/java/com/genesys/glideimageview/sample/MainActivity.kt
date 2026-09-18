package com.genesys.glideimageview.sample

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.genesys.glideimageview.sample.databinding.ActivityMainBinding
import com.genesys.glideimageview.sample.demo.BasicsFragment
import com.genesys.glideimageview.sample.demo.ExtensionsFragment
import com.genesys.glideimageview.sample.demo.ListFragment
import com.genesys.glideimageview.sample.demo.PlaygroundFragment
import com.genesys.glideimageview.sample.demo.ShapesFragment
import com.google.android.material.tabs.TabLayoutMediator

private enum class DemoTab(@param:StringRes val titleRes: Int, val create: () -> Fragment) {
    BASICS(R.string.tab_basics, ::BasicsFragment),
    SHAPES(R.string.tab_shapes, ::ShapesFragment),
    LIST(R.string.tab_list, ::ListFragment),
    EXTENSIONS(R.string.tab_extensions, ::ExtensionsFragment),
    PLAYGROUND(R.string.tab_playground, ::PlaygroundFragment)
}

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.statusBars())
            view.updatePadding(top = insets.top)
            windowInsets
        }

        val tabs = DemoTab.entries
        binding.pager.adapter = DemoPagerAdapter(this, tabs)
        TabLayoutMediator(binding.tabs, binding.pager) { tab, position ->
            tab.setText(tabs[position].titleRes)
        }.attach()
    }

    private class DemoPagerAdapter(
        activity: FragmentActivity,
        private val tabs: List<DemoTab>
    ) : FragmentStateAdapter(activity) {
        override fun getItemCount(): Int = tabs.size
        override fun createFragment(position: Int): Fragment = tabs[position].create()
    }
}
