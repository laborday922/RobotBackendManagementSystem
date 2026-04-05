<template>
  <div class="data-cleaning-tools">

    <!-- ===== 配置卡片 ===== -->
    <div class="glass-card">
      <div class="card-header">
        <h3>数据清洗规则配置</h3>
      </div>

      <div class="card-body">
        <!-- 新增：文本格式处理 + 状态统一映射 -->
        <div class="two-column">
          <div class="column">
            <div class="form-item">
              <label>文本格式处理</label>
              <el-select v-model="cleaningRules.textCleaning" placeholder="请选择">
                <el-option label="移除HTML标签" value="REMOVE_HTML" />
                <el-option label="移除特殊字符" value="REMOVE_SPECIAL_CHAR" />
                <el-option label="保持原样" value="KEEP_ORIGINAL" />
              </el-select>
            </div>
          </div>

          <div class="column">
            <div class="form-item">
              <label>状态统一映射</label>
              <el-select v-model="cleaningRules.statusMapping" placeholder="请选择">
                <el-option label="保持原样" value="KEEP_ORIGINAL" />
                <el-option label="映射到平台枚举" value="MAP_TO_PLATFORM_ENUM" />
              </el-select>
            </div>
          </div>
        </div>
        <!-- 两列（增加“保持原样”选项） -->
        <div class="two-column">
          <div class="column">
            <div class="form-item">
              <label>重复数据处理</label>
              <el-select v-model="cleaningRules.duplicateHandling" placeholder="请选择">
                <el-option label="保持原样" value="KEEP_ORIGINAL" />
                <el-option label="保留第一条" value="KEEP_FIRST" />
              </el-select>
            </div>
          </div>
        </div>

        <!-- 应用数据源选择卡片 -->
        <div class="data-source-section">
          <div class="data-source-section">
            <h4>应用数据源</h4>
            <el-tag type="success">交互历史表（t_interaction_history）</el-tag>
          </div>
        </div>

        <!-- 执行方式 -->
        <div class="section">
          <h4>执行方式</h4>

          <div class="execution-box">
            <div
              class="execution-item"
              :class="{active: cleaningRules.executionType === 'schedule'}"
              @click="cleaningRules.executionType = 'schedule'"
            >
              ⏰ 定时执行
            </div>

            <div
              class="execution-item"
              :class="{active: cleaningRules.executionType === 'manual'}"
              @click="cleaningRules.executionType = 'manual'"
            >
              ▶ 手动执行
            </div>
          </div>

          <!-- 定时配置（仅当选择定时执行时显示） -->
          <div v-if="cleaningRules.executionType === 'schedule'" class="schedule-config">
            <div class="schedule-row">
              <div class="schedule-label">周期</div>
              <el-select v-model="scheduleConfig.period" placeholder="请选择周期">
                <el-option label="每日" value="daily" />
                <el-option label="每周" value="weekly" />
                <el-option label="每月" value="monthly" />
              </el-select>
            </div>

            <div class="schedule-row" v-if="scheduleConfig.period === 'weekly'">
              <div class="schedule-label">星期几</div>
              <el-select v-model="scheduleConfig.weekDay" placeholder="请选择">
                <el-option label="周一" value="1" />
                <el-option label="周二" value="2" />
                <el-option label="周三" value="3" />
                <el-option label="周四" value="4" />
                <el-option label="周五" value="5" />
                <el-option label="周六" value="6" />
                <el-option label="周日" value="7" />
              </el-select>
            </div>

            <div class="schedule-row" v-if="scheduleConfig.period === 'monthly'">
              <div class="schedule-label">每月几号</div>
              <el-input-number v-model="scheduleConfig.monthDay" :min="1" :max="31" />
            </div>

            <div class="schedule-row">
              <div class="schedule-label">时间</div>
              <el-time-picker
                v-model="scheduleConfig.time"
                format="HH:mm"
                value-format="HH:mm"
                placeholder="选择时间"
              />
            </div>
          </div>
        </div>

        <!-- 按钮区域：根据执行方式显示不同按钮 -->
        <div class="actions">
          <el-button
            v-if="cleaningRules.executionType === 'schedule'"
            type="primary"
            @click="saveScheduleConfig"
          >
            保存配置
          </el-button>

          <el-button
            v-if="cleaningRules.executionType === 'manual'"
            type="success"
            @click="executeManual"
          >
            立即执行
          </el-button>
        </div>

      </div>
    </div>

    <!-- ===== 记录卡片 ===== -->
    <div class="glass-card">
      <div class="card-header">
        <h3>历史处理记录</h3>
      </div>

      <div class="card-body">
        <!-- 统计（动态计算） -->
        <div class="summary">
          <div class="summary-item">
            <div class="num">{{ totalTasks }}</div>
            <div class="label">总任务数</div>
          </div>
          <div class="summary-item">
            <div class="num green">{{ successTasks }}</div>
            <div class="label">成功任务</div>
          </div>
          <div class="summary-item">
            <div class="num orange">{{ failedTasks }}</div>
            <div class="label">失败任务</div>
          </div>
        </div>

        <!-- 表格 -->
        <el-table :data="historyList" class="glass-table">
          <el-table-column prop="id" label="ID" />
          <el-table-column prop="runTime" label="执行时间" >
            <template slot-scope="scope">
              {{ scope.row.runTime || '待定时执行' }}
            </template>
          </el-table-column>
          <el-table-column prop="executeMode" label="类型">
            <template slot-scope="scope">
              {{ scope.row.executeMode === 'SCHEDULED' ? '定时' : '手动' }}
            </template>
          </el-table-column>
          <el-table-column prop="applyDataSource" label="数据源" />
          <el-table-column prop="status" label="状态">
            <template slot-scope="scope">
              <el-tag
                :type="scope.row.status === 1 ? 'success' : (scope.row.status === 2 ? 'info' : 'danger')"
                size="small">
                {{ scope.row.status === 1 ? '成功' : (scope.row.status === 2 ? '待定' : '失败') }}
              </el-tag>
            </template>
          </el-table-column>
        </el-table>

      </div>
    </div>

  </div>
