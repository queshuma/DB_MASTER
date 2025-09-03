package org.shuzhi.Common;

import cn.dev33.satoken.stp.StpInterface;
import org.shuzhi.Utils.SysUserUtils;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 自定义权限加载接口实现类
 */
@Component
public class StpInterfaceImpl implements StpInterface {

    private final SysUserUtils sysUserUtils;

    public StpInterfaceImpl(SysUserUtils sysUserUtils) {
        this.sysUserUtils = sysUserUtils;
    }

    /**
     * 返回一个账号所拥有的权限码集合
     */
    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {
        List<String> list = new ArrayList<>();
        // 设置权限码集合
        sysUserUtils.getPermission().get(loginId).forEach(list::add);
        return list;
    }

    /**
     * 返回一个账号所拥有的角色标识集合 (权限与角色可分开校验)
     */
    @Override
    public List<String> getRoleList(Object loginId, String loginType) {
        List<String> list = new ArrayList<>();
        sysUserUtils.getRole().get(loginId).forEach(list::add);
        return list;
    }
}
