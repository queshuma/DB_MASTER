package org.shuzhi.Utils;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import org.apache.ibatis.reflection.MetaObject;
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
public class TimeUtils implements MetaObjectHandler {

    public static Date getCurrentTime(){
        return new Date();
    }

    @Override
    public void insertFill(MetaObject metaObject) {
        // 自动填充创建时间字段
        Object createDate = getFieldValByName("createDate", metaObject);
        if (createDate == null) {
            this.strictInsertFill(metaObject, "createDate", Date.class, new Date());
        }
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        Object createDate = getFieldValByName("updateDate", metaObject);
        if (createDate == null) {
            this.strictInsertFill(metaObject, "updateDate", Date.class, new Date());
        }
    }
}
