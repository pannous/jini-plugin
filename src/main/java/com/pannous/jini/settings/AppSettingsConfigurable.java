// Copyright 2000-2022 JetBrains s.r.o. and other contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package com.pannous.jini.settings;

import com.intellij.openapi.options.Configurable;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
 * Provides controller functionality for application settings.
 */
public class AppSettingsConfigurable implements Configurable {

    private AppSettingsComponent mySettingsComponent;
    private final String special_key = null;

    // A default constructor with no arguments is required because this implementation
    // is registered as an applicationConfigurable EP

    @Nls(capitalization = Nls.Capitalization.Title)
    @Override
    public String getDisplayName() {
        return "Jini OpenAI Chat-GPT Settings";
    }

    @Override
    public JComponent getPreferredFocusedComponent() {
        return mySettingsComponent.getPreferredFocusedComponent();
    }

    @Nullable
    @Override
    public JComponent createComponent() {
        mySettingsComponent = new AppSettingsComponent();
        return mySettingsComponent.getPanel();
    }

    @Override
    public boolean isModified() {
        AppSettingsState settings = AppSettingsState.getInstance();
        boolean modified = !mySettingsComponent.get_OPENAI_API_KEY().equals(settings.OPENAI_API_KEY);
        modified |= mySettingsComponent.isAutoPopup() != settings.autoPopup;
        modified |= mySettingsComponent.isAutoAddComments() != settings.autoAddComments;
        modified |= mySettingsComponent.isAutoReplaceCode() != settings.autoReplaceCode;
        modified |= mySettingsComponent.isAutoSaveToNewFile() != settings.autoSaveToNewFile;
        modified |= !mySettingsComponent.getCustomRefactor().equals(settings.customRefactor);
        return modified;
    }

    @Override
    public void apply() {
        AppSettingsState settings = AppSettingsState.getInstance();
        settings.OPENAI_API_KEY = mySettingsComponent.get_OPENAI_API_KEY();
        if (settings.OPENAI_API_KEY == null)
            settings.OPENAI_API_KEY = ApiKeys.OPENAI_API_KEY;
        settings.autoPopup = mySettingsComponent.isAutoPopup();
        settings.autoAddComments = mySettingsComponent.isAutoAddComments();
        settings.autoReplaceCode = mySettingsComponent.isAutoReplaceCode();
        settings.customRefactor = mySettingsComponent.getCustomRefactor();
        settings.autoSaveToNewFile = mySettingsComponent.isAutoSaveToNewFile();
    }

    @Override
    public void reset() {
        AppSettingsState settings = AppSettingsState.getInstance();
        if (settings.OPENAI_API_KEY == null)
            settings.OPENAI_API_KEY = special_key;
        if (settings.OPENAI_API_KEY == null)
            settings.OPENAI_API_KEY = System.getenv("OPENAI_API_KEY");
        mySettingsComponent.set_OPENAI_API_KEY(settings.OPENAI_API_KEY);
        mySettingsComponent.setAutoPopup(settings.autoPopup);
        mySettingsComponent.setAutoAddComments(settings.autoAddComments);
        mySettingsComponent.setAutoReplaceCode(settings.autoReplaceCode);
        mySettingsComponent.setCustomRefactor(settings.customRefactor);
        mySettingsComponent.setAutoSaveToNewFile(settings.autoSaveToNewFile);
    }

    @Override
    public void disposeUIResources() {
        mySettingsComponent = null;
    }

}
