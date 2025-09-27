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
    dishesList: [],
    selectedDishes: [],
    userInfo: null
  },
  onLoad: function (options) {
  },
  onShow() {
    wx.getStorage({
      key: 'userInfo',
      success: (res) => {
        this.setData({userInfo: res.data})
        this.queryDishesList(res.data.id)
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
  // 提交已选菜品
  submitSelectedDishes: function() {
    const selectedDishes = this.data.selectedDishes;

    if (selectedDishes.length === 0) {
      wx.showToast({
        title: '请选择至少一个菜品',
        icon: 'none'
      });
      return;
    }

    // 发送请求到服务器
    wx.request({
      url: 'http://127.0.0.1:9527/api/addDishesRecord', // 替换为实际API地址
      method: 'POST',
      data: {
        record: JSON.stringify(selectedDishes),
        userId: this.data.userInfo.id
      },
      success: (res) => {
        if (res.data) {
          wx.showToast({
            title: '提交成功',
            icon: 'success'
          });

          // 清空已选菜品
          this.setData({
            selectedDishes: [],
            totalNutrition: {
              totalHeat: 0,
              totalProtein: 0,
              totalFat: 0
            }
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
  // 选择菜品
  selectDish: function(e) {
    const dish = e.currentTarget.dataset;
    const selectedDishes = this.data.selectedDishes;

    // 检查是否已选择
    const isExist = selectedDishes.some(item => item.id === dish.id);
    if (isExist) {
      wx.showToast({
        title: '该菜品已选择',
        icon: 'none'
      });
      return;
    }

    // 添加到已选列表
    selectedDishes.push({
      id: dish.id,
      name: dish.name,
      heat: dish.heat,
      protein: dish.protein,
      fat: dish.fat
    });

    // 更新总计营养
    this.updateTotalNutrition(selectedDishes);

    this.setData({
      selectedDishes: selectedDishes
    });

    wx.showToast({
      title: '添加成功',
      icon: 'success'
    });
  },

  // 删除已选菜品
  removeDish: function(e) {
    const id = e.currentTarget.dataset.id;
    const selectedDishes = this.data.selectedDishes.filter(item => item.id !== id);

    this.updateTotalNutrition(selectedDishes);

    this.setData({
      selectedDishes: selectedDishes
    });

    wx.showToast({
      title: '删除成功',
      icon: 'success'
    });
  },

  // 更新总计营养
  updateTotalNutrition: function(selectedDishes) {
    let totalHeat = 0;
    let totalProtein = 0;
    let totalFat = 0;

    selectedDishes.forEach(dish => {
      totalHeat += parseFloat(dish.heat) || 0;
      totalProtein += parseFloat(dish.protein) || 0;
      totalFat += parseFloat(dish.fat) || 0;
    });

    this.setData({
      totalNutrition: {
        totalHeat: totalHeat.toFixed(2),
        totalProtein: totalProtein.toFixed(2),
        totalFat: totalFat.toFixed(2)
      }
    });
  },

  queryDishesList(userId) {
    http.get('dishes/list/all', {userId}).then((r) => {
      this.setData({dishesList: r.data})
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
