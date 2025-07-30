package org.shuzhi.Config;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Setter
@Getter
public class DatabaseConfig implements Serializable {
    private Integer id;
    private String url;
    private String username;
    private String password;
    private String driverClassName;
    private String databaseName;
}
