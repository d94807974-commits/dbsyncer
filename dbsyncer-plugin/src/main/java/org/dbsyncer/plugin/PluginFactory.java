/**
 * DBSyncer Copyright 2020-2024 All Rights Reserved.
 */
package org.dbsyncer.plugin;

import org.dbsyncer.common.util.CollectionUtils;
import org.dbsyncer.common.util.StringUtil;
import org.dbsyncer.plugin.enums.ProcessEnum;
import org.dbsyncer.sdk.model.Plugin;
import org.dbsyncer.sdk.model.PluginFile;
import org.dbsyncer.sdk.plugin.PluginContext;
import org.dbsyncer.sdk.spi.PluginService;

import org.apache.commons.io.FileUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * @author AE86
 * @version 1.0.0
 * @date 2019/10/1 13:26
 */
@Component
public class PluginFactory implements DisposableBean {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * PluginPathdbsyncer/plugin/
     */
    private final String PLUGIN_PATH = System.getProperty("user.dir") + File.separatorChar + "plugins" + File.separatorChar;

    /**
     * Reliance Pathdbsyncer/lib/
     */
    private final String LIBRARY_PATH = System.getProperty("user.dir") + File.separatorChar + "lib" + File.separatorChar;

    private final List<Plugin> plugins = new LinkedList<>();

    private final Map<String, PluginService> service = new ConcurrentHashMap<>();

    /**
     * Upload JAR CorrespondingClassLoader（PressFilename）。
     */
    private final Map<String, URLClassLoader> classLoaders = new ConcurrentHashMap<>();

    /**
     * Loading temporary copy(s)key=OriginalFilename）。ClassLoader Only the copy is open.Windows Downplugins OriginalDocumentationDeleted/Overwrite。
     */
    private final Map<String, File> loadCopies = new ConcurrentHashMap<>();

    @Resource
    private ApplicationContext applicationContext;

    @PostConstruct
    private void init() {
        Map<String, PluginService> services = applicationContext.getBeansOfType(PluginService.class);
        if (!CollectionUtils.isEmpty(services)) {
            services.forEach((k, s) -> {
                String pluginId = createPluginId(s.getClass().getName(), s.getVersion());
                service.putIfAbsent(pluginId, s);
                plugins.add(new Plugin(s.getName(), s.getClass().getName(), s.getVersion(), "", true));
                logger.info("{}_{} {}", s.getName(), s.getVersion(), s.getClass().getName());
                try {
                    s.init();
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                }
            });
        }
    }

    public synchronized void loadPlugins() {
        // Release loaded first.UploadPlugin，AvoidWindows DownJAR DocumentationLock causedNoneFrenchOverwrite/Delete
        unloadAllUploaded();
        if (!CollectionUtils.isEmpty(plugins)) {
            List<Plugin> unmodifiablePlugin = plugins.stream().filter(Plugin::isUnmodifiable).collect(Collectors.toList());
            plugins.clear();
            plugins.addAll(unmodifiablePlugin);
        }
        try {
            FileUtils.forceMkdir(new File(PLUGIN_PATH));
        } catch (IOException e) {
            logger.error(e.getMessage());
        }
        Collection<File> files = FileUtils.listFiles(new File(PLUGIN_PATH), new String[]{"jar"}, true);
        if (!CollectionUtils.isEmpty(files)) {
            files.forEach(this::loadPlugin);
        }
        logger.info("PreLoad plugin:{}", plugins.size());
    }

    public String getPluginPath() {
        return PLUGIN_PATH;
    }

    public String getLibraryPath() {
        return LIBRARY_PATH;
    }

    public List<Plugin> getPluginAll() {
        return Collections.unmodifiableList(plugins);
    }

    /**
     * Unmount onlyMemoryMediumUploadPlugin（OffSPI + ClassLoader），No, no, no.Documentation。OverwriteUploadCall before.
     *
     * @param fileName PluginFilename
     */
    public synchronized void unloadPlugin(String fileName) {
        Assert.hasText(fileName, "Plugin filename cannot be empty");
        if (!PluginFile.isSafeName(fileName)) {
            throw new IllegalArgumentException("Invalid plugin filename; only letters, digits, dots, underscores and hyphens are allowed");
        }
        unloadJar(fileName);
    }

