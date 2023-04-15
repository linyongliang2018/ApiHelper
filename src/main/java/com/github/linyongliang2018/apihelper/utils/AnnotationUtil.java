package com.github.linyongliang2018.apihelper.utils;

import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiAnnotationOwner;
import com.intellij.psi.PsiJavaCodeReferenceElement;
import com.intellij.psi.PsiModifierListOwner;
import com.intellij.psi.impl.source.SourceJavaCodeReference;
import org.jetbrains.annotations.NotNull;

/**
 * @author 40696
 */
public class AnnotationUtil {

    public static PsiAnnotation findAnnotation(@NotNull PsiModifierListOwner psiModifierListOwner, @NotNull String annotationFqn) {
        PsiAnnotationOwner annotationOwner = psiModifierListOwner.getModifierList();
        if (annotationOwner == null) {
            return null;
        }
        PsiAnnotation[] annotations = annotationOwner.getAnnotations();
        if (annotations.length == 0) {
            return null;
        }
        final String shortName = StringUtil.getShortName(annotationFqn);
        for (PsiAnnotation annotation : annotations) {
            PsiJavaCodeReferenceElement referenceElement = annotation.getNameReferenceElement();
            if (null != referenceElement) {
                final String referenceName = referenceElement.getReferenceName();
                if (shortName.equals(referenceName)) {
                    if (referenceElement.isQualified() && referenceElement instanceof SourceJavaCodeReference) {
                        String possibleFullQualifiedName = ((SourceJavaCodeReference) referenceElement).getClassNameText();
                        if (annotationFqn.equals(possibleFullQualifiedName)) {
                            return annotation;
                        }
                    }
                    String annotationQualifiedName = annotation.getQualifiedName();
                    if (null != annotationQualifiedName && annotationFqn.endsWith(annotationQualifiedName)) {
                        return annotation;
                    }
                }
            }
        }
        return null;
    }
}
