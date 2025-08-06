package org.shuzhi.Config;

import lombok.Getter;
import lombok.Setter;
import org.shuzhi.Dto.DataBaseInfo;

import java.io.Serializable;

@Setter
@Getter
public class DatabaseConfig extends DataBaseInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    private String id;

    /**
     * 版本号
     */
    private String version;
}
