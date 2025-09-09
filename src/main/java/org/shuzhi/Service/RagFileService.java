package org.shuzhi.Service;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import org.shuzhi.Config.MINIOConfig;
import org.shuzhi.Mapper.RagFileInfoMapper;
import org.shuzhi.PO.RagFileInfoPO;
import org.shuzhi.Utils.PageDTO;
import org.shuzhi.Utils.SysUserUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Service;
import org.stringtemplate.v4.ST;

import java.util.List;

@Service
public class RagFileService {

    @Autowired
    private SysUserUtils sysUserUtils;
    @Autowired
    private MINIOConfig minioConfig;

    @Resource
    private RagFileInfoMapper ragFileInfoMapper;

    private final String MINIO_FILE_PATH_START = "/api/v1/buckets/db-master-bucket/objects/download?preview=true&prefix=";


    public void uploadRagFile(String fileName, String type) {
        RagFileInfoPO ragFileInfoPO = new RagFileInfoPO();
        ragFileInfoPO.setFileName(fileName);
        ragFileInfoPO.setFilePath(this.getPreviewPath(fileName));
        ragFileInfoPO.setFileType(type);
        ragFileInfoPO.setSync(false);
        ragFileInfoPO.setCreatorName(sysUserUtils.getLoginUserInfo().getUsername());
        ragFileInfoPO.setCreatorId(sysUserUtils.getLoginUserInfo().getId());
        ragFileInfoMapper.insert(ragFileInfoPO);
    }

    public record RagFile(String id, String fileName, String fileType, String fileSize, String filePath) {
    }

    public IPage<RagFile> getRagFileList(PageDTO pageDTO) {
        IPage<RagFileInfoPO> ragFileInfoPOPage = ragFileInfoMapper.selectPage(pageDTO.getPage(), new LambdaQueryWrapper<>());
        return ragFileInfoPOPage.convert(t -> {
            return new RagFile(
                    t.getId(),
                    t.getFileName(),
                    t.getFileType(),
                    t.getFileSize(),
                    t.getFilePath()
            );
        });
    }

    public String getPreviewPath(String fileName) {
        return minioConfig.getEndpoint().replace("9000", "9001") + MINIO_FILE_PATH_START + fileName;
    }
}
