package team.javaee.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.multipart.MultipartFile;
import team.javaee.common.config.ReturnResponse;
import team.javaee.common.enums.ReturnStatus;
import team.javaee.entity.domain.*;
import team.javaee.entity.dto.*;
import team.javaee.entity.vo.BackgroundVO;
import team.javaee.entity.vo.ImageVO;
import team.javaee.mapper.*;
import team.javaee.service.SongService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import javax.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.util.Date;
import java.util.UUID;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author nwh
 * @since 2022-05-13
 */
@Service
public class SongServiceImpl extends ServiceImpl<SongMapper, Song> implements SongService {

    @Autowired
    private SongMapper songMapper;

    @Autowired
    private TrackMapper trackMapper;

    @Autowired
    private NoteMapper noteMapper;

    @Autowired
    private ChangeBackgroundOperationMapper changeBackgroundOperationMapper;

    @Autowired
    private ChangeColorOperationMapper changeColorOperationMapper;

    @Autowired
    private ChangeWidthOperationMapper changeWidthOperationMapper;

    @Autowired
    private MoveOperationMapper moveOperationMapper;

    @Autowired
    private BestRecordMapper bestRecordMapper;

    @Autowired
    private RecentRecordMapper recentRecordMapper;

    @Value("${web.upload-path}")
    private String uploadPath;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public ReturnResponse<Integer> newChart(NewChartDTO newChartDTO) {
        try {
            Song song = new Song();
            song.setSongName(newChartDTO.getSongName());
            song.setSongWriter(newChartDTO.getSongWriter());
            song.setSongUrl(newChartDTO.getSongUrl());
            song.setDefaultBackground(newChartDTO.getDefaultBackground());
            song.setSongCover(newChartDTO.getSongCover());
            song.setLoadingText(newChartDTO.getLoadingText());
            song.setLoadedText(newChartDTO.getLoadedText());
            song.setChartConstant(newChartDTO.getChartConstant());
            song.setNotesCount(0);
            song.setPlayTime(0);
            song.setStatus(0);
            song.setUploaderId(newChartDTO.getUploaderId());
            song.setUploadDate(new Date());
            songMapper.insert(song);
            return ReturnResponse.OK(song.getId()); // 返回歌曲id
        } catch (Exception e) {
            e.printStackTrace();
            return ReturnResponse.systemException(ReturnStatus.BUSINESS_EXCEPTION);
        }
    }

    @Override
    public ReturnResponse<String> editChartInfo(EditChartDTO editChartDTO) {
        try {
            // 按照 songId 查询
            Song song = songMapper.selectOne(new QueryWrapper<Song>().eq("id", editChartDTO.getSongId()));

            // 验证 uploader 和当前用户是否相同
            if (!editChartDTO.getUploaderId().equals(song.getUploaderId())) {
                return ReturnResponse.packageObject("您没有权利修改当前谱面", ReturnStatus.FAILURE);
            }

            song.setSongName(editChartDTO.getSongName());
            song.setSongWriter(editChartDTO.getSongWriter());
            song.setSongUrl(editChartDTO.getSongUrl());
            song.setDefaultBackground(editChartDTO.getDefaultBackground());
            song.setSongCover(editChartDTO.getSongCover());
            song.setLoadingText(editChartDTO.getLoadingText());
            song.setLoadedText(editChartDTO.getLoadedText());
            song.setChartConstant(editChartDTO.getChartConstant());

            songMapper.update(song, new QueryWrapper<Song>().eq("id", song.getId()));
            return ReturnResponse.OK("更新成功！");
        } catch (Exception e) {
            e.printStackTrace();
            return ReturnResponse.systemException(ReturnStatus.BUSINESS_EXCEPTION);
        }
    }

