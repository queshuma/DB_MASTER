package org.shuzhi.Controller;

import jakarta.servlet.http.HttpServletResponse;
import org.shuzhi.Common.ResponseResult;
import org.shuzhi.Dto.UserBaseDTO;
import org.shuzhi.Dto.UserRegisterDTO;
import org.shuzhi.Service.MINIOFileService;
import org.shuzhi.Service.SysUserService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
public class UserController {

    private final SysUserService sysUserService;
    private final MINIOFileService minIOService;

    public UserController(SysUserService sysUserService, MINIOFileService minIOService) {
        this.sysUserService = sysUserService;
        this.minIOService = minIOService;
    }

    /**
     * 通过账密登录
     * @param username
     * @param password
     * @return
     */
    @GetMapping("/login")
    public ResponseResult<Object> login(@RequestParam String username, @RequestParam String password) {
        return sysUserService.login(username, password);
    }

    /**
     * 通过邮箱动态登录
     * @param email
     * @param dynamicPassword
     * @return
     */
    @GetMapping("/loginByEmail")
    public ResponseResult<Object> loginByEmail(@RequestParam String email, @RequestParam String dynamicPassword) {
        return sysUserService.loginByEmail(email, dynamicPassword);
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

    /**
     * 获取用户信息
     * @return
     */
    @GetMapping("/getUserInfo")
    public UserBaseDTO getUserInfo() {
        return sysUserService.getUserInfo();
    }

    /**
     * 修改用户信息
     * @return
     */
    @PostMapping("/modifyUserInfo")
    public void modifyUserInfo(@RequestBody UserBaseDTO userBaseDTO) {
        sysUserService.modifyUserInfo(userBaseDTO);
    }

    /**
     * 获取用户头像
     * @param response
     * @throws Exception
     */
    @GetMapping("/getAvatarImage")
    public void uploadFile(HttpServletResponse response) throws Exception {
        String avatarName = sysUserService.getAvatarImage();
        if (avatarName != null) {
            minIOService.getFileUrl("db-master-bucket", avatarName, response);
        }
    }
}
