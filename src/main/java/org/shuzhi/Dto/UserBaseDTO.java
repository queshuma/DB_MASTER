package org.shuzhi.Dto;

import lombok.Data;

@Data
public class UserBaseDTO {
    /**
     * 用户唯一标识符
     */
    private String id;
    
    /**
     * 用户名
     */
    private String username;
    
    /**
     * 用户电话号码
     */
    private String telephone;
    
    /**
     * 用户电子邮箱
     */
    private String email;
}
