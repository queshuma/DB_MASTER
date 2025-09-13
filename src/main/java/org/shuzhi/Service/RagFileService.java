package org.shuzhi.Service;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.shuzhi.Config.MINIOConfig;
import org.shuzhi.Mapper.RagFileInfoMapper;
import org.shuzhi.PO.RagFileInfoPO;
import org.shuzhi.Utils.MinIOUtils;
import org.shuzhi.Utils.PageDTO;
import org.shuzhi.Utils.SysUserUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@RequiredArgsConstructor
@Service
public class RagFileService {

    private final SysUserUtils sysUserUtils;
    private final MINIOConfig minioConfig;
    private final RagFileInfoMapper ragFileInfoMapper;
    private final VectorStore vectorStore;
    private final MinIOUtils minIOUtils;

    Logger logger = LoggerFactory.getLogger(RagFileService.class);

    private final String MINIO_RAG_FILE_PATH_START = "/api/v1/buckets/db-master-rag-bucket/objects/download?preview=true&prefix=";

    private final String MINIO_AVATAR_FILE_PATH_START = "/api/v1/buckets/db-master-bucket/objects/download?preview=true&prefix=";

    public void uploadRagFile(String fileInitialName, String fileName, String type, String size) {
        RagFileInfoPO ragFileInfoPO = new RagFileInfoPO();
        ragFileInfoPO.setFileName(fileName);
        ragFileInfoPO.setFileInitialName(fileInitialName);
        ragFileInfoPO.setFilePath(this.getRagPreviewPath(fileName));
        ragFileInfoPO.setFileType(type);
        ragFileInfoPO.setFileSize(size);
        ragFileInfoPO.setSync(false);
        ragFileInfoPO.setCreatorName(sysUserUtils.getLoginUserInfo().getUsername());
        ragFileInfoPO.setCreatorId(sysUserUtils.getLoginUserInfo().getId());
        ragFileInfoMapper.insert(ragFileInfoPO);
    }

    public void syncRagFile(RagFile ragFile) throws Exception {
        try (PDDocument pdf = PDDocument.load(minIOUtils.getFileStream("db-master-rag-bucket", ragFile).getInputStream())){
            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(pdf);

            List<Document> documents = List.of(new Document(text));
            vectorStore.add(documents);
            RagFileInfoPO ragFileInfoPO = ragFileInfoMapper.selectById(ragFile.id);
            ragFileInfoPO.setSync(true);
            ragFileInfoPO.setSyncDate(new Date());
            ragFileInfoPO.setSyncCreatorId(sysUserUtils.getLoginUserInfo().getId());
            ragFileInfoPO.setSyncCreatorId(sysUserUtils.getLoginUserInfo().getUsername());
            ragFileInfoMapper.updateById(ragFileInfoPO);
            logger.info(ragFile.fileInitialName + "文件转入向量文件库成功");
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    public record RagFile(String id, String fileName, String fileInitialName, String fileType, String fileSize, String filePath, Boolean sync) {
    }

    public IPage<RagFile> getRagFileList(PageDTO pageDTO) {
        IPage<RagFileInfoPO> ragFileInfoPOPage = ragFileInfoMapper.selectPage(pageDTO.getPage(), new LambdaQueryWrapper<>());
        return ragFileInfoPOPage.convert(t -> {
            return new RagFile(
                    t.getId(),
                    t.getFileName(),
                    t.getFileInitialName(),
                    t.getFileType(),
                    t.getFileSize(),
                    t.getFilePath(),
                    t.getSync()
            );
        });
    }

    public String getPreviewPath(String fileName) {
        return minioConfig.getEndpoint().replace("9000", "9001") + MINIO_AVATAR_FILE_PATH_START + fileName;
    }

    public String getRagPreviewPath(String fileName) {
        return minioConfig.getEndpoint().replace("9000", "9001") + MINIO_RAG_FILE_PATH_START + fileName;
    }
}
