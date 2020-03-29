package me.wuwenbin.notepress.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qiniu.common.QiniuException;
import com.qiniu.http.Response;
import com.qiniu.storage.BucketManager;
import com.qiniu.storage.Configuration;
import com.qiniu.storage.Region;
import com.qiniu.storage.UploadManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.wuwenbin.notepress.api.constants.ParamKeyConstant;
import me.wuwenbin.notepress.api.constants.enums.ReferTypeEnum;
import me.wuwenbin.notepress.api.exception.NotePressErrorCode;
import me.wuwenbin.notepress.api.exception.NotePressException;
import me.wuwenbin.notepress.api.model.NotePressResult;
import me.wuwenbin.notepress.api.model.entity.Param;
import me.wuwenbin.notepress.api.model.entity.Refer;
import me.wuwenbin.notepress.api.model.entity.Res;
import me.wuwenbin.notepress.api.model.entity.system.SysUser;
import me.wuwenbin.notepress.api.model.layui.LayuiTable;
import me.wuwenbin.notepress.api.model.layui.query.LayuiTableQuery;
import me.wuwenbin.notepress.api.model.page.NotePressPage;
import me.wuwenbin.notepress.api.query.ReferQuery;
import me.wuwenbin.notepress.api.service.IResService;
import me.wuwenbin.notepress.service.mapper.ParamMapper;
import me.wuwenbin.notepress.service.mapper.ReferMapper;
import me.wuwenbin.notepress.service.mapper.ResCateMapper;
import me.wuwenbin.notepress.service.mapper.ResMapper;
import me.wuwenbin.notepress.service.utils.NotePressSessionUtils;
import me.wuwenbin.notepress.service.utils.NotePressUploadUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author wuwen
 */
@Service
@Slf4j
@Transactional(rollbackFor = Exception.class)
@RequiredArgsConstructor(onConstructor = @__({@Autowired}))
public class ResServiceImpl extends ServiceImpl<ResMapper, Res> implements IResService {

    private final ResMapper resMapper;
    private final ResCateMapper resCateMapper;
    private final ReferMapper referMapper;
    private final ParamMapper paramMapper;

    @Override
    public NotePressResult findResList(IPage<Res> resPage, LayuiTableQuery<Res> layuiTableQuery) {
        NotePressResult r = findLayuiTableList(resMapper, resPage, layuiTableQuery);
        if (r.isSuccess()) {
            //noinspection rawtypes
            LayuiTable p = r.getDataBean(LayuiTable.class);
            if (p != null) {
                if (p.getData() != null && p.getData().size() > 0) {
                    List<Map<String, Object>> beanMapList = new ArrayList<>(p.getData().size());
                    for (Object o : p.getData()) {
                        Map<String, Object> beanMap = BeanUtil.beanToMap(o);
                        List<String> cateNameList = referMapper.findRefersByTypeAndSelfId(beanMap.get("id").toString(), ReferTypeEnum.RES_RESCATE)
                                .stream().map(refer -> resCateMapper.selectById(refer.getReferId()).getName()).collect(Collectors.toList());
                        beanMap.putIfAbsent("cateNames", cateNameList);
                        beanMapList.add(beanMap);
                    }
                    //noinspection unchecked
                    p.setData(beanMapList);
                    return NotePressResult.createOkData(p);
                }
            }
            return NotePressResult.createErrorMsg("无数据");
        }
        return NotePressResult.createErrorMsg("获取失败！");
    }

    @Override
    public NotePressResult uploadRes(MultipartFile multipartFile, int coin, String remark, List<String> cateIds) {
        Res res = this.doQiniuUpload(multipartFile, coin, remark);
        if (res != null) {
            int r = resMapper.insert(res);
            if (r == 1) {
                cateIds.forEach(cateId -> referMapper.insert(Refer.builder().selfId(res.getId()).referId(cateId).referType(ReferTypeEnum.RES_RESCATE).build()));
                return NotePressResult.createOkData(res);
            }
        }
        return NotePressResult.createErrorMsg("处理失败！");
    }


    @Override
    public NotePressResult deleteRes(String id) {
        Configuration cfg = new Configuration(Region.region0());
        BucketManager bucketManager = new BucketManager(NotePressUploadUtils.getQiniuAuth(), cfg);
        try {
            Res res = resMapper.selectById(id);
            if (res == null) {
                throw new NotePressException("文件不存在！");
            }
            Response qiniuRes = bucketManager.delete(NotePressUploadUtils.getBucketName(), res.getResHash());
            if (qiniuRes.isOK()) {
                int c = resMapper.deleteById(res.getId());
                if (c > 0) {
                    referMapper.delete(ReferQuery.buildBySelfIdAndType(res.getId(), ReferTypeEnum.RES_RESCATE));
                    return NotePressResult.createOkMsg("删除成功！");
                }
            }
            return NotePressResult.createErrorMsg("删除失败！");
        } catch (QiniuException ex) {
            //如果遇到异常，说明删除失败
            throw new NotePressException(NotePressErrorCode.QiniuError, ex.response.toString());
        }
    }

