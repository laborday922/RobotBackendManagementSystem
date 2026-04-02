<template>
  <div class="data-dashboard">

    <!-- ===== 顶部筛选（玻璃条） ===== -->
    <div class="glass-bar">
      <el-select v-model="dashboardTimeRange" size="small" @change="onTimeRangeChange">
        <el-option label="今日" value="today"/>
        <el-option label="本周" value="week"/>
        <el-option label="本月" value="month"/>
        <el-option label="本年" value="year"/>
      </el-select>

      <el-button size="small" @click="refreshDashboard">
        刷新
      </el-button>
    </div>

    <!-- ===== 关键指标 ===== -->
    <div class="key-metrics">
      <div class="glass-card metric-card" v-for="item in metricList" :key="item.label">
        <div class="metric-value">{{ item.value }}</div>
        <div class="metric-label">{{ item.label }}</div>
      </div>
    </div>

    <!-- ===== 主体 ===== -->
    <div class="dashboard-main">

      <!-- 左 -->
      <div class="left-panel">

        <div class="glass-card">
          <h3>在线机器人情况</h3>
          <div id="robotStatusChart" class="chart-box"></div>
        </div>

        <div class="glass-card">
          <h3>任务执行情况</h3>

          <div class="task-section">

            <div class="task-category">
              <div class="category-title">
                ⏳ 执行中 ({{ taskStats.executing }})
              </div>

              <div class="task-item" v-for="item in executingTasks" :key="item.id">
                <div class="task-header">
                  <span>{{ item.name }}</span>
                  <span>{{ item.progress }}%</span>
                </div>
                <el-progress :percentage="item.progress" :show-text="false"/>
              </div>
            </div>

            <div class="task-category">
              <div class="category-title">
                ⏰ 待执行 ({{ taskStats.pending }})
              </div>

              <div class="task-item pending" v-for="item in pendingTasks" :key="item.id">
                <div class="task-header">
                  <span>{{ item.name }}</span>
                  <span>{{ item.scheduledTime }}</span>
                </div>
              </div>
            </div>

          </div>
        </div>

      </div>

      <!-- 右 -->
      <div class="right-panel">

        <div class="glass-card">
          <h3>机器人分组状态</h3>

          <div class="group-item" v-for="g in groups" :key="g.name">
            <div class="group-header">
              <span>{{ g.name }}</span>
              <span>{{ g.percent }}%</span>
            </div>
            <el-progress :percentage="g.percent" :show-text="false"/>
          </div>
        </div>

        <div class="glass-card">
          <h3>最近上线机器人</h3>

          <div class="recent-item" v-for="r in recentOnlineRobots" :key="r.id">
            <span>{{ r.name }}</span>
            <span>{{ r.time }}</span>
          </div>
        </div>

        <div class="glass-card">
          <h3>机器人位置</h3>
          <div id="robotMapChart" class="chart-box"></div>
        </div>

      </div>

    </div>

    <!-- ===== 新增图表区域：词云图 + 趋势图 ===== -->
    <div class="extra-charts">
      <div class="glass-card chart-card">
        <h3>用户反馈词云图</h3>
        <div id="wordCloudChart" class="chart-box"></div>
      </div>
      <div class="glass-card chart-card">
        <h3>每日异常趋势图</h3>
        <div id="dailyExceptionChart" class="chart-box"></div>
      </div>
    </div>

  </div>
</template>

<script>
import * as echarts from "echarts"
import "echarts-wordcloud" // 引入词云图扩展
import chinaJson from "@/assets/map/china.json"

