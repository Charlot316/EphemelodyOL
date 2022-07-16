package team.javaee.controller;


import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import org.springframework.web.multipart.MultipartFile;
import team.javaee.common.Normal;
import team.javaee.common.config.ReturnResponse;
import team.javaee.entity.dto.*;
import team.javaee.entity.vo.BackgroundVO;
import team.javaee.entity.vo.ImageVO;
import team.javaee.service.SongService;

import javax.servlet.http.HttpServletRequest;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author nwh
 * @since 2022-05-13
 */
@RestController
@RequestMapping("/chart")
public class SongController {

    @Autowired
    private SongService songService;

    @ApiOperation("新增谱面")
    @PostMapping("newChart")
    public ReturnResponse<Integer> newChart(HttpServletRequest request, @RequestBody NewChartDTO newChartDTO) {
        String userId = Normal.getUserIdByCookie(request);
        newChartDTO.setUploaderId(userId);
        return songService.newChart(newChartDTO);
    }

    @ApiOperation("修改谱面")
    @PostMapping("editChartInfo")
    public ReturnResponse<String> editChartInfo(HttpServletRequest request, @RequestBody EditChartDTO editChartDTO) {
        String userId = Normal.getUserIdByCookie(request);
        editChartDTO.setUploaderId(userId);
        return songService.editChartInfo(editChartDTO);
    }

    @ApiOperation("上传歌曲封面")
    @PostMapping("uploadSongCover")
    public ReturnResponse<ImageVO> uploadSongCover(HttpServletRequest request, @RequestParam("file") MultipartFile file, @RequestParam("songId") Integer songId) {
        return songService.uploadSongCover(file, songId, request);
    }

    @ApiOperation("上传歌曲默认背景")
    @PostMapping("uploadDefaultBackground")
    public ReturnResponse<ImageVO> uploadDefaultBackground(HttpServletRequest request,  @RequestParam("file") MultipartFile file, @RequestParam("songId") Integer songId) {
        return songService.uploadDefaultBackground(file, songId, request);
    }

    @ApiOperation("上传音频")
    @PostMapping("uploadSong")
    public ReturnResponse<String> uploadSong(HttpServletRequest request, @RequestParam("file") MultipartFile file, @RequestParam("songId") Integer songId) {
        return songService.uploadSong(file, songId, request);
    }

    @ApiOperation("修改谱面具体内容")
    @PostMapping("editChartContent")
    public ReturnResponse<String> editChartContent(@RequestBody ChartContentDTO chartContentDTO) {
        return songService.editChartContent(chartContentDTO);
    }

    @ApiOperation("新增歌曲背景")
    @PostMapping("uploadBackground")
    public ReturnResponse<BackgroundVO> uploadBackground(HttpServletRequest request, @RequestBody UploadBackgroundDTO uploadBackgroundDTO) {
        return songService.uploadBackground(uploadBackgroundDTO, request);
    }
}

