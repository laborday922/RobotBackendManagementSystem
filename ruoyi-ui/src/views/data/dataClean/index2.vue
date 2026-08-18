<template>
  <div class="data-cleaning-tools">

    <!-- ===== 配置卡片 ===== -->
    <div class="glass-card">
      <div class="card-header">
        <h3>数据清洗规则配置</h3>
        <el-tag v-if="editingId" type="warning" size="small">正在编辑任务 #{{ editingId }}</el-tag>
      </div>

      <div class="card-body">
        <!-- 文本格式处理 + 重复数据处理 -->
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
              <label>重复数据处理</label>
              <el-select v-model="cleaningRules.duplicateHandling" placeholder="请选择">
                <el-option label="保持原样" value="KEEP_ORIGINAL" />
                <el-option label="保留第一条" value="KEEP_FIRST" />
              </el-select>
            </div>
          </div>
        </div>

        <!-- 应用数据源 -->
        <div class="data-source-section">
          <h4>应用数据源</h4>
          <el-tag type="success">交互历史表（t_interaction_history）</el-tag>
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
          <template v-if="cleaningRules.executionType === 'schedule'">
            <el-button type="primary" @click="saveScheduleConfig">
              {{ editingId ? '更新配置' : '保存配置' }}
            </el-button>
            <el-button v-if="editingId" @click="resetForm">取消编辑</el-button>
          </template>

          <el-button
            v-if="cleaningRules.executionType === 'manual'"
            type="success"
            @click="handleManualExecute"
          >
            立即执行
          </el-button>
        </div>

      </div>
    </div>

    <!-- ===== 定时任务表 ===== -->
    <div class="glass-card">
      <div class="card-header">
        <h3>定时任务</h3>
      </div>

      <div class="card-body">
        <div class="summary">
          <div class="summary-item">
            <div class="num">{{ taskList.length }}</div>
            <div class="label">等待执行的定时任务</div>
          </div>
          <div class="summary-item">
            <div class="num green">{{ recordList.length }}</div>
            <div class="label">执行记录总数</div>
          </div>
        </div>

        <el-table :data="taskList" class="glass-table">
          <el-table-column prop="id" label="ID" width="70" />
          <el-table-column label="数据清洗规则" min-width="220">
            <template slot-scope="scope">
              {{ formatRules(scope.row.configJson) }}
            </template>
          </el-table-column>
          <el-table-column prop="applyDataSource" label="数据源" width="160" />
          <el-table-column prop="cronExpression" label="cron 表达式" width="140" />
          <el-table-column label="下次执行时间" width="170">
            <template slot-scope="scope">
              {{ formatTime(scope.row.nextRunTime) }}
            </template>
          </el-table-column>
          <el-table-column label="操作" align="center" width="140">
            <template slot-scope="scope">
              <el-button type="text" size="small" @click="handleEdit(scope.row)">编辑</el-button>
              <el-button type="text" size="small" class="danger-text" @click="handleDelete(scope.row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </div>

    <!-- ===== 执行记录表 ===== -->
    <div class="glass-card">
      <div class="card-header">
        <h3>执行记录</h3>
      </div>

      <div class="card-body">
        <el-table :data="recordList" class="glass-table">
          <el-table-column prop="id" label="ID" width="70" />
          <el-table-column label="执行时间" width="170">
            <template slot-scope="scope">
              {{ formatTime(scope.row.runTime) }}
            </template>
          </el-table-column>
          <el-table-column label="执行方式" width="100">
            <template slot-scope="scope">
              {{ scope.row.executeMode === 'SCHEDULED' ? '定时' : '手动' }}
            </template>
          </el-table-column>
          <el-table-column label="执行结果" width="100">
            <template slot-scope="scope">
              <el-tag :type="scope.row.success === 1 ? 'success' : 'danger'" size="small">
                {{ scope.row.success === 1 ? '成功' : '失败' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="message" label="失败原因" min-width="200">
            <template slot-scope="scope">
              {{ scope.row.message || '-' }}
            </template>
          </el-table-column>
        </el-table>
      </div>
    </div>

  </div>
</template>

<script>
import {createTask, updateTask, deleteTask, executeManual, getTaskList, getRecordList} from '@/api/data/dataClean/dataCleanConfig'

export default {
  name: 'DataCleaningTools',
  data() {
    return {
      cleaningRules: {
        duplicateHandling: '',   // KEEP_FIRST / KEEP_ORIGINAL
        textCleaning: '',        // REMOVE_HTML / REMOVE_SPECIAL_CHAR / KEEP_ORIGINAL
        executionType: 'schedule'
      },
      scheduleConfig: {
        period: 'daily',      // daily, weekly, monthly
        weekDay: '1',         // 1-7 (周一=1, 周日=7)
        monthDay: 1,          // 1-31
        time: '00:00'         // HH:mm
      },
      editingId: null,      // 正在编辑的任务ID，null 表示新增
      taskList: [],         // 定时任务列表
      recordList: []        // 执行记录列表
    }
  },
  mounted() {
    this.fetchTaskList()
    this.fetchRecordList()
  },
  methods: {
    // 获取定时任务列表
    async fetchTaskList() {
      try {
        const res = await getTaskList()
        if (res.code === 200) {
          this.taskList = res.data || []
        } else {
          this.$message.error(res.msg || '获取定时任务失败')
        }
      } catch (error) {
        console.error('获取定时任务失败', error)
        this.$message.error('获取定时任务失败')
      }
    },

    // 获取执行记录列表
    async fetchRecordList() {
      try {
        const res = await getRecordList()
        if (res.code === 200) {
          this.recordList = res.data || []
        } else {
          this.$message.error(res.msg || '获取执行记录失败')
        }
      } catch (error) {
        console.error('获取执行记录失败', error)
        this.$message.error('获取执行记录失败')
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
          return `0 ${minute} ${hour} ? * ${weekDay}`
        case 'monthly':
          return `0 ${minute} ${hour} ${monthDay} * ?`
        default:
          return null
      }
    },

    // 从 cron 表达式反解出定时配置（用于编辑回显）
    parseCron(cron) {
      const def = { period: 'daily', weekDay: '1', monthDay: 1, time: '00:00' }
      if (!cron) return def
      const parts = String(cron).split(' ')
      if (parts.length < 6) return def
      const minute = parts[1]
      const hour = parts[2]
      const time = `${String(hour).padStart(2, '0')}:${String(minute).padStart(2, '0')}`
      if (parts[5] !== '?') {
        return { period: 'weekly', weekDay: parts[5], monthDay: 1, time }
      }
      if (parts[3] !== '*' && parts[3] !== '?') {
        return { period: 'monthly', weekDay: '1', monthDay: parseInt(parts[3]) || 1, time }
      }
      return { period: 'daily', weekDay: '1', monthDay: 1, time }
    },

    // 解析规则配置 JSON
    parseRules(configJson) {
      let obj = {}
      try {
        obj = JSON.parse(configJson || '{}')
      } catch (e) {
        obj = {}
      }
      return {
        textCleaning: obj.textCleaning || '',
        duplicateHandling: obj.duplicateHandling || ''
      }
    },

    // 规则 JSON 转为可读文本（用于表格展示）
    formatRules(configJson) {
      const rules = this.parseRules(configJson)
      const textMap = {
        REMOVE_HTML: '移除HTML标签',
        REMOVE_SPECIAL_CHAR: '移除特殊字符',
        KEEP_ORIGINAL: '保持原样'
      }
      const dupMap = {
        KEEP_FIRST: '保留第一条',
        KEEP_ORIGINAL: '保持原样',
        DELETE_ALL: '删除全部'
      }
      const text = textMap[rules.textCleaning] || rules.textCleaning || '-'
      const dup = dupMap[rules.duplicateHandling] || rules.duplicateHandling || '-'
      return `文本：${text}；去重：${dup}`
    },

    // 时间格式化（兼容 'T' 分隔与空格分隔）
    formatTime(value) {
      if (!value) return '-'
      return String(value).replace('T', ' ').slice(0, 19)
    },

    // 构造保存请求参数
    buildParams(executeMode, cronExpression = null) {
      return {
        executeMode,                             // 'SCHEDULED'
        applyDataSource: 't_interaction_history',
        configJson: JSON.stringify({
          duplicateHandling: this.cleaningRules.duplicateHandling,
          textCleaning: this.cleaningRules.textCleaning
        }),
        cronExpression
      }
    },

    // 定时模式：新增或更新定时任务
    async saveScheduleConfig() {
      if (!this.cleaningRules.textCleaning || !this.cleaningRules.duplicateHandling) {
        this.$message.warning('请选择文本处理和重复数据处理方式')
        return
      }
      const cron = this.generateCron()
      const params = this.buildParams('SCHEDULED', cron)
      try {
        const res = this.editingId
          ? await updateTask(this.editingId, params)
          : await createTask(params)
        if (res.code === 200) {
          this.$message.success(this.editingId ? '更新成功' : '保存成功')
          this.resetForm()
          this.fetchTaskList()
        } else {
          this.$message.error(res.msg || '保存失败')
        }
      } catch (error) {
        console.error('保存失败', error)
        this.$message.error('保存失败')
      }
    },

    // 手动模式：立即执行（不落任务，只产生执行记录）
    async handleManualExecute() {
      if (!this.cleaningRules.textCleaning || !this.cleaningRules.duplicateHandling) {
        this.$message.warning('请选择文本处理和重复数据处理方式')
        return
      }
      const configJson = JSON.stringify({
        duplicateHandling: this.cleaningRules.duplicateHandling,
        textCleaning: this.cleaningRules.textCleaning
      })
      try {
        const res = await executeManual({
          configJson,
          applyDataSource: 't_interaction_history'
        })
        if (res.code === 200) {
          this.$message.success('执行完成')
          this.fetchRecordList()
        } else {
          this.$message.error(res.msg || '执行失败')
        }
      } catch (error) {
        console.error('执行失败', error)
        this.$message.error('执行失败')
      }
    },

    // 编辑定时任务：回显配置
    handleEdit(row) {
      this.editingId = row.id
      this.cleaningRules.executionType = 'schedule'
      const rules = this.parseRules(row.configJson)
      this.cleaningRules.textCleaning = rules.textCleaning
      this.cleaningRules.duplicateHandling = rules.duplicateHandling
      this.scheduleConfig = this.parseCron(row.cronExpression)
      this.$nextTick(() => {
        this.$el && this.$el.scrollIntoView({ behavior: 'smooth', block: 'start' })
      })
    },

    // 删除定时任务（删除后不再定时执行）
    async handleDelete(row) {
      try {
        await this.$confirm('确认删除该定时任务？删除后将不再定时执行。', '提示', { type: 'warning' })
      } catch (e) {
        return
      }
      try {
        const res = await deleteTask(row.id)
        if (res.code === 200) {
          this.$message.success('删除成功')
          this.fetchTaskList()
        } else {
          this.$message.error(res.msg || '删除失败')
        }
      } catch (error) {
        console.error('删除失败', error)
        this.$message.error('删除失败')
      }
    },

    // 重置表单（新增状态）
    resetForm() {
      this.editingId = null
      this.cleaningRules.duplicateHandling = ''
      this.cleaningRules.textCleaning = ''
      this.cleaningRules.executionType = 'schedule'
      this.scheduleConfig = { period: 'daily', weekDay: '1', monthDay: 1, time: '00:00' }
    }
  }
}
</script>

<style scoped lang="scss">
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
  display: flex;
  align-items: center;
  gap: 12px;
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

.data-source-section {
  margin: 20px 0;
  h4 {
    margin-bottom: 12px;
    font-size: 14px;
    font-weight: 500;
    color: #333;
  }
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

.glass-table ::v-deep .el-table {
  background: transparent;
}

.glass-table ::v-deep th {
  background: rgba(0,0,0,0.03);
}

.glass-table ::v-deep tr {
  background: transparent;
}

.danger-text {
  color: #f56c6c;
}
</style>
