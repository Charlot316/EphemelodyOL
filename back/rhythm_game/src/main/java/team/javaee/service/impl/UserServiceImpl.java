package team.javaee.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import team.javaee.common.Normal;
import team.javaee.common.config.ReturnResponse;
import team.javaee.common.enums.ReturnStatus;
import team.javaee.entity.domain.*;
import team.javaee.entity.dto.*;
import team.javaee.entity.vo.*;
import team.javaee.mapper.*;
import team.javaee.service.UserService;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.util.*;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author nwh
 * @since 2022-05-13
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private SongMapper songMapper;

    @Autowired
    private ChangeBackgroundOperationMapper changeBackgroundOperationMapper;

    @Autowired
    private ChangeColorOperationMapper changeColorOperationMapper;

    @Autowired
    private ChangeWidthOperationMapper changeWidthOperationMapper;

    @Autowired
    private MoveOperationMapper moveOperationMapper;

    @Autowired
    private TrackMapper trackMapper;

    @Autowired
    private NoteMapper noteMapper;

    @Autowired
    private BestRecordMapper bestRecordMapper;

    @Autowired
    private RecentRecordMapper recentRecordMapper;

    @Override
    public ReturnResponse<Object> login(HttpServletRequest request, HttpServletResponse response, LoginDTO loginDTO) {
        try{
            LoginVO loginVO = new LoginVO();
            String username = loginDTO.getUsername();
            String password = loginDTO.getPassword();
            QueryWrapper<User> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("username", username);
            User user = userMapper.selectOne(queryWrapper);
            if(user == null){
                return ReturnResponse.packageObject("用户名不存在", ReturnStatus.FAILURE);
            }
            else{
                if(!user.getPassword().equals(password)){
                    return ReturnResponse.packageObject("密码错误", ReturnStatus.FAILURE);
                }
                else{
                    Cookie cookie = new Cookie("userId", user.getUserId() + "");
                    cookie.setPath("/");
                    response.addCookie(cookie);
                    loginVO.setIcon(user.getIcon());
                    loginVO.setPotential(String.valueOf(user.getPotential()));
                    loginVO.setIsAdmin(user.getIsAdmin());
                    loginVO.setUserId(user.getUserId());
                    return ReturnResponse.OK(loginVO);
                }
            }
        } catch (Exception e){
            return ReturnResponse.systemException(ReturnStatus.BUSINESS_EXCEPTION);
        }
    }

    @Override
    public ReturnResponse<String> register(UserDTO userDTO) {
        try{
            String username = userDTO.getUsername();
            QueryWrapper<User> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("username", username);
            if(userMapper.selectOne(queryWrapper) != null){
                return ReturnResponse.packageObject("用户名已存在", ReturnStatus.FAILURE);
            }
            else{
                User user = new User();
                user.setUserId(UUID.randomUUID().toString());
                user.setUsername(username);
                user.setPassword(userDTO.getPassword());
                user.setIsAdmin(0);
                user.setPotential((float) 0);
                userMapper.insert(user);
                return ReturnResponse.OK("注册成功");
            }
        } catch (Exception e){
            e.printStackTrace();
            return ReturnResponse.systemException(ReturnStatus.BUSINESS_EXCEPTION);
        }
    }

    @Override
    public ReturnResponse<String> password(String userId, PasswordDTO passwordDTO) {
        try{
            UpdateWrapper<User> userUpdateWrapper = new UpdateWrapper<>();
            String newPassword = passwordDTO.getNewPassword();
            String oldPassword = passwordDTO.getOldPassword();
            userUpdateWrapper.eq("user_id",userId);
            User user = userMapper.selectOne(userUpdateWrapper);
            if(user.getPassword().equals(oldPassword)){
                user.setPassword(newPassword);
                userMapper.update(user, userUpdateWrapper);
                return ReturnResponse.OK("成功修改个人密码");
            }
            else{
                return ReturnResponse.packageObject("旧密码错误", ReturnStatus.FAILURE);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ReturnResponse.systemException(ReturnStatus.BUSINESS_EXCEPTION);
        }
    }

    @Override
    public ReturnResponse<UserVO> getUserInformation(String userId) {
        try{
            QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
            userQueryWrapper.eq("user_id", userId);
            User user = userMapper.selectOne(userQueryWrapper);
            UserVO userVO = new UserVO();
            userVO.setUserId(userId);
            userVO.setIconUrl(user.getIcon());
            userVO.setUsername(user.getUsername());
            userVO.setPassword(user.getPassword());
            return ReturnResponse.OK(userVO);
        } catch (Exception e){
            e.printStackTrace();
            return ReturnResponse.systemException(ReturnStatus.BUSINESS_EXCEPTION);
        }
    }

    @Override
    public ReturnResponse<ImageVO> imgUpload(@Param("file") MultipartFile file, HttpServletRequest req) {
        try {
            String userId = Objects.requireNonNull(Normal.getUserIdByCookie(req));
            ImageVO imageVO = new ImageVO();
            String realPath = "/data/image/user/";
//            String realPath = "E:\\新建文件夹\\j2ee_springboot\\rhythm_game\\src\\main\\resources\\static\\files\\";
//            String realPath = "D:\\资料\\3 大三下\\j2ee架构\\大作业\\j2ee_springboot\\rhythm_game\\src\\main\\resources\\static\\files\\";
            File folder = new File(realPath);
            if (!folder.exists()) {
                folder.mkdirs();
            }
            String oldName = file.getOriginalFilename();
            User user = userMapper.selectById(userId);
            String newName = UUID.randomUUID().toString() + oldName.substring(oldName.lastIndexOf("."));
            file.transferTo(new File(folder, newName));
            String url = req.getScheme() + "://47.113.89.104:8090/image/user/" + newName;
            System.out.println("用户名为"+userId);
            user.setIcon(url);
            System.out.println(url);
            QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
            userQueryWrapper.eq("user_id",user.getUserId());
            userMapper.update(user,userQueryWrapper);
            imageVO.setUrl(url);
            return ReturnResponse.OK(imageVO);
        } catch (Exception e) {
            e.printStackTrace();
            return ReturnResponse.systemException(ReturnStatus.BUSINESS_EXCEPTION);
        }
    }

    @Override
    public ReturnResponse<String> chartPublicise(String userId, ChartChangeDTO chartChangeDTO) {
        try{
            String songId = chartChangeDTO.getSongId();
            UpdateWrapper<Song> songUpdateWrapper = new UpdateWrapper<>();
            songUpdateWrapper.eq("id",songId);
            Song song = songMapper.selectOne(songUpdateWrapper);
            if(song.getUploaderId().equals(userId)){
                song.setStatus(1);
                songMapper.update(song, songUpdateWrapper);
                return ReturnResponse.OK("公开谱面成功");
            }
            else{
                return ReturnResponse.packageObject("不是对应上传者", ReturnStatus.FAILURE);
            }
        } catch (Exception e){
            e.printStackTrace();
            return ReturnResponse.systemException(ReturnStatus.BUSINESS_EXCEPTION);
        }
    }

    @Override
    public ReturnResponse<String> privatizeChart(String userId, ChartChangeDTO chartChangeDTO) {
        try{
            String songId = chartChangeDTO.getSongId();
            UpdateWrapper<Song> songUpdateWrapper = new UpdateWrapper<>();
            songUpdateWrapper.eq("id",songId);
            Song song = songMapper.selectOne(songUpdateWrapper);
            if(song.getUploaderId().equals(userId)){
                song.setStatus(0);
                songMapper.update(song, songUpdateWrapper);
                return ReturnResponse.OK("私有化谱面成功");
            }
            else{
                return ReturnResponse.packageObject("不是对应上传者", ReturnStatus.FAILURE);
            }
        } catch (Exception e){
            e.printStackTrace();
            return ReturnResponse.systemException(ReturnStatus.BUSINESS_EXCEPTION);
        }
    }

    @Override
    public ReturnResponse<SelfSongsListVO> getMyCharts(String userId, PublicChartsDTO publicChartsDTO) {
        try{
            int status = publicChartsDTO.getStatus();
            String searchType = publicChartsDTO.getSearchType();
            String searchContent = publicChartsDTO.getSearchContent();
            String sortType = publicChartsDTO.getSortType();
            String sortWay = publicChartsDTO.getSortWay();
            QueryWrapper<Song> songQueryWrapper = new QueryWrapper<>();
            songQueryWrapper.eq("uploader_id",userId);
            songQueryWrapper.eq("status", status);
            if(searchType.equals("0")){
                songQueryWrapper.eq("song_name", searchContent);
            }
            else if(searchType.equals("1")){
                songQueryWrapper.eq("song_writer", searchContent);
            }
            else if(searchType.equals("3")){
                songQueryWrapper.eq("chart_constant", searchContent);
            }
            List<Song> songs = songMapper.selectList(songQueryWrapper);
            List<SongVO> songsReturn = new ArrayList<>();
            SelfSongsListVO selfSongsListVO = new SelfSongsListVO();
            for(Song song : songs){
                SongVO songVO = new SongVO();
                songVO.setSongId(song.getId());
                songVO.setSongName(song.getSongName());
                songVO.setUploaderId(song.getUploaderId());
                songVO.setSongWriter(song.getSongWriter());
                songVO.setSongCover(song.getSongCover());
                songVO.setLoadingText(song.getLoadingText());
                songVO.setNotesCount(song.getNotesCount());
                songVO.setSongLength(song.getSongLength());
                songVO.setDefaultBackground(song.getDefaultBackground());
                songVO.setSongUrl(song.getSongUrl());
                songVO.setStatus(String.valueOf(song.getStatus()));
                songsReturn.add(songVO);
            }
            if(sortType.equals("0")){
                if(sortWay.equals("0")){
                    songsReturn.sort((a,b)->a.getSongName().compareTo(b.getSongName()));
                }
                else{
                    songsReturn.sort((b,a)->a.getSongName().compareTo(b.getSongName()));
                }
            }
            else if(sortType.equals("1")){
                if(sortWay.equals("0")){
                    songsReturn.sort((a,b)->a.getSongWriter().compareTo(b.getSongWriter()));
                }
                else{
                    songsReturn.sort((b,a)->a.getSongWriter().compareTo(b.getSongWriter()));
                }
            }
            selfSongsListVO.setCharts(songsReturn);
            return ReturnResponse.OK(selfSongsListVO);
        } catch (Exception e){
            e.printStackTrace();
            return ReturnResponse.systemException(ReturnStatus.BUSINESS_EXCEPTION);
        }
    }

    @Override
    public ReturnResponse<SelfSongsListVO> getAllMyCharts(String userId) {
        try{
            UpdateWrapper<Song> songWrapper = new UpdateWrapper<>();
            songWrapper.eq("uploader_id",userId);
            List<Song> songs = songMapper.selectList(songWrapper);
            List<SongVO> songsReturn = new ArrayList<>();
            SelfSongsListVO selfSongsListVO = new SelfSongsListVO();
            for(Song song : songs){
                SongVO songVO = new SongVO();
                songVO.setSongId(song.getId());
                songVO.setSongName(song.getSongName());
                songVO.setUploaderId(song.getUploaderId());
                songVO.setSongWriter(song.getSongWriter());
                songVO.setSongCover(song.getSongCover());
                songVO.setLoadingText(song.getLoadingText());
                songVO.setNotesCount(song.getNotesCount());
                songVO.setSongLength(song.getSongLength());
                songVO.setDefaultBackground(song.getDefaultBackground());
                songVO.setSongUrl(song.getSongUrl());
                songVO.setStatus(String.valueOf(song.getStatus()));
                songVO.setChartConstant(String.valueOf(song.getChartConstant()));
                songVO.setLoadedText(song.getLoadedText());
                songsReturn.add(songVO);
            }
            selfSongsListVO.setCharts(songsReturn);
            return ReturnResponse.OK(selfSongsListVO);
        } catch (Exception e){
            e.printStackTrace();
            return ReturnResponse.systemException(ReturnStatus.BUSINESS_EXCEPTION);
        }
    }

    @Override
    public ReturnResponse<SelfSongsListVO> getAllCharts() {
        try{
            UpdateWrapper<Song> songWrapper = new UpdateWrapper<>();
            songWrapper.eq("status","1");
            UpdateWrapper<Song> songWrapper1 = new UpdateWrapper<>();
            songWrapper1.eq("status","2");
            List<Song> songs = songMapper.selectList(songWrapper);
            List<Song> songs1 = songMapper.selectList(songWrapper1);
            songs.addAll(songs1);
            List<SongVO> songsReturn = new ArrayList<>();
            SelfSongsListVO selfSongsListVO = new SelfSongsListVO();
            for(Song song : songs){
                SongVO songVO = new SongVO();
                songVO.setSongId(song.getId());
                songVO.setSongName(song.getSongName());
                songVO.setUploaderId(song.getUploaderId());
                songVO.setSongWriter(song.getSongWriter());
                songVO.setSongCover(song.getSongCover());
                songVO.setLoadingText(song.getLoadingText());
                songVO.setNotesCount(song.getNotesCount());
                songVO.setSongLength(song.getSongLength());
                songVO.setDefaultBackground(song.getDefaultBackground());
                songVO.setSongUrl(song.getSongUrl());
                songVO.setStatus(String.valueOf(song.getStatus()));
                songVO.setChartConstant(String.valueOf(song.getChartConstant()));
                songVO.setLoadedText(song.getLoadedText());
                songsReturn.add(songVO);
            }
            selfSongsListVO.setCharts(songsReturn);
            return ReturnResponse.OK(selfSongsListVO);
        } catch (Exception e){
            e.printStackTrace();
            return ReturnResponse.systemException(ReturnStatus.BUSINESS_EXCEPTION);
        }
    }

    @Override
    public ReturnResponse<SingleSongVO> getChart(SongDTO songDTO) {
        try{
            String songId = songDTO.getSongId();
            QueryWrapper<Song> songWrapper = new QueryWrapper<>();
            songWrapper.eq("id",Integer.valueOf(songId));
            Song song = songMapper.selectOne(songWrapper);
            SingleSongVO singleSongVO = new SingleSongVO();
            singleSongVO.setSongName(song.getSongName());
            singleSongVO.setSongId(song.getId());
            QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
            userQueryWrapper.eq("user_id",song.getUploaderId());
            User user = userMapper.selectOne(userQueryWrapper);
            singleSongVO.setUploader(user.getUsername());
            singleSongVO.setSongWriter(song.getSongWriter());
            singleSongVO.setSongCover(song.getSongCover());
            singleSongVO.setLoadingText(song.getLoadingText());
            singleSongVO.setLoadedText(song.getLoadedText());
            singleSongVO.setNotesCount(song.getNotesCount());
            singleSongVO.setSongLength(song.getSongLength());
            singleSongVO.setDefaultBackground(song.getDefaultBackground());
            singleSongVO.setSongUrl(song.getSongUrl());
            singleSongVO.setBPM(song.getBPM());
            singleSongVO.setFirstBeatDelay(song.getFirstBeatDelay());
            List<ChangeBackgroundOperation> changeBackgroundOperationList = new ArrayList<>();
            QueryWrapper<ChangeBackgroundOperation> changeBackgroundOperationQueryWrapper = new QueryWrapper<>();
            changeBackgroundOperationQueryWrapper.eq("song_id", songId);
            changeBackgroundOperationList = changeBackgroundOperationMapper.selectList(changeBackgroundOperationQueryWrapper);
            List<ChangeBackgroundOperationsVO> changeBackgroundOperations = new ArrayList<>();
            for(ChangeBackgroundOperation item : changeBackgroundOperationList){
                ChangeBackgroundOperationsVO changeBackgroundOperation = new ChangeBackgroundOperationsVO();
                changeBackgroundOperation.setId(item.getId());
                changeBackgroundOperation.setStartTime(item.getStartTime());
                changeBackgroundOperation.setBackground(item.getBackground());
                changeBackgroundOperations.add(changeBackgroundOperation);
            }
            singleSongVO.setChangeBackgroundOperations(changeBackgroundOperations);
            List<TracksVO> tracksVOList = new ArrayList<>();
            List<Track> trackList = new ArrayList<>();
            QueryWrapper<Track> trackQueryWrapper = new QueryWrapper<>();
            trackQueryWrapper.eq("song_id", songId);
            trackList = trackMapper.selectList(trackQueryWrapper);
            for(Track item : trackList){
                TracksVO tracksVO = new TracksVO();
                tracksVO.setId(item.getId());
                tracksVO.setType(item.getType());
                tracksVO.setKey(item.getKeyX());
                tracksVO.setStartTiming(item.getStartTiming());
                tracksVO.setEndTiming(item.getEndTiming());
                tracksVO.setPositionX(String.valueOf(item.getPositionX()));
                tracksVO.setWidth(String.valueOf(item.getWidth()));
                tracksVO.setR(item.getR());
                tracksVO.setG(item.getG());
                tracksVO.setB(item.getB());
                List<NotesVO> notesVOList = new ArrayList<>();
                List<Note> noteList = new ArrayList<>();
                QueryWrapper<Note> noteQueryWrapper = new QueryWrapper<>();
                noteQueryWrapper.eq("song_id", songId);
                noteQueryWrapper.eq("based_track", item.getId());
                noteList = noteMapper.selectList(noteQueryWrapper);
                for(Note it : noteList){
                    NotesVO notesVO = new NotesVO();
                    notesVO.setId(it.getId());
                    notesVO.setNoteType(it.getNoteType());
                    notesVO.setKey(it.getKeyX());
                    notesVO.setTiming(it.getTiming());
                    notesVO.setEndTiming(it.getEndTiming());
                    notesVOList.add(notesVO);
                }
                tracksVO.setNotes(notesVOList);
                List<MoveOperationsVO> moveOperationsVOList = new ArrayList<>();
                QueryWrapper<MoveOperation> moveOperationQueryWrapper = new QueryWrapper<>();
                moveOperationQueryWrapper.eq("song_id", songId);
                moveOperationQueryWrapper.eq("based_track", item.getId());
                List<MoveOperation> moveOperationList = moveOperationMapper.selectList(moveOperationQueryWrapper);
                for(MoveOperation x : moveOperationList){
                    MoveOperationsVO moveOperationsVO = new MoveOperationsVO();
                    moveOperationsVO.setId(x.getId());
                    moveOperationsVO.setStartTime(x.getStartTime());
                    moveOperationsVO.setEndTime(x.getEndTime());
                    moveOperationsVO.setEndX(String.valueOf(x.getEndX()));
                    moveOperationsVO.setStartX(String.valueOf(x.getStartX()));
                    moveOperationsVOList.add(moveOperationsVO);
                }
                tracksVO.setMoveOperations(moveOperationsVOList);
                List<ChangeWidthOperationsVO> changeWidthOperationsVOList = new ArrayList<>();
                QueryWrapper<ChangeWidthOperation> changeWidthOperationQueryWrapper = new QueryWrapper<>();
                changeWidthOperationQueryWrapper.eq("song_id", songId);
                changeWidthOperationQueryWrapper.eq("based_track", item.getId());
                List<ChangeWidthOperation> changeWidthOperationList = changeWidthOperationMapper.selectList(changeWidthOperationQueryWrapper);
                for(ChangeWidthOperation y : changeWidthOperationList){
                    ChangeWidthOperationsVO changeWidthOperationsVO = new ChangeWidthOperationsVO();
                    changeWidthOperationsVO.setId(y.getId());
                    changeWidthOperationsVO.setStartTime(y.getStartTime());
                    changeWidthOperationsVO.setEndTime(y.getEndTime());
                    changeWidthOperationsVO.setEndWidth(String.valueOf(y.getEndWidth()));
                    changeWidthOperationsVO.setStartWidth(String.valueOf(y.getStartWidth()));
                    changeWidthOperationsVOList.add(changeWidthOperationsVO);
                }
                tracksVO.setChangeWidthOperations(changeWidthOperationsVOList);
                List<ChangeColorOperationsVO> changeColorOperationsVOList = new ArrayList<>();
                QueryWrapper<ChangeColorOperation> changeColorOperationQueryWrapper = new QueryWrapper<>();
                changeColorOperationQueryWrapper.eq("song_id", songId);
                changeColorOperationQueryWrapper.eq("based_track", item.getId());
                List<ChangeColorOperation> changeColorOperationList = changeColorOperationMapper.selectList(changeColorOperationQueryWrapper);
                for(ChangeColorOperation z : changeColorOperationList){
                    ChangeColorOperationsVO changeColorOperationsVO = new ChangeColorOperationsVO();
                    changeColorOperationsVO.setId(z.getId());
                    changeColorOperationsVO.setStartTime(z.getStartTime());
                    changeColorOperationsVO.setEndTime(z.getEndTime());
                    changeColorOperationsVO.setEndR(z.getEndR());
                    changeColorOperationsVO.setEndG(z.getEndG());
                    changeColorOperationsVO.setEndB(z.getEndB());
                    changeColorOperationsVO.setStartR(z.getStartR());
                    changeColorOperationsVO.setStartG(z.getStartG());
                    changeColorOperationsVO.setStartB(z.getStartB());;
                    changeColorOperationsVOList.add(changeColorOperationsVO);
                }
                tracksVO.setChangeColorOperations(changeColorOperationsVOList);
                tracksVOList.add(tracksVO);
            }
            singleSongVO.setTracks(tracksVOList);
            return ReturnResponse.OK(singleSongVO);
        } catch (Exception e){
            e.printStackTrace();
            return ReturnResponse.systemException(ReturnStatus.BUSINESS_EXCEPTION);
        }
    }

    @Override
    public ReturnResponse<String> deleteChart(String userId, SongDTO songDTO) {
        try{
            String songId = songDTO.getSongId();
            QueryWrapper<Song> songQueryWrapper = new QueryWrapper<>();
            songQueryWrapper.eq("id", songId);
            Song song = songMapper.selectOne(songQueryWrapper);
            if(!song.getUploaderId().equals(userId)){
                return ReturnResponse.packageObject("不是对应上传者", ReturnStatus.FAILURE);
            }
            else{
                QueryWrapper<BestRecord> bestRecordQueryWrapper = new QueryWrapper<>();
                bestRecordQueryWrapper.eq("song_id", songId);
                bestRecordMapper.delete(bestRecordQueryWrapper);
                QueryWrapper<ChangeBackgroundOperation> changeBackgroundOperationQueryWrapper = new QueryWrapper<>();
                changeBackgroundOperationQueryWrapper.eq("song_id", songId);
                changeBackgroundOperationMapper.delete(changeBackgroundOperationQueryWrapper);
                QueryWrapper<ChangeColorOperation> changeColorOperationQueryWrapper = new QueryWrapper<>();
                changeColorOperationQueryWrapper.eq("song_id", songId);
                changeColorOperationMapper.delete(changeColorOperationQueryWrapper);
                QueryWrapper<ChangeWidthOperation> changeWidthOperationQueryWrapper = new QueryWrapper<>();
                changeWidthOperationQueryWrapper.eq("song_id", songId);
                changeWidthOperationMapper.delete(changeWidthOperationQueryWrapper);
                QueryWrapper<MoveOperation> moveOperationQueryWrapper = new QueryWrapper<>();
                moveOperationQueryWrapper.eq("song_id", songId);
                moveOperationMapper.delete(moveOperationQueryWrapper);
                QueryWrapper<Note> noteQueryWrapper = new QueryWrapper<>();
                noteQueryWrapper.eq("song_id", songId);
                noteMapper.delete(noteQueryWrapper);
                QueryWrapper<RecentRecord> recentRecordQueryWrapper = new QueryWrapper<>();
                recentRecordQueryWrapper.eq("song_id", songId);
                recentRecordMapper.delete(recentRecordQueryWrapper);
                QueryWrapper<Song> songQueryWrapper1 = new QueryWrapper<>();
                songQueryWrapper1.eq("id", songId);
                songMapper.delete(songQueryWrapper1);
                QueryWrapper<Track> trackQueryWrapper = new QueryWrapper<>();
                trackQueryWrapper.eq("song_id", songId);
                trackMapper.delete(trackQueryWrapper);
                return ReturnResponse.OK("删除谱面");
            }
        } catch (Exception e){
            e.printStackTrace();
            return ReturnResponse.systemException(ReturnStatus.BUSINESS_EXCEPTION);
        }
    }

    @Override
    public ReturnResponse<PublicChartsVO> getPublicCharts(String userId, PublicChartsDTO publicChartsDTO) {
        try{
            PublicChartsVO publicChartsVO = new PublicChartsVO();
            int status = publicChartsDTO.getStatus();
            String searchType = publicChartsDTO.getSearchType();
            String searchContent = publicChartsDTO.getSearchContent();
            String sortType = publicChartsDTO.getSortType();
            String sortWay = publicChartsDTO.getSortWay();
            QueryWrapper<Song> songQueryWrapper = new QueryWrapper<>();
            songQueryWrapper.eq("status", String.valueOf(status));
            if(searchType.equals("0")){
                songQueryWrapper.eq("song_name", searchContent);
            }
            else if(searchType.equals("1")){
                songQueryWrapper.eq("song_writer", searchContent);
            }
            else if(searchType.equals("2")){
                QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
                userQueryWrapper.eq("username", searchContent);
                User user = userMapper.selectOne(userQueryWrapper);
                songQueryWrapper.eq("uploader_id", user.getUserId());
            }
            else if(searchType.equals("3")){
                songQueryWrapper.eq("chart_constant", searchContent);
            }
            List<Song> songList = songMapper.selectList(songQueryWrapper);
            List<SongsVO> songsVOList = new ArrayList<>();
            for(Song song : songList){
                SongsVO songsVO = new SongsVO();
                SongInfoVO songInfoVO = new SongInfoVO();
                songInfoVO.setSongName(song.getSongName());
                User user = userMapper.selectById(song.getUploaderId());
                songInfoVO.setUploader(user.getUsername());
                songInfoVO.setSongCover(song.getSongCover());
                songInfoVO.setSongWriter(song.getSongWriter());
                songInfoVO.setSongDifficulty(String.valueOf(song.getChartConstant()));
                songInfoVO.setSongId(String.valueOf(song.getId()));
                songInfoVO.setDefaultBackground(song.getDefaultBackground());
                songInfoVO.setChartConstant(String.valueOf(song.getChartConstant()));
                songInfoVO.setLoadedText(song.getLoadedText());
                songInfoVO.setLoadingText(song.getLoadingText());
                songInfoVO.setStatus(String.valueOf(song.getStatus()));
                songsVO.setSongInfo(songInfoVO);
                MyRecordVO1 myRecordVO1 = new MyRecordVO1();
                BestRecord bestRecord = new BestRecord();
                QueryWrapper<BestRecord> bestRecordQueryWrapper = new QueryWrapper<>();
                bestRecordQueryWrapper.eq("song_id", song.getId());
                bestRecordQueryWrapper.eq("user_id", userId);
                bestRecord = bestRecordMapper.selectOne(bestRecordQueryWrapper);
                if(bestRecord == null){
                    myRecordVO1.setBestScore("0");
                    myRecordVO1.setRecordStatus("0");
                }
                else{
                    myRecordVO1.setBestScore(String.valueOf(bestRecord.getScore()));
                    if(bestRecord.getCombo() == song.getNotesCount()){
                        myRecordVO1.setRecordStatus("2");
                    }
                    else if(bestRecord.getPure() == song.getNotesCount()){
                        myRecordVO1.setRecordStatus("3");
                    }
                    else{
                        myRecordVO1.setRecordStatus("1");
                    }
                }
                QueryWrapper<BestRecord> wrapper1 = new QueryWrapper<>();
                wrapper1.eq("song_id", song.getId());
                List<BestRecord> bestRecordList = bestRecordMapper.selectList(wrapper1);
                Collections.sort(bestRecordList, (a,b)->{
                    return b.getScore() - a.getScore();
                });
                int ranking = 0;
                for(BestRecord item : bestRecordList){
                    ranking++;
                    if(item.getUserId().equals(userId)){
                        break;
                    }
                }
                myRecordVO1.setRanking(String.valueOf(ranking));
                songsVO.setMyRecord(myRecordVO1);
                List<TenBestRecordsVO> tenBestRecordsVOList = new ArrayList<>();
                int maxLen = 10;
                if(bestRecordList.size() <= 10){
                    maxLen = bestRecordList.size();
                }
                for(int i = 0; i < maxLen; i++){
                    TenBestRecordsVO tenBestRecordsVO = new TenBestRecordsVO();
                    tenBestRecordsVO.setBestScore(bestRecordList.get(i).getScore());
                    tenBestRecordsVO.setRanking(i + 1);
                    QueryWrapper<User> q = new QueryWrapper<>();
                    q.eq("user_id", bestRecordList.get(i).getUserId());
                    User u = userMapper.selectOne(q);
                    tenBestRecordsVO.setPlayerIcon(u.getIcon());
                    tenBestRecordsVO.setPlayer(u.getUsername());
                    if(bestRecordList.get(i).getCombo() == song.getNotesCount()){
                        tenBestRecordsVO.setRecordStatus(2);
                    }
                    else if(bestRecordList.get(i).getPure() == song.getNotesCount()){
                        tenBestRecordsVO.setRecordStatus(3);
                    }
                    else{
                        tenBestRecordsVO.setRecordStatus(1);
                    }
                    tenBestRecordsVOList.add(tenBestRecordsVO);
                }
                songsVO.setTenBestRecords(tenBestRecordsVOList);
                songsVOList.add(songsVO);
            }
            if(sortType.equals("0")){
                if(sortWay.equals("0")){
                    songsVOList.sort((a,b)->a.getSongInfo().getSongName().compareTo(b.getSongInfo().getSongName()));
                }
                else{
                    songsVOList.sort((b,a)->a.getSongInfo().getSongName().compareTo(b.getSongInfo().getSongName()));
                }
            }
            else if(sortType.equals("1")){
                if(sortWay.equals("0")){
                    songsVOList.sort((a,b)->a.getSongInfo().getSongWriter().compareTo(b.getSongInfo().getSongWriter()));
                }
                else{
                    songsVOList.sort((b,a)->a.getSongInfo().getSongWriter().compareTo(b.getSongInfo().getSongWriter()));
                }
            }
            else if(sortType.equals("2")){
                if(sortWay.equals("0")){
                    songsVOList.sort((a,b)->a.getSongInfo().getUploader().compareTo(b.getSongInfo().getUploader()));
                }
                else{
                    songsVOList.sort((b,a)->a.getSongInfo().getUploader().compareTo(b.getSongInfo().getUploader()));
                }
            }
            else if(sortType.equals("3")){
                if(sortWay.equals("0")){
                    songsVOList.sort((a,b)->a.getSongInfo().getSongDifficulty().compareTo(b.getSongInfo().getSongDifficulty()));
                }
                else{
                    songsVOList.sort((b,a)->a.getSongInfo().getSongDifficulty().compareTo(b.getSongInfo().getSongDifficulty()));
                }
            }
            publicChartsVO.setSongs(songsVOList);
            return ReturnResponse.OK(publicChartsVO);
        } catch (Exception e){
            e.printStackTrace();
            return ReturnResponse.systemException(ReturnStatus.BUSINESS_EXCEPTION);
        }
    }

}
