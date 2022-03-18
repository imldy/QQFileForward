package cn.imldy.mirai.console.plugin;

import cn.imldy.mirai.console.plugin.conf.MyConf;
import cn.imldy.mirai.console.plugin.conf.Patterns;
import com.ejlchina.okhttps.HTTP;
import kotlin.coroutines.CoroutineContext;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.contact.file.AbsoluteFile;
import net.mamoe.mirai.contact.file.AbsoluteFolder;
import net.mamoe.mirai.contact.file.RemoteFiles;
import net.mamoe.mirai.event.EventHandler;
import net.mamoe.mirai.event.SimpleListenerHost;
import net.mamoe.mirai.event.events.GroupMessageEvent;
import net.mamoe.mirai.message.data.FileMessage;
import net.mamoe.mirai.message.data.SingleMessage;
import net.mamoe.mirai.utils.ExternalResource;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.regex.Pattern;

/**
 * @author imldy
 * @date 2022/03/18 16:16
 **/
public class QQEventHandlers extends SimpleListenerHost {
    HTTP http = HTTP.builder().build();

    @Override
    public void handleException(@NotNull CoroutineContext context, @NotNull Throwable exception) {
        // super.handleException(context, exception);
        System.out.println("出错了");
        exception.printStackTrace();
    }

    @EventHandler
    public void onGroupMessageEvent(GroupMessageEvent event) {
        if (!matchSourceGroup(event.getGroup()))
            return;

        FileMessage fileMessage = null;
        for (SingleMessage singleMessage : event.getMessage()) {
            if (singleMessage instanceof FileMessage) {
                if (matchFileMessage((FileMessage) singleMessage))
                    fileMessage = (FileMessage) singleMessage;
            }
        }
        // 为空代表此条信息无文件，直接结束
        if (fileMessage == null)
            return;

        // 确定本地文件存放目录
        File path = FileForward.INSTANCE.resolveDataFile("/量化");
        File toDayFile = new File(path + "/" + fileMessage.getName());

        // 下载文件
        AbsoluteFile absoluteFile = fileMessage.toAbsoluteFile(event.getGroup());

        String url = absoluteFile.getUrl();

        downloadFile(url, toDayFile);
        // 上传文件

        Group targetGroup = event.getBot().getGroup(MyConf.conf.getTargetGroup());
        assert targetGroup != null;

        uploadAndSend(targetGroup, toDayFile, fileMessage.getName());

    }

    /**
     *
     * @param url
     * @param destFile
     */
    private void downloadFile(String url, File destFile) {
        http.sync(url)
                .get()
                .getBody()
                .toFile(destFile)
                .start();
    }

    /**
     * 上传到目标群并命名
     *
     * @param targetGroup 目标群
     * @param localFile   本地文件
     * @param remoteName  上传后的命名
     */
    private void uploadAndSend(Group targetGroup, File localFile, String remoteName) {

        ExternalResource resource = ExternalResource.create(localFile);
        // 文件标识
        RemoteFiles files = targetGroup.getFiles();
        AbsoluteFolder root = files.getRoot();

        AbsoluteFolder folder = root.resolveFolder(MyConf.conf.getTargetPath());

        AbsoluteFile uploadNewFile = folder.uploadNewFile(remoteName, resource);

        // 好像不需要执行发送消息的步骤
        // FileMessage fileMessage1 = uploadNewFile.toMessage();
        // targetGroup.sendMessage(fileMessage1);
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
