package org.shuzhi.Controller;

import cn.dev33.satoken.stp.StpUtil;
import org.shuzhi.Common.ResponseResult;
import org.shuzhi.Dto.UserRegisterDTO;
import org.shuzhi.Service.SysUserService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
public class UserController {

    private final SysUserService sysUserService;

    public UserController(SysUserService sysUserService) {
        this.sysUserService = sysUserService;
    }

    @GetMapping("/login")
    public ResponseResult<Object> login(@RequestParam String username, @RequestParam String password) {
        return sysUserService.login(username, password);
    }

    @PostMapping("/register")
    public void register(@RequestBody UserRegisterDTO userRegisterDTO) {
        sysUserService.register(userRegisterDTO);
    }

    /**
     * 退出登录
     * @return
     */
    @RequestMapping("/logout")
    public String logout() {
        return sysUserService.logout();
    }
}
