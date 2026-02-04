package team.javaee.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import team.javaee.common.config.ReturnResponse;
import team.javaee.service.R2CleanupService;

@RestController
@RequestMapping("/api/admin/cleanup")
public class R2CleanupController {

    @Autowired
    private R2CleanupService cleanupService;

    @PostMapping("/r2")
    public ReturnResponse<String> cleanupR2(@RequestParam(defaultValue = "true") boolean dryRun) {
        String result = cleanupService.cleanupOrphanedFiles(dryRun);
        return ReturnResponse.OK(result);
    }
}
