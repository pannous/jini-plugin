// Copyright 2000-2022 JetBrains s.r.o. and other contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package com.pannous.jini.window;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.ui.SimpleToolWindowPanel;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.util.messages.MessageBus;
import com.pannous.jini.openai.OpenAI;
import com.pannous.jini.openai.Prompt;
import com.pannous.jini.settings.Options;

import javax.swing.*;
import java.awt.*;
import java.util.function.Consumer;

//extends , TransferHandler
public class MyToolWindow extends SimpleToolWindowPanel implements JiniListener, Disposable {

    private JButton sendButton;
    private JButton hideButton;
    private JPanel panel;
    private JTextField input;
    private JTextArea result;


    @Override
    public void onMessageReceived(String message) {
        addResponse(message);
    }

    void registerListener() {
        Project project = ProjectManager.getInstance().getDefaultProject();
        MessageBus bus = project.getMessageBus();
        bus.connect().subscribe(JiniListener.TOPIC, this);
    }

    public MyToolWindow(ToolWindow toolWindow) {
        super(true, true);
//        setContent(panel);
        if (panel == null)
            $$$setupUI$$$();

        panel.setTransferHandler(new JiniTransferHandler(this));
        hideButton.addActionListener(e -> toolWindow.hide(null));
        sendButton.addActionListener(e -> submit());
        input.addActionListener(e -> submit());
        registerListener();
    }

    // this should be auto generated!?!
    private void $$$setupUI$$$() {
        panel = new JPanel();
        panel.setLayout(new GridLayoutManager(2, 5, new Insets(0, 0, 0, 0), -1, -1));
        final JLabel label1 = new JLabel();
        label1.setText("Input");
        panel.add(label1, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        input = new JTextField();
        panel.add(input, new GridConstraints(1, 1, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, new Dimension(150, -1), null, null, 0, false));
        sendButton = new JButton();
        sendButton.setText("Submit");
        panel.add(sendButton, new GridConstraints(1, 3, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        hideButton = new JButton();
        hideButton.setText("Hide");
        panel.add(hideButton, new GridConstraints(1, 4, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));

        result = new JTextArea();
        result.setWrapStyleWord(true);
//        result.wrappingStyle = JTextPane.WRAPPING_STYLE_WORD;
        panel.add(result, new GridConstraints(0, 0, 1, 5, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_WANT_GROW, new Dimension(150, 50), null, null, 0, false));

    }

    public void submit() {
        String text = input.getText();
        result.setText(result.getText() + "\nUSER: " + text);
        input.setText("… thinking …");
        Consumer<String> callback = this::addResponse;
        Project project = ProjectManager.getInstance().getDefaultProject();
        Prompt prompt = Prompt.CHAT;
        OpenAI.query(project, prompt, text, null, callback, Options.none);
    }

    public JPanel getContent() {
        return panel;
    }

    public void addResponse(String text) {
        ApplicationManager.getApplication().invokeLater(() -> {
            result.setText(result.getText() + "\nAI: " + text);
            input.setText("");
        });
    }

    @Override
    public void dispose() {
    }
}
