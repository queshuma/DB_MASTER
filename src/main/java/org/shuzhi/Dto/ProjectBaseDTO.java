package org.shuzhi.Dto;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class ProjectBaseDTO implements Serializable {
    /**
     * id
     */
    private String id;

    /**
     * 项目名称
     */
    private String projectName;

    /**
     * 项目描述
     */
    private String projectDescription;

    /**
     * 项目路径
     */
    private String projectPath;

    /**
     * 项目类型
     */
    private String projectType;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 备份次数
     */
    private Integer backCount;

    /**
     * 版本号
     */
    private String version;
}
