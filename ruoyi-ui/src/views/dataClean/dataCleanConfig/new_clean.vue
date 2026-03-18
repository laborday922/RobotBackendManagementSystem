<template>
  <div class="data-cleaning-tools">
    <!-- 规则配置卡片 -->
    <div class="card">
      <div class="card-header">
        <div class="card-title">
          <i class="fas fa-broom" style="margin-right:8px;"></i>数据清洗规则配置
          <el-tooltip placement="right" effect="light">
            <template #content>
              <div style="max-width: 300px;">
                配置数据清洗规则，对机器人采集的交互日志、状态与用户反馈数据进行标准化处理，为后续分析提供高质量数据。
              </div>
            </template>
            <i class="fas fa-question-circle" style="color: #3976E4; cursor: pointer; margin-left: 8px;"></i>
          </el-tooltip>
        </div>
        <!-- 此处可添加额外操作，例如导出配置等 -->
      </div>
      <div class="card-body">
        <!-- 两列表单（水平布局） -->
        <div class="two-column-form">
          <div class="form-column">
            <div class="form-row horizontal">
              <label>字段完整性校验</label>
              <el-select v-model="cleaningRules.fieldValidation" placeholder="请选择" clearable>
                <el-option label="缺失字段标记异常" value="mark"></el-option>
                <el-option label="缺失字段自动丢弃" value="discard"></el-option>
                <el-option label="缺失字段填充默认值" value="fill"></el-option>
              </el-select>
            </div>
            <div class="form-row horizontal">
              <label>时间格式统一</label>
              <el-select v-model="cleaningRules.timeFormat" placeholder="请选择" clearable>
                <el-option label="ISO 8601" value="iso8601"></el-option>
                <el-option label="时间戳（毫秒）" value="timestamp_ms"></el-option>
                <el-option label="时间戳（秒）" value="timestamp_s"></el-option>
              </el-select>
            </div>
            <div class="form-row horizontal">
              <label>状态码映射规则</label>
              <el-select v-model="cleaningRules.statusMapping" placeholder="请选择" clearable>
                <el-option label="映射为平台统一枚举" value="enum"></el-option>
                <el-option label="保留原始值" value="original"></el-option>
                <el-option label="自定义映射表" value="custom"></el-option>
              </el-select>
            </div>
          </div>

          <div class="form-column">
            <div class="form-row horizontal">
              <label>异常值过滤策略</label>
              <el-select v-model="cleaningRules.outlierFilter" placeholder="请选择" clearable>
                <el-option label="标记异常" value="mark"></el-option>
                <el-option label="直接过滤" value="filter"></el-option>
                <el-option label="使用统计方法修正" value="correct"></el-option>
              </el-select>
            </div>
            <div class="form-row horizontal">
              <label>文本数据清洗</label>
              <el-select v-model="cleaningRules.textCleaning" placeholder="请选择" clearable>
                <el-option label="去除HTML标签" value="html"></el-option>
                <el-option label="去除特殊字符" value="special"></el-option>
                <el-option label="保留原始文本" value="original"></el-option>
              </el-select>
            </div>
            <div class="form-row horizontal">
              <label>重复数据处理</label>
              <el-select v-model="cleaningRules.duplicateHandling" placeholder="请选择" clearable>
                <el-option label="保留第一条" value="keep_first"></el-option>
                <el-option label="保留最后一条" value="keep_last"></el-option>
                <el-option label="删除所有重复" value="remove_all"></el-option>
              </el-select>
            </div>
          </div>
        </div>

        <!-- 时间配置（执行方式） -->
        <div class="time-config-section">
          <h4>执行方式</h4>
          <el-radio-group v-model="cleaningRules.executionType" class="execution-options">
            <el-radio-button value="schedule">
              <i class="fas fa-clock"></i> 定时清洗
            </el-radio-button>
            <el-radio-button value="manual">
              <i class="fas fa-play-circle"></i> 手动触发
            </el-radio-button>
          </el-radio-group>

          <div v-if="cleaningRules.executionType === 'schedule'" class="option-details">
            <div class="detail-row combined">
              <div class="detail-item">
                <label>执行频率</label>
                <el-select v-model="cleaningRules.scheduleFrequency" placeholder="请选择" style="width:140px;">
                  <el-option label="每小时" value="hourly"></el-option>
                  <el-option label="每天" value="daily"></el-option>
                  <el-option label="每周" value="weekly"></el-option>
                  <el-option label="每月" value="monthly"></el-option>
                </el-select>
              </div>

              <!-- 每天的执行时间 -->
              <div class="detail-item" v-if="cleaningRules.scheduleFrequency === 'daily'">
                <label>执行时间</label>
                <el-time-picker
                  v-model="cleaningRules.scheduleTime"
                  format="HH:mm"
                  value-format="HH:mm"
                  placeholder="选择时间"
                  style="width:140px;">
                </el-time-picker>
              </div>

              <!-- 每周的执行星期 -->
              <div class="detail-item" v-if="cleaningRules.scheduleFrequency === 'weekly'">
                <label>执行星期</label>
                <el-select v-model="cleaningRules.scheduleDay" placeholder="请选择" style="width:140px;">
                  <el-option label="星期一" value="1"></el-option>
                  <el-option label="星期二" value="2"></el-option>
                  <el-option label="星期三" value="3"></el-option>
                  <el-option label="星期四" value="4"></el-option>
                  <el-option label="星期五" value="5"></el-option>
                  <el-option label="星期六" value="6"></el-option>
                  <el-option label="星期日" value="0"></el-option>
                </el-select>
              </div>
            </div>
          </div>
        </div>

        <!-- 应用数据源（复选框） -->
        <div class="data-source-section">
          <h4>应用数据源</h4>
          <el-checkbox-group v-model="selectedDataSources" class="simple-checkbox-group">
            <el-checkbox label="interactionLogs">交互日志</el-checkbox>
            <el-checkbox label="robotStatus">机器人状态</el-checkbox>
            <el-checkbox label="userFeedback">用户反馈</el-checkbox>
            <el-checkbox label="taskExecution">任务执行记录</el-checkbox>
            <el-checkbox label="performance">性能指标</el-checkbox>
            <el-checkbox label="errorLogs">错误日志</el-checkbox>
          </el-checkbox-group>
          <div class="selected-count">已选择 {{ selectedDataSourcesCount }} 个数据源</div>
        </div>

        <!-- 操作按钮 -->
        <div class="action-buttons">
          <el-button type="primary" @click="saveCleaningRules">
            <i class="fas fa-save"></i> 保存配置
          </el-button>
          <el-button @click="testCleaningRules">
            <i class="fas fa-vial"></i> 测试规则
          </el-button>
          <el-button
            v-if="cleaningRules.executionType === 'manual'"
            type="success"
            @click="executeCleaningNow">
            <i class="fas fa-play"></i> 立即执行
          </el-button>
        </div>
      </div>
    </div>

    <!-- 最近处理记录卡片 -->
    <div class="card">
      <div class="card-header">
        <div class="card-title"><i class="fas fa-history" style="margin-right:8px;"></i>最近处理记录</div>
        <div>
          <el-button size="small" @click="refreshRecords"><i class="fas fa-sync-alt"></i> 刷新</el-button>
          <el-button size="small" type="primary" @click="showAllRecords"><i class="fas fa-list"></i> 查看全部</el-button>
        </div>
      </div>
      <div class="card-body">
        <!-- 统计数据卡片 -->
        <div class="records-summary">
          <div class="summary-item">
            <div class="summary-icon" style="background: #e6f7ff; color: #1890ff">
              <i class="fas fa-database"></i>
            </div>
            <div class="summary-info">
              <div class="summary-value">5,425</div>
              <div class="summary-label">今日处理总数</div>
            </div>
          </div>
          <div class="summary-item">
            <div class="summary-icon" style="background: #f6ffed; color: #52c41a">
              <i class="fas fa-check-circle"></i>
            </div>
            <div class="summary-info">
              <div class="summary-value">5,321</div>
              <div class="summary-label">正常处理数</div>
            </div>
          </div>
          <div class="summary-item">
            <div class="summary-icon" style="background: #fff7e6; color: #fa8c14">
              <i class="fas fa-exclamation-circle"></i>
            </div>
            <div class="summary-info">
              <div class="summary-value">104</div>
              <div class="summary-label">异常数据数</div>
            </div>
          </div>
          <div class="summary-item">
            <div class="summary-icon" style="background: #f0f0f0; color: #595959">
              <i class="fas fa-clock"></i>
            </div>
            <div class="summary-info">
              <div class="summary-value">1.8%</div>
              <div class="summary-label">异常率</div>
            </div>
          </div>
        </div>

        <!-- 处理记录表格 -->
        <el-table :data="recentRecords" style="width:100%" border>
          <el-table-column label="执行时间" width="180">
            <template #default="scope">
              <div class="time-cell">
                <div class="time-date">{{ scope.row.date }}</div>
                <div class="time-clock">{{ scope.row.time }}</div>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="任务类型" width="120">
            <template #default="scope">
              <el-tag size="small" :type="scope.row.type === '定时清洗' ? 'info' : ''">{{ scope.row.type }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="数据来源" width="150">
            <template #default="scope">
              <div class="data-source-cell">
                <i :class="scope.row.sourceIcon"></i> {{ scope.row.source }}
              </div>
            </template>
          </el-table-column>
          <el-table-column label="处理条数" width="100">
            <template #default="scope">{{ scope.row.processed }}</template>
          </el-table-column>
          <el-table-column label="异常条数" width="100">
            <template #default="scope">
              <span :class="{ 'warning': scope.row.errors > 0 }">{{ scope.row.errors }}</span>
            </template>
          </el-table-column>
          <el-table-column label="执行状态" width="120">
            <template #default="scope">
              <el-tag size="small" :type="scope.row.statusType" class="status-tag">
                <i :class="scope.row.statusIcon"></i> {{ scope.row.status }}
              </el-tag>
            </template>
          </el-table-column>
        </el-table>

        <!-- 分页 -->
        <div style="margin-top:16px; display:flex; justify-content:flex-end;">
          <el-pagination
            background
            layout="prev, pager, next, sizes, jumper"
            :total="128"
            v-model:current-page="recordPage"
            v-model:page-size="recordPageSize"
            :page-sizes="[5, 10, 20]">
          </el-pagination>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'DataCleaningTools',
  data() {
    return {
      cleaningRules: {
        fieldValidation: '',
        timeFormat: '',
        statusMapping: '',
        outlierFilter: '',
        textCleaning: '',
        duplicateHandling: '',
        executionType: 'schedule',
        scheduleFrequency: 'daily',
        scheduleTime: '02:00',
        scheduleDay: '1'
      },
      // 使用数组存储选中的数据源，便于 v-model 绑定
      selectedDataSources: ['interactionLogs', 'robotStatus', 'userFeedback'],
      dataSourcesMap: {
        interactionLogs: '交互日志',
        robotStatus: '机器人状态',
        userFeedback: '用户反馈',
        taskExecution: '任务执行记录',
        performance: '性能指标',
        errorLogs: '错误日志'
      },
      recentRecords: [
        {
          date: '2026-01-25',
          time: '14:30:22',
          type: '定时清洗',
          source: '机器人状态',
          sourceIcon: 'fas fa-microchip',
          processed: 1240,
          errors: 12,
          status: '完成',
          statusType: 'success',
          statusIcon: 'fas fa-check'
        },
        {
          date: '2026-01-25',
          time: '13:10:45',
          type: '手动触发',
          source: '交互日志',
          sourceIcon: 'fas fa-comments',
          processed: 3560,
          errors: 87,
          status: '存在异常',
          statusType: 'warning',
          statusIcon: 'fas fa-exclamation'
        },
        {
          date: '2026-01-25',
          time: '10:15:33',
          type: '定时清洗',
          source: '用户反馈',
          sourceIcon: 'fas fa-star',
          processed: 625,
          errors: 5,
          status: '完成',
          statusType: 'success',
          statusIcon: 'fas fa-check'
        }
      ],
      recordPage: 1,
      recordPageSize: 5
    }
  },
  computed: {
    selectedDataSourcesCount() {
      return this.selectedDataSources.length
    }
  },
  methods: {
    saveCleaningRules() {
      this.$message.success('数据清洗规则已保存')
    },
    testCleaningRules() {
      this.$message.info('测试规则功能正在执行...')
    },
    executeCleaningNow() {
      this.$message.info('正在执行数据清洗...')
    },
    refreshRecords() {
      this.$message.info('正在刷新处理记录...')
    },
    showAllRecords() {
      this.$message.info('跳转到完整记录页面...')
    },
    calculateNextExecutionTime() {
      const now = new Date()
      const next = new Date()
      const freq = this.cleaningRules.scheduleFrequency

      if (freq === 'hourly') {
        next.setHours(now.getHours() + 1, 0, 0, 0)
      } else if (freq === 'daily') {
        const [hours, minutes] = this.cleaningRules.scheduleTime.split(':')
        next.setDate(now.getDate() + 1)
        next.setHours(parseInt(hours), parseInt(minutes), 0, 0)
      } else if (freq === 'weekly') {
        const targetDay = parseInt(this.cleaningRules.scheduleDay)
        const currentDay = now.getDay()
        const daysToAdd = (targetDay - currentDay + 7) % 7 || 7
        next.setDate(now.getDate() + daysToAdd)
        next.setHours(2, 0, 0, 0)
      }
      return next.toLocaleString('zh-CN', {
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit',
        hour12: false
      })
    },
    selectAllSources() {
      const allKeys = Object.keys(this.dataSourcesMap)
      if (this.selectedDataSources.length === allKeys.length) {
        this.selectedDataSources = []
      } else {
        this.selectedDataSources = allKeys
      }
    }
  }
}
</script>

