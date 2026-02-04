package team.javaee.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import team.javaee.entity.domain.Song;
import team.javaee.entity.domain.SongAsset;
import team.javaee.entity.domain.User;
import team.javaee.mapper.SongAssetMapper;
import team.javaee.mapper.SongMapper;
import team.javaee.mapper.UserMapper;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

@Slf4j
@Service
public class R2MigrationService {

    @Autowired
    private SongMapper songMapper;

    @Autowired
    private SongAssetMapper songAssetMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private FileStorageService fileStorageService;

    @Value("${web.upload-path}")
    private String uploadPath;

    public void migrateAll() {
        migrateSongs();
        migrateSongAssets();
        migrateUsers();
        migrateCharts();
    }

    private void migrateCharts() {
        File chartsFolder = new File(uploadPath + "charts/");
        if (!chartsFolder.exists())
            return;

        File[] files = chartsFolder.listFiles((dir, name) -> name.endsWith(".json"));
        if (files == null)
            return;

        for (File file : files) {
            try {
                byte[] content = Files.readAllBytes(file.toPath());
                String r2Url = fileStorageService.uploadFile(content, "charts/" + file.getName(), "application/json");
                log.info("谱面迁移成功: {} -> {}", file.getName(), r2Url);
            } catch (Exception e) {
                log.error("谱面迁移失败: " + file.getName(), e);
            }
        }
    }

    private void migrateSongs() {
        List<Song> songs = songMapper.selectList(null);
        for (Song song : songs) {
            song.setSongCover(processAndMigrate(song.getSongCover(), "covers"));
            song.setDefaultBackground(processAndMigrate(song.getDefaultBackground(), "backgrounds"));
            song.setSongUrl(processAndMigrate(song.getSongUrl(), "mp3"));
            songMapper.updateById(song);
        }
    }

    private void migrateSongAssets() {
        List<SongAsset> assets = songAssetMapper.selectList(null);
        for (SongAsset asset : assets) {
            asset.setUrl(processAndMigrate(asset.getUrl(), "assets"));
            songAssetMapper.updateById(asset);
        }
    }

    private void migrateUsers() {
        List<User> users = userMapper.selectList(null);
        for (User user : users) {
            String newIcon = processAndMigrate(user.getIcon(), "avatars");
            if (newIcon != null && !newIcon.equals(user.getIcon())) {
                user.setIcon(newIcon);
                userMapper.updateById(user);
            }
        }
    }

    private String processAndMigrate(String urlStr, String prefix) {
        if (urlStr == null || urlStr.isEmpty() || urlStr.contains("images.heycharlot.com")) {
            return urlStr;
        }

        // 如果是本地路径或旧的开发环境 URL，需要下载/读取并重新上传
        log.info("迁移资源: {} -> prefix: {}", urlStr, prefix);
        Path tempFile = null;
        try {
            if (urlStr.startsWith("http")) {
                tempFile = downloadFile(urlStr);
            } else {
                // 处理本地路径 (如 /image/user/xxx.png)
                String localPath = uploadPath + urlStr.replaceFirst("^/", "");
                File localFile = new File(localPath);
                if (localFile.exists()) {
                    tempFile = Files.createTempFile("migrate_local_", localFile.getName());
                    Files.copy(localFile.toPath(), tempFile, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                }
            }

            if (tempFile == null)
                return urlStr;

            // 构造一个模拟的 MultipartFile 进行迁移
            String fileName = urlStr.substring(urlStr.lastIndexOf("/") + 1);
            String contentType = Files.probeContentType(tempFile);
            if (contentType == null) {
                if (urlStr.endsWith(".wav") || urlStr.endsWith(".mp3"))
                    contentType = "audio/mpeg";
                else if (urlStr.endsWith(".jpg") || urlStr.endsWith(".jpeg"))
                    contentType = "image/jpeg";
                else if (urlStr.endsWith(".png"))
                    contentType = "image/png";
                else if (urlStr.endsWith(".mp4"))
                    contentType = "video/mp4";
            }

            final Path finalTempFile = tempFile;
            final String finalContentType = contentType;
            final String finalFileName = fileName;

            MultipartFile multipartFile = new MockMultipartFile(finalFileName, finalTempFile, finalContentType);

            String newUrl = fileStorageService.uploadFile(multipartFile, prefix);
            log.info("迁移成功: {} -> {}", urlStr, newUrl);
            return newUrl;
        } catch (Exception e) {
            log.error("迁移失败: " + urlStr, e);
            return urlStr;
        } finally {
            try {
                if (tempFile != null)
                    Files.deleteIfExists(tempFile);
            } catch (IOException e) {
                // ignore
            }
        }
    }

    private Path downloadFile(String urlStr) throws IOException {
        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setConnectTimeout(5000);
        conn.setReadTimeout(15000);

        if (conn.getResponseCode() != 200) {
            log.warn("无法下载文件: {}, 响应码: {}", urlStr, conn.getResponseCode());
            return null;
        }

        Path tempFile = Files.createTempFile("migrate_", "_" + urlStr.substring(urlStr.lastIndexOf("/") + 1));
        try (InputStream is = conn.getInputStream();
                OutputStream os = new FileOutputStream(tempFile.toFile())) {
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = is.read(buffer)) != -1) {
                os.write(buffer, 0, bytesRead);
            }
        }
        return tempFile;
    }

    // 内部简版模拟类，避免引入额外依赖
    private static class MockMultipartFile implements MultipartFile {
        private final String name;
        private final Path path;
        private final String contentType;

        public MockMultipartFile(String name, Path path, String contentType) {
            this.name = (name != null) ? name : "file";
            this.path = path;
            this.contentType = contentType;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public String getOriginalFilename() {
            return name;
        }

        @Override
        public String getContentType() {
            return contentType;
        }

        @Override
        public boolean isEmpty() {
            return false;
        }

        @Override
        public long getSize() {
            try {
                return Files.size(path);
            } catch (IOException e) {
                return 0;
            }
        }

        @Override
        public byte[] getBytes() throws IOException {
            return Files.readAllBytes(path);
        }

        @Override
        public InputStream getInputStream() throws IOException {
            return new FileInputStream(path.toFile());
        }

        @Override
        public void transferTo(java.io.File dest) throws IOException, IllegalStateException {
            Files.copy(path, dest.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        }
    }
}
