package team.javaee.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import team.javaee.common.config.ReturnResponse;
import team.javaee.common.enums.ReturnStatus;
import team.javaee.entity.domain.BestRecord;
import team.javaee.entity.domain.RecentRecord;
import team.javaee.entity.domain.Song;
import team.javaee.entity.domain.User;
import team.javaee.entity.dto.PlayResultDTO;
import team.javaee.entity.dto.SongDTO;
import team.javaee.entity.vo.*;
import team.javaee.mapper.BestRecordMapper;
import team.javaee.mapper.RecentRecordMapper;
import team.javaee.mapper.SongMapper;
import team.javaee.mapper.UserMapper;
import team.javaee.service.PlayService;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class PlayServiceImpl implements PlayService {
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private SongMapper songMapper;
    @Autowired
    private BestRecordMapper bestRecordMapper;
    @Autowired
    private RecentRecordMapper recentRecordMapper;
    @Override
    public ReturnResponse<ChartInfVO> getChartInfo(String userId, SongDTO songDTO) {
        try {
            String songId = songDTO.getSongId();
            Song song = songMapper.selectById(songId);
            ChartInfVO chartInfVO = new ChartInfVO();
            SongInfVO songInfVO = new SongInfVO();
            MyRecordVO myRecordVO = new MyRecordVO();
            List<TenBestRecordsVO> tenBestRecordsVOS = new ArrayList<>();
            String uploader = userMapper.selectById(song.getUploaderId()).getUsername();

            songInfVO.setSongCover(song.getSongCover());
            songInfVO.setSongName(song.getSongName());
            songInfVO.setSongWriter(song.getSongWriter());
            songInfVO.setSongDifficulty(Math.floor(song.getChartConstant()));
            songInfVO.setUploader(uploader);

            BestRecord myBestRecord = bestRecordMapper.selectOne(
                    new QueryWrapper<BestRecord>().eq("user_id", userId).eq("song_id", songId)
            );
            if (myBestRecord != null) {
                myRecordVO.setBestScore(myBestRecord.getScore());
                myRecordVO.setRanking(bestRecordMapper.getRank(userId, songId));
                if (song.getNotesCount().equals(myBestRecord.getPure())) {
                    myRecordVO.setRecordStatus(3);
                } else if (song.getNotesCount().equals(myBestRecord.getCombo())) {
                    myRecordVO.setRecordStatus(2);
                } else {
                    myRecordVO.setRecordStatus(1);
                }
            } else {
                myRecordVO.setBestScore(0);
                myRecordVO.setRanking(0);
                myRecordVO.setRecordStatus(0);
            }

            List<RankVO> rankVOS = bestRecordMapper.getPlayerRank(
                    new QueryWrapper<BestRecord>().eq("song_id", songId).last("limit 10"));
            for (RankVO rankVO : rankVOS) {
                TenBestRecordsVO tenBestRecordsVO = new TenBestRecordsVO();
                tenBestRecordsVO.setBestScore(rankVO.getScore());
                tenBestRecordsVO.setRanking(rankVO.getRank());
                User user = userMapper.selectById(rankVO.getUserId());
                tenBestRecordsVO.setPlayer(user.getUsername());
                tenBestRecordsVO.setPlayerIcon(user.getIcon());
                if (song.getNotesCount().equals(rankVO.getPure())) {
                    tenBestRecordsVO.setRecordStatus(3);
                } else if (song.getNotesCount().equals(rankVO.getCombo())) {
                    tenBestRecordsVO.setRecordStatus(2);
                } else {
                    tenBestRecordsVO.setRecordStatus(1);
                }
                tenBestRecordsVOS.add(tenBestRecordsVO);
            }
            chartInfVO.setSongInfo(songInfVO);
            chartInfVO.setMyRecord(myRecordVO);
            chartInfVO.setTenBestRecords(tenBestRecordsVOS);

            return ReturnResponse.OK(chartInfVO);
        } catch (Exception e) {
            return ReturnResponse.systemException(ReturnStatus.BUSINESS_EXCEPTION);
        }
    }

    @Override
    public ReturnResponse<PlayResultVO> uploadRecord(PlayResultDTO playResultDTO) {
        try {
            String userId = playResultDTO.getUserId();
            String songId = playResultDTO.getSongId();
            int score = playResultDTO.getScore();
            int pure = playResultDTO.getPure();
            int far = playResultDTO.getFar();
            int lost = playResultDTO.getLost();
            int combo = playResultDTO.getCombo();
            double potential = 0.0f;
            double allPotential = 0.0f;
            Song song = songMapper.selectById(songId);
            double chartConstant = (double) song.getChartConstant();
            if (score == 10000000) {
                potential = chartConstant + 2;
            } else if (score >= 9800000 && score < 10000000) {
                potential = chartConstant + 1 + (double) (score - 9800000) / 200000;
            } else {
                double tmp = (double) (score - 9500000) / 300000;
                potential = chartConstant + tmp < 0.0f ?  0.0f : chartConstant + tmp;
            }
            PlayResultVO playResultVO = new PlayResultVO();
            BestRecord myBestRecord = bestRecordMapper.selectOne(
                    new QueryWrapper<BestRecord>().eq("user_id", userId).eq("song_id", songId)
            );
            if (myBestRecord == null) {
                playResultVO.setFormerBestScore(0);
                bestRecordMapper.insert(new BestRecord(Integer.parseInt(songId), userId, score, pure, far, lost, combo, (float) potential, LocalDateTime.now()));
            } else {
                playResultVO.setFormerBestScore(myBestRecord.getScore());
                if (score >= myBestRecord.getScore()) {
                    bestRecordMapper.delete(new QueryWrapper<BestRecord>().eq("user_id", userId).eq("song_id", songId));
                    bestRecordMapper.insert(new BestRecord(Integer.parseInt(songId), userId, score, pure, far, lost, combo, (float) potential, LocalDateTime.now()));
                }
            }
            if (song.getStatus() == 2) {
                if (recentRecordMapper.selectList(new QueryWrapper<RecentRecord>().eq("user_id", userId).eq("song_id", songId)).size() >= 10) {
                    recentRecordMapper.deleteById(
                            recentRecordMapper.selectList(new QueryWrapper<RecentRecord>().eq("user_id", userId).eq("song_id", songId).orderByAsc("create_time")).get(0).getId()
                    );
                }
                recentRecordMapper.insert(
                        new RecentRecord(Integer.parseInt(songId), userId, score, pure, far, lost, combo, potential, LocalDateTime.now())
                );
            }

            List<RecentRecord> recentRecords = recentRecordMapper.selectList(
                    new QueryWrapper<RecentRecord>().orderByDesc("create_time").eq("user_id", userId)
            );
            List<RecentRecord> tenRecentRecords = new ArrayList<>();
            List<Integer> recentSongs = new ArrayList<>();
            for (RecentRecord recentRecord : recentRecords) {
                if (!recentSongs.contains(recentRecord.getSongId())) {
                    recentSongs.add(recentRecord.getSongId());
                }
                if (recentSongs.size() == 10) {
                    break;
                }
            }
            for (Integer recentSongId : recentSongs) {
                tenRecentRecords.add(recentRecordMapper.selectList(
                        new QueryWrapper<RecentRecord>().eq("song_id", recentSongId).eq("user_id", userId).orderByDesc("score")).get(0));
            }
            recordSort(tenRecentRecords);
            for (int i = 0; i < 5; i++) {
                if (tenRecentRecords.size() == i) {
                    break;
                }
                allPotential += tenRecentRecords.get(i).getPotential();
            }
            allPotential += bestRecordMapper.getTenBestRecordPotential(userId);
            allPotential = allPotential / 15.0;
            User user = userMapper.selectById(userId);
            user.setPotential((float) allPotential);
            userMapper.update(user, new QueryWrapper<User>().eq("user_id", userId));
            playResultVO.setPotential(allPotential);
            song.setPlayTime(song.getPlayTime() + 1);
            songMapper.update(song, new QueryWrapper<Song>().eq("id", songId));
            return ReturnResponse.OK(playResultVO);
        } catch (Exception e) {
            return ReturnResponse.systemException(ReturnStatus.BUSINESS_EXCEPTION);
        }
    }

    public void recordSort(List<RecentRecord> recentRecords) {
        recentRecords.sort(new Comparator<RecentRecord>() {
            @Override
            public int compare(RecentRecord o1, RecentRecord o2) {
                if (o1.getPotential() > o2.getPotential()) {
                    return -1;
                } else if (o1.getPotential() < o2.getPotential()){
                    return 1;
                } else {
                    return 0;
                }
            }
        });
    }
}
