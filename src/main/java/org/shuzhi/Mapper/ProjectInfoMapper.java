package org.shuzhi.Mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
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
public interface ProjectInfoMapper extends BaseMapper<ProjectPO> {

}
