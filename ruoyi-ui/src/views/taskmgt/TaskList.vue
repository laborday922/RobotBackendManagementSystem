<template>
  <div class="app-container">
    <!-- 搜索栏 -->
    <el-card class="search-card">
      <el-form :model="queryParams" ref="queryRef" :inline="true" label-width="90px">
        <el-form-item label="任务名称" prop="name">
          <el-input
            v-model="queryParams.name"
            placeholder="请输入任务名称"
            clearable
            style="width: 180px"
            @keyup.enter="handleQuery"
          />
        </el-form-item>
        <el-form-item label="任务状态" prop="status">
          <el-select
            v-model="queryParams.status"
            placeholder="全部"
            clearable
            style="width: 120px"
          >
            <el-option label="未开始" :value="0" />
            <el-option label="准备中" :value="1" />
            <el-option label="执行中" :value="2" />
            <el-option label="已暂停" :value="3" />
            <el-option label="已完成" :value="4" />
            <el-option label="已禁用" :value="5" />
            <el-option label="已终止" :value="6" />
          </el-select>
        </el-form-item>
        <el-form-item label="任务类型" prop="taskType">
          <el-select
            v-model="queryParams.taskType"
            placeholder="全部"
            clearable
            style="width: 120px"
          >
            <el-option label="定时任务" :value="1" />
            <el-option label="电量任务" :value="2" />
            <el-option label="闲时任务" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="机器人" prop="robotId">
          <el-select
            v-model="queryParams.robotId"
            placeholder="请选择机器人"
            clearable
            style="width: 150px"
          >
            <el-option
              v-for="item in robotOptions"
              :key="item.id"
              :label="item.name"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="机器人组" prop="robotGroupId">
          <el-select
            v-model="queryParams.robotGroupId"
            placeholder="请选择组"
            clearable
            style="width: 150px"
          >
            <el-option
              v-for="item in robotGroupOptions"
              :key="item.id"
              :label="item.name"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
          <el-button icon="Refresh" @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 操作按钮 -->
    <el-card class="table-card">
      <template #header>
        <div class="card-header">
          <span>任务列表</span>
          <div>
            <el-button type="primary" icon="Plus" @click="handleAdd('process')">创建流程任务</el-button>
            <el-button type="primary" plain icon="Plus" @click="handleAdd('simple')">创建简单任务</el-button>
          </div>
        </div>
      </template>

      <!-- 表格 -->
      <el-table v-loading="loading" :data="taskList" border style="width: 100%">
        <el-table-column label="ID" prop="id" width="80" align="center" />
        <el-table-column label="任务名称" prop="name" min-width="150" show-overflow-tooltip />
        <el-table-column label="模板" prop="templateName" width="140" show-overflow-tooltip />
        <el-table-column label="类型" width="100" align="center">
          <template #default="scope">
            <el-tag :type="scope.row.templateId ? 'primary' : 'info'">
              {{ scope.row.templateId ? '流程' : '简单' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="触发方式" width="100" align="center">
          <template #default="scope">
            <el-tag :type="getTriggerTypeTag(scope.row.taskType)">
              {{ getTriggerTypeText(scope.row.taskType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100" align="center">
          <template #default="scope">
            <span :class="'status-tag status-' + getStatusClass(scope.row.status)">{{ getStatusText(scope.row.status) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="机器人" prop="robotName" width="120" show-overflow-tooltip />
        <el-table-column label="优先级" width="80" align="center">
          <template #default="scope">
            <el-tag v-if="scope.row.priority === 1" type="danger">高</el-tag>
            <el-tag v-else-if="scope.row.priority === 2" type="warning">中</el-tag>
            <el-tag v-else type="info">低</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="创建时间" prop="createTime" width="160" align="center" />
        <el-table-column label="操作" width="300" fixed="right" align="center">
          <template #default="scope">
            <el-button link type="primary" icon="View" @click="handleView(scope.row)">详情</el-button>
            <el-button link type="primary" icon="Edit" @click="handleEdit(scope.row)" v-if="canEdit(scope.row)">编辑</el-button>
            <el-button link type="danger" icon="Delete" @click="handleDelete(scope.row)" v-if="canDelete(scope.row)">删除</el-button>
            <!-- 状态操作按钮 -->
            <el-dropdown @command="(cmd) => handleStatusCommand(cmd, scope.row)" v-if="scope.row.status !== 4 && scope.row.status !== 6">
              <el-button link type="primary" icon="MoreFilled">更多</el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item v-if="scope.row.status === 0 || scope.row.status === 1" command="pause">暂停</el-dropdown-item>
                  <el-dropdown-item v-if="scope.row.status === 3" command="continue">继续</el-dropdown-item>
                  <el-dropdown-item v-if="scope.row.status === 1 || scope.row.status === 2 || scope.row.status === 3" command="cancel">取消</el-dropdown-item>
                  <el-dropdown-item v-if="scope.row.status === 0 || scope.row.status === 1 || scope.row.status === 2 || scope.row.status === 3" command="terminate">终止</el-dropdown-item>
                  <el-dropdown-item v-if="scope.row.status !== 5" command="ban">禁用</el-dropdown-item>
                  <el-dropdown-item v-if="scope.row.status === 5" command="resume">恢复</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
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

    <!-- 任务创建/编辑对话框 -->
    <el-dialog
      :title="dialog.title"
      v-model="dialog.visible"
      width="750px"
      append-to-body
      @close="cancelTaskDialog"
    >
      <el-form ref="taskFormRef" :model="taskForm" :rules="taskRules" label-width="130px">
        <!-- 基本信息 -->
        <el-form-item label="任务名称" prop="name">
          <el-input v-model="taskForm.name" placeholder="请输入任务名称" />
        </el-form-item>

        <!-- 流程任务：模板选择 -->
        <template v-if="taskForm.type === 'process'">
          <el-form-item label="选择模板" prop="templateId" required>
            <el-select
              v-model="taskForm.templateId"
              placeholder="请选择模板"
              style="width:100%"
              @change="onTemplateChange"
              :disabled="dialog.mode === 'edit'"
            >
              <el-option
                v-for="item in templateOptions"
                :key="item.id"
                :label="item.name"
                :value="item.id"
              />
            </el-select>
          </el-form-item>

          <!-- 动态表单字段（根据模板渲染） -->
          <template v-if="taskForm.templateId && currentTemplate">
            <el-form-item
              v-for="field in currentTemplate.fields"
              :key="field.id"
              :label="field.label"
              :required="field.required"
            >
              <!-- 文本 -->
              <el-input
                v-if="field.type === 'text'"
                v-model="taskForm.formData[field.id]"
                :placeholder="'请输入' + field.label"
              />
              <!-- 数字 -->
              <el-input-number
                v-else-if="field.type === 'number'"
                v-model="taskForm.formData[field.id]"
                style="width:100%"
                :placeholder="'请输入' + field.label"
              />
              <!-- 下拉选择（简单模拟） -->
              <el-select
                v-else-if="field.type === 'select'"
                v-model="taskForm.formData[field.id]"
                placeholder="请选择"
                style="width:100%"
              >
                <el-option label="选项1" value="option1" />
                <el-option label="选项2" value="option2" />
              </el-select>
              <!-- 日期 -->
              <el-date-picker
                v-else-if="field.type === 'date'"
                v-model="taskForm.formData[field.id]"
                type="date"
                placeholder="选择日期"
                style="width:100%"
              />
              <!-- 时间 -->
              <el-time-picker
                v-else-if="field.type === 'time'"
                v-model="taskForm.formData[field.id]"
                placeholder="选择时间"
                style="width:100%"
              />
              <!-- 位置（简单输入） -->
              <el-input
                v-else-if="field.type === 'location'"
                v-model="taskForm.formData[field.id]"
                placeholder="请输入位置"
              />
              <!-- 文件类型暂简化处理，仅展示文本提示 -->
              <el-input
                v-else-if="['image','audio','video','file'].includes(field.type)"
                v-model="taskForm.formData[field.id]"
                placeholder="文件上传暂未实现，请填写文件路径"
              />
              <div v-else>未知字段类型</div>
            </el-form-item>
          </template>
        </template>

        <!-- 简单任务 -->
        <template v-if="taskForm.type === 'simple'">
          <el-form-item label="简单动作" prop="simpleAction" required>
            <el-select v-model="taskForm.simpleAction" placeholder="请选择简单动作" style="width:100%">
              <el-option label="清洁任务" value="clean" />
              <el-option label="巡检任务" value="inspect" />
              <el-option label="配送任务" value="deliver" />
              <el-option label="安防巡逻" value="patrol" />
              <el-option label="送餐服务" value="meal" />
            </el-select>
          </el-form-item>
        </template>

        <!-- 通用设置 -->
        <el-divider content-position="left">任务设置</el-divider>
        <el-form-item label="任务优先级" prop="priority">
          <el-radio-group v-model="taskForm.priority">
            <el-radio :label="1">高</el-radio>
            <el-radio :label="2">中</el-radio>
            <el-radio :label="3">低</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="是否为组任务">
          <el-switch v-model="taskForm.isGroupTask" />
          <span class="el-form-item-msg">组任务可由多个机器人协同完成</span>
        </el-form-item>

        <el-form-item label="任务时长(分钟)" prop="duration">
          <el-input-number v-model="taskForm.duration" :min="1" :max="480" style="width:100%" />
        </el-form-item>

        <!-- 触发类型 -->
        <el-form-item label="任务触发类型" prop="taskType" required>
          <el-radio-group v-model="taskForm.taskType">
            <el-radio :label="1">定时任务</el-radio>
            <el-radio :label="2">电量任务</el-radio>
            <el-radio :label="3">闲时任务</el-radio>
          </el-radio-group>
        </el-form-item>

        <!-- 定时任务设置 -->
        <template v-if="taskForm.taskType === 1">
          <el-form-item label="Cron表达式" prop="cronExpression">
            <el-input v-model="taskForm.cronExpression" placeholder="如：0 0 9 * * ?" />
            <div class="el-form-item-msg">留空表示立即执行</div>
          </el-form-item>
          <el-form-item label="定时开始时间" prop="scheduledTime">
            <el-date-picker
              v-model="taskForm.scheduledTime"
              type="datetime"
              placeholder="选择开始时间"
              style="width:100%"
            />
          </el-form-item>
        </template>

        <!-- 电量任务设置 -->
        <template v-if="taskForm.taskType === 2">
          <el-form-item label="触发电量(%)" prop="batteryThreshold" required>
            <el-slider v-model="taskForm.batteryThreshold" :max="100" show-input />
          </el-form-item>
        </template>

        <!-- 闲时任务设置 -->
        <template v-if="taskForm.taskType === 3">
          <el-form-item label="闲时等待(分钟)" prop="idleTime" required>
            <el-input-number v-model="taskForm.idleTime" :min="1" :max="120" style="width:100%" />
          </el-form-item>
        </template>

        <!-- 机器人/机器人组选择 -->
        <el-form-item :label="taskForm.isGroupTask ? '选择机器人组' : '选择机器人'" prop="targetId" required>
          <template v-if="taskForm.isGroupTask">
            <el-select v-model="taskForm.robotGroupId" placeholder="请选择机器人组" style="width:100%">
              <el-option
                v-for="item in robotGroupOptions"
                :key="item.id"
                :label="item.name"
                :value="item.id"
              />
            </el-select>
          </template>
          <template v-else>
            <el-select v-model="taskForm.robotId" placeholder="请选择机器人" style="width:100%">
              <el-option
                v-for="item in availableRobots"
                :key="item.id"
                :label="item.name"
                :value="item.id"
              />
            </el-select>
          </template>
        </el-form-item>
      </el-form>

      <template #footer>
        <div class="dialog-footer">
          <el-button @click="dialog.visible = false">取 消</el-button>
          <el-button type="primary" @click="submitTask" :loading="dialog.loading">确 定</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 任务详情对话框 -->
    <el-dialog
      title="任务详情"
      v-model="detailDialog.visible"
      width="800px"
      append-to-body
    >
      <div v-loading="detailLoading" v-if="currentTask">
        <!-- 基本信息 -->
        <el-descriptions :column="2" border>
          <el-descriptions-item label="任务名称">{{ currentTask.name }}</el-descriptions-item>
          <el-descriptions-item label="模板">{{ currentTask.templateName || '-' }}</el-descriptions-item>
          <el-descriptions-item label="任务类型">
            {{ currentTask.templateId ? '流程任务' : '简单任务' }}
          </el-descriptions-item>
          <el-descriptions-item label="触发方式">
            {{ getTriggerTypeText(currentTask.taskType) }}
          </el-descriptions-item>
          <el-descriptions-item label="状态">
            <span :class="'status-tag status-' + getStatusClass(currentTask.status)">{{ getStatusText(currentTask.status) }}</span>
          </el-descriptions-item>
          <el-descriptions-item label="优先级">
            <el-tag v-if="currentTask.priority === 1" type="danger">高</el-tag>
            <el-tag v-else-if="currentTask.priority === 2" type="warning">中</el-tag>
            <el-tag v-else type="info">低</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="机器人">{{ currentTask.robotName || '-' }}</el-descriptions-item>
          <el-descriptions-item label="机器人组">{{ currentTask.robotGroupName || '-' }}</el-descriptions-item>
          <el-descriptions-item label="创建时间">{{ currentTask.createTime }}</el-descriptions-item>
          <el-descriptions-item label="更新时间">{{ currentTask.updateTime }}</el-descriptions-item>
          <el-descriptions-item label="任务时长">{{ currentTask.duration }}分钟</el-descriptions-item>
          <el-descriptions-item label="终止原因" v-if="currentTask.terminateReason">{{ currentTask.terminateReason }}</el-descriptions-item>
        </el-descriptions>

        <!-- 表单内容（解析formContent） -->
        <el-divider content-position="left">表单内容</el-divider>
        <div v-if="formFields.length > 0">
          <el-descriptions :column="1" border>
            <el-descriptions-item v-for="field in formFields" :key="field.id" :label="field.label">
              {{ formatFormValue(field, currentTask.formData ? currentTask.formData[field.id] : '') }}
            </el-descriptions-item>
          </el-descriptions>
        </div>
        <div v-else class="empty-tip">无表单内容</div>

        <!-- 作业步骤 -->
        <el-divider content-position="left">作业步骤</el-divider>
        <el-table :data="taskSteps" border size="small" style="width:100%">
          <el-table-column label="步骤序号" width="80" align="center">
            <template #default="scope">{{ scope.row.orderNum }}</template>
          </el-table-column>
          <el-table-column prop="stepName" label="步骤名称" width="150" />
          <el-table-column prop="description" label="描述" min-width="200" />
          <el-table-column label="状态" width="100" align="center">
            <template #default="scope">
              <span :class="'status-tag status-' + getStepStatusClass(scope.row.status)">{{ getStepStatusText(scope.row.status) }}</span>
            </template>
          </el-table-column>
        </el-table>

        <!-- 任务日志 -->
        <el-divider content-position="left">任务日志</el-divider>
        <el-timeline>
          <el-timeline-item
            v-for="log in taskLogs"
            :key="log.id"
            :type="log.eventType === 'ERROR' ? 'danger' : 'info'"
            :timestamp="log.createTime"
          >
            {{ log.content }}
          </el-timeline-item>
        </el-timeline>
        <div v-if="taskLogs.length === 0" class="empty-tip">暂无日志</div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed, watch, getCurrentInstance } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  listTask,
  getTask,
  addTask,
  updateTask,
  delTask,
  banTask,
  resumeTask,
  pauseTask,
  continueTask,
  cancelTask,
  terminateTask,
  getTaskSteps,
  addTaskSteps,
  listLogByTask
} from '@/api/taskmgt'
import { listTemplate } from '@/api/taskmgt' // 模板列表

const { proxy } = getCurrentInstance()

// ========== 查询参数 ==========
const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  name: undefined,
  status: undefined,
  taskType: undefined,
  robotId: undefined,
  robotGroupId: undefined,
  displayOrder: 'status ASC, pending_order ASC, priority DESC, create_time DESC'
})

// ========== 数据 ==========
const loading = ref(false)
const taskList = ref([])
const total = ref(0)

// 机器人选项（模拟，实际应从接口获取）
const robotOptions = ref([
  { id: 1, name: '机器人A', groupId: 1, status: 'online' },
  { id: 2, name: '机器人B', groupId: 1, status: 'online' },
  { id: 3, name: '机器人C', groupId: 2, status: 'offline' }
])

const robotGroupOptions = ref([
  { id: 1, name: '配送组' },
  { id: 2, name: '巡检组' }
])

// 可用机器人（仅在线）
const availableRobots = computed(() => robotOptions.value.filter(r => r.status === 'online'))

// 模板选项
const templateOptions = ref([])

// 获取模板列表
const fetchTemplates = async () => {
  try {
    const res = await listTemplate({ pageNum: 1, pageSize: 100 })
    templateOptions.value = res.rows || []
  } catch (error) {
    console.error('获取模板列表失败', error)
  }
}

// 获取任务列表
const getList = async () => {
  loading.value = true
  try {
    const res = await listTask(queryParams)
    taskList.value = res.rows || []
    total.value = res.total || 0
  } catch (error) {
    ElMessage.error('获取任务列表失败')
  } finally {
    loading.value = false
  }
}

const handleQuery = () => {
  queryParams.pageNum = 1
  getList()
}

const resetQuery = () => {
  queryParams.name = undefined
  queryParams.status = undefined
  queryParams.taskType = undefined
  queryParams.robotId = undefined
  queryParams.robotGroupId = undefined
  handleQuery()
}

// 状态映射
const statusMap = {
  0: 'pending', 1: 'ready', 2: 'executing', 3: 'paused', 4: 'completed', 5: 'disabled', 6: 'aborted'
}
const statusTextMap = {
  0: '未开始', 1: '准备中', 2: '执行中', 3: '已暂停', 4: '已完成', 5: '已禁用', 6: '已终止'
}
const getStatusText = (status) => statusTextMap[status] || status
const getStatusClass = (status) => statusMap[status] || 'pending'

const triggerTypeMap = {
  1: '定时任务', 2: '电量任务', 3: '闲时任务'
}
const getTriggerTypeText = (type) => triggerTypeMap[type] || type
const getTriggerTypeTag = (type) => {
  const map = { 1: 'primary', 2: 'success', 3: 'warning' }
  return map[type] || 'info'
}

const canEdit = (row) => [0, 1, 5].includes(row.status)
const canDelete = (row) => [0, 1, 4, 5, 6].includes(row.status)

// 操作处理
const handleStatusCommand = async (cmd, row) => {
  const id = row.id
  try {
    switch (cmd) {
      case 'pause':
        await pauseTask(id)
        ElMessage.success('已暂停')
        break
      case 'continue':
        await continueTask(id)
        ElMessage.success('已继续')
        break
      case 'cancel':
        await cancelTask(id)
        ElMessage.success('已取消')
        break
      case 'terminate': {
        const { value: reason } = await ElMessageBox.prompt('请输入终止原因', '终止任务', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          inputPlaceholder: '终止原因'
        })
        await terminateTask(id, reason)
        ElMessage.success('已终止')
        break
      }
      case 'ban':
        await banTask(id)
        ElMessage.success('已禁用')
        break
      case 'resume':
        await resumeTask(id)
        ElMessage.success('已恢复')
        break
    }
    getList()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('操作失败')
    }
  }
}

// 删除任务
const handleDelete = (row) => {
  ElMessageBox.confirm('确认删除该任务吗？', '警告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    await delTask(row.id)
    ElMessage.success('删除成功')
    getList()
  }).catch(() => {})
}

