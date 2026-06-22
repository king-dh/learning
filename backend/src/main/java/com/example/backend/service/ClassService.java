package com.example.backend.service; // 声明服务接口包

import com.baomidou.mybatisplus.core.metadata.IPage; // MyBatis-Plus 分页结果
import com.example.backend.dto.ClassDTO; // 班级 DTO
import com.example.backend.dto.ClassQueryDTO; // 班级查询参数 DTO
import com.example.backend.vo.ClassVO; // 班级视图对象
import com.example.backend.vo.StudentVO; // 学生视图对象

import java.util.List; // 列表

/**
 * 班级服务接口
 * 负责班级信息的增删改查、分页搜索和班级学生列表
 */
public interface ClassService {

    /**
     * 分页条件查询班级列表
     * @param dto 查询参数（班级名称、年级、页码、每页大小）
     * @return 分页结果（包含班主任姓名、学生人数）
     */
    IPage<ClassVO> pageQuery(ClassQueryDTO dto);

    /**
     * 根据 ID 查询班级详情（含班主任姓名和学生人数）
     * @param id 班级 ID
     * @return 班级视图对象
     */
    ClassVO getById(Long id);

    /**
     * 新增班级
     * @param dto 班级信息
     */
    void save(ClassDTO dto);

    /**
     * 修改班级信息
     * @param dto 班级信息
     */
    void update(ClassDTO dto);

    /**
     * 删除班级
     * @param id 班级 ID
     */
    void delete(Long id);

    /**
     * 查询班级下的所有学生列表
     * @param classId 班级 ID
     * @return 学生视图对象列表
     */
    List<StudentVO> getStudentsByClassId(Long classId);
}
