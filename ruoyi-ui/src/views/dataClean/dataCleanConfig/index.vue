<template>
  <div class="data-cleaning-tools">
    <!-- 规则配置卡片 -->
    <div class="card">
      <div class="card-body">
        <div class="form-section">
          <div class="section-header" style="display: flex; align-items: center; gap: 8px; margin-bottom: 16px;">
            <h3 style="margin: 0;">数据清洗规则配置</h3>
            <el-tooltip placement="right" effect="light">
              <div slot="content" style="max-width: 300px;">
                配置数据清洗规则，对机器人采集的交互日志、状态与用户反馈数据进行标准化处理，为后续分析提供高质量数据。
              </div>
              <i class="fas fa-question-circle" style="color: #1890ff; cursor: pointer;"></i>
            </el-tooltip>
          </div>

          <!-- 两列表单（水平布局） -->
          <div class="two-column-form">
            <div class="form-column">
              <div class="form-row horizontal">
                <label>字段完整性校验</label>
                <select v-model="cleaningRules.fieldValidation">
                  <option value="" selected>请选择</option>
                  <option value="mark">缺失字段标记异常</option>
                  <option value="discard">缺失字段自动丢弃</option>
                  <option value="fill">缺失字段填充默认值</option>
                </select>
              </div>
              <div class="form-row horizontal">
                <label>时间格式统一</label>
                <select v-model="cleaningRules.timeFormat">
                  <option value="" selected>请选择</option>
                  <option value="iso8601">ISO 8601</option>
                  <option value="timestamp_ms">时间戳（毫秒）</option>
                  <option value="timestamp_s">时间戳（秒）</option>
                </select>
              </div>
              <div class="form-row horizontal">
                <label>状态码映射规则</label>
                <select v-model="cleaningRules.statusMapping">
                  <option value="" selected>请选择</option>
                  <option value="enum">映射为平台统一枚举</option>
                  <option value="original">保留原始值</option>
                  <option value="custom">自定义映射表</option>
                </select>
              </div>
            </div>

            <div class="form-column">
              <div class="form-row horizontal">
                <label>异常值过滤策略</label>
                <select v-model="cleaningRules.outlierFilter">
                  <option value="" selected>请选择</option>
                  <option value="mark">标记异常</option>
                  <option value="filter">直接过滤</option>
                  <option value="correct">使用统计方法修正</option>
                </select>
              </div>
              <div class="form-row horizontal">
                <label>文本数据清洗</label>
                <select v-model="cleaningRules.textCleaning">
                  <option value="" selected>请选择</option>
                  <option value="html">去除HTML标签</option>
                  <option value="special">去除特殊字符</option>
                  <option value="original">保留原始文本</option>
                </select>
              </div>
              <div class="form-row horizontal">
                <label>重复数据处理</label>
                <select v-model="cleaningRules.duplicateHandling">
                  <option value="" selected>请选择</option>
                  <option value="keep_first">保留第一条</option>
                  <option value="keep_last">保留最后一条</option>
                  <option value="remove_all">删除所有重复</option>
                </select>
              </div>
            </div>
          </div>

          <!-- 时间配置（执行方式） -->
          <div class="time-config-section">
            <h4>执行方式</h4>
            <div class="execution-options">
              <div class="execution-option">
                <input
                  type="radio"
                  id="scheduleAuto"
                  name="executionType"
                  value="schedule"
                  v-model="cleaningRules.executionType"
                />
                <label for="scheduleAuto">
                  <i class="fas fa-clock"></i>
                  <span>定时清洗</span>
                </label>

                <div class="option-details" v-if="cleaningRules.executionType === 'schedule'">
                  <div class="detail-row combined">
                    <div class="detail-item">
                      <label>执行频率</label>
                      <select v-model="cleaningRules.scheduleFrequency">
                        <option value="" selected>请选择</option>
                        <option value="hourly">每小时</option>
                        <option value="daily">每天</option>
                        <option value="weekly">每周</option>
                        <option value="monthly">每月</option>
                      </select>
                    </div>

                    <!-- 每天的执行时间 -->
                    <div class="detail-item" v-if="cleaningRules.scheduleFrequency === 'daily'">
                      <label>执行时间</label>
                      <input type="time" v-model="cleaningRules.scheduleTime" />
                    </div>

                    <!-- 每周的执行星期 -->
                    <div class="detail-item" v-if="cleaningRules.scheduleFrequency === 'weekly'">
                      <label>执行星期</label>
                      <select v-model="cleaningRules.scheduleDay">
                        <option value="" selected>请选择</option>
                        <option value="1">星期一</option>
                        <option value="2">星期二</option>
                        <option value="3">星期三</option>
                        <option value="4">星期四</option>
                        <option value="5">星期五</option>
                        <option value="6">星期六</option>
                        <option value="0">星期日</option>
                      </select>
                    </div>
                  </div>
                </div>
              </div>

              <div class="execution-option">
                <input
                  type="radio"
                  id="manualTrigger"
                  name="executionType"
                  value="manual"
                  v-model="cleaningRules.executionType"
                />
                <label for="manualTrigger">
                  <i class="fas fa-play-circle"></i>
                  <span>手动触发</span>
                </label>
              </div>
            </div>
          </div>

          <!-- 应用数据源（复选框） -->
          <div class="data-source-section">
            <h4>应用数据源</h4>
            <div class="simple-checkbox-group">
              <div class="simple-checkbox">
                <input type="checkbox" id="interaction" v-model="dataSources.interactionLogs" checked />
                <label for="interaction">交互日志</label>
              </div>
              <div class="simple-checkbox">
                <input type="checkbox" id="status" v-model="dataSources.robotStatus" checked />
                <label for="status">机器人状态</label>
              </div>
              <div class="simple-checkbox">
                <input type="checkbox" id="feedback" v-model="dataSources.userFeedback" checked />
                <label for="feedback">用户反馈</label>
              </div>
              <div class="simple-checkbox">
                <input type="checkbox" id="task" v-model="dataSources.taskExecution" />
                <label for="task">任务执行记录</label>
              </div>
              <div class="simple-checkbox">
                <input type="checkbox" id="performance" v-model="dataSources.performance" />
                <label for="performance">性能指标</label>
              </div>
              <div class="simple-checkbox">
                <input type="checkbox" id="error" v-model="dataSources.errorLogs" />
                <label for="error">错误日志</label>
              </div>
            </div>
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
              type="success"
              @click="executeCleaningNow"
              v-if="cleaningRules.executionType === 'manual'"
            >
              <i class="fas fa-play"></i> 立即执行
            </el-button>
          </div>
        </div>
      </div>
    </div>

    <!-- 最近处理记录卡片 -->
    <div class="card">
      <div class="card-body">
        <div class="form-section">
          <div class="section-header">
            <h3>最近处理记录</h3>
            <div class="section-actions">
              <el-button size="small" @click="refreshRecords">
                <i class="fas fa-sync-alt"></i> 刷新
              </el-button>
              <el-button size="small" type="primary" @click="showAllRecords">
                <i class="fas fa-list"></i> 查看全部
              </el-button>
            </div>
          </div>

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
          <div class="compact-table-container">
            <table class="compact-table">
              <thead>
              <tr>
                <th width="160">执行时间</th>
                <th width="100">任务类型</th>
                <th width="120">数据来源</th>
                <th width="100">处理条数</th>
                <th width="100">异常条数</th>
                <th width="100">执行状态</th>
              </tr>
              </thead>
              <tbody>
              <tr>
                <td>
                  <div class="time-cell">
                    <div class="time-date">2026-01-25</div>
                    <div class="time-clock">14:30:22</div>
                  </div>
                </td>
                <td><el-tag size="small" type="info">定时清洗</el-tag></td>
                <td>
                  <div class="data-source-cell">
                    <i class="fas fa-microchip"></i> 机器人状态
                  </div>
                </td>
                <td><div class="count-cell">1,240</div></td>
                <td><div class="count-cell warning">12</div></td>
                <td>
                  <el-tag size="small" type="success" class="status-tag">
                    <i class="fas fa-check"></i> 完成
                  </el-tag>
                </td>
              </tr>
              <tr>
                <td>
                  <div class="time-cell">
                    <div class="time-date">2026-01-25</div>
                    <div class="time-clock">13:10:45</div>
                  </div>
                </td>
                <td><el-tag size="small">手动触发</el-tag></td>
                <td>
                  <div class="data-source-cell">
                    <i class="fas fa-comments"></i> 交互日志
                  </div>
                </td>
                <td><div class="count-cell">3,560</div></td>
                <td><div class="count-cell warning">87</div></td>
                <td>
                  <el-tag size="small" type="warning" class="status-tag">
                    <i class="fas fa-exclamation"></i> 存在异常
                  </el-tag>
                </td>
              </tr>
              <tr>
                <td>
                  <div class="time-cell">
                    <div class="time-date">2026-01-25</div>
                    <div class="time-clock">10:15:33</div>
                  </div>
                </td>
                <td><el-tag size="small" type="info">定时清洗</el-tag></td>
                <td>
                  <div class="data-source-cell">
                    <i class="fas fa-star"></i> 用户反馈
                  </div>
                </td>
                <td><div class="count-cell">625</div></td>
                <td><div class="count-cell warning">5</div></td>
                <td>
                  <el-tag size="small" type="success" class="status-tag">
                    <i class="fas fa-check"></i> 完成
                  </el-tag>
                </td>
              </tr>
              </tbody>
            </table>

            <!-- 表格底部（分页） -->
            <div class="table-footer">
              <div class="pagination-info">显示 3 条记录，共 128 条</div>
              <div class="pagination">
                <el-button size="small" disabled>
                  <i class="fas fa-chevron-left"></i>
                </el-button>
                <el-button size="small" class="active">1</el-button>
                <el-button size="small">2</el-button>
                <el-button size="small">3</el-button>
                <el-button size="small">
                  <i class="fas fa-chevron-right"></i>
                </el-button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
