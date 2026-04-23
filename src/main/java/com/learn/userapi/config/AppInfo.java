package com.learn.userapi.config;

public class AppInfo {

    private final String environment;
    private final String version;
    private final boolean seedDataEnabled;

    public AppInfo(String environment, String version, boolean seedDataEnabled) {
        this.environment = environment;
        this.version = version;
        this.seedDataEnabled = seedDataEnabled;
    }

    public String getEnvironment() {
        return environment;
    }

    public String getVersion() {
        return version;
    }

    public boolean isSeedDataEnabled() {
        return seedDataEnabled;
    }

    @Override
    public String toString() {
        return "AppInfo{" +
                "environment='" + environment + '\'' +
                ", version='" + version + '\'' +
                ", seedDataEnabled=" + seedDataEnabled +
                '}';
    }
}
