package org.dbsyncer.sdk.model;

/**
 * @author AE86
 * @version 1.0.0
 * @date 2019/10/1 13:59
 */
public class Plugin {

    // PluginName
    private String name;

    // PluginAchieved
    private String className;

    // VersionNo. No.
    private String version;

    // JarName
    private String fileName;

    // YesNoSystem preset
    private boolean unmodifiable;

    public Plugin() {
    }

    public Plugin(String name, String className, String version, String fileName, boolean unmodifiable) {
        this.name = name;
        this.className = className;
        this.version = version;
        this.fileName = fileName;
        this.unmodifiable = unmodifiable;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getClassName() {
        return className;
    }

    public Plugin setClassName(String className) {
        this.className = className;
        return this;
    }

    public String getVersion() {
        return version;
    }

    public Plugin setVersion(String version) {
        this.version = version;
        return this;
    }

    public String getFileName() {
        return fileName;
    }

    public Plugin setFileName(String fileName) {
        this.fileName = fileName;
        return this;
    }

    public boolean isUnmodifiable() {
        return unmodifiable;
    }

    public void setUnmodifiable(boolean unmodifiable) {
        this.unmodifiable = unmodifiable;
    }
}
