package team.javaee.service;

import org.springframework.web.bind.annotation.RequestBody;
import team.javaee.common.config.ReturnResponse;
import team.javaee.entity.dto.PlayResultDTO;
import team.javaee.entity.dto.SongDTO;
import team.javaee.entity.vo.ChartInfVO;
import team.javaee.entity.vo.PlayResultVO;

import javax.servlet.http.HttpServletRequest;

public interface PlayService {
    ReturnResponse<ChartInfVO> getChartInfo(String userId, SongDTO songDTO);
    ReturnResponse<PlayResultVO> uploadRecord(PlayResultDTO playResultDTO);
}