</template>

<script>
import {createHistory, executeTask, getHistoryList} from '@/api/data/dataClean/dataCleanConfig'

export default {
  name: 'DataCleaningTools',
  data() {
    return {
      cleaningRules: {
        duplicateHandling: '',   // KEEP_FIRST / DELETE_ALL / KEEP_ORIGINAL
        textCleaning: '',        // REMOVE_HTML / REMOVE_SPECIAL_CHAR / KEEP_ORIGINAL
        statusMapping: '',       // MAP_TO_PLATFORM_ENUM / KEEP_ORIGINAL
        executionType: 'schedule'
      },
      // dataSources: {
      //   interactionLogs: false,   // 交互日志（默认选中）
      //   robotStatus: false,       // 机器人状态（默认选中）
      //   userFeedback: false,     // 用户反馈
      //   taskExecution: false,    // 任务执行记录
      //   performance: false,      // 性能指标
      //   errorLogs: false         // 错误日志
      // },
      dataSources: {
        interactionHistory: true // 默认选中 & 唯一
      },
      scheduleConfig: {
        period: 'daily',      // daily, weekly, monthly
        weekDay: '1',         // 1-7 (周一=1, 周日=7)
        monthDay: 1,          // 1-31
        time: '00:00'         // HH:mm
      },
      historyList: []         // 历史记录列表
    }
  },
  computed: {
    // 总任务数
    totalTasks() {
      return this.historyList.length
    },
    // 成功任务数（status === 1）
    successTasks() {
      return this.historyList.filter(item => item.status === 1).length
    },
    // 失败任务数（status === 0）
    failedTasks() {
      return this.historyList.filter(item => item.status === 0).length
    },
    // 已选数据源数量（保持不变）
    selectedDataSourcesCount() {
      return Object.values(this.dataSources).filter(v => v).length
    }
  },
  mounted() {
    this.fetchHistoryList()
  },
  methods: {
    // 判断日期是否为今天
    isToday(dateStr) {
      if (!dateStr) return false
      const date = new Date(dateStr)
      const today = new Date()
      return (
        date.getFullYear() === today.getFullYear() &&
        date.getMonth() === today.getMonth() &&
        date.getDate() === today.getDate()
      )
    },

    // 获取历史记录列表
    async fetchHistoryList() {
      try {
        const res = await getHistoryList()
        if (res.code === 200) {
          this.historyList = res.data || []
        } else {
          this.$message.error(res.msg || '获取历史记录失败')
        }
      } catch (error) {
        console.error('获取历史记录失败', error)
        this.$message.error('获取历史记录失败')
      }
    },

    // 生成 cron 表达式（秒 分 时 日 月 周）
    generateCron() {
      const { period, weekDay, monthDay, time } = this.scheduleConfig
      const [hour, minute] = time.split(':')
      switch (period) {
        case 'daily':
          return `0 ${minute} ${hour} * * ?`
        case 'weekly':
          // 注意：Quartz 中周一=1，周日=7，前端传入的值直接使用
          return `0 ${minute} ${hour} ? * ${weekDay}`
        case 'monthly':
          return `0 ${minute} ${hour} ${monthDay} * ?`
        default:
          return null
      }
    },

    // 获取选中的数据源枚举值（根据实际后端期望的枚举名映射）
    getSelectedDataSources() {
      const mapping = {
        interactionLogs: 'ROBOT_INTERACTION_LOG',
        robotStatus: 'ROBOT_STATUS',
        userFeedback: 'USER_FEEDBACK',
        taskExecution: 'TASK_EXECUTION',
        performance: 'PERFORMANCE',
        errorLogs: 'ERROR_LOG'
      };
      const selected = Object.keys(this.dataSources)
        .filter(key => this.dataSources[key])
        .map(key => mapping[key]);
      return selected.join(',');
    },

    // 构造保存请求参数
    buildHistoryParams(executeMode, cronExpression = null) {
      return {
        executeMode: executeMode,           // 'SCHEDULED' 或 'IMMEDIATE'
        applyDataSource: 't_interaction_history', // 可根据实际需求改为用户选择
        // applyDataSource: this.getSelectedDataSources(),
        configJson: JSON.stringify({
          duplicateHandling: this.cleaningRules.duplicateHandling,
          textCleaning: this.cleaningRules.textCleaning,
          statusMapping: this.cleaningRules.statusMapping
        }),
        status: 1,                                 // 启用
        createTime:  new Date().toISOString().slice(0, 19).replace(' ', 'T'),
        cronExpression: cronExpression
      }
    },

    // 定时模式：保存配置（不执行）
    async saveScheduleConfig() {
      const cron = this.generateCron()
      const params = this.buildHistoryParams('SCHEDULED', cron)
      try {
        const res = await createHistory(params)
        if (res.code === 200) {
          this.$message.success('定时配置保存成功')
          this.fetchHistoryList()
        } else {
          this.$message.error(res.msg || '保存失败')
        }
      } catch (error) {
        console.error('保存失败', error)
      }
    },

    // 手动模式：立即执行（先保存配置，再执行）
    async executeManual() {
      // 1. 保存规则（无 cron）
      const params = this.buildHistoryParams('IMMEDIATE', null)
      let recordId
      try {
        const res = await createHistory(params)
        if (res.code === 200) {
          recordId = res.data
          this.$message.success('规则保存成功')
        } else {
          this.$message.error(res.msg || '保存规则失败')
          return
        }
      } catch (error) {
        console.error('保存规则失败', error)
        return
      }

      // 2. 执行任务
      try {
        const execRes = await executeTask(recordId)
        if (execRes.code === 200) {
          this.$message.success('任务已提交执行')
          // 稍后刷新历史记录，等待执行结果
          setTimeout(() => this.fetchHistoryList(), 1000)
        } else {
          this.$message.error(execRes.msg || '执行失败')
        }
      } catch (error) {
        console.error('执行失败', error)
        this.$message.error('执行失败')
      }
    }
  }
}
</script>

