<template>
  <div class="metrics-analysis" v-loading="loading">

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
                <h3>{{ metric.metricName }}</h3>
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
                      <el-dropdown-item command="refresh" divided>刷新数据</el-dropdown-item>
                    </el-dropdown-menu>
                  </template>
                </el-dropdown>
              </div>
            </div>

            <div class="metric-card-body">
              <!-- 有图表数据时显示图表 -->
              <div v-if="metric.chartData" class="metric-chart">
                <div :id="'chart-' + metric.id" class="chart-container" style="height: 200px;"></div>
              </div>
              <!-- 否则显示简单计算值 -->
              <div v-else class="metric-simple">
                <div class="simple-value">{{ metric.computedValue !== undefined ? metric.computedValue : '—' }}</div>
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

        <el-form-item label="数据源" required>
          <el-select
            v-model="currentDataSource"
            placeholder="请选择数据源"
            clearable
            style="width: 100%"
            @change="onDataSourceChange"
          >
            <el-option label="交互日志" value="interaction"></el-option>
            <el-option label="告警记录" value="warning"></el-option>
            <el-option label="任务记录" value="task"></el-option>
          </el-select>
        </el-form-item>

        <el-form-item label="选择字段" required>
          <el-select
            v-model="metricForm.selectedFields"
            multiple
            filterable
            collapse-tags
            collapse-tags-tooltip
            placeholder="请先选择数据源"
            :loading="fieldLoading"
            :disabled="!currentDataSource"
            style="width: 100%"
          >
            <el-option
              v-for="field in fieldOptions"
              :key="field"
              :label="field"
              :value="field"
            />
          </el-select>
          <div v-if="currentDataSource && fieldOptions.length === 0 && !fieldLoading" class="field-hint">
            该数据源暂无可用字段
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
          <el-button type="primary" @click="saveMetric" :loading="saving">{{ isEditing ? '保存修改' : '创建指标' }}</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import * as echarts from 'echarts'
import {
  computeMetric,
  createMetric,
  deleteMetric,
  getChartData,
  getMetricById,
  getTableFields,
  listMetrics,
  updateMetric
} from '@/api/data/metrics/metric'

