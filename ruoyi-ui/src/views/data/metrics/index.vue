<template>
  <div class="metrics-analysis">

    <!-- ===== 操作卡片 ===== -->
    <div class="glass-card">
      <div class="card-header">
        <h3>指标分析</h3>
        <el-button type="primary" @click="showCreateMetricDialog">
          <i class="fas fa-plus"></i> 创建新指标
        </el-button>
      </div>

      <div class="card-body">
        <!-- 筛选栏 -->
        <div class="metrics-filter">
          <el-input
            v-model="metricSearch"
            placeholder="搜索指标名称"
            clearable
            style="width: 300px;"
          >
            <template #prefix>
              <i class="fas fa-search"></i>
            </template>
          </el-input>

          <el-select v-model="metricCategory" placeholder="选择指标分类" clearable style="width: 200px;">
            <el-option label="用户满意度" value="satisfaction"></el-option>
            <el-option label="响应时间" value="response"></el-option>
            <el-option label="任务完成率" value="completion"></el-option>
            <el-option label="异常率" value="exception"></el-option>
            <el-option label="使用率" value="usage"></el-option>
          </el-select>
        </div>

        <!-- 指标卡片网格 -->
        <div class="metrics-grid">
          <div
            v-for="metric in filteredMetrics"
            :key="metric.id"
            class="metric-card"
          >
            <div class="metric-card-header">
              <div class="metric-title">
                <h3>{{ metric.name }}</h3>
                <el-tag size="small" :type="metric.enabled ? 'success' : 'info'">
                  {{ metric.enabled ? '已启用' : '已停用' }}
                </el-tag>
              </div>
              <div class="metric-actions">
                <el-dropdown @command="(cmd) => handleMetricCommand(metric, cmd)">
                  <el-button size="small" type="text">
                    <i class="fas fa-ellipsis-v"></i>
                  </el-button>
                  <template #dropdown>
                    <el-dropdown-menu>
                      <el-dropdown-item command="edit">编辑配置</el-dropdown-item>
                      <el-dropdown-item :command="metric.enabled ? 'disable' : 'enable'">
                        {{ metric.enabled ? '停用指标' : '启用指标' }}
                      </el-dropdown-item>
                      <el-dropdown-item command="delete">删除指标</el-dropdown-item>
                      <el-dropdown-item command="export">导出数据</el-dropdown-item>
                    </el-dropdown-menu>
                  </template>
                </el-dropdown>
              </div>
            </div>

            <div class="metric-card-body">
              <!-- 根据指标类型展示不同图表内容 -->
              <div v-if="metric.id === 'satisfaction'" class="metric-chart">
                <div :id="`chart-satisfaction-${metric.id}`" class="chart-container" style="height: 200px;"></div>
              </div>

              <div v-else-if="metric.id === 'response'" class="metric-chart">
                <div :id="`chart-response-${metric.id}`" class="chart-container" style="height: 200px;"></div>
              </div>

              <div v-else-if="!metric.enabled || !metric.calculationExpression" class="metric-placeholder">
                <i class="fas fa-cog"></i>
                <p>指标尚未配置或已停用，请点击编辑完成配置</p>
              </div>

              <div v-else class="metric-simple">
                <div class="simple-value">{{ metric.sampleValue || '—' }}</div>
                <div class="simple-desc">{{ metric.description || '暂无数据' }}</div>
              </div>

              <!-- 通用统计信息 -->
              <div class="metric-stats">
                <div class="stat-item">
                  <div class="stat-label">更新频率</div>
                  <div class="stat-value">{{ getFrequencyLabel(metric.updateFrequency) }}</div>
                </div>
                <div class="stat-item">
                  <div class="stat-label">图表类型</div>
                  <div class="stat-value">{{ getChartTypeLabel(metric.chartType) }}</div>
                </div>
                <div class="stat-item">
                  <div class="stat-label">告警阈值</div>
                  <div class="stat-value">{{ metric.enableAlert ? metric.alertThreshold : '未启用' }}</div>
                </div>
              </div>
            </div>

            <div class="metric-card-footer">
              <div class="metric-description">{{ metric.description || '暂无描述' }}</div>
              <div class="metric-tags">
                <el-tag size="small">{{ getCategoryLabel(metric.category) }}</el-tag>
                <el-tag v-for="tag in metric.tags" :key="tag" size="small">{{ tag }}</el-tag>
              </div>
            </div>
          </div>

          <!-- 添加新指标卡片 -->
          <div class="metric-card add-metric-card" @click="showCreateMetricDialog">
            <div class="add-metric-content">
              <i class="fas fa-plus-circle"></i>
              <div class="add-metric-text">添加新指标</div>
              <div class="add-metric-desc">创建自定义监控指标</div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- ===== 创建/编辑指标对话框 ===== -->
    <el-dialog
      :visible.sync="metricDialogVisible"
      :title="isEditing ? '编辑指标配置' : '创建新指标'"
      :modal="false"
      width="800px"
    >
      <el-form :model="metricForm" label-width="120px">
        <el-form-item label="指标名称" required>
          <el-input v-model="metricForm.name" placeholder="请输入指标名称"></el-input>
        </el-form-item>

        <el-form-item label="指标分类" required>
          <el-select v-model="metricForm.category" placeholder="请选择指标分类">
            <el-option label="用户满意度" value="satisfaction"></el-option>
            <el-option label="响应时间" value="response"></el-option>
            <el-option label="任务完成率" value="completion"></el-option>
            <el-option label="异常率" value="exception"></el-option>
            <el-option label="使用率" value="usage"></el-option>
            <el-option label="其他" value="other"></el-option>
          </el-select>
        </el-form-item>

        <el-form-item label="指标描述">
          <el-input v-model="metricForm.description" type="textarea" :rows="3" placeholder="请输入指标描述"></el-input>
        </el-form-item>

        <el-form-item label="数据来源" required>
          <el-checkbox-group v-model="metricForm.dataSources">
            <el-checkbox label="interaction">交互日志</el-checkbox>
            <el-checkbox label="status">机器人状态</el-checkbox>
            <el-checkbox label="feedback">用户反馈</el-checkbox>
            <el-checkbox label="task">任务记录</el-checkbox>
          </el-checkbox-group>
        </el-form-item>

        <el-form-item label="字段选择">
          <div class="field-selection">
            <div class="field-group">
              <div class="field-group-title">交互日志表字段：</div>
              <el-checkbox-group v-model="metricForm.selectedFields">
                <div class="field-row">
                  <el-checkbox label="response_time">响应时间</el-checkbox>
                  <el-checkbox label="success">是否成功</el-checkbox>
                  <el-checkbox label="error_code">错误码</el-checkbox>
                </div>
                <div class="field-row">
                  <el-checkbox label="user_id">用户ID</el-checkbox>
                  <el-checkbox label="robot_id">机器人ID</el-checkbox>
                  <el-checkbox label="timestamp">时间戳</el-checkbox>
                </div>
              </el-checkbox-group>
            </div>

            <div class="field-group">
              <div class="field-group-title">机器人状态表字段：</div>
              <el-checkbox-group v-model="metricForm.selectedFields">
                <div class="field-row">
                  <el-checkbox label="cpu_usage">CPU使用率</el-checkbox>
                  <el-checkbox label="memory_usage">内存使用率</el-checkbox>
                  <el-checkbox label="status">状态</el-checkbox>
                </div>
              </el-checkbox-group>
            </div>
          </div>
        </el-form-item>

        <el-form-item label="计算表达式" required>
          <el-input
            v-model="metricForm.calculationExpression"
            type="textarea"
            :rows="3"
            placeholder="例如：SUM(response_time) / COUNT(*) * 100"
          ></el-input>
          <div class="expression-hint">提示：可使用字段名和SQL函数，如 COUNT(), SUM(), AVG(), MAX(), MIN()</div>
        </el-form-item>

        <el-form-item label="更新频率" required>
          <el-select v-model="metricForm.updateFrequency" style="width: 200px;">
            <el-option label="实时更新" value="realtime"></el-option>
            <el-option label="每小时" value="hourly"></el-option>
            <el-option label="每天" value="daily"></el-option>
            <el-option label="每周" value="weekly"></el-option>
          </el-select>
        </el-form-item>

        <el-form-item label="图表类型" required>
          <el-select v-model="metricForm.chartType" style="width: 200px;">
            <el-option label="柱状图" value="bar"></el-option>
            <el-option label="折线图" value="line"></el-option>
            <el-option label="饼图" value="pie"></el-option>
            <el-option label="环形图" value="ring"></el-option>
          </el-select>
        </el-form-item>

        <el-form-item label="阈值告警">
          <el-switch v-model="metricForm.enableAlert"></el-switch>
          <div v-if="metricForm.enableAlert" style="margin-top: 10px;">
            <el-input-number v-model="metricForm.alertThreshold" :min="0" :step="0.1" style="width: 150px;"></el-input-number>
            <span style="margin-left: 10px; color: #999;">超过此值触发告警</span>
          </div>
        </el-form-item>

        <el-form-item label="指标标签">
          <el-tag
            v-for="tag in metricForm.tags"
            :key="tag"
            closable
            @close="removeMetricTag(tag)"
          >
            {{ tag }}
          </el-tag>
          <el-input
            v-if="tagInputVisible"
            ref="tagInputRef"
            v-model="tagInputValue"
            size="small"
            @keyup.enter="addMetricTag"
            @blur="addMetricTag"
          ></el-input>
          <el-button v-else size="small" @click="showTagInput">+ 添加标签</el-button>
        </el-form-item>
      </el-form>

      <template #footer>
        <span class="dialog-footer">
          <el-button @click="metricDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="saveMetric">{{ isEditing ? '保存修改' : '创建指标' }}</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import * as echarts from 'echarts'


