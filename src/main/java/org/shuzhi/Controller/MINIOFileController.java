package org.shuzhi.Controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import okhttp3.Headers;
import org.shuzhi.Service.MINIOFileService;
import org.shuzhi.Service.RagFileService;
import org.shuzhi.Utils.PageDTO;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/file")
public class MINIOFileController {

    private final MINIOFileService minIOService;
    private final RagFileService ragFileService;

    @PostMapping("/uploadFile")
    public String uploadFile(@RequestBody MultipartFile file) throws Exception {
        return minIOService.uploadFile("db-master-bucket", file);
    }

    @PostMapping("/uploadAvatar")
    public String uploadAvatar(@RequestBody MultipartFile file) throws Exception {
        return minIOService.uploadAvatar("db-master-bucket", file);
    }

    @PostMapping("/uploadRagFile")
    public String uploadRagFile(@RequestBody MultipartFile file) throws Exception {
        return minIOService.uploadRagFile("db-master-rag-bucket", file);
    }

    @PostMapping("/getRagFileList")
    public IPage<RagFileService.RagFile> getRagFileList(@RequestBody PageDTO pageDTO) {
        return ragFileService.getRagFileList(pageDTO);
    }
}
