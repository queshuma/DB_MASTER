package org.shuzhi.library.Service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.Getter;
import lombok.Setter;
import okhttp3.*;
import org.apache.commons.lang3.StringUtils;
import org.shuzhi.library.Config.DatabaseConfig;
import org.shuzhi.library.MapStruct.TableInfoMapstruct;
import org.shuzhi.library.PO.TableInfoPO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.work.manager.work_manager_system.Utils.IdUtils;
import org.work.manager.work_manager_system.mapper.TableInfoMapper;

import java.io.IOException;
import java.sql.*;
import java.sql.Connection;
import java.util.*;
import java.util.Date;
import java.util.stream.Collectors;

@Service
public class DatabaseMetadataService {
    @Autowired
    private TableInfoMapper tableInfoMapper;

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
        }

        return tableNames;
    }

    public Map<String, List<ColumnInfo>> getTableColumns(DatabaseConfig config) throws SQLException, ClassNotFoundException {
        Map<String, List<ColumnInfo>> tableColumns = new HashMap<>();
        List<String> tableNames = getTableNames(config);

        try (Connection connection = getConnection(config)) {
            DatabaseMetaData metaData = connection.getMetaData();

            for (String tableName : tableNames) {
                List<ColumnInfo> columns = new ArrayList<>();

                // 获取列信息
                ResultSet columnsResult = metaData.getColumns(config.getDatabaseName(), null, tableName, null);

                while (columnsResult.next()) {
                    ColumnInfo column = new ColumnInfo();
                    column.setColumnName(columnsResult.getString("COLUMN_NAME"));
                    column.setTypeName(columnsResult.getString("TYPE_NAME"));
                    column.setColumnSize(columnsResult.getInt("COLUMN_SIZE"));
                    column.setNullable(columnsResult.getInt("NULLABLE") == DatabaseMetaData.columnNullable);
                    column.setRemarks(columnsResult.getString("REMARKS"));

                    columns.add(column);
                    saveTableColumns(tableName, column);
                }

                tableColumns.put(tableName, columns);
            }
        }
        return tableColumns;
    }

    @Async
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

                while (columnsResult.next()) {
                    ColumnInfo column = new ColumnInfo();
                    column.setColumnName(columnsResult.getString("COLUMN_NAME"));
                    column.setTypeName(columnsResult.getString("TYPE_NAME"));
                    column.setColumnSize(columnsResult.getInt("COLUMN_SIZE"));
                    column.setNullable(columnsResult.getInt("NULLABLE") == DatabaseMetaData.columnNullable);
                    column.setRemarks(columnsResult.getString("REMARKS"));

                    if (existColumn.stream().filter(t -> t.getColumnName().equals(column.getColumnName())).count() == 1) {
                        TableInfoPO tableInfoPO = existColumn.stream().filter(t -> t.getColumnName().equals(column.getColumnName())).collect(Collectors.toList()).get(0);
                        if (!tableInfoPO.getTypeName().equals(column.getTypeName())) {
                            column.setErrorRemarks(column.getErrorRemarks() + "类型有变化;调整为" + tableInfoPO.getTableName());
                        }
                        if (!tableInfoPO.getColumnSize().equals(column.getColumnSize())) {
                            column.setErrorRemarks(column.getErrorRemarks() + "长度有变化;调整为" + tableInfoPO.getColumnSize());
                        }
                        if (!tableInfoPO.getRemarks().equals(column.getRemarks())) {
                            column.setErrorRemarks(column.getErrorRemarks() + "备注有变化;调整为");
                        }
                        if (!tableInfoPO.getNullable().equals(column.getNullable())) {
                            column.setErrorRemarks(column.getErrorRemarks() + "是否为空有变化;调整为");
                        }
                        if (StringUtils.isBlank(column.getErrorRemarks())) {
                            break;
                        } else {
                            columns.add(column);
                        }
                    } else {
                        column.setErrorRemarks("新增字段;");
                        columns.add(column);
                    }
                }
                this.sentToLLM(columns);
                tableColumns.put(tableName, columns);
            }
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

    private Connection getConnection(DatabaseConfig config) throws SQLException, ClassNotFoundException {
        // 加载驱动类
        Class.forName(config.getDriverClassName());

        // 创建连接
        return DriverManager.getConnection(
                config.getUrl(),
                config.getUsername(),
                config.getPassword()
        );
    }

    @Setter
    @Getter
    public static class ColumnInfo {
        private String columnName;
        private String typeName;
        private Integer columnSize;
        private Boolean nullable;
        private String remarks;
        private String errorRemarks;
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

    private void sentToLLM(List<ColumnInfo> columns) {
        
        OkHttpClient client = new OkHttpClient();


        String baseContext = String.format("{\"model\": \"Qwen/QwQ-32B\",\"messages\": [{\"role\": \"user\",\"content\": \"%s\"}]}", columns.stream()
                .map(ColumnInfo::getErrorRemarks)
                .collect(Collectors.joining(" ")));
        RequestBody requestBody = RequestBody.create(baseContext, MediaType.parse("application/json"));
        Request httpRequest = new Request.Builder()
                .url("https://api.siliconflow.cn/v1/chat/completions")
                .post(requestBody)
                .addHeader("Authorization", "Bearer " + "sk-mjuwxxmsunrdtadjqfwnxokdemogbgtcndfepkqxogpqpipq")
                .addHeader("Content-Type", "application/json")
                .build();

        // 4. 发送请求
        try (Response response = client.newCall(httpRequest).execute()) {
            if (!response.isSuccessful()) {
                throw new RuntimeException("请求失败: " + response.code() + " - " + response.body().string());
            }

            // 5. 解析响应
            String responseBody = response.body().string();
            System.out.println("响应结果: " + responseBody);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