    @Override
    public ReturnResponse<ImageVO> uploadSongCover(@Param("file") MultipartFile file, @Param("songId") Integer songId,
            HttpServletRequest request) {
        try {
            ImageVO imageVO = new ImageVO();
            String realPath = uploadPath + "image/song/";

            File folder = new File(realPath);
            if (!folder.exists()) {
                folder.mkdirs();
            }
            String oldName = file.getOriginalFilename();
            String newName = UUID.randomUUID().toString() + oldName.substring(oldName.lastIndexOf("."));
            file.transferTo(new File(folder, newName));
            String url = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort()
                    + "/image/song/" + newName;

            Song song = songMapper.selectById(songId);
            song.setSongCover(url);
            songMapper.update(song, new QueryWrapper<Song>().eq("id", song.getId()));

            imageVO.setUrl(url); // 返回 url
            return ReturnResponse.OK(imageVO);
        } catch (Exception e) {
            e.printStackTrace();
            return ReturnResponse.systemException(ReturnStatus.BUSINESS_EXCEPTION);
        }
    }

    @Override
    public ReturnResponse<ImageVO> uploadDefaultBackground(@Param("file") MultipartFile file,
            @Param("songId") Integer songId, HttpServletRequest request) {
        try {
            ImageVO imageVO = new ImageVO();
            String realPath = uploadPath + "image/song/";

            File folder = new File(realPath);
            if (!folder.exists()) {
                folder.mkdirs();
            }
            String oldName = file.getOriginalFilename();
            String newName = UUID.randomUUID().toString() + oldName.substring(oldName.lastIndexOf("."));
            file.transferTo(new File(folder, newName));
            String url = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort()
                    + "/image/song/" + newName;

            Song song = songMapper.selectById(songId);
            song.setDefaultBackground(url);
            songMapper.update(song, new QueryWrapper<Song>().eq("id", song.getId()));

            imageVO.setUrl(url); // 返回 url
            return ReturnResponse.OK(imageVO);
        } catch (Exception e) {
            e.printStackTrace();
            return ReturnResponse.systemException(ReturnStatus.BUSINESS_EXCEPTION);
        }
    }

    @Override
    public ReturnResponse<String> uploadSong(@Param("file") MultipartFile file, @Param("songId") Integer songId,
            HttpServletRequest request) {
        try {
            String realPath = uploadPath + "mp3/";

            File folder = new File(realPath);
            if (!folder.exists()) {
                folder.mkdirs();
            }

            String oldName = file.getOriginalFilename();
            String newName = UUID.randomUUID().toString() + oldName.substring(oldName.lastIndexOf("."));
            file.transferTo(new File(folder, newName)); // 上传文件
            String url = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort() + "/mp3/"
                    + newName;

            Song song = songMapper.selectById(songId);
            song.setSongUrl(url);
            songMapper.update(song, new QueryWrapper<Song>().eq("id", song.getId()));

            return ReturnResponse.OK(url);
        } catch (Exception e) {
            e.printStackTrace();
            return ReturnResponse.systemException(ReturnStatus.BUSINESS_EXCEPTION);
        }
    }

    @Override
    public ReturnResponse<String> editChartContent(ChartContentDTO chartContentDTO) {
        try {
            Integer songId = chartContentDTO.getSongId();

            // 1. 保存详情到 JSON 文件
            String chartsFolder = uploadPath + "charts/";
            File folder = new File(chartsFolder);
            if (!folder.exists()) {
                folder.mkdirs();
            }
            File jsonFile = new File(folder, songId + ".json");
            objectMapper.writeValue(jsonFile, chartContentDTO);

            // 2. 更新数据库中的元数据
            Song song = songMapper.selectById(songId);
            song.setBPM(chartContentDTO.getBPM());
            song.setFirstBeatDelay(chartContentDTO.getFirstBeatDelay());

            int notesCount = 0;
            for (TrackDTO trackDTO : chartContentDTO.getTracks()) {
                notesCount += trackDTO.getNotes().size();
            }
            song.setNotesCount(notesCount);
            songMapper.update(song, new QueryWrapper<Song>().eq("id", songId));

            // 3. (可选) 清理旧的数据库详情数据
            // 以后迁移完成后可以彻底移除这些表
            trackMapper.delete(new QueryWrapper<Track>().eq("song_id", songId));
            noteMapper.delete(new QueryWrapper<Note>().eq("song_id", songId));
            changeBackgroundOperationMapper.delete(new QueryWrapper<ChangeBackgroundOperation>().eq("song_id", songId));
            changeColorOperationMapper.delete(new QueryWrapper<ChangeColorOperation>().eq("song_id", songId));
            changeWidthOperationMapper.delete(new QueryWrapper<ChangeWidthOperation>().eq("song_id", songId));
            moveOperationMapper.delete(new QueryWrapper<MoveOperation>().eq("song_id", songId));

            return ReturnResponse.OK("更新谱面成功（已保存至 JSON）！");
        } catch (Exception e) {
            e.printStackTrace();
            return ReturnResponse.systemException(ReturnStatus.BUSINESS_EXCEPTION);
        }
    }

