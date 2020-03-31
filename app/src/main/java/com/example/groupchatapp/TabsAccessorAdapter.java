package com.example.groupchatapp;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import java.util.ArrayList;
import java.util.List;

public class TabsAccessorAdapter extends FragmentPagerAdapter
{
    public TabsAccessorAdapter(FragmentManager fm)
    {
        super(fm);
    }
    private List<Fragment> fragments = new ArrayList<Fragment>(){{
        add(new MyGroupsFragment());
 //       add(new GroupsFragment());
   //     add(new ContactsFragment());
    }};

    @Override
    public Fragment getItem(int position) {
        return fragments.get(position);
    }

    @Override
    public int getCount()
    {
        return fragments.size();
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position)
    {
        return fragments.get(position).toString();
    }
}

