package org.shuzhi.Mapstruct;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import org.shuzhi.Dto.SysOperationDTO;
import org.shuzhi.Dto.SysOperationFilterDTO;
import org.shuzhi.PO.SysOperationHistoryPO;

@Mapper
public interface SysOperationMapstruct {
    SysOperationMapstruct INSTANCE = Mappers.getMapper(SysOperationMapstruct.class);

    SysOperationDTO toSysOperationDTO(SysOperationHistoryPO sysOperationHistoryPO);
}
