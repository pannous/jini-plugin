// Copyright 2000-2022 JetBrains s.r.o. and other contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package com.pannous.jini.window;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.ui.SimpleToolWindowPanel;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.ui.components.JBScrollPane;
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
    private JScrollPane scrollPane;
//    private JBScrollPane scrollPane;
private JTextField input;
    private JTextPane result;


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
        panel.setLayout(new GridLayoutManager(2, 5, new Insets(0, 2, 0, 0), -1, -1));
        final JLabel label1 = new JLabel();
        label1.setText("Input");
        panel.add(label1, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        input = new JTextField();
        panel.add(input, new GridConstraints(1, 1, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, new Dimension(150, -1), null, null, 0, false));
        sendButton = new JButton();
        sendButton.setText("Submit");
        int shrink_or_grow = GridConstraints.SIZEPOLICY_CAN_GROW | GridConstraints.SIZEPOLICY_CAN_SHRINK;
        panel.add(sendButton, new GridConstraints(1, 3, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, shrink_or_grow, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        hideButton = new JButton();
        hideButton.setText("Hide");
        panel.add(hideButton, new GridConstraints(1, 4, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, shrink_or_grow, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));

        result = new JTextPane();
        result.setEditable(true);
//
//        JPanel clutch = new JPanel();
//        clutch.add(result, BorderLayout.CENTER);

        scrollPane = new JBScrollPane(result, JBScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JBScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setVisible(true);
//        scrollPane.setViewportView(result);

        Dimension dim = new Dimension(400, 400);
//        panel.add(scrollPane, new GridConstraints(0, 0, 1, 5, GridConstraints.ANCHOR_NORTHWEST, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, dim, null,null, 0, true));
        panel.add(scrollPane, new GridConstraints(0, 0, 1, 5, GridConstraints.ANCHOR_NORTHWEST, GridConstraints.FILL_BOTH, shrink_or_grow, shrink_or_grow, dim, null, null, 0, true));

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

    private void createUIComponents() {
        // TODO: place custom component creation code here
    }
}
