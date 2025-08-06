package org.shuzhi.Mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.mapstruct.factory.Mappers;
import org.shuzhi.Dto.ColumnInfo;
import org.shuzhi.PO.ColumnInfoPO;

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
public interface ColumnInfoMapper extends BaseMapper<ColumnInfoPO> {

}
