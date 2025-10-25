package com.jdjm.jdjmpicturebackend.controller;

import com.jdjm.jdjmpicturebackend.common.BaseResponse;
import com.jdjm.jdjmpicturebackend.common.ResultUtils;
import com.jdjm.jdjmpicturebackend.exception.BusinessException;
import com.jdjm.jdjmpicturebackend.exception.ErrorCode;
import com.jdjm.jdjmpicturebackend.exception.ThrowUtils;
import com.jdjm.jdjmpicturebackend.model.dto.vip.RedeemCodeRequest;
import com.jdjm.jdjmpicturebackend.model.entity.User;
import com.jdjm.jdjmpicturebackend.model.entity.VIPRedemptionCode;
import com.jdjm.jdjmpicturebackend.model.vo.VIPInfoVO;
import com.jdjm.jdjmpicturebackend.service.UserService;
import com.jdjm.jdjmpicturebackend.service.VIPRedemptionCodeService;
import com.jdjm.jdjmpicturebackend.service.VIPService;
import com.jdjm.jdjmpicturebackend.service.impl.VIPServiceImpl;
import com.jdjm.jdjmpicturebackend.util.RedemptionCodeGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/vip")
public class VIPController {
    @Resource
    private VIPService vipService;
    @Resource
    private UserService userService;
    @Resource
    private VIPRedemptionCodeService vipRedemptionCodeService;

    /**
     * 获取用户 VIP 状态和详细信息
     * @param userId 用户ID
     * @return VIP 信息（包含是否是 VIP、到期时间、剩余天数等）
     */
    @GetMapping("/status")
    public BaseResponse<VIPInfoVO> checkVIPStatus(@RequestParam Long userId) {
        VIPInfoVO vipInfo = vipService.getVIPInfo(userId);
        return ResultUtils.success(vipInfo);
    }

    /**
     * 兑换VIP
     * @param redeemCodeRequest
     * @param request
     * @return
     */
    @PostMapping("/redeem")
    public BaseResponse<String> redeemVIP(@RequestBody RedeemCodeRequest redeemCodeRequest, HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        ThrowUtils.throwIf(!RedemptionCodeGenerator.validate(redeemCodeRequest.getCode()), ErrorCode.PARAMS_ERROR, "无效的兑换码格式");
        VIPRedemptionCode redemptionCode = vipRedemptionCodeService.findByCode(redeemCodeRequest.getCode());
        ThrowUtils.throwIf(redemptionCode == null, ErrorCode.NOT_FOUND_ERROR, "兑换码不存在");
        ThrowUtils.throwIf(redemptionCode.getIsUsed(), ErrorCode.OPERATION_ERROR, "兑换码已被使用");
        RedemptionCodeGenerator.VIPType type = RedemptionCodeGenerator.extractType(redeemCodeRequest.getCode());
        ThrowUtils.throwIf(type == null, ErrorCode.PARAMS_ERROR, "无法识别兑换码类型");
        vipService.activateVIP(redemptionCode,loginUser,type);
        return ResultUtils.success("兑换成功，已激活" + type.name() + "VIP会员");
    }
}