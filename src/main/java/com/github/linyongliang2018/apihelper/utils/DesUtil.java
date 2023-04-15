package com.github.linyongliang2018.apihelper.utils;

import com.google.common.base.Strings;
import com.intellij.psi.javadoc.PsiDocComment;

import java.util.Objects;

/**
 * 描述工具
 *
 * @author chengsheng@qbb6.com
 * @date 2019/4/30 4:13 PM
 */
public class DesUtil {


    /**
     * 去除字符串首尾出现的某个字符.
     *
     * @param source  源字符串.
     * @param element 需要去除的字符.
     * @return String.
     */
    public static String trimFirstAndLastChar(String source, char element) {
        boolean beginIndexFlag;
        boolean endIndexFlag;
        do {
            if (Strings.isNullOrEmpty(source.trim()) || source.equals(String.valueOf(element))) {
                source = "";
                break;
            }
            int beginIndex = source.indexOf(element) == 0 ? 1 : 0;
            int endIndex = source.lastIndexOf(element) + 1 == source.length() ? source.lastIndexOf(element) : source.length();
            source = source.substring(beginIndex, endIndex);
            beginIndexFlag = (source.indexOf(element) == 0);
            endIndexFlag = (source.lastIndexOf(element) + 1 == source.length());
        } while (beginIndexFlag || endIndexFlag);
        return source;
    }


    /**
     * 获得属性注释
     */
    public static String getFiledDesc(PsiDocComment psiDocComment) {
        if (Objects.nonNull(psiDocComment)) {
            String fileText = psiDocComment.getText();
            if (!Strings.isNullOrEmpty(fileText)) {
                String trim = fileText.replace("/", "").replace("*", "");
                String trimWhiteSpace = trim.replace(" ", "");
                String trimEnter = trimWhiteSpace.replace("\n", ",");
                String trimTab = trimEnter.replace("\t", "");
                return trimFirstAndLastChar(trimTab, ',');
            }
        }
        return "";
    }
}
