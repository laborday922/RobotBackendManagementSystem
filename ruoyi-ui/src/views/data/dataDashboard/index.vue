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

      <el-button size="small" @click="refreshDashboard" :loading="refreshBtnLoading">
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
      <!-- 左面板 -->
      <div class="left-panel">
        <div class="glass-card">
          <h3>在线机器人情况</h3>
          <div id="robotStatusChart" class="chart-box"></div>
        </div>

        <div class="glass-card">
          <h3>任务执行情况</h3>
          <div class="task-section">
            <div class="task-category">
              <div class="category-title">⏳ 执行中 ({{ taskStats.executing }})</div>
              <div class="task-item" v-for="item in executingTasks" :key="item.id">
                <div class="task-header">
                  <span>{{ item.name }}</span>
                  <span>{{ item.progress }}%</span>
                </div>
                <el-progress :percentage="item.progress" :show-text="false"/>
              </div>
            </div>
            <div class="task-category">
              <div class="category-title">⏰ 待执行 ({{ taskStats.pending }})</div>
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

      <!-- 右面板 -->
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

    <!-- ===== 词云图 + 异常趋势图 ===== -->
    <div class="extra-charts">
      <!-- 词云图卡片：独立 loading 遮罩 -->
      <div class="glass-card chart-card" v-loading="wordCloudLoading" element-loading-text="加载词云中...">
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
import "echarts-wordcloud"
import chinaJson from "@/assets/map/china.json"
import {
  getAnomalyTrend,
  getFeedbackWordCloud,
  getRobotGeo,
  getRobotGroups,
  getServiceOverview,
  getTaskExecutions
} from "@/api/data/dataDashboard/dashboard"

