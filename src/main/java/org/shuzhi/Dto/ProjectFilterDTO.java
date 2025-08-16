package org.shuzhi.Dto;

import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.Data;
import org.shuzhi.Utils.PageDTO;

@Data
public class ProjectFilterDTO extends PageDTO {
    private String projectName;
}
