package org.shuzhi.Service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import io.minio.GetObjectArgs;
import io.minio.GetObjectResponse;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.apache.commons.compress.utils.IOUtils;
import org.shuzhi.Config.MINIOConfig;
import org.shuzhi.Utils.EncoderUtils;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.InputStream;
import java.util.Date;
import java.util.List;

@RequiredArgsConstructor
@Service
public class MINIOFileService {

    private final MinioClient minioClient;
    private final MINIOConfig minIOConfig;

    private final SysUserService sysUserService;
    private final RagFileService ragFileService;

    public String uploadFile(String bucket, MultipartFile multipartFile) throws Exception {
        return this.uploadFileHandle(bucket, multipartFile);
    }

    public String uploadAvatar(String bucket, MultipartFile file) throws Exception {
        String fileName = this.uploadFileHandle(minIOConfig.getBucketName(), file);
        sysUserService.uploadAvatar(fileName);
        return fileName;
    }

    public void getFileUrl(String bucket, String fileName, HttpServletResponse httpServletResponse) throws Exception {
        this.getFileUrlHandle(bucket, fileName, httpServletResponse);
    }

    public String uploadFileHandle(String bucket, MultipartFile multipartFile) throws Exception {
        String fileName = (new Date()).getTime() + multipartFile.getOriginalFilename().substring(multipartFile.getOriginalFilename().indexOf("."));
        try {
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(bucket)
                    .object(fileName)
                    .stream(multipartFile.getInputStream(), multipartFile.getInputStream().available(), -1)
                    .contentType(multipartFile.getContentType()).build());
            return fileName;
        } catch (Exception e) {
            throw new Exception(e);
        }
    }

    public void getFileUrlHandle(String bucket, String fileName, HttpServletResponse response) throws Exception {

        try (GetObjectResponse object = minioClient.getObject(
                GetObjectArgs.builder()
                        .bucket(bucket)
                        .object(fileName)
                        .build()
        )) {
            // 这里假设都是 jpg，可以根据实际情况动态判断类型
            response.setContentType("image/jpeg");
            // 将输入流写到响应输出流
            IOUtils.copy(object, response.getOutputStream());
            response.flushBuffer();
        }
    }

    /**
     * 上传rag文件
     * @param bucket
     * @param file
     * @return
     * @throws Exception
     */
    public String uploadRagFile(String bucket, MultipartFile file) throws Exception {
        String fileName = this.uploadFileHandle(bucket, file);
        ragFileService.uploadRagFile(file.getOriginalFilename(), fileName, file.getContentType(), String.valueOf(file.getSize()));
        return fileName;
    }
}