    @Override
    public NotePressResult findResList(NotePressPage<Res> resPage, String cateId, String resName) {
        String sql = "SELECT * FROM np_res WHERE id in ";
        if (StrUtil.isNotEmpty(cateId)) {
            sql += "(SELECT self_id FROM np_refer WHERE refer_id = ? and refer_type = 'res_rescate')";
            if (StrUtil.isNotEmpty(resName)) {
                sql += " AND (res_hash LIKE ? OR remark LIKE ?)";
                String resNameParam = "%" + resName + "%";
                resPage = resMapper.findPageListBeanByArray(sql, Res.class, resPage, cateId, resNameParam, resNameParam);
            } else {
                resPage = resMapper.findPageListBeanByArray(sql, Res.class, resPage, cateId);
            }
        } else {
            sql += "(SELECT self_id FROM np_refer WHERE refer_type = 'res_rescate')";
            if (StrUtil.isNotEmpty(resName)) {
                sql += " AND (res_hash LIKE ? OR remark LIKE ?)";
                String resNameParam = "%" + resName + "%";
                resPage = resMapper.findPageListBeanByArray(sql, Res.class, resPage, resNameParam, resNameParam);
            } else {
                resPage = resMapper.findPageListBeanByArray(sql, Res.class, resPage);
            }
        }
        SysUser sessionUser = NotePressSessionUtils.getSessionUser();
        if (sessionUser == null || !sessionUser.getAdmin()) {
            List<Res> newResList = resPage.getTResult().stream().peek(res -> {
                String hash = res.getResHash().substring(0, res.getResHash().indexOf("."));
                String ext = res.getResHash().substring(res.getResHash().indexOf("."));
                res.setResHash(
                        hash.length() < 3 ?
                                StrUtil.repeat("*", 3) + ext :
                                hash.substring(0, 3).concat(StrUtil.repeat("*", hash.length() - 3).concat(ext)));
                if (StrUtil.isNotEmpty(res.getRemark())) {
                    int index = res.getRemark().indexOf(".");
                    if (index > 0) {
                        String remark = res.getRemark().substring(0, index);
                        res.setRemark(remark.length() < 3 ?
                                StrUtil.repeat("*", 3) :
                                remark.substring(0, 3).concat(StrUtil.repeat("*", remark.length() - 3)));
                    }
                }
            }).collect(Collectors.toList());
            resPage.setResult(newResList);
            resPage.setTResult(newResList);
            resPage.setRawResult(newResList);
        }
        return NotePressResult.createOkData(resPage);
    }


    //============================私有方法==============================

    /**
     * 七牛上传，生成upload对象
     *
     * @param file 文件
     * @return Response
     */
    private Res doQiniuUpload(MultipartFile file, int coin, String remark) {
        try {
            String fileName = file.getOriginalFilename();
            //构造一个带指定Zone对象的配置类
            Configuration cfg = new Configuration(Region.autoRegion());
            String extend = Objects.requireNonNull(fileName).substring(fileName.lastIndexOf("."));
            //调用put方法上传
            String fileHashKey = fileName;
            int c = resMapper.selectCount(Wrappers.<Res>query().eq("res_hash", fileName));
            if (c > 0) {
                fileHashKey = fileName.substring(0, fileName.lastIndexOf(".")) + System.currentTimeMillis() + extend;
            }
            Response res = new UploadManager(cfg).put(file.getBytes(), fileHashKey, NotePressUploadUtils.getQiniuUpToken(fileHashKey));
            log.info("[七牛上传文件] - [{}] - 返回信息：{}", res.isOK(), res.bodyString());
            if (res.isOK()) {
                JSONObject respObj = JSONUtil.parseObj(res.bodyString());
                String generateFileName = respObj.getStr("key");
                String qiniuDomain = paramMapper.selectOne(Wrappers.<Param>query().eq("name", ParamKeyConstant.QINIU_DOMAIN)).getValue();
                String src = qiniuDomain + "/" + generateFileName;

                //插入到数据库中
                return newUpload(generateFileName, src, file.getSize(), coin, remark);
            } else {
                throw new NotePressException("==> 上传文件至七牛云失败，信息：" + res.error);
            }
        } catch (QiniuException e) {
            Response re = e.response;
            log.error("==> [七牛上传文件] - [{}] - 异常信息：{}", re.isOK(), re.toString());
            try {
                log.error("==> 响应异常文本信息：{}", re.bodyString());
            } catch (QiniuException ignored) {
            }
            throw new NotePressException(NotePressErrorCode.QiniuError, e.getMessage());
        } catch (Exception ex) {
            log.error("==> 文件IO读取异常，异常信息：{}", ex.getMessage());
            throw new NotePressException(NotePressErrorCode.InternalServerError, ex.getMessage());
        }
    }

    //=============================静态私有方法=======================

    /**
     * 添加记录到资源表中
     *
     * @param fileHash
     * @param fileUrl
     * @param fileByteSize
     * @return
     */
    private Res newUpload(String fileHash, String fileUrl, double fileByteSize, int coin, String remark) {
        Res res = Res.builder().id(IdUtil.objectId()).resHash(fileHash).resFsizeBytes(fileByteSize).resUrl(fileUrl).coin(coin).build();
        //上传的一些说明
        if (StringUtils.isEmpty(remark)) {
            res.setRemark(remark);
        }
        //插入到数据库中
        Long userId = Objects.requireNonNull(NotePressSessionUtils.getSessionUser()).getId();
        return res.createBy(userId).gmtCreate(LocalDateTime.now()).remark(remark);
    }
}
