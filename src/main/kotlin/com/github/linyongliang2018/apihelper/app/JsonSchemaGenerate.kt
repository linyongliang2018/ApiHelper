package com.github.linyongliang2018.apihelper.app

import com.github.linyongliang2018.apihelper.jsonSchema.BuildJsonForYapi
import com.github.linyongliang2018.apihelper.pojo.JsonType
import com.intellij.notification.NotificationDisplayType
import com.intellij.notification.NotificationGroup
import com.intellij.notification.NotificationType
import com.intellij.notification.Notifications
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiMethod

/**
 * JsonSchema生成类
 *
 * 如果遇到有QA,那么就是在写代码的过程中问过了chatgpt，然后chatgpt给出的解答
 *
 */
class JsonSchemaGenerate {

    companion object {

        private val notificationGroup: NotificationGroup

        init {
            notificationGroup = NotificationGroup("Java2Json.NotificationGroup", NotificationDisplayType.BALLOON, true)
        }
    }

    /**
     * @param psiMethodTarget 目标的方法
     * @param project         当前的项目
     * @return
     */
    fun actionPerformed(psiMethodTarget: PsiMethod, project: Project): JsonType? {
        try {
            // 生成响应参数
            val response = BuildJsonForYapi.getPojoJson(project, psiMethodTarget.returnType)
            val request = BuildJsonForYapi.getRequest(project, psiMethodTarget)
            return JsonType(request, response)
        } catch (ex: Exception) {
            val error = notificationGroup.createNotification("生成JsonSchema失败了！", NotificationType.ERROR)
            Notifications.Bus.notify(error, project)
        }
        // Q:一定要返回null吗，有没有更好的写法
        // A:如果没有其他逻辑要处理，确实可以返回null。
        return null
    }
}
