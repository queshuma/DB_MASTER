package org.shuzhi.Utils;

import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.util.UUID;

/**
 * Description:
 * Author: chentao
 * Date: 11 2月 2025
 *
 * @version 1.0
 */
@Component
public class IdUtils {

    public static String createId(){
        return String.valueOf(UUID.randomUUID());
    }

    public static Object setDefaultId(Object obj) {
        try{
            for (Field declaredField : obj.getClass().getDeclaredFields()) {
                if (declaredField.getName().equals("id")) {
                    declaredField.setAccessible(true);
                    declaredField.set(obj, null);
                    break;
                }
            }
            return obj;
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
