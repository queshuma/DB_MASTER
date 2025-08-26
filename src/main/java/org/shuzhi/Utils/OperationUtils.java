package org.shuzhi.Utils;

import cn.dev33.satoken.stp.StpUtil;
import lombok.Getter;
import org.shuzhi.Dto.OperationInfoDTO;
import org.shuzhi.Mapper.SysOperationHistoryMapper;
import org.shuzhi.PO.SysOperationHistoryPO;
import org.shuzhi.PO.SysUserInfoPO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.function.Consumer;

@Getter
public enum OperationUtils {
    CREATE_PROJECT(1, "创建项目", OperationUtils::insertCreateProject),
    UPDATE_PROJECT(2, "更新项目", OperationUtils::insertUpdateProject),
    UPDATE_PROJECT_DATA(2, "更新项目数据库", OperationUtils::insertUpdateProjectData),
    BACKUP_PROJECT(4, "备份项目", OperationUtils::insertBackupProject),
    UPDATE_PROJECT_DATA_CONFIG(5, "更新项目数据配置", OperationUtils::insertUpdateProjectDataConfig),
    COMPARE_VERSION_TABLE_DIFF(5, "比较版本的表差异", OperationUtils::insertCompareVersionTableDiff),
    COMPARE_VERSION_FIELD_DIFF(5, "比较版本的字段差异", OperationUtils::insertCompareVersionFieldDiff),
    ;

    private final int code;
    private final String name;
    private final Consumer<OperationInfoDTO> consumer;

    @Autowired
    private static SysOperationHistoryMapper sysOperationHistoryMapper;

    @Autowired
    private static RedisTemplate redisTemplate;

    OperationUtils(int code, String name, Consumer<OperationInfoDTO> consumer) {
        this.code = code;
        this.name = name;
        this.consumer = consumer;
    }

    private static void insertCreateProject(OperationInfoDTO operationInfoDTO) {
        insertOperationHistory(String.format("创建项目 %s", operationInfoDTO.getProjectName()), operationInfoDTO.getProjectId(), operationInfoDTO.getProjectName());
    }

    private static void insertUpdateProject(OperationInfoDTO operationInfoDTO) {
        insertOperationHistory(String.format("更新项目信息 %s", operationInfoDTO.getProjectName()), operationInfoDTO.getProjectId(), operationInfoDTO.getProjectName());
    }


    private static void insertUpdateProjectData(OperationInfoDTO operationInfoDTO) {
        insertOperationHistory(String.format("更新项目数据库信息 %s", operationInfoDTO.getProjectName()), operationInfoDTO.getProjectId(), operationInfoDTO.getProjectName());
    }

    private static void insertBackupProject(OperationInfoDTO operationInfoDTO) {
        insertOperationHistory(String.format("数据结构备份 %s", operationInfoDTO.getProjectName()), operationInfoDTO.getProjectId(), operationInfoDTO.getProjectName());
    }

    private static void insertUpdateProjectDataConfig(OperationInfoDTO operationInfoDTO) {
        insertOperationHistory(String.format("更新项目数据配置 %s", operationInfoDTO.getProjectName()), operationInfoDTO.getProjectId(), operationInfoDTO.getProjectName());
    }

    private static void insertCompareVersionTableDiff(OperationInfoDTO operationInfoDTO) {
        insertOperationHistory(String.format("查询数据库表差异 %s", operationInfoDTO.getProjectName()), operationInfoDTO.getProjectId(), operationInfoDTO.getProjectName());
    }

    private static void insertCompareVersionFieldDiff(OperationInfoDTO operationInfoDTO) {
        insertOperationHistory(String.format("查询数据库字段差异 %s", operationInfoDTO.getProjectName()), operationInfoDTO.getProjectId(), operationInfoDTO.getProjectName());
    }


    private static void insertOperationHistory(String operation, String projectId, String projectName) {
        SysUserInfoPO sysUserInfoPO = (SysUserInfoPO)redisTemplate.opsForSet().pop(StpUtil.getLoginId().toString());
        SysOperationHistoryPO sysOperationHistoryPO = new SysOperationHistoryPO();
        sysOperationHistoryPO.setOperation(operation);
        sysOperationHistoryPO.setUserId(StpUtil.getLoginId().toString());
        sysOperationHistoryPO.setUserName(sysUserInfoPO.getUsername());
        sysOperationHistoryPO.setProjectId(projectId);
        sysOperationHistoryPO.setProjectName(projectName);
        sysOperationHistoryMapper.insert(sysOperationHistoryPO);
    }
}
