package team.javaee.service;

import org.apache.ibatis.annotations.Param;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.multipart.MultipartFile;
import team.javaee.common.config.ReturnResponse;
import team.javaee.entity.domain.Song;
import team.javaee.entity.domain.SongAsset;
import com.baomidou.mybatisplus.extension.service.IService;
import team.javaee.entity.dto.*;
import team.javaee.entity.vo.BackgroundVO;
import team.javaee.entity.vo.ImageVO;
import team.javaee.entity.vo.SongVO;

import javax.servlet.http.HttpServletRequest;

/**
 * <p>
 * 服务类
 * </p>
 *
 * @author nwh
 * @since 2022-05-13
 */
public interface SongService extends IService<Song> {

        // 新增谱面
        ReturnResponse<String> newChart(NewChartDTO newChartDTO);

        // 修改谱面
        ReturnResponse<String> editChartInfo(EditChartDTO editChartDTO);

        // 上传歌曲封面
        ReturnResponse<ImageVO> uploadSongCover(@Param("file") MultipartFile file, @Param("songId") String songId,
                        HttpServletRequest request);

        // 上传歌曲默认背景
        ReturnResponse<ImageVO> uploadDefaultBackground(@Param("file") MultipartFile file,
                        @Param("songId") String songId,
                        HttpServletRequest request);

        // 上传音频
        ReturnResponse<String> uploadSong(@Param("file") MultipartFile file, @Param("songId") String songId,
                        HttpServletRequest request);

        // 修改谱面具体内容
        ReturnResponse<String> editChartContent(ChartContentDTO chartContentDTO);

        // 新增歌曲背景
        ReturnResponse<BackgroundVO> uploadBackground(UploadBackgroundDTO uploadBackgroundDTO,
                        HttpServletRequest request);

        // 认定已公开的谱面
        ReturnResponse<String> accreditChart(SongDTO songDTO);

        // 认定已公开的谱面
        ReturnResponse<String> disAccreditChart(SongDTO songDTO);

        // 删除谱面
        ReturnResponse<String> deleteChart(SongDTO songDTO);

        // 上传资源素材
        ReturnResponse<SongAsset> uploadAsset(MultipartFile file, String songId, String type,
                        HttpServletRequest request);

        // 删除资源素材
        ReturnResponse<String> deleteAsset(Integer id);

        // 从 JSON 重置数据库
        ReturnResponse<String> resetChartFromJSON(String songId);

        // 获取谱面详情 (包含轨道和音符)
        ReturnResponse<ChartContentDTO> getChart(String songId);

        // 强制同步所有 DB 到 JSON
        ReturnResponse<String> forceSyncAll();
}
