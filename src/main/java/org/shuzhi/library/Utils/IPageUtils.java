package org.shuzhi.library.Utils;

import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springframework.stereotype.Component;

@Component
public class IPageUtils {
    /**
     * 分页方法基本属性拷贝
     * @param sourceData
     * @param targetData
     * @return
     */
    public static IPage<?> copyIPageAttribute(IPage<?> sourceData, IPage<?> targetData) {
        targetData.setCurrent(sourceData.getCurrent());
        targetData.setSize(sourceData.getSize());
        targetData.setTotal(sourceData.getTotal());
        return targetData;
    }
}
