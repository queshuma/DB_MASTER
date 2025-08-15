package org.shuzhi.Common;

import lombok.Getter;
import org.springframework.web.bind.annotation.GetMapping;

public interface ResultCode {
    String SUCCESS = "200";
    String ERROR = "403";

    /**
     * 用户登录信息有误_错误码
     */
    String LOGIN_ERROR = "10000";
    /**
     * 用户登录失败_错误码
     */
    String LOGIN_FAIL = "10001";
}
