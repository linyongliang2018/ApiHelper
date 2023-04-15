package com.github.linyongliang2018.apihelper.jsonSchema;

import java.util.LinkedHashMap;

/**
 * @description: kv
 * @author: chengsheng@qbb6.com
 * @date: 2018/10/27
 */
public class KV<K, V> extends LinkedHashMap<K, V> {
    public <K, V> KV() {
    }

    public KV set(K key, V value) {
        super.put(key, value);
        return this;
    }
}
