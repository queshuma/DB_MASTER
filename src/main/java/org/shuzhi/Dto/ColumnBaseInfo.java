package org.shuzhi.Dto;

import lombok.Data;

@Data
public class ColumnBaseInfo {
    private String columnName;
    private String typeName;
    private Integer columnSize;
    private Boolean nullable;
    private String remarks;
    private String errorRemarks = "";
}
