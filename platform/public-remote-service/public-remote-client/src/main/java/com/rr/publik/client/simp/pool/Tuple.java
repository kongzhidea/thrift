package com.rr.publik.client.simp.pool;

import java.io.Serializable;

/**
 * @author he.li
 */
public class Tuple<K, T> implements Serializable{

    private static final long serialVersionUID = -2522476715868603699L;

    public K left;

    public T right;

    public Tuple(K left, T right) {
        this.left = left;
        this.right = right;
    }
    
    public static <K, T> Tuple<K,T> newTuple(K left, T right) {
        return new Tuple<K, T>(left, right);
    }
    
    public String toString() {
        return this.left + "," + this.right;
    }
}