// ========== 任务创建/编辑对话框 ==========
const dialog = reactive({
  visible: false,
  title: '',
  mode: 'create', // 'create' or 'edit'
  loading: false
})

const taskFormRef = ref()
const taskForm = reactive({
  id: undefined,
  name: '',
  type: 'process', // 'process' or 'simple'
  templateId: undefined,
  simpleAction: '',
  formData: {}, // 存储动态表单值
  priority: 2,
  isGroupTask: false,
  duration: 30,
  taskType: 1, // 1定时 2电量 3闲时
  cronExpression: '',
  scheduledTime: undefined,
  batteryThreshold: 80,
  idleTime: 30,
  robotId: undefined,
  robotGroupId: undefined,
})

const taskRules = {
  name: [{ required: true, message: '请输入任务名称', trigger: 'blur' }],
  templateId: [{ required: true, message: '请选择模板', trigger: 'change' }],
  simpleAction: [{ required: true, message: '请选择简单动作', trigger: 'change' }],
  targetId: [{ required: true, message: '请选择机器人或机器人组', trigger: 'change' }],
  batteryThreshold: [{ required: true, message: '请输入触发电量', trigger: 'blur' }],
  idleTime: [{ required: true, message: '请输入闲时等待时间', trigger: 'blur' }],
}