export default {
  data() {
    return {
      loading: false,
      saving: false,
      metricSearch: '',
      metricCategory: '',
      metricDialogVisible: false,
      isEditing: false,
      editingId: null,
      tagInputVisible: false,
      tagInputValue: '',
      // 表单数据
      metricForm: {
        name: '',
        category: '',
        description: '',
        selectedFields: [],
        calculationExpression: '',
        updateFrequency: 'daily',
        chartType: 'line',
        enableAlert: false,
        alertThreshold: 0,
        tags: []
      },
      // 当前选中的数据源（单选）
      currentDataSource: '',
      // 动态加载的字段列表
      fieldOptions: [],
      fieldLoading: false,
      // 数据源与表名映射
      sourceTableMap: {
        interaction: 't_interaction_history',
        warning: 'robot_warnings',
        task: 'tm_task'
      },
      // 指标列表（从后端加载）
      metricsList: [],
      // ECharts 实例存储
      chartInstances: {}
    };
  },
  computed: {
    filteredMetrics() {
      let list = this.metricsList.filter(m => {
        const matchSearch = m.metricName.toLowerCase().includes(this.metricSearch.toLowerCase());
        const matchCategory = !this.metricCategory || m.category === this.metricCategory;
        return matchSearch && matchCategory;
      });
      return list;
    }
  },
  watch: {
    metricsList: {
      handler() {
        this.$nextTick(() => {
          this.renderCharts();
        });
      },
      deep: true
    }
  },
  mounted() {
    this.loadMetrics();
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
    // ========== API 调用 ==========
    async loadMetrics() {
      this.loading = true;
      try {
        const res = await listMetrics();
        const metrics = res.data || [];
        // 为每个指标添加额外字段（用于存储计算值和图表数据）
        this.metricsList = metrics.map(m => ({
          ...m,
          enabled: m.enabled !== undefined ? m.enabled : true, // 默认启用
          computedValue: null,
          chartData: null,
          dataLoading: false
        }));
        // 批量获取计算值和图表数据
        await this.fetchMetricsData(this.metricsList);
      } catch (error) {
        console.error('加载指标列表失败', error);
        this.$message.error('加载指标列表失败');
      } finally {
        this.loading = false;
      }
    },

    // 批量获取指标的计算值和图表数据
    async fetchMetricsData(metrics) {
      // 并行请求所有指标的数据
      const promises = metrics.map(metric => this.fetchMetricData(metric));
      await Promise.allSettled(promises);
    },

    // 获取单个指标的计算值和图表数据
    async fetchMetricData(metric) {
      if (!metric.enabled) {
        // 停用的指标不请求数据
        return;
      }
      metric.dataLoading = true;
      try {
        // 并行请求计算值和图表数据
        const [computeRes, chartRes] = await Promise.allSettled([
          computeMetric(metric.id),
          getChartData(metric.id)
        ]);
        if (computeRes.status === 'fulfilled') {
          metric.computedValue = computeRes.value.data;
        } else {
          console.warn(`计算指标 ${metric.id} 失败`, computeRes.reason);
          metric.computedValue = null;
        }
        if (chartRes.status === 'fulfilled') {
          metric.chartData = chartRes.value.data;
        } else {
          console.warn(`获取图表数据 ${metric.id} 失败`, chartRes.reason);
          metric.chartData = null;
        }
      } finally {
        metric.dataLoading = false;
      }
    },

    // 手动刷新单个指标的数据（用于下拉菜单中的刷新操作）
    async refreshMetric(metric) {
      const loading = this.$loading({
        text: '刷新数据中...',
        spinner: 'el-icon-loading',
        background: 'rgba(0, 0, 0, 0.7)'
      });
      try {
        await this.fetchMetricData(metric);
        // 更新图表视图
        this.$nextTick(() => {
          this.renderMetricChart(metric);
        });
        this.$message.success('数据刷新成功');
      } catch (error) {
        this.$message.error('刷新失败');
      } finally {
        loading.close();
      }
    },

    // 获取数据表字段（保留原有逻辑）
    getTableNameFromSource(source) {
      return this.sourceTableMap[source] || '';
    },
    async fetchFieldsByDataSource(dataSource) {
      if (!dataSource) {
        this.fieldOptions = [];
        return;
      }
      const tableName = this.getTableNameFromSource(dataSource);
      if (!tableName) {
        this.fieldOptions = [];
        return;
      }
      this.fieldLoading = true;
      try {
        const res = await getTableFields(tableName);
        this.fieldOptions = res.data || [];
      } catch (error) {
        console.error('加载字段列表失败', error);
        this.$message.error('加载字段列表失败');
        this.fieldOptions = [];
      } finally {
        this.fieldLoading = false;
      }
    },
    onDataSourceChange(value) {
      this.metricForm.selectedFields = [];
      if (value) {
        this.fetchFieldsByDataSource(value);
      } else {
        this.fieldOptions = [];
      }
    },

    /**
     * 校验计算表达式的安全性
     * @param {string} expression 计算表达式
     * @returns {boolean} 是否安全
     */
    validateExpression(expression) {
      if (!expression) return true; // 必填已在外层校验

      const lowerExpr = expression.toLowerCase();

      // 危险 SQL 关键字列表（DML/DDL 操作）
      const dangerousKeywords = [
        'delete', 'update', 'insert', 'drop', 'alter',
        'create', 'truncate', 'merge', 'replace', 'exec', 'execute'
      ];

      for (const kw of dangerousKeywords) {
        if (lowerExpr.includes(kw)) {
          this.$message.error(`计算表达式中不允许包含 "${kw}" 操作，请修改`);
          return false;
        }
      }

      // 敏感字段列表（通常涉及用户隐私或安全）
      const sensitiveFields = [
        'password', 'passwd', 'pwd', 'secret', 'token',
        'api_key', 'auth', 'credential', 'private_key'
      ];

      for (const field of sensitiveFields) {
        if (lowerExpr.includes(field)) {
          this.$message.error(`计算表达式中不允许使用敏感字段 "${field}"，请移除后重试`);
          return false;
        }
      }

      return true;
    },

    async saveMetric() {
      // 基本必填校验
      if (!this.metricForm.name || !this.metricForm.category || !this.metricForm.calculationExpression) {
        this.$message.warning('请填写必填项（名称、分类、计算表达式）');
        return;
      }
      if (!this.currentDataSource) {
        this.$message.warning('请选择数据源');
        return;
      }
      if (!this.metricForm.selectedFields || this.metricForm.selectedFields.length === 0) {
        this.$message.warning('请至少选择一个字段');
        return;
      }

      // 安全校验：计算表达式不能包含危险操作或敏感字段
      if (!this.validateExpression(this.metricForm.calculationExpression)) {
        return;
      }

      const metricData = {
        metricName: this.metricForm.name,
        category: this.metricForm.category,
        description: this.metricForm.description,
        dataSources: [this.currentDataSource],
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

      this.saving = true;
      try {
        if (this.isEditing) {
          metricData.id = this.editingId;
          await updateMetric(metricData);
          this.$message.success('修改成功');
        } else {
          await createMetric(metricData);
          this.$message.success('创建成功');
        }
        this.metricDialogVisible = false;
        this.resetForm();
        await this.loadMetrics();
      } catch (error) {
        console.error('保存指标失败', error);
        this.$message.error(this.isEditing ? '修改失败' : '创建失败');
      } finally {
        this.saving = false;
      }
    },
    async handleMetricCommand(metric, command) {
      switch (command) {
        case 'edit':
          this.editMetric(metric);
          break;
        case 'enable':
        case 'disable':
          this.toggleMetricStatus(metric, command === 'enable');
          break;
        case 'delete':
          this.deleteMetricConfirm(metric);
          break;
        case 'export':
          this.$message.info(`导出指标数据：${metric.metricName}`);
          break;
        case 'refresh':
          this.refreshMetric(metric);
          break;
        default:
          break;
      }
    },
    async editMetric(metric) {
      this.isEditing = true;
      this.editingId = metric.id;
      try {
        const res = await getMetricById(metric.id);
        const data = res.data;
        this.metricForm = {
          name: data.metricName,
          category: data.category,
          description: data.description || '',
          selectedFields: data.selectedFields || [],
          calculationExpression: data.calculationExpression,
          updateFrequency: data.updateFrequency,
          chartType: data.chartType,
          enableAlert: data.enableAlert,
          alertThreshold: data.alertThreshold,
          tags: data.tags || []
        };
        const source = (data.dataSources && data.dataSources.length) ? data.dataSources[0] : '';
        this.currentDataSource = source;
        if (source) {
          await this.fetchFieldsByDataSource(source);
        } else {
          this.fieldOptions = [];
        }
        this.metricDialogVisible = true;
      } catch (error) {
        console.error('获取指标详情失败', error);
        this.$message.error('获取指标详情失败');
      }
    },
    async toggleMetricStatus(metric, enable) {
      const action = enable ? '启用' : '停用';
      try {
        await this.$confirm(`确认${action}指标“${metric.metricName}”吗？`, '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        });
        const updatedMetric = {...metric, enabled: enable};
        await updateMetric(updatedMetric);
        this.$message.success(`${action}成功`);
        await this.loadMetrics();
      } catch (error) {
        if (error !== 'cancel') {
          console.error(`${action}失败`, error);
          this.$message.error(`${action}失败`);
        }
      }
    },
    deleteMetricConfirm(metric) {
      this.$confirm(`确定删除指标“${metric.metricName}”吗？`, '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        try {
          await deleteMetric(metric.id);
          this.$message.success('删除成功');
          await this.loadMetrics();
        } catch (error) {
          console.error('删除失败', error);
          this.$message.error('删除失败');
        }
      }).catch(() => {});
    },
    // ========== 表单操作 ==========
    showCreateMetricDialog() {
      this.isEditing = false;
      this.editingId = null;
      this.resetForm();
      this.metricDialogVisible = true;
    },
    resetForm() {
      this.metricForm = {
        name: '',
        category: '',
        description: '',
        selectedFields: [],
        calculationExpression: '',
        updateFrequency: 'daily',
        chartType: 'line',
        enableAlert: false,
        alertThreshold: 0,
        tags: []
      };
      this.currentDataSource = '';
      this.fieldOptions = [];
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
    // ========== 图表渲染 ==========
    renderCharts() {
      if (this.metricDialogVisible) return;
      this.filteredMetrics.forEach(metric => {
        if (metric.chartData && metric.enabled) {
          this.renderMetricChart(metric);
        }
      });
    },
    renderMetricChart(metric) {
      const domId = `chart-${metric.id}`;
      const dom = document.getElementById(domId);
      if (!dom) return;
      // 销毁旧实例
      if (this.chartInstances[domId]) {
        this.chartInstances[domId].dispose();
        delete this.chartInstances[domId];
      }
      const chart = echarts.init(dom);
      this.chartInstances[domId] = chart;
      // 根据图表类型构建配置
      let option = {};
      const chartType = metric.chartType;
      const data = metric.chartData;
      if (!data) {
        // 无数据时显示空状态
        option = {
          title: { text: '暂无数据', left: 'center', top: 'center' }
        };
      } else if (chartType === 'pie' || chartType === 'ring') {
        // 饼图 / 环形图：预期 data 为 [{ name, value }]
        option = {
          tooltip: { trigger: 'item', formatter: '{b}: {d}%' },
          legend: { orient: 'vertical', left: 'left' },
          series: [{
            name: metric.name,
            type: 'pie',
            radius: chartType === 'ring' ? ['40%', '70%'] : '50%',
            data: data,
            label: { show: true, formatter: '{b}: {d}%' }
          }]
        };
      } else if (chartType === 'bar' || chartType === 'line') {
        // 柱状图 / 折线图：预期 data 为 { xAxis: [], series: [{ name, data }] } 或 { xAxis: [], data: [] }
        let xAxisData = data.xAxis || [];
        let seriesData = data.series || (data.data ? [{ name: metric.name, data: data.data }] : []);
        if (!Array.isArray(seriesData)) seriesData = [seriesData];
        option = {
          tooltip: { trigger: 'axis' },
          xAxis: { type: 'category', data: xAxisData },
          yAxis: { type: 'value' },
          series: seriesData.map(s => ({
            name: s.name || metric.name,
            type: chartType,
            data: s.data,
            smooth: chartType === 'line',
            itemStyle: { borderRadius: chartType === 'bar' ? [4,4,0,0] : undefined }
          }))
        };
      } else {
        // 默认使用折线图
        option = {
          tooltip: { trigger: 'axis' },
          xAxis: { type: 'category', data: data.xAxis || [] },
          yAxis: { type: 'value' },
          series: [{ type: 'line', data: data.data || [] }]
        };
      }
      chart.setOption(option);
      // 窗口自适应
      const resizeHandler = () => chart.resize();
      window.addEventListener('resize', resizeHandler);
      // 存储清理函数（可选）
      chart._resizeHandler = resizeHandler;
    },
    // ========== 辅助方法 ==========
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
/* 样式与原始代码保持一致，新增字段提示样式 */
.metrics-analysis {
  padding: 0 24px;
}

.glass-card {
  margin-top: 20px;
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

.field-hint {
  font-size: 12px;
  color: #e6a23c;
  margin-top: 4px;
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
