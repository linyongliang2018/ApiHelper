package com.github.linyongliang2018.apihelper.jsonSchema

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiMethod
import com.intellij.psi.util.PsiTreeUtil

class RequestBodyAnnotationChecker : AnAction() {
    override fun actionPerformed(e: AnActionEvent) {
        // 获取当前项目和编辑器
        val project = e.project
        val editor = e.getRequiredData(CommonDataKeys.EDITOR)

        // 获取当前文件
        val psiFile = PsiDocumentManager.getInstance(project!!).getPsiFile(editor.document)

        // 获取当前光标位置的元素
        val offset = editor.caretModel.offset
        val elementAtOffset = psiFile?.findElementAt(offset)

        // 从光标位置向上查找方法
        val method = PsiTreeUtil.getParentOfType(elementAtOffset, PsiMethod::class.java)

        if (method != null) {
            var requestBodyAnnotationFound = false

            // 遍历方法参数
            for (parameter in method.parameterList.parameters) {
                // 检查参数是否使用了@RequestBody注解
                val requestBodyAnnotation =
                    parameter.getAnnotation("org.springframework.web.bind.annotation.RequestBody")
                if (requestBodyAnnotation != null) {
                    requestBodyAnnotationFound = true
                    println("参数 '${parameter.name}' 使用了 @RequestBody 注解。")
                }
            }

            if (!requestBodyAnnotationFound) {
                println("在此方法中没有找到使用 @RequestBody 注解的参数。")
            }
        } else {
            println("当前光标位置找不到方法。")
        }
    }
}
