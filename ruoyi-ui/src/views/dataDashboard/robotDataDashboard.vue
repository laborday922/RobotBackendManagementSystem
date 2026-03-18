<template>
  <div class="data-dashboard">
    <!-- 顶部筛选保持不变 -->
    <div class="dashboard-header">
      <el-select v-model="dashboardTimeRange" size="small" style="width:120px">
        <el-option label="今日" value="today"/>
        <el-option label="本周" value="week"/>
        <el-option label="本月" value="month"/>
        <el-option label="本年" value="year"/>
      </el-select>
      <el-button size="small" @click="refreshDashboard">刷新</el-button>
    </div>

    <!-- ===== 关键指标 ===== -->
    <div class="key-metrics">
      <div class="metric-card">
        <div class="metric-value">{{ robotStats.total }}</div>
        <div class="metric-label">总机器人数量</div>
      </div>

      <div class="metric-card">
        <div class="metric-value">{{ robotStats.online }}</div>
        <div class="metric-label">在线机器人</div>
      </div>



      <div class="metric-card">
        <div class="metric-value">{{ exceptionStats.today }}</div>
        <div class="metric-label">今日异常</div>
      </div>

      <div class="metric-card">
        <div class="metric-value">{{ feedbackStats.today }}</div>
        <div class="metric-label">今日反馈</div>
      </div>

      <div class="metric-card">
        <div class="metric-value">{{ taskStats.completed }}</div>
        <div class="metric-label">完成任务</div>
      </div>
    </div>

    <!-- ===== 主体区域 ===== -->
    <div class="dashboard-main">
      <!-- 左侧 -->
      <div class="left-panel">
        <!-- 机器人状态图保持不变 -->
        <div class="chart-card">
          <h3>在线机器人情况</h3>
          <div id="robotStatusChart" class="chart-box"></div>
        </div>

        <!-- 任务执行情况 - 优化后 -->
        <div class="chart-card">
          <h3>任务执行情况</h3>
          <div class="task-section">
            <!-- 执行中卡片 -->
            <div class="task-category">
              <div class="category-title">
                <i class="el-icon-loading"></i> 执行中 ({{ taskStats.executing }})
              </div>
              <div class="task-list">
                <div class="task-item" v-for="item in executingTasks" :key="item.id">
                  <div class="task-header">
                    <span class="task-name">{{ item.name }}</span>
                    <span class="task-progress-label">{{ item.progress }}%</span>
                  </div>
                  <div class="task-desc">{{ item.description }}</div>
                  <el-progress :percentage="item.progress" :stroke-width="8" status="success" :show-text="false"/>
                </div>
              </div>
            </div>

            <!-- 待执行卡片 -->
            <div class="task-category">
              <div class="category-title">
                <i class="el-icon-time"></i> 待执行 ({{ taskStats.pending }})
              </div>
              <div class="task-list">
                <div class="task-item pending" v-for="item in pendingTasks" :key="item.id">
                  <div class="task-header">
                    <span class="task-name">{{ item.name }}</span>
                    <span class="task-time">{{ item.scheduledTime }}</span>
                  </div>
                  <div class="task-desc">计划时间</div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 右侧 -->
      <div class="right-panel">
        <!-- 机器人分组状态 - 优化后 -->
        <div class="chart-card">
          <h3>机器人分组状态</h3>
          <div class="group-list">
            <div class="group-item" v-for="(group, index) in groups" :key="index">
              <div class="group-info">
                <i :class="group.icon"></i>
                <span class="group-name">{{ group.name }}</span>
              </div>
              <div class="group-stats">
                <span class="stats-number">{{ group.online }}/{{ group.total }}</span>
                <span class="stats-percent">{{ group.percent }}%</span>
              </div>
              <el-progress :percentage="group.percent" :stroke-width="6" :color="group.color" :show-text="false"/>
            </div>
          </div>
        </div>

        <!-- 最近上线机器人 - 微调样式 -->
        <div class="chart-card">
          <h3>最近上线机器人</h3>
          <div class="recent-list">
            <div class="recent-item" v-for="robot in recentOnlineRobots" :key="robot.id">
              <span class="robot-name">{{ robot.name }}</span>
              <span class="robot-time">{{ robot.time }}</span>
              <span class="online-dot"></span>
            </div>
          </div>
        </div>

        <!-- 地图 -->
        <div class="chart-card">
          <h3>机器人当前位置</h3>
          <div id="robotMapChart" class="chart-box"></div>
        </div>

      </div>

    </div>
  </div>
</template>

<script>
import * as echarts from "echarts"
import chinaJson from "@/assets/map/china.json"

