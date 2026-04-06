<template>
  <div class="data-ai">
    <!-- 分析报告生成区域 -->
    <div class="ai-section">
      <div class="section-header">
        <h3><i class="fas fa-file-alt"></i> AI分析报告 </h3>
      </div>

      <div class="report-generation">
        <div class="generation-form">
          <div class="form-row">
            <label>报告类型</label>
            <el-select v-model="reportForm.reportType" placeholder="选择报告类型">
              <el-option label="服务质量评估报告" value="quality"></el-option>
              <el-option label="用户满意度分析报告" value="satisfaction"></el-option>
              <el-option label="机器人性能分析报告" value="performance"></el-option>
              <el-option label="异常趋势分析报告" value="exception"></el-option>
            </el-select>
          </div>

          <div class="form-row">
            <label>时间范围</label>
            <el-date-picker
              v-model="reportForm.dateRange"
              type="daterange"
              range-separator="至"
              start-placeholder="开始日期"
              end-placeholder="结束日期"
            ></el-date-picker>
          </div>

          <div class="form-row">
            <label>分析维度</label>
            <el-select v-model="reportForm.analysisDimension" placeholder="选择分析维度">
              <el-option label="用户满意度" value="user_satisfaction"></el-option>
              <el-option label="任务完成率" value="task_completion_rate"></el-option>
              <el-option label="异常率" value="exception_rate"></el-option>
            </el-select>
          </div>

          <div class="form-row">
            <label>自定义要求</label>
            <div class="natural-language-input">
              <el-input
                v-model="reportForm.customPrompt"
                type="textarea"
                :rows="3"
                placeholder="请输入您的具体要求，例如：重点关注华东地区的服务质量，分析周末和工作日的差异，提供改进建议..."
                maxlength="500"
                show-word-limit
              ></el-input>
              <div class="input-hint">
                <i class="fas fa-lightbulb"></i> 您可以在此输入自然语言描述您的分析需求，AI会理解并生成相应报告
              </div>
            </div>
          </div>

          <div class="form-row">
            <label>报告深度</label>
            <el-radio-group v-model="reportForm.reportDepth">
              <el-radio label="summary">摘要版</el-radio>
              <el-radio label="standard">标准版</el-radio>
            </el-radio-group>
          </div>

          <div class="form-actions">
            <el-button type="primary" @click="generateReport" :loading="generating">
              <i class="fas fa-play"></i>
              开始生成报告
            </el-button>
            <el-button @click="resetReportForm">
              <i class="fas fa-undo"></i>
              重置
            </el-button>
          </div>
        </div>

        <div class="report-preview">
          <div class="preview-header">
            <h4>报告预览</h4>
            <span class="preview-status" :class="previewStatusClass">{{ previewStatusText }}</span>
          </div>
          <!-- ===== 报告预览（已改 Markdown）===== -->
          <div class="preview-content" v-if="generatedReportContent">
            <div class="preview-card">
              <div class="preview-summary">
                <h5>生成结果</h5>
                <!-- ⭐关键：Markdown渲染 -->
                <div class="markdown-body" v-html="renderedPreviewContent"></div>
              </div>
            </div>
          </div>
          <div class="no-preview" v-else>
            <i class="fas fa-file-import"></i>
            <p>选择参数并生成报告后，将在此处显示预览</p>
          </div>
        </div>
      </div>

      <!-- 历史报告列表（带分页） -->
      <div class="historical-reports">
        <div class="reports-header">
          <h4>历史报告列表</h4>
          <el-input
            v-model="reportQuery.reportName"
            placeholder="请输入报告名称"
            size="small"
            style="width: 200px;"
            clearable
            @clear="loadReports"
            @keyup.enter.native="loadReports"
          >
            <template #prefix>
              <i class="fas fa-search"></i>
            </template>
          </el-input>
        </div>

        <div class="reports-list">
          <el-table :data="reports" style="width: 100%" v-loading="loadingReports">
            <el-table-column prop="reportName" label="报告名称" width="360">
              <template #default="scope">
                <div class="report-title-cell">
                  <i class="fas fa-file-alt" :style="{color: scope.row.typeColor || '#409EFF'}"></i>
                  <span>{{ scope.row.reportName }}</span>
                </div>
              </template>
            </el-table-column>
            <el-table-column prop="reportType" label="类型" width="120">
              <template #default="scope">
                {{ getReportTypeLabel(scope.row.reportType) }}
              </template>
            </el-table-column>
            <el-table-column prop="createdAt" label="生成时间" width="180">
              <template #default="scope">
                {{ formatDateTime(scope.row.createdAt) }}
              </template>
            </el-table-column>
            <el-table-column label="状态" width="100">
              <template #default="scope">
                <el-tag :type="getStatusTagType(scope.row.status)" size="small">
                  {{ getStatusText(scope.row.status) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="260">
              <template #default="scope">
                <el-button size="small" @click="viewReportDetail(scope.row)">查看</el-button>
                <el-button size="small" @click="downloadReportById(scope.row.id)">下载</el-button>
                <el-button size="small" type="danger" @click="deleteReport(scope.row.id)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>

          <!-- 分页组件 -->
          <el-pagination
            background
            layout="total, sizes, prev, pager, next, jumper"
            :total="total"
            :page-size="reportQuery.size"
            :current-page="reportQuery.page"
            :page-sizes="[10, 20, 50, 100]"
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
            style="margin-top: 16px; text-align: right;"
          />
        </div>
      </div>
    </div>

    <!-- 问题智能分类展示（保持不变） -->
    <div class="ai-section">
      <div class="section-header">
        <h3><i class="fas fa-tags"></i> 问题智能分类</h3>
        <div class="section-subtitle">AI自动识别和分类的问题类型分布</div>
      </div>

      <div class="problem-classification">
        <div class="classification-list full-width">
          <div class="list-header">
            <h4>分类详情</h4>
            <el-button size="small" @click="exportClassifications">
              <i class="fas fa-download"></i> 导出
            </el-button>
          </div>

          <div class="list-content">
            <el-table :data="problemCategories" style="width: 100%" height="300" v-loading="loadingCategories">
              <el-table-column label="分类" width="180">
                <template #default="scope">
                  <div class="category-cell">
                    <span class="category-color" :style="{backgroundColor: scope.row.color}"></span>
                    <span class="category-name">{{ scope.row.name }}</span>
                  </div>
                </template>
              </el-table-column>
              <el-table-column prop="count" label="问题数量" width="150">
                <template #default="scope">
                  <el-tag size="small">{{ scope.row.count }}</el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="percentage" label="占比" width="150">
                <template #default="scope">
                  {{ scope.row.percentage }}%
                </template>
              </el-table-column>
<!--              <el-table-column label="趋势" width="120">-->
<!--                <template #default="scope">-->
<!--                  <div class="trend-cell" :class="scope.row.trend > 0 ? 'up' : scope.row.trend < 0 ? 'down' : 'stable'">-->
<!--                    <i class="fas" :class="getTrendIcon(scope.row.trend)"></i>-->
<!--                    <span>{{ scope.row.trend > 0 ? '+' : '' }}{{ scope.row.trend }}%</span>-->
<!--                  </div>-->
<!--                </template>-->
<!--              </el-table-column>-->
              <el-table-column label="处理建议">
                <template #default="scope">
                  <div class="suggestion-cell">
                    {{ scope.row.suggestion }}
                  </div>
                </template>
              </el-table-column>
              <el-table-column label="操作" width="280">
                <template #default="scope">
                  <el-button size="small" @click="viewCategoryDetail(scope.row)">
                    详情
                  </el-button>
                </template>
              </el-table-column>
            </el-table>
          </div>
        </div>

        <div class="hot-problems">
          <div class="hot-header">
            <h4>热门问题示例</h4>
            <span class="hot-subtitle">AI识别的高频问题</span>
          </div>

          <div class="hot-content">
            <div class="problem-examples">
              <div class="example-card" v-for="problem in hotProblems" :key="problem.id">
                <div class="example-header">
                  <span class="problem-category" :style="{backgroundColor: problem.color}">
                    {{ problem.category }}
                  </span>
                  <span class="problem-frequency">
                    <i class="fas fa-chart-bar"></i> {{ problem.frequency }}次
                  </span>
                </div>
                <div class="example-body">
                  <div class="problem-text">{{ problem.text }}</div>
                  <div class="problem-meta">
                    <span><i class="fas fa-clock"></i> 最近出现: {{ problem.lastOccurred }}</span>
                    <span><i class="fas fa-user"></i> 影响用户: {{ problem.affectedUsers }}</span>
                  </div>
                </div>
                <div class="example-footer">
                  <el-tag size="small" :type="problem.priority === 'high' ? 'danger' : problem.priority === 'medium' ? 'warning' : ''">
                    {{ problem.priority === 'high' ? '高优先级' : problem.priority === 'medium' ? '中优先级' : '低优先级' }}
                  </el-tag>
<!--                  <el-button size="small" type="text" @click="viewProblemDetail(problem)">-->
<!--                    <i class="fas fa-chevron-right"></i>-->
<!--                  </el-button>-->
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- markdown渲染弹窗（已优化滚动） -->
    <el-dialog
      title="报告详情"
      :visible.sync="detailVisible"
      width="60%"
      :modal="false"
      custom-class="report-detail-dialog"
    >
      <div v-if="reportDetail">
        <!-- ⭐ Markdown正文，由外层滚动条控制 -->
        <div class="markdown-body" v-html="renderedDetailContent"></div>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import {
  deleteReport,
  downloadReport,
  generateReport,
  getIssueDistribution,
  getReportDetail,
  getReportList
} from '@/api/data/ai/ai'
import md from '@/utils/markdown'

export default {
  name: 'AIAnalysis',
  data() {
    return {
      // 报告生成表单（与后端 DTO 一致）
      reportForm: {
        reportType: 'quality',
        dateRange: null,
        analysisDimension: 'user_satisfaction',
        customPrompt: '',
        reportDepth: 'standard'
      },
      generating: false,
      generatedReportContent: '',
      previewStatusClass: '',
      previewStatusText: '等待生成',

      // 报告列表分页参数
      reportQuery: {
        page: 1,
        size: 10,
        reportName: ''
      },
      reports: [],
      total: 0,
      reportDetail: null,
      detailVisible: false,
      loadingReports: false,

      // 问题分类数据
      problemCategories: [],
      hotProblems: [],
      loadingCategories: false
    }
  },
  mounted() {
    this.loadReports()
    this.loadIssueClassification()
  },
  computed: {
    renderedPreviewContent() {
      return md.render(this.generatedReportContent || '')
    },
    renderedDetailContent() {
      return md.render(this.reportDetail?.content || '')
    },
    highlightList() {
      if (!this.reportDetail?.highlights) return []
      try {
        return JSON.parse(this.reportDetail.highlights)
      } catch {
        return this.reportDetail.highlights.split('\n')
      }
    }
  },
  methods: {
    // ==================== 报告生成 ====================
    async generateReport() {
      if (!this.reportForm.dateRange || this.reportForm.dateRange.length !== 2) {
        this.$message.warning('请选择时间范围')
        return
      }

      this.generating = true
      this.previewStatusText = '生成中...'
      this.previewStatusClass = 'generating'

      try {
        const params = {
          reportType: this.reportForm.reportType,
          startDate: this.formatDate(this.reportForm.dateRange[0]),
          endDate: this.formatDate(this.reportForm.dateRange[1]),
          analysisDimension: this.reportForm.analysisDimension,
          customPrompt: this.reportForm.customPrompt,
          reportDepth: this.reportForm.reportDepth
        }

        const res = await generateReport(params)
        if (res.code === 200) {
          this.generatedReportContent = res.msg || '报告生成成功，无返回内容'
          this.previewStatusText = '生成完成'
          this.previewStatusClass = 'completed'
          this.$message.success('报告生成成功')
          this.loadReports()
        } else {
          throw new Error(res.msg || '生成失败')
        }
      } catch (error) {
        this.$message.error(error.message || '报告生成失败')
        this.previewStatusText = '生成失败'
        this.previewStatusClass = 'error'
      } finally {
        this.generating = false
      }
    },

    resetReportForm() {
      this.reportForm = {
        reportType: 'quality',
        dateRange: null,
        analysisDimension: 'user_satisfaction',
        customPrompt: '',
        reportDepth: 'standard'
      }
      this.generatedReportContent = ''
      this.previewStatusText = '等待生成'
      this.previewStatusClass = ''
    },

    getReportTypeLabel(type) {
      const map = {
        'quality': '服务质量',
        'satisfaction': '用户满意度',
        'performance': '机器人性能',
        'exception': '异常趋势',
      }
      return map[type] || type
    },

    getStatusText(status) {
      const map = {
        'success': '已完成',
        'failed': '失败',
        'generating': '生成中'
      }
      return map[status] || '未知'
    },

    getStatusTagType(status) {
      const map = {
        'success': 'success',
        'failed': 'danger',
        'generating': 'warning'
      }
      return map[status] || 'info'
    },

    formatDateTime(dateStr) {
      if (!dateStr) return ''
      const date = new Date(dateStr)
      return date.toLocaleString()
    },

    async loadReports() {
      this.loadingReports = true
      try {
        const res = await getReportList(this.reportQuery)
        if (res.code === 200) {
          const data = res.data || {}
          this.reports = data.rows || data.list || []
          this.total = data.total || 0
        } else {
          throw new Error(res.msg || '获取列表失败')
        }
      } catch (error) {
        this.$message.error(error.message || '加载报告列表失败')
      } finally {
        this.loadingReports = false
      }
    },

    handleSizeChange(size) {
      this.reportQuery.size = size
      this.reportQuery.page = 1
      this.loadReports()
    },

    handleCurrentChange(page) {
      this.reportQuery.page = page
      this.loadReports()
    },

    async viewReportDetail(row) {
      try {
        const res = await getReportDetail(row.id)
        if (res.code === 200) {
          this.reportDetail = res.data
          this.$nextTick(() => {
            this.detailVisible = true
          })
        } else {
          throw new Error(res.msg || '获取详情失败')
        }
      } catch (error) {
        this.$message.error(error.message || '加载报告详情失败')
      }
    },

    downloadReportById(id) {
      downloadReport(id, 'pdf').then(blob => {
        const url = window.URL.createObjectURL(blob)
        const link = document.createElement('a')
        link.href = url
        link.download = `report_${id}.pdf`
        document.body.appendChild(link)
        link.click()
        document.body.removeChild(link)
        window.URL.revokeObjectURL(url)
        this.$message.success('下载已开始')
      }).catch(error => {
        this.$message.error('下载失败')
      })
    },

    async deleteReport(id) {
      try {
        await this.$confirm('确认删除该报告？', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        })
        const res = await deleteReport(id)
        if (res.code === 200 ) {
          this.$message.success('删除成功')
          this.loadReports()
        } else {
          throw new Error(res.msg || '删除失败')
        }
      } catch (error) {
        if (error !== 'cancel') {
          this.$message.error(error.message || '删除失败')
        }
      }
    },

    // ==================== 问题智能分类 ====================
    async loadIssueClassification(timeRange = 'last_30_days') {
      this.loadingCategories = true
      try {
        const res = await getIssueDistribution({ time_range: timeRange })
        if (res.code === 200) {
          const data = res.data || []
          this.problemCategories = data.map((item, index) => ({
            id: index + 1,
            name: item.category || item.name,
            count: item.count,
            percentage: item.percentage,
            trend: item.trend || 0,
            color: item.color || this.getRandomColor(),
            suggestion: item.suggestion || '暂无建议'
          }))

          const topCategories = [...this.problemCategories]
            .sort((a, b) => b.count - a.count)
            .slice(0, 3)
          this.hotProblems = topCategories.map((cat, idx) => ({
            id: idx + 1,
            category: cat.name,
            text: `${cat.name}相关问题出现频率较高，建议${cat.suggestion}`,
            frequency: cat.count,
            lastOccurred: '最近24小时',
            affectedUsers: Math.floor(cat.count * 0.8),
            priority: cat.count > 50 ? 'high' : cat.count > 20 ? 'medium' : 'low',
            color: cat.color
          }))
        } else {
          throw new Error(res.msg || '获取分类失败')
        }
      } catch (error) {
        this.$message.error(error.message || '加载问题分类失败')
      } finally {
        this.loadingCategories = false
      }
    },

    exportClassifications() {
      this.$message.success('导出功能开发中')
    },

    getTrendIcon(trend) {
      if (trend > 0) return 'fa-arrow-up'
      if (trend < 0) return 'fa-arrow-down'
      return 'fa-minus'
    },

    viewCategoryDetail(category) {
      this.$message.info(`查看分类详情：${category.name}`)
    },

    viewProblemDetail(problem) {
      this.$message.info(`查看问题详情：${problem.text}`)
    },

    formatDate(date) {
      const d = new Date(date)
      const year = d.getFullYear()
      const month = String(d.getMonth() + 1).padStart(2, '0')
      const day = String(d.getDate()).padStart(2, '0')
      return `${year}-${month}-${day}`
    },

    getRandomColor() {
      const colors = ['#FF6B6B', '#4ECDC4', '#FFE66D', '#1A535C', '#F7B05E']
      return colors[Math.floor(Math.random() * colors.length)]
    }
  }
}
</script>

<style scoped>
.data-ai {
  margin-top: 20px;
  padding: 0 20px;
}

.ai-header {
  margin-bottom: 20px;
  display: flex;
  justify-content: flex-end;
}

.ai-section {

  background: #fff;
  border-radius: 8px;
  padding: 20px;
  margin-bottom: 24px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.06);
}