// 如果使用 Element UI，无需单独导入 ElMessage，通过 this.$message 调用
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
        executionType: 'schedule', // 默认定时
        scheduleFrequency: 'daily',
        scheduleTime: '02:00',
        scheduleDay: '1'
      },
      dataSources: {
        interactionLogs: true,
        robotStatus: true,
        userFeedback: true,
        taskExecution: false,
        performance: true,
        errorLogs: false
      }
    }
  },
  computed: {
    selectedDataSourcesCount() {
      return Object.values(this.dataSources).filter(Boolean).length
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
      const allChecked = this.selectedDataSourcesCount === Object.keys(this.dataSources).length
      Object.keys(this.dataSources).forEach(key => {
        this.$set(this.dataSources, key, !allChecked)
      })
    }
  }
}
</script>

<style scoped>
/* 从原全局样式中提取的相关样式，做必要调整 */
* {
  box-sizing: border-box;
}

.data-cleaning-tools {
  background-color: #f0f2f5;
  color: #000000e0;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial,
  sans-serif;
  padding: 24px;
}

.page-title {
  font-size: 24px;
  font-weight: 600;
  color: #000000e0;
  margin-bottom: 8px;
}

.page-desc {
  font-size: 14px;
  color: #000000a6;
  margin-bottom: 24px;
  line-height: 1.5;
}

