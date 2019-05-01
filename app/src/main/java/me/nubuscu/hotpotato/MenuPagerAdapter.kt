package me.nubuscu.hotpotato

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import me.nubuscu.hotpotato.menu.HostGameFragment
import me.nubuscu.hotpotato.menu.JoinGameFragment

class MenuPagerAdapter(fm: FragmentManager): FragmentPagerAdapter(fm) {

    private var fragments = listOf(JoinGameFragment(), HostGameFragment())
    private var fragmentTitles = listOf("Join Game", "Host Game") //TODO set titles via static string values
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