.section-header {
  margin-bottom: 20px;
  border-left: 4px solid #409EFF;
  padding-left: 12px;
}
.section-header h3 {
  margin: 0 0 4px 0;
  font-size: 18px;
}
.section-subtitle {
  font-size: 12px;
  color: #8c8c8c;
}

/* 修改点1：左右等宽，去除左侧最小宽度限制，表单控件自适应宽度 */
.report-generation {
  display: flex;
  gap: 24px;
  flex-wrap: wrap;
}
.generation-form,
.report-preview {
  flex: 1;
  min-width: 0; /* 允许收缩，确保等宽 */
}

/* 表单控件宽度自适应 */
.generation-form .form-row {
  display: flex;
  align-items: flex-start;
  gap: 16px;
  flex-wrap: wrap;
  margin-bottom: 20px;
}
.generation-form .form-row label {
  width: 100px;
  text-align: right;
  line-height: 32px;
  font-weight: 500;
  flex-shrink: 0;
}
.generation-form .form-row .el-select,
.generation-form .form-row .el-date-picker,
.generation-form .form-row .natural-language-input {
  flex: 1;
  min-width: 0;
}
.generation-form .form-row .el-radio-group {
  flex: 1;
}
.generation-form .form-actions {
  padding-left: 116px;
}
.input-hint {
  font-size: 12px;
  color: #8c8c8c;
  margin-top: 4px;
}