// 当前选中的模板
const currentTemplate = computed(() => {
  if (!taskForm.templateId) return null
  return templateOptions.value.find(t => t.id === taskForm.templateId)
})

// 模板变更时初始化formData
const onTemplateChange = (val) => {
  const template = templateOptions.value.find(t => t.id === val)
  if (template) {
    // 解析模板字段
    let fields = []
    try {
      if (template.formContent) {
        const content = JSON.parse(template.formContent)
        fields = content.fields || []
      }
    } catch (e) {
      console.warn('解析模板字段失败', e)
    }
    // 初始化formData
    const formData = {}
    fields.forEach(field => {
      formData[field.id] = ''
    })
    taskForm.formData = formData
  }
}

// 打开新增对话框
const handleAdd = (type) => {
  dialog.mode = 'create'
  dialog.title = type === 'process' ? '创建流程任务' : '创建简单任务'
  // 重置表单
  taskForm.id = undefined
  taskForm.name = ''
  taskForm.type = type
  taskForm.templateId = undefined
  taskForm.simpleAction = ''
  taskForm.formData = {}
  taskForm.priority = 2
  taskForm.isGroupTask = false
  taskForm.duration = 30
  taskForm.taskType = 1
  taskForm.cronExpression = ''
  taskForm.scheduledTime = undefined
  taskForm.batteryThreshold = 80
  taskForm.idleTime = 30
  taskForm.robotId = undefined
  taskForm.robotGroupId = undefined
  dialog.visible = true
}