export default {
  data() {
    return {
      metricSearch: '',
      metricCategory: '',
      metricDialogVisible: false,
      isEditing: false,
      editingId: null,
      loading: false,
      tagInputVisible: false,
      tagInputValue: '',
      metricForm: {
        name: '',
        category: '',
        description: '',
        dataSources: [],
        selectedFields: [],
        calculationExpression: '',
        updateFrequency: 'daily',
        chartType: 'line',
        enableAlert: false,
        alertThreshold: 0,
        tags: []
      },
      // 预置指标数据
      metricsList: [
        {
          id: 'satisfaction',
          name: '用户满意度分布',
          category: 'satisfaction',
          description: '统计用户对机器人服务的满意度评分分布情况',
          dataSources: ['feedback'],
          selectedFields: ['user_id', 'timestamp'],
          calculationExpression: 'AVG(score)',
          updateFrequency: 'daily',
          chartType: 'pie',
          enableAlert: false,
          alertThreshold: 0,
          tags: ['满意度', '用户反馈'],
          enabled: true,
          sampleValue: '4.3'
        },
        {
          id: 'response',
          name: '平均响应时间',
          category: 'response',
          description: '监控机器人从接收请求到给出响应的平均时间',
          dataSources: ['interaction'],
          selectedFields: ['response_time', 'success'],
          calculationExpression: 'AVG(response_time)',
          updateFrequency: 'realtime',
          chartType: 'line',
          enableAlert: true,
          alertThreshold: 2.5,
          tags: ['性能指标', '实时监控'],
          enabled: true,
          sampleValue: '2.3s'
        },
        {
          id: 'completion',
          name: '任务完成率',
          category: 'completion',
          description: '统计机器人任务成功完成的比例',
          dataSources: ['task'],
          selectedFields: ['success'],
          calculationExpression: 'SUM(success) / COUNT(*) * 100',
          updateFrequency: 'hourly',
          chartType: 'bar',
          enableAlert: false,
          alertThreshold: 0,
          tags: ['任务指标', '成功率'],
          enabled: false,
          sampleValue: null
        }
      ],
      // 模拟响应时间趋势数据
      responseTimeData: [
        { day: '周一', value: 2.8 },
        { day: '周二', value: 2.5 },
        { day: '周三', value: 2.3 },
        { day: '周四', value: 2.4 },
        { day: '周五', value: 2.2 }
      ],
      // 满意度数据
      satisfactionData: [
        { name: '非常满意', value: 60 },
        { name: '满意', value: 25 },
        { name: '一般', value: 10 },
        { name: '不满意', value: 5 }
      ],
      chartInstances: {} // 存储 ECharts 实例
    };
  },
  computed: {
    filteredMetrics() {
      let list = this.metricsList.filter(m => {
        const matchSearch = m.name.toLowerCase().includes(this.metricSearch.toLowerCase());
        const matchCategory = !this.metricCategory || m.category === this.metricCategory;
        return matchSearch && matchCategory;
      });
      return list;
    }
  },
  watch: {
    metricsList() {
      this.$nextTick(() => {
        this.renderCharts();
      });
    }
  },
  mounted() {
    this.$nextTick(() => {
      this.renderCharts();
    });
  },
  beforeDestroy() {
    // 销毁所有 ECharts 实例
    Object.values(this.chartInstances).forEach(chart => {
      if (chart && !chart.isDisposed()) {
        chart.dispose();
      }
    });
  },
  methods: {
    renderCharts() {
      if (this.metricDialogVisible) return; // 👈 弹窗打开时不渲染
      // 为每个指标卡片渲染图表
      this.filteredMetrics.forEach(metric => {
        if (metric.id === 'satisfaction') {
          this.renderSatisfactionChart(metric.id);
        } else if (metric.id === 'response') {
          this.renderResponseChart(metric.id);
        }
      });
    },
    renderSatisfactionChart(id) {
      const domId = `chart-satisfaction-${id}`;
      const dom = document.getElementById(domId);
      if (!dom) return;
      // 如果已存在实例，先销毁
      if (this.chartInstances[domId]) {
        this.chartInstances[domId].dispose();
      }
      const chart = echarts.init(dom);
      this.chartInstances[domId] = chart;
      const option = {
        tooltip: {
          trigger: 'item',
          formatter: '{b}: {d}%'
        },
        legend: {
          orient: 'vertical',
          left: 'left',
          data: this.satisfactionData.map(item => item.name)
        },
        series: [
          {
            name: '满意度分布',
            type: 'pie',
            radius: '50%',
            data: this.satisfactionData,
            emphasis: {
              itemStyle: {
                shadowBlur: 10,
                shadowOffsetX: 0,
                shadowColor: 'rgba(0, 0, 0, 0.5)'
              }
            },
            label: {
              show: true,
              formatter: '{b}: {d}%'
            }
          }
        ]
      };
      chart.setOption(option);
      window.addEventListener('resize', () => chart.resize());
    },
    renderResponseChart(id) {
      const domId = `chart-response-${id}`;
      const dom = document.getElementById(domId);
      if (!dom) return;
      if (this.chartInstances[domId]) {
        this.chartInstances[domId].dispose();
      }
      const chart = echarts.init(dom);
      this.chartInstances[domId] = chart;
      const option = {
        tooltip: {
          trigger: 'axis',
          axisPointer: { type: 'shadow' }
        },
        xAxis: {
          type: 'category',
          data: this.responseTimeData.map(item => item.day)
        },
        yAxis: {
          type: 'value',
          name: '响应时间 (s)'
        },
        series: [
          {
            name: '平均响应时间',
            type: 'line',
            data: this.responseTimeData.map(item => item.value),
            smooth: true,
            lineStyle: {
              color: '#4e8cff',
              width: 2
            },
            areaStyle: {
              color: 'rgba(78, 140, 255, 0.2)'
            },
            symbol: 'circle',
            symbolSize: 6,
            itemStyle: {
              color: '#4e8cff'
            }
          }
        ]
      };
      chart.setOption(option);
      window.addEventListener('resize', () => chart.resize());
    },
    showCreateMetricDialog() {
      this.isEditing = false;
      this.editingId = null;
      this.resetForm();
      this.metricDialogVisible = true;
    },
    editMetric(metric) {
      this.isEditing = true;
      this.editingId = metric.id;
      this.metricForm = {
        name: metric.name,
        category: metric.category,
        description: metric.description,
        dataSources: [...metric.dataSources],
        selectedFields: [...metric.selectedFields],
        calculationExpression: metric.calculationExpression,
        updateFrequency: metric.updateFrequency,
        chartType: metric.chartType,
        enableAlert: metric.enableAlert,
        alertThreshold: metric.alertThreshold,
        tags: [...metric.tags]
      };
      this.metricDialogVisible = true;
    },
    saveMetric() {
      if (!this.metricForm.name || !this.metricForm.category || !this.metricForm.calculationExpression) {
        this.$message.warning('请填写必填项（名称、分类、计算表达式）');
        return;
      }
      const newMetric = {
        id: this.isEditing ? this.editingId : Date.now().toString(),
        name: this.metricForm.name,
        category: this.metricForm.category,
        description: this.metricForm.description,
        dataSources: this.metricForm.dataSources,
        selectedFields: this.metricForm.selectedFields,
        calculationExpression: this.metricForm.calculationExpression,
        updateFrequency: this.metricForm.updateFrequency,
        chartType: this.metricForm.chartType,
        enableAlert: this.metricForm.enableAlert,
        alertThreshold: this.metricForm.alertThreshold,
        tags: this.metricForm.tags,
        enabled: true,
        sampleValue: null
      };
      if (this.isEditing) {
        const index = this.metricsList.findIndex(m => m.id === this.editingId);
        if (index !== -1) {
          this.metricsList.splice(index, 1, newMetric);
          this.$message.success('修改成功');
        }
      } else {
        this.metricsList.push(newMetric);
        this.$message.success('创建成功');
      }
      this.metricDialogVisible = false;
      this.resetForm();
    },
    handleMetricCommand(metric, command) {
      switch (command) {
        case 'edit':
          this.editMetric(metric);
          break;
        case 'enable':
          metric.enabled = true;
          this.$message.success(`已启用指标：${metric.name}`);
          break;
        case 'disable':
          metric.enabled = false;
          this.$message.success(`已停用指标：${metric.name}`);
          break;
        case 'delete':
          this.$confirm(`确定删除指标“${metric.name}”吗？`, '提示', {
            confirmButtonText: '确定',
            cancelButtonText: '取消',
            type: 'warning'
          }).then(() => {
            const index = this.metricsList.findIndex(m => m.id === metric.id);
            if (index !== -1) {
              this.metricsList.splice(index, 1);
              this.$message.success('删除成功');
            }
          }).catch(() => {});
          break;
        case 'export':
          this.$message.info(`导出指标数据：${metric.name}`);
          break;
        default:
          break;
      }
    },
    resetForm() {
      this.metricForm = {
        name: '',
        category: '',
        description: '',
        dataSources: [],
        selectedFields: [],
        calculationExpression: '',
        updateFrequency: 'daily',
        chartType: 'line',
        enableAlert: false,
        alertThreshold: 0,
        tags: []
      };
      this.tagInputVisible = false;
      this.tagInputValue = '';
    },
    showTagInput() {
      this.tagInputVisible = true;
      this.$nextTick(() => {
        this.$refs.tagInputRef?.focus();
      });
    },
    addMetricTag() {
      if (this.tagInputValue) {
        if (!this.metricForm.tags.includes(this.tagInputValue)) {
          this.metricForm.tags.push(this.tagInputValue);
        }
        this.tagInputValue = '';
      }
      this.tagInputVisible = false;
    },
    removeMetricTag(tag) {
      const index = this.metricForm.tags.indexOf(tag);
      if (index > -1) {
        this.metricForm.tags.splice(index, 1);
      }
    },
    getCategoryLabel(cat) {
      const map = {
        satisfaction: '用户满意度',
        response: '响应时间',
        completion: '任务完成率',
        exception: '异常率',
        usage: '使用率',
        other: '其他'
      };
      return map[cat] || cat;
    },
    getFrequencyLabel(freq) {
      const map = {
        realtime: '实时更新',
        hourly: '每小时',
        daily: '每天',
        weekly: '每周'
      };
      return map[freq] || freq;
    },
    getChartTypeLabel(type) {
      const map = {
        bar: '柱状图',
        line: '折线图',
        pie: '饼图',
        ring: '环形图'
      };
      return map[type] || type;
    }
  }
};
</script>

