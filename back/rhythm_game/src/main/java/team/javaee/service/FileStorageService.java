package team.javaee.service;

import org.springframework.web.multipart.MultipartFile;

public interface FileStorageService {
    /**
     * 上传文件到 R2
     * 
     * @param file   文件对象
     * @param prefix 路径前缀（例如 "covers/", "videos/"）
     * @return 文件的公开访问 URL
     */
    String uploadFile(MultipartFile file, String prefix);

    String uploadFile(byte[] content, String key, String contentType);

    /**
     * 删除 R2 中的文件
     * 
     * @param fileUrl 文件的公开访问 URL
     */
    void deleteFile(String fileUrl);
}
