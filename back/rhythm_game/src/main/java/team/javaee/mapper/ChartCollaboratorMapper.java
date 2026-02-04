package team.javaee.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import team.javaee.entity.domain.ChartCollaborator;

import java.util.List;

/**
 * 谱面协作者Mapper接口
 * 
 * @author Antigravity
 * @since 2026-02-03
 */
@Mapper
public interface ChartCollaboratorMapper extends BaseMapper<ChartCollaborator> {

    /**
     * 查询用户在指定谱面的协作权限
     */
    @Select("SELECT * FROM chart_collaborator WHERE song_id = #{songId} AND user_id = #{userId} AND status = 1")
    ChartCollaborator getActiveCollaboration(@Param("songId") String songId, @Param("userId") String userId);

    /**
     * 查询谱面的所有协作者
     */
    @Select("SELECT * FROM chart_collaborator WHERE song_id = #{songId} AND status = 1")
    List<ChartCollaborator> getCollaboratorsBySongId(@Param("songId") String songId);

    /**
     * 查询用户待处理的邀请
     */
    @Select("SELECT * FROM chart_collaborator WHERE user_id = #{userId} AND status = 0")
    List<ChartCollaborator> getPendingInvitations(@Param("userId") String userId);
}
