package org.shuzhi.library.Utils;

import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * Description:
 * Author: chentao
 * Date: 11 2月 2025
 *
 * @version 1.0
 */
@Component
public class TimeUtils {

    public static Date getCurrentTime(){
        return new Date();
    }
}
