package me.nubuscu.hotpotato

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import me.nubuscu.hotpotato.menu.HostGameFragment
import me.nubuscu.hotpotato.menu.JoinGameFragment
import me.nubuscu.hotpotato.util.DataHolder

/**
 * Adapter to allow the main menu screen to tab between the join and host game fragments
 */
class MenuPagerAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

    private var context = DataHolder.instance.context.get()
    private var fragments = listOf(JoinGameFragment(), HostGameFragment())
    private var fragmentTitles = listOf(
        context?.getString(R.string.join_game) ?: "Join Game",
        context?.getString(R.string.host_game) ?: "Host Game"
    )

    override fun getItem(position: Int): Fragment {
        return fragments[position]
    }

    override fun getCount(): Int {
        return fragments.size
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return fragmentTitles[position]
    }


}