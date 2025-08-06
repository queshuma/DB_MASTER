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

//    @Mappings({
//            @Mapping(target = "id", ignore = true),
//            @Mapping(target = "sourceId", ignore = true),
//            @Mapping(target = "tableName", expression = "java(tables.getString(\"TABLE_NAME\"))"),
//            @Mapping(target = "description", expression = "java(tables.getString(\"REMARKS\"))"),
//            @Mapping(target = "engine", expression = "java(tables.getString(\"ENGINE\"))"),
//            @Mapping(target = "charset", expression = "java(tables.getString(\"TABLE_CAT\"))"),
//            @Mapping(target = "collate", expression = "java(tables.getString(\"TABLE_SCHEM\"))"),
//            @Mapping(target = "privateKey", ignore = true),
//            @Mapping(target = "key", ignore = true),
//            @Mapping(target = "updateDate", expression = "java(new java.util.Date())"),
//            @Mapping(target = "version", ignore = true)
//    })
//    TableInfoPO toTableInfoPO(ResultSet tables);
}
