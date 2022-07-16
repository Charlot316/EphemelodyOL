package team.javaee.controller;

import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import team.javaee.common.config.ReturnResponse;
import team.javaee.entity.dto.SongDTO;
import team.javaee.service.SongService;

@RestController
@RequestMapping("/admin")
public class AdminController {
    @Autowired
    private SongService songService;

    @ApiOperation("认定已公开的谱面")
    @PostMapping("accreditChart")
    public ReturnResponse<String> accreditChart(@RequestBody SongDTO songDTO) {
        return songService.accreditChart(songDTO);
    }

    @ApiOperation("认定已公开的谱面")
    @PostMapping("disaccreditChart")
    public ReturnResponse<String> disAccreditChart(@RequestBody SongDTO songDTO) {
        return songService.disAccreditChart(songDTO);
    }

    @ApiOperation("删除谱面")
    @DeleteMapping("deleteChart")
    public ReturnResponse<String> deleteChart(@RequestBody SongDTO songDTO) {
        return songService.deleteChart(songDTO);
    }
}
