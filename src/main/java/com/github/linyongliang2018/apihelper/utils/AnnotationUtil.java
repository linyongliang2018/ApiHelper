package com.github.linyongliang2018.apihelper.utils;

import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiAnnotationOwner;
import com.intellij.psi.PsiJavaCodeReferenceElement;
import com.intellij.psi.PsiModifierListOwner;
import com.intellij.psi.impl.source.SourceJavaCodeReference;
import org.apache.commons.lang3.ObjectUtils;
import org.jetbrains.annotations.NotNull;

/**
 * @author 40696
 */
public class AnnotationUtil {

    /**
     * 用于查找特定全限定名的注解
     *
     * @param psiModifierListOwner
     * @param annotationFqn
     * @return
     */
    public static PsiAnnotation findAnnotation(@NotNull PsiModifierListOwner psiModifierListOwner, @NotNull String annotationFqn) {
        // 从psiModifierListOwner中获取注解所有者（PsiAnnotationOwner）
        PsiAnnotationOwner annotationOwner = psiModifierListOwner.getModifierList();
        // 从注解所有者中获取所有注解
        PsiAnnotation[] annotations = annotationOwner.getAnnotations();
        // 如果注解所有者为空，则返回null
        if (annotations.length == 0) {
            return null;
        }
        // 获取注解全限定名的短名称（即类名）
        final String shortName = StringUtil.getShortName(annotationFqn);
        // 遍历所有注解
        for (PsiAnnotation annotation : annotations) {
            // 获取注解的名称引用元素
            PsiJavaCodeReferenceElement referenceElement = annotation.getNameReferenceElement();
            // 如果引用元素不为null
            if (ObjectUtils.isNotEmpty(referenceElement)) {
                // 获取引用名称
                final String referenceName = referenceElement.getReferenceName();
                // 如果短名称与引用名称相同
                if (shortName.equals(referenceName)) {
                    // 如果引用元素是限定的且为SourceJavaCodeReference类型
                    if (referenceElement.isQualified() && referenceElement instanceof SourceJavaCodeReference) {
                        // 获取可能的全限定名
                        String possibleFullQualifiedName = ((SourceJavaCodeReference) referenceElement).getClassNameText();
                        // 如果全限定名与可能的全限定名相同，则返回此注解
                        if (annotationFqn.equals(possibleFullQualifiedName)) {
                            return annotation;
                        }
                    }
                    // 获取注解的全限定名
                    String annotationQualifiedName = annotation.getQualifiedName();
                    // 如果注解全限定名不为空且与给定的全限定名相同，则返回此注解
                    if (null != annotationQualifiedName && annotationFqn.endsWith(annotationQualifiedName)) {
                        return annotation;
                    }
                }
            }
        }
        // 如果没有找到匹配的注解，则返回null
        return null;
    }

}
