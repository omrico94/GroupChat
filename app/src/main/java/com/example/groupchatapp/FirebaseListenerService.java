package com.example.groupchatapp;

import android.os.Build;
import android.provider.ContactsContract;

import androidx.annotation.RequiresApi;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DatabaseReference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class FirebaseListenerService {

    private static HashMap<DatabaseReference, ArrayList<ChildEventListener>> m_ChildListenerToRemove;

    public static void addChildEventListenerToRemoveList(DatabaseReference ref, ChildEventListener childListener)
    {
        if(m_ChildListenerToRemove==null)
        {
            m_ChildListenerToRemove=new HashMap<>();
        }

        ArrayList<ChildEventListener> listeners = m_ChildListenerToRemove.get(ref);
        if(listeners ==null)
        {
            listeners = new ArrayList<>();

        }
        listeners.add(childListener);
        m_ChildListenerToRemove.put(ref,listeners);

    }

    public static void removeAllChildListeners() {

        if (m_ChildListenerToRemove != null) {
            for (DatabaseReference ref : m_ChildListenerToRemove.keySet()) {
                for (ChildEventListener listener : m_ChildListenerToRemove.get(ref)) {
                    ref.removeEventListener(listener);
                }
            }
            m_ChildListenerToRemove.clear();
        }
    }

}
