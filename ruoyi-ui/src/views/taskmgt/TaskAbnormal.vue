<template>
  <div class="app-container">
    <!-- 统计卡片 -->
    <el-row :gutter="16" class="stats-cards">
      <el-col :span="6">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-icon red"><i class="el-icon-warning"></i></div>
          <div class="stat-info">
            <div class="stat-value">{{ highRiskCount }}</div>
            <div class="stat-label">高风险</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-icon orange"><i class="el-icon-warning-outline"></i></div>
          <div class="stat-info">
            <div class="stat-value">{{ riskCount }}</div>
            <div class="stat-label">风险</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-icon blue"><i class="el-icon-odometer"></i></div>
          <div class="stat-info">
            <div class="stat-value">{{ abnormalRobotCount }}</div>
            <div class="stat-label">异常机器人</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-icon green"><i class="el-icon-circle-check"></i></div>
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
          <span><i class="el-icon-warning" style="color:#ff4d4f;"></i> 异常任务列表</span>
          <el-button type="text" @click="refreshList">
            <i class="el-icon-refresh"></i> 刷新
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
          @clear="handleQuery"
        >
          <el-option label="高风险" :value="2" />
          <el-option label="风险" :value="1" />
        </el-select>
        <el-select
          v-model="queryParams.robotId"
          placeholder="异常机器人"
          clearable
          style="width:180px; margin-left:10px;"
          @change="handleQuery"
          @clear="handleQuery"
        >
          <el-option
            v-for="r in abnormalRobots"
            :key="r.id"
            :label="r.name"
            :value="r.id"
          />
        </el-select>
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
          <template slot-scope="scope">
            <el-tag v-if="scope.row.riskLevel === 2" type="danger" effect="dark">高风险</el-tag>
            <el-tag v-else-if="scope.row.riskLevel === 1" type="warning">风险</el-tag>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column label="任务状态" width="100" align="center">
          <template slot-scope="scope">
            <span :class="'status-tag status-' + getStatusClass(scope.row.status)">
              {{ getStatusText(scope.row.status) }}
            </span>
          </template>
        </el-table-column>
        <!-- 异常原因显示 -->
        <el-table-column label="异常原因" min-width="180" show-overflow-tooltip>
          <template slot-scope="scope">
            <div v-if="scope.row.robotStatusSummary">
              <i class="el-icon-info" style="color:#ff4d4f; margin-right:4px;"></i>
              {{ scope.row.robotStatusSummary }}
            </div>
            <div v-else-if="scope.row.robotStatuses && scope.row.robotStatuses.length > 0">
              <div v-for="(rs, idx) in scope.row.robotStatuses" :key="idx" style="margin-bottom:2px;">
                <i class="el-icon-odometer" style="color:#909399; margin-right:4px;"></i>
                {{ rs.robotName }}: {{ getRobotStatusText(rs.status) }}
              </div>
            </div>
            <div v-else>-</div>
          </template>
        </el-table-column>
        <el-table-column label="是否是组任务" width="100" align="center">
          <template slot-scope="scope">
            {{ scope.row.isGroupTask === 1 ? '是' : '否' }}
          </template>
        </el-table-column>
        <el-table-column label="原机器人组" prop="robotGroupName" min-width="120" show-overflow-tooltip />
        <el-table-column prop="robotName" label="原机器人" width="120" show-overflow-tooltip />
        <el-table-column label="进度" width="120" align="center">
          <template slot-scope="scope">
            {{ scope.row.completedSteps || 0 }}/{{ scope.row.totalSteps || 0 }}
          </template>
        </el-table-column>
        <!-- 操作列 -->
        <el-table-column label="操作" width="380" fixed="right" align="center">
          <template slot-scope="scope">
            <!-- 详情 -->
            <el-button size="small" circle title="详情" @click="handleView(scope.row)">
              <i class="el-icon-view"></i>
            </el-button>

            <!-- 高风险任务操作 -->
            <template v-if="scope.row.riskLevel === 2">
              <!-- 执行中任务可中止 -->
              <el-button
                v-if="scope.row.status === 0"
                size="small"
                type="warning"
                circle
                title="中止"
                @click="handleAbortHighRisk(scope.row)"
              >
                <i class="el-icon-remove-outline"></i>
              </el-button>

              <!-- 已暂停任务的操作组 -->
              <template v-else-if="scope.row.status === 2">
                <!-- 恢复按钮：根据机器人/组状态判断是否可以恢复 -->
                <el-button
                  size="small"
                  :type="isRobotOrGroupNormal(scope.row) ? 'success' : 'info'"
                  :disabled="!isRobotOrGroupNormal(scope.row)"
                  circle
                  :title="isRobotOrGroupNormal(scope.row) ? '恢复' : '机器人/组状态异常，无法恢复'"
                  @click="handleResume(scope.row)"
                >
                  <i class="el-icon-video-play"></i>
                </el-button>

                <el-button
                  size="small"
                  type="primary"
                  circle
                  title="重新分配"
                  @click="showReassignDialog(scope.row)"
                >
                  <i class="el-icon-refresh"></i>
                </el-button>

                <el-button
                  size="small"
                  type="danger"
                  circle
                  title="终止"
                  @click="handleTerminate(scope.row)"
                >
                  <i class="el-icon-circle-close"></i>
                </el-button>
              </template>

              <!-- 已终止任务显示解决风险按钮 -->
              <el-button
                v-if="scope.row.status === 5 ||scope.row.status === 3"
                size="small"
                type="success"
                circle
                title="解决风险"
                @click="handleResolveRisk(scope.row)"
              >
                <i class="el-icon-check"></i>
              </el-button>

              <!-- 操作后显示解决风险标记（等待手动解除） -->
              <el-button
                v-if="isTaskPendingResolve(scope.row)"
                size="small"
                type="success"
                circle
                title="点击解除风险标记"
                @click="handleResolveRisk(scope.row)"
                style="margin-left: 8px;"
              >
                <i class="el-icon-check"></i>
              </el-button>
            </template>

            <!-- 普通风险任务操作 -->
            <template v-else-if="scope.row.riskLevel === 1">
              <!-- 准备中任务可取消 -->
              <el-button
                v-if="scope.row.status === 1"
                size="small"
                type="warning"
                circle
                title="取消"
                @click="handleCancel(scope.row)"
              >
                <i class="el-icon-close"></i>
              </el-button>

              <!-- 已取消任务显示解决风险按钮 -->
              <el-button
                v-if="scope.row.status === 5||scope.row.status === 3"
                size="small"
                type="success"
                circle
                title="解决风险"
                @click="handleResolveRisk(scope.row)"
              >
                <i class="el-icon-check"></i>
              </el-button>

              <!-- 重新分配 -->
              <el-button
                v-if="scope.row.status !== 5 && scope.row.status !== 6"
                size="small"
                type="primary"
                circle
                title="重新分配"
                @click="showReassignDialog(scope.row)"
              >
                <i class="el-icon-refresh"></i>
              </el-button>

              <!-- 操作后显示解决风险标记（等待手动解除） -->
              <el-button
                v-if="isTaskPendingResolve(scope.row)"
                size="small"
                type="success"
                circle
                title="点击解除风险标记"
                @click="handleResolveRisk(scope.row)"
                style="margin-left: 8px;"
              >
                <i class="el-icon-check"></i>
              </el-button>
            </template>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <pagination
        v-show="total > 0"
        :total="total"
        :page.sync="queryParams.pageNum"
        :limit.sync="queryParams.pageSize"
        @pagination="getList"
      />
    </el-card>

    <!-- 重新分配对话框 - 支持组任务和单任务 -->
    <el-dialog
      title="重新分配任务"
      :visible.sync="reassignDialog.visible"
      width="600px"
      append-to-body
    >
      <div v-if="reassignDialog.task">
        <el-alert
          v-if="reassignDialog.task.riskLevel === 2"
          title="高风险任务"
          type="error"
          description="此任务已被中止，需要重新分配"
          show-icon
          style="margin-bottom:16px;"
        />
        <div style="margin-bottom:16px; padding:12px; background:#f5f5f5; border-radius:4px;">
          <div><strong>任务名称：</strong>{{ reassignDialog.task.name }}</div>
          <div><strong>任务类型：</strong>{{ reassignDialog.task.isGroupTask === 1 ? '组任务' : '单任务' }}</div>
          <div v-if="reassignDialog.task.isGroupTask === 0">
            <strong>原机器人：</strong>{{ reassignDialog.task.robotName }}
          </div>
          <div v-else>
            <strong>原机器人组：</strong>{{ reassignDialog.task.robotGroupName }}
          </div>
          <div><strong>适用机器人组：</strong>{{ getRobotGroupNames(reassignDialog.task.templateId) }}</div>
        </div>

        <!-- 单任务：显示可用机器人列表（原逻辑） -->
        <div v-if="reassignDialog.task.isGroupTask === 0">
          <div style="font-weight:bold; margin-bottom:8px;">可用机器人</div>
          <el-radio-group v-model="reassignDialog.selectedRobotId">
            <div
              v-for="robot in availableRobotsForReassign"
              :key="robot.id"
              class="robot-option"
            >
              <el-radio :label="robot.id" style="width:100%;">
                <div style="display:flex; align-items:center;">
                  <i class="el-icon-odometer" style="font-size:24px; color:#1890ff; margin-right:12px;"></i>
                  <div>
                    <div style="font-weight:bold;">{{ robot.name }}</div>
                    <div style="font-size:12px; color:#666;">
                      <span><i class="el-icon-battery"></i> {{ robot.battery }}%</span>
                      <span style="margin-left:12px;"><i class="el-icon-location"></i> {{ robot.location }}</span>
                    </div>
                  </div>
                </div>
              </el-radio>
            </div>
          </el-radio-group>
          <div v-if="availableRobotsForReassign.length === 0" class="empty-tip">
            <i class="el-icon-odometer" style="font-size:24px; margin-bottom:8px;"></i>
            <div>当前没有可用的机器人</div>
          </div>
        </div>

        <!-- 组任务：显示步骤列表，每个步骤可单独选择机器人 -->
        <div v-else>
          <div style="font-weight:bold; margin-bottom:8px;">步骤机器人分配</div>
          <el-table :data="reassignDialog.steps" border size="small">
            <el-table-column label="序号" width="60" align="center">
              <template slot-scope="scope">{{ scope.row.orderNum }}</template>
            </el-table-column>
            <el-table-column prop="stepName" label="步骤名称" min-width="150" />
            <el-table-column label="当前机器人" width="120">
              <template slot-scope="scope">
                {{ getRobotNameById(scope.row.assignedRobotId) || '未分配' }}
              </template>
            </el-table-column>
            <el-table-column label="重新分配" min-width="200">
              <template slot-scope="scope">
                <el-select
                  v-model="scope.row.newRobotId"
                  placeholder="选择机器人"
                  clearable
                  size="small"
                  style="width:100%"
                >
                  <el-option
                    v-for="robot in availableRobotsForGroupStep"
                    :key="robot.id"
                    :label="robot.name"
                    :value="robot.id"
                  >
                    <span>{{ robot.name }}</span>
                    <span style="float: right; color: #8492a6; font-size: 12px">
                      电量{{ robot.battery }}%
                    </span>
                  </el-option>
                </el-select>
              </template>
            </el-table-column>
          </el-table>
          <div v-if="availableRobotsForGroupStep.length === 0" class="empty-tip">
            <i class="el-icon-odometer" style="font-size:24px; margin-bottom:8px;"></i>
            <div>没有可用的机器人（模板允许的组内所有机器人不可用）</div>
          </div>
        </div>
      </div>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="reassignDialog.visible = false">取 消</el-button>
          <el-button
            type="primary"
            @click="confirmReassign"
            :disabled="!canConfirmReassign"
          >确认分配</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 任务详情对话框 -->
    <el-dialog title="任务详情" :visible.sync="detailVisible" width="800px" append-to-body>
      <div v-if="currentTask">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="任务名称" :span="2">{{ currentTask.name }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <span :class="'status-tag status-' + getStatusClass(currentTask.status)">
              {{ getStatusText(currentTask.status) }}
            </span>
          </el-descriptions-item>
          <el-descriptions-item label="风险等级">
            <el-tag v-if="currentTask.riskLevel === 2" type="danger">高风险</el-tag>
            <el-tag v-else-if="currentTask.riskLevel === 1" type="warning">风险</el-tag>
            <span v-else>无</span>
          </el-descriptions-item>
          <el-descriptions-item label="是否是组任务">{{ currentTask.isGroupTask === 1 ? '是' : '否' }}</el-descriptions-item>
          <el-descriptions-item label="机器人">{{ currentTask.robotName || '-' }}</el-descriptions-item>
          <el-descriptions-item label="机器人组">{{ currentTask.robotGroupName || '-' }}</el-descriptions-item>
          <el-descriptions-item label="创建时间">{{ currentTask.createTime || '-' }}</el-descriptions-item>
        </el-descriptions>

        <!-- 异常详情展示 -->
        <template v-if="currentTask.riskLevel > 0">
          <el-divider content-position="left">异常详情</el-divider>
          <el-descriptions :column="1" border>
            <el-descriptions-item label="异常原因">
              {{ currentTask.robotStatusSummary || '无' }}
            </el-descriptions-item>
          </el-descriptions>

          <!-- 机器人状态列表 -->
          <div v-if="currentTask.robotStatuses && currentTask.robotStatuses.length > 0" style="margin-top:16px;">
            <div style="font-weight:bold; margin-bottom:8px;">机器人状态详情</div>
            <el-table :data="currentTask.robotStatuses" border size="small">
              <el-table-column prop="robotName" label="机器人名称" width="180" />
              <el-table-column prop="status" label="状态" width="120">
                <template slot-scope="scope">
                  <el-tag :type="getRobotStatusType(scope.row.status)" size="small">
                    {{ getRobotStatusText(scope.row.status) }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column label="预警信息" min-width="200">
                <template slot-scope="scope">
                  <span v-if="scope.row.status !== 'normal' && scope.row.status !== '0'" style="color: #f56c6c;">
                    <i class="el-icon-warning"></i> 存在异常
                  </span>
                  <span v-else style="color: #67c23a;">
                    <i class="el-icon-success"></i> 正常
                  </span>
                </template>
              </el-table-column>
            </el-table>
          </div>
        </template>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import {
  cancelTask,
  continueTask,
  getAbnormalTask, getTemplate,
  listAbnormalTask,
  pauseTask,
  resolveTaskRisk,
  terminateTask,
  updateTask,
  getTaskSteps,
  updateTaskSteps
} from '@/api/taskmgt/taskmgt'
import { listRobots, listGroups } from '@/api/system/robots'

export default {
  name: 'TaskAbnormal',
  data() {
    return {
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        riskLevel: undefined,
        robotId: undefined,
        robotGroupId: undefined
      },
      loading: false,
      abnormalList: [],
      total: 0,
      robotList: [],
      robotGroupList: [], // 机器人组列表
      resolvedToday: 0,
      detailVisible: false,
      currentTask: null,
      reassignDialog: {
        visible: false,
        task: null,
        selectedRobotId: null,  // 单任务选中的机器人
        steps: [],
        templateInfo: null
      },
      // 记录已执行操作等待解除风险的任务ID集合
      pendingResolveTasks: new Set()
    }
  },
  computed: {
    highRiskCount() {
      return this.abnormalList.filter(t => t.riskLevel === 2).length
    },
    riskCount() {
      return this.abnormalList.filter(t => t.riskLevel === 1).length
    },
    abnormalRobots() {
      return this.robotList.filter(r =>
        r.status === 0 ||
        r.status === 2 ||
        r.hardwareStatus === 2 ||
        (r.battery && r.battery <= 20)
      )
    },
    abnormalRobotCount() {
      return this.abnormalRobots.length
    },
    // 单任务：可用机器人
    availableRobotsForReassign() {
      if (!this.reassignDialog.task || this.reassignDialog.task.isGroupTask === 1) return []
      const task = this.reassignDialog.task
      const groupIds = this.ensureArray(task.robotGroupIds)
      return this.robotList.filter(r =>
        groupIds.includes(r.groupId) &&
        r.status === 1 &&
        r.hardwareStatus === 0 &&
        r.battery > 20 &&
        r.id !== task.robotId
      )
    },
    availableRobotsForGroupStep() {
      const task = this.reassignDialog.task
      if (!task || task.isGroupTask !== 1) return []
      const groupIds = this.ensureArray(task.robotGroupIds)
      return this.robotList.filter(r =>
        groupIds.includes(r.groupId) &&
        r.status === 1 &&
        r.hardwareStatus === 0 &&
        r.battery > 20
      )
    },
    // 组任务：可用组（组内所有机器人正常）
    availableGroupsForReassign() {
      if (!this.reassignDialog.task || this.reassignDialog.task.isGroupTask === 0) return []
      const task = this.reassignDialog.task
      const groupIds = this.ensureArray(task.robotGroupIds)

      return this.robotGroupList.filter(group => {
        // 只显示模板允许的组，且不是当前组
        if (!groupIds.includes(group.id) || group.id === task.robotGroupId) return false

        // 获取该组所有机器人
        const groupRobots = this.robotList.filter(r => r.groupId === group.id)
        if (groupRobots.length === 0) return false

        // 检查是否所有机器人都正常
        const allNormal = groupRobots.every(r =>
          r.status === 1 &&
          r.hardwareStatus === 0 &&
          r.battery > 20
        )

        return allNormal
      }).map(group => ({
        ...group,
        robotCount: this.robotList.filter(r => r.groupId === group.id).length
      }))
    },
    // 是否可以确认重新分配
    canConfirmReassign() {
      const task = this.reassignDialog.task
      if (!task) return false
      if (task.isGroupTask === 0) {
        return !!this.reassignDialog.selectedRobotId
      } else {
        // 组任务：至少有一个步骤选择了新机器人
        return this.reassignDialog.steps.some(step => step.newRobotId)
      }
    }
  },
  created() {
    this.getRobotList()
    this.getRobotGroupList()
    this.getList()
  },
  methods: {
    ensureArray(ids) {
      if (!ids) return []
      if (Array.isArray(ids)) return ids
      if (typeof ids === 'string') return ids.split(',').map(s => Number(s.trim()))
      return [ids]
    },
    isRobotOnlineAndHealthy(robot) {
      return robot && robot.status === 1 && robot.hardwareStatus === 0
    },
    async getRobotList() {
      try {
        const res = await listRobots({ pageSize: 1000 })
        this.robotList = res.rows || []
      } catch (error) {
        this.$message.error('获取机器人列表失败')
      }
    },
    async getRobotGroupList() {
      try {
        const res = await listGroups({ pageSize: 1000 })
        this.robotGroupList = res.rows || []
      } catch (error) {
        this.$message.error('获取机器人组列表失败')
      }
    },
    async getList() {
      this.loading = true
      try {
        const params = { ...this.queryParams }
        const res = await listAbnormalTask(params)
        this.abnormalList = res.rows || []
        this.total = res.total || 0
      } catch (error) {
        this.$message.error('获取异常任务列表失败')
      } finally {
        this.loading = false
      }
    },
    handleQuery() {
      this.queryParams.pageNum = 1
      this.getList()
    },
    refreshList() {
      this.getList()
    },
    isRobotNormal(task) {
      if (!task.robotId) return false
      const robot = this.robotList.find(r => r.id === task.robotId)
      return this.isRobotOnlineAndHealthy(robot) && robot.battery > 20
    },
    // 检查机器人或组是否正常（用于恢复按钮状态）
    isRobotOrGroupNormal(task) {
      if (!task) return false

      if (task.isGroupTask === 1) {
        // 组任务：检查组内所有机器人是否正常
        if (!task.robotGroupId) return false
        const groupRobots = this.robotList.filter(r => r.groupId === task.robotGroupId)
        if (groupRobots.length === 0) return false

        return groupRobots.every(r =>
          r.status === 1 &&
          r.hardwareStatus === 0 &&
          r.battery > 20
        )
      } else {
        // 单任务：检查机器人是否正常
        if (!task.robotId) return false
        const robot = this.robotList.find(r => r.id === task.robotId)
        if (!robot) return false

        return robot.status === 1 &&
          robot.hardwareStatus === 0 &&
          robot.battery > 20
      }
    },
    // 检查任务是否处于等待解除风险状态
    isTaskPendingResolve(row) {
      return this.pendingResolveTasks.has(row.id)
    },
    // 获取模板允许的机器人组名称（用于显示）
    getRobotGroupNames(templateId) {
      if (!this.reassignDialog.templateInfo || this.reassignDialog.task?.templateId !== templateId) {
        return '-'
      }
      const ids = this.ensureArray(this.reassignDialog.templateInfo.robotGroupIds)
      if (!ids.length) return '-'
      const names = ids.map(id => {
        const group = this.robotGroupList.find(g => g.id === id)
        return group ? group.name : `组${id}`
      })
      return names.join(', ')
    },

    // 根据机器人ID获取名称
    getRobotNameById(robotId) {
      const robot = this.robotList.find(r => r.id === robotId)
      return robot ? robot.name : ''
    },
    getRobotStatusText(status) {
      const map = {
        0: '低电量',
        1: '硬件故障',
        2: '硬件异常',
        3: '离线',
        'normal': '正常'
      }
      return map[status] || status
    },
    getRobotStatusType(status) {
      if (status === 0) return 'warning'
      if (status === 1 || status === 2) return 'danger'
      if (status === 3) return 'info'
      if (status === 'normal') return 'success'
      return 'info'
    },
    async handleView(row) {
      try {
        const res = await getAbnormalTask(row.id)
        this.currentTask = res.data
        this.detailVisible = true
      } catch (error) {
        this.$message.error('获取详情失败')
      }
    },
    async handleResume(row) {
      try {
        // 检查机器人/组状态是否正常
        if (!this.isRobotOrGroupNormal(row)) {
          this.$message.warning('机器人或组状态异常，无法恢复')
          return
        }

        await continueTask(row.id)
        this.$message.success('任务已恢复执行')
        // 添加到等待解除风险集合
        this.pendingResolveTasks.add(row.id)
        this.getList()
      } catch (error) {
        this.$message.error('恢复失败')
      }
    },
    async handleCancel(row){
      try{
        await cancelTask(row.id)
        this.$message.success('任务已取消')
        // 添加到等待解除风险集合
        this.pendingResolveTasks.add(row.id)
        this.getList()
      }catch (error){
        this.$message.error('取消失败')
      }
    },
    // 解决风险 - 需要手动点击，重新分配不会自动解决
    async handleResolveRisk(row) {
      try {
        await this.$confirm('确认解决该任务的风险吗？风险移除后，任务将从异常列表中消失。', '确认解决', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'info'
        })
        await resolveTaskRisk(row.id)
        this.resolvedToday += 1
        // 从等待集合中移除
        this.pendingResolveTasks.delete(row.id)
        this.$message.success('风险已解决')
        this.getList()
      } catch (error) {
        if (error !== 'cancel') this.$message.error('操作失败')
      }
    },
    async handleAbortHighRisk(row) {
      try {
        await pauseTask(row.id)
        this.$message.success('任务已暂停，请重新分配、恢复或终止')
        this.getList()
      } catch (error) {
        this.$message.error('操作失败')
      }
    },
    async handleTerminate(row) {
      try {
        const { value: reason } = await this.$prompt('请输入终止原因', '终止任务', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          inputPlaceholder: '终止原因'
        })
        await terminateTask(row.id, reason)
        this.resolvedToday += 1
        this.$message.success('任务已终止')
        // 添加到等待解除风险集合（已终止任务也允许解除风险）
        this.pendingResolveTasks.add(row.id)
        this.getList()
      } catch (error) {
        if (error !== 'cancel') this.$message.error('操作失败')
      }
    },
    async showReassignDialog(row) {
      this.reassignDialog.task = row
      this.reassignDialog.selectedRobotId = null
      this.reassignDialog.steps = []
      this.reassignDialog.visible = true

      // 获取模板信息（用于获取允许的机器人组）
      if (row.templateId) {
        try {
          const res = await getTemplate(row.templateId)
          this.reassignDialog.templateInfo = res.data || res
        } catch (error) {
          console.error('获取模板信息失败', error)
          this.reassignDialog.templateInfo = null
        }
      }

      if (row.isGroupTask === 1) {
        // 组任务：获取步骤列表
        try {
          const stepsRes = await getTaskSteps(row.id)
          const steps = stepsRes.data || []
          this.reassignDialog.steps = steps.map(step => ({
            ...step,
            newRobotId: null   // 存储新分配的机器人ID
          }))
        } catch (error) {
          this.$message.error('获取步骤列表失败')
          this.reassignDialog.visible = false
        }
      }
    },
    // 确认重新分配
    async confirmReassign() {
      const task = this.reassignDialog.task
      try {
        if (task.isGroupTask === 0) {
          // 单任务：更新任务机器人
          await updateTask(task.id, { robotId: Number(this.reassignDialog.selectedRobotId) })
          this.$message.success('重新分配成功')
        } else {
          // 组任务：更新每个步骤的机器人（使用 orderNum）
          const updates = this.reassignDialog.steps
            .filter(step => step.newRobotId)
            .map(step => ({
              orderNum: step.orderNum,
              assignedRobotId: step.newRobotId
            }))
          if (updates.length === 0) {
            this.$message.warning('请至少为一个步骤选择机器人')
            return
          }
          await updateTaskSteps(task.id, updates)
          this.$message.success('步骤机器人分配成功')
        }
        // 添加到等待解除风险集合
        this.pendingResolveTasks.add(task.id)
        this.reassignDialog.visible = false
        this.getList()
      } catch (error) {
        this.$message.error('分配失败：' + (error.message || '未知错误'))
      }
    },
    resetReassignDialog() {
      this.reassignDialog.task = null
      this.reassignDialog.selectedRobotId = null
      this.reassignDialog.steps = []
      this.reassignDialog.templateInfo = null
    },
    getStatusText(status) {
      const map = { 3: '未开始', 1: '准备中', 0: '执行中', 2: '已暂停', 6: '已完成', 4: '已禁用', 5: '已终止' }
      return map[status] || status
    },
    getStatusClass(status) {
      const map = { 3: 'pending', 1: 'ready', 0: 'executing', 2: 'paused', 6: 'completed', 4: 'disabled', 5: 'aborted' }
      return map[status] || 'pending'
    },
    tableRowClassName({ row }) {
      if (row.riskLevel === 2) return 'abnormal-task-row high-risk'
      if (row.riskLevel === 1) return 'abnormal-task-row'
      return ''
    }
  }
}
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
/* Element UI 图标大小调整 */
.el-button [class*="el-icon-"] {
  font-size: 14px;
  font-weight: bold;
}
</style>

<style>
.abnormal-task-row {
  background-color: #fff8e6 !important;
}
.abnormal-task-row.high-risk {
  background-color: #fff1f0 !important;
}
/* 表格操作列按钮间距 */
.el-table .cell .el-button + .el-button {
  margin-left: 4px;
}
</style>
