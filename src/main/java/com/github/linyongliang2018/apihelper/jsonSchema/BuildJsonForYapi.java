package com.github.linyongliang2018.apihelper.jsonSchema;

import com.github.linyongliang2018.apihelper.constant.JavaConstant;
import com.github.linyongliang2018.apihelper.constant.SpringMVCConstant;
import com.github.linyongliang2018.apihelper.pojo.YapiApiDTO;
import com.github.linyongliang2018.apihelper.utils.DesUtil;
import com.github.linyongliang2018.apihelper.utils.AnnotationUtil;
import com.google.common.base.Strings;
import com.google.gson.JsonObject;
import com.intellij.notification.*;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.impl.compiled.ClsFileImpl;
import com.intellij.psi.impl.source.PsiClassReferenceType;
import com.intellij.psi.impl.source.PsiJavaFileImpl;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.util.PsiUtil;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.TimeUnit;

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
    public static YapiApiDTO actionPerformed(PsiMethod psiMethodTarget, Project project) {
        YapiApiDTO yapiApiDTO = new YapiApiDTO();
        try {
            // 生成响应参数
            String response = getPojoJson(project, psiMethodTarget.getReturnType());
            yapiApiDTO.setResponse(response);
            String request = getRequest(project, psiMethodTarget);
            yapiApiDTO.setRequestBody(request);
            return yapiApiDTO;
        } catch (Exception ex) {
            Notification error = notificationGroup.createNotification("Convert to JSON failed.", NotificationType.ERROR);
            Notifications.Bus.notify(error, project);
        }
        return null;
    }

    /**
     * @description: 获得请求参数
     * @param: [project, yapiApiDTO, psiMethodTarget]
     * @return: void
     * @author: chengsheng@qbb6.com
     * @date: 2019/2/19
     */
    public static String getRequest(Project project, PsiMethod psiMethodTarget) {
        PsiParameter[] psiParameters = psiMethodTarget.getParameterList().getParameters();
        for (PsiParameter psiParameter : psiParameters) {
            // 找到requestBody
            PsiAnnotation psiAnnotation = AnnotationUtil.findAnnotation(psiParameter, SpringMVCConstant.REQUEST_BODY);
            if (psiAnnotation != null) {
                return getPojoJson(project, psiParameter.getType());
            }
        }
        return null;
    }

    public static String getPojoJson(Project project, PsiType psiType) {
        if (psiType instanceof PsiPrimitiveType) {
            //如果是基本类型
            KV kvClass = KV.create();
            kvClass.set(psiType.getCanonicalText(), NormalTypes.NORMAL_TYPES.get(psiType.getPresentableText()));
        } else if (NormalTypes.isNormalType(psiType.getPresentableText())) {
            //如果是包装类型
            KV kvClass = KV.create();
            kvClass.set(psiType.getCanonicalText(), NormalTypes.NORMAL_TYPES.get(psiType.getPresentableText()));
        } else if (psiType.getPresentableText().startsWith("List")) {
            String[] types = psiType.getCanonicalText().split("<");
            KV listKv = new KV();
            if (types.length > 1) {
                String childPackage = types[1].split(">")[0];
                if (NormalTypes.NORMAL_TYPES_PACKAGES.containsKey(childPackage)) {
                    String[] childTypes = childPackage.split("\\.");
                    listKv.set("type", childTypes[childTypes.length - 1]);
                } else if (NormalTypes.COLLECT_TYPES_PACKAGES.containsKey(childPackage)) {
                    String[] childTypes = childPackage.split("\\.");
                    listKv.set("type", childTypes[childTypes.length - 1]);
                } else {
                    PsiClass psiClassChild = JavaPsiFacade.getInstance(project).findClass(childPackage, GlobalSearchScope.allScope(project));
                    List<String> requiredList = new ArrayList<>();
                    KV kvObject = getFields(psiClassChild, project, null, null, requiredList);
                    listKv.set("type", "object");
                    listKv.set("properties", kvObject);
                    listKv.set("required", requiredList);
                }
            }
            KV result = new KV();
            result.set("type", "array");
            result.set("title", psiType.getPresentableText());
            result.set("description", psiType.getPresentableText());
            result.set("items", listKv);
            String json = result.toPrettyJson();
            return json;
        } else if (psiType.getPresentableText().startsWith("Set")) {
            String[] types = psiType.getCanonicalText().split("<");
            KV listKv = new KV();
            if (types.length > 1) {
                String childPackage = types[1].split(">")[0];
                if (NormalTypes.NORMAL_TYPES_PACKAGES.containsKey(childPackage)) {
                    String[] childTypes = childPackage.split("\\.");
                    listKv.set("type", childTypes[childTypes.length - 1]);
                } else if (NormalTypes.COLLECT_TYPES_PACKAGES.containsKey(childPackage)) {
                    String[] childTypes = childPackage.split("\\.");
                    listKv.set("type", childTypes[childTypes.length - 1]);
                } else {
                    PsiClass psiClassChild = JavaPsiFacade.getInstance(project).findClass(childPackage, GlobalSearchScope.allScope(project));
                    List<String> requiredList = new ArrayList<>();
                    KV kvObject = getFields(psiClassChild, project, null, null, requiredList);
                    listKv.set("type", "object");
                    listKv.set("properties", kvObject);
                    listKv.set("required", requiredList);
                }
            }
            KV result = new KV();
            result.set("type", "array");
            result.set("title", psiType.getPresentableText());
            result.set("description", psiType.getPresentableText());
            result.set("items", listKv);
            String json = result.toPrettyJson();
            return json;
        } else if (psiType.getPresentableText().startsWith("Map")) {
            HashMap hashMapChild = new HashMap();
            String[] types = psiType.getCanonicalText().split("<");
            if (types.length > 1) {
                hashMapChild.put("paramMap", psiType.getPresentableText());
            }
            KV kvClass = KV.create();
            kvClass.set(types[0], hashMapChild);
            KV result = new KV();
            result.set("type", "object");
            result.set("title", psiType.getPresentableText());
            result.set("description", psiType.getPresentableText());
            result.set("properties", hashMapChild);
            String json = result.toPrettyJson();
            return json;
        } else if (NormalTypes.COLLECT_TYPES.containsKey(psiType.getPresentableText())) {
            //如果是集合类型
            KV kvClass = KV.create();
            kvClass.set(psiType.getCanonicalText(), NormalTypes.COLLECT_TYPES.get(psiType.getPresentableText()));
        } else {
            String[] types = psiType.getCanonicalText().split("<");
            if (types.length > 1) {
                PsiClass psiClassChild = JavaPsiFacade.getInstance(project).findClass(types[0], GlobalSearchScope.allScope(project));
                KV result = new KV();
                List<String> requiredList = new ArrayList<>();
                KV kvObject = getFields(psiClassChild, project, types, 1, requiredList);
                result.set("type", "object");
                result.set("title", psiType.getPresentableText());
                result.set("required", requiredList);
                result.set("description", (psiType.getPresentableText() + " :" + psiClassChild.getName()).trim());
                result.set("properties", kvObject);
                String json = result.toPrettyJson();
                return json;
            } else {
                PsiClass psiClassChild = JavaPsiFacade.getInstance(project).findClass(psiType.getCanonicalText(), GlobalSearchScope.allScope(project));
                KV result = new KV();
                List<String> requiredList = new ArrayList<>();
                KV kvObject = getFields(psiClassChild, project, null, null, requiredList);
                result.set("type", "object");
                result.set("required", requiredList);
                result.set("title", psiType.getPresentableText());
                result.set("description", (psiType.getPresentableText() + " :" + psiClassChild.getName()).trim());
                result.set("properties", kvObject);
                String json = result.toPrettyJson();
                return json;
            }
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
    public static KV getFields(PsiClass psiClass, Project project, String[] childType, Integer index, List<String> requiredList) {
        KV kv = KV.create();
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
    public static void getField(PsiField field, Project project, KV kv, String[] childType, Integer index, String pName) {
        if (field.getModifierList().hasModifierProperty("final")) {
            return;
        }
        PsiType type = field.getType();
        String name = field.getName();
        String remark = "";
        if (field.getDocComment() != null) {
            remark = DesUtil.getFiledDesc(field.getDocComment());
            //获得link 备注
            remark = DesUtil.getLinkRemark(remark, project, field);
        }
        // 如果是基本类型
        if (type instanceof PsiPrimitiveType) {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("type", type.getPresentableText());
            if (!Strings.isNullOrEmpty(remark)) {
                jsonObject.addProperty("description", remark);
            }
            kv.set(name, jsonObject);
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
                kv.set(name, jsonObject);
            } else if (!(type instanceof PsiArrayType) && ((PsiClassReferenceType) type).resolve().isEnum()) {
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("type", "enum");
                if (!Strings.isNullOrEmpty(remark)) {
                    jsonObject.addProperty("description", remark);
                }
                kv.set(name, jsonObject);
            } else if (NormalTypes.GENERIC_LIST.contains(fieldTypeName)) {
                if (childType != null) {
                    String child = childType[index].split(">")[0];
                    if (child.contains("java.util.List") || child.contains("java.util.Set") || child.contains("java.util.HashSet")) {
                        index = index + 1;
                        PsiClass psiClassChild = JavaPsiFacade.getInstance(project).findClass(childType[index].split(">")[0], GlobalSearchScope.allScope(project));
                        getCollect(kv, psiClassChild.getName(), remark, psiClassChild, project, name, pName, childType, index + 1);
                    } else {
                        //class type
                        KV kv1 = new KV();
                        kv1.set(KV.create().set("type", "object"));
                        PsiClass psiClassChild = JavaPsiFacade.getInstance(project).findClass(child, GlobalSearchScope.allScope(project));
                        kv1.set(KV.create().set("description", (Strings.isNullOrEmpty(remark) ? (psiClassChild.getName().trim()) : remark + " ," + psiClassChild.getName().trim())));
                        if (!pName.equals(psiClassChild.getName())) {
                            List<String> requiredList = new ArrayList<>();
                            kv1.set(KV.create().set("properties", getFields(psiClassChild, project, childType, index + 1, requiredList)));
                            kv1.set("required", requiredList);
                        } else {
                            kv1.set(KV.create().set("type", pName));
                        }
                        kv.set(name, kv1);
                    }
                }
                //    getField()
            } else if (type instanceof PsiArrayType) {
                //array type
                PsiType deepType = type.getDeepComponentType();
                KV kvlist = new KV();
                String deepTypeName = deepType.getPresentableText();
                String cType = "";
                if (deepType instanceof PsiPrimitiveType) {
                    kvlist.set("type", type.getPresentableText());
                    if (!Strings.isNullOrEmpty(remark)) {
                        kvlist.set("description", remark);
                    }
                } else if (NormalTypes.isNormalType(deepTypeName)) {
                    kvlist.set("type", deepTypeName);
                    if (!Strings.isNullOrEmpty(remark)) {
                        kvlist.set("description", remark);
                    }
                } else {
                    kvlist.set(KV.create().set("type", "object"));
                    PsiClass psiClass = PsiUtil.resolveClassInType(deepType);
                    cType = psiClass.getName();
                    kvlist.set(KV.create().set("description", (Strings.isNullOrEmpty(remark) ? (psiClass.getName().trim()) : remark + " ," + psiClass.getName().trim())));
                    if (!pName.equals(PsiUtil.resolveClassInType(deepType).getName())) {
                        List<String> requiredList = new ArrayList<>();
                        kvlist.set("properties", getFields(psiClass, project, null, null, requiredList));
                        kvlist.set("required", requiredList);
                    } else {
                        kvlist.set(KV.create().set("type", pName));
                    }
                }
                KV kv1 = new KV();
                kv1.set(KV.create().set("type", "array"));
                kv1.set(KV.create().set("description", (remark + " :" + cType).trim()));
                kv1.set("items", kvlist);
                kv.set(name, kv1);
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
                kv1.set(KV.create().set("type", "object"));
                kv1.set(KV.create().set("description", (Strings.isNullOrEmpty(remark) ? (psiClass.getName().trim()) : (remark + " ," + psiClass.getName()).trim())));
                if (!pName.equals(((PsiClassReferenceType) type).getClassName())) {
                    List<String> requiredList = new ArrayList<>();
                    kv1.set(KV.create().set("properties", getFields(PsiUtil.resolveClassInType(type), project, childType, index, requiredList)));
                    kv1.set("required", requiredList);
                } else {
                    kv1.set(KV.create().set("type", pName));
                }
                kv.set(name, kv1);
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
    public static void getCollect(KV kv, String classTypeName, String remark, PsiClass psiClass, Project project, String name, String pName, String[] childType, Integer index) {
        KV kvlist = new KV();
        if (NormalTypes.isNormalType(classTypeName) || NormalTypes.COLLECT_TYPES.containsKey(classTypeName)) {
            kvlist.set("type", classTypeName);
            if (!Strings.isNullOrEmpty(remark)) {
                kvlist.set("description", remark);
            }
        } else {
            kvlist.set(KV.create().set("type", "object"));
            kvlist.set(KV.create().set("description", (Strings.isNullOrEmpty(remark) ? (psiClass.getName().trim()) : remark + " ," + psiClass.getName().trim())));
            if (!pName.equals(psiClass.getName())) {
                List<String> requiredList = new ArrayList<>();
                kvlist.set("properties", getFields(psiClass, project, childType, index, requiredList));
                kvlist.set("required", requiredList);
            } else {
                kvlist.set(KV.create().set("type", pName));
            }
        }
        KV kv1 = new KV();
        kv1.set(KV.create().set("type", "array"));
        kv1.set(KV.create().set("description", (Strings.isNullOrEmpty(remark) ? (psiClass.getName().trim()) : remark + " ," + psiClass.getName().trim())));
        kv1.set("items", kvlist);
        kv.set(name, kv1);
    }


    /**
     * 批量生成 接口数据
     *
     * @param actionEvent
     * @return
     */
    public ArrayList<YapiApiDTO> actionPerformedList(AnActionEvent actionEvent) {
        Editor editor = actionEvent.getDataContext().getData(CommonDataKeys.EDITOR);
        PsiFile psiFile = actionEvent.getDataContext().getData(CommonDataKeys.PSI_FILE);
        String selectedText = actionEvent.getRequiredData(CommonDataKeys.EDITOR).getSelectionModel().getSelectedText();
        Project project = editor.getProject();
        if (Strings.isNullOrEmpty(selectedText)) {
            Notification error = notificationGroup.createNotification("please select method or class", NotificationType.ERROR);
            Notifications.Bus.notify(error, project);
            return null;
        }
        PsiElement referenceAt = psiFile.findElementAt(editor.getCaretModel().getOffset());
        PsiClass selectedClass = (PsiClass) PsiTreeUtil.getContextOfType(referenceAt, new Class[]{PsiClass.class});
        ArrayList<YapiApiDTO> yapiApiDTOS = new ArrayList<>();
        if (selectedText.equals(selectedClass.getName())) {
            PsiMethod[] psiMethods = selectedClass.getMethods();
            for (PsiMethod psiMethodTarget : psiMethods) {
                //去除私有方法
                if (!psiMethodTarget.getModifierList().hasModifierProperty("private")) {
                    YapiApiDTO yapiApiDTO = actionPerformed(psiMethodTarget, project);
                    yapiApiDTOS.add(yapiApiDTO);
                }
            }
        } else {
            PsiMethod[] psiMethods = selectedClass.getAllMethods();
            //寻找目标Method
            PsiMethod psiMethodTarget = null;
            for (PsiMethod psiMethod : psiMethods) {
                if (psiMethod.getName().equals(selectedText)) {
                    psiMethodTarget = psiMethod;
                    break;
                }
            }
            if (Objects.nonNull(psiMethodTarget)) {
                YapiApiDTO yapiApiDTO = actionPerformed(psiMethodTarget, project);
                yapiApiDTOS.add(yapiApiDTO);
            } else {
                Notification error = notificationGroup.createNotification("can not find method:" + selectedText, NotificationType.ERROR);
                Notifications.Bus.notify(error, project);
                return null;
            }
        }
        return yapiApiDTOS;
    }

}
