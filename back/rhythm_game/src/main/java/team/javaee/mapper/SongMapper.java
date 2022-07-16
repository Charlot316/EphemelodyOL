package team.javaee.mapper;

import org.apache.ibatis.annotations.Mapper;
import team.javaee.entity.domain.Song;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author nwh
 * @since 2022-05-13
 */
@Mapper
public interface SongMapper extends BaseMapper<Song> {

}
