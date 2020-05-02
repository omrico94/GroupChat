package com.example.groupchatapp.Models;

public class MyPair<T1,T2> {
    private T1 m_First;
    private T2 m_Second;

    public MyPair() {

    }

    public MyPair(T1 first, T2 second) {
        m_First = first;
        m_Second = second;
    }

    public T1 getFirst() {
        return m_First;
    }

    public T2 getSecond() {
        return m_Second;
    }

    public void setFirst(T1 first) {
        m_First = first;
    }

    public void setSecond(T2 second) {
        m_Second = second;
    }


}
