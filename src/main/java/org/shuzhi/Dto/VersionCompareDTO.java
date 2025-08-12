package org.shuzhi.Dto;

import lombok.Data;

@Data
public class VersionCompareDTO {

    private String projectName;

    private String projectId;

    private String oldVersion;

    private String newVersion;
}
