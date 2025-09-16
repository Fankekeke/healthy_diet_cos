package cc.mrbird.febs.cos.controller;


import cc.mrbird.febs.common.utils.R;
import cc.mrbird.febs.cos.entity.SportTypeInfo;
import cc.mrbird.febs.cos.entity.UserInfo;
import cc.mrbird.febs.cos.entity.WeightRecordInfo;
import cc.mrbird.febs.cos.service.ISportTypeInfoService;
import cc.mrbird.febs.cos.service.IUserInfoService;
import cc.mrbird.febs.cos.service.IWeightRecordInfoService;
import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

/**
 * @author FanK
 */
@RestController
@RequestMapping("/cos/weight-record-info")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class WeightRecordInfoController {

    private final IWeightRecordInfoService weightRecordInfoService;

    private final ISportTypeInfoService sportTypeInfoService;

    private final IUserInfoService userInfoService;

    /**
     * 分页查询体重记录信息
     *
     * @param page             分页对象
     * @param weightRecordInfo 参数
     * @return 结果
     */
    @GetMapping("/page")
    public R page(Page<WeightRecordInfo> page, WeightRecordInfo weightRecordInfo) {
        return R.ok(weightRecordInfoService.queryWeightPage(page, weightRecordInfo));
    }

    /**
     * 获取数据统计
     *
     * @param userId 用户ID
     * @return 结果
     */
    @GetMapping("/selectRateWithDays")
    public R selectRateWithDays(Integer userId) {
        // 设置用户ID
        UserInfo userInfo = userInfoService.getOne(Wrappers.<UserInfo>lambdaQuery().eq(UserInfo::getUserId, userId));
        if (userInfo != null) {
            userId = userInfo.getId();
        }
        return R.ok(weightRecordInfoService.selectRateWithDays(userId));
    }

    /**
     * 查询今日摄入量
     *
     * @param userId 用户ID
     * @return 结果
     */
    @GetMapping("/queryHeatByUserToday")
    public R queryHeatByUserToday(Integer userId) {
        // 设置用户ID
        UserInfo userInfo = userInfoService.getOne(Wrappers.<UserInfo>lambdaQuery().eq(UserInfo::getUserId, userId));
        if (userInfo != null) {
            userId = userInfo.getId();
        }
        return R.ok(weightRecordInfoService.queryHeatByUserToday(userId));
    }

    /**
     * 查询所有体重记录信息
     *
     * @return 结果
     */
    @GetMapping("/list")
    public R list() {
        return R.ok(weightRecordInfoService.list());
    }

    /**
     * 获取体重记录信息详情
     *
     * @param id id
     * @return 结果
     */
    @GetMapping("/detail")
    public R detail(@RequestParam(value = "id", required = true) Integer id) {
        return R.ok(weightRecordInfoService.getById(id));
    }

    /**
     * 新增体重记录信息
     *
     * @param weightRecordInfo 参数
     * @return 结果
     */
    @PostMapping
    public R save(WeightRecordInfo weightRecordInfo) {
        weightRecordInfo.setCreateDate(DateUtil.formatDateTime(new Date()));
        // 获取运动信息
        SportTypeInfo sportTypeInfo = sportTypeInfoService.getOne(Wrappers.<SportTypeInfo>lambdaQuery().eq(SportTypeInfo::getId, weightRecordInfo.getSportId()));
        if (sportTypeInfo != null) {
            weightRecordInfo.setSportAmount(sportTypeInfo.getHeat());
            weightRecordInfo.setSportName(sportTypeInfo.getName());
            weightRecordInfo.setSportTime(sportTypeInfo.getSportTime());
        }
        if (weightRecordInfo.getUserId() != null) {
            UserInfo userInfo = userInfoService.getOne(Wrappers.<UserInfo>lambdaQuery().eq(UserInfo::getUserId, weightRecordInfo.getUserId()));
            if (userInfo != null) {
                weightRecordInfo.setUserId(userInfo.getId());
            }
        }
        return R.ok(weightRecordInfoService.save(weightRecordInfo));
    }

    /**
     * 修改体重记录信息
     *
     * @param weightRecordInfo 参数
     * @return 结果
     */
    @PutMapping
    public R edit(WeightRecordInfo weightRecordInfo) {
        return R.ok(weightRecordInfoService.updateById(weightRecordInfo));
    }

    /**
     * 删除体重记录信息
     *
     * @param ids 参数
     * @return 结果
     */
    @DeleteMapping("/{ids}")
    public R deleteByIds(@PathVariable("ids") List<Integer> ids) {
        return R.ok(weightRecordInfoService.removeByIds(ids));
    }
}
