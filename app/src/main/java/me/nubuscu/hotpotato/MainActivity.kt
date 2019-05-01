package me.nubuscu.hotpotato

import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout
import me.nubuscu.hotpotato.util.DataHolder
import java.lang.ref.WeakReference

const val serviceId = "me.nubuscu.hotpotato"
class MainActivity : AppCompatActivity() {

    private lateinit var adapter: MenuPagerAdapter
    private lateinit var tabLayout: TabLayout
    private lateinit var viewPager: ViewPager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        //request dangerous permissions if we need to
        if (Build.VERSION.SDK_INT >= 23) {
            val permissions = arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION)
            requestPermissions(permissions, 100)
        }
        DataHolder.instance.context = WeakReference(this)


        viewPager = findViewById(R.id.viewPager)
        tabLayout = findViewById(R.id.tabLayout)

        adapter = MenuPagerAdapter(supportFragmentManager)
        viewPager.adapter = adapter
        tabLayout.setupWithViewPager(viewPager)
    }
}

