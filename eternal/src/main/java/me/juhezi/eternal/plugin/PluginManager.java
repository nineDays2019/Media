package me.juhezi.eternal.plugin;

import android.util.Log;

import me.juhezi.eternal.sdk.demo.TestInterface;

public class PluginManager {

    private static final String PLUGIN_NAME = "plugin.apk";

    private static final String TAG = "PluginManager";

    private PluginLoader mPluginLoader;

    private static final class PluginManagerHolder {
        private static final PluginManager INSTANCE = new PluginManager();
    }

    public static PluginManager getInstance() {
        return PluginManagerHolder.INSTANCE;
    }

    public void loadPluginApk() {
        if (mPluginLoader == null) {
            mPluginLoader = new PluginLoader();
            mPluginLoader.loadPluginApk(PLUGIN_NAME);
        }
    }

    /**
     * 创建一个TestUtil插件
     */
    public TestInterface createTestPluginInstance() {
        if (mPluginLoader == null) {
            Log.e(TAG, "The plugin loader is null");
            return null;
        }

        TestInterface testManager = (TestInterface) mPluginLoader.newInstance("me.juhezi.eternal.plugin.demo.TestUtil");
        return testManager;
    }

}