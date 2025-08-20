package org.shuzhi.Controller;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import okhttp3.Headers;
import org.shuzhi.Service.MINIOFileService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/file")
public class MINIOFileController {

    private final MINIOFileService minIOService;

    @PostMapping("/uploadFile")
    public String uploadFile(@RequestBody MultipartFile file) throws Exception {
        return minIOService.uploadFile("db-master-bucket", file);
    }

    @PostMapping("/uploadAvatar")
    public String uploadAvatar(@RequestBody MultipartFile file) throws Exception {
        return minIOService.uploadAvatar("db-master-bucket", file);
    }
}
