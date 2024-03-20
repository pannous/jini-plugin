package com.pannous.jini;

import com.intellij.ide.plugins.IdeaPluginDescriptor;
import com.intellij.ide.plugins.IdeaPluginDescriptorImpl;
import com.intellij.ide.plugins.PluginInstaller;
import com.intellij.ide.plugins.PluginManager;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.extensions.PluginId;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Comparator;
import java.util.Optional;

public class Update extends AnAction {
//    PsiElementBaseIntentionAction

    @Override
    public void actionPerformed(AnActionEvent e) {
        String jini_id = "com.pannous.jini-plugin";
        Path path = downloadLatestJar();
        System.err.println("Updating Jini plugin to " + path);
        PluginManager pluginManager = PluginManager.getInstance();
        IdeaPluginDescriptor jini = pluginManager.findEnabledPlugin(PluginId.getId(jini_id));
        IdeaPluginDescriptorImpl pluginDescriptor = (IdeaPluginDescriptorImpl) jini;
        try {
            pluginDescriptor.setEnabled(false);
        } catch (Exception ex) {
            System.err.println("Update.actionPerformed: " + ex);
        }

        PluginManager.disablePlugin(jini_id);

        try {
            PluginInstaller.installAndLoadDynamicPlugin(path, pluginDescriptor);
        } catch (Exception ex) {
//        this HARD method works but is not (always?) necessary:
            PluginInstaller.uninstallDynamicPlugin(null, pluginDescriptor, true);
            PluginInstaller.installAndLoadDynamicPlugin(path, pluginDescriptor);
        }
        try {
            PluginManager.enablePlugin(pluginDescriptor.getPluginId().getIdString());
        } catch (Exception ex) {
        }

//        this HARD method works but is not (always?) necessary:
        Application application = ApplicationManager.getApplication();
        application.restart();
    }

    private Path downloadLatestJar() {
        try {
            String fileUrl = "http://pannous.com/files/intelliJini-plugin-latest.jar";
            URL url = new URL(fileUrl);
            String fileName = url.getFile();
            String saveDir = ".";
            Path savePath = Paths.get(saveDir, fileName);
            Files.copy(url.openStream(), savePath, StandardCopyOption.REPLACE_EXISTING);
            return savePath;
        } catch (MalformedURLException ex) {
            throw new RuntimeException(ex);
        } catch (IOException ex) {
            return getLatestLocalJar();
//            throw new RuntimeException(ex);
        }
    }

    private Path getLatestLocalJar() {
        Path path = Path.of("/Users/me/dev/apps/jini-plugin/build/libs/");
        try {
            Optional<Path> mostRecentJar = Files.list(path)
                    .filter(p -> p.toString().endsWith(".jar"))
                    .max(Comparator.comparingLong(p -> p.toFile().lastModified()));
            path = mostRecentJar.get();
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
        return path;
    }
}
