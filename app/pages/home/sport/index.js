const app = getApp();
let http = require('../../../utils/request')
Page({
  data: {
    StatusBar: app.globalData.StatusBar,
    CustomBar: app.globalData.CustomBar,
    TabbarBot: app.globalData.tabbar_bottom,
    bulletinList: [],
    TabCur: 0,
    scrollLeft: 0,
    userInfo: null,
    sportList: [], // 运动列表
    selectedSports: [], // 已选运动
    totalCalories: 0 // 总消耗热量
  },
  onLoad: function (options) {
    this.getPostInfo()
  },
  onShow() {
    wx.getStorage({
      key: 'userInfo',
      success: (res) => {
        this.setData({userInfo: res.data})
        this.querySportList(res.data.id)
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
  // 选择运动
  selectSport: function(e) {
    const sport = e.currentTarget.dataset;
    const sportList = this.data.sportList;
    const selectedSports = this.data.selectedSports;

    // 获取用户输入的时间
    const sportItem = sportList.find(item => item.id === sport.id);
    const userTime = sportItem && sportItem.userTime ? sportItem.userTime : sport.time;

    // 计算实际消耗热量（按比例计算）
    const actualHeat = Math.round((sport.heat / sport.time) * userTime);

    // 检查是否已选择
    const isExist = selectedSports.some(item => item.id === sport.id);
    if (isExist) {
      wx.showToast({
        title: '该运动已选择',
        icon: 'none'
      });
      return;
    }

    // 添加到已选列表
    selectedSports.push({
      id: sport.id,
      name: sport.name,
      heat: actualHeat, // 使用实际计算的热量
      sportTime: userTime // 使用用户输入的时间
    });

    // 更新总热量
    this.updateTotalCalories(selectedSports);

    this.setData({
      selectedSports: selectedSports
    });

    wx.showToast({
      title: '添加成功',
      icon: 'success'
    });
  },

  // 删除已选运动
  removeSport: function(e) {
    const id = e.currentTarget.dataset.id;
    const selectedSports = this.data.selectedSports.filter(item => item.id !== id);

    this.updateTotalCalories(selectedSports);

    this.setData({
      selectedSports: selectedSports
    });

    wx.showToast({
      title: '删除成功',
      icon: 'success'
    });
  },

  // 更新总热量
  updateTotalCalories: function(selectedSports) {
    let total = 0;
    selectedSports.forEach(sport => {
      total += parseFloat(sport.heat) || 0;
    });

    this.setData({
      totalCalories: total
    });
  },

  // 提交已选运动
  submitSelectedSports: function() {
    const selectedSports = this.data.selectedSports;

    if (selectedSports.length === 0) {
      wx.showToast({
        title: '请选择至少一个运动',
        icon: 'none'
      });
      return;
    }

    // 发送请求到服务器
    wx.request({
      url: 'http://127.0.0.1:9527/api/addSportRecord', // 替换为实际API地址
      method: 'POST',
      data: {
        record: JSON.stringify(selectedSports),
        userId: this.data.userInfo.id
      },
      success: (res) => {
        if (res.data) {
          wx.showToast({
            title: '提交成功',
            icon: 'success'
          });

          // 清空已选运动
          this.setData({
            selectedSports: [],
            totalCalories: 0
          });
        } else {
          wx.showToast({
            title: '提交失败',
            icon: 'none'
          });
        }
      },
      fail: () => {
        wx.showToast({
          title: '网络错误',
          icon: 'none'
        });
      }
    });
  },
  // 处理时间输入
  onTimeInput: function(e) {
    const id = e.currentTarget.dataset.id;
    const time = e.detail.value;

    // 更新运动列表中的用户输入时间
    const sportList = this.data.sportList.map(sport => {
      if (sport.id === id) {
        sport.userTime = time;
      }
      return sport;
    });

    this.setData({
      sportList: sportList
    });
  },
  querySportList(userId) {
    http.get('sport/list/all', {userId}).then((r) => {
      this.setData({sportList: r.data})
    })
  },
  tabSelect(e) {
    console.log(e.currentTarget.dataset.id)
    this.setData({
      TabCur: e.currentTarget.dataset.id,
      scrollLeft: (e.currentTarget.dataset.id-1)*60
    })
  },
  getPostInfo() {
    http.get('getBulletinList').then((r) => {
      // r.data.forEach(item => {
      //   if (item.images != null) {
      //     item.imagesList = item.images.split(',')
      //   } else {
      //     item.imagesList = []
      //   }
      // });
      this.setData({bulletinList: r.data})
    })
  }
});
