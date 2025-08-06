package org.shuzhi.Dto;

import lombok.Data;

@Data
public class DataBaseInfo {
    /**
     * 数据库地址
     */
    private String databaseUrl;

    /**
     * 数据库驱动
     */
    private String databaseDriver;

    /**
     * 数据库用户
     */
    private String databaseUser;

    /**
     * 数据库密码
     */
    private String databasePassword;

    /**
     * 数据库名称
     */
    private String databaseName;
}
