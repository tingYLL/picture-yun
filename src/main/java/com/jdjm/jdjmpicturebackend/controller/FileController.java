package com.jdjm.jdjmpicturebackend.controller;

import com.jdjm.jdjmpicturebackend.annotation.AuthCheck;
import com.jdjm.jdjmpicturebackend.common.BaseResponse;
import com.jdjm.jdjmpicturebackend.common.ResultUtils;
import com.jdjm.jdjmpicturebackend.config.UploadProperties;
import com.jdjm.jdjmpicturebackend.constant.UserConstant;
import com.jdjm.jdjmpicturebackend.exception.BusinessException;
import com.jdjm.jdjmpicturebackend.exception.ErrorCode;
import com.jdjm.jdjmpicturebackend.manager.CosManager;
import com.qcloud.cos.model.COSObject;
import com.qcloud.cos.model.COSObjectInputStream;
import com.qcloud.cos.utils.IOUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/file")
public class FileController {

    @Resource
    private CosManager cosManager;

    @Resource
    private UploadProperties uploadProperties;

//    @Autowired
//    private MinioUtil minioUtil;

    @PostMapping("/upload/local")
    public String uploadImage(@RequestParam("image") MultipartFile file) {
        if (file.isEmpty()) {
            return "上传文件不能为空";
        }

        try {
            // 生成唯一文件名，避免覆盖
            String originalFilename = file.getOriginalFilename();
            String fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
            String savedFileName = UUID.randomUUID().toString() + fileExtension;

            // 构建文件保存路径
            File destFile = new File(Paths.get(uploadProperties.getDir(), savedFileName).toString());

            // 确保目录存在
            if (!destFile.getParentFile().exists()) {
                destFile.getParentFile().mkdirs();
            }

            // 保存文件
            file.transferTo(destFile);

            // 返回可供访问的文件名或相对路径
            return savedFileName;
        } catch (IOException e) {
            e.printStackTrace();
            return "文件上传失败: " + e.getMessage();
        }
    }

    /**
     * 测试文件上传（minio）
     */
//    @PostMapping(value = "/upload")
//    @ApiOperation("minio上传文件")
//    public BaseResponse<String> upload(@RequestPart("file") MultipartFile file) {
//        // 获取到上传文件的完整名称，包括文件后缀
//        String fileName = file.getOriginalFilename();
//        // 获取不带后缀的文件名
//        String baseName = FilenameUtils.getBaseName(fileName);
//        // 获取文件后缀
//        String extension = FilenameUtils.getExtension(fileName);
//        //创建一个独一的文件名(存于服务器名),格式为 name_时间戳.后缀
//        String saveFileName = baseName + "_" + System.currentTimeMillis() + "." + extension;
//        minioUtil.upload(file, saveFileName);
//        return R.ok("上传成功！存放文件名为：" + saveFileName);
//        return ResultUtils.success(fileName);
//    }
//
//    /**
//     * 根据文件ID下载文件（minio）
//     */
//    @GetMapping("/download")
//    @ApiOperation("根据文件ID下载文件")
//    public void downloadById(@RequestParam("fileName") String fileName, @RequestParam("saveFileName") String saveFileName, HttpServletResponse response) {
//        // 下载文件，传递存储文件名和显示文件名
//        minioUtil.download(response, fileName, saveFileName);
//        return;
//    }

    /**
     * 测试文件上传 (腾讯云cos)
     *
     * @param multipartFile
     * @return
     */
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    @PostMapping("/test/upload")
    public BaseResponse<String> testUploadFile(@RequestPart("file") MultipartFile multipartFile) {
        // 文件目录
        String filename = multipartFile.getOriginalFilename();
        String filepath = String.format("/test/%s", filename);
        File file = null;
        try {
            // 上传文件
            file = File.createTempFile(filepath, null);
            multipartFile.transferTo(file);
            cosManager.putObject(filepath, file);
            // 返回可访问的地址
            return ResultUtils.success(filepath);
        } catch (Exception e) {
            log.error("file upload error, filepath = " + filepath, e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "上传失败");
        } finally {
            if (file != null) {
                // 删除临时文件
                boolean delete = file.delete();
                if (!delete) {
                    log.error("file delete error, filepath = {}", filepath);
                }
            }
        }
    }

    /**
     * 测试文件下载 (腾讯云cos)
     *
     * @param filepath 文件路径
     * @param response 响应对象
     */
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    @GetMapping("/test/download/")
    public void testDownloadFile(String filepath, HttpServletResponse response) throws IOException {
        COSObjectInputStream cosObjectInput = null;
        try {
            COSObject cosObject = cosManager.getObject(filepath);
            cosObjectInput = cosObject.getObjectContent();
            byte[] bytes = IOUtils.toByteArray(cosObjectInput);
            // 设置响应头
            response.setContentType("application/octet-stream;charset=UTF-8");
            response.setHeader("Content-Disposition", "attachment; filename=" + filepath);
            // 写入响应
            response.getOutputStream().write(bytes);
            response.getOutputStream().flush();
        } catch (Exception e) {
            log.error("file download error, filepath = " + filepath, e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "下载失败");
        } finally {
            // 释放流
            if (cosObjectInput != null) {
                cosObjectInput.close();
            }
        }
    }
}







