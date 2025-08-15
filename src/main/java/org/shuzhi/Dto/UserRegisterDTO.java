package org.shuzhi.Dto;

import lombok.Data;
import lombok.Setter;

@Data
public class UserRegisterDTO {
    /**
     * 用户名
     */
    private String username;

    /**
     * 密码
     */
    private String password;

    /**
     * 手机号码
     */
    private String telephone;

    /**
     * 邮箱
     */
    private String email;
}
