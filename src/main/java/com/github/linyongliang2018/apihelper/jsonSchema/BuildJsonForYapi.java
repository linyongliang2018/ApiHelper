package com.github.linyongliang2018.apihelper.jsonSchema;

import com.github.linyongliang2018.apihelper.constant.JavaConstant;
import com.github.linyongliang2018.apihelper.constant.SpringMVCConstant;
import com.github.linyongliang2018.apihelper.pojo.ApiDto;
import com.github.linyongliang2018.apihelper.utils.AnnotationUtil;
import com.github.linyongliang2018.apihelper.utils.DesUtil;
import com.google.common.base.Strings;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.intellij.notification.*;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.impl.source.PsiClassReferenceType;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.util.PsiUtil;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static com.github.linyongliang2018.apihelper.utils.JsonUtils.getPojoJson;
import static com.github.linyongliang2018.apihelper.utils.JsonUtils.getRequest;

/**
 * @description: 为了yapi 创建的
 * @author: chengsheng@qbb6.com
 * @date: 2018/10/27
 */
public class BuildJsonForYapi {
    private static final NotificationGroup notificationGroup;

    static {
        notificationGroup = new NotificationGroup("Java2Json.NotificationGroup", NotificationDisplayType.BALLOON, true);
    }

    /**
     * @param psiMethodTarget 目标的方法
     * @param project         当前的项目
     * @return
     */
    public static ApiDto actionPerformed(PsiMethod psiMethodTarget, Project project) {
        ApiDto apiDto = new ApiDto();
        try {
            // 生成响应参数
            String response = getPojoJson(project, psiMethodTarget.getReturnType());
            apiDto.setResponse(response);
            String request = getRequest(project, psiMethodTarget);
            apiDto.setRequestBody(request);
            return apiDto;
        } catch (Exception ex) {
            Notification error = notificationGroup.createNotification("Convert to JSON failed.", NotificationType.ERROR);
            Notifications.Bus.notify(error, project);
        }
        return null;
    }



    /**
     * @description: 获得属性列表
     * @param: [psiClass, project, childType, index]
     * @return: com.qbb.build.KV
     * @author: chengsheng@qbb6.com
     * @date: 2019/5/15
     */
    public static LinkedHashMap<String, Object> getFields(PsiClass psiClass, Project project, String[] childType, Integer index, List<String> requiredList) {
        LinkedHashMap<String,Object> kv = new KV();
        if (psiClass != null) {
            if (Objects.nonNull(psiClass.getSuperClass()) && Objects.nonNull(NormalTypes.COLLECT_TYPES.get(psiClass.getSuperClass().getName()))) {
                for (PsiField field : psiClass.getFields()) {
                    //如果是有notnull 和 notEmpty 注解就加入必填
                    if (Objects.nonNull(AnnotationUtil.findAnnotation(field, JavaConstant.NOT_NULL))
                            || Objects.nonNull(AnnotationUtil.findAnnotation(field, JavaConstant.NOT_EMPTY))) {
                        requiredList.add(field.getName());
                    }
                    getField(field, project, kv, childType, index, psiClass.getName());
                }
            } else {
                if (NormalTypes.GENERIC_LIST.contains(psiClass.getName()) && childType != null && childType.length > index) {
                    String child = childType[index].split(">")[0];
                    PsiClass psiClassChild = JavaPsiFacade.getInstance(project).findClass(child, GlobalSearchScope.allScope(project));
                    return getFields(psiClassChild, project, childType, index + 1, requiredList);
                } else {
                    for (PsiField field : psiClass.getAllFields()) {
                        //如果是有notnull 和 notEmpty 注解就加入必填
                        if (Objects.nonNull(AnnotationUtil.findAnnotation(field, JavaConstant.NOT_NULL))
                                || Objects.nonNull(AnnotationUtil.findAnnotation(field, JavaConstant.NOT_EMPTY))) {
                            requiredList.add(field.getName());
                        }
                        getField(field, project, kv, childType, index, psiClass.getName());
                    }
                }
            }
        }
        return kv;
    }

