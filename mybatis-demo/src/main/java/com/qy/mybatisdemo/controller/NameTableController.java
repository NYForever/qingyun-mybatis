package com.qy.mybatisdemo.controller;

import com.qy.mybatisdemo.mapper.NameTableMapper;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @author wenan.ren
 * @date 2023/1/10 11:24
 * @Description
 */
@RestController("/qingyun")
public class NameTableController {

    @Resource
    private NameTableMapper nameTableMapper;

    @GetMapping("/allData")
    public Object getAllData() {
        return nameTableMapper.selectAllData();
    }

    @GetMapping("/getByid")
    public Object getByid(Integer id) {
        return nameTableMapper.selectById(id);
    }
}
