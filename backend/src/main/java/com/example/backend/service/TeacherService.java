package com.example.backend.service; // 声明服务接口包

import com.baomidou.mybatisplus.core.metadata.IPage; // MyBatis-Plus 分页结果
import com.example.backend.dto.TeacherDTO; // 教师 DTO
import com.example.backend.dto.TeacherQueryDTO; // 教师查询参数 DTO
import com.example.backend.vo.TeacherVO; // 教师视图对象

/**
 * 教师服务接口
 * 负责教师信息的增删改查和分页搜索
 */
public interface TeacherService {

    /**
     * 分页条件查询教师列表
     * @param dto 查询参数（姓名、院系、页码、每页大小）
     * @return 分页结果
     */
    IPage<TeacherVO> pageQuery(TeacherQueryDTO dto);

    /**
     * 根据 ID 查询教师详情
     * @param id 教师 ID
     * @return 教师视图对象
     */
    TeacherVO getById(Long id);

    /**
     * 新增教师
     * @param dto 教师信息
     */
    void save(TeacherDTO dto);

    /**
     * 修改教师信息
     * @param dto 教师信息
     */
    void update(TeacherDTO dto);

    /**
     * 删除教师
     * @param id 教师 ID
     */
    void delete(Long id);
}
