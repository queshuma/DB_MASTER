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

    /**
     * 获取项目列表
     * @param projectFilterDTO
     * @return
     */
    @PostMapping("/getList")
    public IPage<ProjectBaseDTO> getProjectList(@RequestBody ProjectFilterDTO projectFilterDTO) {
        return metadataService.getProjectList(projectFilterDTO);
    }

    /**
     * 获取数据详情
     * @param projectId
     * @return
     */
    @GetMapping("/getDetail")
    public ProjectDatabaseDTO getDetail(@RequestParam String projectId) {
        return metadataService.getDetail(projectId);
    }

    /**
     * 测试项目连接
     * @param projectId
     * @return
     */
    @GetMapping("/connectCheck")
    public Boolean connectCheck(@RequestParam String projectId) throws SQLException, ClassNotFoundException {
        return metadataService.connectCheck(projectId);
    }

}
