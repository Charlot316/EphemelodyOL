package team.javaee.controller;

import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import team.javaee.common.Normal;
import team.javaee.common.config.ReturnResponse;
import team.javaee.entity.dto.PlayResultDTO;
import team.javaee.entity.dto.SongDTO;
import team.javaee.entity.vo.ChartInfVO;
import team.javaee.entity.vo.PlayResultVO;
import team.javaee.service.PlayService;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/play")
public class PlayController {
    @Autowired
    private PlayService playService;
    @ApiOperation("获取一首曲子的基本信息")
    @GetMapping("getChartInfo")
    public ReturnResponse<ChartInfVO> getChartInfo(HttpServletRequest request, @RequestParam(required = true) String songId) {
        String userId = Normal.getUserIdByCookie(request);
//        String userId = "1";
        SongDTO songDTO = new SongDTO(songId);
        return playService.getChartInfo(userId, songDTO);
    }

    @ApiOperation("上传游玩结果")
    @PostMapping("uploadRecord")
    public ReturnResponse<PlayResultVO> uploadRecord(HttpServletRequest request, @RequestBody PlayResultDTO playResultDTO) {
        String userId = Normal.getUserIdByCookie(request);
//        String userId = "1";
        playResultDTO.setUserId(userId);
        return playService.uploadRecord(playResultDTO);
    }

}
