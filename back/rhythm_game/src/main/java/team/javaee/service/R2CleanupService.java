package team.javaee.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import team.javaee.entity.domain.Song;
import team.javaee.entity.domain.SongAsset;
import team.javaee.entity.domain.User;
import team.javaee.mapper.SongAssetMapper;
import team.javaee.mapper.SongMapper;
import team.javaee.mapper.UserMapper;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
public class R2CleanupService {

    @Autowired
    private S3Client s3Client;

    @Autowired
    private SongMapper songMapper;

    @Autowired
    private SongAssetMapper songAssetMapper;

    @Autowired
    private UserMapper userMapper;

    @Value("${cloudflare.r2.bucket-name}")
    private String bucketName;

    @Value("${cloudflare.r2.public-url}")
    private String publicUrl;

    private static final String R2_ROOT = "ephemelody/";

    public String cleanupOrphanedFiles(boolean dryRun) {
        log.info("开始 R2 孤立文件整理 (dryRun={})...", dryRun);

        // 1. 收集数据库中引用的所有 R2 Key
        Set<String> referencedKeys = new HashSet<>();

        List<Song> songs = songMapper.selectList(null);
        for (Song s : songs) {
            addKeyIfR2(referencedKeys, s.getSongCover());
            addKeyIfR2(referencedKeys, s.getDefaultBackground());
            addKeyIfR2(referencedKeys, s.getSongUrl());
            // 谱面文件也是引用的
            referencedKeys.add(R2_ROOT + "charts/" + s.getId() + ".json");
        }

        List<SongAsset> assets = songAssetMapper.selectList(null);
        for (SongAsset a : assets) {
            addKeyIfR2(referencedKeys, a.getUrl());
        }

        List<User> users = userMapper.selectList(null);
        for (User u : users) {
            addKeyIfR2(referencedKeys, u.getIcon());
        }

        log.info("数据库中引用的总 Key 数量: {}", referencedKeys.size());

        // 2. 遍历 R2 中的所有文件
        ListObjectsV2Request listRequest = ListObjectsV2Request.builder()
                .bucket(bucketName)
                .prefix(R2_ROOT)
                .build();

        ListObjectsV2Response listResponse = s3Client.listObjectsV2(listRequest);
        List<S3Object> allObjects = listResponse.contents();

        List<String> orphanedKeys = allObjects.stream()
                .map(S3Object::key)
                .filter(key -> !referencedKeys.contains(key))
                .collect(Collectors.toList());

        log.info("发现孤立文件数量: {}", orphanedKeys.size());

        if (dryRun) {
            return "发现 " + orphanedKeys.size() + " 个孤立文件 (未删除):\n" + String.join("\n", orphanedKeys);
        }

        // 3. 执行删除
        int deletedCount = 0;
        for (String key : orphanedKeys) {
            try {
                DeleteObjectRequest deleteRequest = DeleteObjectRequest.builder()
                        .bucket(bucketName)
                        .key(key)
                        .build();
                s3Client.deleteObject(deleteRequest);
                log.info("已删除孤立文件: {}", key);
                deletedCount++;
            } catch (Exception e) {
                log.error("删除文件失败: {}", key, e);
            }
        }

        return "整理完成。共扫描 " + allObjects.size() + " 个文件，删除 " + deletedCount + " 个孤立文件。";
    }

    private void addKeyIfR2(Set<String> keys, String url) {
        if (url == null || url.isEmpty())
            return;
        if (url.startsWith(publicUrl)) {
            String key = url.replace(publicUrl, "");
            if (key.startsWith("/"))
                key = key.substring(1);
            keys.add(key);
        }
    }
}
