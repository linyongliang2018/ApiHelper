package com.github.linyongliang2018.apihelper.jsonSchema;

import com.google.gson.GsonBuilder;

import java.util.LinkedHashMap;

/**
 * @description: kv
 * @author: chengsheng@qbb6.com
 * @date: 2018/10/27
 */
public class KV<K, V> extends LinkedHashMap<K, V> {
    public <K, V> KV() {
    }
    public static <K, V> KV create() {
        return new KV();
    }

    public KV set(K key, V value) {
        super.put(key, value);
        return this;
    }


    public KV set(KV KV) {
        super.putAll(KV);
        return this;
    }


    public String toPrettyJson() {
        return new GsonBuilder().setPrettyPrinting().create().toJson(this);
    }

    public boolean equals(Object KV) {
        return KV instanceof KV && super.equals(KV);
    }


}
