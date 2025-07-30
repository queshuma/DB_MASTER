package org.shuzhi.Utils;

import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Date;

@Component
public class TableFillUtils {
    public static Object fillCreate(Object obj) {
        try {
            if (Arrays.stream(obj.getClass().getDeclaredFields()).map(t -> t.getName().equals("createName")).count() == 0 ) {
                throw new Exception("填充创建名称失败");
            }
            for (Field declaredField : obj.getClass().getDeclaredFields()) {
                declaredField.setAccessible(true);
                if (declaredField.getName().equals("createName")) {
                    declaredField.set(obj, "admin");
                }
            }
            return obj;
        }catch (Exception e) {

        }
        return obj;
    }

    public static Object fillCreateTime(Object obj) {
        try {
            if (Arrays.stream(obj.getClass().getDeclaredFields()).map(t -> t.getName().equals("createName")).count() == 0 ) {
                throw new Exception("填充创建时间失败");
            }
            for (Field declaredField : obj.getClass().getDeclaredFields()) {
                declaredField.setAccessible(true);
                if (declaredField.getName().equals("createTime")) {
                    declaredField.set(obj, new Date());
                }
            }
            return obj;
        } catch (Exception e) {

        }
        return obj;
    }
}
