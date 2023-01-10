package com.qy.mybatisdemo.mapper;

import com.qy.mybatisdemo.pojo.NameTable;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @author wenan.ren
 * @date 2023/1/10 11:21
 * @Description
 */
public interface NameTableMapper {

    @Select("select * from name_table where id = #{id}")
    NameTable selectById(Integer id);

    @Select("select * from name_table")
    List<NameTable> selectAllData();
}
