package me.nubuscu.hotpotato

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout
import me.nubuscu.hotpotato.util.DataHolder
import me.nubuscu.hotpotato.util.GameInfoHolder
import java.lang.ref.WeakReference

const val serviceId = "me.nubuscu.hotpotato"

class MainActivity : ThemedActivity() {

    private lateinit var adapter: MenuPagerAdapter
    private lateinit var tabLayout: TabLayout
    private lateinit var viewPager: ViewPager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(findViewById(R.id.toolbar))
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

        // TODO remove debug button
        val debugInGameButton: Button = findViewById(R.id.debugInGameButton)
        debugInGameButton.setOnClickListener {
            GameInfoHolder.instance.isHost = true
            val intent = Intent(this, InGameActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.home, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.settings -> {
                val intent = Intent(this, SettingsActivity::class.java)
                startActivity(intent)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}