    /**
     * @description: 获得单个属性
     * @param: [field, project, kv, childType, index, pName]
     * @return: void
     * @author: chengsheng@qbb6.com
     * @date: 2019/5/15
     */
    public static void getField(PsiField field, Project project, LinkedHashMap<String, Object> kv, String[] childType, Integer index, String pName) {
        if (field.getModifierList().hasModifierProperty("final")) {
            return;
        }
        PsiType type = field.getType();
        String name = field.getName();
        String remark = "";
        if (field.getDocComment() != null) {
            remark = DesUtil.getFiledDesc(field.getDocComment());
        }
        // 如果是基本类型
        if (type instanceof PsiPrimitiveType) {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("type", type.getPresentableText());
            if (!Strings.isNullOrEmpty(remark)) {
                jsonObject.addProperty("description", remark);
            }
            kv.put(name, jsonObject);
        } else {
            //reference Type
            String fieldTypeName = type.getPresentableText();
            //normal Type
            if (NormalTypes.isNormalType(fieldTypeName)) {
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("type", fieldTypeName);
                if (!Strings.isNullOrEmpty(remark)) {
                    jsonObject.addProperty("description", remark);
                }
                kv.put(name, jsonObject);
            } else if (!(type instanceof PsiArrayType) && ((PsiClassReferenceType) type).resolve().isEnum()) {
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("type", "enum");
                if (!Strings.isNullOrEmpty(remark)) {
                    jsonObject.addProperty("description", remark);
                }
                kv.put(name, jsonObject);
            } else if (NormalTypes.GENERIC_LIST.contains(fieldTypeName)) {
                if (childType != null) {
                    String child = childType[index].split(">")[0];
                    if (child.contains("java.util.List") || child.contains("java.util.Set") || child.contains("java.util.HashSet")) {
                        index = index + 1;
                        PsiClass psiClassChild = JavaPsiFacade.getInstance(project).findClass(childType[index].split(">")[0], GlobalSearchScope.allScope(project));
                        getCollect(kv, psiClassChild.getName(), remark, psiClassChild, project, name, pName, childType, index + 1);
                    } else {
                        //class type
                        LinkedHashMap<String, Object> kv1 = new KV();
                        kv1.putAll(new KV().set("type", "object"));
                        PsiClass psiClassChild = JavaPsiFacade.getInstance(project).findClass(child, GlobalSearchScope.allScope(project));
                        kv1.putAll(new KV().set("description", (Strings.isNullOrEmpty(remark) ? (psiClassChild.getName().trim()) : remark + " ," + psiClassChild.getName().trim())));
                        if (!pName.equals(psiClassChild.getName())) {
                            List<String> requiredList = new ArrayList<>();
                            kv1.putAll(new KV().set("properties", getFields(psiClassChild, project, childType, index + 1, requiredList)));
                            kv1.put("required", requiredList);
                        } else {
                            kv1.putAll(new KV().set("type", pName));
                        }
                        kv.put(name, kv1);
                    }
                }
            } else if (type instanceof PsiArrayType) {
                //array type
                PsiType deepType = type.getDeepComponentType();
                KV kvlist = new KV();
                String deepTypeName = deepType.getPresentableText();
                String cType = "";
                if (deepType instanceof PsiPrimitiveType) {
                    kvlist.put("type", type.getPresentableText());
                    if (!Strings.isNullOrEmpty(remark)) {
                        kvlist.put("description", remark);
                    }
                } else if (NormalTypes.isNormalType(deepTypeName)) {
                    kvlist.put("type", deepTypeName);
                    if (!Strings.isNullOrEmpty(remark)) {
                        kvlist.put("description", remark);
                    }
                } else {
                    kvlist.putAll(new KV().set("type", "object"));
                    PsiClass psiClass = PsiUtil.resolveClassInType(deepType);
                    cType = psiClass.getName();
                    kvlist.putAll(new KV().set("description", (Strings.isNullOrEmpty(remark) ? (psiClass.getName().trim()) : remark + " ," + psiClass.getName().trim())));
                    if (!pName.equals(PsiUtil.resolveClassInType(deepType).getName())) {
                        List<String> requiredList = new ArrayList<>();
                        kvlist.put("properties", getFields(psiClass, project, null, null, requiredList));
                        kvlist.put("required", requiredList);
                    } else {
                        kvlist.putAll(new KV().set("type", pName));
                    }
                }
                LinkedHashMap<String, Object> kv1 = new KV();
                kv1.putAll(new KV().set("type", "array"));
                kv1.putAll(new KV().set("description", (remark + " :" + cType).trim()));
                kv1.put("items", kvlist);
                kv.put(name, kv1);
            } else if (fieldTypeName.startsWith("List") || fieldTypeName.startsWith("Set") || fieldTypeName.startsWith("HashSet")) {
                //list type
                PsiType iterableType = PsiUtil.extractIterableTypeParameter(type, false);
                PsiClass iterableClass = PsiUtil.resolveClassInClassTypeOnly(iterableType);
                if (Objects.nonNull(iterableClass)) {
                    String classTypeName = iterableClass.getName();
                    getCollect(kv, classTypeName, remark, iterableClass, project, name, pName, childType, index);
                }
            } else if (fieldTypeName.startsWith("HashMap") || fieldTypeName.startsWith("Map") || fieldTypeName.startsWith("LinkedHashMap")) {
                //HashMap or Map
                CompletableFuture.runAsync(() -> {
                    try {
                        TimeUnit.MILLISECONDS.sleep(700);
                        Notification warning = notificationGroup.createNotification("Map Type Can not Change,So pass", NotificationType.WARNING);
                        Notifications.Bus.notify(warning, project);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                });
            } else {
                //class type
                KV kv1 = new KV();
                PsiClass psiClass = PsiUtil.resolveClassInType(type);
                kv1.putAll(new KV().set("type", "object"));
                kv1.putAll(new KV().set("description", (Strings.isNullOrEmpty(remark) ? (psiClass.getName().trim()) : (remark + " ," + psiClass.getName()).trim())));
                if (!pName.equals(((PsiClassReferenceType) type).getClassName())) {
                    List<String> requiredList = new ArrayList<>();
                    kv1.putAll(new KV().set("properties", getFields(PsiUtil.resolveClassInType(type), project, childType, index, requiredList)));
                    kv1.set("required", requiredList);
                } else {
                    kv1.putAll(new KV().set("type", pName));
                }
                kv.put(name, kv1);
            }
        }
    }

    /**
     * @description: 获得集合
     * @param: [kv, classTypeName, remark, psiClass, project, name, pName]
     * @return: void
     * @author: chengsheng@qbb6.com
     * @date: 2019/5/15
     */
    public static void getCollect(LinkedHashMap<String, Object> kv, String classTypeName, String remark, PsiClass psiClass, Project project, String name, String pName, String[] childType, Integer index) {
        LinkedHashMap<String, Object> kvlist = new KV();
        if (NormalTypes.isNormalType(classTypeName) || NormalTypes.COLLECT_TYPES.containsKey(classTypeName)) {
            kvlist.put("type", classTypeName);
            if (!Strings.isNullOrEmpty(remark)) {
                kvlist.put("description", remark);
            }
        } else {
            kvlist.putAll(new KV().set("type", "object"));
            kvlist.putAll(new KV().set("description", (Strings.isNullOrEmpty(remark) ? (psiClass.getName().trim()) : remark + " ," + psiClass.getName().trim())));
            if (!pName.equals(psiClass.getName())) {
                List<String> requiredList = new ArrayList<>();
                kvlist.put("properties", getFields(psiClass, project, childType, index, requiredList));
                kvlist.put("required", requiredList);
            } else {
                kvlist.putAll(new KV().set("type", pName));
            }
        }
        KV kv1 = new KV();
        kv1.putAll(new KV().set("type", "array"));
        kv1.putAll(new KV().set("description", (Strings.isNullOrEmpty(remark) ? (psiClass.getName().trim()) : remark + " ," + psiClass.getName().trim())));
        kv1.set("items", kvlist);
        kv.put(name, kv1);
    }


    /**
     * 批量生成 接口数据
     *
     * @param actionEvent
     * @return
     */
    public List<ApiDto> actionPerformedList(AnActionEvent actionEvent) {
        Editor editor = actionEvent.getDataContext().getData(CommonDataKeys.EDITOR);
        PsiFile psiFile = actionEvent.getDataContext().getData(CommonDataKeys.PSI_FILE);
        String selectedText = actionEvent.getRequiredData(CommonDataKeys.EDITOR).getSelectionModel().getSelectedText();
        Project project = editor.getProject();
        if (Strings.isNullOrEmpty(selectedText)) {
            Notification error = notificationGroup.createNotification("please select method or class", NotificationType.ERROR);
            Notifications.Bus.notify(error, project);
            return null;
        }
        // 获取当前光标位置的元素
        PsiElement referenceAt = psiFile.findElementAt(editor.getCaretModel().getOffset());
        PsiClass selectedClass = PsiTreeUtil.getContextOfType(referenceAt, PsiClass.class);
        List<ApiDto> apiDtos = new ArrayList<>();
        // 判断是否直接作用于整个controller类上面
        if (StringUtils.equals(selectedText, selectedClass.getName())) {
            // 获取光标选择的方法
            for (PsiMethod psiMethod : selectedClass.getMethods()) {
                // 过滤私有方法
                if (!psiMethod.getModifierList().hasModifierProperty(PsiModifier.PRIVATE)) {
                    ApiDto apiDto = actionPerformed(psiMethod, project);
                    apiDtos.add(apiDto);
                }
            }
        } else {
            // 找到特定的method
            for (PsiMethod psiMethod : selectedClass.getMethods()) {
                if (!psiMethod.getModifierList().hasModifierProperty(PsiModifier.PRIVATE)
                        && StringUtils.equals(psiMethod.getName(), selectedText)) {
                    ApiDto apiDto = actionPerformed(psiMethod, project);
                    apiDtos.add(apiDto);
                    break;
                }
            }
        }
        return apiDtos;
    }


    public void actionPerformed(AnActionEvent e) {
        // 获取当前项目和编辑器
        Project project = e.getProject();
        Editor editor = e.getRequiredData(CommonDataKeys.EDITOR);

        // 获取当前文件
        PsiFile psiFile = PsiDocumentManager.getInstance(project).getPsiFile(editor.getDocument());

        // 获取当前光标位置的元素
        int offset = editor.getCaretModel().getOffset();
        PsiElement elementAtOffset = psiFile.findElementAt(offset);

        // 从光标位置向上查找方法
        PsiMethod method = PsiTreeUtil.getParentOfType(elementAtOffset, PsiMethod.class);

        if (method != null) {
            boolean requestBodyAnnotationFound = false;

            // 遍历方法参数
            for (PsiParameter parameter : method.getParameterList().getParameters()) {
                // 检查参数是否使用了@RequestBody注解
                PsiAnnotation requestBodyAnnotation = parameter.getAnnotation("org.springframework.web.bind.annotation.RequestBody");
                if (requestBodyAnnotation != null) {
                    requestBodyAnnotationFound = true;
                    System.out.println("参数 '" + parameter.getName() + "' 使用了 @RequestBody 注解。");
                }
            }

            if (!requestBodyAnnotationFound) {
                System.out.println("在此方法中没有找到使用 @RequestBody 注解的参数。");
            }
        } else {
            System.out.println("当前光标位置找不到方法。");
        }
    }
}
