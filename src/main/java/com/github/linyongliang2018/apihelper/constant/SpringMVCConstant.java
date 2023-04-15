package com.github.linyongliang2018.apihelper.constant;

import com.intellij.ide.actions.FqnUtil;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * Spring mvc 注解包路径
 *
 * @author chengsheng@qbb6.com
 * @date 2019/2/11 3:57 PM
 */
public interface SpringMVCConstant {
    String REQUEST_BODY = "org.springframework.web.bind.annotation.RequestBody";

    public static void main(String[] args) {
        String name = RequestBody.class.getName();
        System.out.println("name = " + name);
    }
}
