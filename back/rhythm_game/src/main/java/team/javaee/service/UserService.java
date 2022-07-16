package team.javaee.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.web.multipart.MultipartFile;
import team.javaee.common.config.ReturnResponse;
import team.javaee.entity.domain.User;
import team.javaee.entity.dto.*;
import team.javaee.entity.vo.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author nwh
 * @since 2022-05-13
 */
public interface UserService extends IService<User> {

    //登录
    ReturnResponse<Object> login(HttpServletRequest request, HttpServletResponse response, LoginDTO loginDTO);

    //注册
    ReturnResponse<String> register(UserDTO userDTO);

    //修改密码
    ReturnResponse<String> password(String userId, PasswordDTO passwordDTO);

    //获取个人信息
    ReturnResponse<UserVO> getUserInformation(String userId);

    //用户上传头像
    ReturnResponse<ImageVO> imgUpload(MultipartFile file, HttpServletRequest req);

    //用户公开自己已经存储的谱面
    ReturnResponse<String> chartPublicise(String userId, ChartChangeDTO chartChangeDTO);

    //用户私有化自己公开的谱面
    ReturnResponse<String> privatizeChart(String userId, ChartChangeDTO chartChangeDTO);

    //搜索自己上传的谱面的基本信息列表
    ReturnResponse<SelfSongsListVO> getMyCharts(String userId, PublicChartsDTO publicChartsDTO);

    //获取自己上传的全部谱面
    ReturnResponse<SelfSongsListVO> getAllMyCharts(String userId);

    //获取单个谱面
    ReturnResponse<SingleSongVO> getChart(SongDTO songDTO);

    //删除谱面
    ReturnResponse<String> deleteChart(String userId,SongDTO songDTO);

    //获取已公开的谱面列表
    ReturnResponse<PublicChartsVO> getPublicCharts(String userId, PublicChartsDTO publicChartsDTO);

    //获取所有公开谱面
    public ReturnResponse<SelfSongsListVO> getAllCharts();
}