<style scoped lang="scss">
/* 原有样式保持不变，增加定时配置相关样式 */

.data-cleaning-tools {
  padding: 0 24px;
}

.glass-card {
  margin-top: 20px;
  margin-bottom: 24px;
  border-radius: 16px;
  backdrop-filter: blur(12px);
  background: rgba(255,255,255);
  box-shadow: 0 5px 5px rgba(0,0,0,0.2);
  border: 1px solid rgba(255,255,255,0.3);
  overflow: hidden;
}

.card-header {
  padding: 10px 24px;
  font-weight: 400;
  border-bottom: 1px solid rgba(0,0,0,0.05);
}

.card-body {
  padding: 24px;
}

.two-column {
  display: flex;
  gap: 24px;
}

.column {
  flex: 1;
}

.form-item {
  margin-bottom: 20px;
}

.form-item label {
  display: block;
  margin-bottom: 6px;
  font-size: 13px;
  color: #666;
}

.execution-box {
  display: flex;
  gap: 12px;
  margin-top: 10px;
}

.execution-item {
  flex: 1;
  padding: 12px;
  border-radius: 10px;
  background: #f5f7fa;
  text-align: center;
  cursor: pointer;
  transition: all 0.2s;
}

.execution-item.active {
  background: linear-gradient(135deg, #4e8cff, #6ea8ff);
  color: #fff;
}

/* 定时配置样式 */
.schedule-config {
  margin-top: 16px;
  padding: 16px;
  background: #f9fafc;
  border-radius: 12px;
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
}

.schedule-row {
  display: flex;
  align-items: center;
  gap: 12px;
  flex: 1;
  min-width: 160px;
}

.schedule-label {
  font-size: 13px;
  color: #666;
  width: 70px;
}

.actions {
  margin-top: 20px;
  display: flex;
  gap: 12px;
}

.summary {
  display: flex;
  gap: 20px;
  margin-bottom: 20px;
}

.summary-item {
  flex: 1;
  background: rgba(255,255,255,0.6);
  padding: 16px;
  border-radius: 12px;
  text-align: center;
}

.num {
  font-size: 22px;
  font-weight: bold;
}

.green { color: #52c41a; }
.orange { color: #fa8c16; }

.glass-table ::v-deep .el-table {
  background: transparent;
}

.glass-table ::v-deep th {
  background: rgba(0,0,0,0.03);
}

.glass-table ::v-deep tr {
  background: transparent;
}

//数据源卡片样式
.data-source-section {
  margin: 20px 0;
  h4 {
    margin-bottom: 12px;
    font-size: 14px;
    font-weight: 500;
    color: #333;
  }
}
.simple-checkbox-group {
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
}
.simple-checkbox {
  display: flex;
  align-items: center;
  input {
    margin-right: 6px;
    cursor: pointer;
  }
  label {
    cursor: pointer;
    font-size: 13px;
    color: #666;
  }
}
.selected-count {
  margin-top: 10px;
  font-size: 12px;
  color: #999;
}
</style>
