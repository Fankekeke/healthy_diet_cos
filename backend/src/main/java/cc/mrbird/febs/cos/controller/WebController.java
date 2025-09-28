package cc.mrbird.febs.cos.controller;

import cc.mrbird.febs.common.exception.FebsException;
import cc.mrbird.febs.common.utils.R;
import cc.mrbird.febs.cos.dao.MessageInfoMapper;
import cc.mrbird.febs.cos.dao.SensitiveInfoMapper;
import cc.mrbird.febs.cos.entity.*;
import cc.mrbird.febs.cos.service.*;
import cc.mrbird.febs.system.service.UserService;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import org.apache.http.HttpEntity;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class WebController {

    private final IUserInfoService userInfoService;

    private final IBulletinInfoService bulletinInfoService;

    private final IPostInfoService postInfoService;

    private final IReplyInfoService replyInfoService;

    private final IMessageInfoService messageInfoService;

    private final ITagInfoService tagInfoService;

    private final ICollectInfoService collectInfoService;

    private final IFocusInfoService focusInfoService;

    private final SensitiveInfoMapper sensitiveInfoMapper;

    private final MessageInfoMapper messageInfoMapper;

    private final UserService userService;

    private final IWeightRecordInfoService weightRecordInfoService;

    private final IDishesInfoService dishesInfoService;

    private final ISportTypeInfoService sportTypeInfoService;

    private final IDietRecordInfoService dietRecordInfoService;

    @PostMapping("/userAdd")
    public R userAdd(@RequestBody UserInfo user) throws Exception {
        String url = "https://api.weixin.qq.com/sns/jscode2session";
        url += "?appid=wx76a6577665633a86";//自己的appid
        url += "&secret=78070ccedf3f17b272b84bdeeca28a2e";//自己的appSecret
        url += "&js_code=" + user.getOpenId();
        url += "&grant_type=authorization_code";
        url += "&connect_redirect=1";
        String res = null;
        CloseableHttpClient httpClient = HttpClientBuilder.create().build();
        // DefaultHttpClient();
        HttpGet httpget = new HttpGet(url);
        CloseableHttpResponse response = null;
        // 配置信息
        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectTimeout(5000)
                .setConnectionRequestTimeout(5000)
                .setSocketTimeout(5000)
                .setRedirectsEnabled(false).build();
        httpget.setConfig(requestConfig);
        response = httpClient.execute(httpget);
        HttpEntity responseEntity = response.getEntity();
        System.out.println("响应状态为:" + response.getStatusLine());
        if (responseEntity != null) {
            res = EntityUtils.toString(responseEntity);
            System.out.println("响应内容长度为:" + responseEntity.getContentLength());
            System.out.println("响应内容为:" + res);
        }
        // 释放资源
        httpClient.close();
        response.close();
        String openid = JSON.parseObject(res).get("openid").toString();
        System.out.println("openid" + openid);
        int count = userInfoService.count(Wrappers.<UserInfo>lambdaQuery().eq(UserInfo::getOpenId, openid));
        if (count > 0) {
            return R.ok(userInfoService.getOne(Wrappers.<UserInfo>lambdaQuery().eq(UserInfo::getOpenId, openid)));
        } else {
            user.setOpenId(openid);
            user.setCreateDate(DateUtil.formatDateTime(new Date()));
            user.setCode("UR-" + System.currentTimeMillis());
            user.setName(user.getUserName());

            // 图片上传
            byte[] bytes = HttpUtil.downloadBytes(user.getAvatar());
            MultipartFile multipartFile = new MockMultipartFile("xxx.jpg", "xxx.jpg", ".jpg", bytes);
//            MultipartFile cMultiFile = new MockMultipartFile("file", file.getName(), null, new FileInputStream(file));
//
            // 1定义要上传文件 的存放路径
            String localPath = "G:/Project/20250916健康饮食管理小程序/db";
            // 2获得文件名字
            String fileName = multipartFile.getName();
            // 2上传失败提示
            String warning = "";
            String newFileName = cc.mrbird.febs.common.utils.FileUtil.upload(multipartFile, localPath, fileName);
            if (newFileName != null) {
                //上传成功
                warning = newFileName;
            } else {
                warning = "上传失败";
            }
            System.out.println(warning);
            user.setImages(warning);
            return R.ok(userService.registUser(user, "1234qwer"));
        }
    }

    @RequestMapping("/openid")
    public R getUserInfo(@RequestParam(name = "code") String code) throws Exception {
        System.out.println("code" + code);
        String url = "https://api.weixin.qq.com/sns/jscode2session";
        url += "?appid=wx9fffb151ded22005";//自己的appid
        url += "&secret=9666e94c91361e7de4d3a6d09a23402f";//自己的appSecret
        url += "&js_code=" + code;
        url += "&grant_type=authorization_code";
        url += "&connect_redirect=1";
        String res = null;
        CloseableHttpClient httpClient = HttpClientBuilder.create().build();
        // DefaultHttpClient();
        HttpGet httpget = new HttpGet(url);    //GET方式
        CloseableHttpResponse response = null;
        // 配置信息
        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectTimeout(5000)
                .setConnectionRequestTimeout(5000)
                .setSocketTimeout(5000)
                .setRedirectsEnabled(false).build();
        httpget.setConfig(requestConfig);
        response = httpClient.execute(httpget);
        HttpEntity responseEntity = response.getEntity();
        System.out.println("响应状态为:" + response.getStatusLine());
        if (responseEntity != null) {
            res = EntityUtils.toString(responseEntity);
            System.out.println("响应内容长度为:" + responseEntity.getContentLength());
            System.out.println("响应内容为:" + res);
        }
        // 释放资源
        httpClient.close();
        response.close();
        String openid = JSON.parseObject(res).get("openid").toString();
        System.out.println("openid" + openid);
        return R.ok(openid);
    }

    @GetMapping("/subscription")
    public R subscription(@RequestParam("taskCode") String taskCode) throws Exception {
        LinkedHashMap<String, Object> tokenParam = new LinkedHashMap<String, Object>() {
            {
                put("grant_type", "client_credential");
                put("appid", "wx76a6577665633a86");
                put("secret", "78070ccedf3f17b272b84bdeeca28a2e");
            }
        };
        String tokenResult = HttpUtil.get("https://api.weixin.qq.com/cgi-bin/token", tokenParam);
        String token = JSON.parseObject(tokenResult).get("access_token").toString();
        LinkedHashMap<String, Object> data = new LinkedHashMap<String, Object>() {
            {
                put("thing1", new HashMap<String, Object>() {
                    {
                        put("value", "张三");
                    }
                });
                put("character_string3", new HashMap<String, Object>() {
                    {
                        put("value", taskCode);
                    }
                });
                put("time4", new HashMap<String, Object>() {
                    {
                        put("value", DateUtil.formatDateTime(new Date()));
                    }
                });
                put("thing5", new HashMap<String, Object>() {
                    {
                        put("value", "若查看详细内容，请登录小程序");
                    }
                });
            }
        };
        String url = "https://api.weixin.qq.com/cgi-bin/message/subscribe/send?access_token=" + token;
        LinkedHashMap<String, Object> subscription = new LinkedHashMap<String, Object>() {
            {
                put("touser", "oeDfR5zqxQD3EEA3uPT836qnauSc");
                put("template_id", "Z27pBK1n9WnKNtQ_fo7TC-nUJUlOQ9KVJk6LIgp0nH8");
                put("data", data);
            }
        };
        String result = HttpUtil.post(url, JSONUtil.toJsonStr(subscription));
        return R.ok(result);
    }

//    /**
//     * 进入小程序主页信息
//     *
//     * @return 结果
//     */
//    @GetMapping("/home")
//    public R home() {
//        LinkedHashMap<String, Object> result = new LinkedHashMap<>();
//        result.put("commodityHot", commodityInfoService.getCommodityHot());
//        result.put("shopInfo", shopInfoService.shopInfoHot());
//        result.put("postInfo", postInfoService.getPostListHot());
//        return R.ok(result);
//    }

    @GetMapping("/selectTagList")
    public R selectTagList() {
        return R.ok(tagInfoService.list(Wrappers.<TagInfo>lambdaQuery().eq(TagInfo::getDeleteFlag, 0)));
    }

    /**
     * 获取贴子信息
     *
     * @return 结果
     */
    @GetMapping("/getPostList")
    public R getPostList() {
        return R.ok(postInfoService.getPostByTag(null));
    }

    /**
     * 根据用户获取贴子信息
     *
     * @param userId 用户ID
     * @return 结果
     */
    @GetMapping("/getPostByUser")
    public R getPostByUser(@RequestParam("userId") Integer userId) {
        return R.ok(postInfoService.getPostByUser(userId));
    }

    /**
     * 删除贴子信息
     *
     * @param postId 贴子ID
     * @return 结果
     */
    @GetMapping("/deletePost")
    public R deletePost(@RequestParam("postId") Integer postId) {
        return R.ok(postInfoService.update(Wrappers.<PostInfo>lambdaUpdate().set(PostInfo::getDeleteFlag, 1).eq(PostInfo::getId, postId)));
    }

    /**
     * 获取用户记录
     *
     * @param userId 用户ID
     * @return 结果
     */
    @GetMapping("/getListByUserId")
    public R getListByUserId(@RequestParam("userId") Integer userId) {
        // 返回数据
        LinkedHashMap<String, Object> result = new LinkedHashMap<String, Object>() {
            {
                put("collect", Collections.emptyList());
                put("focus", Collections.emptyList());
                put("reply", Collections.emptyList());
            }
        };
        result.put("collect", collectInfoService.selectCollectByUser(userId));
        result.put("focus", focusInfoService.selectFocusByUser(userId));
        result.put("reply", replyInfoService.replyListByUserId(userId));
        return R.ok(result);
    }

    /**
     * 根据贴子编号获取详细信息
     *
     * @param postId
     * @return 结果
     */
    @GetMapping("/getPostInfoById")
    public R getPostInfoById(@RequestParam Integer postId, @RequestParam(value = "userId", required = false) String userId) {
        return R.ok(postInfoService.getPostInfoById(postId));
    }

    @GetMapping("/home")
    public R home() {
        LinkedHashMap<String, Object> result = new LinkedHashMap<>();
        result.put("postInfo", postInfoService.getPostListHot());
        return R.ok(result);
    }

    /**
     * 获取用户信息
     *
     * @param userId 用户ID
     * @return 结果
     */
    @GetMapping("/selectUserInfo")
    public R selectUserInfo(@RequestParam("userId") Integer userId) {
        return R.ok(userInfoService.getById(userId));
    }

    /**
     * 修改用户信息
     *
     * @param userInfo 用户信息
     * @return 结果
     */
    @PostMapping("/editUserInfo")
    public R editUserInfo(@RequestBody UserInfo userInfo) {
        return R.ok(userInfoService.updateById(userInfo));
    }

    /**
     * 贴子回复
     *
     * @return 结果
     */
    @PostMapping("/replyPost")
    public R replyPost(@RequestBody ReplyInfo replyInfo) throws FebsException {
        replyInfo.setSendCreate(DateUtil.formatDateTime(new Date()));
        // 是否违规
        List<SensitiveInfo> sensitiveInfoList = sensitiveInfoMapper.selectList(Wrappers.<SensitiveInfo>lambdaQuery());
        List<String> sensitiveWordList = sensitiveInfoList.stream().map(SensitiveInfo::getKeyName).collect(Collectors.toList());
        for (String word : sensitiveWordList) {
            if (replyInfo.getContent().contains(word)) {
                throw new FebsException("回复内容中包含敏感词！");
            }
        }

        // 帖子信息
        PostInfo postInfo = postInfoService.getById(replyInfo.getPostId());
        // 帖子所属人消息
        MessageInfo messageInfo = new MessageInfo();
        messageInfo.setSendUser(postInfo.getUserId());
        messageInfo.setCreateDate(DateUtil.formatDateTime(new Date()));
        String content = "您发布的贴子【" + postInfo.getTitle() + "】有人进行了回复，【" + replyInfo.getContent() + "】，请查看";
        messageInfo.setContent(content);
        messageInfoService.save(messageInfo);
        replyInfo.setDeleteFlag(0);
        return R.ok(replyInfoService.save(replyInfo));
    }

    /**
     * 添加贴子
     *
     * @param postInfo 帖子信息
     * @return 结果
     */
    @PostMapping("/postAdd")
    public R postAdd(@RequestBody PostInfo postInfo) throws FebsException {
        postInfo.setCreateDate(DateUtil.formatDateTime(new Date()));
        // 是否违规
        List<SensitiveInfo> sensitiveInfoList = sensitiveInfoMapper.selectList(Wrappers.<SensitiveInfo>lambdaQuery());
        List<String> sensitiveWordList = sensitiveInfoList.stream().map(SensitiveInfo::getKeyName).collect(Collectors.toList());
        for (String word : sensitiveWordList) {
            if (postInfo.getContent().contains(word)) {
                throw new FebsException("帖子内容中包含敏感词！");
            }
        }

        // 此用户的粉丝
        List<FocusInfo> focusInfoList = focusInfoService.list(Wrappers.<FocusInfo>lambdaQuery().eq(FocusInfo::getCollectUserId, postInfo.getUserId()));
        if (CollectionUtil.isNotEmpty(focusInfoList)) {
            List<MessageInfo> messageList = new ArrayList<>();
            String content = "您关注的用户发布了贴子【" + postInfo.getTitle() + "】，请查看";
            for (FocusInfo focusInfo : focusInfoList) {
                MessageInfo messageInfo = new MessageInfo();
                messageInfo.setSendUser(focusInfo.getUserId());
                messageInfo.setCreateDate(DateUtil.formatDateTime(new Date()));
                messageInfo.setContent(content);
                messageList.add(messageInfo);
            }

            messageInfoService.saveBatch(messageList);
        }
        postInfo.setDeleteFlag(0);
        postInfo.setPageviews(0);
        return R.ok(postInfoService.save(postInfo));
    }

    /**
     * 获取公告信息
     *
     * @return 结果
     */
    @GetMapping("/getBulletinList")
    public R getBulletinList() {
        return R.ok(bulletinInfoService.list());
    }

    /**
     * 关注/取关 用户
     *
     * @param userId      用户ID
     * @param focusUserId 关注用户ID
     * @param type        操作 1.关注 2.取关
     * @return 结果
     */
    @GetMapping("/focusUser")
    public R focusUser(@RequestParam("userId") Integer userId, @RequestParam("focusUserId") Integer focusUserId, @RequestParam("type") Integer type) {
        return R.ok(focusInfoService.focusUser(userId, focusUserId, type));
    }

    /**
     * 获取用户是否关注此用户
     *
     * @param userId      用户ID
     * @param focusUserId 用户ID
     * @return 结果
     */
    @GetMapping("/queryFocusUser")
    public R queryFocusUser(@RequestParam("userId") Integer userId, @RequestParam("focusUserId") Integer focusUserId) {
        return R.ok(focusInfoService.count(Wrappers.<FocusInfo>lambdaQuery().eq(FocusInfo::getUserId, userId).eq(FocusInfo::getCollectUserId, focusUserId)));
    }

    /**
     * 收藏/取消 贴子
     *
     * @param userId 用户ID
     * @param postId 贴子ID
     * @param type   操作 1.关注 2.取关
     * @return 结果
     */
    @GetMapping("/collectPost")
    public R collectPost(@RequestParam("userId") Integer userId, @RequestParam("postId") Integer postId, @RequestParam("type") Integer type) {
        return R.ok(collectInfoService.collectPost(userId, postId, type));
    }

    /**
     * 获取用户是否收藏贴子
     *
     * @param userId 用户ID
     * @param postId 贴子ID
     * @return 结果
     */
    @GetMapping("/queryCollectPost")
    public R queryCollectPost(@RequestParam("userId") Integer userId, @RequestParam("postId") Integer postId) {
        return R.ok(collectInfoService.count(Wrappers.<CollectInfo>lambdaQuery().eq(CollectInfo::getPostId, postId).eq(CollectInfo::getUserId, userId)));
    }

    /**
     * 根据用户获取消息信息
     *
     * @param userId 用户ID1
     * @return 结果
     */
    @GetMapping("/queryMessageByUser")
    public R queryMessageByUser(@RequestParam("userId") Integer userId) {
        return R.ok(messageInfoMapper.selectList(Wrappers.<MessageInfo>lambdaQuery().eq(MessageInfo::getSendUser, userId)));
    }

    /**
     * 删除消息
     *
     * @param messageId 消息ID
     * @return 结果
     */
    @GetMapping("/delMessage")
    public R delMessage(@RequestParam("messageId") Integer messageId) {
        return R.ok(messageInfoMapper.deleteById(messageId));
    }

    /**
     * 查询帖子及用户信息
     *
     * @return 结果
     */
    @GetMapping("/selShopDetailList")
    public R selShopDetailList(@RequestParam(value = "key", required = false) String key) {
        return R.ok(postInfoService.querySearch(key));
    }

    /**
     * 获取用户及贴子详细信息
     *
     * @param userId 用户ID
     * @return 结果
     */
    @GetMapping("/getShopDetail")
    public R getShopDetail(@RequestParam Integer userId) {
        return R.ok(postInfoService.getUserPostDetail(userId));
    }

    /**
     * 根据用户ID获取权重信息
     * @param userId 用户ID
     * @return 结果
     */
    @GetMapping("/queryWeightListById")
    public R queryWeightListById(Integer userId) {
        return R.ok(weightRecordInfoService.list(Wrappers.<WeightRecordInfo>lambdaQuery().eq(WeightRecordInfo::getUserId, userId)));
    }

    /**
     * 添加权重信息
     * @param weightRecordInfo 权重信息
     * @return 结果
     */
    @PostMapping("/addWeight")
    public R addWeight(@RequestBody WeightRecordInfo weightRecordInfo) {
        weightRecordInfo.setCreateDate(DateUtil.formatDateTime(new Date()));
        return R.ok(weightRecordInfoService.save(weightRecordInfo));
    }

    @GetMapping("/deleteWeight")
    public R deleteWeight(Integer weightId) {
        return R.ok(weightRecordInfoService.removeById(weightId));
    }

    @PostMapping("/addDishesInfo")
    public R addDishesInfo(@RequestBody DishesInfo dishesInfo) {
        dishesInfo.setCode("DIS-" + System.currentTimeMillis());
        dishesInfo.setCreateDate(DateUtil.formatDateTime(new Date()));
        return R.ok(dishesInfoService.save(dishesInfo));
    }

    @GetMapping("/queryDishesListById")
    public R queryDishesListById(@RequestParam("userId") Integer userId) {
        return R.ok(dishesInfoService.list(Wrappers.<DishesInfo>lambdaQuery().eq(DishesInfo::getUserId, userId)));
    }

    @GetMapping("/deleteDishes")
    public R deleteDishes(@RequestParam("postId") Integer postId) {
        return R.ok(dishesInfoService.removeById(postId));
    }

    @PostMapping("/addSport")
    public R addSportInfo(@RequestBody SportTypeInfo sportTypeInfo) {
        sportTypeInfo.setCode("ST-" + System.currentTimeMillis());
        sportTypeInfo.setCreateDate(DateUtil.formatDateTime(new Date()));
        return R.ok(sportTypeInfoService.save(sportTypeInfo));
    }

    @GetMapping("/querySportListById")
    public R querySportListById(@RequestParam("userId") Integer userId) {
        return R.ok(sportTypeInfoService.list(Wrappers.<SportTypeInfo>lambdaQuery().eq(SportTypeInfo::getUserId, userId)));
    }

    @GetMapping("/deleteSport")
    public R deleteSport(@RequestParam("postId") Integer postId) {
        return R.ok(sportTypeInfoService.removeById(postId));
    }

    @GetMapping("/queryDishesRecordListById")
    public R queryDishesRecordListById(@RequestParam("userId") Integer userId) {
        DietRecordInfo dietRecordInfo = new DietRecordInfo();
        dietRecordInfo.setUserId(userId);
        return R.ok(dietRecordInfoService.queryDietRecord(dietRecordInfo));
    }

    @GetMapping("/deleteDishesRecord")
    public R deleteDishesRecord(@RequestParam("postId") Integer postId) {
        return R.ok(dietRecordInfoService.removeById(postId));
    }

    @GetMapping("/querySportRecordListById")
    public R querySportRecordListById(@RequestParam("userId") Integer userId) {
        WeightRecordInfo weightRecordInfo = new WeightRecordInfo();
        weightRecordInfo.setUserId(userId);
        return R.ok(weightRecordInfoService.querySportRecord(weightRecordInfo));
    }

    @GetMapping("/deleteSportRecord")
    public R deleteSportRecord(@RequestParam("postId") Integer postId) {
        return R.ok(weightRecordInfoService.removeById(postId));
    }

    @GetMapping("/queryHeatByUserToday")
    public R queryHeatByUserToday(@RequestParam("userId") Integer userId) {
        return R.ok(weightRecordInfoService.queryHeatByUserToday(userId));
    }

    @GetMapping("/selectRateWithDays")
    public R selectRateWithDays(@RequestParam("userId") Integer userId) {
        return R.ok(weightRecordInfoService.selectRateWithDays(userId));
    }

    /**
     * 根据用户查询菜品信息
     *
     * @param userId 用户id
     * @return 结果
     */
    @GetMapping("/dishes/list/all")
    public R queryDishesByUserId(Integer userId) {
        List<DishesInfo> dishesInfoList = dishesInfoService.list(Wrappers.<DishesInfo>lambdaQuery().isNull(DishesInfo::getUserId));
        for (DishesInfo dishesInfo1 : dishesInfoList) {
            dishesInfo1.setUserName("系统食谱");
        }
        List<DishesInfo> userDishesList = dishesInfoService.queryDishesByUserId(userId);
        // dishesInfoList和userDishesList合并
        dishesInfoList.addAll(userDishesList);
        return R.ok(dishesInfoList);
    }

    /**
     * 根据用户查询运动种类信息
     *
     * @param userId 用户id
     * @return 结果
     */
    @GetMapping("/sport/list/all")
    public R querySportByUserId(Integer userId) {
        List<SportTypeInfo> sportTypeInfoList = sportTypeInfoService.list(Wrappers.<SportTypeInfo>lambdaQuery().isNull(SportTypeInfo::getUserId));
        List<SportTypeInfo> sportTypeInfoList1 = sportTypeInfoService.list(Wrappers.<SportTypeInfo>lambdaQuery().eq(SportTypeInfo::getUserId, userId));
        sportTypeInfoList.addAll(sportTypeInfoList1);
        return R.ok(sportTypeInfoList);
    }

    /**
     * 添加饮食记录
     *
     * @param dietRecordInfo 饮食记录信息
     * @return 添加结果
     */
    @PostMapping("/addDishesRecord")
    public R addDishesRecord(@RequestBody DietRecordInfo dietRecordInfo) {
        dietRecordInfo.setCreateDate(DateUtil.formatDateTime(new Date()));
        List<DietRecordInfo> dietRecordInfoList = JSONUtil.toList(dietRecordInfo.getRecord(), DietRecordInfo.class);
        for (DietRecordInfo record : dietRecordInfoList) {
            record.setDishesId(record.getId());
            record.setUserId(dietRecordInfo.getUserId());
            record.setCreateDate(dietRecordInfo.getCreateDate());
        }
        return R.ok(dietRecordInfoService.saveBatch(dietRecordInfoList));
    }

    /**
     * 添加运动记录
     *
     * @param weightRecordInfo 运动记录信息
     * @return 添加结果
     */
    @PostMapping("/addSportRecord")
    public R addSportRecord(@RequestBody WeightRecordInfo weightRecordInfo) {
        weightRecordInfo.setCreateDate(DateUtil.formatDateTime(new Date()));
        List<WeightRecordInfo> weightRecordInfoList = JSONUtil.toList(weightRecordInfo.getRecord(), WeightRecordInfo.class);
        for (WeightRecordInfo record : weightRecordInfoList) {
            record.setUserId(weightRecordInfo.getUserId());
            record.setCreateDate(weightRecordInfo.getCreateDate());
            record.setSportAmount(NumberUtil.mul(record.getHeat(), record.getSportTime()));
            record.setSportName(record.getName());
            record.setContent(record.getName() + "-" + record.getSportTime() + "分钟");
        }
        return R.ok(weightRecordInfoService.saveBatch(weightRecordInfoList));
    }
}
