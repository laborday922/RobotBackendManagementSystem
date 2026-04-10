<template>
  <div class="app-container">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="page-title">
        <i class="el-icon-s-data"></i>
        <span>机器人位置历史管理</span>
      </div>
      <div class="page-description">
        <el-alert
          title="选择机器人查看其位置历史记录"
          type="info"
          :closable="false"
          show-icon
          class="page-alert">
        </el-alert>
      </div>
    </div>

    <!-- 机器人列表卡片 -->
    <el-card class="robot-list-card" shadow="never">
      <div slot="header" class="clearfix">
        <span class="card-title">
          <i class="el-icon-robot"></i>
          机器人列表
        </span>
      </div>

      <el-table
        v-loading="loading"
        :data="robotList"
        style="width: 100%"
        stripe
        highlight-current-row
        :header-cell-style="{background:'#f5f7fa',color:'#606266'}"
        class="robot-table">
        <el-table-column label="编号" align="center" prop="code" width="150" show-overflow-tooltip>
          <template slot-scope="scope">
            <div class="robot-code-cell">
              <el-tag type="primary" size="small" class="robot-code-tag">
                <i class="el-icon-price-tag"></i>
                {{ scope.row.code }}
              </el-tag>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="名称" align="center" prop="name" width="200" show-overflow-tooltip>
          <template slot-scope="scope">
            <div class="robot-name-cell">
              <i class="el-icon-monitor robot-icon"></i>
              <span class="robot-name-text">{{ scope.row.name }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="状态" align="center" prop="status" width="120" show-overflow-tooltip>
          <template slot-scope="scope">
            <div class="status-cell">
              <el-tag
                :type="getStatusTagType(scope.row.status)"
                size="small"
                class="status-tag">
                <i :class="getStatusIcon(scope.row.status)"></i>
                {{ getStatusText(scope.row.status) }}
              </el-tag>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="操作" align="center" width="120">
          <template slot-scope="scope">
            <el-button
              size="mini"
              type="primary"
              icon="el-icon-view"
              @click="showLocationHistory(scope.row)"
              class="view-btn">
              查看历史
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 位置历史信息弹窗 -->
    <el-dialog
      title="位置历史信息"
      :visible="historyDialogVisible"
      width="90%"
      append-to-body
      class="history-dialog"
      :close-on-click-modal="false"
      :close-on-press-escape="false"
      @close="closeHistoryDialog"
      @closed="clearHistoryData">
      <div class="dialog-header" v-if="currentRobot">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="机器人编号">
            <el-tag type="primary">{{ currentRobot.code }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="机器人名称">
            <i class="el-icon-monitor"></i>
            {{ currentRobot.name }}
          </el-descriptions-item>
        </el-descriptions>
      </div>

      <el-table
        v-loading="historyLoading"
        :data="historyList"
        style="width: 100%"
        stripe
        :header-cell-style="{background:'#f0f9ff',color:'#409eff'}"
        class="history-table">
        <el-table-column label="记录时间" align="center" prop="recordTime" width="180" show-overflow-tooltip>
          <template slot-scope="scope">
            <div class="time-cell">
              <i class="el-icon-time"></i>
              <span>{{ parseTime(scope.row.recordTime, '{y}-{m}-{d} {h}:{i}') }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="位置区域" align="center" prop="locationArea" width="120" show-overflow-tooltip>
          <template slot-scope="scope">
            <el-tag type="warning" size="small">{{ scope.row.locationArea }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="具体位置" align="center" prop="specificLocation" width="150" show-overflow-tooltip>
          <template slot-scope="scope">
            <i class="el-icon-location-information"></i>
            <span style="margin-left: 5px;">{{ scope.row.specificLocation }}</span>
          </template>
        </el-table-column>
        <el-table-column label="坐标X" align="center" prop="coordinateX" width="100" show-overflow-tooltip>
          <template slot-scope="scope">
            <el-tag type="info" size="mini">{{ scope.row.coordinateX }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="坐标Y" align="center" prop="coordinateY" width="100" show-overflow-tooltip>
          <template slot-scope="scope">
            <el-tag type="info" size="mini">{{ scope.row.coordinateY }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="移动速度" align="center" prop="moveSpeed" width="100" show-overflow-tooltip>
          <template slot-scope="scope">
            <el-tag type="success" size="mini">
              <i class="el-icon-s-promotion"></i>
              {{ scope.row.moveSpeed }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态描述" align="center" prop="statusDesc" show-overflow-tooltip>
          <template slot-scope="scope">
            <el-tooltip :content="scope.row.statusDesc" placement="top">
              <span class="status-text">{{ scope.row.statusDesc }}</span>
            </el-tooltip>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrapper" v-show="historyTotal>0">
        <pagination
          :total="historyTotal"
          :page.sync="historyQuery.pageNum"
          :limit.sync="historyQuery.pageSize"
          @pagination="getHistoryList"
        />
      </div>

      <div slot="footer" class="dialog-footer">
        <el-button @click="closeHistoryDialog" icon="el-icon-close">关 闭</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<style lang="scss" scoped>
.app-container {
  padding: 20px;
  background-color: #f5f7fa;
  min-height: calc(100vh - 84px);
  display: flex;
  flex-direction: column;
  align-items: center;
  max-width: 1200px;
  margin: 0 auto;
}

// 页面头部样式
.page-header {
  margin-bottom: 20px;
  width: 100%;
  max-width: 1000px;

  .page-title {
    display: flex;
    align-items: center;
    font-size: 28px;
    font-weight: 600;
    color: #303133;
    margin-bottom: 15px;

    i {
      color: #409eff;
      margin-right: 12px;
      font-size: 32px;
    }
  }

  .page-description {
    .page-alert {
      ::v-deep .el-alert__description {
        color: #606266;
        font-size: 15px;
      }
    }
  }
}

// 机器人列表卡片样式
.robot-list-card {
  width: fit-content;
  min-width: 600px;
  margin: 0 auto 0 100px;

  ::v-deep .el-card__header {
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    color: white;
    padding: 16px 20px;

    .card-title {
      display: flex;
      align-items: center;
      font-size: 16px;
      font-weight: 500;

      i {
        margin-right: 8px;
        font-size: 18px;
      }
    }
  }

  ::v-deep .el-card__body {
    padding: 0;
  }
}

// 表格样式
.robot-table {
  ::v-deep .el-table__row {
    &:hover {
      background-color: #ecf5ff;
    }
  }

  ::v-deep .el-table__cell {
    padding: 12px 0;
    font-size: 14px;
  }

  ::v-deep .el-table__header {
    th {
      padding: 12px 0;
      font-size: 14px;
      font-weight: 600;
    }
  }
}

// 机器人单元格样式
.robot-code-cell {
  display: flex;
  justify-content: center;
  align-items: center;

  .robot-code-tag {
    font-weight: 600;
    letter-spacing: 0.5px;
    font-size: 13px;

    i {
      margin-right: 4px;
    }
  }
}

.robot-name-cell {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;

  .robot-icon {
    font-size: 18px;
    color: #409eff;
    margin-bottom: 4px;
  }

  .robot-name-text {
    font-size: 13px;
    font-weight: 500;
    color: #303133;
    text-align: center;
    line-height: 1.4;
  }
}

// 状态单元格样式
.status-cell {
  display: flex;
  justify-content: center;
  align-items: center;

  .status-tag {
    font-size: 12px;

    i {
      margin-right: 3px;
    }
  }
}

// 按钮样式
.view-btn {
  transition: all 0.3s ease;

  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 4px 12px rgba(64, 158, 255, 0.3);
  }
}

// 弹窗样式
.history-dialog {
  ::v-deep .el-dialog {
    border-radius: 8px;
    overflow: hidden;
    transition: all 0.15s ease-out !important;
  }

  ::v-deep .el-dialog__wrapper {
    transition: all 0.15s ease-out !important;
  }

  ::v-deep .el-dialog__header {
    background: linear-gradient(135deg, #409eff 0%, #66b1ff 100%);
    color: white;
    margin: 0;
    padding: 20px;

    .el-dialog__title {
      font-weight: 500;
    }

    .el-dialog__headerbtn {
      top: 15px;
      right: 20px;

      .el-dialog__close {
        color: white;
        font-size: 20px;

        &:hover {
          color: #e6a23c;
        }
      }
    }
  }

  ::v-deep .el-dialog__body {
    padding: 20px;
  }
}

// 弹窗头部信息样式
.dialog-header {
  margin-bottom: 20px;

  ::v-deep .el-descriptions {
    .el-descriptions__title {
      font-weight: 600;
      color: #409eff;
    }

    .el-descriptions-item__label {
      background-color: #f5f7fa;
      font-weight: 500;
    }
  }
}

// 历史表格样式
.history-table {
  ::v-deep .el-table__header-wrapper {
    background-color: #f0f9ff;
  }

  ::v-deep .el-table__row {
    &:hover {
      background-color: #f0f9ff;
    }
  }

  ::v-deep .el-table__cell {
    padding: 10px 0;
  }
}

// 时间单元格样式
.time-cell {
  display: flex;
  align-items: center;

  i {
    color: #909399;
    margin-right: 5px;
  }

  span {
    color: #606266;
    font-weight: 500;
  }
}

// 状态文本样式
.status-text {
  display: inline-block;
  max-width: 200px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  cursor: pointer;
  color: #606266;

  &:hover {
    color: #409eff;
  }
}

// 分页样式
.pagination-wrapper {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}

// 弹窗底部样式
.dialog-footer {
  text-align: center;

  .el-button {
    padding: 10px 30px;
    font-weight: 500;
    transition: all 0.3s ease;

    &:hover {
      transform: translateY(-1px);
    }
  }
}

// 响应式设计
@media (max-width: 768px) {
  .app-container {
    padding: 10px;
    max-width: 100%;
  }

  .page-title {
    font-size: 22px;

    i {
      font-size: 26px;
    }
  }

  .robot-list-card {
    ::v-deep .el-card__header {
      padding: 12px 16px;

      .card-title {
        font-size: 14px;
      }
    }
  }

  .robot-table {
    ::v-deep .el-table__cell {
      padding: 8px 0;
      font-size: 12px;
    }

    ::v-deep .el-table__header {
      th {
        padding: 8px 0;
        font-size: 12px;
      }
    }
  }

  .robot-name-text {
    font-size: 11px;
  }

  .robot-code-tag {
    font-size: 11px;
  }

  .status-tag {
    font-size: 10px;
  }

  .history-dialog {
    ::v-deep .el-dialog {
      width: 95% !important;
      margin: 5vh auto;
    }
  }
}

// 动画效果
@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.robot-list-card,
.history-dialog {
  animation: fadeInUp 0.5s ease-out;
}

// 加载状态样式
::v-deep .el-loading-mask {
  background-color: rgba(255, 255, 255, 0.8);
}
</style>

<script>
import { listRobots } from "@/api/robots/robots"
import { listHistory } from "@/api/robots/history"

export default {
  name: "History",
  data() {
    return {
      // 遮罩层
      loading: true,
      // 机器人列表
      robotList: [],
      // 历史弹窗相关
      historyDialogVisible: false,
      historyLoading: false,
      historyList: [],
      historyTotal: 0,
      currentRobot: null,
      // 历史查询参数
      historyQuery: {
        pageNum: 1,
        pageSize: 10,
        robotId: null
      }
    }
  },
  created() {
    this.getRobotList()
  },
  methods: {
    /** 查询机器人列表 */
    getRobotList() {
      this.loading = true
      listRobots({}).then(response => {
        this.robotList = response.rows || response.data || []
        this.loading = false
      }).catch(error => {
        console.error('查询机器人列表失败:', error)
        this.loading = false
        this.$modal.msgError("获取机器人列表失败")
      })
    },
    /** 显示位置历史弹窗 */
    showLocationHistory(robot) {
      this.currentRobot = robot
      this.historyDialogVisible = true
      this.historyQuery.robotId = robot.id
      this.historyQuery.pageNum = 1
      this.getHistoryList()
    },
    /** 查询位置历史列表 */
    getHistoryList() {
      this.historyLoading = true
      listHistory(this.historyQuery).then(response => {
        this.historyList = response.rows || []
        this.historyTotal = response.total || 0
        this.historyLoading = false
      }).catch(error => {
        console.error('查询位置历史失败:', error)
        this.historyLoading = false
        this.$modal.msgError("获取历史数据失败")
      })
    },
    /** 关闭弹窗（仅隐藏） */
    closeHistoryDialog() {
      this.historyDialogVisible = false
    },
    /** 弹窗动画完全关闭后清空数据 */
    clearHistoryData() {
      this.historyList = []
      this.historyTotal = 0
      this.currentRobot = null
      this.historyQuery.pageNum = 1
    },
    /** 获取状态文本 */
    getStatusText(status) {
      if (status === 1) return '在线'
      if (status === 0) return '离线'
      return '待激活'
    },
    /** 获取状态标签类型 */
    getStatusTagType(status) {
      if (status === 1) return 'success'
      if (status === 0) return 'info'
      return 'warning'
    },
    /** 获取状态图标 */
    getStatusIcon(status) {
      if (status === 1) return 'el-icon-circle-check'
      if (status === 0) return 'el-icon-circle-close'
      return 'el-icon-warning'
    }
  }
}
</script>