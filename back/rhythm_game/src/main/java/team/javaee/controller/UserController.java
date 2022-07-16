package team.javaee.controller;


import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import org.springframework.web.multipart.MultipartFile;
import team.javaee.common.Normal;
import team.javaee.common.config.ReturnResponse;
import team.javaee.entity.dto.*;
import team.javaee.entity.vo.*;
import team.javaee.service.UserService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author nwh
 * @since 2022-05-13
 */
@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    @ApiOperation("用户登录")
    @PostMapping("login")
    public ReturnResponse<Object> login(HttpServletRequest request, HttpServletResponse response, @RequestBody LoginDTO loginDTO) {
        return userService.login(request, response, loginDTO);
    }

    @ApiOperation("用户注册")
    @PostMapping("register")
    public ReturnResponse<String> register(@RequestBody UserDTO userDTO) {
        return userService.register(userDTO);
    }

    @ApiOperation("修改个人密码")
    @PostMapping("changePassword")
    public ReturnResponse<String> password(HttpServletRequest request, @RequestBody PasswordDTO passwordDTO){
        String userId = Normal.getUserIdByCookie(request);
        return userService.password(userId,passwordDTO);
    }

    @ApiOperation("获取个人信息")
    @PostMapping("getUserInformation")
    public ReturnResponse<UserVO> getUserInformation(HttpServletRequest request){
        String userId = Normal.getUserIdByCookie(request);
        return userService.getUserInformation(userId);
    }

    @ApiOperation("上传头像")
    @PostMapping("/uploadIcon")
    public ReturnResponse<ImageVO> imgUpload(@RequestParam("file") MultipartFile file, HttpServletRequest req) {
        String userId = Normal.getUserIdByCookie(req);
        return userService.imgUpload(file,req);
    }

    @ApiOperation("公开自己已经存储的谱面")
    @PostMapping("/publiciseChart")
    public ReturnResponse<String> chartPublicise(HttpServletRequest req, @RequestBody ChartChangeDTO chartChangeDTO) {
        String userId = Normal.getUserIdByCookie(req);
        return userService.chartPublicise(userId, chartChangeDTO);
    }

    @ApiOperation("私有化自己已经存储的谱面")
    @PostMapping("/privatizeChart")
    public ReturnResponse<String> privatizeChart(HttpServletRequest req, @RequestBody ChartChangeDTO chartChangeDTO) {
        String userId = Normal.getUserIdByCookie(req);
        return userService.privatizeChart(userId, chartChangeDTO);
    }

    @ApiOperation("搜索自己上传的谱面的基本信息列表")
    @PostMapping("/getMyCharts")
    public ReturnResponse<SelfSongsListVO> getMyCharts(HttpServletRequest req, @RequestBody PublicChartsDTO publicChartsDTO) {
        String userId = Normal.getUserIdByCookie(req);
        return userService.getMyCharts(userId,publicChartsDTO);
    }

    @ApiOperation("获取自己上传的谱面的基本信息列表")
    @PostMapping("/getAllMyCharts")
    public ReturnResponse<SelfSongsListVO> getAllMyCharts(HttpServletRequest req) {
        String userId = Normal.getUserIdByCookie(req);
        return userService.getAllMyCharts(userId);
    }

    @ApiOperation("获取单个谱面")
    @GetMapping("/getChart")
    public ReturnResponse<SingleSongVO> getChart(@RequestParam String songId) {
        SongDTO songDTO = new SongDTO();
        songDTO.setSongId(songId);
        return userService.getChart(songDTO);
    }

    @ApiOperation("删除谱面")
    @PostMapping("/deleteChart")
    public ReturnResponse<String> deleteChart(HttpServletRequest req,@RequestBody SongDTO songDTO){
        String userId = Normal.getUserIdByCookie(req);
        return userService.deleteChart(userId, songDTO);
    }

    @ApiOperation("获取已公开的谱面列表")
    @PostMapping("/getPublicCharts")
    public ReturnResponse<PublicChartsVO> getPublicCharts(HttpServletRequest req, @RequestBody PublicChartsDTO publicChartsDTO){
        String userId = Normal.getUserIdByCookie(req);
        return userService.getPublicCharts(userId, publicChartsDTO);
    }

    @ApiOperation("获取所有公开的谱面列表")
    @PostMapping("/getAllCharts")
    public ReturnResponse<SelfSongsListVO> getAllCharts(){
        return userService.getAllCharts();
    }
}

