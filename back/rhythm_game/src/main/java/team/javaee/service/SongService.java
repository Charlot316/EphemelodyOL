package team.javaee.service;

import org.apache.ibatis.annotations.Param;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.multipart.MultipartFile;
import team.javaee.common.config.ReturnResponse;
import team.javaee.entity.domain.Song;
import com.baomidou.mybatisplus.extension.service.IService;
import team.javaee.entity.dto.*;
import team.javaee.entity.vo.BackgroundVO;
import team.javaee.entity.vo.ImageVO;
import team.javaee.entity.vo.SongVO;

import javax.servlet.http.HttpServletRequest;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author nwh
 * @since 2022-05-13
 */
public interface SongService extends IService<Song> {

    // 新增谱面
    ReturnResponse<Integer> newChart(NewChartDTO newChartDTO);

    // 修改谱面
    ReturnResponse<String> editChartInfo(EditChartDTO editChartDTO);

    // 上传歌曲封面
    ReturnResponse<ImageVO> uploadSongCover(@Param("file") MultipartFile file, @Param("songId") Integer songId, HttpServletRequest request);

    // 上传歌曲默认背景
    ReturnResponse<ImageVO> uploadDefaultBackground(@Param("file") MultipartFile file, @Param("songId") Integer songId, HttpServletRequest request);

    // 上传音频
    ReturnResponse<String> uploadSong(@Param("file") MultipartFile file, @Param("songId") Integer songId, HttpServletRequest request);

    // 修改谱面具体内容
    ReturnResponse<String> editChartContent(ChartContentDTO chartContentDTO);

    // 新增歌曲背景
    ReturnResponse<BackgroundVO> uploadBackground(UploadBackgroundDTO uploadBackgroundDTO, HttpServletRequest request);

    // 认定已公开的谱面
    ReturnResponse<String> accreditChart(SongDTO songDTO);

    // 认定已公开的谱面
    ReturnResponse<String> disAccreditChart(SongDTO songDTO);

    // 删除谱面
    ReturnResponse<String> deleteChart(SongDTO songDTO);
}
