package com.qing.mybatisdemo.mapper.qingyun;

import com.qing.mybatisdemo.pojo.NameTable;
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
