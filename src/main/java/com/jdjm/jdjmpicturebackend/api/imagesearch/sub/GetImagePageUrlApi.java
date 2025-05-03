package com.jdjm.jdjmpicturebackend.api.imagesearch.sub;

import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.URLUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpStatus;
import cn.hutool.json.JSONUtil;
import com.jdjm.jdjmpicturebackend.exception.BusinessException;
import com.jdjm.jdjmpicturebackend.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * 获取以图搜图页面地址（step 1）
 */
@Slf4j
public class GetImagePageUrlApi {

    /**
     * 获取以图搜图页面地址
     *
     * @param imageUrl
     * @return
     */
    public static String getImagePageUrl(String imageUrl) {
        // image: https%3A%2F%2Fwww.codefather.cn%2Flogo.png
        //tn: pc
        //from: pc
        //image_source: PC_UPLOAD_URL
        //sdkParams:
        // 1. 准备请求参数
        Map<String, Object> formData = new HashMap<>();
        formData.put("image", imageUrl);
        formData.put("tn", "pc");
        formData.put("from", "pc");
        formData.put("image_source", "PC_UPLOAD_URL");
        // 获取当前时间戳
        long uptime = System.currentTimeMillis();
        // 请求地址
        String url = "https://graph.baidu.com/upload?uptime=" + uptime;
        try {
            // 2. 发送请求
            HttpResponse httpResponse = HttpRequest.post(url)
//                    .header("accept", "*/*")
//                    .header("accept-language", "zh,zh-CN;q=0.9")
                    .header("acs-token", "1746246349298_1746261316239_1N6hk592SCf2cB1SBnPUxwlSEYFgPnJrsgFMcdb7BjZEjuLTmaTjWlll5NKYb/wJln/NE9Q1lHcEpRWUHpkSa9/ptfGzlfNnS141C7xd/eL5IB0mLz9dUjV7nTv5OnlQi3w0yeYME/kJtLNN/XVdiAxDU3VLCeBF92Gkd80EY31qj+4DTmQvadrFY4r6oKvtjFvOioJQWnzIbOGwsk+PgMMNKYYKGKsJNaXojlZIwXNCAIk9gHy9Q9ARPywqv1gPopjBx8Ovc5sUzEMgsN0N368v4n6rcihRgeg8Yua3iqxsauKiiRoLFmb9+jq7CQ4852n9g8jPe9k5KlGWW41XP2tIwO65U5IvIU9xg5M7KwBPkbW51chZX1I/MmQZmlRd7MjUXqL1MigPwUeIR0DEuukGteU2AovkXbgXzYpgfvB9KZbmaOKwBXecvGjpOzYv7j0g5k2ztN/3TIOnNF5KxQ==") // 完整token见原curl
//                    .header("cache-control", "no-cache")
//                    .header("origin", "https://graph.baidu.com")
//                    .header("referer", "https://graph.baidu.com/s?card_key=&entrance=GENERAL&extUiData%5BisLogoShow%5D=1&f=all&isLogoShow=1&session_id=8551797933707589268&sign=126f6e63e42cdb9f7168d01746260633&tpl_from=pc") // 完整referer见原curl
//                    .header("x-requested-with", "XMLHttpRequest")
//                    .header("user-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64)...")

                    // 设置Cookie（示例关键cookie，需根据实际完整cookie设置）
//                    .cookie("BAIDUID=5BF498BD7036CC975A4D33DD58CB1CFD:FG=1; BIDUPSID=5BF498BD7036CC975A4D33DD58CB1CFD; PSTM=1739617312; MCITY=-332%3A134%3A; BDUSS=s2STduaFB3TVpxRHM0cWFNSEZCQmVwMFlNU1Y2fkNBbXRHR0NBNk5nQ21xQVZvSVFBQUFBJCQAAAAAAAAAAAEAAAB~8I-Q0KHI~NPaMQAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAKYb3memG95nNE; BDUSS_BFESS=s2STduaFB3TVpxRHM0cWFNSEZCQmVwMFlNU1Y2fkNBbXRHR0NBNk5nQ21xQVZvSVFBQUFBJCQAAAAAAAAAAAEAAAB~8I-Q0KHI~NPaMQAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAKYb3memG95nNE; BAIDUID_BFESS=5BF498BD7036CC975A4D33DD58CB1CFD:FG=1; H_WISE_SIDS_BFESS=61027_61683_62341_62485_62327_62638_62702_62519_62744_62329_62844_62867_62879_62886; MAWEBCUID=web_AegdGQKVZgIksWibiIwmFJoIhzVIBvnewQUPFVHCpIlqhjwXIE; H_PS_PSSID=61027_61683_62485_62327_62867_62886_62928_62967_63051_63056_63148_63103; antispam_key_id=23; H_WISE_SIDS=61027_61683_62485_62327_62867_62886_62928_62967_63051_63056_63148_63103; BA_HECTOR=258hag2ga1a0248420812524844m5r1k19nh622; ZFY=syxSiESHiAHzWAlctWLIdMllnxLxp1a2htnkY9R0tT8:C; BDRCVFR[uPX25oyLwh6]=mk3SLVN4HKm; delPer=0; PSINO=3; ab_sr=1.0.1_MTcwM2RmOTk5ODE1ODNmNWM4YmQ2NmZlMmQ2OGE1ZWQzOWIzOGY5N2UzODQ1NzRjY2E3NWM5YTY1MDFiNjc0ZjU1NTBkMGZkODcwYmIwNGJhZGM0ZGIyYzIzZmJlY2I1ODg0MTY1NDVlNjAzYjAwZGQ2NGU1ZmIxNDBjNWQ2MWI3ZDIxOTgwNWUxZjgyZWFiYmZhNzdjYjc1OWI4NjI4NQ==; antispam_data=74f5dcb3d56deb1b9a6c2cc89486a2245835896c86b9da56e365d452c0a928155ce00bdeec41de77a23fc04482bee48fada481eb9b0c0b61e2b46d8e2320b6181908d638ff9df691ef01a200adcd445e")
                    .form(formData)
                    .timeout(5000)
                    .execute();
            if (httpResponse.getStatus() != HttpStatus.HTTP_OK) {
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "接口调用失败");
            }
            // 解析响应
            // {"status":0,"msg":"Success","data":{"url":"https://graph.baidu.com/sc","sign":"1262fe97cd54acd88139901734784257"}}
            String body = httpResponse.body();
            Map<String, Object> result = JSONUtil.toBean(body, Map.class);
            // 3. 处理响应结果
            if (result == null || !Integer.valueOf(0).equals(result.get("status"))) {
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "接口调用失败");
            }
            Map<String, Object> data = (Map<String, Object>) result.get("data");
            // 对 URL 进行解码
            String rawUrl = (String) data.get("url");
            String searchResultUrl = URLUtil.decode(rawUrl, StandardCharsets.UTF_8);
            // 如果 URL 为空
            if (StrUtil.isBlank(searchResultUrl)) {
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "未返回有效的结果地址");
            }
            return searchResultUrl;
        } catch (Exception e) {
            log.error("调用百度以图搜图接口失败", e);
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "搜索失败");
        }
    }

    public static void main(String[] args) {
        // 测试以图搜图功能
        String imageUrl = "https://www.codefather.cn/logo.png";
        String searchResultUrl = getImagePageUrl(imageUrl);
        System.out.println("搜索成功，结果 URL：" + searchResultUrl);
    }
}
