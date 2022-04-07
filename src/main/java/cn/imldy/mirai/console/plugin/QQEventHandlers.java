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
import net.mamoe.mirai.utils.MiraiLogger;
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
        MiraiLogger logger = event.getBot().getLogger();
        if (!matchSourceGroup(event.getGroup())) {
            logger.debug("当前群消息的来源不是文件转发的sourceGroup");
            return;
        }
        logger.info("当前群消息的来源是文件转发的sourceGroup");

        FileMessage fileMessage = null;
        for (SingleMessage singleMessage : event.getMessage()) {
            logger.debug(String.format("开始判断是否是文件消息：%s", singleMessage));
            if (singleMessage instanceof FileMessage) {
                logger.debug(String.format("是文件消息：%s", singleMessage));
                FileMessage tempFileMessage = (FileMessage) singleMessage;
                logger.debug(String.format("尝试匹配文件：%s", tempFileMessage));
                if (matchFileMessage(tempFileMessage)) {
                    logger.info(String.format("匹配到文件：%s", tempFileMessage));
                    fileMessage = tempFileMessage;
                } else {
                    logger.debug(String.format("未匹配到文件：%s", tempFileMessage));
                }
            } else {
                logger.debug(String.format("不是文件消息：%s", singleMessage));
            }
        }
        // 为空代表此条信息无文件，直接结束
        if (fileMessage == null) {
            logger.debug("本次收到的消息中没有匹配到文件消息");
            return;
        }

        // 确定本地文件存放目录
        File path = FileForward.INSTANCE.resolveDataFile("/");
        File toDayFile = new File(path + "/" + fileMessage.getName());
        logger.info(String.format("此匹配到文件将被下载至目录：%s", toDayFile));

        logger.info("下载文件");
        // 下载文件
        AbsoluteFile absoluteFile = fileMessage.toAbsoluteFile(event.getGroup());

        String url = absoluteFile.getUrl();

        downloadFile(url, toDayFile);

        logger.info("上传文件");
        // 上传文件

        Group targetGroup = event.getBot().getGroup(MyConf.conf.getTargetGroup());
        assert targetGroup != null;

        logger.info(String.format("准备上传文件到目标群：%d", targetGroup.getId()));

        logger.info("开始上传并发送");
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
        MiraiLogger logger = targetGroup.getBot().getLogger();
        logger.info(String.format("开始把本地文件[%s]上传到群[%d]文件目录[%s]中并命名为[%s]",
                localFile,
                targetGroup.getId(),
                MyConf.conf.getTargetPath(),
                remoteName
        ));

        ExternalResource resource = ExternalResource.create(localFile);
        // 文件标识
        RemoteFiles files = targetGroup.getFiles();
        AbsoluteFolder root = files.getRoot();

        AbsoluteFolder folder = root.resolveFolder(MyConf.conf.getTargetPath());

        AbsoluteFile uploadNewFile = folder.uploadNewFile(remoteName, resource);
        logger.info(String.format("上传文件成功，返回结果[%s]", uploadNewFile));

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
