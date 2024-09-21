package com.sky.controller.admin;

import com.sky.result.Result;
import com.sky.utils.AliOssUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

@Slf4j
@RestController
@Api(tags = "通用接口")
@RequestMapping("/admin/common")
public class CommonController {

//    @Autowired
//    private AliOssUtil aliOssUtil;

//    @ApiOperation("文件上传")
//    @PostMapping("/upload")
//    public Result<String> upload(MultipartFile file) throws IOException {
//        log.info("文件上传{}", file.getOriginalFilename());
//        //文件原始名称
//        String filename = file.getOriginalFilename();
//        //文件扩展名
//        String ex = filename.substring(filename.lastIndexOf("."));
//        //文件名
//        String name = UUID.randomUUID() + ex;
//        //文件在阿里OSS中存储的路径
//        String filepath = aliOssUtil.upload(file.getBytes(), name);
//        return Result.success(filepath);
//    }
    @Value("${sky.base-path}")
    private String basePath;

    @ApiOperation("文件上传")
    @PostMapping("/upload")
    public Result<String> upload(MultipartFile file) throws IOException {
        //文件原始名称
        String originalName = file.getOriginalFilename();
        //文件扩展名
        String ex = originalName.substring(originalName.lastIndexOf("."));
        //文件名
        String name = UUID.randomUUID() + ex;
        //创建目录对象
        File dir = new File(basePath);
        if (!dir.exists()) {
            //不存在则创建
            dir.mkdirs();
        }
        //保存文件到指定目录
        String filePath = basePath + name;
        file.transferTo(new File(filePath));
        //返回文件的绝对路径
        return Result.success(filePath);
    }

}
