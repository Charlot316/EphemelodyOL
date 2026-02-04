package team.javaee.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import team.javaee.common.config.ReturnResponse;
import team.javaee.service.R2MigrationService;

@RestController
@RequestMapping("/api/admin/migration")
public class R2MigrationController {

    @Autowired
    private R2MigrationService migrationService;

    @PostMapping("/r2")
    public ReturnResponse<String> migrateToR2() {
        // 开启异步线程执行，避免前端超时
        new Thread(() -> {
            try {
                migrationService.migrateAll();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();

        return ReturnResponse.OK("迁移任务已在后台启动，请查看后端日志。");
    }
}
