package team.javaee.service.impl;

import team.javaee.entity.domain.Note;
import team.javaee.mapper.NoteMapper;
import team.javaee.service.NoteService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author nwh
 * @since 2022-05-13
 */
@Service
public class NoteServiceImpl extends ServiceImpl<NoteMapper, Note> implements NoteService {

}
