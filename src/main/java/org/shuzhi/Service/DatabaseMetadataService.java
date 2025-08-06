package org.shuzhi.Service;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson2.JSONArray;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import okhttp3.*;
import org.shuzhi.Config.TableInfo;
import org.shuzhi.Dto.ColumnInfo;
import org.shuzhi.Dto.CreateProjectDTO;
import org.shuzhi.Dto.ProjectBaseDTO;
import org.shuzhi.Mapper.ColumnInfoMapper;
import org.shuzhi.Mapper.ProjectInfoMapper;
import org.shuzhi.Mapper.TableInfoMapper;
import org.shuzhi.Mapstruct.ColumnInfoMapstruct;
import org.shuzhi.Mapstruct.ProjectInfoMapstruct;
import org.shuzhi.Mapstruct.TableInfoMapstruct;
import org.shuzhi.PO.ColumnInfoPO;
import org.shuzhi.PO.ProjectPO;
import org.shuzhi.PO.TableInfoPO;
import org.shuzhi.Config.DatabaseConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Description;

import java.util.*;
import java.util.Date;
import java.util.function.Function;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.sql.*;
import java.sql.Connection;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class DatabaseMetadataService {
    private final TableInfoMapper tableInfoMapper;

    private final ProjectInfoMapper projectInfoMapper;

    private final ColumnInfoMapper columnInfoMapper;

    public DatabaseMetadataService(TableInfoMapper tableInfoMapper, ProjectInfoMapper projectInfoMapper, ColumnInfoMapper columnInfoMapper) {
        this.tableInfoMapper = tableInfoMapper;
        this.projectInfoMapper = projectInfoMapper;
        this.columnInfoMapper = columnInfoMapper;
    }

    public List<String> getTableNames(DatabaseConfig config) throws SQLException, ClassNotFoundException {
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

    @Bean
    @Description("创建项目")
    public Function<CreateProjectDTO, String> createProject() {
        return input -> {
            projectInfoMapper.insert(ProjectInfoMapstruct.INSTANCE.createToProjectPO(input));
            return input.getProjectName();
        };
    }

    @Bean
    @Description("查询项目列表")
    public Function<Object, List<ProjectBaseDTO>> getProjectList() {
        return input -> {
            List<ProjectPO> projectPOList = projectInfoMapper.selectList(new LambdaQueryWrapper<>());
            if (projectPOList.isEmpty()) {new ArrayList<>();}
            return ProjectInfoMapstruct.INSTANCE.toProjectDTOList(projectPOList);
        };
    }

    @Bean
    @Description("根据编号或名称查询数据库信息")
    public Function<ProjectBaseDTO, ProjectBaseDTO> getProjectDataBase() {
        return input -> ProjectInfoMapstruct.INSTANCE.toProjectDatabaseDTO(projectInfoMapper.selectOne(new LambdaQueryWrapper<ProjectPO>()
                .eq(!Objects.isNull(input.getId()), ProjectPO::getId, input.getId())
                .eq(!Objects.isNull(input.getProjectName()), ProjectPO::getProjectName, input.getProjectName()))
        );
    }

    @Bean
    @Description("根据编号或名称查询备份版本")
    public Function<Object, List<ProjectBaseDTO>> getProjectHistory() {
        return input -> ProjectInfoMapstruct.INSTANCE.toProjectDTOList(projectInfoMapper.selectList(new LambdaQueryWrapper<>()));
    }

    @Bean
    @Description("根据数据库查询表")
    public Function<DatabaseConfig, List<TableInfo>> getDataTableList() {
        return input -> {
            try {
                return this.getTableInfo(input);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        };
    }

    /**
     * 保存指定的数据库信息
     * @param
     * @return
     * @throws ClassNotFoundException
     */

    @Bean
    @Description("保存数据表字段信息")
    @Transactional(rollbackFor = Exception.class)
    public Function<DatabaseConfig, List<String>> backupData() {
        return input -> {
            // 返回内容
            List<String> result = new ArrayList<>();
            DatabaseConfig config = input;
            try (Connection connection = getConnection(config)) {
                DatabaseMetaData metaData = connection.getMetaData();

                List<TableInfo> tableInfos = new ArrayList<>();

                try (ResultSet tables = metaData.getTables(config.getDatabaseName(), null, null, new String[]{"TABLE"})) {
                    while (tables.next()) {
//                        TableInfoPO tableInfoPO = TableInfoMapstruct.INSTANCE.toTableInfoPO(tables);
                        TableInfoPO tableInfoPO = new TableInfoPO();

                        tableInfoPO.setTableName(tables.getString("TABLE_NAME"));
//                        tableInfoPO.set(tables.getString("TABLE_TYPE"));
                        tableInfoPO.setDescription(tables.getString("REMARKS"));
                        tableInfoPO.setCharset(tables.getString("TYPE_CAT"));
                        tableInfoPO.setSourceId(input.getId());
//                        tableInfoPO.setEngine(tables.getString("TYPE_SCHEM"));
//                        tableInfoPO.setTypeName(tables.getString("TYPE_NAME"));
//                        tableInfoPO.setSelfReferencingColName(tables.getString("SELF_REFERENCING_COL_NAME"));
//                        tableInfoPO.setRefGeneration(tables.getString("REF_GENERATION"));
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

//                                columns.add(column);
                                this.saveTableColumns(tableInfoPO.getId(), tableInfoPO.getTableName(), column);
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
            return result;
        };
    }

    /**
     * 插入当前数据库表结构
     */
    public String saveTableColumns(String sourceId,String tableName, ColumnInfo columnInfo) {
        ColumnInfoPO columnInfoPO = ColumnInfoMapstruct.INSTANCE.toColumnInfoPO(columnInfo);
        columnInfoPO.setTableName(tableName);
        columnInfoPO.setUpdateDate(new Date());
        columnInfoPO.setSourceId(sourceId);
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


    /**
     * curl --request POST \
     *   --url https://api.siliconflow.cn/v1/chat/completions \
     *   --header 'Authorization: Bearer <token>' \
     *   --header 'Content-Type: application/json' \
     *   --data '
     *   {
     *   "model": "Qwen/QwQ-32B",
     *   "messages": [
     *     {
     *       "role": "user",
     *       "content": "What opportunities and challenges will the Chinese large model industry face in 2025?"
     *     }
     *   ]
     * }'
     * @param columns
     */
    /**
     * 硅基流动
     * @param columns
     */
    private void sentToSiliconFlowLLM(List<ColumnInfo> columns) {
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(300, TimeUnit.SECONDS)   // 连接超时
                .readTimeout(300, TimeUnit.SECONDS)      // 读取超时
                .writeTimeout(300, TimeUnit.SECONDS)     // 写入超时
                .build();


        String baseContext = String.format("{\"model\": \"Qwen/QwQ-32B\",\"messages\": [{\"role\": \"user\",\"content\": \"你现在是一名专业的数据库运维工程师，你通过和上一个版本的数据表进行了比对，你发现了以下问题，当前是mysql模式，请根据以下内容生成修改语句,不需要解释思考过程，请直接给出语句,%s\"}]}", columns.stream()
                .map(ColumnInfo::getErrorRemarks)
                .collect(Collectors.joining(" ")));
        RequestBody requestBody = RequestBody.create(baseContext, MediaType.parse("application/json"));
        Request httpRequest = new Request.Builder()
                .url("https://api.siliconflow.cn/v1/chat/completions")
                .post(requestBody)
                .addHeader("Authorization", "Bearer " + "sk-mjuwxxmsunrdtadjqfwnxokdemogbgtcndfepkqxogpqpipq")
                .addHeader("Content-Type", "application/json")
                .build();

        System.out.println("开始请求" + LocalDateTime.now());
        // 4. 发送请求
        try (Response response = client.newCall(httpRequest).execute()) {
            if (!response.isSuccessful()) {
                throw new RuntimeException("请求失败: " + response.code() + " - " + response.body().string());
            }

            // 5. 解析响应
            String responseBody = response.body().string();
            System.out.println("请求内容: " + baseContext);
            System.out.println("响应结果: " + responseBody);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        System.out.println("结束请求" + LocalDateTime.now());
    }

    private void sentToOllamaLLM(List<ColumnInfo> columns) {
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(300, TimeUnit.SECONDS)   // 连接超时
                .readTimeout(300, TimeUnit.SECONDS)      // 读取超时
                .writeTimeout(300, TimeUnit.SECONDS)     // 写入超时
                .build();


        String baseContext = String.format("{\"model\": \"gemma3:12b\",\"messages\": [{\"role\": \"user\",\"content\": \"你现在是一名专业的数据库运维工程师，你通过和上一个版本的数据表进行了比对，你发现了以下问题，当前是mysql模式，请根据以下内容生成修改语句,不需要解释思考过程，请直接给出语句,%s\"}]}", columns.stream()
                .map(ColumnInfo::getErrorRemarks)
                .collect(Collectors.joining(" ")));
        RequestBody requestBody = RequestBody.create(baseContext, MediaType.parse("application/json"));
        Request httpRequest = new Request.Builder()
                .url("http://localhost:8080/api/chat/completions")
                .post(requestBody)
                .addHeader("Authorization", "Bearer " + "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpZCI6IjlmMjhhMGZmLWYzYTAtNDcyYy04NDBkLTkxZmM1ZTBkODNhMSJ9.OdIEo54N3ttQi9jf48JvzDes49RkiZeYkTQyFLWlhfg")
                .addHeader("Content-Type", "application/json")
                .build();

        System.out.println("开始请求" + LocalDateTime.now());
        // 4. 发送请求
        try (Response response = client.newCall(httpRequest).execute()) {
            if (!response.isSuccessful()) {
                throw new RuntimeException("请求失败: " + response.code() + " - " + response.body().string());
            }

            // 5. 解析响应
            String responseBody = response.body().string();
            System.out.println("请求内容: " + baseContext);
            System.out.println("响应结果: " + responseBody);
            System.out.println("返回内容" + JSONObject.parseObject(JSONObject.parseObject(JSONArray.parseArray(JSONObject.parseObject(responseBody).get("choices").toString()).get(0).toString()).get("message").toString()).get("content"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        System.out.println("结束请求" + LocalDateTime.now());
    }

}
