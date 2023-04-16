package com.github.linyongliang2018.apihelper.utils;

import com.google.gson.GsonBuilder;
import com.intellij.psi.*;
import com.intellij.psi.javadoc.PsiDocComment;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JsonSchemaGenerator {

    public static String generateJsonSchemaFromPsiClass(PsiClass psiClass) {
        Map<String, Object> schema = new LinkedHashMap<>();
        schema.put("type", "object");
        schema.put("title", psiClass.getName());
        String classDescription = PsiUtil.getClassDescription(psiClass);
        // 如果在类上面没有写javaDoc，那么就直接使用类名了
        if (StringUtils.isNotBlank(classDescription)) {
            String description = extractDescription(classDescription);
            schema.put("description", StringUtils.isNotBlank(description) ? description : psiClass.getName());
        } else {
            schema.put("description", psiClass.getName());
        }

        Map<String, Object> properties = new LinkedHashMap<>();
        schema.put("properties", properties);

        List<PsiField> allFields = new ArrayList<>(Arrays.asList(psiClass.getAllFields()));
        PsiClass superClass = psiClass.getSuperClass();
        while (superClass != null) {
            allFields.addAll(Arrays.asList(superClass.getAllFields()));
            superClass = superClass.getSuperClass();
        }

        for (PsiField field : allFields) {
            PsiDocComment docComment = field.getDocComment();
            PsiType fieldType = field.getType();
            Map<String, Object> fieldSchema = new LinkedHashMap<>();
            fieldSchema.put("type", PsiUtil.getJsonSchemaType(fieldType));

            // 原理也是使用正则替换掉多余的字符
            fieldSchema.put("description", docComment != null ? docComment.getText().replaceAll("\\n|/|\\*", "").trim() : "");

            if (PsiUtil.isFieldRequired(field)) {
                List<String> requiredList = (List<String>) schema.get("required");
                if (requiredList == null) {
                    requiredList = new ArrayList<>();
                    schema.put("required", requiredList);
                }
                requiredList.add(field.getName());
            }
            properties.put(field.getName(), fieldSchema);
        }

        return new GsonBuilder().setPrettyPrinting().create().toJson(schema);
    }


    /**
     * 这个是需要IDEA格式化过的代码，不然获取不到，原理是使用正则
     *
     * @param javadoc
     * @return
     */
    public static String extractDescription(String javadoc) {
        Pattern pattern = Pattern.compile("/\\*\\*\\s*\\n\\s*\\*(.*?)\\n", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(javadoc);
        if (matcher.find()) {
            return matcher.group(1).trim();
        }
        return null;
    }

    public static String generateMethodReturnValueJsonSchema(PsiMethod psiMethod) {
        if (psiMethod == null) {
            return null;
        }

        PsiType returnType = psiMethod.getReturnType();
        if (returnType == null) {
            return null;
        }

        // 使用递归处理嵌套泛型参数的 getJsonSchemaType 方法
        String jsonSchemaType = PsiUtil.getJsonSchemaType(returnType);
        if (jsonSchemaType == null) {
            return null;
        }

        Map<String, Object> schema = new LinkedHashMap<>();
        schema.put("type", jsonSchemaType);
        schema.put("title", psiMethod.getName() + "ReturnValue");

        PsiDocComment docComment = psiMethod.getDocComment();
        if (docComment != null) {
            String description = extractDescription(docComment.getText());
            schema.put("description", description);
        } else {
            schema.put("description", psiMethod.getName() + " return value");
        }

        return new GsonBuilder().setPrettyPrinting().create().toJson(schema);
    }

    public static String generateReturnValueJsonSchema(PsiMethod psiMethod) {
        if (psiMethod == null) {
            return null;
        }
        PsiType returnType = psiMethod.getReturnType();
        if (returnType == null) {
            return null;
        }
        // 使用递归处理嵌套泛型参数的 getJsonSchemaType 方法
        Map<String, Object> schema = generateJsonSchemaFromPsiType(returnType);
        return new GsonBuilder().setPrettyPrinting().create().toJson(schema);
    }

    public static Map<String, Object> generateJsonSchemaFromPsiType(PsiType psiType) {
        Map<String, Object> schema = new LinkedHashMap<>();
        if (psiType instanceof PsiClassType) {
            PsiClass psiClass = ((PsiClassType) psiType).resolve();
            schema.put("type", "object");
            schema.put("title", psiType.getPresentableText());
            String classDescription = PsiUtil.getClassDescription(psiClass);
            schema.put("description", StringUtils.isNotBlank(classDescription) ? classDescription : psiType.getPresentableText());

            Map<String, Object> properties = new LinkedHashMap<>();
            schema.put("properties", properties);

            for (PsiField field : psiClass.getAllFields()) {
                PsiType fieldType = field.getType();
                Map<String, Object> fieldSchema = new LinkedHashMap<>();
                if (fieldType instanceof PsiArrayType || fieldType instanceof PsiClassType) {
                    PsiType componentType = fieldType instanceof PsiArrayType ? ((PsiArrayType) fieldType).getComponentType() : fieldType;
                    if (PsiUtil.isGenericCollectionType(componentType)) {
                        fieldSchema.put("type", "array");
                        fieldSchema.put("items", generateJsonSchemaFromPsiType((PsiType) ((PsiClassType) componentType).resolve().getTypeParameters()[0]));
                    } else {
                        fieldSchema.putAll(generateJsonSchemaFromPsiType(componentType));
                    }
                } else {
                    fieldSchema.put("type", PsiUtil.getJsonSchemaType(fieldType));
                }

                fieldSchema.put("description", PsiUtil.getFieldDescription(field));
                if (PsiUtil.isFieldRequired(field)) {
                    List<String> required = (List<String>) schema.getOrDefault("required", new ArrayList<>());
                    required.add(field.getName());
                    schema.put("required", required);
                }
                properties.put(field.getName(), fieldSchema);
            }
        } else if (PsiUtil.isGenericCollectionType(psiType)) {
            schema.put("type", "array");
            PsiType elementType = (PsiType) ((PsiClassType) psiType).resolve().getTypeParameters()[0];
            schema.put("items", generateJsonSchemaFromPsiType(elementType));
        } else {
            schema.put("type", PsiUtil.getJsonSchemaType(psiType));
        }
        return schema;
    }

    /**
     * 获取对应的请求参数
     *
     * @param selectedMethod
     * @return
     */
    public String generateMethodParametersJsonSchema(PsiMethod selectedMethod) {
        PsiParameter[] parameters = selectedMethod.getParameterList().getParameters();
        if (parameters.length == 0) {
            return null;
        }

        for (PsiParameter parameter : parameters) {
            if (PsiUtil.isRequestBodyAnnotated(parameter)) {
                return generateJsonSchema(parameter.getType());
            }
        }
        return null;
    }

    public String generateJsonSchema(PsiType psiType) {
        PsiClass psiClass = PsiUtil.resolveClassFromPsiType(psiType);
        if (psiClass == null) {
            return null;
        }
        return generateJsonSchemaFromPsiClass(psiClass);
    }
}