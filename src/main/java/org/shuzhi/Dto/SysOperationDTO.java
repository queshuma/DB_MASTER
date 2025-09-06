package org.shuzhi.Dto;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;

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
public class SysOperationDTO {

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
     * 项目名称
     */
    private String projectName;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createDate;
}
