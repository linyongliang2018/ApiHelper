package com.github.linyongliang2018.apihelper.utils;

import com.google.common.base.Strings;
import com.intellij.openapi.project.Project;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiField;
import com.intellij.psi.impl.source.PsiClassImpl;
import com.intellij.psi.impl.source.PsiJavaFileImpl;
import com.intellij.psi.javadoc.PsiDocComment;
import com.intellij.psi.search.GlobalSearchScope;

import java.util.ArrayList;
import java.util.List;
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
        boolean beginIndexFlag = true;
        boolean endIndexFlag = true;
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
     * @description: 获得属性注释
     * @param: [psiDocComment]
     * @return: java.lang.String
     * @author: chengsheng@qbb6.com
     * @date: 2019/4/27
     */
    public static String getFiledDesc(PsiDocComment psiDocComment) {
        if (Objects.nonNull(psiDocComment)) {
            String fileText = psiDocComment.getText();
            if (!Strings.isNullOrEmpty(fileText)) {
                return trimFirstAndLastChar(fileText.replace("*", "").replace("/", "").replace(" ", "").replace("\n", ",").replace("\t", ""), ',').split("\\{@link")[0];
            }
        }
        return "";
    }


    /**
     * @description: 获得link 备注
     * @param: [remark, project, field]
     * @return: java.lang.String
     * @author: chengsheng@qbb6.com
     * @date: 2019/5/18
     */
    public static String getLinkRemark(String remark, Project project, PsiField field) {
        // 尝试获得@link 的常量定义
        if (Objects.isNull(field.getDocComment())) {
            return remark;
        }
        String[] linkString = field.getDocComment().getText().split("@link");
        if (linkString.length > 1) {
            //说明有link
            String linkAddress = linkString[1].split("}")[0].trim();
            PsiClass psiClassLink = JavaPsiFacade.getInstance(project).findClass(linkAddress, GlobalSearchScope.allScope(project));
            if (Objects.isNull(psiClassLink)) {
                //可能没有获得全路径，尝试获得全路径
                String[] importPaths = field.getParent().getContext().getText().split("import");
                if (importPaths.length > 1) {
                    for (String importPath : importPaths) {
                        if (importPath.contains(linkAddress.split("\\.")[0])) {
                            linkAddress = importPath.split(linkAddress.split("\\.")[0])[0] + linkAddress;
                            psiClassLink = JavaPsiFacade.getInstance(project).findClass(linkAddress.trim(), GlobalSearchScope.allScope(project));
                            break;
                        }
                    }
                } else {
                    //如果是同包情况
                    linkAddress = ((PsiJavaFileImpl) ((PsiClassImpl) field.getParent()).getContext()).getPackageName() + "." + linkAddress;
                    psiClassLink = JavaPsiFacade.getInstance(project).findClass(linkAddress, GlobalSearchScope.allScope(project));
                }
                //如果小于等于一为不存在import，不做处理
            }
            if (Objects.nonNull(psiClassLink)) {
                //说明获得了link 的class
                PsiField[] linkFields = psiClassLink.getFields();
                if (linkFields.length > 0) {
                    remark += "," + psiClassLink.getName() + "[";
                    for (int i = 0; i < linkFields.length; i++) {
                        PsiField psiField = linkFields[i];
                        if (i > 0) {
                            remark += ",";
                        }
                        // 先获得名称
                        remark += psiField.getName();
                        // 后获得value,通过= 来截取获得，第二个值，再截取;
                        String[] splitValue = psiField.getText().split("=");
                        if (splitValue.length > 1) {
                            String value = splitValue[1].split(";")[0];
                            remark += ":" + value;
                        }
                        String filedValue = DesUtil.getFiledDesc(psiField.getDocComment());
                        if (!Strings.isNullOrEmpty(filedValue)) {
                            remark += "(" + filedValue + ")";
                        }
                    }
                    remark += "]";
                }
            }
        }
        return remark;
    }


    /**
     * @description: 获得从start 开始 end 结束中间的内容
     * @param: [content, start, end]
     * @return: java.util.List<java.lang.String>
     * @author: chengsheng@qbb6.com
     * @date: 2019/7/2
     */
    public static List<PsiClass> getFieldLinks(Project project, PsiField field) {
        if (Objects.isNull(field.getDocComment())) {
            return new ArrayList<>();
        }
        List<PsiClass> result = new ArrayList<>();
        String[] linkstr = field.getDocComment().getText().split("@link");
        for (int i = 1; i < linkstr.length; i++) {
            String linkAddress = linkstr[i].split("}")[0].trim();
            PsiClass psiClassLink = JavaPsiFacade.getInstance(project).findClass(linkAddress, GlobalSearchScope.allScope(project));
            if (Objects.isNull(psiClassLink)) {
                //可能没有获得全路径，尝试获得全路径
                String[] importPaths = field.getParent().getContext().getText().split("import");
                if (importPaths.length > 1) {
                    for (String importPath : importPaths) {
                        if (importPath.contains(linkAddress.split("\\.")[0])) {
                            linkAddress = importPath.split(linkAddress.split("\\.")[0])[0] + linkAddress;
                            psiClassLink = JavaPsiFacade.getInstance(project).findClass(linkAddress.trim(), GlobalSearchScope.allScope(project));
                            if (Objects.nonNull(psiClassLink)) {
                                result.add(psiClassLink);
                            }
                            break;
                        }
                    }
                } else {
                    //如果是同包情况
                    linkAddress = ((PsiJavaFileImpl) ((PsiClassImpl) field.getParent()).getContext()).getPackageName() + "." + linkAddress;
                    psiClassLink = JavaPsiFacade.getInstance(project).findClass(linkAddress, GlobalSearchScope.allScope(project));
                    if (Objects.nonNull(psiClassLink)) {
                        result.add(psiClassLink);
                    }
                }
                //如果小于等于一为不存在import，不做处理
            } else {
                result.add(psiClassLink);
            }
        }
        return result;
    }
}