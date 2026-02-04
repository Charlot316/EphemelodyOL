package team.javaee.entity.domain;

import com.baomidou.mybatisplus.annotation.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * 谱面协作者实体类
 * 用于管理谱面的协作编辑权限
 * 
 * @author Antigravity
 * @since 2026-02-03
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("chart_collaborator")
public class ChartCollaborator implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 歌曲ID
     */
    private String songId;

    /**
     * 协作者用户ID
     */
    private String userId;

    /**
     * 权限类型：1-编辑权限 2-只读权限
     */
    private Integer permissionType;

    /**
     * 邀请人ID
     */
    private String invitedBy;

    /**
     * 状态：0-待接受 1-已接受 2-已拒绝
     */
    private Integer status;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /**
     * 权限类型常量
     */
    public static class PermissionType {
        public static final int EDIT = 1;
        public static final int READ_ONLY = 2;
    }

    /**
     * 状态常量
     */
    public static class Status {
        public static final int PENDING = 0;
        public static final int ACCEPTED = 1;
        public static final int REJECTED = 2;
    }
}
