package team.javaee.service.impl;

import team.javaee.entity.domain.Track;
import team.javaee.mapper.TrackMapper;
import team.javaee.service.TrackService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author nwh
 * @since 2022-05-13
 */
@Service
public class TrackServiceImpl extends ServiceImpl<TrackMapper, Track> implements TrackService {

}
