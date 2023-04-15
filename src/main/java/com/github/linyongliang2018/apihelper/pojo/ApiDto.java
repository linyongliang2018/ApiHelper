package com.github.linyongliang2018.apihelper.pojo;

import java.io.Serializable;

/**
 * yapi dto
 *
 * @author chengsheng@qbb6.com
 * @date 2019/2/11 3:16 PM
 */
public class ApiDto implements Serializable {
    /**
     * 响应
     */
    private String response;
    /**
     * 请求体
     */
    private String requestBody;

    public ApiDto() {
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
