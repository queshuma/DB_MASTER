package org.shuzhi.Config;

import lombok.Data;
import org.shuzhi.Dto.DataBaseInfo;

import java.io.Serializable;

@Data
public class DatabaseConfig extends DataBaseInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    private String id;

    private String projectName;

    /**
     * 版本号
     */
    private String version;
}
