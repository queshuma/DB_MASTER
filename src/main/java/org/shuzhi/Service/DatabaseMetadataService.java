package org.shuzhi.Service;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson2.JSONArray;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.Getter;
import lombok.Setter;
import okhttp3.*;
import org.shuzhi.Config.TableInfo;
import org.shuzhi.MapStruct.TableInfoMapstruct;
import org.shuzhi.Mapper.TableInfoMapper;
import org.shuzhi.PO.TableInfoPO;
import org.shuzhi.Utils.IdUtils;
import org.shuzhi.Config.DatabaseConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Description;

import java.net.SocketException;
import java.util.*;
import java.util.Date;
import java.util.function.Function;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.sql.*;
import java.sql.Connection;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class DatabaseMetadataService {
    private final TableInfoMapper tableInfoMapper;

    public DatabaseMetadataService(TableInfoMapper tableInfoMapper) {
        this.tableInfoMapper = tableInfoMapper;
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
    @Description("数据库列表")
    public Function<Object, List<DatabaseConfig>> getDatabaseList() {
        return input -> {
            DatabaseConfig config = new DatabaseConfig();
            config.setId(Integer.valueOf(1));
            config.setUrl("jdbc:mysql://49.232.61.41:3306/work_manager_sys?useUnicode=true&characterEncoding=utf-8&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Shanghai");;
            config.setUsername("root");
            config.setPassword("123456");
            config.setDriverClassName("com.mysql.cj.jdbc.Driver");
            return Arrays.asList(config);
        };
    }

    @Bean
    @Description("根据编号查询数据库")
    public Function<String, DatabaseConfig> getDatabaseConfig() {
        return input -> {
            DatabaseConfig config = new DatabaseConfig();
            config.setUrl("jdbc:mysql://49.232.61.41:3306/work_manager_sys?useUnicode=true&characterEncoding=utf-8&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Shanghai");
            config.setUsername("root");
            config.setPassword("Secure!2024");
            config.setDriverClassName("com.mysql.cj.jdbc.Driver");
            return config;
        };
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
     * @throws SQLException
     * @throws ClassNotFoundException
     */

    @Bean
    @Description("保存数据表字段信息")
    public Function<DatabaseConfig, Map<String, List<ColumnInfo>>> saveTableColumns() throws SQLException, ClassNotFoundException {
        return input -> {
            DatabaseConfig config = input;;
            Map<String, List<ColumnInfo>> tableColumns = new HashMap<>();
            List<String> tableNames;
            try {
                tableNames = this.getTableNames(config);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }

            try (Connection connection = getConnection(config)) {
                DatabaseMetaData metaData = connection.getMetaData();

                for (String tableName : tableNames) {
                    List<ColumnInfo> columns = new ArrayList<>();

                    // 获取列信息
                    try (ResultSet columnsResult = metaData.getColumns(config.getDatabaseName(), null, tableName, null)) {

                        while (columnsResult.next()) {
                            ColumnInfo column = new ColumnInfo();
                            column.setColumnName(columnsResult.getString("COLUMN_NAME"));
                            column.setTypeName(columnsResult.getString("TYPE_NAME"));
                            column.setColumnSize(columnsResult.getInt("COLUMN_SIZE"));
                            column.setNullable(columnsResult.getInt("NULLABLE") == DatabaseMetaData.columnNullable);
                            column.setRemarks(columnsResult.getString("REMARKS"));

                            columns.add(column);
                            this.saveTableColumns(tableName, column);
                        }
                    }

                    tableColumns.put(tableName, columns);
                }
            } catch (SQLException e) {
                throw new RuntimeException("保存表字段信息时发生错误", e);
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            return tableColumns;
        };
    }

    @Description("检查字段区别")
    public Map<String, List<ColumnInfo>> checkColumns(DatabaseConfig config) throws SQLException, ClassNotFoundException {
        Map<String, List<ColumnInfo>> tableColumns = new HashMap<>();
        List<String> tableNames = getTableNames(config);

        List<Integer> version = tableInfoMapper.selectObjs(new LambdaQueryWrapper<TableInfoPO>().select(TableInfoPO::getVersion));
        List<TableInfoPO> tableInfoPOList;
        if (version.isEmpty()) {
            return null;
        } else {
            tableInfoPOList = tableInfoMapper.selectList(new LambdaQueryWrapper<TableInfoPO>().eq(TableInfoPO::getVersion, version.get(0)));
        }


        try (Connection connection = getConnection(config)) {
            DatabaseMetaData metaData = connection.getMetaData();

            for (String tableName : tableNames) {
                List<ColumnInfo> columns = new ArrayList<>();

                // 获取列信息
                ResultSet columnsResult = metaData.getColumns(config.getDatabaseName(), null, tableName, null);
                List<TableInfoPO> existColumn  = tableInfoPOList.stream().filter(t -> t.getTableName().equals(tableName)).collect(Collectors.toList());

                ColumnInfo column = new ColumnInfo();
                column.setErrorRemarks(tableName + "表 以下字段有更新:");
                while (columnsResult.next()) {
                    column.setColumnName(columnsResult.getString("COLUMN_NAME"));
                    column.setTypeName(columnsResult.getString("TYPE_NAME"));
                    column.setColumnSize(columnsResult.getInt("COLUMN_SIZE"));
                    column.setNullable(columnsResult.getInt("NULLABLE") == DatabaseMetaData.columnNullable);
                    column.setRemarks(columnsResult.getString("REMARKS"));

                    if (existColumn.stream().filter(t -> t.getColumnName().equals(column.getColumnName())).count() == 1) {
                        TableInfoPO tableInfoPO = existColumn.stream().filter(t -> t.getColumnName().equals(column.getColumnName())).collect(Collectors.toList()).get(0);
                        if (!tableInfoPO.getTypeName().equals(column.getTypeName())) {
                            column.setErrorRemarks(column.getErrorRemarks() + " " + column.getColumnName() + " 字段'类型'有变化,调整为" + column.getTypeName());
                        }
                        if (!tableInfoPO.getColumnSize().equals(column.getColumnSize())) {
                            column.setErrorRemarks(column.getErrorRemarks() + " " + column.getColumnName() +  " 字段'长度'有变化,调整为" + column.getColumnSize());
                        }
                        if (!tableInfoPO.getRemarks().equals(column.getRemarks())) {
                            column.setErrorRemarks(column.getErrorRemarks() + " " + column.getColumnName() +  " 字段'备注'有变化,调整为" + column.getRemarks());
                        }
                        if (!tableInfoPO.getNullable().equals(column.getNullable())) {
                            column.setErrorRemarks(column.getErrorRemarks() + " " + column.getColumnName() +  " 字段'是否为空'有变化,调整为" + column.getNullable());
                        }
                        if (column.getErrorRemarks().equals(tableInfoPO.getColumnName() + "表 以下字段有更新:")) {
                            break;
                        } else {
                            columns.add(column);
                        }
                    } else {
                        column.setErrorRemarks("新增字段;");
                        columns.add(column);
                    }
                }
                tableColumns.put(tableName, columns);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return tableColumns;
    }

    /**
     * 插入当前数据库表结构
     */
    public String saveTableColumns(String tableName, ColumnInfo columnInfo) {
        TableInfoPO tableInfoPO = TableInfoMapstruct.INSTANCE.toTableInfoPO(columnInfo);
        tableInfoPO.setTableName(tableName);
        tableInfoPO.setUpdateDate(new Date());
        List<Integer> version = tableInfoMapper.selectObjs(new LambdaQueryWrapper<TableInfoPO>().eq(TableInfoPO::getTableName, tableName).select(TableInfoPO::getVersion));
        if (version.size() > 1) {
            tableInfoPO.setVersion(version.stream().sorted().collect(Collectors.toList()).get(0));
        } else {
            tableInfoPO.setVersion(1);
        }
        tableInfoPO.setId(IdUtils.createId());
        tableInfoMapper.insert(tableInfoPO);
        System.out.println("当前插入的数据:" + tableInfoPO);
        return String.valueOf(tableInfoPO.getId());
    }

    private Connection getConnection(DatabaseConfig config) throws Exception {
        // 加载驱动类
        Class.forName(config.getDriverClassName());

        try {
            // 创建连接
            return DriverManager.getConnection(
                    config.getUrl(),
                    config.getUsername(),
                    config.getPassword()
            );
        } catch (Exception e) {
            throw new RuntimeException("数据库连接失败，请检查数据库配置" + e);
        }
    }

    @Setter
    @Getter
    public static class ColumnInfo {
        private String columnName;
        private String typeName;
        private Integer columnSize;
        private Boolean nullable;
        private String remarks;
        private String errorRemarks = "";
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
