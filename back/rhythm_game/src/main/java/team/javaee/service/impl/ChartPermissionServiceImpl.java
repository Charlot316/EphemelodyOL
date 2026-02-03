package team.javaee.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import team.javaee.entity.domain.ChartCollaborator;
import team.javaee.mapper.ChartCollaboratorMapper;
import team.javaee.service.ChartPermissionService;

import java.util.List;

/**
 * 谱面权限服务实现类
 * 
 * @author Antigravity
 * @since 2026-02-03
 */
@Service
public class ChartPermissionServiceImpl extends ServiceImpl<ChartCollaboratorMapper, ChartCollaborator>
        implements ChartPermissionService {

    @Autowired
    private ChartCollaboratorMapper collaboratorMapper;

    @Override
    public boolean hasEditPermission(Integer songId, String userId, Integer isAdmin, String uploaderId) {
        // 1. 超级管理员有权限
        if (isAdmin != null && isAdmin == 1) {
            return true;
        }

        // 2. 谱面创作者有权限
        if (uploaderId != null && uploaderId.equals(userId)) {
            return true;
        }

        // 3. 检查是否是受邀协作者
        ChartCollaborator collaboration = collaboratorMapper.getActiveCollaboration(songId, userId);
        return collaboration != null && collaboration.getPermissionType() == ChartCollaborator.PermissionType.EDIT;
    }

    @Override
    @Transactional
    public boolean inviteCollaborator(Integer songId, String inviterId, String targetUserId, Integer permissionType) {
        // 检查是否已存在邀请
        QueryWrapper<ChartCollaborator> wrapper = new QueryWrapper<>();
        wrapper.eq("song_id", songId)
                .eq("user_id", targetUserId);
        ChartCollaborator existing = this.getOne(wrapper);

        if (existing != null) {
            // 如果已存在且状态是待接受或已接受，则不允许重复邀请
            if (existing.getStatus() == ChartCollaborator.Status.PENDING
                    || existing.getStatus() == ChartCollaborator.Status.ACCEPTED) {
                return false;
            }
            // 如果之前被拒绝，可以重新邀请
            existing.setStatus(ChartCollaborator.Status.PENDING);
            existing.setInvitedBy(inviterId);
            existing.setPermissionType(permissionType);
            return this.updateById(existing);
        }

        // 创建新邀请
        ChartCollaborator collaborator = new ChartCollaborator();
        collaborator.setSongId(songId);
        collaborator.setUserId(targetUserId);
        collaborator.setInvitedBy(inviterId);
        collaborator.setPermissionType(permissionType);
        collaborator.setStatus(ChartCollaborator.Status.PENDING);
        return this.save(collaborator);
    }

    @Override
    @Transactional
    public boolean acceptInvitation(Long collaboratorId, String userId) {
        ChartCollaborator collaborator = this.getById(collaboratorId);
        if (collaborator == null || !collaborator.getUserId().equals(userId)) {
            return false;
        }
        collaborator.setStatus(ChartCollaborator.Status.ACCEPTED);
        return this.updateById(collaborator);
    }

    @Override
    @Transactional
    public boolean rejectInvitation(Long collaboratorId, String userId) {
        ChartCollaborator collaborator = this.getById(collaboratorId);
        if (collaborator == null || !collaborator.getUserId().equals(userId)) {
            return false;
        }
        collaborator.setStatus(ChartCollaborator.Status.REJECTED);
        return this.updateById(collaborator);
    }

    @Override
    @Transactional
    public boolean removeCollaborator(Integer songId, String operatorId, String targetUserId) {
        QueryWrapper<ChartCollaborator> wrapper = new QueryWrapper<>();
        wrapper.eq("song_id", songId)
                .eq("user_id", targetUserId);
        return this.remove(wrapper);
    }

    @Override
    public List<ChartCollaborator> getCollaborators(Integer songId) {
        return collaboratorMapper.getCollaboratorsBySongId(songId);
    }

    @Override
    public List<ChartCollaborator> getPendingInvitations(String userId) {
        return collaboratorMapper.getPendingInvitations(userId);
    }
}
