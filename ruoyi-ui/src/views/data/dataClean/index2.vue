<template>
  <div class="app-container">
    <el-card class="search-card" shadow="never">
      <div class="summary">
        <div class="summary-item">
          <div class="num">{{ taskList.length }}</div>
          <div class="label">等待执行的定时任务</div>
        </div>
        <div class="summary-item">
          <div class="num green">{{ recordAllTotal || recordTotal }}</div>
          <div class="label">执行记录总数</div>
        </div>
      </div>
    </el-card>

    <el-card class="table-card" shadow="never">
      <el-tabs v-model="activeTab">
        <el-tab-pane label="定时任务" name="task">
          <div class="sub-card">
            <div class="table-header">
              <span>定时任务列表</span>
              <div class="table-actions">
                <el-button type="primary" size="small" icon="el-icon-plus" @click="openCreateDialog">新增数据清洗</el-button>
              </div>
            </div>

            <div class="table-wrapper">
              <el-table v-loading="taskLoading" :data="taskList" border>
                <el-table-column prop="id" label="ID" width="70" />
                <el-table-column label="数据清洗规则" min-width="220">
                  <template slot-scope="scope">
                    {{ formatRules(scope.row.configJson) }}
                  </template>
                </el-table-column>
                <el-table-column label="数据源" min-width="160">
                  <template slot-scope="scope">
                    {{ formatDataSources(scope.row.applyDataSource) }}
                  </template>
                </el-table-column>
                <el-table-column prop="cronExpression" label="cron 表达式" min-width="140" />
                <el-table-column label="下次执行时间" width="180">
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
        </el-tab-pane>

        <el-tab-pane label="执行记录" name="record">
          <div class="sub-card">
            <div class="table-header">
              <span>执行记录列表</span>
            </div>

            <el-form :model="recordQuery" ref="recordQueryForm" size="small" :inline="true" label-width="80px" class="query-form">
              <el-form-item label="执行方式" prop="executeMode">
                <el-select v-model="recordQuery.executeMode" placeholder="请选择" clearable style="width: 140px">
                  <el-option label="定时" value="SCHEDULED" />
                  <el-option label="手动" value="MANUAL" />
                </el-select>
              </el-form-item>
              <el-form-item label="执行结果" prop="success">
                <el-select v-model="recordQuery.success" placeholder="请选择" clearable style="width: 140px">
                  <el-option label="成功" :value="1" />
                  <el-option label="失败" :value="0" />
                </el-select>
              </el-form-item>
              <el-form-item>
                <el-button type="primary" icon="el-icon-search" size="mini" @click="handleRecordQuery">查询</el-button>
                <el-button icon="el-icon-refresh" size="mini" @click="resetRecordQuery">重置</el-button>
              </el-form-item>
            </el-form>

            <div class="table-wrapper">
              <el-table v-loading="recordLoading" :data="recordList" border>
                <el-table-column prop="id" label="ID" width="70" />
                <el-table-column label="执行时间" width="180">
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

            <div class="pagination-wrap">
              <pagination
                v-show="recordTotal>0"
                :total="recordTotal"
                :page.sync="recordQuery.pageNum"
                :limit.sync="recordQuery.pageSize"
                @pagination="handleRecordPagination"
              />
            </div>
          </div>
        </el-tab-pane>
      </el-tabs>
    </el-card>

    <el-dialog :title="configDialogTitle" :visible.sync="configDialogVisible" width="760px" append-to-body @close="handleDialogClose">
      <div class="dialog-body">
        <div class="two-column">
          <div class="column">
            <div class="form-item">
              <label>文本格式处理</label>
              <el-select v-model="cleaningRules.textCleaning" placeholder="请选择" style="width: 100%">
                <el-option label="移除HTML标签" value="REMOVE_HTML" />
                <el-option label="移除特殊字符" value="REMOVE_SPECIAL_CHAR" />
                <el-option label="保持原样" value="KEEP_ORIGINAL" />
              </el-select>
            </div>
          </div>

          <div class="column">
            <div class="form-item">
              <label>重复数据处理</label>
              <el-select v-model="cleaningRules.duplicateHandling" placeholder="请选择" style="width: 100%">
                <el-option label="保持原样" value="KEEP_ORIGINAL" />
                <el-option label="保留第一条" value="KEEP_FIRST" />
              </el-select>
            </div>
          </div>
        </div>

        <div class="data-source-section">
          <h4>应用数据源</h4>
          <el-checkbox-group v-model="cleaningRules.applyDataSources">
            <el-checkbox label="t_interaction_history">交互历史表（t_interaction_history）</el-checkbox>
            <el-checkbox label="qa_log">QA日志表（qa_log）</el-checkbox>
          </el-checkbox-group>
        </div>

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

          <div v-if="cleaningRules.executionType === 'schedule'" class="schedule-config">
            <div class="schedule-row">
              <div class="schedule-label">周期</div>
              <el-select v-model="scheduleConfig.period" placeholder="请选择周期" style="width: 100%">
                <el-option label="每日" value="daily" />
                <el-option label="每周" value="weekly" />
                <el-option label="每月" value="monthly" />
              </el-select>
            </div>

            <div class="schedule-row" v-if="scheduleConfig.period === 'weekly'">
              <div class="schedule-label">星期几</div>
              <el-select v-model="scheduleConfig.weekDay" placeholder="请选择" style="width: 100%">
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
              <el-input-number v-model="scheduleConfig.monthDay" :min="1" :max="31" style="width: 100%" />
            </div>

            <div class="schedule-row">
              <div class="schedule-label">时间</div>
              <el-time-picker
                v-model="scheduleConfig.time"
                format="HH:mm"
                value-format="HH:mm"
                placeholder="选择时间"
                style="width: 100%"
              />
            </div>
          </div>
        </div>
      </div>

      <div slot="footer" class="dialog-footer">
        <el-button @click="configDialogVisible=false">取 消</el-button>
        <template v-if="cleaningRules.executionType === 'schedule'">
          <el-button type="primary" @click="saveScheduleConfig">{{ editingId ? '更新配置' : '保存配置' }}</el-button>
        </template>
        <el-button v-else type="success" @click="handleManualExecute">立即执行</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import {createTask, updateTask, deleteTask, executeManual, getTaskList, getRecordList} from '@/api/data/dataClean/dataCleanConfig'

