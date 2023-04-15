package com.github.linyongliang2018.apihelper.pojo;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * yapi dto
 *
 * @author chengsheng@qbb6.com
 * @date 2019/2/11 3:16 PM
 */
public class YapiApiDTO implements Serializable {
    /**
     * 响应
     */
    private String response;
    /**
     * 请求体
     */
    private String requestBody;

    public YapiApiDTO() {
    }

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }


    public void setRequestBody(String requestBody) {
        this.requestBody = requestBody;
    }

    public String getRequestBody() {
        return requestBody;
    }

    @Override
    public String toString() {
        return "YapiApiDTO{" +
                "response='" + response + '\'' +
                ", requestBody='" + requestBody + '\'' +
                '}';
    }
}
