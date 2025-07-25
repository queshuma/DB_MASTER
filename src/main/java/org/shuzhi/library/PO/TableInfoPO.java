package org.shuzhi.library.PO;

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
@TableName("table_info")
public class TableInfoPO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String id;

    private String tableName;

    private String columnName;

    private String typeName;

    private Integer columnSize;

    private String nullable;

    private String remarks;

    private Date updateDate;

    private Integer version;
}
