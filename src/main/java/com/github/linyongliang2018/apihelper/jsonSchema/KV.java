package com.github.linyongliang2018.apihelper.jsonSchema;

import java.util.LinkedHashMap;

/**
 * @description: kv
 * @author: chengsheng@qbb6.com
 * @date: 2018/10/27
 */
public class KV<String, Object> extends LinkedHashMap<String, Object> {
    public KV() {
    }

    public KV<String,Object> set(String key, Object value) {
        super.put(key, value);
        return this;
    }
}
