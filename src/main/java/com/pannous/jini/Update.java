package com.pannous.jini;

import com.intellij.ide.plugins.IdeaPluginDescriptor;
import com.intellij.ide.plugins.IdeaPluginDescriptorImpl;
import com.intellij.ide.plugins.PluginInstaller;
import com.intellij.ide.plugins.PluginManager;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.extensions.PluginId;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.Optional;

public class Update extends AnAction {

    @Override
    public void actionPerformed(AnActionEvent e) {
        String jini_id = "com.pannous.jini-plugin";
        Path path = Path.of("/Users/me/dev/apps/jini-plugin/build/libs/");
        try {
            Optional<Path> mostRecentJar = Files.list(path)
                    .filter(p -> p.toString().endsWith(".jar"))
                    .max(Comparator.comparingLong(p -> p.toFile().lastModified()));
            path = mostRecentJar.get();
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
        PluginManager pluginManager = PluginManager.getInstance();
        IdeaPluginDescriptor jini = pluginManager.findEnabledPlugin(PluginId.getId(jini_id));
        IdeaPluginDescriptorImpl pluginDescriptor = (IdeaPluginDescriptorImpl) jini;
        pluginDescriptor.setEnabled(false);
        Path finalPath = path;
        PluginInstaller.installAndLoadDynamicPlugin(finalPath, pluginDescriptor);
        PluginManager.enablePlugin(pluginDescriptor.getPluginId().getIdString());
        Application application = ApplicationManager.getApplication();
        application.restart();
    }
}
