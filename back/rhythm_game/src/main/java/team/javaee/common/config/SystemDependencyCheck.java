package team.javaee.common.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;

@Slf4j
@Component
public class SystemDependencyCheck implements CommandLineRunner {

    @Override
    public void run(String... args) {
        log.info("正在检查系统依赖：ffmpeg 和 cwebp...");

        boolean ffmpegFound = checkCommand("ffmpeg -version");
        boolean cwebpFound = checkCommand("cwebp -version");

        if (!ffmpegFound) {
            log.warn("警告: [ffmpeg] 未在系统中找到！视频压缩功能将无法正常运行。");
        } else {
            log.info("确认: [ffmpeg] 已就绪。");
        }

        if (!cwebpFound) {
            log.warn("警告: [cwebp] 未在系统中找到！图片转换为 WebP 的功能将无法正常运行。");
        } else {
            log.info("确认: [cwebp] 已就绪。");
        }

        if (!ffmpegFound || !cwebpFound) {
            log.warn("环境提示: 如果您在 Docker 中运行，请确保 Dockerfile 中包含相关的安装命令。如果在本地运行，请手动安装 ffmpeg 和 webp 工具包。");
        }
    }

    private boolean checkCommand(String command) {
        try {
            Process process = Runtime.getRuntime().exec(command);
            int exitCode = process.waitFor();
            return exitCode == 0;
        } catch (Exception e) {
            return false;
        }
    }
}
