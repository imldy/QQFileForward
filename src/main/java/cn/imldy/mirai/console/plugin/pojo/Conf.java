package cn.imldy.mirai.console.plugin.pojo;

import java.util.List;

/**
 * @author imldy
 * @date 2022/03/18 17:02
 **/
public class Conf {
    List<String> patterns;
    Long sourceGroup;

    public List<String> getPatterns() {
        return patterns;
    }

    public void setPatterns(List<String> patterns) {
        this.patterns = patterns;
    }

    public Long getSourceGroup() {
        return sourceGroup;
    }

    public void setSourceGroup(Long sourceGroup) {
        this.sourceGroup = sourceGroup;
    }

    public Long getTargetGroup() {
        return targetGroup;
    }

    public void setTargetGroup(Long targetGroup) {
        this.targetGroup = targetGroup;
    }

    Long targetGroup;
}
