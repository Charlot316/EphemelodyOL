package team.javaee.controller;

import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import team.javaee.common.config.ReturnResponse;
import team.javaee.entity.dto.SongDTO;
import team.javaee.service.SongService;
import team.javaee.service.UserService; // Import
import team.javaee.common.Normal; // Import
import javax.servlet.http.HttpServletRequest; // Import

@RestController
@RequestMapping("/admin")
public class AdminController {
    @Autowired
    private SongService songService;

    @Autowired
    private UserService userService; // Added UserService to check permissions

    private ReturnResponse<String> checkAdmin(HttpServletRequest request) {
        String userId = Normal.getUserIdByCookie(request);
        if (userId == null)
            return new ReturnResponse<String>(1, "Not logged in");
        team.javaee.entity.domain.User user = userService.getById(userId); // Use fully qualified name if needed or
                                                                           // import
        if (user != null && user.getIsAdmin() != null && user.getIsAdmin() == 1) {
            return null; // OK
        }
        return new ReturnResponse<String>(1, "Permission Denied: Admins Only");
    }

    @ApiOperation("认定已公开的谱面")
    @PostMapping("accreditChart")
    public ReturnResponse<String> accreditChart(@RequestBody SongDTO songDTO, HttpServletRequest request) {
        ReturnResponse<String> check = checkAdmin(request);
        if (check != null)
            return check;
        return songService.accreditChart(songDTO);
    }

    @ApiOperation("认定已公开的谱面")
    @PostMapping("disaccreditChart")
    public ReturnResponse<String> disAccreditChart(@RequestBody SongDTO songDTO, HttpServletRequest request) {
        ReturnResponse<String> check = checkAdmin(request);
        if (check != null)
            return check;
        return songService.disAccreditChart(songDTO);
    }

    @ApiOperation("删除谱面")
    @DeleteMapping("deleteChart")
    public ReturnResponse<String> deleteChart(@RequestBody SongDTO songDTO, HttpServletRequest request) {
        ReturnResponse<String> check = checkAdmin(request);
        if (check != null)
            return check;
        return songService.deleteChart(songDTO);
    }
}
