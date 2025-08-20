package org.shuzhi.Mapstruct;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import org.shuzhi.Dto.UserBaseDTO;
import org.shuzhi.Dto.UserRegisterDTO;
import org.shuzhi.PO.SysUserInfoPO;

@Mapper
public interface SysUserInfoMapstruct {
    SysUserInfoMapstruct INSTANCE = Mappers.getMapper(SysUserInfoMapstruct.class);

    SysUserInfoPO toSysUserInfoPO(UserRegisterDTO userRegisterDTO);

    SysUserInfoPO toSysUserInfoPO(UserBaseDTO userBaseDTO);

    UserBaseDTO toUserBaseDTO(SysUserInfoPO sysUserInfoPO);
}