<style scoped lang="scss">
/* 样式保持不变，与之前相同，仅删除了原有的 CSS 图表样式，保留图表容器样式 */
.metrics-analysis {
  padding: 0 24px;
}

.glass-card {
  margin-bottom: 24px;
  border-radius: 16px;
  backdrop-filter: blur(12px);
  background: rgba(255, 255, 255, 0.95);
  box-shadow: 0 5px 5px rgba(0, 0, 0, 0.2);
  border: 1px solid rgba(255, 255, 255, 0.3);
  overflow: hidden;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 24px;
  border-bottom: 1px solid rgba(0, 0, 0, 0.05);
}

.card-header h3 {
  margin: 0;
  font-weight: 400;
}

.card-body {
  padding: 24px;
}

.metrics-filter {
  display: flex;
  gap: 16px;
  margin-bottom: 24px;
}

.metrics-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(380px, 1fr));
  gap: 24px;
}

.metric-card {
  background: rgba(255, 255, 255, 0.8);
  border-radius: 16px;
  border: 1px solid rgba(0, 0, 0, 0.05);
  transition: all 0.3s;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

.metric-card:hover {
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.1);
  transform: translateY(-2px);
}

.metric-card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px 8px 20px;
}

.metric-title {
  display: flex;
  align-items: center;
  gap: 12px;
}