    /**
     * DeleteUploadYes.Plugin：Unmount firstMemory（SPI + ClassLoader + temporary copy, delete{@code plugins/} OriginalDocumentation。
     * <p>Delete onlyDiskDocumentationNot enough.——RunningUrbanYesTemporary copy, no unloaded ruleList/ConvertIt's still possible to hit old cases.
     *
     * @param fileName PluginFilename（It Given the Lawjar First name)
     */
    public synchronized void deletePlugin(String fileName) {
        Assert.hasText(fileName, "Plugin filename cannot be empty");
        if (!PluginFile.isSafeName(fileName)) {
            throw new IllegalArgumentException("Invalid plugin filename; only letters, digits, dots, underscores and hyphens are allowed");
        }
        File file = new File(PLUGIN_PATH, fileName);
        boolean known = classLoaders.containsKey(fileName) || loadCopies.containsKey(fileName);
        for (Plugin plugin : plugins) {
            if (StringUtil.equals(fileName, plugin.getFileName())) {
                known = true;
                if (plugin.isUnmodifiable()) {
                    throw new IllegalArgumentException("Internal plugin cannot be deleted");
                }
            }
        }
        if (!file.exists() && !known) {
            throw new IllegalArgumentException("Plugin file does not exist: " + fileName);
        }
        // 1) Unload loaded instance (including temporary copy)ClassLoader）
        unloadJar(fileName);
        // 2) Again.plugins Table of contentsDocumentation（OriginalDocumentationNotClassLoader OpenWindows Deleted)
        if (file.exists()) {
            deleteFileWithRetry(file);
        }
        logger.info("Unmounted and deleted plugin: {", fileName);
    }

    /**
     * Full sync/Incremental syncProcessing
     *
     * @param context
     */
    public void process(PluginContext context, ProcessEnum processEnum) {
        Plugin plugin = context.getPlugin();
        if (null != plugin) {
            String pluginId = createPluginId(plugin.getClassName(), plugin.getVersion());
            service.computeIfPresent(pluginId, (k, c) -> {
                switch (processEnum) {
                    case BEFORE:
                        c.postProcessBefore(context);
                        break;
                    case CONVERT:
                        c.convert(context);
                        break;
                    case AFTER:
                        c.postProcessAfter(context);
                        break;
                }
                return c;
            });
        }
    }

    public String createPluginId(String pluginClassName, String pluginVersion) {
        return pluginClassName + "_" + pluginVersion;
    }

    /**
     * SPI：Scanjar Extension achieved.
     * <p>A temporary copy must be loaded:Windows Let's go.{@link URLClassLoader} It'll lock.jar，
     * If directly loaded{@code plugins/} OriginalDocumentation，Oh, no.StopProcessNoneManual/InterfaceDelete。
     *
     * @param jar plugins Original under DirectoryDocumentation
     */
    private void loadPlugin(File jar) {
        String fileName = jar.getName();
        List<String> addedIds = new ArrayList<>();
        List<Plugin> addedPlugins = new ArrayList<>();
        List<PluginService> addedServices = new ArrayList<>();
        List<PluginService> replacedServices = new ArrayList<>();
        List<PluginService> replacedToClose = new ArrayList<>();
        try {
            unloadJar(fileName);
            File copy = File.createTempFile("dbs-plugin-", "-" + fileName);
            copy.deleteOnExit();
            FileUtils.copyFile(jar, copy);
            loadCopies.put(fileName, copy);
            URLClassLoader loader = new URLClassLoader(new URL[]{copy.toURI().toURL()},
                    Thread.currentThread().getContextClassLoader());
            classLoaders.put(fileName, loader);
            ServiceLoader<PluginService> services = ServiceLoader.load(PluginService.class, loader);
            boolean loaded = false;
            for (PluginService s : services) {
                String pluginId = createPluginId(s.getClass().getName(), s.getVersion());
                Plugin plugin = new Plugin(s.getName(), s.getClass().getName(), s.getVersion(), fileName, false);
                PluginService previous = service.put(pluginId, s);
                plugins.add(plugin);
                addedIds.add(pluginId);
                addedPlugins.add(plugin);
                addedServices.add(s);
                replacedServices.add(previous);
                logger.info("{}, {}_{} {}", fileName, s.getName(), s.getVersion(), s.getClass().getName());
                s.init();
                if (previous != null) {
                    replacedToClose.add(previous);
                }
                loaded = true;
            }
            for (PluginService previous : replacedToClose) {
                try {
                    previous.close();
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                }
            }
            if (!loaded) {
                closeClassLoader(fileName);
                logger.warn("JAR did not find PluginService", fileName);
            }
        } catch (Throwable e) {
            rollbackLoaded(addedIds, addedPlugins, addedServices, replacedServices);
            closeClassLoader(fileName);
            logger.error(e.getMessage(), e);
        }
    }

