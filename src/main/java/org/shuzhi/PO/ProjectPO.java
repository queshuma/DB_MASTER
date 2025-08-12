package org.shuzhi.PO;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Data
@TableName("prj_project_info")
public class ProjectPO {
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
     * 项目类型
     */
    private String projectType;

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
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.UPDATE)
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
