package cn.imldy.mirai.console.plugin;

import net.mamoe.mirai.console.plugin.jvm.JavaPlugin;
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescriptionBuilder;

public final class FileForward extends JavaPlugin {
    public static final FileForward INSTANCE = new FileForward();

    private FileForward() {
        super(new JvmPluginDescriptionBuilder("cn.imldy.mirai.console.plugin.FileForward", "1.0-SNAPSHOT").build());
    }

    @Override
    public void onEnable() {
        getLogger().info("Plugin loaded!");
    }
}