.card {
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  margin-bottom: 24px;
  overflow: hidden;
}

.card-body {
  padding: 24px;
}

.form-section h3 {
  font-size: 16px;
  font-weight: 600;
  color: #000000e0;
  margin-bottom: 16px;
  padding-bottom: 8px;
  border-bottom: 1px solid #f0f0f0;
}

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
  color: #000000e0;
  text-align: right;
  margin-bottom: 0;
}

.form-row.horizontal select {
  flex: 1;
  padding: 8px 12px;
  border: 1px solid #f0f0f0;
  border-radius: 4px;
  background-color: white;
  font-size: 14px;
  outline: none;
  transition: border-color 0.3s;
}

.form-row.horizontal select:focus {
  border-color: #1890ff;
}

.time-config-section {
  margin: 24px 0;
  padding: 20px;
  background-color: #f9f9f9;
  border-radius: 6px;
}

.time-config-section h4 {
  font-size: 16px;
  font-weight: 600;
  color: #000000e0;
  margin-bottom: 16px;
}

.execution-options {
  display: flex;
  gap: 16px;
  margin-bottom: 16px;
}

.execution-option {
  flex: 1;
}

.execution-option input[type='radio'] {
  display: none;
}

.execution-option label {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 16px;
  border: 1px solid #f0f0f0;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.2s;
  background-color: white;
}

.execution-option input[type='radio']:checked + label {
  border-color: #1890ff;
  background-color: #e6f7ff;
}

