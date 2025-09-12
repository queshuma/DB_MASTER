package org.shuzhi.Utils;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.shuzhi.Mapper.SysUserInfoMapper;
import org.shuzhi.PO.SysUserInfoPO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class SysUserUtils {

    private final SysUserInfoMapper sysUserInfoMapper;

    private final RedisTemplate redisTemplate;

    Logger logger = LoggerFactory.getLogger(SysUserUtils.class);

    public SysUserUtils(SysUserInfoMapper sysUserInfoMapper, RedisTemplate redisTemplate) {
        this.sysUserInfoMapper = sysUserInfoMapper;
        this.redisTemplate = redisTemplate;
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

    public SysUserInfoPO getLoginUserInfo() {
        System.out.println(redisTemplate.opsForSet().pop(StpUtil.getLoginId().toString()));
        SysUserInfoPO sysUserInfoPO = (SysUserInfoPO) redisTemplate.opsForSet().pop(StpUtil.getLoginId().toString());
        if (sysUserInfoPO == null) {
            sysUserInfoPO = sysUserInfoMapper.selectById(StpUtil.getLoginId().toString());
            try{
                redisTemplate.opsForSet().add(StpUtil.getLoginId().toString(), sysUserInfoPO);
            } catch (Exception e) {
                logger.error("redis插入用户信息异常", e);
            }
        }
        return sysUserInfoPO;
    }
}
