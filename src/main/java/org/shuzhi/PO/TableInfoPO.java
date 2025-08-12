package org.shuzhi.PO;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

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
@Data
@TableName("prj_table_info")
public class TableInfoPO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    private String id;

    /**
     * 主键ID
     */
    private String sourceId;

    /**
     * 表名
     */
    private String databaseName;

    /**
     * 表名
     */
    private String tableName;

    /**
     * 表描述信息
     */
    private String description;

    /**
     * 存储引擎类型
     */
    private String engine;

    /**
     * 字符集
     */
    private String charset;

    /**
     * 最后更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date updateDate;

    /**
     * 版本号
     */
    private String version;
}
