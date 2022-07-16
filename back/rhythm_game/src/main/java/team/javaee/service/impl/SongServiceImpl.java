package team.javaee.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.api.R;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.multipart.MultipartFile;
import team.javaee.common.Normal;
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
import java.io.File;
import java.util.Date;
import java.util.Objects;
import java.util.UUID;

/**
 * <p>
 *  服务实现类
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
    public ReturnResponse<ImageVO> uploadSongCover(@Param("file") MultipartFile file, @Param("songId") Integer songId, HttpServletRequest request) {
        try {
            ImageVO imageVO = new ImageVO();
            String realPath = "/data/image/song/";
//            String realPath = "F:\\IdeaProjects\\j2ee_springboot\\rhythm_game\\src\\main\\resources\\static\\files\\";
//            String realPath = "E:\\新建文件夹\\j2ee_springboot\\rhythm_game\\src\\main\\resources\\static\\files\\";

            File folder = new File(realPath);
            if (!folder.exists()) {
                folder.mkdirs();
            }
            String oldName = file.getOriginalFilename();
            String newName = UUID.randomUUID().toString() + oldName.substring(oldName.lastIndexOf("."));
            file.transferTo(new File(folder, newName));
            String url = request.getScheme() + "://47.113.89.104:8090/image/song/" + newName;

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
    public ReturnResponse<ImageVO> uploadDefaultBackground(@Param("file") MultipartFile file, @Param("songId") Integer songId, HttpServletRequest request) {
        try {
            ImageVO imageVO = new ImageVO();
            String realPath = "/data/image/song/";
//            String realPath = "F:\\IdeaProjects\\j2ee_springboot\\rhythm_game\\src\\main\\resources\\static\\files\\";
//            String realPath = "E:\\新建文件夹\\j2ee_springboot\\rhythm_game\\src\\main\\resources\\static\\files\\";

            File folder = new File(realPath);
            if (!folder.exists()) {
                folder.mkdirs();
            }
            String oldName = file.getOriginalFilename();
            String newName = UUID.randomUUID().toString() + oldName.substring(oldName.lastIndexOf("."));
            file.transferTo(new File(folder, newName));
            String url = request.getScheme() + "://47.113.89.104:8090/image/song/" + newName;

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
    public ReturnResponse<String> uploadSong(@Param("file") MultipartFile file, @Param("songId") Integer songId, HttpServletRequest request) {
        try {
            String realPath = "/data/mp3/";
//            String realPath = "F:\\IdeaProjects\\j2ee_springboot\\rhythm_game\\src\\main\\resources\\static\\files\\";
//            String realPath = "E:\\新建文件夹\\j2ee_springboot\\rhythm_game\\src\\main\\resources\\static\\files\\";

            File folder = new File(realPath);
            if (!folder.exists()) {
                folder.mkdirs();
            }

            String oldName = file.getOriginalFilename();
            String newName = UUID.randomUUID().toString() + oldName.substring(oldName.lastIndexOf("."));
            file.transferTo(new File(folder, newName)); // 上传文件
            String url = request.getScheme() + "://47.113.89.104:8090/mp3/" + newName;

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

            // 更新 song
            Song song = songMapper.selectById(songId);
            song.setBPM(chartContentDTO.getBPM());
            song.setFirstBeatDelay(chartContentDTO.getFirstBeatDelay());

            // 根据 songId 删除对应行
            trackMapper.delete(new QueryWrapper<Track>().eq("song_id", songId));
            noteMapper.delete(new QueryWrapper<Note>().eq("song_id", songId));
            changeBackgroundOperationMapper.delete(new QueryWrapper<ChangeBackgroundOperation>().eq("song_id", songId));
            changeColorOperationMapper.delete(new QueryWrapper<ChangeColorOperation>().eq("song_id", songId));
            changeWidthOperationMapper.delete(new QueryWrapper<ChangeWidthOperation>().eq("song_id", songId));
            moveOperationMapper.delete(new QueryWrapper<MoveOperation>().eq("song_id", songId));

            // 插入
            for (ChangeBackgroundOperationDTO changeBackgroundOperationDTO : chartContentDTO.getChangeBackgroundOperations()) {
                ChangeBackgroundOperation changeBackgroundOperation = new ChangeBackgroundOperation();
                changeBackgroundOperation.setSongId(songId);
                changeBackgroundOperation.setStartTime(changeBackgroundOperationDTO.getStartTime());
                changeBackgroundOperation.setBackground(changeBackgroundOperationDTO.getBackground());
                changeBackgroundOperationMapper.insert(changeBackgroundOperation);
            }
            
            int notesCount = 0;
            for (TrackDTO trackDTO : chartContentDTO.getTracks()) {
                Track track = new Track();
                track.setSongId(songId);
                track.setType(trackDTO.getType());
                track.setKeyX(trackDTO.getKey());
                track.setStartTiming(trackDTO.getStartTiming());
                track.setEndTiming(trackDTO.getEndTiming());
                track.setPositionX(trackDTO.getPositionX());
                track.setWidth(trackDTO.getWidth());
                track.setR(trackDTO.getR());
                track.setG(trackDTO.getG());
                track.setB(trackDTO.getB());
                trackMapper.insert(track);
                Integer trackId = track.getId();
                
                notesCount += trackDTO.getNotes().size();
                for (NoteDTO noteDTO : trackDTO.getNotes()) {
                    Note note = new Note();
                    note.setSongId(songId);
                    note.setBasedTrack(trackId);
                    note.setNoteType(noteDTO.getNoteType());
                    note.setKeyX(noteDTO.getKey());
                    note.setTiming(noteDTO.getTiming());
                    note.setEndTiming(noteDTO.getEndTiming());
                    noteMapper.insert(note);
                }

                for (MoveOperationDTO moveOperationDTO : trackDTO.getMoveOperations()) {
                    MoveOperation moveOperation = new MoveOperation();
                    moveOperation.setSongId(songId);
                    moveOperation.setBasedTrack(trackId);
                    moveOperation.setStartTime(moveOperationDTO.getStartTime());
                    moveOperation.setEndTime(moveOperationDTO.getEndTime());
                    moveOperation.setStartX(moveOperationDTO.getStartX());
                    moveOperation.setEndX(moveOperationDTO.getEndX());
                    moveOperationMapper.insert(moveOperation);
                }

                for (ChangeWidthOperationDTO changeWidthOperationDTO : trackDTO.getChangeWidthOperations()) {
                    ChangeWidthOperation changeWidthOperation = new ChangeWidthOperation();
                    changeWidthOperation.setSongId(songId);
                    changeWidthOperation.setBasedTrack(trackId);
                    changeWidthOperation.setStartTime(changeWidthOperationDTO.getStartTime());
                    changeWidthOperation.setEndTime(changeWidthOperationDTO.getEndTime());
                    changeWidthOperation.setStartWidth(changeWidthOperationDTO.getStartWidth());
                    changeWidthOperation.setEndWidth(changeWidthOperationDTO.getEndWidth());
                    changeWidthOperationMapper.insert(changeWidthOperation);
                }

                for (ChangeColorOperationDTO changeColorOperationDTO : trackDTO.getChangeColorOperations()) {
                    ChangeColorOperation changeColorOperation = new ChangeColorOperation();
                    changeColorOperation.setSongId(songId);
                    changeColorOperation.setBasedTrack(trackId);
                    changeColorOperation.setStartTime(changeColorOperationDTO.getStartTime());
                    changeColorOperation.setEndTime(changeColorOperationDTO.getEndTime());
                    changeColorOperation.setStartR(changeColorOperationDTO.getStartR());
                    changeColorOperation.setStartG(changeColorOperationDTO.getStartG());
                    changeColorOperation.setStartB(changeColorOperationDTO.getStartB());
                    changeColorOperation.setEndR(changeColorOperationDTO.getEndR());
                    changeColorOperation.setEndG(changeColorOperationDTO.getEndG());
                    changeColorOperation.setEndB(changeColorOperationDTO.getEndB());
                    changeColorOperationMapper.insert(changeColorOperation);
                }
            }

            song.setNotesCount(notesCount);
            songMapper.update(song, new QueryWrapper<Song>().eq("id", songId));

            return ReturnResponse.OK("更新谱面成功！");
        } catch (Exception e) {
            e.printStackTrace();
            return ReturnResponse.systemException(ReturnStatus.BUSINESS_EXCEPTION);
        }
    }

    @Override
    public ReturnResponse<BackgroundVO> uploadBackground(UploadBackgroundDTO uploadBackgroundDTO, HttpServletRequest request) {
        try {
            MultipartFile file = uploadBackgroundDTO.getBackground();
            BackgroundVO backgroundVO = new BackgroundVO();
            String realPath = "/data/jpg";

            File folder = new File(realPath);
            if (!folder.exists()) {
                folder.mkdirs();
            }
            String oldName = file.getOriginalFilename();
            String newName = UUID.randomUUID().toString() + oldName.substring(oldName.lastIndexOf("."));
            file.transferTo(new File(folder, newName));
            String url = request.getScheme() + ":localhost/jpg/" + newName;

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
