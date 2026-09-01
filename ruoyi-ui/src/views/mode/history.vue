<template>
  <div class="card">
    <div class="card-header">
      <div class="card-title">
        <i class="fas fa-history"></i> 历史记录
      </div>
    </div>

    <div class="card-body">
      <!-- 筛选区域 -->
      <div class="filter-bar">
        <el-select v-model="queryParams.operationType" placeholder="操作类型" clearable style="width: 150px;">
          <el-option label="紧急操作" value="emergency" />
          <el-option label="状态操作" value="status" />
          <el-option label="系统操作" value="system" />
        </el-select>

        <el-date-picker
          v-model="queryParams.dateRange"
          type="daterange"
          range-separator="至"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
          value-format="yyyy-MM-dd"
          style="width: 300px;">
        </el-date-picker>

        <!-- 机器人下拉框 - 兼容后端返回的 id 和 name 字段 -->
        <el-select v-model="queryParams.robotId" placeholder="机器人" clearable filterable style="width: 150px;">
          <el-option
            v-for="robot in robotOptions"
            :key="robot.id || robot.robotId"
            :label="robot.name || robot.robotName"
            :value="robot.id || robot.robotId" />
        </el-select>

        <el-button type="primary" @click="handleQuery">
          <i class="fas fa-search"></i> 查询
        </el-button>
        <el-button @click="resetQuery">
          <i class="fas fa-undo"></i> 重置
        </el-button>
        <el-button type="warning" @click="handleClear" :disabled="!historyList.length">
          <i class="fas fa-trash"></i> 清空记录
        </el-button>
      </div>

      <!-- 统计卡片 - 显示全量统计数据 -->
      <div class="stats-cards">
        <div class="stat-card" :class="stat.type" v-for="stat in statistics" :key="stat.type" @click="filterByType(stat.type)">
          <div class="stat-icon">
            <i :class="stat.icon"></i>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ stat.count }}</div>
            <div class="stat-label">{{ stat.label }}</div>
          </div>
        </div>
      </div>

      <!-- 历史记录表格 -->
      <el-table :data="historyList" v-loading="loading" border style="width: 100%">
        <el-table-column prop="operationTime" label="操作时间" width="160" align="center">
          <template slot-scope="scope">
            {{ parseTime(scope.row.operationTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="operationType" label="操作类型" width="120" align="center">
          <template slot-scope="scope">
            <el-tag :type="getOperationTypeTag(scope.row.operationType)">
              {{ getOperationTypeText(scope.row.operationType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="operationCategory" label="操作分类" width="100" align="center">
          <template slot-scope="scope">
            <el-tag :type="getCategoryTag(scope.row.operationCategory)" size="mini">
              {{ getCategoryText(scope.row.operationCategory) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="robotName" label="涉及机器人" width="150" align="center" />
        <el-table-column prop="content" label="操作内容" min-width="250" show-overflow-tooltip />
        <el-table-column prop="operator" label="操作人" width="120" align="center" />
        <el-table-column prop="status" label="状态" width="80" align="center">
          <template slot-scope="scope">
            <el-tag :type="getStatusTag(scope.row.status)" size="mini">
              {{ getStatusText(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="100" align="center">
          <template slot-scope="scope">
            <el-button type="text" size="mini" @click="viewDetail(scope.row)">详情</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-wrap">
        <pagination
          v-show="total > 0"
          :total="total"
          :page.sync="queryParams.pageNum"
          :limit.sync="queryParams.pageSize"
          @pagination="getList"
        />
      </div>
    </div>

    <!-- 详情对话框 - 调整垂直位置靠下一些 -->
    <el-dialog
      :title="detailTitle"
      :visible.sync="detailVisible"
      width="500px"
      top="15vh"
      :before-close="handleClose">
      <el-descriptions :column="1" border size="small">
        <el-descriptions-item label="操作时间">{{ parseTime(detailData.operationTime) }}</el-descriptions-item>
        <el-descriptions-item label="操作类型">{{ getOperationTypeText(detailData.operationType) }}</el-descriptions-item>
        <el-descriptions-item label="操作分类">{{ getCategoryText(detailData.operationCategory) }}</el-descriptions-item>
        <el-descriptions-item label="涉及机器人">{{ detailData.robotName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="操作内容">{{ detailData.content }}</el-descriptions-item>
        <el-descriptions-item label="操作人">{{ detailData.operator }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="getStatusTag(detailData.status)" size="mini">
            {{ getStatusText(detailData.status) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ parseTime(detailData.createTime) }}</el-descriptions-item>
      </el-descriptions>
      <span slot="footer" class="dialog-footer">
        <el-button type="primary" @click="detailVisible = false">关 闭</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import { listHistory, clearHistory } from "@/api/mode/history";
import { listRobot } from "@/api/mode/robot";
import { parseTime } from "@/utils/ruoyi";

export default {
  name: "History",
  data() {
    return {
      loading: false,
      total: 0,
      historyList: [],
      robotOptions: [],
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        operationType: null,
        operationTypes: null,
        robotId: null,
        dateRange: null
      },
      detailVisible: false,
      detailTitle: '操作详情',
      detailData: {},
      statistics: [
        { type: 'total', label: '总记录', icon: 'fas fa-database', count: 0 },
        { type: 'emergency', label: '紧急操作', icon: 'fas fa-exclamation-triangle', count: 0 },
        { type: 'status', label: '状态操作', icon: 'fas fa-chart-line', count: 0 },
        { type: 'system', label: '系统操作', icon: 'fas fa-cogs', count: 0 }
      ],
      categoryTypeMap: {
        'emergency': ['emergency_stop', 'return_charge', 'batch_restart', 'emergency', 'emergency_evacuation'],
        'status': ['refresh_status', 'test_alert', 'clear_alerts', 'alert'],
        'system': ['standby_mode', 'maintenance_mode', 'charge_mode', 'mode-switch', 'config-change']
      },
      operationCategoryMap: {
        'emergency_stop': 'emergency',
        'return_charge': 'emergency',
        'batch_restart': 'emergency',
        'emergency_evacuation': 'emergency',
        'refresh_status': 'status',
        'test_alert': 'status',
        'clear_alerts': 'status',
        'standby_mode': 'system',
        'maintenance_mode': 'system',
        'charge_mode': 'system',
        'mode-switch': 'system',
        'config-change': 'system',
        'alert': 'status',
        'emergency': 'emergency'
      },
      operationTypeTextMap: {
        'emergency_stop': '紧急停止',
        'return_charge': '返回充电',
        'batch_restart': '批量重启',
        'emergency_evacuation': '紧急撤离',
        'refresh_status': '刷新状态',
        'test_alert': '测试告警',
        'clear_alerts': '清除告警',
        'standby_mode': '待机模式',
        'maintenance_mode': '维护模式',
        'charge_mode': '充电模式',
        'mode-switch': '模式切换',
        'config-change': '配置修改',
        'alert': '告警记录',
        'emergency': '紧急操作'
      },
      operationTypeTagMap: {
        'emergency_stop': 'danger',
        'return_charge': 'danger',
        'batch_restart': 'danger',
        'emergency_evacuation': 'danger',
        'emergency': 'danger',
        'refresh_status': 'warning',
        'test_alert': 'warning',
        'clear_alerts': 'warning',
        'alert': 'warning',
        'standby_mode': 'primary',
        'maintenance_mode': 'primary',
        'charge_mode': 'primary',
        'mode-switch': 'primary',
        'config-change': 'primary'
      }
    };
  },
  created() {
    this.getList();
    this.getRobotList();
    this.loadFullStatistics();
  },
  methods: {
    parseTime,

    /** 加载全量统计数据（不受分类筛选影响） */
    loadFullStatistics() {
      const statsParams = {
        pageNum: 1,
        pageSize: 10000
      };

      if (this.queryParams.dateRange) {
        statsParams.beginTime = this.queryParams.dateRange[0];
        statsParams.endTime = this.queryParams.dateRange[1];
      }

      if (this.queryParams.robotId) {
        statsParams.robotId = this.queryParams.robotId;
      }

      listHistory(statsParams).then(response => {
        const allData = response.rows || [];

        allData.forEach(item => {
          item.operationCategory = this.getOperationCategory(item.operationType);
        });

        this.statistics[0].count = allData.length;
        this.statistics[1].count = allData.filter(h => h.operationCategory === 'emergency').length;
        this.statistics[2].count = allData.filter(h => h.operationCategory === 'status').length;
        this.statistics[3].count = allData.filter(h => h.operationCategory === 'system').length;
      }).catch(error => {
        console.error('获取统计数据失败:', error);
      });
    },

    /** 查询历史记录列表（应用筛选） */
    getList() {
      this.loading = true;

      const params = {
        pageNum: this.queryParams.pageNum,
        pageSize: this.queryParams.pageSize,
        robotId: this.queryParams.robotId
      };

      if (this.queryParams.dateRange) {
        params.beginTime = this.queryParams.dateRange[0];
        params.endTime = this.queryParams.dateRange[1];
      }

      if (this.queryParams.operationTypes && this.queryParams.operationTypes.length > 0) {
        params.operationType = this.queryParams.operationTypes.join(',');
      } else if (this.queryParams.operationType) {
        params.operationType = this.queryParams.operationType;
      }

      listHistory(params).then(response => {
        this.historyList = response.rows || [];
        this.total = response.total || 0;

        this.historyList.forEach(item => {
          item.operationCategory = this.getOperationCategory(item.operationType);
        });

        this.loading = false;
      }).catch(error => {
        console.error('获取历史记录失败:', error);
        this.loading = false;
      });
    },

    /** 查询机器人列表 - 兼容后端返回的字段名 */
    getRobotList() {
      listRobot({ pageNum: 1, pageSize: 100 }).then(response => {
        const rows = response.rows || [];
        // 转换数据格式，统一为 robotId 和 robotName
        this.robotOptions = rows.map(robot => ({
          robotId: robot.id || robot.robotId,
          robotName: robot.name || robot.robotName,
          robotCode: robot.code || robot.robotCode
        }));
        console.log('机器人列表加载完成:', this.robotOptions);
      }).catch(error => {
        console.error('获取机器人列表失败:', error);
      });
    },

    /** 获取操作类型所属分类 */
    getOperationCategory(operationType) {
      return this.operationCategoryMap[operationType] || 'system';
    },

    /** 搜索按钮操作 */
    handleQuery() {
      this.queryParams.pageNum = 1;
      this.getList();
      this.loadFullStatistics();
    },

    /** 重置按钮操作 */
    resetQuery() {
      this.queryParams = {
        pageNum: 1,
        pageSize: 10,
        operationType: null,
        operationTypes: null,
        robotId: null,
        dateRange: null
      };
      this.handleQuery();
    },

    /** 清空记录 */
    handleClear() {
      this.$confirm('确定要清空所有历史记录吗？此操作不可恢复。', '警告', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        clearHistory().then(() => {
          this.$message.success('清空成功');
          this.getList();
          this.loadFullStatistics();
        });
      }).catch(() => {});
    },

    /** 按分类筛选 */
    filterByType(type) {
      if (type === 'total') {
        this.queryParams.operationType = null;
        this.queryParams.operationTypes = null;
      } else {
        this.queryParams.operationTypes = this.categoryTypeMap[type] || [];
        this.queryParams.operationType = null;
      }
      this.queryParams.pageNum = 1;
      this.getList();
    },

    /** 关闭对话框 */
    handleClose(done) {
      done();
    },

    /** 获取操作类型标签类型（颜色） */
    getOperationTypeTag(type) {
      return this.operationTypeTagMap[type] || 'info';
    },

    /** 获取操作类型文本（中文） */
    getOperationTypeText(type) {
      return this.operationTypeTextMap[type] || type;
    },

    /** 获取分类标签类型 */
    getCategoryTag(category) {
      const map = {
        'emergency': 'danger',
        'status': 'warning',
        'system': 'primary'
      };
      return map[category] || 'info';
    },

    /** 获取分类文本 */
    getCategoryText(category) {
      const map = {
        'emergency': '紧急操作',
        'status': '状态操作',
        'system': '系统操作'
      };
      return map[category] || category;
    },

    /** 获取状态标签类型 - 失败状态显示为红色(danger) */
    getStatusTag(status) {
      const map = {
        'success': 'success',
        'warning': 'warning',
        'danger': 'danger',
        'fail': 'danger',      // 失败状态改为红色
        'error': 'danger',     // 错误状态改为红色
        'failed': 'danger'     // failed 状态改为红色
      };
      return map[status] || 'info';
    },

    /** 获取状态文本（中文） */
    getStatusText(status) {
      const map = {
        'success': '成功',
        'warning': '告警',
        'danger': '紧急',
        'fail': '失败',
        'error': '错误',
        'failed': '失败'
      };
      return map[status] || status;
    },

    /** 查看详情 */
    viewDetail(row) {
      this.detailData = row;
      this.detailVisible = true;
    }
  }
};
</script>

<style scoped>
.card {
  background: white;
  border-radius: 10px;
  box-shadow: 0px 2px 2px rgba(0, 0, 0, 0.5);
  margin-bottom: 24px;
  overflow: hidden;
  position: relative;
  z-index: 1;
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
  display: flex;
  align-items: center;
  gap: 8px;
}

.card-title i {
  color: #3976E4;
}

.card-body {
  padding: 24px 20px;
}

.filter-bar {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 24px;
  flex-wrap: wrap;
  background-color: #f8f9fa;
  padding: 16px;
  border-radius: 8px;
  border: 1px solid #E5E7EB;
}

.filter-bar i {
  color: #3976E4;
  margin-right: 4px;
}

.stats-cards {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  margin-bottom: 24px;
}

.stat-card {
  background: white;
  border-radius: 8px;
  padding: 16px;
  box-shadow: 0px 2px 2px rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  gap: 12px;
  cursor: pointer;
  transition: all 0.2s;
  border-left: 4px solid transparent;
}

.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.stat-card.total {
  border-left-color: #3976E4;
}

.stat-card.total .stat-icon {
  color: #3976E4;
}

.stat-card.emergency {
  border-left-color: #f56c6c;
}

.stat-card.emergency .stat-icon {
  color: #f56c6c;
}

.stat-card.status {
  border-left-color: #e6a23c;
}

.stat-card.status .stat-icon {
  color: #e6a23c;
}

.stat-card.system {
  border-left-color: #67c23a;
}

.stat-card.system .stat-icon {
  color: #67c23a;
}

.stat-icon {
  font-size: 28px;
  margin-right: 8px;
  min-width: 40px;
  text-align: center;
}

.stat-content {
  flex: 1;
}

.stat-value {
  font-size: 24px;
  font-weight: 700;
  line-height: 1.2;
  color: #4D4D4D;
}

.stat-label {
  font-size: 14px;
  color: #808080;
  margin-top: 4px;
}

.pagination-wrap {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}

.el-table {
  border: 1px solid #E5E7EB !important;
  border-radius: 8px;
  overflow: hidden;
}

.el-table th.el-table__cell {
  background: #F5F5F5 !important;
  color: #4D4D4D !important;
  font-weight: 500 !important;
  border-bottom: 1px solid #E0E0E0 !important;
}

.el-table td.el-table__cell {
  border-bottom: 1px solid #F0F0F0 !important;
}

/* 详情对话框样式优化 */
::v-deep .el-dialog {
  border-radius: 8px;
}

::v-deep .el-dialog .el-dialog__body {
  padding: 15px 20px;
}

::v-deep .el-descriptions {
  font-size: 13px;
}

::v-deep .el-descriptions .el-descriptions__label {
  width: 100px;
  background-color: #fafafa;
}

::v-deep .el-descriptions .el-descriptions__cell {
  padding: 8px 12px;
}

::v-deep .el-dialog__footer {
  padding: 10px 20px 15px;
  border-top: 1px solid #EBEEF5;
}

/* 响应式样式 */
@media (max-width: 992px) {
  .stats-cards {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 768px) {
  .stats-cards {
    grid-template-columns: repeat(2, 1fr);
  }

  .filter-bar {
    flex-direction: column;
    align-items: stretch;
  }

  .filter-bar .el-select,
  .filter-bar .el-date-picker,
  .filter-bar .el-button {
    width: 100% !important;
  }

  .card-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
  }
}

@media (max-width: 576px) {
  .stats-cards {
    grid-template-columns: 1fr;
  }

  ::v-deep .el-dialog {
    width: 90% !important;
  }
}
</style>