    /**
     * Load MidwayFailed：Remove this registration and restore it.OverwriteAn example, at this timeClassLoader Still on.
     */
    private void rollbackLoaded(List<String> addedIds, List<Plugin> addedPlugins, List<PluginService> addedServices,
                                List<PluginService> replacedServices) {
        for (int i = addedIds.size() - 1; i >= 0; i--) {
            plugins.remove(addedPlugins.get(i));
            String pluginId = addedIds.get(i);
            PluginService current = addedServices.get(i);
            PluginService previous = replacedServices.get(i);
            if (service.get(pluginId) == current) {
                if (previous != null) {
                    service.put(pluginId, previous);
                } else {
                    service.remove(pluginId);
                }
            }
            try {
                current.close();
            } catch (Exception ex) {
                logger.error(ex.getMessage(), ex);
            }
        }
    }

    private void unloadAllUploaded() {
        Set<String> fileNames = new HashSet<>(classLoaders.keySet());
        for (Plugin plugin : plugins) {
            if (!plugin.isUnmodifiable() && StringUtil.isNotBlank(plugin.getFileName())) {
                fileNames.add(plugin.getFileName());
            }
        }
        for (String fileName : fileNames) {
            unloadJar(fileName);
        }
    }

    private void unloadJar(String fileName) {
        List<Plugin> matched = new ArrayList<>();
        for (Plugin plugin : plugins) {
            if (StringUtil.equals(fileName, plugin.getFileName())) {
                matched.add(plugin);
            }
        }
        for (Plugin plugin : matched) {
            String pluginId = createPluginId(plugin.getClassName(), plugin.getVersion());
            PluginService pluginService = service.remove(pluginId);
            if (pluginService != null) {
                try {
                    pluginService.close();
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                }
            }
            plugins.remove(plugin);
        }
        closeClassLoader(fileName);
    }

    private void closeClassLoader(String fileName) {
        URLClassLoader loader = classLoaders.remove(fileName);
        if (loader != null) {
            try {
                loader.close();
            } catch (IOException e) {
                logger.warn("ClassLoader failed, file=}, err={", fileName, e.getMessage());
            }
        }
        File copy = loadCopies.remove(fileName);
        if (copy == null || !copy.exists()) {
            return;
        }
        for (int i = 0; i < 3; i++) {
            if (copy.delete() || !copy.exists()) {
                return;
            }
            try {
                Thread.sleep(50L);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        if (copy.exists()) {
            copy.deleteOnExit();
            logger.warn("A temporary copy of the plugin is not deleted. A registered JVM is cleared on exit: {", copy.getAbsolutePath());
        }
    }

    /**
     * Delete plugins OriginalDocumentation。If still occupied, change of name from the directory (no longer scanned).
     */
    private void deleteFileWithRetry(File file) {
        final int attempts = 3;
        for (int i = 0; i < attempts; i++) {
            if (!file.exists()) {
                return;
            }
            if (file.delete()) {
                return;
            }
            try {
                Thread.sleep(50L);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        if (!file.exists()) {
            return;
        }
        File tombstone = new File(file.getParentFile(), file.getName() + ".deleted." + System.currentTimeMillis());
        if (file.renameTo(tombstone)) {
            tombstone.deleteOnExit();
            logger.warn("Plugin files are still occupied and renamed out of plugin directory: {", tombstone.getName());
            return;
        }
        throw new IllegalStateException("Plugin file deletion failed (possibly still occupied): " + file.getName());
    }

    @Override
    public void destroy() {
        unloadAllUploaded();
        service.values().forEach(s -> {
            logger.info("{}_{} {}", s.getName(), s.getVersion(), s.getClass().getName());
            try {
                s.close();
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        });
        service.clear();
    }

}