// 打开编辑对话框
const handleEdit = (row) => {
  dialog.mode = 'edit'
  dialog.title = '修改任务'
  // 填充基本信息（注意：编辑时通常不允许修改模板和表单内容，仅修改基本信息）
  taskForm.id = row.id
  taskForm.name = row.name
  taskForm.type = row.templateId ? 'process' : 'simple'
  taskForm.templateId = row.templateId
  taskForm.simpleAction = row.simpleAction || ''
  taskForm.priority = row.priority
  taskForm.isGroupTask = row.isGroupTask === 1
  taskForm.duration = row.duration
  taskForm.taskType = row.taskType
  taskForm.cronExpression = row.cronExpression || ''
  taskForm.scheduledTime = row.scheduledTime
  taskForm.batteryThreshold = row.batteryThreshold
  taskForm.idleTime = row.idleTime
  taskForm.robotId = row.robotId
  taskForm.robotGroupId = row.robotGroupId
  // 解析formData（如果需要展示，但编辑时通常不修改表单内容）
  if (row.formContent) {
    try {
      taskForm.formData = JSON.parse(row.formContent) || {}
    } catch (e) {
      taskForm.formData = {}
    }
  } else {
    taskForm.formData = {}
  }
  dialog.visible = true
}

// 取消对话框
const cancelTaskDialog = () => {
  dialog.visible = false
  taskFormRef.value?.resetFields()
}

