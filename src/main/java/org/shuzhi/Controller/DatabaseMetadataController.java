package org.shuzhi.Controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/database")
public class DatabaseMetadataController {
    @Value(value = "minio.endpoint")
    private String config;
//    @Autowired
//    private DatabaseMetadataService metadataService;

//    @PostMapping("/tables")
//    public List<String> getTables(@RequestBody DatabaseConfig config) throws SQLException, ClassNotFoundException {
//        return metadataService.getTableNames(config);
//    }
//
//    @PostMapping("/columns")
//    public Map<String, List<DatabaseMetadataService.ColumnInfo>> getColumns(@RequestBody DatabaseConfig config)
//            throws SQLException, ClassNotFoundException {
//        return metadataService.getTableColumns(config);
//    }
//
//    @PostMapping("/checkColumns")
//    public Map<String, List<DatabaseMetadataService.ColumnInfo>> checkColumns(@RequestBody DatabaseConfig config)
//            throws SQLException, ClassNotFoundException {
//        return metadataService.checkColumns(config);
//    }
}
