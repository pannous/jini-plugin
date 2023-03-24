// Copyright 2000-2022 JetBrains s.r.o. and other contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package com.pannous.jini.settings;

import com.intellij.ui.components.JBCheckBox;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBPasswordField;
import com.intellij.ui.components.JBTextField;
import com.intellij.util.ui.FormBuilder;

import javax.swing.*;

/**
 * Supports creating and managing a {@link JPanel} for the Settings Dialog.
 */
public class AppSettingsComponent {

    private final JPanel myMainPanel;
    private final JBPasswordField OPENAI_API_KEY = new JBPasswordField();
    private final JLabel getApiKey = new JLabel("<p><a href='https://platform.openai.com/account/api-keys'>Get API key</a></p>");
    private final JLabel apiStatusLink = new JLabel("<p><a href='https://status.openai.com/'>API status</a></p>");

    private final JBCheckBox autoPopup = new JBCheckBox("Show popup of OpenAI answers");
    private final JBCheckBox autoAddComments = new JBCheckBox("Allow adding comments to the code from OpenAI");
    private final JBCheckBox autoReplaceCode = new JBCheckBox("Allow auto-replace code with OpenAI suggestions");
    private final JBCheckBox autoSaveToNewFile = new JBCheckBox("Write transformed code to new file");
    private final JBTextField customRefactor = new JBTextField("Custom command to refactor selected code");
//    private final JBTextField targetLanguage = new JBTextField("Target language of transpilation code");
//    private final JBCheckBox autoExecuteCommands = new JBCheckBox("⚠️ Automatically execute commands from OpenAI");

    public AppSettingsComponent() {
        myMainPanel = FormBuilder.createFormBuilder()
                .addLabeledComponent(new JBLabel("OPENAI_API_KEY"), OPENAI_API_KEY, 1, false)
//                .addComponent(getApiKey, 1)
//                .addComponent(apiStatusLink, 1)
                .addSeparator()
                .addComponent(autoPopup, 1)
                .addComponent(autoAddComments, 1)
                .addComponent(autoReplaceCode, 1)
                .addComponent(autoSaveToNewFile, 1)
                .addSeparator()
                .addLabeledComponent(new JBLabel("Custom command"), customRefactor, 1, false)
//                .addComponent(autoExecuteCommands, 1)
                .addComponentFillVertically(new JPanel(), 0)
                .getPanel();
    }

    public JPanel getPanel() {
        return myMainPanel;
    }

    public JComponent getPreferredFocusedComponent() {
        return OPENAI_API_KEY;
    }

    public String get_OPENAI_API_KEY() {
        return new String(OPENAI_API_KEY.getPassword());
    }

    public void set_OPENAI_API_KEY(String openai_api_key) {
        OPENAI_API_KEY.setText(openai_api_key);
    }

    public boolean isAutoPopup() {
        return autoPopup.isSelected();
    }

    public void setAutoPopup(boolean isChecked) {
        autoPopup.setSelected(isChecked);
    }

    public boolean isAutoAddComments() {
        return autoAddComments.isSelected();
    }

    public void setAutoAddComments(boolean isChecked) {
        autoAddComments.setSelected(isChecked);
    }

    public boolean isAutoReplaceCode() {
        return autoReplaceCode.isSelected();
    }

    public void setAutoReplaceCode(boolean isChecked) {
        autoReplaceCode.setSelected(isChecked);
    }

    public String getCustomRefactor() {
        return customRefactor.getText();
    }

    public void setCustomRefactor(String customRefactor) {
        this.customRefactor.setText(customRefactor);
    }

    public boolean isAutoSaveToNewFile() {
        return autoSaveToNewFile.isSelected();
    }

    public void setAutoSaveToNewFile(boolean isChecked) {
        autoSaveToNewFile.setSelected(isChecked);
    }
}