export default {
  name: 'DataCleaningTools',
  data() {
    return {
      activeTab: 'task',
      taskLoading: false,
      recordLoading: false,
      cleaningRules: {
        duplicateHandling: '',   // KEEP_FIRST / KEEP_ORIGINAL
        textCleaning: '',        // REMOVE_HTML / REMOVE_SPECIAL_CHAR / KEEP_ORIGINAL
        executionType: 'schedule',
        applyDataSources: ['t_interaction_history']
      },
      scheduleConfig: {
        period: 'daily',      // daily, weekly, monthly
        weekDay: '1',         // 1-7 (周一=1, 周日=7)
        monthDay: 1,          // 1-31
        time: '00:00'         // HH:mm
      },
      configDialogVisible: false,
      editingId: null,      // 正在编辑的任务ID，null 表示新增
      taskList: [],         // 定时任务列表
      recordList: [],       // 执行记录列表
      recordAllList: [],
      recordAllTotal: 0,
      recordTotal: 0,
      recordQuery: {
        pageNum: 1,
        pageSize: 10,
        executeMode: null,
        success: null
      }
    }
  },
  mounted() {
    this.fetchTaskList()
    this.fetchRecordList()
  },
  computed: {
    configDialogTitle() {
      return this.editingId ? `编辑清洗任务 #${this.editingId}` : '新增清洗任务'
    }
  },
  methods: {
    openCreateDialog() {
      this.resetForm()
      this.configDialogVisible = true
    },

    // 获取定时任务列表
    async fetchTaskList() {
      this.taskLoading = true
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
      } finally {
        this.taskLoading = false
      }
    },

    // 获取执行记录列表
    async fetchRecordList() {
      this.recordLoading = true
      try {
        const params = {
          pageNum: this.recordQuery.pageNum,
          pageSize: this.recordQuery.pageSize,
          executeMode: this.recordQuery.executeMode,
          success: this.recordQuery.success
        }
        const res = await getRecordList(params)
        if (res.code === 200) {
          if (Array.isArray(res.rows) || typeof res.total === 'number') {
            const rows = res.rows || []
            this.recordList = rows
            this.recordTotal = typeof res.total === 'number' ? res.total : rows.length
            this.recordAllTotal = this.recordTotal
            this.recordAllList = []
            return
          }

          const all = Array.isArray(res.data) ? res.data : []
          this.recordAllList = all
          this.recordAllTotal = all.length
          this.applyRecordFilterAndPaging()
        } else {
          this.$message.error(res.msg || '获取执行记录失败')
        }
      } catch (error) {
        console.error('获取执行记录失败', error)
        this.$message.error('获取执行记录失败')
      } finally {
        this.recordLoading = false
      }
    },

    handleRecordQuery() {
      this.recordQuery.pageNum = 1
      if (this.recordAllList && this.recordAllList.length) {
        this.applyRecordFilterAndPaging()
      } else {
        this.fetchRecordList()
      }
    },

    handleRecordPagination() {
      if (this.recordAllList && this.recordAllList.length) {
        this.applyRecordFilterAndPaging()
        return
      }
      this.fetchRecordList()
    },

    resetRecordQuery() {
      this.recordQuery = { pageNum: 1, pageSize: 10, executeMode: null, success: null }
      this.fetchRecordList()
    },

    applyRecordFilterAndPaging() {
      let list = Array.isArray(this.recordAllList) ? this.recordAllList.slice() : []
      if (this.recordQuery.executeMode) {
        if (this.recordQuery.executeMode === 'SCHEDULED') {
          list = list.filter(r => String(r.executeMode) === 'SCHEDULED')
        } else if (this.recordQuery.executeMode === 'MANUAL') {
          list = list.filter(r => String(r.executeMode) !== 'SCHEDULED')
        }
      }
      if (this.recordQuery.success !== null && this.recordQuery.success !== undefined && this.recordQuery.success !== '') {
        const expected = Number(this.recordQuery.success)
        list = list.filter(r => Number(r.success) === expected)
      }

      this.recordTotal = list.length
      const pageNum = Number(this.recordQuery.pageNum) || 1
      const pageSize = Number(this.recordQuery.pageSize) || 10
      const start = (pageNum - 1) * pageSize
      if (start >= list.length && pageNum > 1) {
        this.recordQuery.pageNum = 1
        this.recordList = list.slice(0, pageSize)
        return
      }
      this.recordList = list.slice(start, start + pageSize)
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

    formatDataSources(value) {
      const list = String(value || '')
        .split(',')
        .map(s => s.trim())
        .filter(Boolean)
      if (!list.length) return '-'
      const map = {
        t_interaction_history: '交互历史',
        qa_log: 'QA日志'
      }
      return list.map(x => map[x] || x).join('、')
    },

    // 构造保存请求参数
    buildParams(executeMode, cronExpression = null) {
      return {
        executeMode,                             // 'SCHEDULED'
        applyDataSource: (this.cleaningRules.applyDataSources || []).join(','),
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
      if (!this.cleaningRules.applyDataSources || !this.cleaningRules.applyDataSources.length) {
        this.$message.warning('请选择至少一个数据源')
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
          this.configDialogVisible = false
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
      if (!this.cleaningRules.applyDataSources || !this.cleaningRules.applyDataSources.length) {
        this.$message.warning('请选择至少一个数据源')
        return
      }
      const configJson = JSON.stringify({
        duplicateHandling: this.cleaningRules.duplicateHandling,
        textCleaning: this.cleaningRules.textCleaning
      })
      try {
        const res = await executeManual({
          configJson,
          applyDataSource: (this.cleaningRules.applyDataSources || []).join(',')
        })
        if (res.code === 200) {
          this.$message.success('执行完成')
          this.recordQuery.pageNum = 1
          await this.fetchRecordList()
          this.configDialogVisible = false
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
      this.cleaningRules.applyDataSources = String(row.applyDataSource || '')
        .split(',')
        .map(s => s.trim())
        .filter(Boolean)
      this.scheduleConfig = this.parseCron(row.cronExpression)
      this.configDialogVisible = true
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
      this.cleaningRules.applyDataSources = ['t_interaction_history']
      this.scheduleConfig = { period: 'daily', weekDay: '1', monthDay: 1, time: '00:00' }
    },

    handleDialogClose() {
      this.resetForm()
    }
  }
}
</script>

<style scoped lang="scss">
.card-header {
  display: flex;
  align-items: center;
  gap: 12px;
  justify-content: space-between;
}

.app-container {
  padding: 20px;
}

.search-card {
  margin-bottom: 20px;
}

.table-card {
  margin-bottom: 20px;
}

.sub-card {
  background: #ffffff;
  border-radius: 8px;
  padding: 20px;
  border: 1px solid #E5E7EB;
}

.table-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  flex-wrap: wrap;
  gap: 15px;
}

.table-header span {
  font-size: 16px;
  font-weight: 600;
  color: #1e2a3a;
}

.table-actions {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.query-form {
  margin-bottom: 12px;
}

.table-wrapper {
  width: 100%;
  overflow-x: auto;
}

.pagination-wrap {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}

.card-actions {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}

.summary {
  display: flex;
  gap: 20px;
}

.summary-item {
  flex: 1;
  padding: 16px;
  border-radius: 8px;
  background: #f8fafc;
  text-align: center;
}

.num {
  font-size: 22px;
  font-weight: 700;
  color: #303133;
}

.label {
  margin-top: 6px;
  font-size: 13px;
  color: #606266;
}

.green {
  color: #52c41a;
}


.hint-text {
  color: #909399;
  font-size: 13px;
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

.danger-text {
  color: #f56c6c;
}
</style>
