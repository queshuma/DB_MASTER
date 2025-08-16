package org.shuzhi.Controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.Getter;
import org.shuzhi.Config.DatabaseConfig;
import org.shuzhi.Dto.ProjectBaseDTO;
import org.shuzhi.Dto.ProjectDatabaseDTO;
import org.shuzhi.Dto.ProjectFilterDTO;
import org.shuzhi.Service.DatabaseMetadataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.sql.SQLException;
import java.util.List;

@RestController
@RequestMapping("/project")
public class DatabaseMetadataController {
    @Autowired
    private DatabaseMetadataService metadataService;

    @PostMapping("/getList")
    public IPage<ProjectBaseDTO> getProjectList(@RequestBody ProjectFilterDTO projectFilterDTO) {
        return metadataService.getProjectList(projectFilterDTO);
    }

    @GetMapping("/getDetail")
    public ProjectDatabaseDTO getDetail(@RequestParam String projectId) {
        return metadataService.getDetail(projectId);
    }

}