.metric-title h3 {
  margin: 0;
  font-size: 16px;
  font-weight: 500;
}

.metric-card-body {
  padding: 12px 20px;
  flex: 1;
}

.metric-chart {
  margin-bottom: 16px;
  background: rgba(250, 250, 250, 0.6);
  border-radius: 12px;
  padding: 12px;
}

.chart-container {
  width: 100%;
  height: 200px;
}

.metric-placeholder {
  text-align: center;
  padding: 32px 16px;
  color: #999;
}

.metric-placeholder i {
  font-size: 48px;
  margin-bottom: 12px;
  color: #ccc;
}

.metric-simple {
  text-align: center;
  padding: 16px;
  background: #f9f9f9;
  border-radius: 12px;
  margin-bottom: 16px;
}

.simple-value {
  font-size: 28px;
  font-weight: bold;
  color: #4e8cff;
}

.metric-stats {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  margin-top: 16px;
  padding-top: 12px;
  border-top: 1px solid rgba(0, 0, 0, 0.05);
}

.stat-item {
  text-align: center;
  flex: 1;
}

.stat-label {
  font-size: 11px;
  color: #888;
  margin-bottom: 4px;
}

.stat-value {
  font-size: 13px;
  font-weight: 500;
}

.metric-card-footer {
  padding: 12px 20px 16px;
  background: rgba(0, 0, 0, 0.02);
  border-top: 1px solid rgba(0, 0, 0, 0.05);
}

