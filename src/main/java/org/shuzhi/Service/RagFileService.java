package org.shuzhi.Service;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.shuzhi.Config.MINIOConfig;
import org.shuzhi.Dto.DocumentParticiple;
import org.shuzhi.Mapper.RagFileInfoMapper;
import org.shuzhi.PO.RagFileInfoPO;
import org.shuzhi.Utils.MinIOUtils;
import org.shuzhi.Utils.PageDTO;
import org.shuzhi.Utils.SysUserUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Stream;

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

    // todo 数据库存一份
    // todo 向量数据库清除



    /**
     * 自动根据规则自动分词
     */
    public List<DocumentParticiple.Block> autoParticiple(String context) {
            // 按连续 2 个或更多换行符分割
            String[] paragraphs = context.split("(\\r?\\n){2,}");

            StringBuffer documentContext = new StringBuffer();
            DocumentParticiple document = new DocumentParticiple();
            List<DocumentParticiple.Block> blockList = new ArrayList<>();
            Integer index = 0;
            for (Integer i = 0 ;i < paragraphs.length; i++) {
                String para =  paragraphs[i];
                String clean = para.trim(); // 去掉首尾空白
                if (clean.endsWith("。") || clean.endsWith(".") || i == paragraphs.length - 1) {
                    documentContext.append(clean);
                    DocumentParticiple.Block block = new DocumentParticiple.Block();
                    block.setSortNum(index);
                    block.setContent(documentContext.toString());
                    blockList.add(block);
                    logger.info("段落 " + index + " 添加成功");
                    documentContext = new StringBuffer();
                    index = index + 1;
                } else {
                    if (StringUtils.isNotBlank(clean)) {
                        documentContext.append(clean);
                    }
                }
            }

        System.out.println("切分完毕，共 " + index + " 个句子");
            return blockList;
    }

    /**
     * 手动拆分段落回传
     */
    public void participleContent(DocumentParticiple document) {
        Map<String, Object> contextMap = new HashMap<>();
        contextMap.put("id", document.getId());
        contextMap.put("paragraphIndex", 0);
        List<Document> documentList = new ArrayList<>();
        Integer index = 0;
        for (DocumentParticiple.Block block : document.getBlocks()) {
            Document documentContent = new Document(block.getContent(), contextMap);
            documentList.add(documentContent);
            logger.info("段落 " + index + " 添加成功");
            index = index + 1;
            contextMap.put("paragraphIndex", index);
        }
        vectorStore.add(documentList);
        System.out.println("切分完毕，共 " + documentList.size() + " 个段落快");
    }

    @Async
    public void syncRagFile(RagFile ragFile) throws Exception {
        try (PDDocument pdf = PDDocument.load(minIOUtils.getFileStream("db-master-rag-bucket", ragFile.fileName).getInputStream());
             ){
//            PDFTextStripper stripper = new PDFTextStripper();
//            stripper.setSortByPosition(true); // 按位置读取，避免顺序乱
//            String text = stripper.getText(pdf);
//            SentenceDetectorME sentenceDetector = new SentenceDetectorME(new SentenceModel(modelIn));

            Map<String, Object> contextMap = new HashMap<>();
            contextMap.put("id", ragFile.id);
            contextMap.put("paragraphIndex", 0);

            PDFTextStripper stripper = new PDFTextStripper();

            // 启用位置排序（有助于还原段落）
            stripper.setSortByPosition(true);

            // 保留换行
            stripper.setAddMoreFormatting(true);

            // 设置换行符
            stripper.setLineSeparator("\n");
            stripper.setParagraphEnd("\n\n");
            String context = stripper.getText(pdf);
            // 按连续 2 个或更多换行符分割
            String[] paragraphs = context.split("(\\r?\\n){2,}");

            StringBuffer documentContext = new StringBuffer();
            DocumentParticiple document = new DocumentParticiple();
            document.setId(ragFile.id);
            List<DocumentParticiple.Block> blockList = new ArrayList<>();
            Integer index = 0;
            for (String para : paragraphs) {
                String clean = para.trim(); // 去掉首尾空白
                if (clean.endsWith("。") || clean.endsWith(".")) {
                    documentContext.append(clean);
                    DocumentParticiple.Block block = new DocumentParticiple.Block();
                    block.setSortNum(index);
                    block.setContent(documentContext.toString());
                    blockList.add(block);
                    logger.info("段落 " + index + " 添加成功");
                    index = index + 1;
                    contextMap.put("paragraphIndex", index);
                } else {
                    if (StringUtils.isNotBlank(clean)) {
                        documentContext.append(clean);
                    }
                }
            }
            this.participleContent(document);
            System.out.println("切分完毕，共 " + index + " 个句子");
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

    public String getRagFileContent(String fileName) {
        try (PDDocument pdf = PDDocument.load(minIOUtils.getFileStream("db-master-rag-bucket", fileName).getInputStream());
        ){
            PDFTextStripper stripper = new PDFTextStripper();
            // 启用位置排序（有助于还原段落）
            stripper.setSortByPosition(true);

            // 保留换行
            stripper.setAddMoreFormatting(true);

            // 设置换行符
            stripper.setLineSeparator("\n");
            stripper.setParagraphEnd("\n\n");
            return stripper.getText(pdf);
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            throw new RuntimeException(e);
        }
//        return null;
    }

    public record RagFile(String id, String fileName, String fileInitialName, String fileType, String fileSize, String filePath, Boolean sync) {
    }

    public IPage<RagFile> getRagFileList(PageDTO pageDTO) {
        IPage<RagFileInfoPO> ragFileInfoPOPage = ragFileInfoMapper.selectPage(pageDTO.getPage(), new LambdaQueryWrapper<>());
        return ragFileInfoPOPage.convert(t -> new RagFile(
                t.getId(),
                t.getFileName(),
                t.getFileInitialName(),
                t.getFileType(),
                t.getFileSize(),
                t.getFilePath(),
                t.getSync()
        ));
    }

    public String getPreviewPath(String fileName) {
        return minioConfig.getEndpoint().replace("9000", "9001") + MINIO_AVATAR_FILE_PATH_START + fileName;
    }

    public String getRagPreviewPath(String fileName) {
        return minioConfig.getEndpoint().replace("9000", "9001") + MINIO_RAG_FILE_PATH_START + fileName;
    }
}
