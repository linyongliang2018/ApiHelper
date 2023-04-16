package com.github.linyongliang2018.apihelper.utils;

import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.impl.source.PsiClassReferenceType;
import com.intellij.psi.javadoc.PsiDocComment;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;

/**
 * @author 40696
 */
public class PsiUtil {

    public static PsiMethod findSelectedPsiMethod(@NotNull Project project, @NotNull Editor editor) {
        int offset = editor.getCaretModel().getOffset();
        PsiFile psiFile = PsiDocumentManager.getInstance(project).getPsiFile(editor.getDocument());
        if (psiFile == null) {
            return null;
        }

        PsiElement elementAtOffset = psiFile.findElementAt(offset);
        if (elementAtOffset == null) {
            return null;
        }

        return PsiTreeUtil.getParentOfType(elementAtOffset, PsiMethod.class);
    }

    public static boolean isRequestBodyAnnotated(@NotNull PsiModifierListOwner psiModifierListOwner) {
        String requestBodyAnnotation = "org.springframework.web.bind.annotation.RequestBody";
        PsiAnnotation annotation = findAnnotation(psiModifierListOwner, requestBodyAnnotation);
        return annotation != null;
    }

    public static PsiAnnotation findAnnotation(@NotNull PsiModifierListOwner psiModifierListOwner, @NotNull String annotationFqn) {
        PsiModifierList modifierList = psiModifierListOwner.getModifierList();
        if (modifierList == null) {
            return null;
        }
        return modifierList.findAnnotation(annotationFqn);
    }

    public static PsiClass resolveClassFromPsiType(PsiType psiType) {
        if (psiType instanceof PsiClassType) {
            return ((PsiClassType) psiType).resolve();
        }
        return null;
    }

    public static String getJsonSchemaType(PsiType psiType) {
        if (psiType == null) {
            return null;
        }

        if (psiType instanceof PsiPrimitiveType) {
            PsiPrimitiveType primitiveType = (PsiPrimitiveType) psiType;
            if (primitiveType.equals(PsiType.INT) || primitiveType.equals(PsiType.LONG)
                    || primitiveType.equals(PsiType.DOUBLE) || primitiveType.equals(PsiType.FLOAT)
                    || primitiveType.equals(PsiType.SHORT) || primitiveType.equals(PsiType.BYTE)) {
                return "number";
            } else if (primitiveType.equals(PsiType.BOOLEAN)) {
                return "boolean";
            } else if (primitiveType.equals(PsiType.CHAR)) {
                return "string";
            }
        } else if (psiType instanceof PsiClassType) {
            PsiClassType classType = (PsiClassType) psiType;
            String typeName = classType.getPresentableText();

            if (typeName != null && typeName.startsWith("String")) {
                return "string";
            } else if (psiType instanceof PsiClassReferenceType) {
                PsiClassReferenceType referenceType = (PsiClassReferenceType) psiType;
                PsiType[] typeParameters = referenceType.getParameters();

                if (typeParameters.length > 0) {
                    StringBuilder sb = new StringBuilder();
                    sb.append(typeName).append("<");

                    for (int i = 0; i < typeParameters.length; i++) {
                        sb.append(getJsonSchemaType(typeParameters[i]));

                        if (i < typeParameters.length - 1) {
                            sb.append(", ");
                        }
                    }

                    sb.append(">");
                    return sb.toString();
                }
            }

            return "object";
        }

        return null;
    }

    public static String getFieldDescription(PsiField field) {
        PsiDocComment docComment = field.getDocComment();
        if (docComment != null) {
            return docComment.getText();
        }
        return "";
    }

    public static boolean isFieldRequired(PsiField field) {
        return findAnnotation(field, "javax.validation.constraints.NotNull") != null || findAnnotation(field, "javax.validation.constraints.NotBlank") != null;
    }

    public static String getClassDescription(PsiClass psiClass) {
        PsiDocComment docComment = psiClass.getDocComment();
        if (docComment != null) {
            return docComment.getText();
        }
        return "";
    }

    /**
     * 是否表示一个泛型集合类型
     *
     * @param psiType
     * @return
     */
    public static boolean isGenericCollectionType(PsiType psiType) {
        if (psiType instanceof PsiClassType) {
            PsiClass psiClass = ((PsiClassType) psiType).resolve();
            if (psiClass != null) {
                String qualifiedName = psiClass.getQualifiedName();
                if (qualifiedName != null && (qualifiedName.equals("java.util.List") || qualifiedName.equals("java.util.Set") || qualifiedName.equals("java.util.Collection"))) {
                    PsiType[] typeParameters = (PsiType[]) ((PsiClassType) psiType).resolve().getTypeParameters();
                    return typeParameters.length == 1;
                }
            }
        }
        return false;
    }
}
