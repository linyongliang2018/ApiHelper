package com.github.linyongliang2018.apihelper.services;

import com.github.linyongliang2018.apihelper.jsonSchema.BuildJsonForYapi;
import com.github.linyongliang2018.apihelper.pojo.ApiDto;
import com.intellij.notification.NotificationDisplayType;
import com.intellij.notification.NotificationGroup;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @description: 入口
 * @author: chengsheng@qbb6.com
 * @date: 2019/5/15
 */
public class UploadToYapi extends AnAction {

    private static NotificationGroup notificationGroup;

    static {
        notificationGroup = new NotificationGroup("Java2Json.NotificationGroup", NotificationDisplayType.BALLOON, true);
    }
    @Override
    public void actionPerformed(AnActionEvent actionEvent) {
        //获得api 需上传的接口列表 参数对象
        BuildJsonForYapi buildJsonForYapi = new BuildJsonForYapi();
        List<ApiDto> apiDtos = buildJsonForYapi.actionPerformedList(actionEvent);
        List<String> requestBodies = apiDtos.stream().map(ApiDto::getResponse).collect(Collectors.toList());
        for (String requestBody : requestBodies) {
            System.out.println("requestBody = " + requestBody);
        }
        List<String> collect = apiDtos.stream().map(ApiDto::getRequestBody).collect(Collectors.toList());
        for (String s : collect) {
            System.out.println("s = " + s);
        }
    }

    /**
     * @description: 设置到剪切板
     * @param: [content]
     * @return: void
     * @author: chengsheng@qbb6.com
     * @date: 2019/7/3
     */
    private void setClipboard(String content) {
        //获取系统剪切板
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        //构建String数据类型
        StringSelection selection = new StringSelection(content);
        //添加文本到系统剪切板
        clipboard.setContents(selection, null);
    }
}