.execution-option label i {
  color: #1890ff;
  font-size: 16px;
}

.option-details {
  margin-top: 16px;
  padding-top: 16px;
  border-top: 1px solid #f0f0f0;
}

.detail-row.combined {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 16px;
  margin-top: 16px;
  padding: 16px;
  background-color: #f9f9f9;
  border-radius: 6px;
  border: 1px solid #f0f0f0;
}

.detail-item {
  display: flex;
  align-items: center;
  gap: 8px;
}

.detail-item label {
  font-size: 14px;
  color: #000000a6;
  white-space: nowrap;
}

.detail-item select,
.detail-item input[type='time'] {
  padding: 6px 10px;
  border: 1px solid #f0f0f0;
  border-radius: 4px;
  font-size: 14px;
  width: 160px;
}

.data-source-section {
  margin: 24px 0;
  padding: 20px;
  background-color: #f9f9f9;
  border-radius: 6px;
}

.data-source-section h4 {
  font-size: 16px;
  font-weight: 600;
  color: #000000e0;
  margin-bottom: 16px;
}

.simple-checkbox-group {
  display: flex;
  flex-wrap: wrap;
  gap: 12px 24px;
  margin-bottom: 12px;
}

.simple-checkbox {
  display: flex;
  align-items: center;
  gap: 6px;
}

.simple-checkbox input[type='checkbox'] {
  width: 16px;
  height: 16px;
}

.simple-checkbox label {
  font-size: 14px;
  color: #000000e0;
  cursor: pointer;
}

.selected-count {
  font-size: 14px;
  color: #000000a6;
  padding-top: 12px;
  border-top: 1px solid #f0f0f0;
}

.action-buttons {
  display: flex;
  gap: 12px;
  margin-top: 24px;
  padding-top: 20px;
  border-top: 1px solid #f0f0f0;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

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
  background-color: #fafafa;
  border-radius: 8px;
  border: 1px solid #f0f0f0;
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
  color: #000000e0;
  line-height: 1.2;
}

.summary-label {
  font-size: 12px;
  color: #000000a6;
}

.compact-table-container {
  border: 1px solid #f0f0f0;
  border-radius: 6px;
  overflow: hidden;
}

.compact-table {
  width: 100%;
  border-collapse: collapse;
  font-size: 13px;
}

.compact-table thead {
  background-color: #fafafa;
}

.compact-table th {
  padding: 12px 16px;
  text-align: left;
  font-weight: 500;
  color: #000000e0;
  border-bottom: 1px solid #f0f0f0;
}

.compact-table td {
  padding: 12px 16px;
  border-bottom: 1px solid #f0f0f0;
}

.compact-table tbody tr:hover {
  background-color: #fafafa;
}

.time-cell {
  display: flex;
  flex-direction: column;
}

.time-date {
  font-weight: 500;
  color: #000000e0;
}

.time-clock {
  font-size: 12px;
  color: #000000a6;
}

.data-source-cell {
  display: flex;
  align-items: center;
  gap: 8px;
}

.data-source-cell i {
  color: #1890ff;
  font-size: 14px;
}

.count-cell {
  font-weight: 500;
  color: #000000e0;
}

.count-cell.warning {
  color: #faad14;
}

.status-tag {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  font-weight: 500;
}

.table-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px;
  background-color: #fafafa;
  border-top: 1px solid #f0f0f0;
}

.pagination-info {
  font-size: 12px;
  color: #000000a6;
}

.pagination {
  display: flex;
  gap: 4px;
}

.pagination .el-button.active {
  background-color: #1890ff;
  color: white;
  border-color: #1890ff;
}

/* 响应式调整 */
@media (max-width: 1200px) {
  .two-column-form {
    grid-template-columns: 1fr;
  }

  .detail-row.combined {
    flex-direction: column;
    align-items: stretch;
  }

  .detail-item {
    flex-direction: column;
    align-items: flex-start;
    width: 100%;
  }

  .detail-item select,
  .detail-item input[type='time'] {
    width: 100%;
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

  .form-row.horizontal select {
    width: 100%;
    min-width: auto;
  }

  .execution-options {
    flex-direction: column;
  }

  .records-summary {
    grid-template-columns: repeat(2, 1fr);
  }

  .table-footer {
    flex-direction: column;
    gap: 12px;
    align-items: flex-start;
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