<style scoped>
/* 继承模板全局样式，并补充局部调整 */
.data-cleaning-tools {
  background: transparent;
  color: #4D4D4D;
  font-size: 14px;
}

/* 卡片复用模板样式，已在外部定义，这里只补充内部细节 */
.card {
  background: white;
  border-radius: 10px;
  box-shadow: 0px 2px 2px rgba(0, 0, 0, 0.5);
  margin-bottom: 24px;
  overflow: hidden;
}

.card-header {
  padding: 16px 20px;
  border-bottom: 1px solid #E5E7EB;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-title {
  font-size: 14px;
  font-weight: 600;
  color: #000;
}

.card-body {
  padding: 24px 20px;
}

/* 两列表单布局 */
.two-column-form {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
  margin-bottom: 20px;
}

@media (max-width: 768px) {
  .two-column-form {
    grid-template-columns: 1fr;
  }
}

.form-row.horizontal {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
}

.form-row.horizontal label {
  width: 140px;
  font-size: 14px;
  font-weight: 500;
  color: #4D4D4D;
  text-align: right;
  flex-shrink: 0;
}

.form-row.horizontal .el-select {
  flex: 1;
}

/* 执行方式区域 */
.time-config-section {
  margin: 24px 0;
  padding: 20px;
  background-color: #FAFAFA;
  border-radius: 6px;
}

