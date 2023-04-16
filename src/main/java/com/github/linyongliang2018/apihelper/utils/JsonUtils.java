package com.github.linyongliang2018.apihelper.utils;

import com.github.linyongliang2018.apihelper.constant.SpringMVCConstant;
import com.google.gson.GsonBuilder;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.search.GlobalSearchScope;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.github.linyongliang2018.apihelper.jsonSchema.GeneratorJsonSechema.getFields;

public class JsonUtils {

    /**
     * @description: 获得请求参数
     * @param: [project, yapiApiDTO, psiMethodTarget]
     * @return: void
     * @author: chengsheng@qbb6.com
     * @date: 2019/2/19
     */
    public static String getRequest(Project project, PsiMethod psiMethodTarget) {
        // 获取所有的参数列表
        PsiParameter[] psiParameters = psiMethodTarget.getParameterList().getParameters();
        for (PsiParameter psiParameter : psiParameters) {
            // 找到@RequestBody注解
            PsiAnnotation psiAnnotation = AnnotationUtil.findAnnotation(psiParameter, SpringMVCConstant.REQUEST_BODY);
            if (psiAnnotation != null) {
                return convertToJsonSchema(project, psiParameter.getType());
            }
        }
        return null;
    }

    /**
     * 获取给定PsiType的JSON表示
     *
     * @param project
     * @param psiType
     * @return
     */
    public static String convertToJsonSchema(Project project, PsiType psiType) {
        // 判断是否带有泛型
        String[] types = psiType.getCanonicalText().split("<");

        // 如果拆分后的数组长度大于1，表示泛型类型
        if (types.length > 1) {
            return dealGenerics(project, psiType, types);
        } else {
            // 非泛型类型的情况
            return dealCommonObject(project, psiType);
        }
    }

    private static String dealCommonObject(Project project, PsiType psiType) {
        // 通过JavaPsiFacade查找子类类型
        PsiClass psiClassChild = JavaPsiFacade.getInstance(project).findClass(psiType.getCanonicalText(), GlobalSearchScope.allScope(project));

        // 创建一个KV对象，用于存储结果
        Map<String, Object> result = new LinkedHashMap<>();

        // 创建一个列表，用于存储必需字段
        List<String> requiredList = new ArrayList<>();

        // 获取子类的字段，并将它们添加到kvObject中
        Map<String, Object> kvObject = getFields(psiClassChild, project, null, null, requiredList);

        // 向结果中添加相关属性
        result.put("type", "object");
        result.put("required", requiredList);
        result.put("title", psiType.getPresentableText());
        result.put("description", (psiType.getPresentableText() + " :" + psiClassChild.getName()).trim());
        result.put("properties", kvObject);

        // 使用GsonBuilder将结果转换为格式化的JSON字符串并返回
        return new GsonBuilder().setPrettyPrinting().create().toJson(result);
    }

    private static String dealGenerics(Project project, PsiType psiType, String[] types) {
        // 通过JavaPsiFacade查找子类类型
        PsiClass psiClassChild = JavaPsiFacade.getInstance(project).findClass(types[0], GlobalSearchScope.allScope(project));

        // 创建一个KV对象，用于存储结果
        Map<String, Object> result = new LinkedHashMap<String, Object>();

        // 创建一个列表，用于存储必需字段
        List<String> requiredList = new ArrayList<>();

        // 获取子类的字段，并将它们添加到kvObject中
        Map<String, Object> kvObject = getFields(psiClassChild, project, types, 1, requiredList);

        // 向结果中添加相关属性
        result.put("type", "object");
        result.put("title", psiType.getPresentableText());
        result.put("required", requiredList);
        result.put("description", (psiType.getPresentableText() + " :" + psiClassChild.getName()).trim());
        result.put("properties", kvObject);

        // 使用GsonBuilder将结果转换为格式化的JSON字符串并返回
        return new GsonBuilder().setPrettyPrinting().create().toJson(result);
    }

}
