package team.javaee.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import team.javaee.entity.domain.SongAsset;
import team.javaee.mapper.SongAssetMapper;
import team.javaee.service.SongAssetService;

@Service
public class SongAssetServiceImpl extends ServiceImpl<SongAssetMapper, SongAsset> implements SongAssetService {
}
