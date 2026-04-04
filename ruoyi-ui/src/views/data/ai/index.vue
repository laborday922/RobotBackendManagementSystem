<template>
  <div class="data-ai">
    <!-- 页面标题和操作 -->
    <!-- 分析报告生成区域 -->
    <div class="ai-section">
      <div class="section-header">
        <h3><i class="fas fa-file-alt"></i> 分析报告生成</h3>
        <div class="section-subtitle">使用AI分析生成服务质量评估报告</div>
      </div>

      <div class="report-generation">
        <div class="generation-form">
          <div class="form-row">
            <label>报告类型</label>
            <el-select v-model="reportType" placeholder="选择报告类型" style="width: 300px;">
              <el-option label="服务质量评估报告" value="quality"></el-option>
              <el-option label="用户满意度分析报告" value="satisfaction"></el-option>
              <el-option label="机器人性能分析报告" value="performance"></el-option>
              <el-option label="异常趋势分析报告" value="exception"></el-option>
              <el-option label="综合运营报告" value="overall"></el-option>
            </el-select>
          </div>

          <div class="form-row">
            <label>时间范围</label>
            <el-date-picker
              v-model="reportDateRange"
              type="daterange"
              range-separator="至"
              start-placeholder="开始日期"
              end-placeholder="结束日期"
              style="width: 300px;"
            ></el-date-picker>
          </div>

          <div class="form-row">
            <label>分析维度</label>
            <el-checkbox-group v-model="reportDimensions">
              <el-checkbox label="responseTime">响应时间</el-checkbox>
              <el-checkbox label="accuracy">准确率</el-checkbox>
              <el-checkbox label="satisfaction">用户满意度</el-checkbox>
              <el-checkbox label="exception">异常情况</el-checkbox>
              <el-checkbox label="trend">趋势分析</el-checkbox>
            </el-checkbox-group>
          </div>

          <!-- 自然语言输入要求 -->
          <div class="form-row">
            <label>自定义要求</label>
            <div class="natural-language-input">
              <el-input
                v-model="naturalLanguageRequirement"
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
            <el-radio-group v-model="reportDepth">
              <el-radio label="summary">摘要版</el-radio>
              <el-radio label="standard">标准版</el-radio>
              <el-radio label="detailed">详细版</el-radio>
            </el-radio-group>
          </div>

          <div class="form-actions">
            <el-button type="primary" @click="startGeneratingReport" :loading="generatingReport">
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

          <div class="preview-content" v-if="activeReport">
            <div class="preview-card">
              <div class="preview-title">{{ activeReport.title }}</div>
              <div class="preview-meta">
                <span><i class="fas fa-calendar"></i> {{ activeReport.date }}</span>
                <span><i class="fas fa-clock"></i> {{ activeReport.duration }}</span>
                <span><i class="fas fa-file"></i> {{ activeReport.pages }} 页</span>
              </div>

              <div class="preview-summary">
                <h5>核心发现</h5>
                <ul>
                  <li v-for="finding in activeReport.findings" :key="finding.id">
                    <i class="fas fa-bullseye"></i> {{ finding.text }}
                  </li>
                </ul>
              </div>

              <div class="preview-actions">
                <el-button size="small" @click="downloadReport(activeReport.id)">
                  <i class="fas fa-download"></i> 下载
                </el-button>
                <el-button size="small" @click="viewReportDetail(activeReport.id)">
                  <i class="fas fa-eye"></i> 查看详情
                </el-button>
                <el-button size="small" @click="shareReport(activeReport.id)">
                  <i class="fas fa-share-alt"></i> 分享
                </el-button>
              </div>
            </div>
          </div>

          <div class="no-preview" v-else>
            <i class="fas fa-file-import"></i>
            <p>选择参数并生成报告后，将在此处显示预览</p>
          </div>
        </div>
      </div>

      <!-- 历史报告列表 -->
      <div class="historical-reports">
        <div class="reports-header">
          <h4>历史报告列表</h4>
          <el-input
            v-model="reportSearch"
            placeholder="搜索报告"
            size="small"
            style="width: 200px;"
          >
            <template #prefix>
              <i class="fas fa-search"></i>
            </template>
          </el-input>
        </div>

        <div class="reports-list">
          <el-table :data="filteredReports" style="width: 100%">
            <el-table-column prop="title" label="报告名称" width="270">
              <template #default="scope">
                <div class="report-title-cell">
                  <i class="fas fa-file-alt" :style="{color: scope.row.typeColor}"></i>
                  <span>{{ scope.row.title }}</span>
                </div>
              </template>
            </el-table-column>
            <el-table-column prop="type" label="类型" width="120"></el-table-column>
            <el-table-column prop="date" label="生成时间" width="180"></el-table-column>
            <el-table-column prop="duration" label="分析耗时" width="100"></el-table-column>
            <el-table-column label="状态" width="100">
              <template #default="scope">
                <el-tag :type="scope.row.status === 'completed' ? 'success' : 'warning'" size="small">
                  {{ scope.row.status === 'completed' ? '已完成' : '处理中' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="260">
              <template #default="scope">
                <el-button size="small" @click="viewReport(scope.row)">查看</el-button>
                <el-button size="small" @click="downloadReport(scope.row.id)">下载</el-button>
                <el-button size="small" type="danger" @click="deleteReport(scope.row.id)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </div>
    </div>

    <!-- 问题智能分类展示（已移除饼图和概览卡片） -->
    <div class="ai-section">
      <div class="section-header">
        <h3><i class="fas fa-tags"></i> 问题智能分类</h3>
        <div class="section-subtitle">AI自动识别和分类的问题类型分布</div>
      </div>

      <div class="problem-classification">
        <!-- 分类详情表格（保留） -->
        <div class="classification-list full-width">
          <div class="list-header">
            <h4>分类详情</h4>
            <el-button size="small" @click="exportClassifications">
              <i class="fas fa-download"></i> 导出
            </el-button>
          </div>

          <div class="list-content">
            <el-table :data="problemCategories" style="width: 100%" height="300">
              <el-table-column label="分类" width="180">
                <template #default="scope">
                  <div class="category-cell">
                    <span class="category-color" :style="{backgroundColor: scope.row.color}"></span>
                    <span class="category-name">{{ scope.row.name }}</span>
                  </div>
                </template>
              </el-table-column>
              <el-table-column prop="count" label="问题数量" width="100">
                <template #default="scope">
                  <el-tag size="small">{{ scope.row.count }}</el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="percentage" label="占比" width="100">
                <template #default="scope">
                  {{ scope.row.percentage }}%
                </template>
              </el-table-column>
              <el-table-column label="趋势" width="120">
                <template #default="scope">
                  <div class="trend-cell" :class="scope.row.trend > 0 ? 'up' : scope.row.trend < 0 ? 'down' : 'stable'">
                    <i class="fas" :class="getTrendIcon(scope.row.trend)"></i>
                    <span>{{ scope.row.trend > 0 ? '+' : '' }}{{ scope.row.trend }}%</span>
                  </div>
                </template>
              </el-table-column>
              <el-table-column label="处理建议">
                <template #default="scope">
                  <div class="suggestion-cell">
                    {{ scope.row.suggestion }}
                  </div>
                </template>
              </el-table-column>
              <el-table-column label="操作" width="120">
                <template #default="scope">
                  <el-button size="small" @click="viewCategoryDetail(scope.row)">
                    详情
                  </el-button>
                </template>
              </el-table-column>
            </el-table>
          </div>
        </div>

        <!-- 热门问题示例（保留） -->
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
                  <el-button size="small" type="text" @click="viewProblemDetail(problem)">
                    <i class="fas fa-chevron-right"></i>
                  </el-button>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'AIAnalysis',
  data() {
    return {
      // 报告生成相关
      reportType: 'quality',
      reportDateRange: null,
      reportDimensions: ['responseTime', 'accuracy', 'satisfaction'],
      naturalLanguageRequirement: '',
      reportDepth: 'standard',
      generatingReport: false,
      activeReport: null,
      previewStatusClass: '',
      previewStatusText: '等待生成',
      // 历史报告
      reportSearch: '',
      reports: [
        // 示例数据
        {
          id: 1,
          title: '2025年第一季度服务质量评估报告',
          type: '服务质量评估',
          date: '2025-03-20 14:30:00',
          duration: '2分30秒',
          status: 'completed',
          typeColor: '#409EFF'
        },
        {
          id: 2,
          title: '用户满意度月度分析报告',
          type: '满意度分析',
          date: '2025-03-15 09:15:00',
          duration: '1分45秒',
          status: 'completed',
          typeColor: '#67C23A'
        }
      ],
      // 问题分类相关（仅保留表格和热门问题所需的数据）
      problemCategories: [
        {
          id: 1,
          name: '响应超时',
          count: 124,
          percentage: 32.5,
          trend: 5.2,
          color: '#FF6B6B',
          suggestion: '优化服务器配置，增加缓存'
        },
        {
          id: 2,
          name: '意图识别错误',
          count: 98,
          percentage: 25.7,
          trend: -2.1,
          color: '#4ECDC4',
          suggestion: '加强训练数据，优化模型'
        },
        {
          id: 3,
          name: '答案质量低',
          count: 76,
          percentage: 19.9,
          trend: 3.4,
          color: '#FFE66D',
          suggestion: '引入知识库审核机制'
        },
        {
          id: 4,
          name: '系统异常',
          count: 52,
          percentage: 13.6,
          trend: -1.5,
          color: '#1A535C',
          suggestion: '增加监控告警，定期巡检'
        },
        {
          id: 5,
          name: '用户投诉',
          count: 31,
          percentage: 8.1,
          trend: -0.8,
          color: '#F7B05E',
          suggestion: '建立投诉快速响应通道'
        }
      ],
      hotProblems: [
        {
          id: 1,
          category: '响应超时',
          text: '用户询问订单状态时，机器人响应超过10秒',
          frequency: 45,
          lastOccurred: '2025-03-22',
          affectedUsers: 38,
          priority: 'high',
          color: '#FF6B6B'
        },
        {
          id: 2,
          category: '意图识别错误',
          text: '用户说“我要退货”，机器人误解为“查询退货政策”',
          frequency: 32,
          lastOccurred: '2025-03-21',
          affectedUsers: 29,
          priority: 'high',
          color: '#4ECDC4'
        },
        {
          id: 3,
          category: '答案质量低',
          text: '关于会员等级的问题，答案不完整，缺少升级条件',
          frequency: 28,
          lastOccurred: '2025-03-20',
          affectedUsers: 25,
          priority: 'medium',
          color: '#FFE66D'
        }
      ]
    };
  },
  computed: {
    filteredReports() {
      if (!this.reportSearch) return this.reports;
      return this.reports.filter(report =>
        report.title.toLowerCase().includes(this.reportSearch.toLowerCase())
      );
    }
  },
  methods: {
    generateNewReport() {
      // 清空表单或跳转到新建
      this.resetReportForm();
      this.$message.info('请选择参数后生成报告');
    },
    refreshAIData() {
      this.$message.success('数据刷新成功');
    },
    startGeneratingReport() {
      this.generatingReport = true;
      this.previewStatusText = '生成中...';
      this.previewStatusClass = 'generating';
      // 模拟生成报告
      setTimeout(() => {
        this.activeReport = {
          id: Date.now(),
          title: `${this.reportType === 'quality' ? '服务质量评估' : '分析报告'} (${new Date().toLocaleDateString()})`,
          date: new Date().toLocaleString(),
          duration: '2分30秒',
          pages: 8,
          findings: [
            { id: 1, text: '响应时间较上一周期提升12%' },
            { id: 2, text: '用户满意度评分4.8/5.0' },
            { id: 3, text: '异常率下降0.5个百分点' }
          ]
        };
        this.generatingReport = false;
        this.previewStatusText = '生成完成';
        this.previewStatusClass = 'completed';
        this.$message.success('报告生成成功');
      }, 2000);
    },
    resetReportForm() {
      this.reportType = 'quality';
      this.reportDateRange = null;
      this.reportDimensions = ['responseTime', 'accuracy', 'satisfaction'];
      this.naturalLanguageRequirement = '';
      this.reportDepth = 'standard';
      this.activeReport = null;
      this.previewStatusText = '等待生成';
      this.previewStatusClass = '';
    },
    downloadReport(id) {
      this.$message.success(`下载报告 ${id}`);
    },
    viewReportDetail(id) {
      this.$message.info(`查看报告详情 ${id}`);
    },
    shareReport(id) {
      this.$message.info(`分享报告 ${id}`);
    },
    viewReport(row) {
      this.$message.info(`查看报告：${row.title}`);
    },
    deleteReport(id) {
      this.$confirm('确认删除该报告？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        const index = this.reports.findIndex(r => r.id === id);
        if (index !== -1) this.reports.splice(index, 1);
        this.$message.success('删除成功');
      });
    },
    exportClassifications() {
      this.$message.success('导出分类数据');
    },
    getTrendIcon(trend) {
      if (trend > 0) return 'fa-arrow-up';
      if (trend < 0) return 'fa-arrow-down';
      return 'fa-minus';
    },
    viewCategoryDetail(category) {
      this.$message.info(`查看分类详情：${category.name}`);
    },
    viewProblemDetail(problem) {
      this.$message.info(`查看问题详情：${problem.text}`);
    }
  }
};
</script>

