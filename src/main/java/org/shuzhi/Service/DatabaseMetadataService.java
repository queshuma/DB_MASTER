package org.shuzhi.Service;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.shuzhi.Config.UserContext;
import org.shuzhi.Dto.TableInfo;
import org.shuzhi.Dto.*;
import org.shuzhi.Mapper.*;
import org.shuzhi.Mapstruct.ColumnInfoMapstruct;
import org.shuzhi.Mapstruct.ProjectInfoMapstruct;
import org.shuzhi.PO.*;
import org.shuzhi.Config.DatabaseConfig;
import org.springframework.ai.tool.annotation.Tool;

import java.util.*;
import java.util.Date;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

import java.sql.*;
import java.sql.Connection;

@RequiredArgsConstructor
@Service
public class DatabaseMetadataService {
    private final TableInfoMapper tableInfoMapper;

    private final ProjectInfoMapper projectInfoMapper;

    private final ColumnInfoMapper columnInfoMapper;

    private final ProjectVersionMapper projectVersionMapper;

    private final OperationService operationService;

    public List<String> getTableNames(DatabaseConfig config) {
        List<String> tableNames = new ArrayList<>();

        try (Connection connection = getConnection(config)) {
            DatabaseMetaData metaData = connection.getMetaData();

            // 获取表信息(参数说明：catalog, schemaPattern, tableNamePattern, types[])
            ResultSet tables = metaData.getTables(config.getDatabaseName(), null, null, new String[]{"TABLE"});

            while (tables.next()) {
                String tableName = tables.getString("TABLE_NAME");
                tableNames.add(tableName);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return tableNames;
    }

    public List<TableInfo> getTableInfo(DatabaseConfig config) throws SQLException, ClassNotFoundException {

        List<TableInfo> tableInfos = new ArrayList<>();
        try (Connection connection = getConnection(config)) {
            DatabaseMetaData metaData = connection.getMetaData();

            // 获取表信息(参数说明：catalog, schemaPattern, tableNamePattern, types[])
            try (ResultSet tables = metaData.getTables(config.getDatabaseName(), null, null, new String[]{"TABLE"})) {
                while (tables.next()) {
                    TableInfo tableInfo = new TableInfo();
                    tableInfo.setTableName(tables.getString("TABLE_NAME"));
                    tableInfo.setTableType(tables.getString("TABLE_TYPE"));
                    tableInfo.setRemarks(tables.getString("REMARKS"));
                    tableInfo.setTypeCat(tables.getString("TYPE_CAT"));
                    tableInfo.setTypeSchem(tables.getString("TYPE_SCHEM"));
                    tableInfo.setTypeName(tables.getString("TYPE_NAME"));
                    tableInfo.setSelfReferencingColName(tables.getString("SELF_REFERENCING_COL_NAME"));
                    tableInfo.setRefGeneration(tables.getString("REF_GENERATION"));
                    tableInfos.add(tableInfo);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return tableInfos;
    }

    @Tool(description = "创建项目")
    @Transactional(rollbackFor = Exception.class)
    public ProjectPO createProject(CreateProjectDTO createProjectDTO) {
        ProjectPO projectPO = ProjectInfoMapstruct.INSTANCE.createToProjectPO(createProjectDTO);
        projectPO.setBackCount(0);
        projectInfoMapper.insert(projectPO);
        // 记录操作
        OperationInfoDTO operationInfoDTO = new OperationInfoDTO();
        operationInfoDTO.setUserId(UserContext.getUserId());
        operationInfoDTO.setProjectId(projectPO.getId());
        operationInfoDTO.setProjectName(projectPO.getProjectName());
        operationService.insertCreateProject(operationInfoDTO);
        return projectPO;
    }

    @Tool(description = "配置数据库")
    @Transactional(rollbackFor = Exception.class)
    public String updateProjectData(ProjectDatabaseDTO projectDatabaseDTO) {
        projectInfoMapper.updateById(ProjectInfoMapstruct.INSTANCE.updateToProjectPO(projectDatabaseDTO));
        // 记录操作
        OperationInfoDTO operationInfoDTO = new OperationInfoDTO();
        operationInfoDTO.setUserId(UserContext.getUserId());
        operationInfoDTO.setProjectId(projectDatabaseDTO.getId());
        operationInfoDTO.setProjectName(projectDatabaseDTO.getProjectName());
        operationService.insertUpdateProjectData(operationInfoDTO);
        return projectDatabaseDTO.getProjectName();
    }

    @Tool(description = "查询项目列表")
    public List<ProjectBaseDTO> getProjectList() throws Exception {

        List<ProjectPO> projectPOList = projectInfoMapper.selectList(new LambdaQueryWrapper<>());
        if (projectPOList.isEmpty()) {
            new ArrayList<>();
        }
        List<ProjectBaseDTO> projectBaseDTOList = ProjectInfoMapstruct.INSTANCE.toProjectDTOList(projectPOList);
        if (projectBaseDTOList.isEmpty()) {
            throw new Exception("当前没有项目数据");
        }
        return projectBaseDTOList;
    }

    public IPage<ProjectBaseDTO> getProjectList(ProjectFilterDTO projectFilterDTO) {
        LambdaQueryWrapper<ProjectPO> queryWrapper = new LambdaQueryWrapper<>();
        if (projectFilterDTO.getProjectName() != null && !projectFilterDTO.getProjectName().isEmpty()) {
            queryWrapper.eq(Objects.isNull(projectFilterDTO.getProjectName()) && StringUtils.isNotBlank(projectFilterDTO.getProjectName()), ProjectPO::getProjectName, projectFilterDTO.getProjectName());
        }
        IPage<ProjectPO> projectIPage = projectInfoMapper.selectPage(projectFilterDTO.getPage(), queryWrapper);
        return projectIPage.convert(ProjectInfoMapstruct.INSTANCE::toProjectBaseDTO);
    }

    @Tool(description = "根据编号或名称查询数据库信息")
    public ProjectBaseDTO getProjectDataBase(ProjectBaseDTO input) {
        return ProjectInfoMapstruct.INSTANCE.toProjectBaseDTO(projectInfoMapper.selectOne(new LambdaQueryWrapper<ProjectPO>()
                .eq(!Objects.isNull(input.getId()), ProjectPO::getId, input.getId())
                .eq(!Objects.isNull(input.getProjectName()), ProjectPO::getProjectName, input.getProjectName())));
    }

    @Tool(description = "根据编号或名称查询备份记录")
    public List<ProjectVersionPO> getProjectHistory(ProjectBaseDTO input) {
        return ProjectInfoMapstruct.INSTANCE.toProjectVersionList(projectVersionMapper.selectList(new LambdaQueryWrapper<ProjectVersionPO>()
                .eq(!Objects.isNull(input.getId()), ProjectVersionPO::getSourceId, input.getId())
                .eq(!Objects.isNull(input.getProjectName()), ProjectVersionPO::getProjectName, input.getProjectName())));
    }

    @Tool(description = "根据数据库查询表")
    public List<TableInfo> getDataTableList(DatabaseConfig input) {
        try {
            return this.getTableInfo(input);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 保存指定的数据库信息
     *
     * @param
     * @return
     * @throws ClassNotFoundException
     */
    @Tool(description = "备份项目数据结构")
    @Transactional(rollbackFor = Exception.class)
    public List<String> backupData(DatabaseConfig databaseConfig) {
        // 返回内容
        List<String> result = new ArrayList<>();
        DatabaseConfig config = databaseConfig;
        // 更新版本表
        ProjectVersionPO projectVersionPO = new ProjectVersionPO();
        projectVersionPO.setProjectName(config.getProjectName());
        projectVersionPO.setVersion(config.getVersion());
        projectVersionPO.setSourceId(config.getId());
        projectVersionMapper.insert(projectVersionPO);
        // 更新项目信息表
        projectVersionPO.setVersion(config.getVersion());
        ProjectPO projectPO = projectInfoMapper.selectById(config.getId());
        projectPO.setBackCount(projectPO.getBackCount() + 1);
        projectPO.setVersion(projectPO.getVersion());
        projectInfoMapper.updateById(projectPO);
        // 更新结构表
        try (Connection connection = getConnection(config)) {
            DatabaseMetaData metaData = connection.getMetaData();

            try (ResultSet tables = metaData.getTables(config.getDatabaseName(), null, null, new String[]{"TABLE"})) {
                while (tables.next()) {
                    TableInfoPO tableInfoPO = new TableInfoPO();

                    tableInfoPO.setTableName(tables.getString("TABLE_NAME"));
                    tableInfoPO.setDescription(tables.getString("REMARKS"));
                    tableInfoPO.setCharset(tables.getString("TYPE_CAT"));
                    tableInfoPO.setSourceId(config.getId());
                    tableInfoPO.setVersion(projectVersionPO.getVersion());
                    result.add(tableInfoPO.getTableName());
                    tableInfoMapper.insert(tableInfoPO);
                    try (ResultSet columnsResult = metaData.getColumns(config.getDatabaseName(), null, tableInfoPO.getTableName(), null)) {
                        while (columnsResult.next()) {
                            ColumnInfo column = new ColumnInfo();
                            column.setColumnName(columnsResult.getString("COLUMN_NAME"));
                            column.setTypeName(columnsResult.getString("TYPE_NAME"));
                            column.setColumnSize(columnsResult.getInt("COLUMN_SIZE"));
                            column.setNullable(columnsResult.getInt("NULLABLE") == DatabaseMetaData.columnNullable);
                            column.setRemarks(columnsResult.getString("REMARKS"));

                            this.saveTableColumns(config.getId(), tableInfoPO.getId(), tableInfoPO.getTableName(), column, config.getVersion());
                        }
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        // 记录操作
        OperationInfoDTO operationInfoDTO = new OperationInfoDTO();
        operationInfoDTO.setUserId(UserContext.getUserId());
        operationInfoDTO.setProjectId(config.getId());
        operationInfoDTO.setProjectName(config.getProjectName());
        operationService.insertBackupProject(operationInfoDTO);
        return result;
    }

    @Tool(description = "比较两个版本的数据表的差异")
    public List<String> compareTable(VersionCompareDTO versionCompareDTO) {
        List<String> versionList = Arrays.asList(versionCompareDTO.getOldVersion(), versionCompareDTO.getNewVersion());
        boolean exists = projectVersionMapper.exists(new LambdaQueryWrapper<ProjectVersionPO>().eq(ProjectVersionPO::getProjectName, versionCompareDTO.getProjectName()).in(ProjectVersionPO::getVersion, versionList));
        if (versionList.size() != 2) {
            throw new RuntimeException("请检查版本数量是否正确");
        }
        if (!exists) {
            throw new RuntimeException("请检查版本号是否正确");
        }
        List<TableInfoPO> tableInfoPOList = tableInfoMapper.selectList(new LambdaQueryWrapper<TableInfoPO>().eq(TableInfoPO::getSourceId, versionCompareDTO.getProjectId()).in(TableInfoPO::getVersion, versionList));
        Map<String, List<TableInfoPO>> versionMap = new HashMap<>();
        for (String version : versionList) {
            versionMap.put(version, new ArrayList<>());
        }
        for (TableInfoPO tableInfoPO : tableInfoPOList) {
            versionMap.get(tableInfoPO.getVersion()).add(tableInfoPO);
        }

        List<String> differences = new ArrayList<>();
        List<TableInfoPO> oldVersionTables = versionMap.get(versionCompareDTO.getOldVersion());
        List<TableInfoPO> newVersionTables = versionMap.get(versionCompareDTO.getNewVersion());

        Set<String> oldTableNames = new HashSet<>();
        Set<String> newTableNames = new HashSet<>();

        for (TableInfoPO table : oldVersionTables) {
            oldTableNames.add(table.getTableName());
        }

        for (TableInfoPO table : newVersionTables) {
            newTableNames.add(table.getTableName());
        }

        // 找出新增的表
        for (String tableName : newTableNames) {
            if (!oldTableNames.contains(tableName)) {
                differences.add("新增表: " + tableName);
            }
        }

        // 找出删除的表
        for (String tableName : oldTableNames) {
            if (!newTableNames.contains(tableName)) {
                differences.add("删除表: " + tableName);
            }
        }
        // 记录操作
        OperationInfoDTO operationInfoDTO = new OperationInfoDTO();
        operationInfoDTO.setUserId(UserContext.getUserId());
        operationInfoDTO.setProjectId(versionCompareDTO.getProjectId());
        operationInfoDTO.setProjectName(versionCompareDTO.getProjectName());
        operationService.insertCompareVersionTableDiff(operationInfoDTO);
        return differences;

    }

    ;

    @Tool(description = "比较两个版本的字段的差异")
    public List<String> compareColumn(VersionCompareDTO versionCompareDTO) {
        List<String> versionList = Arrays.asList(versionCompareDTO.getOldVersion(), versionCompareDTO.getNewVersion());
        boolean exists = projectVersionMapper.exists(new LambdaQueryWrapper<ProjectVersionPO>().eq(ProjectVersionPO::getProjectName, versionCompareDTO.getProjectName()).in(ProjectVersionPO::getVersion, versionList));
        if (versionList.size() != 2) {
            throw new RuntimeException("请检查版本数量是否正确");
        }
        if (!exists) {
            throw new RuntimeException("请检查版本号是否正确");
        }
        List<ColumnInfoPO> columnInfoPOList = columnInfoMapper.selectList(new LambdaQueryWrapper<ColumnInfoPO>().eq(ColumnInfoPO::getSourceId, versionCompareDTO.getProjectId()).in(ColumnInfoPO::getVersion, versionList));
        Map<String, Map<String, List<ColumnInfoPO>>> versionMap = new HashMap<>();
        for (String version : versionList) {
            versionMap.put(version, new HashMap<>());
        }
        for (ColumnInfoPO columnInfoPO : columnInfoPOList) {
            if (versionMap.get(columnInfoPO.getVersion()).containsKey(columnInfoPO.getTableName())) {
                versionMap.get(columnInfoPO.getVersion()).get(columnInfoPO.getTableName()).add(columnInfoPO);
            } else {
                versionMap.get(columnInfoPO.getVersion()).put(columnInfoPO.getTableName(), new ArrayList<>());
                versionMap.get(columnInfoPO.getVersion()).get(columnInfoPO.getTableName()).add(columnInfoPO);
            }
        }

        List<String> differences = new ArrayList<>();
        Map<String, List<ColumnInfoPO>> oldVersionTables = versionMap.get(versionCompareDTO.getOldVersion());
        Map<String, List<ColumnInfoPO>> newVersionTables = versionMap.get(versionCompareDTO.getOldVersion());


        List<String> oldTableList = oldVersionTables.keySet().stream().toList();
        List<String> newTableList = newVersionTables.keySet().stream().toList();

        // 遍历新版本的表及其字段信息
        for (Map.Entry<String, List<ColumnInfoPO>> newEntry : newVersionTables.entrySet()) {
            String tableName = newEntry.getKey();
            List<ColumnInfoPO> newColumns = newEntry.getValue();

            // 获取旧版本对应表的字段信息
            List<ColumnInfoPO> oldColumns = oldVersionTables.getOrDefault(tableName, new ArrayList<>());

            // 转换为列名到字段信息的映射，方便比较
            Map<String, ColumnInfoPO> oldColumnMap = new HashMap<>();
            for (ColumnInfoPO col : oldColumns) {
                oldColumnMap.put(col.getColumnName(), col);
            }

            Map<String, ColumnInfoPO> newColumnMap = new HashMap<>();
            for (ColumnInfoPO col : newColumns) {
                newColumnMap.put(col.getColumnName(), col);
            }

            // 比较字段差异
            for (ColumnInfoPO newCol : newColumns) {
                if (!oldColumnMap.containsKey(newCol.getColumnName())) {
                    differences.add("在表 " + tableName + " 中新增字段: " + newCol.getColumnName());
                } else {
                    ColumnInfoPO oldCol = oldColumnMap.get(newCol.getColumnName());
                    if (!Objects.equals(oldCol.getTypeName(), newCol.getTypeName()) ||
                            !Objects.equals(oldCol.getColumnSize(), newCol.getColumnSize()) ||
                            !Objects.equals(oldCol.getNullable(), newCol.getNullable()) ||
                            !Objects.equals(oldCol.getRemarks(), newCol.getRemarks())) {
                        differences.add("在表 " + tableName + " 中字段 " + newCol.getColumnName() + " 发生变更");
                    }
                }
            }

            // 查找删除的字段
            for (ColumnInfoPO oldCol : oldColumns) {
                if (!newColumnMap.containsKey(oldCol.getColumnName())) {
                    differences.add("在表 " + tableName + " 中删除字段: " + oldCol.getColumnName());
                }
            }
        }

        // 处理旧版本中存在但新版本中不存在的表
        for (String oldTableName : oldVersionTables.keySet()) {
            if (!newVersionTables.containsKey(oldTableName)) {
                differences.add("删除表: " + oldTableName);
            }
        }

        // 处理旧版本中存在但新版本中不存在的表
        for (String newTableName : newVersionTables.keySet()) {
            if (!oldVersionTables.containsKey(newVersionTables)) {
                differences.add("新增表: " + newTableName);
            }
        }
        // 记录操作
        OperationInfoDTO operationInfoDTO = new OperationInfoDTO();
        operationInfoDTO.setUserId(UserContext.getUserId());
        operationInfoDTO.setProjectId(versionCompareDTO.getProjectId());
        operationInfoDTO.setProjectName(versionCompareDTO.getProjectName());
        operationService.insertCompareVersionFieldDiff(operationInfoDTO);
        return differences;
    }

    /**
     * 插入当前数据库表结构
     */
    public String saveTableColumns(String projectId, String sourceId, String tableName, ColumnInfo columnInfo, String version) {
        ColumnInfoPO columnInfoPO = ColumnInfoMapstruct.INSTANCE.toColumnInfoPO(columnInfo);
        columnInfoPO.setTableName(tableName);
        columnInfoPO.setUpdateDate(new Date());
        columnInfoPO.setSourceId(sourceId);
        columnInfoPO.setVersion(version);
        columnInfoPO.setProjectId(projectId);
        columnInfoMapper.insert(columnInfoPO);
        System.out.println("当前插入的数据:" + columnInfoPO);
        return String.valueOf(columnInfoPO.getId());
    }

    private Connection getConnection(DatabaseConfig config) throws ClassNotFoundException, SQLException {
        // 加载驱动类
        Class.forName(config.getDatabaseDriver());

        // 创建连接
        return DriverManager.getConnection(
                config.getDatabaseUrl(),
                config.getDatabaseUser(),
                config.getDatabasePassword()
        );
    }

    public ProjectDatabaseDTO getDetail(String projectId) {
        return ProjectInfoMapstruct.INSTANCE.toProjectDatabaseDTO(projectInfoMapper.selectById(projectId));
    }
}
