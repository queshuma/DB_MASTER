package org.shuzhi.library.MapStruct;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.shuzhi.library.Service.DatabaseMetadataService;
import org.shuzhi.library.PO.TableInfoPO;

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
    TableInfoMapstruct INSTANCE = org.mapstruct.factory.Mappers.getMapper(TableInfoMapstruct.class);

    @Mappings({
            @Mapping(target = "id", ignore = true),
    })
    TableInfoPO toTableInfoPO(DatabaseMetadataService.ColumnInfo columnInfo);
}