export default {
  data() {
    return {
      // 移除全局 loading，新增词云图专用 loading 和刷新按钮 loading
      wordCloudLoading: false,
      refreshBtnLoading: false,
      dashboardTimeRange: "week", // today/week/month/year

      // 图表实例
      robotStatusChart: null,
      robotMapChart: null,
      wordCloudChart: null,
      dailyExceptionChart: null,

      // 数据存储
      robotStats: {
        total: 0,
        online: 0,
        busy: 0,
        offline: 0,
        byGroup: {},
        totalByGroup: {}
      },
      exceptionStats: { today: 0 },
      feedbackStats: { today: 0 },
      taskStats: {
        executing: 0,
        pending: 0,
        completed: 0
      },
      executingTasks: [],
      pendingTasks: [],
      groups: [],
      recentOnlineRobots: [],
      robotGeoList: [],
      wordCloudData: [],
      anomalyTrend: {
        dates: [],
        values: []
      }
    }
  },

  computed: {
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
    this.initCharts()
    window.addEventListener("resize", this.resizeCharts)
    this.loadAllData()
  },

  beforeDestroy() {
    window.removeEventListener("resize", this.resizeCharts)
    this.disposeCharts()
  },

  methods: {
    // ========== 数据加载 ==========
    async loadAllData() {
      try {
        await Promise.all([
          this.loadServiceOverview(),
          this.loadRobotGroups(),
          this.loadTaskExecutions(),
          this.loadFeedbackWordCloud(),
          this.loadRobotGeo(),
          this.loadAnomalyTrend()
        ])
        this.updateAllCharts()
      } catch (error) {
        console.error('加载数据失败:', error)
        this.$message.error('数据加载失败')
      }
    },

    async loadServiceOverview() {
      const res = await getServiceOverview()
      if (res.code === 200) {
        const data = res.data
        this.robotStats.total = data.totalCount
        this.robotStats.online = data.onlineCount

        const distribution = data.statusDistribution || []
        distribution.forEach(item => {
          if (item.name === '离线') {
            this.robotStats.offline = item.value
          } else if (item.name === '忙碌') {
            this.robotStats.busy = item.value
          }
        })
        this.feedbackStats.today = data.todayFeedbacks || 0
        this.taskStats.completed = data.completedTasks || 0
      }
    },

    async loadRobotGroups() {
      const res = await getRobotGroups()
      if (res.code === 200) {
        this.groups = res.data.map(g => ({
          name: g.name,
          online: g.onlineCount,
          total: g.totalCount,
          percent: g.totalCount === 0 ? 0 : Math.round((g.onlineCount / g.totalCount) * 100)
        }))
        this.robotStats.byGroup = {}
        this.robotStats.totalByGroup = {}
        res.data.forEach(g => {
          this.robotStats.byGroup[g.groupName] = g.onlineCount
          this.robotStats.totalByGroup[g.groupName] = g.totalCount
        })
      }
    },

    async loadTaskExecutions() {
      const res = await getTaskExecutions({
        start_time: null,
        end_time: null,
        limit: 20,
        offset: 0
      })
      if (res.code === 200) {
        const data = res.data
        this.executingTasks = data.executingTasks || []
        this.pendingTasks = (data.pendingTasks || []).map(task => ({
          ...task,
          scheduledTime: this.formatDateTime(task.scheduledTime)
        }))
        this.taskStats.executing = this.executingTasks.length
        this.taskStats.pending = this.pendingTasks.length
        if (data.completedCount !== undefined) this.taskStats.completed = data.completedCount
      }
    },

    // 词云图加载（独立 loading 控制）
    async loadFeedbackWordCloud() {
      this.wordCloudLoading = true
      try {
        const {start_time, end_time} = this.getTimeRangeParams()
        const res = await getFeedbackWordCloud({
          start_time: start_time,
          end_time: end_time,
          feedback_type: null
        })
        if (res.code === 200) {
          this.wordCloudData = res.data.map(item => ({
            name: item.name,
            value: item.value
          }))
          if (this.wordCloudChart) {
            this.wordCloudChart.setOption({
              series: [{data: this.wordCloudData}]
            })
          }
        }
      } catch (error) {
        console.error('词云图加载失败:', error)
        this.$message.error('词云图加载失败')
      } finally {
        this.wordCloudLoading = false
      }
    },

    async loadRobotGeo() {
      const res = await getRobotGeo()
      if (res.code === 200) {
        this.robotGeoList = res.data
        this.updateMapChart()
      }
    },

    async loadAnomalyTrend() {
      const now = new Date()
      const endDate = this.formatDate(now)
      const startDate = new Date(now)
      startDate.setDate(now.getDate() - 6)

      const time_range = `${this.formatDate(startDate)},${endDate}`
      const granularity = 'day'

      const res = await getAnomalyTrend({granularity, time_range})

      if (res.code === 200) {
        this.anomalyTrend.dates = res.data.xaxis || []
        this.anomalyTrend.values = res.data.series || []

        console.log("异常趋势数据：", this.anomalyTrend)

        this.dailyExceptionChart.setOption({
          xAxis: {
            type: 'category',
            data: this.anomalyTrend.dates
          },
          yAxis: {
            type: 'value',
            name: '异常次数'
          },
          series: [{
            type: 'line',
            data: this.anomalyTrend.values,
            smooth: true,
            areaStyle: {}
          }]
        }, true)
      }
    },

    initDailyExceptionChart() {
      this.dailyExceptionChart = echarts.init(document.getElementById("dailyExceptionChart"))
      this.dailyExceptionChart.setOption({
        tooltip: {trigger: 'axis'},
        xAxis: {type: 'category', data: []},
        yAxis: {type: 'value', name: '异常次数'},
        series: [{type: 'line', data: [], smooth: true, areaStyle: {}}]
      })
    },

    // ========== 时间参数辅助方法 ==========
    getTimeRangeParams() {
      const now = new Date()
      let start_time = null, end_time = null
      switch (this.dashboardTimeRange) {
        case 'today':
          start_time = new Date(now.setHours(0, 0, 0, 0))
          end_time = new Date()
          break
        case 'week':
          const weekStart = new Date(now)
          weekStart.setDate(now.getDate() - now.getDay())
          weekStart.setHours(0, 0, 0, 0)
          start_time = weekStart
          end_time = new Date()
          break
        case 'month':
          const monthStart = new Date(now.getFullYear(), now.getMonth(), 1)
          start_time = monthStart
          end_time = new Date()
          break
        case 'year':
          const yearStart = new Date(now.getFullYear(), 0, 1)
          start_time = yearStart
          end_time = new Date()
          break
      }
      return {start_time, end_time}
    },

    getTrendParams() {
      let granularity = 'day'
      let time_range = ''
      const now = new Date()
      switch (this.dashboardTimeRange) {
        case 'today':
          time_range = `${this.formatDate(now)},${this.formatDate(now)}`
          break
        case 'week':
          const weekStart = new Date(now)
          weekStart.setDate(now.getDate() - now.getDay())
          time_range = `${this.formatDate(weekStart)},${this.formatDate(now)}`
          break
        case 'month':
          const monthStart = new Date(now.getFullYear(), now.getMonth(), 1)
          time_range = `${this.formatDate(monthStart)},${this.formatDate(now)}`
          break
        case 'year':
          const yearStart = new Date(now.getFullYear(), 0, 1)
          time_range = `${this.formatDate(yearStart)},${this.formatDate(now)}`
          break
      }
      return {granularity, time_range}
    },

    formatDate(date) {
      const y = date.getFullYear()
      const m = String(date.getMonth() + 1).padStart(2, '0')
      const d = String(date.getDate()).padStart(2, '0')
      return `${y}-${m}-${d}`
    },

    formatDateTime(isoString) {
      if (!isoString) return ''
      const date = new Date(isoString)
      const month = String(date.getMonth() + 1).padStart(2, '0')
      const day = String(date.getDate()).padStart(2, '0')
      const hours = String(date.getHours()).padStart(2, '0')
      const minutes = String(date.getMinutes()).padStart(2, '0')
      return `${month}-${day} ${hours}:${minutes}`
    },

    // ========== 图表初始化与更新 ==========
    initCharts() {
      this.initStatusChart()
      this.initWordCloudChart()
      this.initDailyExceptionChart()
      this.initMapChart()
    },

    initStatusChart() {
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
          ]
        }]
      })
    },

    initWordCloudChart() {
      this.wordCloudChart = echarts.init(document.getElementById("wordCloudChart"))
      this.wordCloudChart.setOption({
        tooltip: {show: true, formatter: (params) => `${params.name}: ${params.value}`},
        series: [{
          type: 'wordCloud',
          shape: 'circle',
          width: '100%',
          height: '100%',
          sizeRange: [14, 42],
          rotationRange: [-45, 90],
          textStyle: {
            fontFamily: 'sans-serif',
            fontWeight: 'normal',
            color: function () {
              const colors = ['#409EFF', '#67C23A', '#E6A23C', '#F56C6C', '#909399', '#8E44AD', '#2C3E50', '#1abc9c', '#e67e22', '#3498db'];
              return colors[Math.floor(Math.random() * colors.length)];
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

    initMapChart() {
      this.robotMapChart = echarts.init(document.getElementById("robotMapChart"))
      this.updateMapChart()
    },

    updateMapChart() {
      if (!this.robotMapChart) return
      const scatterData = this.robotGeoList.map(robot => {
        const locationName = robot.locationArea || robot.specificLocation || '未知位置'
        const displayName = `${locationName} (${robot.robotId})`
        return {
          name: displayName,
          value: [robot.coordinateX, robot.coordinateY, 1]
        }
      })
      this.robotMapChart.setOption({
        geo: {map: "china", roam: true},
        series: [{
          type: "effectScatter",
          coordinateSystem: "geo",
          rippleEffect: {brushType: "stroke"},
          symbolSize: 12,
          data: scatterData,
          label: {
            show: true,
            formatter: '{b}',
            position: 'right',
            offset: [5, 0]
          }
        }]
      })
    },

    updateAllCharts() {
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
    },

    resizeCharts() {
      this.robotStatusChart?.resize()
      this.robotMapChart?.resize()
      this.wordCloudChart?.resize()
      this.dailyExceptionChart?.resize()
    },

    disposeCharts() {
      this.robotStatusChart?.dispose()
      this.robotMapChart?.dispose()
      this.wordCloudChart?.dispose()
      this.dailyExceptionChart?.dispose()
    },

    onTimeRangeChange() {
      this.loadFeedbackWordCloud()
      this.loadServiceOverview()
    },

    async refreshDashboard() {
      this.refreshBtnLoading = true
      try {
        await this.loadAllData()
      } catch (error) {
        console.error('刷新失败:', error)
        this.$message.error('刷新失败')
      } finally {
        this.refreshBtnLoading = false
      }
    }
  }
}
</script>

<style scoped>
.data-dashboard {
  padding: 0 24px;
  margin-bottom: 20px;
}

/* ===== 顶部玻璃条 ===== */
.glass-bar {
  display: flex;
  gap: 12px;
  margin-top: 20px;
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
  flex-wrap: wrap; /* 允许换行，避免宽度溢出 */
  width: 100%;
  margin-top: 20px;
}

.chart-card {
  flex: 1 1 400px; /* 基础宽度 400px，允许拉伸和收缩 */
  min-width: 280px; /* 最小宽度，防止过小 */
  box-sizing: border-box;
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
  background: rgba(255, 255, 255, 0.92);
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.08);
  border: 1px solid rgba(255, 255, 255, 0.4);
  transition: all 0.2s;
}

.glass-card h3 {
  margin-bottom: 16px;
  font-size: 15px;
  font-weight: 600;
  color: #333;
}

/* ===== 图表 ===== */
/* 确保图表容器不溢出 */
.chart-box {
  width: 100%;
  max-width: 100%;
  height: 260px;
  overflow: hidden;
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
  background: rgba(255, 255, 255, 0.5);
}

.category-title {
  font-size: 14px;
  margin-bottom: 10px;
  color: #333;
}

.task-item {
  padding: 10px;
  border-radius: 8px;
  background: rgba(255, 255, 255, 0.8);
  margin-bottom: 10px;
}

.task-item.pending {
  background: rgba(240, 240, 240, 0.6);
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
  background: rgba(255, 255, 255, 0.6);
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
