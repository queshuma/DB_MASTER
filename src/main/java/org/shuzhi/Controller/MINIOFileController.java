package org.shuzhi.Controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import okhttp3.Headers;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
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
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/file")
public class MINIOFileController {

    private final MINIOFileService minIOService;
    private final RagFileService ragFileService;
    private final MinIOUtils minIOUtils;

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

    @PostMapping("/uploadRagFile")
    public String uploadRagFile(@RequestBody MultipartFile file) throws Exception {
        return minIOService.uploadRagFile("db-master-rag-bucket", file);
    }

    @PostMapping("/downloadRagFile")
    public ResponseEntity<Resource> downloadRagFile(@RequestBody RagFileService.RagFile ragFile) throws Exception {
        InputStreamResource inputStreamResource = minIOUtils.getFileStream("db-master-rag-bucket", ragFile);

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

}
