<template>
  <div class="data-cleaning-tools">

    <!-- ===== 配置卡片 ===== -->
    <div class="glass-card">
      <div class="card-header">
        <h3>数据清洗规则配置</h3>
      </div>

      <div class="card-body">

        <!-- 两列（增加“保持原样”选项） -->
        <div class="two-column">
          <div class="column">
            <div class="form-item">
              <label>字段完整性校验</label>
              <el-select v-model="cleaningRules.fieldValidation" placeholder="请选择">
                <el-option label="保持原样" value="keep"/>
                <el-option label="缺失字段标记异常" value="mark"/>
                <el-option label="自动丢弃" value="discard"/>
                <el-option label="填充默认值" value="fill"/>
              </el-select>
            </div>

            <div class="form-item">
              <label>时间格式统一</label>
              <el-select v-model="cleaningRules.timeFormat" placeholder="请选择">
                <el-option label="保持原样" value="keep"/>
                <el-option label="ISO8601" value="iso"/>
                <el-option label="时间戳(毫秒)" value="ms"/>
              </el-select>
            </div>
          </div>

          <div class="column">
            <div class="form-item">
              <label>异常值处理</label>
              <el-select v-model="cleaningRules.outlierFilter" placeholder="请选择">
                <el-option label="保持原样" value="keep"/>
                <el-option label="标记异常" value="mark"/>
                <el-option label="过滤" value="filter"/>
              </el-select>
            </div>

            <div class="form-item">
              <label>重复数据处理</label>
              <el-select v-model="cleaningRules.duplicateHandling" placeholder="请选择">
                <el-option label="保持原样" value="keep"/>
                <el-option label="保留第一条" value="first"/>
                <el-option label="删除重复" value="remove"/>
              </el-select>
            </div>
          </div>
        </div>

        <!-- 新增：文本格式处理 + 状态统一映射 -->
        <div class="two-column">
          <div class="column">
            <div class="form-item">
              <label>文本格式处理</label>
              <el-select v-model="cleaningRules.textCleaning" placeholder="请选择">
                <el-option label="移除HTML标签" value="REMOVE_HTML"/>
                <el-option label="移除特殊字符" value="REMOVE_SPECIAL_CHAR"/>
                <el-option label="保持原样" value="KEEP_ORIGINAL"/>
              </el-select>
            </div>
          </div>

          <div class="column">
            <div class="form-item">
              <label>状态统一映射</label>
              <el-select v-model="cleaningRules.statusMapping" placeholder="请选择">
                <el-option label="保持原样" value="KEEP_ORIGINAL"/>
                <el-option label="映射到平台枚举" value="MAP_TO_PLATFORM_ENUM"/>
              </el-select>
            </div>
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
                <el-option label="每日" value="daily"/>
                <el-option label="每周" value="weekly"/>
                <el-option label="每月" value="monthly"/>
              </el-select>
            </div>

            <div class="schedule-row" v-if="scheduleConfig.period === 'weekly'">
              <div class="schedule-label">星期几</div>
              <el-select v-model="scheduleConfig.weekDay" placeholder="请选择">
                <el-option label="周一" value="1"/>
                <el-option label="周二" value="2"/>
                <el-option label="周三" value="3"/>
                <el-option label="周四" value="4"/>
                <el-option label="周五" value="5"/>
                <el-option label="周六" value="6"/>
                <el-option label="周日" value="0"/>
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

        <!-- 按钮 -->
        <div class="actions">
          <el-button type="primary">保存配置</el-button>
          <el-button>测试规则</el-button>
          <el-button type="success" v-if="cleaningRules.executionType==='manual'">
            立即执行
          </el-button>
        </div>

      </div>
    </div>

    <!-- ===== 记录卡片 ===== -->
    <div class="glass-card">
      <div class="card-header">
        <h3>最近处理记录</h3>
      </div>

      <div class="card-body">
        <!-- 统计 -->
        <div class="summary">
          <div class="summary-item">
            <div class="num">5425</div>
            <div class="label">今日处理</div>
          </div>

          <div class="summary-item">
            <div class="num green">5321</div>
            <div class="label">成功</div>
          </div>

          <div class="summary-item">
            <div class="num orange">104</div>
            <div class="label">异常</div>
          </div>
        </div>

        <!-- 表格 -->
        <el-table :data="tableData" class="glass-table">
          <el-table-column prop="time" label="时间"/>
          <el-table-column prop="type" label="类型"/>
          <el-table-column prop="count" label="数量"/>
          <el-table-column prop="status" label="状态"/>
        </el-table>

      </div>
    </div>

  </div>
</template>

<script>
export default {
  data() {
    return {
      cleaningRules: {
        fieldValidation: '',
        timeFormat: '',
        outlierFilter: '',
        duplicateHandling: '',
        executionType: 'schedule',
        textCleaning: '',      // 默认保持原样
        statusMapping: ''      // 默认保持原样
      },
      scheduleConfig: {
        period: 'daily',      // daily, weekly, monthly
        weekDay: '1',         // 1-7 (周一至周日)
        monthDay: 1,          // 1-31
        time: '00:00'         // HH:mm
      },
      tableData: [
        { time: '2026-01-25 14:30', type: '定时', count: 1200, status: '成功' },
        { time: '2026-01-25 13:10', type: '手动', count: 800, status: '异常' }
      ]
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
</style>
