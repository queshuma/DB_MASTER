package org.shuzhi.Mapstruct;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;
import org.shuzhi.Dto.CreateProjectDTO;
import org.shuzhi.Dto.ProjectBaseDTO;
import org.shuzhi.PO.ProjectPO;

import java.util.List;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author chentao
 * @since 2025-06-29
 */
@Mapper
public interface ProjectInfoMapstruct {

    ProjectInfoMapstruct INSTANCE = Mappers.getMapper(ProjectInfoMapstruct.class);

    ProjectBaseDTO toProjectDatabaseDTO(ProjectPO projectPO);

    List<ProjectBaseDTO> toProjectDTOList(List<ProjectPO> projectPOList);

    ProjectPO toProjectPO(ProjectBaseDTO input);

    ProjectPO createToProjectPO(CreateProjectDTO input);
}
