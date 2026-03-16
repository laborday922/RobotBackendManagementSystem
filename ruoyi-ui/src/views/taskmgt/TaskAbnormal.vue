<template>
  <div class="app-container">
    <!-- 统计卡片 -->
    <el-row :gutter="16" class="stats-cards">
      <el-col :span="6">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-icon red"><i class="fas fa-exclamation-triangle"></i></div>
          <div class="stat-info">
            <div class="stat-value">{{ highRiskCount }}</div>
            <div class="stat-label">高风险</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-icon orange"><i class="fas fa-exclamation-circle"></i></div>
          <div class="stat-info">
            <div class="stat-value">{{ riskCount }}</div>
            <div class="stat-label">风险</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-icon blue"><i class="fas fa-robot"></i></div>
          <div class="stat-info">
            <div class="stat-value">{{ abnormalRobotCount }}</div>
            <div class="stat-label">异常机器人</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-icon green"><i class="fas fa-check-circle"></i></div>
          <div class="stat-info">
            <div class="stat-value">{{ resolvedToday }}</div>
            <div class="stat-label">今日已处理</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 主卡片 -->
    <el-card class="table-card">
      <template #header>
        <div class="card-header">
          <span><i class="fas fa-exclamation-triangle" style="color:#ff4d4f;"></i> 异常任务列表</span>
          <el-button type="text" @click="refreshList">
            <i class="fas fa-sync-alt"></i> 刷新
          </el-button>
        </div>
      </template>

      <!-- 筛选栏 -->
      <div class="filter-bar">
        <el-select
          v-model="queryParams.riskLevel"
          placeholder="风险等级"
          clearable
          style="width:150px"
          @change="handleQuery"
        >
          <el-option label="高风险" :value="1" />
          <el-option label="风险" :value="2" />
        </el-select>
        <el-select
          v-model="queryParams.robotId"
          placeholder="异常机器人"
          clearable
          style="width:180px; margin-left:10px;"
          @change="handleQuery"
        >
          <el-option
            v-for="r in abnormalRobots"
            :key="r.id"
            :label="r.name"
            :value="r.id"
          />
        </el-select>
        <el-button style="margin-left:10px;" @click="resetQuery">重置</el-button>
      </div>

      <!-- 表格 -->
      <el-table
        v-loading="loading"
        :data="abnormalList"
        border
        style="width:100%"
        row-key="id"
        :row-class-name="tableRowClassName"
      >
        <el-table-column prop="id" label="ID" width="80" align="center" />
        <el-table-column prop="name" label="任务名称" min-width="180" show-overflow-tooltip />
        <el-table-column label="风险等级" width="120" align="center">
          <template #default="scope">
            <el-tag v-if="scope.row.riskLevel === 1" type="danger" effect="dark">高风险</el-tag>
            <el-tag v-else-if="scope.row.riskLevel === 2" type="warning">风险</el-tag>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column label="任务状态" width="100" align="center">
          <template #default="scope">
            <span :class="'status-tag status-' + getStatusClass(scope.row.status)">
              {{ getStatusText(scope.row.status) }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="异常原因" width="150" show-overflow-tooltip>
          <template #default="scope">
            <div v-if="scope.row.robotStatus">
              <i v-if="scope.row.robotStatus === 'offline'" class="fas fa-wifi" style="color:#ff4d4f;">离线</i>
              <i v-else-if="scope.row.robotStatus === 'fault'" class="fas fa-times-circle" style="color:#ff4d4f;">故障</i>
              <i v-else-if="scope.row.robotStatus === 'low_battery'" class="fas fa-battery-quarter" style="color:#faad14;">电量低</i>
              <span v-else>{{ scope.row.robotStatus }}</span>
            </div>
            <div v-else>-</div>
          </template>
        </el-table-column>
        <el-table-column prop="robotName" label="原机器人" width="120" show-overflow-tooltip />
        <el-table-column label="进度" width="120" align="center">
          <template #default="scope">
            {{ scope.row.completedSteps || 0 }}/{{ scope.row.totalSteps || 5 }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="350" fixed="right" align="center">
          <template #default="scope">
            <el-button link type="primary" icon="View" @click="handleView(scope.row)">详情</el-button>

            <!-- 高风险任务操作 -->
            <template v-if="scope.row.riskLevel === 1">
              <!-- 执行中任务可中止 -->
              <el-button
                link
                type="warning"
                icon="Pause"
                @click="handleAbortHighRisk(scope.row)"
                v-if="scope.row.status === 2"
              >中止</el-button>
              <!-- 已暂停任务的操作组 -->
              <template v-else-if="scope.row.status === 3">
                <el-button link type="primary" icon="Refresh" @click="showReassignDialog(scope.row)">重新分配</el-button>
                <!-- 恢复任务（继续执行，不改变风险） -->
                <el-button link type="success" icon="VideoPlay" @click="handleResume(scope.row)">恢复</el-button>
                <!-- 解决风险（仅当机器人状态正常时可用） -->
                <el-button
                  link
                  :type="isRobotNormal(scope.row) ? 'success' : 'info'"
                  icon="Check"
                  :disabled="!isRobotNormal(scope.row)"
                  @click="handleResolveRisk(scope.row)"
                >解决风险</el-button>
                <el-button link type="danger" icon="Ban" @click="handleTerminate(scope.row)">终止</el-button>
              </template>
            </template>

            <!-- 普通风险任务操作 -->
            <template v-else-if="scope.row.riskLevel === 2">
              <el-button link type="primary" icon="Refresh" @click="showReassignDialog(scope.row)">重新分配</el-button>
              <!-- 如果任务是暂停状态，显示恢复按钮 -->
              <el-button
                v-if="scope.row.status === 3"
                link
                type="success"
                icon="VideoPlay"
                @click="handleResume(scope.row)"
              >恢复</el-button>
              <!-- 解决风险（仅当机器人状态正常时可用） -->
              <el-button
                link
                :type="isRobotNormal(scope.row) ? 'success' : 'info'"
                icon="Check"
                :disabled="!isRobotNormal(scope.row)"
                @click="handleResolveRisk(scope.row)"
              >解决风险</el-button>
            </template>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <pagination
        v-show="total > 0"
        :total="total"
        v-model:page="queryParams.pageNum"
        v-model:limit="queryParams.pageSize"
        @pagination="getList"
      />
    </el-card>

    <!-- 重新分配对话框（与之前相同） -->
    <el-dialog
      title="重新分配任务"
      v-model="reassignDialog.visible"
      width="600px"
      append-to-body
    >
      <div v-if="reassignDialog.task">
        <el-alert
          v-if="reassignDialog.task.riskLevel === 1"
          title="高风险任务"
          type="error"
          description="此任务已被中止，需要重新分配"
          show-icon
          style="margin-bottom:16px;"
        />
        <div style="margin-bottom:16px; padding:12px; background:#f5f5f5; border-radius:4px;">
          <div><strong>任务名称：</strong>{{ reassignDialog.task.name }}</div>
          <div><strong>原机器人：</strong>{{ reassignDialog.task.robotName }}</div>
          <div><strong>适用组：</strong>{{ getRobotGroupNames(reassignDialog.task.robotGroupIds) }}</div>
        </div>
        <div style="margin-bottom:16px;">
          <div style="font-weight:bold; margin-bottom:8px;">可用机器人</div>
          <el-radio-group v-model="reassignDialog.selectedRobotId">
            <div
              v-for="robot in availableRobotsForReassign"
              :key="robot.id"
              class="robot-option"
            >
              <el-radio :label="robot.id" style="width:100%;">
                <div style="display:flex; align-items:center;">
                  <i class="fas fa-robot" style="font-size:24px; color:#1890ff; margin-right:12px;"></i>
                  <div>
                    <div style="font-weight:bold;">{{ robot.name }}</div>
                    <div style="font-size:12px; color:#666;">
                      <span><i class="fas fa-battery-full"></i> {{ robot.battery }}%</span>
                      <span style="margin-left:12px;"><i class="fas fa-map-marker-alt"></i> {{ robot.location }}</span>
                      <span style="margin-left:12px; color:#52c41a;"><i class="fas fa-circle" style="font-size:8px;"></i> 在线</span>
                    </div>
                  </div>
                </div>
              </el-radio>
            </div>
          </el-radio-group>
          <div v-if="availableRobotsForReassign.length === 0" class="empty-tip">
            <i class="fas fa-robot" style="font-size:24px; margin-bottom:8px;"></i>
            <div>当前没有可用的机器人</div>
          </div>
        </div>
      </div>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="reassignDialog.visible = false">取 消</el-button>
          <el-button
            type="primary"
            @click="confirmReassign"
            :disabled="!reassignDialog.selectedRobotId"
          >确认分配</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 任务详情对话框（简化） -->
    <el-dialog title="任务详情" v-model="detailVisible" width="700px" append-to-body>
      <div v-if="currentTask">
        <el-descriptions :column="1" border>
          <el-descriptions-item label="任务名称">{{ currentTask.name }}</el-descriptions-item>
          <el-descriptions-item label="状态">{{ getStatusText(currentTask.status) }}</el-descriptions-item>
          <el-descriptions-item label="风险等级">
            <el-tag v-if="currentTask.riskLevel === 1" type="danger">高风险</el-tag>
            <el-tag v-else-if="currentTask.riskLevel === 2" type="warning">风险</el-tag>
            <span v-else>无</span>
          </el-descriptions-item>
          <el-descriptions-item label="机器人">{{ currentTask.robotName }}</el-descriptions-item>
        </el-descriptions>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  listAbnormalTask,
  resolveRisk,
  updateTask,
  terminateTask,
  getTask,
  pauseTask,
  continueTask // 继续任务接口
} from '@/api/taskmgt'
import { listRobot } from '@/api/system/robot'

// ========== 查询参数 ==========
const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  riskLevel: undefined,
  robotId: undefined,
  robotGroupId: undefined
})

