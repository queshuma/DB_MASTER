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
@TableName("prj_project_version")
public class ProjectVersionPO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    private String id;

    /**
     * 数据源ID(项目id)
     */
    private String sourceId;

    /**
     * 项目名称
     */
    private String projectName;

    /**
     * 版本号
     */
    private String version;

    /**
     * 版本号
     */
    @TableField(fill = FieldFill.INSERT)
    private Date createDate;
}
