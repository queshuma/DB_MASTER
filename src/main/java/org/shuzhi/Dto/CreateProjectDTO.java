package org.shuzhi.Dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CreateProjectDTO {
    /**
     * 项目名称
     */
    private String projectName;

    /**
     * 项目描述
     */
    private String projectDescription;

    /**
     * 项目类型
     */
    private String projectType;
}
