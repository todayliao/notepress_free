package me.wuwenbin.notepress.web.controllers.api.common;

import cn.hutool.core.util.StrUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.wuwenbin.notepress.api.constants.ParamKeyConstant;
import me.wuwenbin.notepress.api.constants.UploadConstant;
import me.wuwenbin.notepress.api.constants.enums.FileTypeEnum;
import me.wuwenbin.notepress.api.model.NotePressResult;
import me.wuwenbin.notepress.api.model.entity.Param;
import me.wuwenbin.notepress.api.service.IParamService;
import me.wuwenbin.notepress.api.service.IUploadService;
import me.wuwenbin.notepress.api.utils.NotePressFileUtils;
import me.wuwenbin.notepress.service.utils.NotePressSessionUtils;
import me.wuwenbin.notepress.service.utils.NotePressUploadUtils;
import me.wuwenbin.notepress.web.controllers.api.NotePressBaseController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Map;

/**
 * created by Wuwenbin on 2018/8/3 at 22:06
 *
 * @author wuwenbin
 */
@Slf4j
@RestController
@RequiredArgsConstructor(onConstructor = @__({@Autowired}))
public class UploadController extends NotePressBaseController {

    private final IUploadService uploadService;
    private final IParamService paramService;

    /**
     * 未登录的上传接口
     * 只允许上传图片
     *
     * @param file
     * @param reqType
     * @return
     * @throws IOException
     */
    @PostMapping("/init/upload")
    public NotePressResult upload(@RequestParam(value = UploadConstant.FORM_NAME_LAY) MultipartFile file,
                                  @RequestParam("reqType") String reqType) throws IOException {
        if (file != null && !file.isEmpty()) {
            long initFileSize = 5 * 1024 * 1024;
            if (file.getSize() <= initFileSize) {
                if (FileTypeEnum.getFileTypeByContentType(file.getContentType()).isImg()) {
                    return uploadService.doUpload(file, reqType, NotePressUploadUtils.setUploadParams(null));
                }
            } else {
                return writeJsonErrorMsg("文件大小不能超过5MB");
            }
        }
        return writeJsonErrorMsg("非法上传！");
    }

    /**
     * 登陆后上传，不限制文件和放开最大文件限制的大小
     *
     * @param file
     * @param reqType
     * @return
     * @throws IOException
     */
    @PostMapping(value = {"/token/upload", "/admin/upload"})
    public NotePressResult uploadWithToken(@RequestParam(value = UploadConstant.FORM_NAME_COMMON) MultipartFile file,
                                           @RequestParam("reqType") String reqType) throws IOException {
        if (file != null && !file.isEmpty()) {
            Param param = paramService.fetchParamByName(ParamKeyConstant.UPLOAD_MAX_SIZE).getDataBean(Param.class);
            long maxSizeB = param.getValue() != null ? Long.parseLong(param.getValue()) * 1024 : 100 * 1024 * 1024;
            if (file.getSize() <= maxSizeB) {
                Map<String, Object> uploadParams = new HashMap<>(4);
                uploadParams.put(UploadConstant.REQUEST_PARAM_USER_ID, NotePressSessionUtils.getSessionUser().getId());
                return uploadService.doUpload(file, reqType, NotePressUploadUtils.setUploadParams(uploadParams));
            } else {
                float a = (float) maxSizeB / 1024 / 1024;
                NumberFormat nf = NumberFormat.getNumberInstance();
                nf.setMaximumFractionDigits(2);
                String aa = nf.format(a);
                return writeJsonErrorMsg("文件大小不能超过{}MB", aa);
            }
        }
        return writeJsonErrorMsg("不允许上传空文件！");
    }

    /**
     * nkeditor的涂鸦上传
     *
     * @param reqType
     * @return
     * @throws IOException
     */
    @PostMapping("/admin/upload/graffiti")
    public NotePressResult uploadGraffitiWithToken(@RequestParam("reqType") String reqType) throws IOException {
        String base64 = request.getParameter("base64");
        String graffiti = "1";
        MultipartFile file = null;
        if (StrUtil.isNotEmpty(base64) && graffiti.equals(base64)) {
            String base64Str = request.getParameter("img_base64_data");
            file = NotePressFileUtils.base64ToMultipartFile(base64Str);
        }
        return this.uploadWithToken(file, reqType);
    }
}