export default {
  data() {
    return {
      dashboardTimeRange: "today",
      robotStatusChart: null,
      robotMapChart: null,
      wordCloudChart: null,
      dailyExceptionChart: null,

      robotStats: {
        total: 120,
        online: 80,
        busy: 25,
        offline: 15,
        byGroup: {
          customerService: 20,
          operations: 30,
          quality: 15
        },
        totalByGroup: {
          customerService: 30,
          operations: 40,
          quality: 20
        }
      },

      exceptionStats: {today: 5},
      feedbackStats: {today: 12},

      taskStats: {
        executing: 2,
        pending: 3,
        completed: 50
      },

      executingTasks: [
        {id: 1, name: "巡检任务A", description: "设备巡检", progress: 60},
        {id: 2, name: "清洁任务B", description: "区域清洁", progress: 30}
      ],

      pendingTasks: [
        {id: 3, name: "配送任务C", scheduledTime: "10:30"},
        {id: 4, name: "盘点任务D", scheduledTime: "11:00"}
      ],

      recentOnlineRobots: [
        {id: 1, name: "机器人001", time: "09:15"},
        {id: 2, name: "机器人002", time: "09:20"}
      ],

      // 词云图数据（用户反馈关键词及权重）
      wordCloudData: [
        { name: "定位精准", value: 85 },
        { name: "响应速度快", value: 72 },
        { name: "导航异常", value: 34 },
        { name: "避障灵敏", value: 68 },
        { name: "续航不足", value: 42 },
        { name: "操作便捷", value: 79 },
        { name: "语音识别差", value: 28 },
        { name: "交互友好", value: 63 },
        { name: "充电故障", value: 19 },
        { name: "地图更新慢", value: 37 },
        { name: "任务执行成功", value: 91 },
        { name: "噪音较大", value: 23 },
        { name: "调度高效", value: 74 },
        { name: "硬件耐用", value: 56 },
        { name: "软件卡顿", value: 31 }
      ],

      // 每日异常趋势数据（最近7天）
      dailyExceptionDates: [],
      dailyExceptionValues: []
    }
  },

  computed: {
    // 计算分组数据用于展示
    groups() {
      const groups = [
        { name: '客服组', key: 'customerService', icon: 'el-icon-service', color: '#409EFF' },
        { name: '运维组', key: 'operations', icon: 'el-icon-setting', color: '#67C23A' },
        { name: '质检组', key: 'quality', icon: 'el-icon-check', color: '#E6A23C' }
      ]
      return groups.map(g => ({
        ...g,
        online: this.robotStats.byGroup[g.key],
        total: this.robotStats.totalByGroup[g.key],
        percent: Math.round((this.robotStats.byGroup[g.key] / this.robotStats.totalByGroup[g.key]) * 100)
      }))
    },
    metricList() {
      return [
        { label: '总机器人', value: this.robotStats.total },
        { label: '在线', value: this.robotStats.online },
        { label: '今日异常', value: this.exceptionStats.today },
        { label: '今日反馈', value: this.feedbackStats.today },
        { label: '完成任务', value: this.taskStats.completed }
      ]
    }
  },

  mounted() {
    echarts.registerMap("china", chinaJson)
    this.initChartData()   // 初始化趋势图日期和基础数据
    this.initCharts()
    window.addEventListener("resize", this.resizeCharts)
  },

  beforeDestroy() {
    window.removeEventListener("resize", this.resizeCharts)
    // 销毁图表实例，避免内存泄漏
    this.disposeCharts()
  },

  methods: {
    // 初始化图表基础数据（生成最近7天日期和模拟异常数据）
    initChartData() {
      const dates = []
      const today = new Date()
      for (let i = 6; i >= 0; i--) {
        const d = new Date(today)
        d.setDate(today.getDate() - i)
        dates.push(`${d.getMonth()+1}/${d.getDate()}`)
      }
      this.dailyExceptionDates = dates
      // 生成模拟的异常趋势数据
      this.generateRandomExceptionData()
    },

    // 生成随机异常数据（模拟不同日期的异常数量）
    generateRandomExceptionData() {
      // 基础数据范围：3~18之间，体现一定波动
      const baseValues = [8, 12, 5, 9, 15, 7, 10]
      // 根据当前时间范围略微调整（今日、本周等影响展示风格，但这里保持数据动态）
      let factor = 1.0
      if (this.dashboardTimeRange === 'today') factor = 0.8
      if (this.dashboardTimeRange === 'week') factor = 1.0
      if (this.dashboardTimeRange === 'month') factor = 1.3
      if (this.dashboardTimeRange === 'year') factor = 1.6

      this.dailyExceptionValues = baseValues.map(v => Math.round(v * factor + Math.random() * 3))
      // 确保今日异常数据与卡片显示同步
      if (this.dashboardTimeRange === 'today') {
        this.exceptionStats.today = this.dailyExceptionValues[this.dailyExceptionValues.length - 1]
      } else {
        // 其他范围保持原有今日异常展示最后一天的数据
        this.exceptionStats.today = this.dailyExceptionValues[this.dailyExceptionValues.length - 1]
      }
    },

    // 刷新词云图数据（可随机变化一些权重，模拟动态反馈）
    refreshWordCloudData() {
      // 动态微调词云数据，让刷新时有变化感
      this.wordCloudData = this.wordCloudData.map(item => ({
        ...item,
        value: Math.max(10, Math.min(100, item.value + (Math.random() > 0.6 ? Math.floor(Math.random() * 8) - 4 : 0)))
      }))
    },

    initCharts() {
      this.initStatusChart()
      this.initMapChart()
      this.initWordCloudChart()
      this.initDailyExceptionChart()
    },

    resizeCharts() {
      this.robotStatusChart && this.robotStatusChart.resize()
      this.robotMapChart && this.robotMapChart.resize()
      this.wordCloudChart && this.wordCloudChart.resize()
      this.dailyExceptionChart && this.dailyExceptionChart.resize()
    },

    disposeCharts() {
      if (this.robotStatusChart) this.robotStatusChart.dispose()
      if (this.robotMapChart) this.robotMapChart.dispose()
      if (this.wordCloudChart) this.wordCloudChart.dispose()
      if (this.dailyExceptionChart) this.dailyExceptionChart.dispose()
      this.robotStatusChart = null
      this.robotMapChart = null
      this.wordCloudChart = null
      this.dailyExceptionChart = null
    },

    refreshDashboard() {
      // 刷新时更新模拟数据，让图表动态变化
      this.generateRandomExceptionData()
      this.refreshWordCloudData()
      // 更新所有图表显示
      this.updateAllCharts()
    },

    // 时间范围切换时，更新趋势数据等
    onTimeRangeChange() {
      this.generateRandomExceptionData()
      this.updateAllCharts()
    },

    updateAllCharts() {
      // 更新机器人状态饼图
      if (this.robotStatusChart) {
        this.robotStatusChart.setOption({
          series: [{
            data: [
              {value: this.robotStats.online, name: "在线"},
              {value: this.robotStats.busy, name: "忙碌"},
              {value: this.robotStats.offline, name: "离线"}
            ]
          }]
        })
      }
      // 更新词云图
      if (this.wordCloudChart) {
        this.wordCloudChart.setOption({
          series: [{
            data: this.wordCloudData
          }]
        })
      }
      // 更新异常趋势图
      if (this.dailyExceptionChart) {
        this.dailyExceptionChart.setOption({
          xAxis: { data: this.dailyExceptionDates },
          series: [{ data: this.dailyExceptionValues }]
        })
      }
      // 地图无需更新数据，保持原样
    },

    initStatusChart() {
      if (this.robotStatusChart) {
        this.robotStatusChart.dispose()
      }
      this.robotStatusChart = echarts.init(document.getElementById("robotStatusChart"))
      this.robotStatusChart.setOption({
        tooltip: {trigger: "item"},
        legend: {bottom: 0},
        series: [{
          type: "pie",
          radius: ["45%", "70%"],
          data: [
            {value: this.robotStats.online, name: "在线"},
            {value: this.robotStats.busy, name: "忙碌"},
            {value: this.robotStats.offline, name: "离线"}
          ],
          itemStyle: {
            borderRadius: 8,
            borderColor: '#fff',
            borderWidth: 2
          },
          label: { show: true, formatter: '{b}: {d}%' }
        }]
      })
    },

    initMapChart() {
      if (this.robotMapChart) {
        this.robotMapChart.dispose()
      }
      this.robotMapChart = echarts.init(document.getElementById("robotMapChart"))
      this.robotMapChart.setOption({
        geo: {
          map: "china",
          roam: true,
          zoom: 1.2,
          itemStyle: {
            borderColor: '#ccc',
            areaColor: '#f5f5f5'
          }
        },
        series: [{
          type: "effectScatter",
          coordinateSystem: "geo",
          rippleEffect: {brushType: "stroke", scale: 4},
          symbolSize: 12,
          data: [
            {name: "北京", value: [116.4, 39.9, 5]},
            {name: "上海", value: [121.47, 31.23, 8]},
            {name: "广州", value: [113.23, 23.16, 3]},
            {name: "深圳", value: [114.05, 22.53, 6]},
            {name: "成都", value: [104.06, 30.67, 4]}
          ],
          label: { show: true, formatter: '{b}', position: 'right', offset: [8, 0] }
        }]
      })
    },

    // 词云图初始化
    initWordCloudChart() {
      if (this.wordCloudChart) {
        this.wordCloudChart.dispose()
      }
      const container = document.getElementById("wordCloudChart")
      if (!container) return
      this.wordCloudChart = echarts.init(container)
      this.wordCloudChart.setOption({
        tooltip: { show: true, formatter: (params) => `${params.name}: ${params.value}` },
        series: [{
          type: 'wordCloud',
          shape: 'pentagon',
          left: 'center',
          top: 'center',
          width: '100%',
          height: '100%',
          right: null,
          bottom: null,
          sizeRange: [14, 42],
          rotationRange: [-45, 90],
          rotationStep: 45,
          gridSize: 8,
          drawOutOfBound: false,
          textStyle: {
            fontFamily: 'sans-serif',
            fontWeight: 'normal',
            color: function () {
              // 随机颜色，美观
              const colors = ['#409EFF', '#67C23A', '#E6A23C', '#F56C6C', '#909399', '#8E44AD', '#2C3E50']
              return colors[Math.floor(Math.random() * colors.length)]
            }
          },
          emphasis: {
            textStyle: {
              fontWeight: 'bold',
              shadowBlur: 10,
              shadowColor: '#333'
            }
          },
          data: this.wordCloudData
        }]
      })
    },

    // 每日异常趋势图初始化
    initDailyExceptionChart() {
      if (this.dailyExceptionChart) {
        this.dailyExceptionChart.dispose()
      }
      const container = document.getElementById("dailyExceptionChart")
      if (!container) return
      this.dailyExceptionChart = echarts.init(container)
      this.dailyExceptionChart.setOption({
        tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
        grid: { top: 30, left: 40, right: 20, bottom: 20, containLabel: true },
        xAxis: {
          type: 'category',
          data: this.dailyExceptionDates,
          axisLabel: { rotate: 30, fontSize: 11 }
        },
        yAxis: {
          type: 'value',
          name: '异常次数',
          nameStyle: { fontSize: 12 }
        },
        series: [{
          type: 'line',
          data: this.dailyExceptionValues,
          smooth: true,
          symbol: 'circle',
          symbolSize: 8,
          lineStyle: { width: 3, color: '#F56C6C', shadowBlur: 8 },
          areaStyle: {
            color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
              { offset: 0, color: 'rgba(245,108,108,0.4)' },
              { offset: 1, color: 'rgba(245,108,108,0.05)' }
            ])
          },
          itemStyle: { color: '#F56C6C', borderColor: '#fff', borderWidth: 2 },
          label: { show: true, position: 'top', fontSize: 11 }
        }]
      })
    }
  }
}
</script>

