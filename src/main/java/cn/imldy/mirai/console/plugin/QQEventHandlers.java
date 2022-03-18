package cn.imldy.mirai.console.plugin;

import cn.imldy.mirai.console.plugin.conf.MyConf;
import cn.imldy.mirai.console.plugin.conf.Patterns;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.event.EventHandler;
import net.mamoe.mirai.event.SimpleListenerHost;
import net.mamoe.mirai.event.events.GroupMessageEvent;
import net.mamoe.mirai.message.data.FileMessage;
import net.mamoe.mirai.message.data.SingleMessage;

import java.util.regex.Pattern;

/**
 * @author imldy
 * @date 2022/03/18 16:16
 **/
public class QQEventHandlers extends SimpleListenerHost {

    @EventHandler
    public void onGroupMessageEvent(GroupMessageEvent event) {
        if (!matchSourceGroup(event.getGroup()))
            return;

        SingleMessage fileMessage = null;
        for (SingleMessage singleMessage : event.getMessage()) {
            if (singleMessage instanceof FileMessage) {
                if (matchFileMessage((FileMessage) singleMessage))
                    fileMessage = singleMessage;
            }
        }
        assert fileMessage != null;
        Group targetGroup = event.getBot().getGroup(MyConf.conf.getTargetGroup());
        assert targetGroup != null;
        targetGroup.sendMessage(fileMessage);
    }

    /**
     * 判断消息来源群组是否满足要求
     *
     * @param group 消息来源群
     * @return 是否满足要求
     */
    private boolean matchSourceGroup(Group group) {
        return group.getId() == MyConf.conf.getSourceGroup();
    }

    /**
     * 判断文件是否满足要求
     *
     * @param fileMessage 文件消息
     * @return 是否满足要求
     */
    private boolean matchFileMessage(FileMessage fileMessage) {
        // 匹配正则表达式列表
        for (Pattern pattern : Patterns.patternList) {
            // 匹配文件名
            boolean matched = pattern.matcher(fileMessage.getName()).find();
            // 匹配到就返回
            if (matched)
                return true;
        }
        return false;
    }
}