/* 修改点2：预览内容限制高度，超出滚动 */
.report-preview {
  background: #fafafa;
  border-radius: 8px;
  padding: 16px;
  display: flex;
  flex-direction: column;
}
.preview-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 16px;
  flex-shrink: 0;
}
.preview-content {
  flex: 1;
  overflow-y: auto;
  max-height: 500px; /* 限制最大高度，超出滚动 */
}
.preview-card {
  background: #fff;
  border-radius: 8px;
  padding: 16px;
}
.no-preview {
  text-align: center;
  padding: 40px 0;
  color: #bfbfbf;
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.preview-status {
  font-size: 12px;
  padding: 2px 8px;
  border-radius: 12px;
}
.preview-status.generating {
  background: #e6f7ff;
  color: #1890ff;
}
.preview-status.completed {
  background: #f6ffed;
  color: #52c41a;
}
.preview-summary h5 {
  margin: 0 0 8px 0;
}
.preview-summary ul {
  padding-left: 20px;
  margin: 0;
}
.preview-actions {
  margin-top: 16px;
  display: flex;
  gap: 8px;
}

.historical-reports {
  margin-top: 24px;
}
.reports-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}
.report-title-cell {
  display: flex;
  align-items: center;
  gap: 8px;
}

/* 问题分类区域 */
.problem-classification {
  display: flex;
  flex-direction: column;
  gap: 24px;
}
.classification-list.full-width {
  width: 100%;
}
.list-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}
.category-cell {
  display: flex;
  align-items: center;
  gap: 8px;
}
.category-color {
  width: 12px;
  height: 12px;
  border-radius: 2px;
}
.trend-cell.up {
  color: #f5222d;
}
.trend-cell.down {
  color: #52c41a;
}
.trend-cell.stable {
  color: #8c8c8c;
}
.suggestion-cell {
  font-size: 12px;
  color: #595959;
}
.hot-problems {
  margin-top: 8px;
}
.hot-header {
  margin-bottom: 16px;
}
.hot-subtitle {
  font-size: 12px;
  color: #8c8c8c;
  margin-left: 8px;
}
.problem-examples {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 16px;
}
.example-card {
  border: 1px solid #f0f0f0;
  border-radius: 8px;
  padding: 12px;
  transition: all 0.2s;
}
.example-card:hover {
  box-shadow: 0 2px 8px rgba(0,0,0,0.08);
}
.example-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 12px;
}
.problem-category {
  font-size: 12px;
  padding: 2px 8px;
  border-radius: 12px;
  color: #fff;
}
.problem-frequency {
  font-size: 12px;
  color: #8c8c8c;
}
.example-body {
  margin-bottom: 12px;
}
.problem-text {
  font-weight: 500;
  margin-bottom: 8px;
}
.problem-meta {
  font-size: 12px;
  color: #8c8c8c;
  display: flex;
  gap: 12px;
}
.example-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

/* 修改点3：报告详情弹窗限制body高度并滚动 */
::v-deep .report-detail-dialog .el-dialog__body {
  max-height: 70vh;
  overflow-y: auto;
  padding: 20px;
}

.markdown-body {
  line-height: 1.8;
  font-size: 14px;
}
.markdown-body h1,
.markdown-body h2,
.markdown-body h3 {
  margin-top: 16px;
}
.markdown-body pre {
  background: #2d2d2d;
  color: #fff;
  padding: 12px;
  border-radius: 6px;
  overflow-x: auto;
}
.markdown-body code {
  background: #f5f5f5;
  padding: 2px 4px;
  border-radius: 4px;
}
</style>