<style scoped>
/* 此处样式与原代码一致，可根据需要保留或微调 */
.data-ai {
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

.report-generation {
  display: flex;
  gap: 24px;
  flex-wrap: wrap;
}
.generation-form {
  flex: 2;
  min-width: 400px;
}
.form-row {
  margin-bottom: 20px;
  display: flex;
  align-items: flex-start;
  gap: 16px;
  flex-wrap: wrap;
}
.form-row label {
  width: 100px;
  text-align: right;
  line-height: 32px;
  font-weight: 500;
}
.form-actions {
  margin-top: 24px;
  padding-left: 116px;
}
.natural-language-input {
  flex: 1;
}
.input-hint {
  font-size: 12px;
  color: #8c8c8c;
  margin-top: 4px;
}
.report-preview {
  flex: 1;
  min-width: 300px;
  background: #fafafa;
  border-radius: 8px;
  padding: 16px;
}
.preview-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 16px;
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
.preview-card {
  background: #fff;
  border-radius: 8px;
  padding: 16px;
}
.preview-title {
  font-weight: bold;
  font-size: 16px;
  margin-bottom: 12px;
}
.preview-meta {
  font-size: 12px;
  color: #8c8c8c;
  margin-bottom: 16px;
  display: flex;
  gap: 12px;
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
.no-preview {
  text-align: center;
  padding: 40px 0;
  color: #bfbfbf;
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

/* 问题分类区域（调整后） */
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


</style>