// ========== 数据 ==========
const loading = ref(false)
const abnormalList = ref([])
const total = ref(0)

// 机器人列表（用于筛选和重新分配）
const robotList = ref([])

// 获取机器人列表
const fetchRobots = async () => {
  try {
    const res = await listRobot({ pageNum: 1, pageSize: 100 })
    robotList.value = res.rows || []
  } catch (error) {
    console.error('获取机器人列表失败', error)
  }
}

// 统计卡片数据
const highRiskCount = computed(() => abnormalList.value.filter(t => t.riskLevel === 1).length)
const riskCount = computed(() => abnormalList.value.filter(t => t.riskLevel === 2).length)
// 异常机器人（状态为离线/故障/低电量）
const abnormalRobots = computed(() => robotList.value.filter(r =>
  r.status === 'offline' || r.status === 'fault' || r.status === 'low_battery'
))
const abnormalRobotCount = computed(() => abnormalRobots.value.length)
// 今日已处理
const resolvedToday = ref(0)

// 判断机器人是否正常（在线且电量>20）
const isRobotNormal = (task) => {
  if (!task.robotId) return false
  const robot = robotList.value.find(r => r.id === task.robotId)
  return robot && robot.status === 'online' && robot.battery > 20
}

// 获取异常任务列表
const getList = async () => {
  loading.value = true
  try {
    const params = { ...queryParams }
    const res = await listAbnormalTask(params)
    abnormalList.value = res.rows || []
    total.value = res.total || 0
  } catch (error) {
    ElMessage.error('获取异常任务列表失败')
  } finally {
    loading.value = false
  }
}

