package org.shuzhi.Service;

import cn.dev33.satoken.stp.StpUtil;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.shuzhi.Common.ResponseResult;
import org.shuzhi.Common.ResultCode;
import org.shuzhi.Dto.UserBaseDTO;
import org.shuzhi.Dto.UserRegisterDTO;
import org.shuzhi.Mapper.SysUserInfoMapper;
import org.shuzhi.Mapstruct.SysUserInfoMapstruct;
import org.shuzhi.PO.SysUserInfoPO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SysUserService {

    private final SysUserInfoMapper sysUserInfoMapper;

    private final RedisTemplate redisTemplate;

    Logger logger = LoggerFactory.getLogger(SysUserService.class);

    public SysUserService(SysUserInfoMapper sysUserInfoMapper, RedisTemplate redisTemplate) {
        this.sysUserInfoMapper = sysUserInfoMapper;
        this.redisTemplate = redisTemplate;
    }

    public ResponseResult<Object> login(String username, String password) {
        List<SysUserInfoPO> userInfo = sysUserInfoMapper.selectList(new LambdaQueryWrapper<SysUserInfoPO>()
                .eq(SysUserInfoPO::getTelephone, username).eq(SysUserInfoPO::getPassword, password).select(SysUserInfoPO::getId, SysUserInfoPO::getUsername, SysUserInfoPO::getTelephone, SysUserInfoPO::getEmail));
        if (userInfo.size() != 1) {
            return new ResponseResult<>(ResultCode.LOGIN_ERROR, "登录失败: 用户信息有误");
        }
        if (userInfo.size() == 1) {
            StpUtil.login(userInfo.get(0).getId());
            this.insertUserCache();
            return new ResponseResult<>(ResultCode.SUCCESS, userInfo.get(0));
        }
        return new ResponseResult<>(ResultCode.LOGIN_FAIL, "登录失败: 请联系管理员");
    }

    public ResponseResult<Object> loginByEmail(String email, String dynamicPassword) {
        List<SysUserInfoPO> userInfo = sysUserInfoMapper.selectList(new LambdaQueryWrapper<SysUserInfoPO>()
                .eq(SysUserInfoPO::getEmail, email).select(SysUserInfoPO::getId, SysUserInfoPO::getUsername, SysUserInfoPO::getTelephone, SysUserInfoPO::getEmail));
        if (userInfo.size() != 1) {
            return new ResponseResult<>(ResultCode.LOGIN_ERROR, "登录失败: 用户信息有误");
        }
        if (!dynamicPassword.equals(redisTemplate.opsForValue().get(email))) {
            return new ResponseResult<>(ResultCode.LOGIN_FAIL, "登录失败: 验证码错误");
        }
        StpUtil.login(userInfo.get(0).getId());
        redisTemplate.opsForSet().remove(email);
        this.insertUserCache();
        return new ResponseResult<>(ResultCode.SUCCESS, userInfo.get(0));
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

    public UserBaseDTO getUserInfo() {
        SysUserInfoPO sysUserInfoPO = sysUserInfoMapper.selectById(StpUtil.getLoginId().toString());
        return SysUserInfoMapstruct.INSTANCE.toUserBaseDTO(sysUserInfoPO);
    }

    public void modifyUserInfo(UserBaseDTO userBaseDTO) {
        SysUserInfoPO sysUserInfoPO = SysUserInfoMapstruct.INSTANCE.toSysUserInfoPO(userBaseDTO);
        userBaseDTO.setId(StpUtil.getLoginId().toString());
        sysUserInfoMapper.updateById(sysUserInfoPO);
    }

    public String getAvatarImage() {
        return sysUserInfoMapper.selectById(StpUtil.getLoginId().toString()).getAvatar();
    }

    public void uploadAvatar(String fileName) {
        SysUserInfoPO sysUserInfoPO = sysUserInfoMapper.selectById(StpUtil.getLoginId().toString());
        sysUserInfoPO.setAvatar(fileName);
        sysUserInfoMapper.updateById(sysUserInfoPO);
    }

    private void insertUserCache() {
        redisTemplate.opsForSet().add(StpUtil.getLoginId().toString(), JSONObject.toJSONString(sysUserInfoMapper.selectById(StpUtil.getLoginId().toString())));

    }
}
