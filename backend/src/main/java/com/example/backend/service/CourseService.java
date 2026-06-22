package com.example.backend.service; // 声明服务接口包

import com.baomidou.mybatisplus.core.metadata.IPage; // MyBatis-Plus 分页结果
import com.example.backend.dto.CourseDTO; // 课程 DTO
import com.example.backend.dto.CourseQueryDTO; // 课程查询参数 DTO
import com.example.backend.vo.CourseVO; // 课程视图对象

import java.util.List; // 列表

/**
 * 课程服务接口
 * 负责课程信息的增删改查和分页搜索
 */
public interface CourseService {

    /**
     * 分页条件查询课程列表
     * @param dto 查询参数（名称、教师ID、学期、页码、每页大小）
     * @return 分页结果（包含教师名称）
     */
    IPage<CourseVO> pageQuery(CourseQueryDTO dto);

    /**
     * 根据 ID 查询课程详情
     * @param id 课程 ID
     * @return 课程视图对象
     */
    CourseVO getById(Long id);

    /**
     * 新增课程
     * @param dto 课程信息
     */
    void save(CourseDTO dto);

    /**
     * 修改课程信息
     * @param dto 课程信息
     */
    void update(CourseDTO dto);

    /**
     * 删除课程
     * @param id 课程 ID
     */
    void delete(Long id);

    /**
     * 根据教师 ID 查询该教师的所有课程（含教师名称）
     * @param teacherId 教师 ID
     * @return 课程视图对象列表
     */
    List<CourseVO> getByTeacherId(Long teacherId);
}