.metric-description {
  font-size: 12px;
  color: #666;
  margin-bottom: 8px;
  line-height: 1.4;
}

.metric-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.add-metric-card {
  background: rgba(245, 247, 250, 0.6);
  border: 1px dashed #ccc;
  cursor: pointer;
  justify-content: center;
  align-items: center;
  text-align: center;
  transition: all 0.2s;
}

.add-metric-card:hover {
  background: rgba(78, 140, 255, 0.1);
  border-color: #4e8cff;
}

.add-metric-content {
  padding: 48px 24px;
}

.add-metric-content i {
  font-size: 48px;
  color: #4e8cff;
  margin-bottom: 16px;
}

.add-metric-text {
  font-size: 16px;
  font-weight: 500;
  margin-bottom: 8px;
}

.add-metric-desc {
  font-size: 12px;
  color: #888;
}

.field-selection {
  max-height: 300px;
  overflow-y: auto;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  padding: 12px;
}

.field-group {
  margin-bottom: 16px;
}

.field-group-title {
  font-weight: 500;
  margin-bottom: 8px;
  font-size: 13px;
}

.field-row {
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
  margin-bottom: 8px;
}

.expression-hint {
  font-size: 12px;
  color: #999;
  margin-top: 4px;
}

@media (max-width: 768px) {
  .metrics-grid {
    grid-template-columns: 1fr;
  }
  .metrics-filter {
    flex-direction: column;
  }
}
</style>
