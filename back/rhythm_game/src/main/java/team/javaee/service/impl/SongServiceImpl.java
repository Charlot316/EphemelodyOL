package team.javaee.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import team.javaee.common.config.ReturnResponse;
import team.javaee.common.enums.ReturnStatus;
import team.javaee.entity.domain.*;
import team.javaee.entity.dto.*;
import team.javaee.entity.vo.BackgroundVO;
import team.javaee.entity.vo.ImageVO;
import team.javaee.mapper.*;
import team.javaee.service.FileStorageService;
import team.javaee.service.SongService;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class SongServiceImpl extends ServiceImpl<SongMapper, Song> implements SongService {

    private static final Logger log = LoggerFactory.getLogger(SongServiceImpl.class);

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

    @Autowired
    private SongAssetMapper songAssetMapper;

    @Autowired
    private FileStorageService fileStorageService;

    @Value("${web.upload-path}")
    private String uploadPath;

    @Value("${cloudflare.r2.public-url}")
    private String publicUrl;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public ReturnResponse<String> newChart(NewChartDTO newChartDTO) {
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
            return ReturnResponse.OK(song.getId());
        } catch (Exception e) {
            log.error("创建谱面失败", e);
            return ReturnResponse.systemException(ReturnStatus.BUSINESS_EXCEPTION);
        }
    }

    @Override
    public ReturnResponse<String> editChartInfo(EditChartDTO editChartDTO) {
        try {
            Song song = songMapper.selectById(editChartDTO.getSongId());
            if (song == null)
                return ReturnResponse.packageObject("谱面不存在", ReturnStatus.FAILURE);
            if (!editChartDTO.getUploaderId().equals(song.getUploaderId())) {
                return ReturnResponse.packageObject("您没有权利修改当前谱面", ReturnStatus.FAILURE);
            }
            if (editChartDTO.getSongUrl() != null && !editChartDTO.getSongUrl().equals(song.getSongUrl())) {
                fileStorageService.deleteFile(song.getSongUrl());
                song.setSongUrl(editChartDTO.getSongUrl());
            }
            if (editChartDTO.getDefaultBackground() != null
                    && !editChartDTO.getDefaultBackground().equals(song.getDefaultBackground())) {
                fileStorageService.deleteFile(song.getDefaultBackground());
                song.setDefaultBackground(editChartDTO.getDefaultBackground());
            }
            if (editChartDTO.getSongCover() != null && !editChartDTO.getSongCover().equals(song.getSongCover())) {
                fileStorageService.deleteFile(song.getSongCover());
                song.setSongCover(editChartDTO.getSongCover());
            }

            song.setSongName(editChartDTO.getSongName());
            song.setSongWriter(editChartDTO.getSongWriter());
            song.setLoadingText(editChartDTO.getLoadingText());
            song.setLoadedText(editChartDTO.getLoadedText());
            song.setChartConstant(editChartDTO.getChartConstant());
            songMapper.updateById(song);

            // [逻辑优化] 直接通过 SQL 清洗 song_asset 表，删除与封面/背景重复的资产项
            songAssetMapper.deleteDuplicateAssets(song.getId());

            return ReturnResponse.OK("更新成功！");
        } catch (Exception e) {
            log.error("更新谱面信息失败", e);
            return ReturnResponse.systemException(ReturnStatus.BUSINESS_EXCEPTION);
        }
    }

    @Override
    public ReturnResponse<ImageVO> uploadSongCover(MultipartFile file, String songId, HttpServletRequest request) {
        try {
            String url = fileStorageService.uploadFile(file, "covers");
            Song song = songMapper.selectById(songId);
            if (song != null) {
                if (song.getSongCover() != null && !song.getSongCover().isEmpty()) {
                    fileStorageService.deleteFile(song.getSongCover());
                }
                song.setSongCover(url);
                songMapper.updateById(song);
            }
            ImageVO vo = new ImageVO();
            vo.setUrl(url);
            return ReturnResponse.OK(vo);
        } catch (Exception e) {
            log.error("上传封面失败", e);
            return ReturnResponse.systemException(ReturnStatus.BUSINESS_EXCEPTION);
        }
    }

    @Override
    public ReturnResponse<ImageVO> uploadDefaultBackground(MultipartFile file, String songId,
            HttpServletRequest request) {
        try {
            String url = fileStorageService.uploadFile(file, "backgrounds");
            Song song = songMapper.selectById(songId);
            if (song != null) {
                if (song.getDefaultBackground() != null && !song.getDefaultBackground().isEmpty()) {
                    fileStorageService.deleteFile(song.getDefaultBackground());
                }
                song.setDefaultBackground(url);
                songMapper.updateById(song);
            }
            ImageVO vo = new ImageVO();
            vo.setUrl(url);
            return ReturnResponse.OK(vo);
        } catch (Exception e) {
            log.error("上传背景失败", e);
            return ReturnResponse.systemException(ReturnStatus.BUSINESS_EXCEPTION);
        }
    }

    @Override
    public ReturnResponse<String> uploadSong(MultipartFile file, String songId, HttpServletRequest request) {
        try {
            String url = fileStorageService.uploadFile(file, "mp3");
            Song song = songMapper.selectById(songId);
            if (song != null) {
                if (song.getSongUrl() != null && !song.getSongUrl().isEmpty()) {
                    fileStorageService.deleteFile(song.getSongUrl());
                }
                song.setSongUrl(url);
                songMapper.updateById(song);
            }
            return ReturnResponse.OK(url);
        } catch (Exception e) {
            log.error("上传音频失败", e);
            return ReturnResponse.systemException(ReturnStatus.BUSINESS_EXCEPTION);
        }
    }

    @Override
    public ReturnResponse<String> editChartContent(ChartContentDTO chartContentDTO) {
        try {
            String songId = chartContentDTO.getSongId();
            byte[] jsonBytes = objectMapper.writeValueAsBytes(chartContentDTO);
            String url = fileStorageService.uploadFile(jsonBytes, "charts/" + songId + ".json", "application/json");
            log.info("谱面 JSON 已同步至 R2: {}", url);

            String chartsFolder = uploadPath + "charts/";
            File folder = new File(chartsFolder);
            if (!folder.exists())
                folder.mkdirs();
            objectMapper.writeValue(new File(folder, songId + ".json"), chartContentDTO);

            Song song = songMapper.selectById(songId);
            if (song != null) {
                song.setBpm(chartContentDTO.getBpm());
                song.setFirstBeatDelay(chartContentDTO.getFirstBeatDelay());
                int notesCount = 0;
                if (chartContentDTO.getTracks() != null) {
                    for (TrackDTO t : chartContentDTO.getTracks()) {
                        if (t.getNotes() != null)
                            notesCount += t.getNotes().size();
                    }
                }
                song.setNotesCount(notesCount);
                songMapper.updateById(song);
            }

            // [逻辑完善] 物理删除所有标记为 isDeleted 的资源
            List<SongAsset> deletedAssets = songAssetMapper.selectList(
                    new QueryWrapper<SongAsset>().eq("song_id", songId).eq("is_deleted", 1));
            for (SongAsset asset : deletedAssets) {
                log.info("🚀 [Publish Clean] 正在物理删除资源: {}", asset.getName());
                fileStorageService.deleteFile(asset.getUrl());
                songAssetMapper.deleteById(asset.getId());
            }

            // [逻辑完善] 通过 SQL 直接清理重复的封面/背景资产项
            songAssetMapper.deleteDuplicateAssets(songId);

            return ReturnResponse.OK("发布谱面成功！云端文件已同步，已清理过期资源。");
        } catch (Exception e) {
            log.error("同步谱面失败", e);
            return ReturnResponse.systemException(ReturnStatus.BUSINESS_EXCEPTION);
        }
    }

    @Override
    public ReturnResponse<BackgroundVO> uploadBackground(UploadBackgroundDTO dto, HttpServletRequest request) {
        try {
            MultipartFile file = dto.getBackground();
            String url = fileStorageService.uploadFile(file, "assets");
            SongAsset asset = new SongAsset();
            asset.setSongId(dto.getSongId());
            asset.setName(file.getOriginalFilename());
            String cType = file.getContentType();
            asset.setType(cType != null && cType.startsWith("video") ? "video" : "image");
            asset.setUrl(url);
            songAssetMapper.insert(asset);

            ChangeBackgroundOperation op = new ChangeBackgroundOperation();
            op.setSongId(dto.getSongId());
            op.setAssetId(asset.getId());
            op.setStartTiming(dto.getStartTiming());
            changeBackgroundOperationMapper.insert(op);

            BackgroundVO vo = new BackgroundVO();
            vo.setBackground(url);
            return ReturnResponse.OK(vo);
        } catch (Exception e) {
            log.error("上传资源素材失败", e);
            return ReturnResponse.systemException(ReturnStatus.BUSINESS_EXCEPTION);
        }
    }

    @Override
    public ReturnResponse<String> accreditChart(SongDTO songDTO) {
        try {
            Song song = songMapper.selectById(songDTO.getSongId());
            if (song != null) {
                song.setStatus(2);
                songMapper.updateById(song);
            }
            return ReturnResponse.OK("认定成功");
        } catch (Exception e) {
            return ReturnResponse.systemException(ReturnStatus.BUSINESS_EXCEPTION);
        }
    }

    @Override
    public ReturnResponse<String> disAccreditChart(SongDTO songDTO) {
        try {
            Song song = songMapper.selectById(songDTO.getSongId());
            if (song != null) {
                song.setStatus(1);
                songMapper.updateById(song);
            }
            return ReturnResponse.OK("取消认定成功");
        } catch (Exception e) {
            return ReturnResponse.systemException(ReturnStatus.BUSINESS_EXCEPTION);
        }
    }

    @Override
    public ReturnResponse<String> deleteChart(SongDTO songDTO) {
        try {
            String songId = songDTO.getSongId();
            Song song = songMapper.selectById(songId);
            if (song != null) {
                // 删除 R2 上的相关资源
                fileStorageService.deleteFile(song.getSongCover());
                fileStorageService.deleteFile(song.getDefaultBackground());
                fileStorageService.deleteFile(song.getSongUrl());
                // 删除谱面 JSON (需要构建 R2 URL)
                String chartJsonUrl = publicUrl + (publicUrl.endsWith("/") ? "" : "/") + "ephemelody/charts/" + songId
                        + ".json";
                fileStorageService.deleteFile(chartJsonUrl);
            }

            // 删除关联的 SongAsset 文件 (R2)
            List<SongAsset> assets = songAssetMapper.selectList(new QueryWrapper<SongAsset>().eq("song_id", songId));
            for (SongAsset asset : assets) {
                fileStorageService.deleteFile(asset.getUrl());
            }

            songMapper.deleteById(songId);
            songAssetMapper.delete(new QueryWrapper<SongAsset>().eq("song_id", songId)); // 这一行也是新增的，确保数据库也删干净
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
            log.error("删除谱面失败", e);
            return ReturnResponse.systemException(ReturnStatus.BUSINESS_EXCEPTION);
        }
    }

    @Override
    public ReturnResponse<SongAsset> uploadAsset(MultipartFile file, String songId, String type,
            HttpServletRequest request) {
        try {
            String url = fileStorageService.uploadFile(file, "assets");
            SongAsset asset = new SongAsset();
            asset.setSongId(songId);
            asset.setName(file.getOriginalFilename());
            asset.setType(type);
            asset.setUrl(url);
            songAssetMapper.insert(asset);
            return ReturnResponse.OK(asset);
        } catch (Exception e) {
            log.error("上传素材失败", e);
            return ReturnResponse.systemException(ReturnStatus.BUSINESS_EXCEPTION);
        }
    }

    @Override
    public ReturnResponse<String> deleteAsset(Integer id) {
        try {
            SongAsset asset = songAssetMapper.selectById(id);
            if (asset != null) {
                // 不直接删除 R2 资源，仅在数据库标记为已删除
                // 待到用户点击“保存发布（editChartContent）”时再统一清理
                asset.setIsDeleted(1);
                songAssetMapper.updateById(asset);
                log.info("🗑️ [Soft Delete] 资源已标记删除: {}", asset.getName());
            }
            return ReturnResponse.OK("已标记删除，发布后生效");
        } catch (Exception e) {
            return ReturnResponse.systemException(ReturnStatus.BUSINESS_EXCEPTION);
        }
    }

    @Override
    @org.springframework.transaction.annotation.Transactional
    public ReturnResponse<String> resetChartFromJSON(String songId) {
        try {
            String r2Url = publicUrl + (publicUrl.endsWith("/") ? "" : "/") + "ephemelody/charts/" + songId + ".json";
            ChartContentDTO content = null;
            try {
                content = objectMapper.readValue(new URL(r2Url), ChartContentDTO.class);
            } catch (IOException e) {
                File localFile = new File(uploadPath + "charts/" + songId + ".json");
                if (localFile.exists())
                    content = objectMapper.readValue(localFile, ChartContentDTO.class);
            }

            if (content == null)
                return ReturnResponse.packageObject("找不到 JSON 备份", ReturnStatus.FAILURE);

            noteMapper.delete(new QueryWrapper<Note>().eq("song_id", songId));
            moveOperationMapper.delete(new QueryWrapper<MoveOperation>().eq("song_id", songId));
            changeWidthOperationMapper.delete(new QueryWrapper<ChangeWidthOperation>().eq("song_id", songId));
            changeColorOperationMapper.delete(new QueryWrapper<ChangeColorOperation>().eq("song_id", songId));
            changeBackgroundOperationMapper.delete(new QueryWrapper<ChangeBackgroundOperation>().eq("song_id", songId));
            trackMapper.delete(new QueryWrapper<Track>().eq("song_id", songId));

            Map<Integer, Integer> trackMap = new HashMap<>();
            if (content.getTracks() != null) {
                for (int i = 0; i < content.getTracks().size(); i++) {
                    TrackDTO tDto = content.getTracks().get(i);
                    Track t = new Track();
                    t.setSongId(songId);
                    t.setStartTiming(tDto.getStartTiming());
                    t.setEndTiming(tDto.getEndTiming());
                    t.setType(tDto.getType());
                    t.setKeyX(tDto.getKey());
                    t.setR(tDto.getR());
                    t.setG(tDto.getG());
                    t.setB(tDto.getB());
                    t.setWidth(tDto.getWidth());
                    t.setPositionX(tDto.getPositionX());
                    trackMapper.insert(t);
                    trackMap.put(i, t.getId());

                    if (tDto.getNotes() != null) {
                        for (NoteDTO nDto : tDto.getNotes()) {
                            Note n = new Note();
                            n.setSongId(songId);
                            n.setBasedTrack(t.getId());
                            n.setNoteType(nDto.getNoteType());
                            n.setKeyX(nDto.getKey());
                            n.setTiming(nDto.getTiming());
                            n.setEndTiming(nDto.getEndTiming());
                            noteMapper.insert(n);
                        }
                    }
                    if (tDto.getMoveOperations() != null) {
                        for (MoveOperationDTO mDto : tDto.getMoveOperations()) {
                            MoveOperation mo = new MoveOperation();
                            mo.setSongId(songId);
                            mo.setBasedTrack(t.getId());
                            mo.setStartTiming(mDto.getStartTiming());
                            mo.setEndTiming(mDto.getEndTiming());
                            mo.setStartX(mDto.getStartX());
                            mo.setEndX(mDto.getEndX());
                            moveOperationMapper.insert(mo);
                        }
                    }
                    if (tDto.getChangeColorOperations() != null) {
                        for (ChangeColorOperationDTO cDto : tDto.getChangeColorOperations()) {
                            ChangeColorOperation co = new ChangeColorOperation();
                            co.setSongId(songId);
                            co.setBasedTrack(t.getId());
                            co.setStartTiming(cDto.getStartTiming());
                            co.setEndTiming(cDto.getEndTiming());
                            co.setStartR(cDto.getStartR());
                            co.setStartG(cDto.getStartG());
                            co.setStartB(cDto.getStartB());
                            co.setEndR(cDto.getEndR());
                            co.setEndG(cDto.getEndG());
                            co.setEndB(cDto.getEndB());
                            changeColorOperationMapper.insert(co);
                        }
                    }
                    if (tDto.getChangeWidthOperations() != null) {
                        for (ChangeWidthOperationDTO wDto : tDto.getChangeWidthOperations()) {
                            ChangeWidthOperation wo = new ChangeWidthOperation();
                            wo.setSongId(songId);
                            wo.setBasedTrack(t.getId());
                            wo.setStartTiming(wDto.getStartTiming());
                            wo.setEndTiming(wDto.getEndTiming());
                            wo.setStartWidth(wDto.getStartWidth());
                            wo.setEndWidth(wDto.getEndWidth());
                            changeWidthOperationMapper.insert(wo);
                        }
                    }
                }
            }

            if (content.getChangeBackgroundOperations() != null) {
                for (ChangeBackgroundOperationDTO bDto : content.getChangeBackgroundOperations()) {
                    ChangeBackgroundOperation bo = new ChangeBackgroundOperation();
                    bo.setSongId(songId);
                    bo.setStartTiming(bDto.getStartTiming());
                    bo.setEndTiming(bDto.getEndTiming());
                    bo.setAssetId(bDto.getAssetId());
                    changeBackgroundOperationMapper.insert(bo);
                }
            }

            // [资产保护] 恢复 JSON 中引用的所有资产状态，撤销可能的删除标记
            if (content.getAssets() != null) {
                for (SongAsset asset : content.getAssets()) {
                    SongAsset dbAsset = songAssetMapper.selectById(asset.getId());
                    if (dbAsset != null) {
                        dbAsset.setIsDeleted(0);
                        songAssetMapper.updateById(dbAsset);
                        log.info("🛡️ [Rollback Protect] 已撤销资产删除标记: {}", asset.getName());
                    }
                }
            }
            return ReturnResponse.OK("重置成功");
        } catch (Exception e) {
            log.error("重置谱面失败", e);
            return ReturnResponse.systemException(ReturnStatus.BUSINESS_EXCEPTION);
        }
    }

    private ChartContentDTO generateChartFromDB(String songId) {
        Song song = songMapper.selectById(songId);
        if (song == null)
            return null;

        ChartContentDTO dto = new ChartContentDTO();
        dto.setSongId(songId);
        dto.setBpm(song.getBpm());
        dto.setFirstBeatDelay(song.getFirstBeatDelay());

        List<Track> tracks = trackMapper.selectList(new QueryWrapper<Track>().eq("song_id", songId));
        log.info("从数据库生成谱面 [{}], 找到 {} 条轨道", song.getSongName(), tracks.size());

        List<TrackDTO> trackDtos = new ArrayList<>();
        for (Track t : tracks) {
            TrackDTO td = new TrackDTO();
            td.setStartTiming(t.getStartTiming());
            td.setEndTiming(t.getEndTiming());
            td.setKey(t.getKeyX());
            td.setType(t.getType());
            td.setR(t.getR());
            td.setG(t.getG());
            td.setB(t.getB());
            td.setWidth(t.getWidth());
            td.setPositionX(t.getPositionX());

            List<Note> notes = noteMapper.selectList(new QueryWrapper<Note>().eq("based_track", t.getId()));
            List<NoteDTO> nDtos = new ArrayList<>();
            for (Note n : notes) {
                NoteDTO nd = new NoteDTO();
                nd.setKey(n.getKeyX());
                nd.setTiming(n.getTiming());
                nd.setNoteType(n.getNoteType());
                nd.setEndTiming(n.getEndTiming());
                nDtos.add(nd);
            }
            td.setNotes(nDtos);

            // MoveOperations
            List<MoveOperation> moves = moveOperationMapper
                    .selectList(new QueryWrapper<MoveOperation>().eq("based_track", t.getId()));
            List<MoveOperationDTO> moveDtos = new ArrayList<>();
            for (MoveOperation mo : moves) {
                MoveOperationDTO md = new MoveOperationDTO();
                md.setStartTiming(mo.getStartTiming());
                md.setEndTiming(mo.getEndTiming());
                md.setStartX(mo.getStartX());
                md.setEndX(mo.getEndX());
                moveDtos.add(md);
            }
            td.setMoveOperations(moveDtos);

            // WidthOperations
            List<ChangeWidthOperation> widths = changeWidthOperationMapper
                    .selectList(new QueryWrapper<ChangeWidthOperation>().eq("based_track", t.getId()));
            List<ChangeWidthOperationDTO> widthDtos = new ArrayList<>();
            for (ChangeWidthOperation wo : widths) {
                ChangeWidthOperationDTO wd = new ChangeWidthOperationDTO();
                wd.setStartTiming(wo.getStartTiming());
                wd.setEndTiming(wo.getEndTiming());
                wd.setStartWidth(wo.getStartWidth());
                wd.setEndWidth(wo.getEndWidth());
                widthDtos.add(wd);
            }
            td.setChangeWidthOperations(widthDtos);

            // ColorOperations
            List<ChangeColorOperation> colors = changeColorOperationMapper
                    .selectList(new QueryWrapper<ChangeColorOperation>().eq("based_track", t.getId()));
            List<ChangeColorOperationDTO> colorDtos = new ArrayList<>();
            for (ChangeColorOperation co : colors) {
                ChangeColorOperationDTO cd = new ChangeColorOperationDTO();
                cd.setStartTiming(co.getStartTiming());
                cd.setEndTiming(co.getEndTiming());
                cd.setStartR(co.getStartR());
                cd.setStartG(co.getStartG());
                cd.setStartB(co.getStartB());
                cd.setEndR(co.getEndR());
                cd.setEndG(co.getEndG());
                cd.setEndB(co.getEndB());
                colorDtos.add(cd);
            }
            td.setChangeColorOperations(colorDtos);

            trackDtos.add(td);
        }
        dto.setTracks(trackDtos);

        // ChangeBackgroundOperations
        List<ChangeBackgroundOperation> bgOps = changeBackgroundOperationMapper
                .selectList(new QueryWrapper<ChangeBackgroundOperation>().eq("song_id", songId));
        List<ChangeBackgroundOperationDTO> bgDtos = new ArrayList<>();
        for (ChangeBackgroundOperation op : bgOps) {
            ChangeBackgroundOperationDTO bd = new ChangeBackgroundOperationDTO();
            bd.setStartTiming(op.getStartTiming());
            bd.setEndTiming(op.getEndTiming());
            bd.setAssetId(op.getAssetId());
            bgDtos.add(bd);
        }
        dto.setChangeBackgroundOperations(bgDtos);

        // Assets (仅包含未标记删除的，且排除已设为封面或背景的资源，确保“只存一份”)
        QueryWrapper<SongAsset> assetWrapper = new QueryWrapper<SongAsset>()
                .eq("song_id", songId)
                .eq("is_deleted", 0);
        if (song.getSongCover() != null) {
            assetWrapper.ne("url", song.getSongCover());
        }
        if (song.getDefaultBackground() != null) {
            assetWrapper.ne("url", song.getDefaultBackground());
        }
        List<SongAsset> assets = songAssetMapper.selectList(assetWrapper);
        dto.setAssets(assets);

        return dto;
    }

    @Override
    public ReturnResponse<ChartContentDTO> getChart(String songId) {
        try {
            String r2Url = publicUrl + (publicUrl.endsWith("/") ? "" : "/") + "ephemelody/charts/" + songId + ".json";
            try {
                return ReturnResponse.OK(objectMapper.readValue(new URL(r2Url), ChartContentDTO.class));
            } catch (IOException e) {
                // If R2 read fails, try to generate from DB
                ChartContentDTO dto = generateChartFromDB(songId);
                if (dto != null) {
                    return ReturnResponse.OK(dto);
                }
            }
            return ReturnResponse.packageObject(null, ReturnStatus.NO_DATA);
        } catch (Exception e) {
            log.error("获取谱面失败", e);
            return ReturnResponse.systemException(ReturnStatus.BUSINESS_EXCEPTION);
        }
    }

    @Override
    public ReturnResponse<String> forceSyncAll() {
        try {
            List<Song> songs = songMapper.selectList(null);
            for (Song song : songs) {
                // 强制从数据库重新生成 JSON，以确保资源 ID (assetId) 是最新的 UUID
                ChartContentDTO dto = generateChartFromDB(song.getId());
                if (dto != null) {
                    editChartContent(dto);
                }
            }
            return ReturnResponse.OK("全量同步完成");
        } catch (Exception e) {
            return ReturnResponse.systemException(ReturnStatus.BUSINESS_EXCEPTION);
        }
    }
}
