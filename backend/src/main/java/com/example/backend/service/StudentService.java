package com.example.backend.service; // 声明服务接口包

import com.baomidou.mybatisplus.core.metadata.IPage; // MyBatis-Plus 分页结果
import com.example.backend.dto.StudentDTO; // 学生 DTO
import com.example.backend.dto.StudentQueryDTO; // 学生查询参数 DTO
import com.example.backend.vo.StudentVO; // 学生视图对象

/**
 * 学生服务接口
 * 负责学生信息的增删改查和分页搜索
 */
public interface StudentService {

    /**
     * 分页条件查询学生列表
     * @param dto 查询参数（名称、学号、页码、每页大小）
     * @return 分页结果（包含学生列表和班级名称）
     */
    IPage<StudentVO> pageQuery(StudentQueryDTO dto);

    /**
     * 根据 ID 查询学生详情
     * @param id 学生 ID
     * @return 学生视图对象
     */
    StudentVO getById(Long id);

    /**
     * 新增学生
     * @param dto 学生信息
     */
    void save(StudentDTO dto);

    /**
     * 修改学生信息
     * @param dto 学生信息
     */
    void update(StudentDTO dto);

    /**
     * 删除学生（会检查是否有关联成绩和选课记录）
     * @param id 学生 ID
     */
    void delete(Long id);
}
