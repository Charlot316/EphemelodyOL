package team.javaee.mapper;

import org.apache.ibatis.annotations.Mapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import team.javaee.entity.domain.SongAsset;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface SongAssetMapper extends BaseMapper<SongAsset> {
    /**
     * 删除与歌曲封面或默认背景地址冲突的重复资产
     */
    @Delete("DELETE FROM song_asset WHERE song_id = #{songId} AND (url = (SELECT song_cover FROM song WHERE id = #{songId}) OR url = (SELECT default_background FROM song WHERE id = #{songId}))")
    int deleteDuplicateAssets(@Param("songId") String songId);
}
