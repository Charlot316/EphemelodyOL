package team.javaee.service;

import com.baomidou.mybatisplus.extension.service.IService;
import team.javaee.entity.domain.ChartCollaborator;

import java.util.List;

/**
 * 谱面权限服务接口
 * 
 * @author Antigravity
 * @since 2026-02-03
 */
public interface ChartPermissionService extends IService<ChartCollaborator> {

    /**
     * 检查用户是否有编辑权限
     * 
     * @param songId     歌曲ID
     * @param userId     用户ID
     * @param isAdmin    是否是管理员
     * @param uploaderId 谱面上传者ID
     * @return 是否有编辑权限
     */
    boolean hasEditPermission(String songId, String userId, Integer isAdmin, String uploaderId);

    /**
     * 邀请协作者
     * 
     * @param songId         歌曲ID
     * @param inviterId      邀请人ID
     * @param targetUserId   被邀请人ID
     * @param permissionType 权限类型
     * @return 是否邀请成功
     */
    boolean inviteCollaborator(String songId, String inviterId, String targetUserId, Integer permissionType);

    /**
     * 接受邀请
     * 
     * @param collaboratorId 协作记录ID
     * @param userId         用户ID
     * @return 是否接受成功
     */
    boolean acceptInvitation(Long collaboratorId, String userId);

    /**
     * 拒绝邀请
     * 
     * @param collaboratorId 协作记录ID
     * @param userId         用户ID
     * @return 是否拒绝成功
     */
    boolean rejectInvitation(Long collaboratorId, String userId);

    /**
     * 移除协作者
     * 
     * @param songId       歌曲ID
     * @param operatorId   操作者ID
     * @param targetUserId 目标用户ID
     * @return 是否移除成功
     */
    boolean removeCollaborator(String songId, String operatorId, String targetUserId);

    /**
     * 获取谱面的所有协作者
     * 
     * @param songId 歌曲ID
     * @return 协作者列表
     */
    List<ChartCollaborator> getCollaborators(String songId);

    /**
     * 获取用户待处理的邀请
     * 
     * @param userId 用户ID
     * @return 待处理邀请列表
     */
    List<ChartCollaborator> getPendingInvitations(String userId);
}