<style scoped>
.data-dashboard {
  padding: 0 24px;
}

/* ===== 顶部玻璃条 ===== */
.glass-bar {
  display: flex;
  gap: 12px;
  margin-bottom: 20px;
  border-radius: 12px;
  backdrop-filter: blur(12px);
}

/* ===== 关键指标 ===== */
.key-metrics {
  display: flex;
  gap: 16px;
  margin-bottom: 20px;
}

.metric-card {
  flex: 1;
  text-align: center;
  padding: 20px;
}

.metric-value {
  font-size: 26px;
  font-weight: bold;
  color: #3976E4;
}

.metric-label {
  font-size: 13px;
  color: #666;
}

/* ===== 新增图表行：两列布局 ===== */
.extra-charts {
  display: flex;
  gap: 20px;
  margin-top: 20px;
}

.chart-card {
  flex: 1;
}

.chart-card h3 {
  margin-bottom: 16px;
  font-size: 15px;
  font-weight: 600;
  color: #333;
}

/* ===== 主体布局 ===== */
.dashboard-main {
  display: flex;
  gap: 20px;
}

.left-panel {
  flex: 2;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.right-panel {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

/* ===== 毛玻璃卡片 ===== */
.glass-card {
  border-radius: 16px;
  padding: 20px;
  backdrop-filter: blur(14px);
  background: rgba(255,255,255,0.92);
  box-shadow: 0 8px 30px rgba(0,0,0,0.08);
  border: 1px solid rgba(255,255,255,0.4);
  transition: all 0.2s;
}

.glass-card h3 {
  margin-bottom: 16px;
  font-size: 15px;
  font-weight: 600;
  color: #333;
}

/* ===== 图表 ===== */
.chart-box {
  height: 260px;
  width: 100%;
}

/* ===== 任务 ===== */
.task-section {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.task-category {
  padding: 12px;
  border-radius: 10px;
  background: rgba(255,255,255,0.5);
}

.category-title {
  font-size: 14px;
  margin-bottom: 10px;
  color: #333;
}

.task-item {
  padding: 10px;
  border-radius: 8px;
  background: rgba(255,255,255,0.8);
  margin-bottom: 10px;
}

.task-item.pending {
  background: rgba(240,240,240,0.6);
}

.task-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 6px;
  font-size: 13px;
}

/* ===== 分组 ===== */
.group-item {
  margin-bottom: 14px;
}

.group-header {
  display: flex;
  justify-content: space-between;
  font-size: 13px;
  margin-bottom: 6px;
}

/* ===== 最近上线 ===== */
.recent-item {
  display: flex;
  justify-content: space-between;
  padding: 10px;
  border-radius: 8px;
  background: rgba(255,255,255,0.6);
  margin-bottom: 8px;
}

/* 响应式适配 */
@media (max-width: 1200px) {
  .extra-charts {
    flex-direction: column;
  }
  .dashboard-main {
    flex-direction: column;
  }
}
</style>
