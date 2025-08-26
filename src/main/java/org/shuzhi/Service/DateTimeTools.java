package org.shuzhi.Service;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class DateTimeTools {
    @Tool(description = "获取时间")
    private String getCurrentDateTime() {
        System.out.println("获取时间");
        System.out.println("开始请求" + LocalDateTime.now().atZone(LocaleContextHolder.getTimeZone().toZoneId()).toString());
        return LocalDateTime.now().atZone(LocaleContextHolder.getTimeZone().toZoneId()).toString();
    }
}
