package org.shuzhi.Controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.conditions.update.LambdaUpdateChainWrapper;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import okhttp3.Headers;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.shuzhi.Dto.DocumentParticiple;
import org.shuzhi.Mapper.RagFileInfoMapper;
import org.shuzhi.PO.RagFileInfoPO;
import org.shuzhi.Service.MINIOFileService;
import org.shuzhi.Service.RagFileService;
import org.shuzhi.Utils.EncoderUtils;
import org.shuzhi.Utils.MinIOUtils;
import org.shuzhi.Utils.PageDTO;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/file")
public class MINIOFileController {

    private final MINIOFileService minIOService;
    private final RagFileService ragFileService;
    private final MinIOUtils minIOUtils;
    private final RagFileInfoMapper ragFileInfoMapper;

//    @PostMapping("/uploadFile")
//    public String uploadFile(@RequestBody MultipartFile file) throws Exception {
//        return minIOService.uploadFile("db-master-bucket", file);
//    }

    @PostMapping("/getRagFileList")
    public IPage<RagFileService.RagFile> getRagFileList(@RequestBody PageDTO pageDTO) {
        return ragFileService.getRagFileList(pageDTO);
    }

    @PostMapping("/uploadAvatar")
    public String uploadAvatar(@RequestBody MultipartFile file) throws Exception {
        return minIOService.uploadAvatar("db-master-bucket", file);
    }

    /**
     * 上传rag文件
     * @param file
     * @return
     * @throws Exception
     */
    @PostMapping("/uploadRagFile")
    public String uploadRagFile(@RequestBody MultipartFile file) throws Exception {
        return minIOService.uploadRagFile("db-master-rag-bucket", file);
    }

    /**
     * 预览RAG文件
     * @param
     * @return
     * @throws Exception
     */
    @GetMapping("/preview/url")
    public String getPreviewUrl(
            @RequestParam String fileName) throws Exception {
        String bucketName = "db-master-rag-bucket";
        String url = minIOService.getPreviewUrl(bucketName, fileName);
        return url;
    }

    /**
     * 下载 rag 文件
     * @param ragFile
     * @return
     * @throws Exception
     */
    @PostMapping("/downloadRagFile")
    public ResponseEntity<Resource> downloadRagFile(@RequestBody RagFileService.RagFile ragFile) throws Exception {
        InputStreamResource inputStreamResource = minIOUtils.getFileStream("db-master-rag-bucket", ragFile.fileName());

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename*=UTF-8''" + EncoderUtils.encode(ragFile.fileInitialName()))
                .body(inputStreamResource);
    }

    @PostMapping("/syncRagFile")
    public void syncRagFile(@RequestBody RagFileService.RagFile ragFile) throws Exception {
        ragFileService.syncRagFile(ragFile);
    }

    /**
     * 取消同步
     * @param documentId
     * @throws Exception
     */
    @GetMapping("/cancelSyncRagFile")
    public void cancelSyncRagFile(@RequestParam String documentId) throws Exception {
        ragFileService.cancelSyncRagFile(documentId);
    }

    /**
     * 保存发布
     * @param document
     */
    @PostMapping("/participleContent")
    public void participleContent(@RequestBody DocumentParticiple document) {
        ragFileService.participleContent(document);
    }

    /**
     * 自动分词
     * @param autoParticipleRequest
     * @return
     */
    @PostMapping("/autoParticiple")
    public List<DocumentParticiple.Block> participleContent(@RequestBody AutoParticipleRequest autoParticipleRequest) {
        return ragFileService.autoParticiple(autoParticipleRequest.context(), autoParticipleRequest.rule());
    }

    /**
     * 获取 rag 文件内容
     * @throws Exception
     */
    @GetMapping("/getRagFileContent")
    public String getRagFileContent(@RequestParam String fileName) throws Exception {
        return ragFileService.getRagFileContent(fileName);
    }

    public record AutoParticipleRequest(String context, String rule) {
    }
}
