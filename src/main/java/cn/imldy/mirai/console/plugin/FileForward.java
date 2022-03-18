package cn.imldy.mirai.console.plugin;

import cn.imldy.mirai.console.plugin.pojo.Conf;
import net.mamoe.mirai.console.plugin.jvm.JavaPlugin;
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescriptionBuilder;
import net.mamoe.mirai.event.GlobalEventChannel;
import net.mamoe.mirai.event.events.BotOnlineEvent;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.regex.Pattern;

import static cn.imldy.mirai.console.plugin.conf.Patterns.patternList;

public final class FileForward extends JavaPlugin {
    public static final FileForward INSTANCE = new FileForward();

    private FileForward() {
        super(new JvmPluginDescriptionBuilder("cn.imldy.mirai.console.plugin.FileForward", "1.0-SNAPSHOT").name("QQ文件转发").author("imldy").build());
    }

    @Override
    public void onEnable() {
        getLogger().info("插件已加载!");
        try {
            loadConfiguration();
        } catch (FileNotFoundException e) {
            getLogger().error("配置文件不存在", e);
        }
        getLogger().info("配置已加载!");
        GlobalEventChannel.INSTANCE.subscribeAlways(BotOnlineEvent.class, this::onLogin);
    }

    private void loadConfiguration() throws FileNotFoundException {
        File configFile = resolveConfigFile("conf.yaml");

        Conf conf = loadConfigurationFile(configFile.getAbsolutePath());

        loadPatternList(patternList, conf);

    }

    /**
     * 加载yaml配置文件
     *
     * @param path yaml配置文件路径
     * @return 配置文件
     * @throws FileNotFoundException 文件不存在
     */
    private Conf loadConfigurationFile(String path) throws FileNotFoundException {
        Yaml yaml = new Yaml(new Constructor(Conf.class));
        return yaml.load(new InputStreamReader(new FileInputStream(path)));
    }

    /**
     * 加载配置中的正则表达式匹配模式至列表
     *
     * @param patternList 列表
     * @param conf        配置文件对象
     */
    private void loadPatternList(List<Pattern> patternList, Conf conf) {
        for (String pattern : conf.getPatterns()) {
            Pattern p = Pattern.compile(pattern);
            patternList.add(p);
        }
    }

    private void onLogin(BotOnlineEvent event) {
        getLogger().info(String.format("%d上线了，开始注册文件转发事件处理器", event.getBot().getId()));
        event.getBot().getEventChannel().registerListenerHost(new QQEventHandlers());
    }
}