export default {
  data() {
    return {
      dashboardTimeRange: "today",
      robotStatusChart: null,
      robotMapChart: null,

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
      ]
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
    }
  },

  mounted() {
    echarts.registerMap("china", chinaJson)
    this.initCharts()
    window.addEventListener("resize", this.resizeCharts)
  },

  beforeDestroy() {
    window.removeEventListener("resize", this.resizeCharts)
  },

  methods: {
    initCharts() {
      this.initStatusChart()
      this.initMapChart()
    },

    resizeCharts() {
      this.robotStatusChart && this.robotStatusChart.resize()
      this.robotMapChart && this.robotMapChart.resize()
    },

    refreshDashboard() {
      this.initCharts()
    },

    initStatusChart() {
      this.robotStatusChart = echarts.init(
        document.getElementById("robotStatusChart")
      )

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

    initMapChart() {
      this.robotMapChart = echarts.init(
        document.getElementById("robotMapChart")
      )

      this.robotMapChart.setOption({
        geo: {
          map: "china",
          roam: true
        },
        series: [{
          type: "effectScatter",
          coordinateSystem: "geo",
          rippleEffect: {brushType: "stroke"},
          symbolSize: 12,
          data: [
            {name: "北京", value: [116.4, 39.9, 5]},
            {name: "上海", value: [121.47, 31.23, 8]},
            {name: "广州", value: [113.23, 23.16, 3]}
          ]
        }]
      })
    }
  }
}
</script>

<style scoped>
.data-dashboard {
  padding: 20px;
  background: #f5f7fa;
  font-family: 'Helvetica Neue', Helvetica, 'PingFang SC', 'Hiragino Sans GB', 'Microsoft YaHei', Arial, sans-serif;
}

.dashboard-header {
  margin-bottom: 20px;
  display: flex;
  gap: 10px;
}

.key-metrics {
  display: flex;
  gap: 15px;
  margin-bottom: 20px;
}

.metric-card {
  flex: 1;
  background: #fff;
  padding: 20px 15px;
  text-align: center;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);
  transition: all 0.3s;
}
.metric-card:hover {
  box-shadow: 0 4px 18px 0 rgba(0, 0, 0, 0.1);
}

.metric-value {
  font-size: 28px;
  font-weight: bold;
  color: #1890ff;
  line-height: 1.2;
}

.metric-label {
  font-size: 14px;
  color: #606266;
  margin-top: 8px;
}

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

.chart-card {
  background: #fff;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);
}

.chart-card h3 {
  margin: 0 0 15px 0;
  font-size: 16px;
  font-weight: 500;
  color: #303133;
  border-left: 4px solid #1890ff;
  padding-left: 12px;
}

.chart-box {
  width: 100%;
  height: 300px;
}

/* 任务执行情况优化 */
.task-section {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.task-category {
  background: #fafafa;
  border-radius: 8px;
  padding: 16px;
}

.category-title {
  font-size: 15px;
  font-weight: 500;
  color: #303133;
  margin-bottom: 15px;
  display: flex;
  align-items: center;
  gap: 6px;
}
.category-title i {
  font-size: 18px;
  color: #1890ff;
}

.task-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.task-item {
  background: #ffffff;
  border-radius: 8px;
  padding: 14px 16px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.05);
  transition: all 0.2s;
}
.task-item:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
}
.task-item.pending {
  background: #f9f9f9;
  border-left: 3px solid #909399;
}

.task-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 6px;
}

.task-name {
  font-weight: 500;
  color: #303133;
  font-size: 14px;
}

.task-progress-label {
  font-size: 13px;
  color: #67C23A;
  font-weight: 500;
}

.task-desc {
  font-size: 13px;
  color: #909399;
  margin-bottom: 8px;
}

.task-time {
  font-size: 13px;
  color: #E6A23C;
  background: #fdf6ec;
  padding: 2px 8px;
  border-radius: 12px;
}

/* 机器人分组状态优化 */
.group-list {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.group-item {
  padding: 8px 0;
}

.group-info {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 6px;
}
.group-info i {
  font-size: 18px;
  color: #606266;
}
.group-name {
  font-size: 14px;
  font-weight: 500;
  color: #303133;
}

.group-stats {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 6px;
  font-size: 13px;
}
.stats-number {
  color: #606266;
}
.stats-percent {
  color: #1890ff;
  font-weight: 500;
}

/* 最近上线优化 */
.recent-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.recent-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 8px 12px;
  background: #f9f9f9;
  border-radius: 6px;
  transition: background 0.2s;
}
.recent-item:hover {
  background: #f0f2f5;
}

.robot-name {
  font-weight: 500;
  color: #303133;
}

.robot-time {
  font-size: 13px;
  color: #909399;
  margin-right: 8px;
}

.online-dot {
  width: 8px;
  height: 8px;
  background: #67C23A;
  border-radius: 50%;
  display: inline-block;
}
</style>
