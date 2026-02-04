package team.javaee.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import team.javaee.service.FileStorageService;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
public class FileStorageServiceImpl implements FileStorageService {

    @Autowired
    private S3Client s3Client;

    @Value("${cloudflare.r2.bucket-name}")
    private String bucketName;

    @Value("${cloudflare.r2.public-url}")
    private String publicUrl;

    private static final String R2_ROOT_FOLDER = "ephemelody/";

    @Override
    public String uploadFile(MultipartFile file, String prefix) {
        String contentType = file.getContentType();
        String originalFilename = file.getOriginalFilename();

        // 强制使用 ephemelody/ 前缀
        String fullPrefix = R2_ROOT_FOLDER + (prefix.endsWith("/") ? prefix : prefix + "/");
        String uuidName = UUID.randomUUID().toString();

        Path tempOriginal = null;
        Path tempProcessed = null;

        try {
            tempOriginal = Files.createTempFile("original_", originalFilename);
            file.transferTo(tempOriginal.toFile());

            String finalKey;
            String finalContentType;
            Path fileToUpload;

            if (contentType != null && contentType.startsWith("image/") && !contentType.contains("svg")) {
                // 压缩图片为 WebP
                tempProcessed = Files.createTempFile("processed_", ".webp");
                compressImageToWebP(tempOriginal, tempProcessed);
                fileToUpload = tempProcessed;
                finalKey = fullPrefix + uuidName + ".webp";
                finalContentType = "image/webp";
            } else if (contentType != null && contentType.startsWith("video/")) {
                // 压缩视频为 MP4 (H.264)
                tempProcessed = Files.createTempFile("processed_", ".mp4");
                compressVideo(tempOriginal, tempProcessed);
                fileToUpload = tempProcessed;
                finalKey = fullPrefix + uuidName + ".mp4";
                finalContentType = "video/mp4";
            } else {
                // 其他文件直接上传
                fileToUpload = tempOriginal;
                String extension = "";
                if (originalFilename != null && originalFilename.contains(".")) {
                    extension = originalFilename.substring(originalFilename.lastIndexOf("."));
                }
                finalKey = fullPrefix + uuidName + extension;
                finalContentType = contentType;
            }

            // 执行上传
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(finalKey)
                    .contentType(finalContentType)
                    .build();

            s3Client.putObject(putObjectRequest, RequestBody.fromFile(fileToUpload));

            String fileUrl = publicUrl + (publicUrl.endsWith("/") ? "" : "/") + finalKey;
            log.info("文件成功上传至 R2: {}", fileUrl);
            return fileUrl;

        } catch (Exception e) {
            log.error("文件处理或上传失败", e);
            throw new RuntimeException("文件处理或上传到 R2 失败: " + e.getMessage(), e);
        } finally {
            // 清理临时文件
            try {
                if (tempOriginal != null)
                    Files.deleteIfExists(tempOriginal);
                if (tempProcessed != null)
                    Files.deleteIfExists(tempProcessed);
            } catch (IOException e) {
                log.warn("无法删除临时文件", e);
            }
        }
    }

    @Override
    public String uploadFile(byte[] content, String key, String contentType) {
        String finalKey = R2_ROOT_FOLDER + key;
        try {
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(finalKey)
                    .contentType(contentType)
                    .build();

            s3Client.putObject(putObjectRequest, RequestBody.fromBytes(content));

            String fileUrl = publicUrl + (publicUrl.endsWith("/") ? "" : "/") + finalKey;
            log.info("字节内容成功上传至 R2: {}", fileUrl);
            return fileUrl;
        } catch (Exception e) {
            log.error("字节内容上传失败", e);
            throw new RuntimeException("字节内容上传到 R2 失败: " + e.getMessage(), e);
        }
    }

    private void compressImageToWebP(Path input, Path output) throws IOException, InterruptedException {
        ProcessBuilder pb = new ProcessBuilder(
                "cwebp", "-q", "75", input.toString(), "-o", output.toString());
        runProcess(pb, "图片压缩 (cwebp)");
    }

    private void compressVideo(Path input, Path output) throws IOException, InterruptedException {
        // 使用 ffmpeg 进行视频压缩，限制为 1080p 并在速度和质量间取得平衡
        ProcessBuilder pb = new ProcessBuilder(
                "ffmpeg", "-y", "-i", input.toString(),
                "-vcodec", "libx264", "-crf", "28", "-preset", "faster",
                "-vf", "scale=-2:1080", // 限制高度 1080，宽度自动且为偶数
                "-acodec", "aac", "-b:a", "128k",
                output.toString());
        runProcess(pb, "视频压缩 (ffmpeg)");
    }

    private void runProcess(ProcessBuilder pb, String taskName) throws IOException, InterruptedException {
        Process process = pb.start();
        int exitCode = process.waitFor();
        if (exitCode != 0) {
            String errorOutput;
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
                errorOutput = reader.lines().collect(Collectors.joining("\n"));
            }
            throw new RuntimeException(taskName + " 失败，错误码: " + exitCode + ", 详情: " + errorOutput);
        }
    }

    @Override
    public void deleteFile(String fileUrl) {
        if (fileUrl == null || !fileUrl.contains(bucketName) && !fileUrl.startsWith(publicUrl)) {
            return;
        }

        // 提取 Key (处理域名前缀)
        String key;
        if (fileUrl.startsWith(publicUrl)) {
            key = fileUrl.replace(publicUrl, "");
        } else {
            // 这里可能需要更复杂的逻辑来处理其他可能的 URL 格式
            key = fileUrl.substring(fileUrl.indexOf(R2_ROOT_FOLDER));
        }

        if (key.startsWith("/")) {
            key = key.substring(1);
        }

        try {
            DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build();

            s3Client.deleteObject(deleteObjectRequest);
            log.info("从 R2 成功删除对象: {}", key);
        } catch (Exception e) {
            log.error("从 R2 删除对象失败: {}", key, e);
        }
    }
}