const handleQuery = () => {
  queryParams.pageNum = 1
  getList()
}

const resetQuery = () => {
  queryParams.riskLevel = undefined
  queryParams.robotId = undefined
  handleQuery()
}

const refreshList = () => {
  getList()
}

// ========== 操作 ==========

// 查看详情
const detailVisible = ref(false)
const currentTask = ref(null)
const handleView = async (row) => {
  try {
    const res = await getTask(row.id)
    currentTask.value = res.data
    detailVisible.value = true
  } catch (error) {
    ElMessage.error('获取详情失败')
  }
}

// 恢复任务（继续执行）
const handleResume = async (row) => {
  try {
    await continueTask(row.id)
    ElMessage.success('任务已恢复执行')
    getList()
  } catch (error) {
    ElMessage.error('恢复失败')
  }
}

// 手动解决风险（清除风险标记）
const handleResolveRisk = async (row) => {
  if (!isRobotNormal(row)) {
    ElMessage.warning('机器人状态异常，无法解决风险')
    return
  }
  try {
    await ElMessageBox.confirm('确认解决该任务的风险吗？风险移除后，任务将从异常列表中消失。', '确认解决', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'info'
    })
    await resolveRisk(row.id)
    resolvedToday.value += 1
    ElMessage.success('风险已解决')
    getList()
  } catch (error) {
    if (error !== 'cancel') ElMessage.error('操作失败')
  }
}

// 中止高风险任务（暂停任务，保持风险等级）
const handleAbortHighRisk = async (row) => {
  try {
    const { value: reason } = await ElMessageBox.prompt('请输入中止原因', '中止高风险任务', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      inputPlaceholder: '中止原因'
    })
    await pauseTask(row.id)
    ElMessage.success('任务已暂停，请重新分配或终止')
    getList()
  } catch (error) {
    if (error !== 'cancel') ElMessage.error('操作失败')
  }
}

