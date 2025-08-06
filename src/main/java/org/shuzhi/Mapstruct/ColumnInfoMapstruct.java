package org.shuzhi.Mapstruct;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import org.shuzhi.Dto.ColumnInfo;
import org.shuzhi.PO.ColumnInfoPO;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author chentao
 * @since 2025-06-29
 */
@Mapper
public interface ColumnInfoMapstruct {

    ColumnInfoMapstruct INSTANCE = Mappers.getMapper(ColumnInfoMapstruct.class);

    ColumnInfoPO toColumnInfoPO(ColumnInfo columnInfo);
}