.time-config-section h4 {
  font-size: 16px;
  font-weight: 600;
  color: #4D4D4D;
  margin-bottom: 16px;
}

.execution-options {
  display: flex;
  gap: 16px;
  margin-bottom: 16px;
}

.execution-options .el-radio-button {
  flex: 1;
}

.execution-options .el-radio-button__inner {
  width: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}

.option-details {
  margin-top: 16px;
  padding-top: 16px;
  border-top: 1px solid #E5E7EB;
}

.detail-row.combined {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 16px;
  margin-top: 16px;
  padding: 16px;
  background-color: #FAFAFA;
  border-radius: 6px;
  border: 1px solid #E5E7EB;
}

.detail-item {
  display: flex;
  align-items: center;
  gap: 8px;
}

.detail-item label {
  font-size: 14px;
  color: #808080;
  white-space: nowrap;
}

/* 数据源区域 */
.data-source-section {
  margin: 24px 0;
  padding: 20px;
  background-color: #FAFAFA;
  border-radius: 6px;
}

.data-source-section h4 {
  font-size: 16px;
  font-weight: 600;
  color: #4D4D4D;
  margin-bottom: 16px;
}

.simple-checkbox-group {
  display: flex;
  flex-wrap: wrap;
  gap: 12px 24px;
  margin-bottom: 12px;
}

