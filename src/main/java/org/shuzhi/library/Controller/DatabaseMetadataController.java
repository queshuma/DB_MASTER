package org.shuzhi.library.Controller;

import org.shuzhi.library.Config.DatabaseConfig;
import org.shuzhi.library.Service.DatabaseMetadataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/database")
public class DatabaseMetadataController {
    @Autowired
    private DatabaseMetadataService metadataService;

    @PostMapping("/tables")
    public List<String> getTables(@RequestBody DatabaseConfig config) throws SQLException, ClassNotFoundException {
        return metadataService.getTableNames(config);
    }

    @PostMapping("/columns")
    public Map<String, List<DatabaseMetadataService.ColumnInfo>> getColumns(@RequestBody DatabaseConfig config)
            throws SQLException, ClassNotFoundException {
        return metadataService.getTableColumns(config);
    }

    @PostMapping("/checkColumns")
    public Map<String, List<DatabaseMetadataService.ColumnInfo>> checkColumns(@RequestBody DatabaseConfig config)
            throws SQLException, ClassNotFoundException {
        return metadataService.checkColumns(config);
    }
}
