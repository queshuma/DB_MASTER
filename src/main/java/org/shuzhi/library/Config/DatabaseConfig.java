package org.shuzhi.library.Config;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class DatabaseConfig {
    private String url;
    private String username;
    private String password;
    private String driverClassName;
    private String databaseName;
}
