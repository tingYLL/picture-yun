package com.jdjm.jdjmpicturebackend.controller;

import com.jdjm.jdjmpicturebackend.common.BaseResponse;
import com.jdjm.jdjmpicturebackend.common.ResultUtils;
import com.jdjm.jdjmpicturebackend.exception.ErrorCode;
import com.jdjm.jdjmpicturebackend.exception.ThrowUtils;
import com.jdjm.jdjmpicturebackend.model.dto.vip.VIPRedemptionCodeDTO;
import com.jdjm.jdjmpicturebackend.model.entity.VIPRedemptionCode;
import com.jdjm.jdjmpicturebackend.service.UserService;
import com.jdjm.jdjmpicturebackend.service.VIPRedemptionCodeService;
import com.jdjm.jdjmpicturebackend.service.VIPService;
import com.jdjm.jdjmpicturebackend.utils.RedemptionCodeGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/vip-redemption-codes")
public class VIPRedemptionCodeController {

    @Autowired
    private VIPRedemptionCodeService vipRedemptionCodeService;

    @Autowired
    private VIPService vipService;
    private UserService userService;

    /**
     * 自动生成兑换码
     * @param type VIP类型
     * @param count 生成数量
     * @return 生成的兑换码信息
     */
    @PostMapping("/generate")
    public BaseResponse<Map<String, Object>> generateCode(@RequestParam(defaultValue = "MONTHLY") String type,
                                                        @RequestParam(defaultValue = "1") int count) {
        Map<String, Object> result = vipRedemptionCodeService.generateBatchCodes(type, count);
        return ResultUtils.success(result);
    }


    /**
     * 手动创建兑换码
     * @param vipRedemptionCodeDTO
     * @return
     */
    @PostMapping
    public BaseResponse<Boolean> create(@RequestBody VIPRedemptionCodeDTO vipRedemptionCodeDTO) {
        ThrowUtils.throwIf(!RedemptionCodeGenerator.validate(vipRedemptionCodeDTO.getCode()), ErrorCode.PARAMS_ERROR, "无效的兑换码格式");

        VIPRedemptionCode existingCode = vipRedemptionCodeService.findByCode(vipRedemptionCodeDTO.getCode());
        ThrowUtils.throwIf(existingCode != null, ErrorCode.OPERATION_ERROR, "兑换码已存在");

        VIPRedemptionCode vipRedemptionCode = new VIPRedemptionCode();
        vipRedemptionCode.setCode(vipRedemptionCodeDTO.getCode());
        vipRedemptionCode.setIsUsed(vipRedemptionCodeDTO.getIsUsed());
        vipRedemptionCode.setUserId(vipRedemptionCodeDTO.getUserId());
        vipRedemptionCode.setUsedAt(vipRedemptionCodeDTO.getUsedAt());
        vipRedemptionCode.setCreatedAt(LocalDateTime.now());
        vipRedemptionCode.setUpdatedAt(LocalDateTime.now());

        return ResultUtils.success(vipRedemptionCodeService.save(vipRedemptionCode));
    }

    @DeleteMapping("/{id}")
    public BaseResponse<Boolean> delete(@PathVariable Long id) {
        return ResultUtils.success(vipRedemptionCodeService.removeById(id));
    }

    @PutMapping
    public BaseResponse<Boolean> update(@RequestBody VIPRedemptionCodeDTO vipRedemptionCodeDTO) {
        VIPRedemptionCode existingCode = vipRedemptionCodeService.getById(vipRedemptionCodeDTO.getId());
        ThrowUtils.throwIf(existingCode == null, ErrorCode.NOT_FOUND_ERROR, "兑换码不存在");

        VIPRedemptionCode vipRedemptionCode = new VIPRedemptionCode();
        vipRedemptionCode.setId(vipRedemptionCodeDTO.getId());
        vipRedemptionCode.setCode(vipRedemptionCodeDTO.getCode());
        vipRedemptionCode.setIsUsed(vipRedemptionCodeDTO.getIsUsed());
        vipRedemptionCode.setUserId(vipRedemptionCodeDTO.getUserId());
        vipRedemptionCode.setUsedAt(vipRedemptionCodeDTO.getUsedAt());
        vipRedemptionCode.setUpdatedAt(LocalDateTime.now());

        return ResultUtils.success(vipRedemptionCodeService.updateById(vipRedemptionCode));
    }

    @GetMapping("/{id}")
    public BaseResponse<VIPRedemptionCode> getById(@PathVariable Long id) {
        VIPRedemptionCode code = vipRedemptionCodeService.getById(id);
        ThrowUtils.throwIf(code == null, ErrorCode.NOT_FOUND_ERROR, "兑换码不存在");
        return ResultUtils.success(code);
    }

    @GetMapping
    public BaseResponse<List<VIPRedemptionCode>> listAll() {
        return ResultUtils.success(vipRedemptionCodeService.list());
    }

    @GetMapping("/validate/{code}")
    public BaseResponse<Map<String, Object>> validateCode(@PathVariable String code) {
        boolean isValid = RedemptionCodeGenerator.validate(code);
        if (!isValid) {
            Map<String, Object> result = new HashMap<>();
            result.put("valid", false);
            result.put("reason", "无效的兑换码格式");
            return ResultUtils.success(result);
        }

        VIPRedemptionCode redemptionCode = vipRedemptionCodeService.findByCode(code);
        if (redemptionCode == null) {
            Map<String, Object> result = new HashMap<>();
            result.put("valid", false);
            result.put("reason", "兑换码不存在");
            return ResultUtils.success(result);
        }

        if (redemptionCode.getIsUsed()) {
            Map<String, Object> result = new HashMap<>();
            result.put("valid", false);
            result.put("reason", "兑换码已被使用");
            result.put("usedBy", redemptionCode.getUserId());
            result.put("usedAt", redemptionCode.getUsedAt());
            return ResultUtils.success(result);
        }

        RedemptionCodeGenerator.VIPType type = RedemptionCodeGenerator.extractType(code);
        Map<String, Object> result = new HashMap<>();
        result.put("valid", true);
        result.put("type", type != null ? type.name() : "UNKNOWN");
        result.put("createdAt", redemptionCode.getCreatedAt());
        return ResultUtils.success(result);
    }
}