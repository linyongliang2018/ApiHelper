package com.github.linyongliang2018.apihelper.action;

import com.github.linyongliang2018.apihelper.utils.JsonSchemaGenerator;
import com.github.linyongliang2018.apihelper.utils.PsiUtil;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.ui.Messages;
import com.intellij.psi.PsiMethod;

public class JsonSchemaAction extends AnAction {

    @Override
    public void actionPerformed(AnActionEvent actionEvent) {
        Editor editor = actionEvent.getDataContext().getData(CommonDataKeys.EDITOR);
        PsiMethod selectedMethod = PsiUtil.findSelectedPsiMethod(actionEvent.getProject(),editor);
        if (selectedMethod == null) {
            Messages.showMessageDialog("Please select a method to generate JSON schema", "Information", Messages.getInformationIcon());
            return;
        }

        JsonSchemaGenerator generator = new JsonSchemaGenerator();

        // Generating method parameters JSON schema
        /*String methodParametersJsonSchema = generator.generateMethodParametersJsonSchema(selectedMethod);
        if (methodParametersJsonSchema != null) {
            // Show or process the method parameters JSON schema
            Messages.showMessageDialog(methodParametersJsonSchema, "Method Parameters JSON Schema", Messages.getInformationIcon());
        }*/

        // Generating method return value JSON schema
        String methodReturnValueJsonSchema = JsonSchemaGenerator.generateReturnValueJsonSchema(selectedMethod);
        if (methodReturnValueJsonSchema != null) {
            // Show or process the method return value JSON schema
            Messages.showMessageDialog(methodReturnValueJsonSchema, "Method Return Value JSON Schema", Messages.getInformationIcon());
        }
    }
}
