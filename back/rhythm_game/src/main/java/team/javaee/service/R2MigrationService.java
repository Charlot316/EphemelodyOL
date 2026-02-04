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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

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

    @Value("${cloudflare.r2.public-url}")
    private String publicUrl;

    public void migrateAll() {
        log.info("开始多线程全量迁移任务 (优化本地路径查找)...");
        ExecutorService executor = Executors.newFixedThreadPool(10); // 10线程并发

        executor.execute(this::migrateSongs);
        executor.execute(this::migrateSongAssets);
        executor.execute(this::migrateUsers);
        executor.execute(this::migrateCharts);

        executor.shutdown();
        try {
            if (!executor.awaitTermination(60, TimeUnit.MINUTES)) {
                log.warn("迁移任务超时，部分任务仍在后台运行。");
            } else {
                log.info("所有迁移任务已完成。");
            }
        } catch (InterruptedException e) {
            log.error("迁移线程池被中断", e);
        }
    }

    private void migrateCharts() {
        log.info("开始迁移谱面 JSON...");
        java.util.Map<String, String> oldToNew = new java.util.HashMap<>();
        oldToNew.put("4", "9b52ceee-01bf-11f1-a4ca-20296da6fcfc");
        oldToNew.put("6", "9b52d18c-01bf-11f1-a4ca-20296da6fcfc");
        oldToNew.put("7", "9b52d25e-01bf-11f1-a4ca-20296da6fcfc");
        oldToNew.put("8", "9b52d2c2-01bf-11f1-a4ca-20296da6fcfc");
        oldToNew.put("9", "9b52d312-01bf-11f1-a4ca-20296da6fcfc");
        oldToNew.put("10", "9b52d36c-01bf-11f1-a4ca-20296da6fcfc");
        oldToNew.put("11", "9b52d3bc-01bf-11f1-a4ca-20296da6fcfc");
        oldToNew.put("12", "9b52d40c-01bf-11f1-a4ca-20296da6fcfc");
        oldToNew.put("13", "9b52d45c-01bf-11f1-a4ca-20296da6fcfc");

        File chartsFolder = new File(uploadPath + "charts/");
        if (!chartsFolder.exists())
            return;

        for (java.util.Map.Entry<String, String> entry : oldToNew.entrySet()) {
            File file = new File(chartsFolder, entry.getKey() + ".json");
            if (file.exists()) {
                try {
                    byte[] content = Files.readAllBytes(file.toPath());
                    String r2Url = fileStorageService.uploadFile(content, "charts/" + entry.getValue() + ".json",
                            "application/json");
                    log.info("【谱面】迁移成功 (UUID 映射): {} -> {}", file.getName(), r2Url);
                } catch (Exception e) {
                    log.error("【谱面】迁移失败: " + file.getName(), e);
                }
            }
        }
    }

    private void migrateSongs() {
        List<Song> songs = songMapper.selectList(null);
        for (Song song : songs) {
            try {
                String newCover = processAndMigrate(song.getSongCover(), "covers", song.getId());
                String newBg = processAndMigrate(song.getDefaultBackground(), "backgrounds", song.getId());
                String newUrl = processAndMigrate(song.getSongUrl(), "mp3", song.getId());

                songMapper.update(null, new com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<Song>()
                        .set(Song::getSongCover, newCover)
                        .set(Song::getDefaultBackground, newBg)
                        .set(Song::getSongUrl, newUrl)
                        .eq(Song::getId, song.getId()));
            } catch (Exception e) {
                log.error("歌曲记录更新失败: " + song.getSongName(), e);
            }
        }
    }

    private void migrateSongAssets() {
        List<SongAsset> assets = songAssetMapper.selectList(null);
        for (SongAsset asset : assets) {
            asset.setUrl(processAndMigrate(asset.getUrl(), "assets", asset.getSongId()));
            songAssetMapper.updateById(asset);
        }
    }

    private void migrateUsers() {
        List<User> users = userMapper.selectList(null);
        for (User user : users) {
            try {
                String newIcon = processAndMigrate(user.getIcon(), "avatars", null);
                if (newIcon != null && !newIcon.equals(user.getIcon())) {
                    userMapper.update(null,
                            new com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<User>()
                                    .set(User::getIcon, newIcon)
                                    .eq(User::getUserId, user.getUserId()));
                }
            } catch (Exception e) {
                log.error("用户记录更新失败: " + user.getUsername(), e);
            }
        }
    }

    private String processAndMigrate(String urlStr, String prefix, String songId) {
        if (urlStr == null || urlStr.isEmpty()) {
            return urlStr;
        }

        String publicHost = "images.heycharlot.com";

        // 如果已经是 R2 地址，检查是否 404
        if (urlStr.contains(publicHost)) {
            if (fileStorageService.exists(urlStr)) {
                return urlStr; // 资源存在，跳过
            }
            log.warn("发现 R2 资源丢失 (404)，尝试从本地找回: {}", urlStr);
        }

        Path tempFile = null;
        try {
            String fileName = urlStr.substring(urlStr.lastIndexOf("/") + 1);
            if (fileName.contains("?"))
                fileName = fileName.substring(0, fileName.indexOf("?"));

            // 增强的本地查找逻辑
            File localFile = findLocalFileRobust(urlStr, prefix, songId);

            if (localFile != null && localFile.exists()) {
                log.info("【本地读取】找到资源: {} -> {}", fileName, localFile.getAbsolutePath());
                tempFile = Files.createTempFile("migrate_local_", fileName);
                Files.copy(localFile.toPath(), tempFile, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            } else if (urlStr.startsWith("http") && !urlStr.contains(publicHost)) {
                log.info("【网络下载】正在从远程获取: {}", urlStr);
                tempFile = downloadFile(urlStr);
            }

            if (tempFile == null) {
                log.error("无法定位资源 Source: {} (Song: {})", urlStr, songId);
                return urlStr;
            }

            String contentType = Files.probeContentType(tempFile);
            if (contentType == null || contentType.equals("application/octet-stream")) {
                String fn = tempFile.getFileName().toString().toLowerCase();
                if (fn.endsWith(".wav"))
                    contentType = "audio/wav";
                else if (fn.endsWith(".mp3"))
                    contentType = "audio/mpeg";
                else if (fn.endsWith(".m4a"))
                    contentType = "audio/mp4";
                else if (fn.endsWith(".jpg") || fn.endsWith(".jpeg"))
                    contentType = "image/jpeg";
                else if (fn.endsWith(".png"))
                    contentType = "image/png";
                else if (fn.endsWith(".mp4"))
                    contentType = "video/mp4";
                else if (fn.endsWith(".webp"))
                    contentType = "image/webp";
            }

            MultipartFile multipartFile = new MockMultipartFile(fileName, tempFile, contentType);
            String newUrl = fileStorageService.uploadFile(multipartFile, prefix);
            log.info("【迁移/修复成功】{} -> {}", urlStr, newUrl);
            return newUrl;
        } catch (Exception e) {
            log.error("【迁移失败】" + urlStr + " | 错误: " + e.getMessage());
            return urlStr;
        } finally {
            try {
                if (tempFile != null)
                    Files.deleteIfExists(tempFile);
            } catch (IOException ignored) {
            }
        }
    }

    private File findLocalFileRobust(String originalUrl, String prefix, String songId) {
        // 基本查找
        String fileName = originalUrl.substring(originalUrl.lastIndexOf("/") + 1);
        File f = findLocalFile(fileName, originalUrl);
        if (f != null)
            return f;

        // 特定映射查找 (针对 R2 丢失的情况)
        if (songId != null) {
            java.util.Map<String, String> idToName = new java.util.HashMap<>();
            idToName.put("9b52ceee-01bf-11f1-a4ca-20296da6fcfc", "netsuai");
            idToName.put("9b52d18c-01bf-11f1-a4ca-20296da6fcfc", "world-execute-me");
            idToName.put("9b52d25e-01bf-11f1-a4ca-20296da6fcfc", "aegle");
            idToName.put("9b52d2c2-01bf-11f1-a4ca-20296da6fcfc", "koinouta");
            idToName.put("9b52d312-01bf-11f1-a4ca-20296da6fcfc", "yts");

            java.util.Map<String, String> newToOld = new java.util.HashMap<>();
            newToOld.put("9b52ceee-01bf-11f1-a4ca-20296da6fcfc", "4");
            newToOld.put("9b52d18c-01bf-11f1-a4ca-20296da6fcfc", "6");
            newToOld.put("9b52d25e-01bf-11f1-a4ca-20296da6fcfc", "7");
            newToOld.put("9b52d2c2-01bf-11f1-a4ca-20296da6fcfc", "8");
            newToOld.put("9b52d312-01bf-11f1-a4ca-20296da6fcfc", "9");
            newToOld.put("9b52d36c-01bf-11f1-a4ca-20296da6fcfc", "10");
            newToOld.put("9b52d3bc-01bf-11f1-a4ca-20296da6fcfc", "11");
            newToOld.put("9b52d40c-01bf-11f1-a4ca-20296da6fcfc", "12");
            newToOld.put("9b52d45c-01bf-11f1-a4ca-20296da6fcfc", "13");

            String shortName = idToName.get(songId);
            String oldId = newToOld.get(songId);

            String[] candidates = null;
            if ("covers".equals(prefix)) {
                candidates = new String[] { shortName + "-cover.jpg", oldId + ".jpg", shortName + ".jpg" };
            } else if ("backgrounds".equals(prefix)) {
                candidates = new String[] { shortName + "-0.jpg", shortName + "-0.PNG", oldId + ".jpg" };
            } else if ("mp3".equals(prefix)) {
                candidates = new String[] { oldId + ".wav", oldId + ".mp3", oldId + ".m4a" };
            }

            if (candidates != null) {
                for (String c : candidates) {
                    File found = findLocalFile(c, c);
                    if (found != null)
                        return found;
                }
            }
        }
        return null;
    }

    private File findLocalFile(String fileName, String originalUrl) {
        // 1. 尝试在 data 目录找
        File f1 = new File(uploadPath + fileName);
        if (f1.exists())
            return f1;

        // 2. 尝试在 resources/static 目录找 (这是老版本的主要存放地)
        File f2 = new File("src/main/resources/static/" + fileName);
        if (f2.exists())
            return f2;

        // 3. 尝试递归处理 URL 中的子路径 (针对 /image/user/xxx.png 这种情况)
        if (!originalUrl.startsWith("http")) {
            File f3 = new File(uploadPath + originalUrl.replaceFirst("^/", ""));
            if (f3.exists())
                return f3;
            File f4 = new File("src/main/resources/static/" + originalUrl.replaceFirst("^/", ""));
            if (f4.exists())
                return f4;
        }

        return null;
    }

    private Path downloadFile(String urlStr) throws IOException {
        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setConnectTimeout(5000);
        conn.setReadTimeout(60000);

        if (conn.getResponseCode() != 200)
            return null;

        String fileName = urlStr.substring(urlStr.lastIndexOf("/") + 1);
        if (fileName.contains("?"))
            fileName = fileName.substring(0, fileName.indexOf("?"));

        Path tempFile = Files.createTempFile("migrate_down_", "_" + fileName);
        try (InputStream is = conn.getInputStream();
                OutputStream os = new FileOutputStream(tempFile.toFile())) {
            byte[] buffer = new byte[32768];
            int bytesRead;
            while ((bytesRead = is.read(buffer)) != -1) {
                os.write(buffer, 0, bytesRead);
            }
        }
        return tempFile;
    }

    private static class MockMultipartFile implements MultipartFile {
        private final String name;
        private final Path path;
        private final String contentType;

        public MockMultipartFile(String name, Path path, String contentType) {
            this.name = name;
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
        public void transferTo(java.io.File dest) throws IOException {
            Files.copy(path, dest.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        }
    }
}
