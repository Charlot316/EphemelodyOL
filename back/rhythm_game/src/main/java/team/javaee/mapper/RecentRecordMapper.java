package team.javaee.mapper;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import team.javaee.entity.domain.RecentRecord;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import team.javaee.entity.vo.RankVO;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author nwh
 * @since 2022-05-13
 */
@Mapper
public interface RecentRecordMapper extends BaseMapper<RecentRecord> {

}
