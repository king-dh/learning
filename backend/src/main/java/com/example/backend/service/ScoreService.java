package com.example.backend.service; // 声明服务接口包

import com.baomidou.mybatisplus.core.metadata.IPage; // MyBatis-Plus 分页结果
import com.example.backend.dto.ScoreDTO; // 成绩 DTO
import com.example.backend.dto.ScoreQueryDTO; // 成绩查询参数 DTO
import com.example.backend.vo.ScoreVO; // 成绩视图对象

import java.util.List; // 列表

/**
 * 成绩服务接口
 * 负责成绩的增删改查和多维度查询
 */
public interface ScoreService {

    /**
     * 分页条件查询成绩列表
     * @param dto 查询参数（学生ID、课程ID、学期、页码、每页大小）
     * @return 分页结果（包含学生姓名、课程名称）
     */
    IPage<ScoreVO> pageQuery(ScoreQueryDTO dto);

    /**
     * 新增成绩
     * @param dto 成绩信息
     */
    void save(ScoreDTO dto);

    /**
     * 修改成绩
     * @param dto 成绩信息
     */
    void update(ScoreDTO dto);

    /**
     * 删除成绩
     * @param id 成绩 ID
     */
    void delete(Long id);

    /**
     * 根据学生 ID 查询该学生的所有成绩（含课程名称和学生姓名）
     * @param studentId 学生 ID
     * @return 成绩视图对象列表
     */
    List<ScoreVO> getByStudentId(Long studentId);
}
