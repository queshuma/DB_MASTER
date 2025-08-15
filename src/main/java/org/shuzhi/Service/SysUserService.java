package org.shuzhi.Service;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.shuzhi.Common.ResponseResult;
import org.shuzhi.Common.ResultCode;
import org.shuzhi.Dto.UserRegisterDTO;
import org.shuzhi.Mapper.SysUserInfoMapper;
import org.shuzhi.Mapstruct.SysUserInfoMapstruct;
import org.shuzhi.PO.SysUserInfoPO;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class SysUserService {

    private final SysUserInfoMapper sysUserInfoMapper;


    public SysUserService(SysUserInfoMapper sysUserInfoMapper) {
        this.sysUserInfoMapper = sysUserInfoMapper;
    }

    public ResponseResult<Object> login(String username, String password) {
        List<SysUserInfoPO> userInfo = sysUserInfoMapper.selectList(new LambdaQueryWrapper<SysUserInfoPO>()
                .eq(SysUserInfoPO::getTelephone, username).eq(SysUserInfoPO::getPassword, password).select(SysUserInfoPO::getId, SysUserInfoPO::getUsername, SysUserInfoPO::getTelephone, SysUserInfoPO::getEmail));
        if (userInfo.size() != 1) {
            return new ResponseResult<>(ResultCode.LOGIN_ERROR, "登录失败: 用户信息有误");
        }
        if (userInfo.size() == 1) {
            StpUtil.login(userInfo.get(0).getId());
            return new ResponseResult<>(ResultCode.SUCCESS, userInfo.get(0));
        }
        return new ResponseResult<>(ResultCode.LOGIN_FAIL, "登录失败: 请联系管理员");

    }

    public String logout() {
        StpUtil.logout();
        return "退出登录";
    }

    public void register(UserRegisterDTO userRegisterDTO) {
        SysUserInfoPO sysUserInfoPO = SysUserInfoMapstruct.INSTANCE.toSysUserInfoPO(userRegisterDTO);
        sysUserInfoPO.setStatus(1);
        sysUserInfoPO.setRole(null);
        sysUserInfoPO.setPermission(null);
        sysUserInfoMapper.insert(sysUserInfoPO);
    }
}