    @Override
    public ReturnResponse<BackgroundVO> uploadBackground(UploadBackgroundDTO uploadBackgroundDTO,
            HttpServletRequest request) {
        try {
            MultipartFile file = uploadBackgroundDTO.getBackground();
            BackgroundVO backgroundVO = new BackgroundVO();
            String realPath = uploadPath + "jpg/";

            File folder = new File(realPath);
            if (!folder.exists()) {
                folder.mkdirs();
            }
            String oldName = file.getOriginalFilename();
            String newName = UUID.randomUUID().toString() + oldName.substring(oldName.lastIndexOf("."));
            file.transferTo(new File(folder, newName));
            String url = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort() + "/jpg/"
                    + newName;

            ChangeBackgroundOperation changeBackgroundOperation = new ChangeBackgroundOperation();
            changeBackgroundOperation.setSongId(uploadBackgroundDTO.getSongId());
            changeBackgroundOperation.setBackground(url);
            changeBackgroundOperation.setStartTime(uploadBackgroundDTO.getStartTime());
            changeBackgroundOperationMapper.insert(changeBackgroundOperation);

            backgroundVO.setBackground(url); // 返回 url
            return ReturnResponse.OK(backgroundVO);
        } catch (Exception e) {
            e.printStackTrace();
            return ReturnResponse.systemException(ReturnStatus.BUSINESS_EXCEPTION);
        }
    }

    @Override
    public ReturnResponse<String> accreditChart(SongDTO songDTO) {
        try {
            String songId = songDTO.getSongId();
            Song song = songMapper.selectById(songId);
            song.setStatus(2);
            songMapper.update(song, new QueryWrapper<Song>().eq("id", songId));
            return ReturnResponse.OK("认定成功");
        } catch (Exception e) {
            return ReturnResponse.systemException(ReturnStatus.BUSINESS_EXCEPTION);
        }
    }

    @Override
    public ReturnResponse<String> disAccreditChart(SongDTO songDTO) {
        try {
            String songId = songDTO.getSongId();
            Song song = songMapper.selectById(songId);
            song.setStatus(1);
            songMapper.update(song, new QueryWrapper<Song>().eq("id", songId));
            return ReturnResponse.OK("取消认定成功");
        } catch (Exception e) {
            return ReturnResponse.systemException(ReturnStatus.BUSINESS_EXCEPTION);
        }
    }

    @Override
    public ReturnResponse<String> deleteChart(SongDTO songDTO) {
        try {
            String songId = songDTO.getSongId();
            songMapper.delete(new QueryWrapper<Song>().eq("song_id", songId));
            trackMapper.delete(new QueryWrapper<Track>().eq("song_id", songId));
            moveOperationMapper.delete(new QueryWrapper<MoveOperation>().eq("song_id", songId));
            noteMapper.delete(new QueryWrapper<Note>().eq("song_id", songId));
            changeBackgroundOperationMapper.delete(new QueryWrapper<ChangeBackgroundOperation>().eq("song_id", songId));
            changeColorOperationMapper.delete(new QueryWrapper<ChangeColorOperation>().eq("song_id", songId));
            changeWidthOperationMapper.delete(new QueryWrapper<ChangeWidthOperation>().eq("song_id", songId));
            recentRecordMapper.delete(new QueryWrapper<RecentRecord>().eq("song_id", songId));
            bestRecordMapper.delete(new QueryWrapper<BestRecord>().eq("song_id", songId));
            return ReturnResponse.OK("删除成功");
        } catch (Exception e) {
            return ReturnResponse.systemException(ReturnStatus.BUSINESS_EXCEPTION);
        }
    }
}
