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
 * 用户信息
 * </p>
 *
 * @author chat2db
 * @since 2025-08-14
 */
@Getter
@Setter
@TableName("sys_user_info")
public class SysUserInfoPO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String id;

    /**
     * 用户名
     */
    private String username;

    /**
     * 密码
     */
    private String password;

    private String telephone;

    private String email;

    private String permission;

    private String role;

    /**
     * 状态 0:禁用 1:正常
     */
    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private Date createDate;

    private Date lastLoginDate;
}
