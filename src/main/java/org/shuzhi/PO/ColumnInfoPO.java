package org.shuzhi.PO;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.Date;

/**
 * <p>
 * 
 * </p>
 *
 * @author chat2db
 * @since 2025-06-29
 */
@Getter
@Setter
@ToString
@TableName("prj_column_info")
public class ColumnInfoPO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    private String id;

    /**
     * 数据源ID(表id)
     */
    private String sourceId;

    /**
     * 数据库名
     */
    private String databaseName;

    /**
     * 表名
     */
    private String tableName;

    /**
     * 列名
     */
    private String columnName;

    /**
     * 数据类型名称
     */
    private String typeName;

    /**
     * 列大小
     */
    private Integer columnSize;

    /**
     * 是否可为空
     */
    private String nullable;

    /**
     * 列注释
     */
    private String remarks;

    /**
     * 更新时间
     */
    private Date updateDate;

    /**
     * 版本号
     */
    private String version;
}
