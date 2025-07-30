package org.shuzhi.MapStruct;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;
import org.shuzhi.Service.DatabaseMetadataService;
import org.shuzhi.PO.TableInfoPO;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author chat2db
 * @since 2025-06-29
 */
@Mapper
public interface TableInfoMapstruct {
    TableInfoMapstruct INSTANCE = Mappers.getMapper(TableInfoMapstruct.class);

    @Mappings({
            @Mapping(target = "id", ignore = true),
    })
    TableInfoPO toTableInfoPO(DatabaseMetadataService.ColumnInfo columnInfo);
}
