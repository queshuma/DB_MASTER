package org.shuzhi.Mapstruct;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;
import org.shuzhi.PO.TableInfoPO;

import java.sql.ResultSet;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author chentao
 * @since 2025-06-29
 */
@Mapper
public interface TableInfoMapstruct {

    TableInfoMapstruct INSTANCE = Mappers.getMapper(TableInfoMapstruct.class);
}
