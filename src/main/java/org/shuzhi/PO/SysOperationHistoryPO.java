package org.shuzhi.PO;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;

import lombok.Getter;
import lombok.Setter;

/**
 * <p>
 * 操作记录
 * </p>
 *
 * @author chat2db
 * @since 2025-08-26
 */
@Getter
@Setter
@TableName("sys_operation_history")
public class SysOperationHistoryPO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String id;

    /**
     * 操作记录
     */
    private String operation;

    /**
     * 用户名称
     */
    private String userName;

    /**
     * 用户编号
     */
    private String userId;

    /**
     * 项目编号
     */
    private String projectId;

    /**
     * 项目名称
     */
    private String projectName;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private Date createDate;
}
