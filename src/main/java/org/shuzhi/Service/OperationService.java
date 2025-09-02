package org.shuzhi.Service;

import cn.dev33.satoken.stp.StpUtil;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.shuzhi.Config.UserContext;
import org.shuzhi.Dto.OperationInfoDTO;
import org.shuzhi.Dto.SysOperationDTO;
import org.shuzhi.Dto.SysOperationFilterDTO;
import org.shuzhi.Mapper.SysOperationHistoryMapper;
import org.shuzhi.Mapstruct.SysOperationMapstruct;
import org.shuzhi.PO.SysOperationHistoryPO;
import org.shuzhi.PO.SysUserInfoPO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class OperationService {

    private static final Logger logger = LoggerFactory.getLogger(OperationService.class);

    @Autowired
    private SysOperationHistoryMapper sysOperationHistoryMapper;

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 获取操作记录列表
     * @return
     */
    public IPage<SysOperationDTO> getList(SysOperationFilterDTO sysOperationFilterDTO) {
        IPage<SysOperationHistoryPO> page = sysOperationHistoryMapper.selectPage(sysOperationFilterDTO.getPage(), new LambdaQueryWrapper<>());
        return page.convert(SysOperationMapstruct.INSTANCE::toSysOperationDTO);
    }

    public void insertCreateProject(OperationInfoDTO operationInfoDTO) {
        insertOperationHistory(String.format("创建项目 %s", operationInfoDTO.getProjectName()), operationInfoDTO);
    }

    public void insertUpdateProject(OperationInfoDTO operationInfoDTO) {
        insertOperationHistory(String.format("更新项目信息 %s", operationInfoDTO.getProjectName()), operationInfoDTO);
    }


    public void insertUpdateProjectData(OperationInfoDTO operationInfoDTO) {
        insertOperationHistory(String.format("更新项目数据库信息 %s", operationInfoDTO.getProjectName()), operationInfoDTO);
    }

    public void insertBackupProject(OperationInfoDTO operationInfoDTO) {
        insertOperationHistory(String.format("数据结构备份 %s", operationInfoDTO.getProjectName()), operationInfoDTO);
    }

    public void insertUpdateProjectDataConfig(OperationInfoDTO operationInfoDTO) {
        insertOperationHistory(String.format("更新项目数据配置 %s", operationInfoDTO.getProjectName()), operationInfoDTO);
    }

    public void insertCompareVersionTableDiff(OperationInfoDTO operationInfoDTO) {
        insertOperationHistory(String.format("查询数据库表差异 %s", operationInfoDTO.getProjectName()), operationInfoDTO);
    }

    public void insertCompareVersionFieldDiff(OperationInfoDTO operationInfoDTO) {
        insertOperationHistory(String.format("查询数据库字段差异 %s", operationInfoDTO.getProjectName()), operationInfoDTO);
    }


    private void insertOperationHistory(String operation, OperationInfoDTO operationInfoDTO) {
        SysUserInfoPO sysUserInfoPO;
        try {
            sysUserInfoPO = JSONObject.parseObject(redisTemplate.opsForSet().pop(operationInfoDTO.getUserId()).toString(), SysUserInfoPO.class);
        } catch (Exception e) {
            logger.error("Redis获取用户信息失败{}", operationInfoDTO.getUserId());
            throw e;
        }
        SysOperationHistoryPO sysOperationHistoryPO = new SysOperationHistoryPO();
        sysOperationHistoryPO.setOperation(operation);
        sysOperationHistoryPO.setUserId(operationInfoDTO.getUserId());
        sysOperationHistoryPO.setUserName(sysUserInfoPO.getUsername());
        sysOperationHistoryPO.setProjectId(operationInfoDTO.getProjectId());
        sysOperationHistoryPO.setProjectName(operationInfoDTO.getProjectName());
        sysOperationHistoryMapper.insert(sysOperationHistoryPO);
    }
}
