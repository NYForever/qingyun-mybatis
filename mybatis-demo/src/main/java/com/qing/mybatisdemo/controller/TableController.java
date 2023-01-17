package com.qing.mybatisdemo.controller;

import com.qing.mybatisdemo.mapper.qingwan.AddressTableMapper;
import com.qing.mybatisdemo.mapper.qingyun.NameTableMapper;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @author wenan.ren
 * @date 2023/1/10 11:24
 * @Description
 */
@RestController("/qingyun")
public class TableController {

    @Resource
    private NameTableMapper nameTableMapper;

    @Resource
    private AddressTableMapper addressTableMapper;

    @GetMapping("/allNameData")
    public Object allNameData() {
        return nameTableMapper.selectAllData();
    }

    @GetMapping("/getNameByid")
    public Object getNameByid(Integer id) {
        return nameTableMapper.selectById(id);
    }

    @GetMapping("/allAddressData")
    public Object allAddressData() {
        return addressTableMapper.selectAllData();
    }

    @GetMapping("/getAddressByid")
    public Object getAddressByid(Integer id) {
        return addressTableMapper.selectById(id);
    }
}
