<template>
  <div class="app-container">
    <el-card class="search-card" shadow="never">
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
          style="width: 300px;"
        />

        <el-select v-model="queryParams.robotId" placeholder="机器人" clearable filterable style="width: 150px;">
          <el-option
            v-for="robot in robotOptions"
            :key="robot.id || robot.robotId"
            :label="robot.name || robot.robotName"
            :value="robot.id || robot.robotId"
          />
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
    </el-card>

    <el-row :gutter="16" class="stats-cards">
      <el-col v-for="stat in statistics" :key="stat.type" :xs="24" :sm="12" :md="6">
        <el-card class="stat-card" shadow="hover" @click.native="filterByType(stat.type)">
          <div class="stat-icon" :class="getStatIconColor(stat.type)">
            <i :class="stat.icon"></i>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ stat.count }}</div>
            <div class="stat-label">{{ stat.label }}</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-card class="table-card" shadow="never">
      <template #header>
        <div class="card-header">
          <span>历史记录</span>
        </div>
      </template>

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

      <div class="pagination-wrap">
        <pagination
          v-show="total > 0"
          :total="total"
          :page.sync="queryParams.pageNum"
          :limit.sync="queryParams.pageSize"
          @pagination="getList"
        />
      </div>
    </el-card>

    <el-dialog
      :title="detailTitle"
      :visible.sync="detailVisible"
      width="500px"
      top="15vh"
      :before-close="handleClose"
    >
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
    },

    getStatIconColor(type) {
      if (type === 'total') return 'blue'
      if (type === 'emergency') return 'red'
      if (type === 'status') return 'orange'
      if (type === 'system') return 'green'
      return 'blue'
    }
  }
};
</script>

<style scoped>
.app-container {
  padding: 20px;
}

.search-card {
  margin-bottom: 20px;
}

.filter-bar {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.filter-bar i {
  color: #3976E4;
  margin-right: 4px;
}

.stats-cards {
  margin-bottom: 20px;
}

.stat-card {
  cursor: pointer;
  border-radius: 8px;
  overflow: hidden;
}

::v-deep .stat-card .el-card__body {
  display: flex;
  align-items: center;
  padding: 16px;
}

.stat-icon {
  width: 48px;
  height: 48px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 22px;
  margin-right: 16px;
  flex-shrink: 0;
}

.stat-icon.blue {
  background-color: #e6f7ff;
  color: #1890ff;
}

.stat-icon.green {
  background-color: #f6ffed;
  color: #52c41a;
}

.stat-icon.orange {
  background-color: #fff7e6;
  color: #fa8c16;
}

.stat-icon.red {
  background-color: #fff1f0;
  color: #ff4d4f;
}

.stat-info {
  flex: 1;
}

.stat-value {
  font-size: 24px;
  font-weight: 700;
  color: #333;
  line-height: 1.2;
}

.stat-label {
  font-size: 14px;
  color: #666;
  margin-top: 4px;
}

.table-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
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

@media (max-width: 768px) {
  .filter-bar {
    flex-direction: column;
    align-items: stretch;
  }

  .filter-bar .el-select,
  .filter-bar .el-date-picker,
  .filter-bar .el-button {
    width: 100% !important;
  }

  ::v-deep .el-dialog {
    width: 90% !important;
  }
}
</style>