// 提交任务
const submitTask = async () => {
  if (!taskFormRef.value) return
  await taskFormRef.value.validate(async (valid) => {
    if (!valid) return
    dialog.loading = true
    try {
      // 构造DTO
      const dto = {
        name: taskForm.name,
        priority: taskForm.priority,
        isGroupTask: taskForm.isGroupTask ? 1 : 0,
        duration: taskForm.duration,
        taskType: taskForm.taskType,
        cronExpression: taskForm.cronExpression,
        scheduledTime: taskForm.scheduledTime,
        batteryThreshold: taskForm.batteryThreshold,
        idleTime: taskForm.idleTime,
        robotId: taskForm.isGroupTask ? null : taskForm.robotId,
        robotGroupId: taskForm.isGroupTask ? taskForm.robotGroupId : null,
        formContent: JSON.stringify(taskForm.formData) // 将表单值序列化
      }

      // 如果是流程任务，需要传templateId
      if (taskForm.type === 'process') {
        dto.templateId = taskForm.templateId
      }

      let taskId
      if (dialog.mode === 'create') {
        const res = await addTask(dto)
        taskId = res.data.id
        ElMessage.success('创建成功')
      } else {
        await updateTask(taskForm.id, dto)
        taskId = taskForm.id
        ElMessage.success('修改成功')
      }

      // 如果是新增流程任务，需要创建步骤
      if (dialog.mode === 'create' && taskForm.type === 'process' && currentTemplate.value) {
        await createTaskSteps(taskId, currentTemplate.value)
      }

      dialog.visible = false
      getList()
    } catch (error) {
      console.error(error)
      ElMessage.error('操作失败')
    } finally {
      dialog.loading = false
    }
  })
}

