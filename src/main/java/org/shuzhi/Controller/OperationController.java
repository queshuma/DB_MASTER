package org.shuzhi.Controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import org.shuzhi.Dto.SysOperationDTO;
import org.shuzhi.Dto.SysOperationFilterDTO;
import org.shuzhi.Service.EmailService;
import org.shuzhi.Service.OperationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/operation")
public class OperationController {
    @Autowired
    private OperationService operationService;

    @PostMapping("/getList")
    public IPage<SysOperationDTO> getList(@RequestBody SysOperationFilterDTO sysOperationFilterDTO) {
        return operationService.getList(sysOperationFilterDTO);
    }
}
