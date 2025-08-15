package org.shuzhi.Utils;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.shuzhi.Mapper.SysUserInfoMapper;
import org.shuzhi.PO.SysUserInfoPO;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class SysUserUtils {

    private final SysUserInfoMapper sysUserInfoMapper;

    public SysUserUtils(SysUserInfoMapper sysUserInfoMapper) {
        this.sysUserInfoMapper = sysUserInfoMapper;
    }

    public List<String> getUserIdList() {
        return sysUserInfoMapper.selectObjs(new LambdaQueryWrapper<SysUserInfoPO>().select(SysUserInfoPO::getId));
    }

    public Map<String, List<String>> getPermission() {
        Map<String, List<String>> userPermission = new HashMap<>();
        List<SysUserInfoPO> sysUserInfoPOList = sysUserInfoMapper.selectList(new LambdaQueryWrapper<SysUserInfoPO>().select(SysUserInfoPO::getId, SysUserInfoPO::getPermission));
        for(String id : this.getUserIdList()) {
            List<SysUserInfoPO> collect = sysUserInfoPOList.stream().filter(t -> id.equals(t.getId())).collect(Collectors.toList());
            if (collect.size() != 1) {
                throw new RuntimeException("用户信息异常");
            }
            userPermission.put(id, Arrays.stream(collect.get(0).getPermission().split(",")).toList());
        }
        return userPermission;
    }

    public Map<String, List<String>> getRole() {
        Map<String, List<String>> userPermission = new HashMap<>();
        List<SysUserInfoPO> sysUserInfoPOList = sysUserInfoMapper.selectList(new LambdaQueryWrapper<SysUserInfoPO>().select(SysUserInfoPO::getId, SysUserInfoPO::getPermission));
        for(String id : this.getUserIdList()) {
            List<SysUserInfoPO> collect = sysUserInfoPOList.stream().filter(t -> id.equals(t.getId())).collect(Collectors.toList());
            if (collect.size() != 1) {
                throw new RuntimeException("用户信息异常");
            }
            userPermission.put(id, Arrays.stream(collect.get(0).getRole().split(",")).toList());
        }
        return userPermission;
    }
}