.simple-checkbox-group .el-checkbox {
  margin-right: 0;
}

.selected-count {
  font-size: 14px;
  color: #808080;
  padding-top: 12px;
  border-top: 1px solid #E5E7EB;
}

/* 按钮组 */
.action-buttons {
  display: flex;
  gap: 12px;
  margin-top: 24px;
  padding-top: 20px;
  border-top: 1px solid #E5E7EB;
}

/* 统计数据卡片 */
.records-summary {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  margin-bottom: 24px;
}

.summary-item {
  display: flex;
  align-items: center;
  padding: 16px;
  background-color: #FAFAFA;
  border-radius: 8px;
  border: 1px solid #E5E7EB;
}

.summary-icon {
  width: 48px;
  height: 48px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 16px;
  font-size: 20px;
}

.summary-info {
  flex: 1;
}

.summary-value {
  font-size: 24px;
  font-weight: 600;
  color: #4D4D4D;
  line-height: 1.2;
}

.summary-label {
  font-size: 12px;
  color: #808080;
}

/* 表格内特殊样式 */
.time-cell {
  display: flex;
  flex-direction: column;
}

.time-date {
  font-weight: 500;
  color: #4D4D4D;
}

.time-clock {
  font-size: 12px;
  color: #808080;
}

.data-source-cell {
  display: flex;
  align-items: center;
  gap: 8px;
}

.data-source-cell i {
  color: #3976E4;
  font-size: 14px;
}

.warning {
  color: #FEB03B;
  font-weight: 500;
}

.status-tag i {
  margin-right: 4px;
}

/* 响应式 */
@media (max-width: 1200px) {
  .detail-row.combined {
    flex-direction: column;
    align-items: stretch;
  }

  .detail-item {
    flex-direction: column;
    align-items: flex-start;
    width: 100%;
  }

  .detail-item .el-select,
  .detail-item .el-time-picker {
    width: 100% !important;
  }
}

@media (max-width: 768px) {
  .form-row.horizontal {
    flex-direction: column;
    align-items: flex-start;
  }

  .form-row.horizontal label {
    width: 100%;
    text-align: left;
    margin-bottom: 6px;
  }

  .form-row.horizontal .el-select {
    width: 100%;
  }

  .execution-options {
    flex-direction: column;
  }

  .records-summary {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 576px) {
  .records-summary {
    grid-template-columns: 1fr;
  }

  .action-buttons {
    flex-direction: column;
  }
}
</style>
