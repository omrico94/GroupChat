package com.example.groupchatapp;

import androidx.fragment.app.Fragment;

public abstract class MyFragment extends Fragment {

    protected String title;

    @Override
    public String toString() {
        return title;
    }
}
