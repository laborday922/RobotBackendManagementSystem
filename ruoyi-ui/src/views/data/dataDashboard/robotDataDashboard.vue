<template>
  <div class="screen-container">
    <!-- Header -->
    <header class="screen-header">
      <div class="header-left">数据来源：系统实时数据</div>
      <h1>机器人运行监控大屏</h1>
      <div class="header-right">
        <span>{{ currentDate }}</span>
        <el-button
          type="primary"
          size="small"
          icon="el-icon-full-screen"
          @click="openFullScreen"
          style="margin-left: 12px"
        >
          全屏
        </el-button>
      </div>
    </header>

    <!-- Body -->
    <div class="screen-body">
      <!-- 第一行 -->
      <div class="row">
        <!-- 左：机器人状态饼图 -->
        <div class="cell">
          <div class="panel">
            <div class="panel-title">机器人状态</div>
            <div id="robotStatusChart" class="chart"></div>
          </div>
        </div>

        <!-- 中：核心指标 + 分组条形图 -->
        <div class="cell center">
          <div class="panel split-panel">
            <div class="panel-section">
              <div class="panel-title">核心指标</div>
              <div class="metric-grid">
                <div
                  class="metric"
                  v-for="m in metricList"
                  :key="m.label"
                  :class="{ clickable: m.clickable }"
                  @click="handleMetricClick(m)"
                >
                  <div class="value">{{ m.value }}</div>
                  <div class="label">{{ m.label }}</div>
                </div>
              </div>
            </div>
            <div class="panel-section">
              <div class="panel-title">机器人分组在线情况</div>
              <div id="robotGroupBarChart" class="chart"></div>
            </div>
          </div>
        </div>

        <!-- 右：机器人分布地图 -->
        <div class="cell">
          <div class="panel">
            <div class="panel-title">机器人分布</div>
            <div id="robotMapChart" class="chart"></div>
          </div>
        </div>
      </div>

      <!-- 第二行 -->
      <div class="row">
        <div class="cell large">
          <div class="panel">
            <div class="panel-title">异常趋势</div>
            <div id="dailyExceptionChart" class="chart"></div>
          </div>
        </div>

        <div class="cell task-cell">
          <div class="panel">
            <div class="panel-title">任务执行情况</div>
            <div class="task-section">
              <div class="task-category">
                <div class="category-title">⏳ 执行中 ({{ taskStats.executing }})</div>
                <div class="task-list">
                  <div class="task-item" v-for="item in executingTasks" :key="item.id">
                    <div class="task-header">
                      <span>{{ item.name }}</span>
                      <span>{{ item.progress }}%</span>
                    </div>
                    <div class="progress-bar">
                      <div class="progress-fill" :style="{ width: item.progress + '%' }"></div>
                    </div>
                  </div>
                  <div v-if="executingTasks.length === 0" class="empty-tip">暂无执行中任务</div>
                </div>
              </div>
              <div class="task-category">
                <div class="category-title">⏰ 待执行 ({{ taskStats.pending }})</div>
                <div class="task-list">
                  <div class="task-item pending" v-for="item in pendingTasks" :key="item.id">
                    <div class="task-header">
                      <span>{{ item.name }}</span>
                      <span>{{ item.scheduledTime }}</span>
                    </div>
                  </div>
                  <div v-if="pendingTasks.length === 0" class="empty-tip">暂无待执行任务</div>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 词云图卡片，带独立 loading -->
        <div class="cell">
          <div class="panel" v-loading="wordCloudLoading" element-loading-text="词云图加载中，请稍后...">
            <div class="panel-title">词云图</div>
            <div id="wordCloudChart" class="chart"></div>
          </div>
        </div>
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
      currentDate: "",
      wordCloudLoading: false,

      robotStatusChart: null,
      robotGroupBarChart: null,
      robotMapChart: null,
      wordCloudChart: null,
      dailyExceptionChart: null,

      robotStats: { total: 0, online: 0, busy: 0, offline: 0 },
      metricList: [],
      robotGeoList: [],
      clusteredMapPoints: [],  // 聚合后的地图点位数据
      wordCloudData: [],
      anomalyTrend: { dates: [], values: [] },
      groups: [],

      taskStats: { executing: 0, pending: 0, completed: 0 },
      executingTasks: [],
      pendingTasks: []
    }
  },

  mounted() {
    echarts.registerMap("china", chinaJson)
    this.updateTime()
    setInterval(this.updateTime, 1000)

    this.initCharts()
    this.loadAllDataExceptWordCloud()
    this.loadWordCloudAsync()

    window.addEventListener("resize", this.resizeCharts)
  },

  beforeDestroy() {
    window.removeEventListener("resize", this.resizeCharts)
  },

  methods: {
    updateTime() {
      this.currentDate = new Date().toLocaleString()
    },

    async loadAllDataExceptWordCloud() {
      await Promise.all([
        this.loadServiceOverview(),
        this.loadRobotGroups(),
        this.loadRobotGeo(),
        this.loadTrend(),
        this.loadTaskExecutions()
      ])
      this.updateNonWordCloudCharts()
    },

    async loadWordCloudAsync() {
      this.wordCloudLoading = true
      try {
        await this.loadWordCloud()
        this.updateWordCloudChart()
      } catch (error) {
        console.error('词云图加载失败:', error)
        this.$message?.error('词云图加载失败')
      } finally {
        this.wordCloudLoading = false
      }
    },

    async loadServiceOverview() {
      const res = await getServiceOverview()
      if (res.code === 200) {
        const d = res.data
        this.robotStats.total = d.totalCount
        this.robotStats.online = d.onlineCount
        d.statusDistribution.forEach(s => {
          if (s.name === "离线") this.robotStats.offline = s.value
          if (s.name === "待激活") this.robotStats.busy = s.value
        })
        // 核心指标构建，新增“离线”和“今日异常数”
        this.metricList = [
          {label: "总机器人", value: d.totalCount, clickable: false},
          {label: "在线", value: d.onlineCount, clickable: false},
          {label: "离线", value: this.robotStats.offline, clickable: false},
          {label: "今日反馈", value: d.todayFeedbacks || 0, clickable: false},
          {label: "完成任务", value: d.completedTasks || 0, clickable: false},
          {
            label: "今日异常数",
            value:1,  // 请根据实际接口字段调整
            clickable: true,
            route: '/robots/warnings'
          }
        ]
      }
    },

    async loadRobotGroups() {
      const res = await getRobotGroups()
      if (res.code === 200) {
        this.groups = res.data.map(g => ({
          name: g.name,
          onlineCount: g.onlineCount,
          totalCount: g.totalCount
        }))
      }
    },

    async loadRobotGeo() {
      const res = await getRobotGeo()
      if (res.code === 200) {
        this.robotGeoList = res.data
        // 聚合地理位置数据
        this.clusteredMapPoints = this.aggregateGeoPoints(this.robotGeoList)
      }
    },

    /**
     * 聚合地理位置数据，将相同坐标的机器人合并
     * @param {Array} geoList 原始机器人地理数据 [{robotId, coordinateX, coordinateY}]
     * @returns {Array} 聚合后的数据 [{lng, lat, count, robotIds, coordinateKey}]
     */
    aggregateGeoPoints(geoList) {
      const locationMap = new Map()

      geoList.forEach(item => {
        // 保留小数点后6位精度，避免浮点数误差
        const lng = parseFloat(item.coordinateX)
        const lat = parseFloat(item.coordinateY)
        const key = `${lng.toFixed(6)},${lat.toFixed(6)}`

        if (locationMap.has(key)) {
          const existing = locationMap.get(key)
          existing.count++
          existing.robotIds.push(item.robotId)
        } else {
          locationMap.set(key, {
            lng: lng,
            lat: lat,
            count: 1,
            robotIds: [item.robotId],
            coordinateKey: key
          })
        }
      })

      return Array.from(locationMap.values())
    },

    async loadWordCloud() {
      const res = await getFeedbackWordCloud({})
      if (res.code === 200) {
        this.wordCloudData = res.data.map(i => ({name: i.name, value: i.value}))
      }
    },

    async loadTrend() {
      const now = new Date()
      const end = this.formatDate(now)
      const startDate = new Date(now)
      startDate.setDate(now.getDate() - 6)
      const start = this.formatDate(startDate)
      const res = await getAnomalyTrend({granularity: 'day', time_range: `${start},${end}`})
      if (res.code === 200) {
        this.anomalyTrend.dates = res.data.xaxis || []
        this.anomalyTrend.values = res.data.series || []
      }
    },

    async loadTaskExecutions() {
      try {
        const res = await getTaskExecutions({start_time: null, end_time: null, limit: 20, offset: 0})
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
      } catch (error) {
        console.error('加载任务列表失败:', error)
      }
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

    initCharts() {
      this.robotStatusChart = echarts.init(document.getElementById("robotStatusChart"))
      this.robotGroupBarChart = echarts.init(document.getElementById("robotGroupBarChart"))
      this.robotMapChart = echarts.init(document.getElementById("robotMapChart"))
      this.wordCloudChart = echarts.init(document.getElementById("wordCloudChart"))
      this.dailyExceptionChart = echarts.init(document.getElementById("dailyExceptionChart"))
    },

    // 更新除词云图以外的所有图表
    updateNonWordCloudCharts() {
      // 1. 饼图
      this.robotStatusChart.setOption({
        tooltip: {trigger: 'item', formatter: '{b}: {d}% ({c})'},
        textStyle: {color: "#b0c2f9"},
        series: [{
          type: "pie",
          radius: ["45%", "75%"],
          data: [
            {value: this.robotStats.online, name: "在线"},
            {value: this.robotStats.busy, name: "待激活"},
            {value: this.robotStats.offline, name: "离线"}
          ],
          label: {color: "#b0c2f9"},
          emphasis: {scale: true}
        }]
      })

      // 2. 分组条形图
      if (this.robotGroupBarChart && this.groups.length) {
        const groupNames = this.groups.map(g => g.name)
        const onlineCounts = this.groups.map(g => g.onlineCount)
        this.robotGroupBarChart.setOption({
          tooltip: {trigger: "axis", axisPointer: {type: "shadow"}, formatter: '{b}<br/>在线数量: {c}'},
          grid: {top: 20, bottom: 20, left: 100, right: 20, containLabel: true},
          xAxis: {
            type: "value",
            name: "在线数量",
            nameTextStyle: {color: "#b0c2f9"},
            axisLabel: {color: "#b0c2f9", formatter: (value) => Math.floor(value)},
            splitLine: {lineStyle: {color: "#2a3e6e"}}
          },
          yAxis: {
            type: "category",
            data: groupNames,
            axisLabel: {color: "#b0c2f9", fontSize: 11},
            axisLine: {show: false},
            axisTick: {show: false}
          },
          series: [{
            type: "bar",
            data: onlineCounts,
            itemStyle: {
              borderRadius: [0, 4, 4, 0],
              color: {
                type: 'linear',
                x: 0, y: 0, x2: 1, y2: 0,
                colorStops: [
                  {offset: 0, color: '#5faee3'},
                  {offset: 1, color: '#a8d8ff'}
                ]
              }
            },
            label: {
              show: true,
              position: "right",
              color: "#fff",
              formatter: (params) => Math.floor(params.value)
            }
          }]
        })
      }

      // 3. 地图
      // 3. 地图（使用聚合数据）
      const mapPoints = this.clusteredMapPoints.map(point => ({
        name: `位置(${point.lng.toFixed(2)}, ${point.lat.toFixed(2)})`,
        value: [point.lng, point.lat],
        robotCount: point.count,
        robotIds: point.robotIds
      }))

      this.robotMapChart.setOption({
        geo: {
          map: "china",
          roam: true,
          zoom: 1.2,
          center: [104.0, 35.0],
          label: { show: true, color: "#ffffff", fontSize: 10 },
          itemStyle: {
            normal: { areaColor: "#a8d8ff", borderColor: "#1f3a8a", borderWidth: 1 },
            emphasis: { areaColor: "#5faee3" }
          }
        },
        tooltip: {
          trigger: 'item',
          formatter: (params) => {
            if (params.data) {
              const data = params.data
              const robotCount = data.robotCount || 0
              const robotIds = data.robotIds || []
              const lng = data.value[0]
              const lat = data.value[1]

              // 构建显示内容
              let html = `<strong>位置信息</strong><br/>`
              html += `经度: ${lng.toFixed(4)}<br/>`
              html += `纬度: ${lat.toFixed(4)}<br/>`
              html += `机器人数量: ${robotCount}<br/>`

              if (robotCount > 0) {
                html += `<hr style="margin: 4px 0; border-color: #2a3e6e;">`
                html += `<strong>机器人ID列表</strong><br/>`
                // 最多显示前10个ID，避免tooltip过长
                const displayIds = robotIds.slice(0, 10)
                displayIds.forEach(id => {
                  html += `• ${id}<br/>`
                })
                if (robotIds.length > 10) {
                  html += `<span style="color: #aaa;">...还有 ${robotIds.length - 10} 个</span>`
                }
              }
              return html
            }
            return params.name
          },
          backgroundColor: 'rgba(0, 0, 0, 0.8)',
          borderColor: '#1f3a8a',
          borderWidth: 1,
          textStyle: {
            color: '#b0c2f9',
            fontSize: 12
          }
        },
        series: [{
          type: "effectScatter",
          coordinateSystem: "geo",
          data: mapPoints,
          // 点的大小根据机器人数量动态调整
          symbolSize: (val) => {
            const count = val.data ? val.data.robotCount : 1
            // 最小8px，最大32px，基数8 + 数量×1.5，限制最大32
            return Math.min(8 + count * 1.5, 32)
          },
          rippleEffect: {
            brushType: 'stroke',
            scale: 2.5,
            period: 4
          },
          label: {
            show: true,
            formatter: (params) => {
              const count = params.data.robotCount || 0
              return count > 1 ? `+${count}` : ''
            },
            position: 'top',
            color: '#ffcc00',
            fontSize: 10,
            fontWeight: 'bold',
            textShadowBlur: 2,
            textShadowColor: '#000'
          },
          itemStyle: {
            color: '#ff6666',
            shadowBlur: 8,
            shadowColor: '#ff6666'
          },
          emphasis: {
            scale: 1.2,
            label: {
              show: true,
              formatter: (params) => {
                const data = params.data
                return `${data.robotCount}个机器人`
              },
              position: 'top',
              fontWeight: 'bold'
            }
          }
        }]
      })

      // 4. 异常趋势折线图
      this.dailyExceptionChart.setOption({
        tooltip: {trigger: "axis", formatter: '{b}<br/>异常次数: {c}'},
        xAxis: {type: "category", data: this.anomalyTrend.dates, axisLabel: {color: "#b0c2f9"}},
        yAxis: {type: "value", axisLabel: {color: "#b0c2f9"}},
        series: [{type: "line", data: this.anomalyTrend.values, smooth: true, areaStyle: {opacity: 0.3}}]
      })
    },

    // 词云图完整配置
    updateWordCloudChart() {
      if (!this.wordCloudChart) return
      this.wordCloudChart.setOption({
        tooltip: {show: true, formatter: (params) => `${params.name}: ${params.value}`},
        series: [{
          type: "wordCloud",
          shape: "circle",
          width: "100%",
          height: "100%",
          sizeRange: [14, 42],
          rotationRange: [-45, 90],
          textStyle: {
            fontFamily: "sans-serif",
            fontWeight: "normal",
            color: () => {
              const colors = ["#409EFF", "#67C23A", "#E6A23C", "#F56C6C", "#909399",
                "#8E44AD", "#2C3E50", "#1abc9c", "#e67e22", "#3498db",
                "#9b59b6", "#f1c40f", "#e84393", "#00cec9"]
              return colors[Math.floor(Math.random() * colors.length)]
            }
          },
          emphasis: {textStyle: {fontWeight: "bold", shadowBlur: 10, shadowColor: "#333"}},
          data: this.wordCloudData
        }]
      }, true)
    },

    formatDate(date) {
      const y = date.getFullYear()
      const m = String(date.getMonth() + 1).padStart(2, '0')
      const d = String(date.getDate()).padStart(2, '0')
      return `${y}-${m}-${d}`
    },

    resizeCharts() {
      this.robotStatusChart?.resize()
      this.robotGroupBarChart?.resize()
      this.robotMapChart?.resize()
      this.wordCloudChart?.resize()
      this.dailyExceptionChart?.resize()
    },

    // 新增：全屏打开新标签页
    openFullScreen() {
      // 假设已配置全屏大屏路由 name 为 'RobotMonitorFullScreen'
      const routeData = this.$router.resolve({name: 'DashboardFullScreen'})
      window.open(routeData.href, '_blank')
    },

    // 新增：核心指标点击处理
    handleMetricClick(metric) {
      if (!metric.clickable) return
      if (metric.label === '今日异常数' && metric.route) {
        this.$router.push(metric.route)
      }
    }
  }
}
</script>