// 根据模板创建任务步骤
const createTaskSteps = async (taskId, template) => {
  let steps = []
  try {
    if (template.workflow) {
      const wf = JSON.parse(template.workflow)
      steps = wf.steps || []
    }
  } catch (e) {
    console.warn('解析workflow失败', e)
  }
  if (steps.length === 0) return

  // 构造TaskStepDto列表
  const stepDtos = steps.map((step, index) => ({
    stepName: step.name,
    description: step.description,
    orderNum: index + 1
  }))
  await addTaskSteps(taskId, stepDtos)
}

// ========== 任务详情对话框 ==========
const detailDialog = reactive({
  visible: false
})
const detailLoading = ref(false)
const currentTask = ref(null)
const taskSteps = ref([])
const taskLogs = ref([])
const formFields = ref([]) // 用于展示表单字段定义

const handleView = async (row) => {
  detailLoading.value = true
  detailDialog.visible = true
  try {
    // 获取任务详情
    const taskRes = await getTask(row.id)
    currentTask.value = taskRes.data

    // 解析表单字段定义（从模板）
    if (currentTask.value.templateId) {
      const template = templateOptions.value.find(t => t.id === currentTask.value.templateId)
      if (template && template.formContent) {
        try {
          const content = JSON.parse(template.formContent)
          formFields.value = content.fields || []
        } catch (e) {
          formFields.value = []
        }
      } else {
        formFields.value = []
      }
    } else {
      formFields.value = [] // 简单任务无模板字段
    }

    // 获取步骤
    const stepsRes = await getTaskSteps(row.id)
    taskSteps.value = stepsRes.data || []

    // 获取日志
    const logsRes = await listLogByTask(row.id)
    taskLogs.value = logsRes.rows || []
  } catch (error) {
    ElMessage.error('获取详情失败')
    detailDialog.visible = false
  } finally {
    detailLoading.value = false
  }
}

// 格式化表单值展示
const formatFormValue = (field, value) => {
  if (value === null || value === undefined) return '-'
  if (Array.isArray(value)) {
    return value.map(v => v.name || v).join(', ')
  }
  return String(value)
}

// 步骤状态
const stepStatusMap = {
  0: 'pending', 1: 'executing', 2: 'completed', 3: 'paused', 4: 'terminated'
}
const stepStatusTextMap = {
  0: '未开始', 1: '执行中', 2: '已完成', 3: '已暂停', 4: '已终止'
}
const getStepStatusText = (status) => stepStatusTextMap[status] || status
const getStepStatusClass = (status) => stepStatusMap[status] || 'pending'

onMounted(() => {
  getList()
  fetchTemplates()
})
</script>

<style scoped>
.app-container {
  padding: 20px;
}
.search-card {
  margin-bottom: 20px;
}
.table-card {
  margin-bottom: 20px;
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
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
.empty-tip {
  text-align: center;
  color: #999;
  padding: 20px;
}
.el-form-item-msg {
  font-size: 12px;
  color: #999;
  line-height: 1;
  margin-top: 4px;
}
</style>
