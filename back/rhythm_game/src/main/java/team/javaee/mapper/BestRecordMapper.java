package team.javaee.mapper;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import team.javaee.entity.domain.BestRecord;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import team.javaee.entity.domain.RecentRecord;
import team.javaee.entity.vo.RankVO;

import java.util.List;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author nwh
 * @since 2022-05-13
 */
@Mapper
public interface BestRecordMapper extends BaseMapper<BestRecord> {

    @Select("SELECT user_id, score, pure, combo, DENSE_RANK()OVER(ORDER BY score DESC) AS 'rank' FROM best_record ${ew.customSqlSegment}")
    List<RankVO> getPlayerRank(@Param(Constants.WRAPPER) QueryWrapper<BestRecord> queryWrapper);

    Integer getRank(String userId, String songId);

    double getTenBestRecordPotential(String userId);

}