<style scoped>
.screen-container {
  height: 100vh;
  background: url('~@/assets/images/bg.png') no-repeat center/cover;
  color: #b0c2f9;
  overflow: hidden;
}

.row {
  display: flex;
}

.cell {
  flex: 1;
}

.screen-header {
  height: 70px;
  background: url('~@/assets/images/header.png') no-repeat center/cover;
  text-align: center;
  position: relative;
}

.screen-header h1 {
  color: #fff;
  line-height: 70px;
  margin: 0;
}

.header-left {
  position: absolute;
  left: 20px;
  top: 25px;
}

.header-right {
  position: absolute;
  right: 20px;
  top: 25px;
  display: flex;
  align-items: center;
}

.screen-body {
  height: calc(100% - 70px);
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.row {
  flex: 1;
  min-height: 0;
  display: flex;
}

.cell {
  flex: 1;
  min-height: 0;
  padding: 10px;
}

.cell.center {
  flex: 1.5;
}

.cell.large {
  flex: 2;
}

.cell.task-cell {
  flex: 1.5;
}

.panel {
  height: 100%;
  background: rgba(0, 0, 0, 0.4);
  border: 1px solid #1f3a8a;
  border-radius: 8px;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.split-panel {
  display: flex;
  flex-direction: column;
}

.panel-section {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  min-height: 0;
}

.panel-section:first-child {
  border-bottom: 1px solid #1f3a8a;
}

.panel-title {
  padding-left: 10px;
  height: 30px;
  line-height: 30px;
  font-size: 14px;
  background: rgba(0, 0, 0, 0.3);
  flex-shrink: 0;
}

.chart {
  flex: 1;
  min-height: 0;
}

.metric-grid {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  justify-content: space-around;
  padding: 10px;
  gap: 8px;            /* 可保持不变，或调整为 12px 等 */
}

.metric {
  width: 30%;          /* 改为三列布局 */
  text-align: center;
  border-radius: 6px;
  transition: background 0.2s;
}

.metric.clickable {
  cursor: pointer;
}

.metric.clickable:hover {
  background: rgba(64, 158, 255, 0.15);
}

.metric .value {
  font-size: 26px;
  color: #fff;
}

.metric .label {
  font-size: 12px;
}

.task-section {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 12px;
  overflow-y: auto;
  padding: 8px;
  min-height: 0;
}

.task-category {
  background: rgba(0, 0, 0, 0.3);
  border-radius: 6px;
  padding: 8px;
}

.category-title {
  font-size: 13px;
  margin-bottom: 8px;
  color: #9aa8d4;
}

.task-list {
  max-height: 180px;
  overflow-y: auto;
}

.task-item {
  padding: 6px 8px;
  border-radius: 4px;
  background: rgba(255, 255, 255, 0.05);
  margin-bottom: 6px;
}

.task-item.pending {
  background: rgba(255, 255, 255, 0.02);
}

.task-header {
  display: flex;
  justify-content: space-between;
  font-size: 12px;
  margin-bottom: 4px;
}

.progress-bar {
  height: 4px;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 2px;
  overflow: hidden;
}

.progress-fill {
  height: 100%;
  background: #67c23a;
  border-radius: 2px;
}

.empty-tip {
  text-align: center;
  color: #7f8c8d;
  font-size: 12px;
  padding: 12px;
}

.task-list::-webkit-scrollbar {
  width: 4px;
}

.task-list::-webkit-scrollbar-track {
  background: #2c3e50;
}

.task-list::-webkit-scrollbar-thumb {
  background: #1f3a8a;
}
</style>
