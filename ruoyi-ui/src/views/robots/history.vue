<template>
  <div class="app-container">
    <el-card class="table-card" shadow="never">
      <template #header>
        <div class="card-header">
          <span>机器人位置历史管理</span>
        </div>
      </template>

      <el-table
        v-loading="loading"
        :data="robotList"
        style="width: 100%"
        border
      >
        <el-table-column label="编号" align="center" prop="code" min-width="150" show-overflow-tooltip />
        <el-table-column label="名称" align="center" prop="name" min-width="200" show-overflow-tooltip />
        <el-table-column label="状态" align="center" prop="status" min-width="120" show-overflow-tooltip>
          <template slot-scope="scope">
            <el-tag :type="getStatusTagType(scope.row.status)" size="small">
              {{ getStatusText(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" align="center" width="120">
          <template slot-scope="scope">
            <el-button
              size="mini"
              type="primary"
              icon="el-icon-view"
              @click="showLocationHistory(scope.row)"
            >
              查看历史
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 位置历史信息弹窗 -->
    <el-dialog
      title="位置历史信息"
      :visible.sync="historyDialogVisible"
      width="60%"
      append-to-body
      :close-on-click-modal="false"
      :close-on-press-escape="false"
      @closed="clearHistoryData"
    >
      <el-descriptions v-if="currentRobot" :column="2" border style="margin-bottom: 16px;">
        <el-descriptions-item label="机器人编号">{{ currentRobot.code }}</el-descriptions-item>
        <el-descriptions-item label="机器人名称">{{ currentRobot.name }}</el-descriptions-item>
      </el-descriptions>

      <el-table
        v-loading="historyLoading"
        :data="historyList"
        style="width: 100%"
        border
      >
        <el-table-column label="记录时间" align="center" prop="recordTime" width="180" show-overflow-tooltip>
          <template slot-scope="scope">
            <span>{{ parseTime(scope.row.recordTime, '{y}-{m}-{d} {h}:{i}') }}</span>
          </template>
        </el-table-column>
        <el-table-column label="具体位置" align="center" prop="specificLocation" min-width="180" show-overflow-tooltip>
          <template slot-scope="scope">
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
        <el-table-column label="移动速度" align="center" prop="moveSpeed" width="120" show-overflow-tooltip>
          <template slot-scope="scope">
            <el-tag type="success" size="mini">
              {{ scope.row.moveSpeed }}
            </el-tag>
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
        <el-button @click="historyDialogVisible = false" icon="el-icon-close">关 闭</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<style lang="scss" scoped>
.app-container {
  padding: 20px;
}

.table-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.pagination-wrapper {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}

.dialog-footer {
  text-align: center;
}

@media (max-width: 768px) {
  .app-container {
    padding: 10px;
  }
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
