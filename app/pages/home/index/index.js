const app = getApp();
let http = require('../../../utils/request')
Page({
    data: {
        StatusBar: app.globalData.StatusBar,
        CustomBar: app.globalData.CustomBar,
        hidden: true,
        current: 0, lines: 0,
        swiperlist: [{
            id: 0,
            url: 'http://127.0.0.1:9527/imagesWeb/SA1758117349160.jpg',
            type: 1
        }, {
            id: 1,
            url: 'http://127.0.0.1:9527/imagesWeb/SA1758117351445.jpg',
            type: 2
        }, {
            id: 2,
            url: 'http://127.0.0.1:9527/imagesWeb/SA1758117353621.jpg',
            type: 3
        }, {
            id: 3,
            url: 'http://127.0.0.1:9527/imagesWeb/SA1758117607504.jpg',
            type: 4
        }],
        iconList: [{
            id: 1,
            icon: 'questionfill',
            color: 'red',
            name: '公告',
            type: 1
        }, {
            id: 2,
            icon: 'group_fill',
            color: 'orange',
            name: '饮食',
            type: 2
        }, {
            id: 3,
            icon: 'shopfill',
            color: 'yellow',
            name: '运动',
            type: 3
        }],
        iconList1: [{
            id: 1,
            icon: 'questionfill',
            color: 'red',
            name: '公告',
            type: 1
        }, {
            id: 2,
            icon: 'group_fill',
            color: 'orange',
            name: '热门',
            type: 2
        }, {
            id: 3,
            icon: 'shopfill',
            color: 'yellow',
            name: '论坛',
            type: 3
        }, {
            id: 4,
            icon: 'discoverfill',
            color: 'olive',
            name: '商品',
            type: 4
        }],
        Headlines: [{
            id: 1,
            title: "新品上架",
            type: 1
        }, {
            id: 2,
            title: "省钱大作战",
            type: 2
        }],
        shopInfo: [],
        postInfo: [],
        commodityHot: [],
        keys: '',
        userInfo: null,
        videosrc: "http://wxsnsdy.tc.qq.com/105/20210/snsdyvideodownload?filekey=30280201010421301f0201690402534804102ca905ce620b1241b726bc41dcff44e00204012882540400&bizid=1023&hy=SH&fileparam=302c020101042530230204136ffd93020457e3c4ff02024ef202031e8d7f02030f42400204045a320a0201000400",
        titleData: {
            heat: 0,
            proteinRule: 0,
            heatRule: 0,
            protein: 0,
            fat: 0,
            fatRule: 0,
        },
        yAxisMax: 500,
        yAxisLabels: [500, 400, 300, 200, 100, 0],
        lineYAxisMax: 500,
        lineYAxisLabels: [500, 400, 300, 200, 100, 0],
        caloriesOut: [],
        caloriesIn: [],
        caloriesInLines: [],
        dishesList: [],
        sportList: [],
        selectedDishes: [], // 已选菜品
    },
    onLoad: function () {
        /*console.log(app.globalData.StatusBar);
        console.log(app.globalData.CustomBar);*/
        // wx.getSetting({
        //     success: res => {
        //         if (!res.authSetting['scope.userInfo']) {
        //             wx.redirectTo({
        //               	url: '/pages/auth/auth'
        //             })
        //         }
        //     }
        // });
        this.calculateLineData();
        this.home()
        // this.getPostInfo()
    },
    onShow() {
        wx.getStorage({
            key: 'userInfo',
            success: (res) => {
                this.setData({userInfo: res.data})
                this.queryHeatByUserToday(res.data.id)
                this.selectRateWithDays(res.data.id)
            },
            fail: res => {
                wx.showToast({
                    title: '请先进行登录',
                    icon: 'none',
                    duration: 2000
                })
            }
        })
    },


    queryHeatByUserToday(userId) {
        http.get('queryHeatByUserToday', {userId}).then((r) => {
            this.setData({titleData: r})
        })
    },
    selectRateWithDays(userId) {
        http.get('selectRateWithDays', {userId}).then((r) => {
            this.setData({caloriesOut: r.caloriesOut, caloriesIn: r.caloriesIn})
            // 计算Y轴最大值
            const yAxisMax = this.calculateYAxisMax(this.data.caloriesOut);
            const yAxisLabels = this.generateYAxisLabels(yAxisMax);

            this.setData({
                yAxisMax: yAxisMax,
                yAxisLabels: yAxisLabels
            });

            // 计算折线图Y轴最大值
            const lineYAxisMax = this.calculateYAxisMax(this.data.caloriesIn);
            const lineYAxisLabels = this.generateYAxisLabels(lineYAxisMax);

            this.setData({
                lineYAxisMax: lineYAxisMax,
                lineYAxisLabels: lineYAxisLabels
            });
        })
    },
    // 计算折线坐标的方法
    calculateLineData: function () {
        const caloriesIn = this.data.caloriesIn;
        const lines = [];

        for (let i = 0; i < caloriesIn.length - 1; i++) {
            const startX = i * 10;
            const startY = 100 - (caloriesIn[i].amount / 500 * 100);
            const endX = (i + 1) * 10;
            const endY = 100 - (caloriesIn[i + 1].amount / 500 * 100);

            const width = Math.sqrt(Math.pow(endX - startX, 2) + Math.pow(endY - startY, 2));
            const angle = Math.atan2(endY - startY, endX - startX) * 180 / Math.PI;

            lines.push({
                startX: startX,
                startY: startY,
                width: width,
                angle: angle
            });
        }

        this.setData({
            caloriesInLines: lines
        });
    },
    // 计算Y轴最大值和刻度
    calculateYAxisMax: function (dataArray) {
        // 获取数据中的最大值
        const maxAmount = Math.max(...dataArray.map(item => item.amount));

        // 将最大值向上取整到最近的100的倍数，确保图表有适当的空间
        const yAxisMax = Math.ceil(maxAmount / 100) * 100;

        // 如果最大值为0，则设置默认值500
        return yAxisMax > 0 ? yAxisMax : 500;
    },

// 生成Y轴刻度标签
    generateYAxisLabels: function (maxValue) {
        const labels = [];
        for (let i = 5; i >= 0; i--) {
            labels.push(Math.round(maxValue * i / 5));
        }
        return labels;
    },
    getPostInfo() {
        http.get('getPostList').then((r) => {
            r.data.forEach(item => {
                if (item.images != null) {
                    item.image = item.images.split(',')[0]
                }
                item.days = this.timeFormat(item.createDate)
            });
            if (r.data !== null) {
                this.setData({postInfo: r.data})
            }
        })
    },
    timeFormat(time) {
        var nowTime = new Date();
        var day = nowTime.getDate();
        var hours = parseInt(nowTime.getHours());
        var minutes = nowTime.getMinutes();
        // 开始分解付入的时间
        var timeday = time.substring(8, 10);
        var timehours = parseInt(time.substring(11, 13));
        var timeminutes = time.substring(14, 16);
        var d_day = Math.abs(day - timeday);
        var d_hours = hours - timehours;
        var d_minutes = Math.abs(minutes - timeminutes);
        if (d_day <= 1) {
            switch (d_day) {
                case 0:
                    if (d_hours == 0 && d_minutes > 0) {
                        return d_minutes + '分钟前';
                    } else if (d_hours == 0 && d_minutes == 0) {
                        return '1分钟前';
                    } else {
                        return d_hours + '小时前';
                    }
                    break;
                case 1:
                    if (d_hours < 0) {
                        return (24 + d_hours) + '小时前';
                    } else {
                        return d_day + '天前';
                    }
                    break;
            }
        } else if (d_day > 1 && d_day < 10) {
            return d_day + '天前';
        } else {
            return time;
        }
    },
    home() {
        http.get('home').then((r) => {
            r.commodityHot.forEach(item => {
                item.image = item.images.split(',')[0]
            });
            r.postInfo.forEach(item => {
                item.image = item.images.split(',')[0]
                item.days = this.timeFormat(item.createDate)
            });
            this.setData({
                shopInfo: r.shopInfo,
                postInfo: r.postInfo,
                commodityHot: r.commodityHot
            })
        })
    },
    postDetail(event) {
        wx.navigateTo({
            url: '/pages/coupon/detail/index?postId=' + event.currentTarget.dataset.postid + ''
        });
    },
    swiperchange: function (e) {
        this.setData({
            current: e.detail.current
        });
    },
    swipclick: function (e) {
        let that = this;
        var swip = that.data.swiperlist[that.data.current];
        console.log(swip);
        if (swip.type === 1) {
            wx.navigateTo({
                url: '/pages/home/doc/index?id=' + swip.id
            });
        }
    },
    lineschange: function (e) {
        this.setData({
            lines: e.detail.current
        });
    },
    linesclick: function (e) {
        let that = this;
        var swip = that.data.Headlines[that.data.current];
        console.log(swip);
        if (swip.type === 1) {
            wx.navigateTo({
                url: '/pages/home/doc/index?id=' + swip.id
            });
        }
    },
    itemckcred: function (e) {
        let that = this;
        var item = e.currentTarget.dataset;
        console.log(item.index, item.itemtype)
        if (item.itemtype === 1) {
            wx.navigateTo({
                url: '/pages/home/bulletin/index'
            });
        }
        if (item.itemtype === 2) {
            wx.navigateTo({
                url: '/pages/home/dishes/index'
            });
        }
        if (item.itemtype === 3) {
            wx.navigateTo({
                url: '/pages/home/sport/index'
            });
        }
        if (item.itemtype === 4) {
            wx.navigateTo({
                url: '/pages/home/search/index?key=生鲜'
            });
        }
    },
    getKeysValue: function (e) {
        this.setData({keys: e.detail.value})
    },
    search: function () {
        wx.navigateTo({
            url: '/pages/home/search/index?key=' + this.data.keys + ''
        });
    }
});
