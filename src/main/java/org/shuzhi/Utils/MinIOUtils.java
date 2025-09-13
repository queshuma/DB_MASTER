package org.shuzhi.Utils;

import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import org.shuzhi.Service.RagFileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.stereotype.Component;

@Component
public class MinIOUtils {

    @Autowired
    private MinioClient minioClient;

    public InputStreamResource getFileStream(String bucket, RagFileService.RagFile file) throws Exception {
        return new InputStreamResource(
                minioClient.getObject(
                        GetObjectArgs.builder()
                                .bucket(bucket)
                                .object(file.fileName())
                                .build()
                )
        );
    }
}