// 终止任务（彻底结束，并清除风险）
const handleTerminate = async (row) => {
  try {
    const { value: reason } = await ElMessageBox.prompt('请输入终止原因', '终止任务', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      inputPlaceholder: '终止原因'
    })
    await terminateTask(row.id, reason)
    resolvedToday.value += 1
    ElMessage.success('任务已终止')
    getList()
  } catch (error) {
    if (error !== 'cancel') ElMessage.error('操作失败')
  }
}

// ========== 重新分配 ==========
const reassignDialog = reactive({
  visible: false,
  task: null,
  selectedRobotId: null
})

// 可用机器人（在线且属于任务适用组）
const availableRobotsForReassign = computed(() => {
  if (!reassignDialog.task) return []
  const task = reassignDialog.task
  const groupIds = task.robotGroupIds || []
  return robotList.value.filter(r =>
    groupIds.includes(r.groupId) &&
    r.status === 'online' &&
    r.battery > 20 &&
    r.id !== task.robotId
  )
})

const showReassignDialog = (row) => {
  reassignDialog.task = row
  reassignDialog.selectedRobotId = null
  reassignDialog.visible = true
}

const confirmReassign = async () => {
  if (!reassignDialog.selectedRobotId) return
  const task = reassignDialog.task
  const robotId = reassignDialog.selectedRobotId
  try {
    await updateTask(task.id, { robotId })
    // 重新分配后需要清除风险
    await resolveRisk(task.id)
    resolvedToday.value += 1
    ElMessage.success('重新分配成功，风险已解除')
    reassignDialog.visible = false
    getList()
  } catch (error) {
    ElMessage.error('分配失败')
  }
}

// ========== 辅助函数 ==========
const statusMap = {
  0: 'pending', 1: 'ready', 2: 'executing', 3: 'paused', 4: 'completed', 5: 'disabled', 6: 'aborted'
}
const statusTextMap = {
  0: '未开始', 1: '准备中', 2: '执行中', 3: '已暂停', 4: '已完成', 5: '已禁用', 6: '已终止'
}
const getStatusText = (status) => statusTextMap[status] || status
const getStatusClass = (status) => statusMap[status] || 'pending'

const getRobotGroupNames = (groupIds) => {
  return groupIds ? groupIds.join(', ') : '-'
}

// 表格行样式（高风险行高亮）
const tableRowClassName = ({ row }) => {
  if (row.riskLevel === 1) return 'abnormal-task-row'
  return ''
}

onMounted(() => {
  getList()
  fetchRobots()
})
</script>

<style scoped>
.app-container {
  padding: 20px;
}
.stats-cards {
  margin-bottom: 20px;
}
.stat-card {
  display: flex;
  align-items: center;
  padding: 16px;
  border-radius: 8px;
  background: white;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
}
.stat-icon {
  width: 48px;
  height: 48px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  margin-right: 16px;
}
.stat-icon.red { background-color: #fff1f0; color: #ff4d4f; }
.stat-icon.orange { background-color: #fff7e6; color: #fa8c16; }
.stat-icon.blue { background-color: #e6f7ff; color: #1890ff; }
.stat-icon.green { background-color: #f6ffed; color: #52c41a; }
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
.filter-bar {
  margin-bottom: 20px;
  display: flex;
  align-items: center;
  flex-wrap: wrap;
}
.status-tag {
  display: inline-block;
  padding: 2px 8px;
  border-radius: 10px;
  font-size: 12px;
  font-weight: 500;
}
.status-pending { background-color: #fff7e6; color: #fa8c16; }
.status-ready { background-color: #e6f7ff; color: #1890ff; }
.status-executing { background-color: #f6ffed; color: #52c41a; }
.status-paused { background-color: #fffbe6; color: #faad14; }
.status-completed { background-color: #f6ffed; color: #52c41a; }
.status-disabled { background-color: #f5f5f5; color: #999; }
.status-aborted { background-color: #fff1f0; color: #ff4d4f; }
.robot-option {
  padding: 10px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  margin-bottom: 8px;
  width: 100%;
}
.robot-option:hover {
  background-color: #f5f5f5;
}
.empty-tip {
  text-align: center;
  color: #999;
  padding: 20px;
}
</style>

<style>
.abnormal-task-row {
  background-color: #fff1f0 !important;
}
</style>
