package com.example.backend.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.backend.entity.Course;
import com.example.backend.entity.Score;
import com.example.backend.entity.Student;
import com.example.backend.mapper.CourseMapper;
import com.example.backend.mapper.ScoreMapper;
import com.example.backend.mapper.StudentMapper;
import com.example.backend.service.ScoreService;
import com.example.backend.vo.ScoreVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ScoreServiceImpl implements ScoreService {

    private final ScoreMapper scoreMapper;
    private final StudentMapper studentMapper;
    private final CourseMapper courseMapper;

    @Override
    public IPage<ScoreVO> pageQuery(com.example.backend.dto.ScoreQueryDTO dto) {
        Page<Score> page = new Page<>(dto.getPageNum(), dto.getPageSize());
        LambdaQueryWrapper<Score> qw = new LambdaQueryWrapper<>();
        if (dto.getStudentId() != null) qw.eq(Score::getStudentId, dto.getStudentId());
        if (dto.getCourseId() != null) qw.eq(Score::getCourseId, dto.getCourseId());
        if (dto.getSemester() != null && !dto.getSemester().isEmpty()) qw.eq(Score::getSemester, dto.getSemester());
        qw.orderByDesc(Score::getCreateTime);
        IPage<Score> sp = scoreMapper.selectPage(page, qw);
        return sp.convert(score -> {
            ScoreVO vo = BeanUtil.copyProperties(score, ScoreVO.class);
            if (score.getStudentId() != null) { Student s = studentMapper.selectById(score.getStudentId()); if (s != null) vo.setStudentName(s.getName()); }
            if (score.getCourseId() != null) { Course c = courseMapper.selectById(score.getCourseId()); if (c != null) vo.setCourseName(c.getName()); }
            return vo;
        });
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void save(com.example.backend.dto.ScoreDTO dto) {
        Score score = BeanUtil.copyProperties(dto, Score.class);
        scoreMapper.insert(score);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(com.example.backend.dto.ScoreDTO dto) {
        Score score = BeanUtil.copyProperties(dto, Score.class);
        scoreMapper.updateById(score);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) { scoreMapper.deleteById(id); }

    @Override
    public List<ScoreVO> getByStudentId(Long studentId) { return scoreMapper.selectByStudentId(studentId); }
}
