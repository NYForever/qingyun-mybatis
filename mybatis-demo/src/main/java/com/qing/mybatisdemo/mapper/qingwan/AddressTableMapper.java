package com.qing.mybatisdemo.mapper.qingwan;

import com.qing.mybatisdemo.pojo.AddressTable;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @author wenan.ren
 * @date 2023/1/10 11:21
 * @Description
 */
public interface AddressTableMapper {

    @Select("select * from address_table where id = #{id}")
    AddressTable selectById(Integer id);

    @Select("select * from address_table")
    List<AddressTable> selectAllData();
}
