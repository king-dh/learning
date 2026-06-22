package com.example.backend.service; // 声明服务接口包

import com.example.backend.dto.EnrollmentDTO; // 选课 DTO
import com.example.backend.vo.EnrollmentVO; // 选课视图对象

import java.util.List; // 列表

/**
 * 选课服务接口
 * 负责选课和退课的业务逻辑
 */
public interface EnrollmentService {

    /**
     * 根据用户名查询其已选课程列表（用于 /my 端点）
     * @param username 登录用户名
     * @return 选课视图对象列表（含课程名、教师名、学分）
     */
    List<EnrollmentVO> getByUsername(String username);

    /**
     * 根据用户名获取对应的学生ID
     * @param username 登录用户名
     * @return 学生ID，如果未找到返回 null
     */
    Long getStudentIdByUsername(String username);

    /**
     * 查询某个学生的已选课程列表
     * @param studentId 学生 ID
     * @return 选课视图对象列表（含课程名、教师名、学分）
     */
    List<EnrollmentVO> getByStudentId(Long studentId);

    /**
     * 选课（学生选择一门课程）
     * 会检查是否已选过该课程，防止重复选课
     * @param dto 选课请求（学生ID、课程ID）
     */
    void enroll(EnrollmentDTO dto);

    /**
     * 退课（取消选课记录）
     * @param id 选课记录 ID
     */
    void unenroll(Long id);
}
