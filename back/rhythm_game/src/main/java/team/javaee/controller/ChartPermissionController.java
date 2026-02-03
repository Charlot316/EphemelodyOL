package team.javaee.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import team.javaee.common.config.ReturnResponse;
import team.javaee.common.enums.ReturnStatus;
import team.javaee.entity.domain.ChartCollaborator;
import team.javaee.entity.domain.Song;
import team.javaee.entity.domain.User;
import team.javaee.service.ChartPermissionService;
import team.javaee.service.SongService;
import team.javaee.service.UserService;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 谱面权限控制器
 * 
 * @author Antigravity
 * @since 2026-02-03
 */
@RestController
@RequestMapping("/chart/permission")
public class ChartPermissionController {

    @Autowired
    private ChartPermissionService permissionService;

    @Autowired
    private SongService songService;

    @Autowired
    private UserService userService;

    /**
     * 检查用户是否有编辑权限
     */
    @GetMapping("/check/{songId}")
    public ReturnResponse<Object> checkPermission(
            @PathVariable Integer songId,
            HttpServletRequest request) {

        User currentUser = (User) request.getSession().getAttribute("user");
        if (currentUser == null) {
            return ReturnResponse.packageObject("未登录", ReturnStatus.FAILURE);
        }

        Song song = songService.getById(songId);
        if (song == null) {
            return ReturnResponse.packageObject("谱面不存在", ReturnStatus.FAILURE);
        }

        boolean hasPermission = permissionService.hasEditPermission(
                songId,
                currentUser.getUserId(),
                currentUser.getIsAdmin(),
                song.getUploaderId());

        Map<String, Object> data = new HashMap<>();
        data.put("hasPermission", hasPermission);
        data.put("isCreator", song.getUploaderId().equals(currentUser.getUserId()));
        data.put("isAdmin", currentUser.getIsAdmin() == 1);

        return ReturnResponse.OK(data);
    }

    /**
     * 邀请协作者
     */
    @PostMapping("/invite")
    public ReturnResponse<String> inviteCollaborator(
            @RequestParam Integer songId,
            @RequestParam String targetUsername,
            @RequestParam(defaultValue = "1") Integer permissionType,
            HttpServletRequest request) {

        User currentUser = (User) request.getSession().getAttribute("user");
        if (currentUser == null) {
            return ReturnResponse.packageObject("未登录", ReturnStatus.FAILURE);
        }

        Song song = songService.getById(songId);
        if (song == null) {
            return ReturnResponse.packageObject("谱面不存在", ReturnStatus.FAILURE);
        }

        // 只有创作者和管理员可以邀请协作者
        if (!song.getUploaderId().equals(currentUser.getUserId()) && currentUser.getIsAdmin() != 1) {
            return ReturnResponse.packageObject("无权限邀请协作者", ReturnStatus.FAILURE);
        }

        // 查找目标用户
        User targetUser = userService.getUserByUsername(targetUsername);
        if (targetUser == null) {
            return ReturnResponse.packageObject("目标用户不存在", ReturnStatus.FAILURE);
        }

        boolean success = permissionService.inviteCollaborator(
                songId,
                currentUser.getUserId(),
                targetUser.getUserId(),
                permissionType);

        return success ? ReturnResponse.OK("邀请成功") : ReturnResponse.packageObject("邀请失败，可能已存在邀请", ReturnStatus.FAILURE);
    }

    /**
     * 接受邀请
     */
    @PostMapping("/accept/{collaboratorId}")
    public ReturnResponse<String> acceptInvitation(
            @PathVariable Long collaboratorId,
            HttpServletRequest request) {

        User currentUser = (User) request.getSession().getAttribute("user");
        if (currentUser == null) {
            return ReturnResponse.packageObject("未登录", ReturnStatus.FAILURE);
        }

        boolean success = permissionService.acceptInvitation(collaboratorId, currentUser.getUserId());
        return success ? ReturnResponse.OK("已接受邀请") : ReturnResponse.packageObject("接受失败", ReturnStatus.FAILURE);
    }

    /**
     * 拒绝邀请
     */
    @PostMapping("/reject/{collaboratorId}")
    public ReturnResponse<String> rejectInvitation(
            @PathVariable Long collaboratorId,
            HttpServletRequest request) {

        User currentUser = (User) request.getSession().getAttribute("user");
        if (currentUser == null) {
            return ReturnResponse.packageObject("未登录", ReturnStatus.FAILURE);
        }

        boolean success = permissionService.rejectInvitation(collaboratorId, currentUser.getUserId());
        return success ? ReturnResponse.OK("已拒绝邀请") : ReturnResponse.packageObject("拒绝失败", ReturnStatus.FAILURE);
    }

    /**
     * 移除协作者
     */
    @DeleteMapping("/remove")
    public ReturnResponse<String> removeCollaborator(
            @RequestParam Integer songId,
            @RequestParam String targetUserId,
            HttpServletRequest request) {

        User currentUser = (User) request.getSession().getAttribute("user");
        if (currentUser == null) {
            return ReturnResponse.packageObject("未登录", ReturnStatus.FAILURE);
        }

        Song song = songService.getById(songId);
        if (song == null) {
            return ReturnResponse.packageObject("谱面不存在", ReturnStatus.FAILURE);
        }

        // 只有创作者和管理员可以移除协作者
        if (!song.getUploaderId().equals(currentUser.getUserId()) && currentUser.getIsAdmin() != 1) {
            return ReturnResponse.packageObject("无权限移除协作者", ReturnStatus.FAILURE);
        }

        boolean success = permissionService.removeCollaborator(songId, currentUser.getUserId(), targetUserId);
        return success ? ReturnResponse.OK("移除成功") : ReturnResponse.packageObject("移除失败", ReturnStatus.FAILURE);
    }

    /**
     * 获取谱面的所有协作者
     */
    @GetMapping("/collaborators/{songId}")
    public ReturnResponse<Object> getCollaborators(
            @PathVariable Integer songId,
            HttpServletRequest request) {

        User currentUser = (User) request.getSession().getAttribute("user");
        if (currentUser == null) {
            return ReturnResponse.packageObject("未登录", ReturnStatus.FAILURE);
        }

        List<ChartCollaborator> collaborators = permissionService.getCollaborators(songId);
        return ReturnResponse.OK(collaborators);
    }

    /**
     * 获取我的待处理邀请
     */
    @GetMapping("/invitations/pending")
    public ReturnResponse<Object> getPendingInvitations(HttpServletRequest request) {
        User currentUser = (User) request.getSession().getAttribute("user");
        if (currentUser == null) {
            return ReturnResponse.packageObject("未登录", ReturnStatus.FAILURE);
        }

        List<ChartCollaborator> invitations = permissionService.getPendingInvitations(currentUser.getUserId());
        return ReturnResponse.OK(invitations);
    }
}
