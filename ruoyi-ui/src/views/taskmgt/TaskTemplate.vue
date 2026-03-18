<template>
  <div class="app-container">
    <!-- 搜索栏 -->
    <el-card class="search-card">
      <el-form :model="queryParams" ref="queryRef" :inline="true" label-width="80px">
        <el-form-item label="模板名称" prop="name">
          <el-input
            v-model="queryParams.name"
            placeholder="请输入模板名称"
            clearable
            style="width: 200px"
            @keyup.enter="handleQuery"
          />
        </el-form-item>
        <el-form-item label="机器人组" prop="robotGroupIds">
          <el-select
            v-model="queryParams.robotGroupIds"
            multiple
            placeholder="请选择机器人组"
            clearable
            style="width: 200px"
          >
            <el-option
              v-for="item in robotGroupOptions"
              :key="item.id"
              :label="item.name"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-select
            v-model="queryParams.status"
            placeholder="模板状态"
            clearable
            style="width: 120px"
          >
            <el-option label="启用" :value="0" />
            <el-option label="禁用" :value="1" />
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
          <span>模板列表</span>
          <el-button type="primary" icon="Plus" @click="handleAdd">新增模板</el-button>
        </div>
      </template>

      <!-- 表格 -->
      <el-table v-loading="loading" :data="templateList" border style="width: 100%">
        <el-table-column label="ID" prop="id" width="80" align="center" />
        <el-table-column label="模板名称" prop="name" min-width="150" show-overflow-tooltip />
        <el-table-column label="描述" prop="description" min-width="200" show-overflow-tooltip />
        <el-table-column label="适用机器人组" width="180">
          <template #default="scope">
            <el-tag v-for="gid in scope.row.robotGroupIds" :key="gid" size="small" style="margin-right:4px;">
              {{ getRobotGroupName(gid) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="字段数量" width="100" align="center">
          <template #default="scope">
            <el-tag size="small" type="success">{{ getFieldCount(scope.row) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="步骤数量" width="100" align="center">
          <template #default="scope">
            <el-tag size="small" type="warning">{{ getStepCount(scope.row) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100" align="center">
          <template #default="scope">
            <el-tag :type="scope.row.status === 0 ? 'success' : 'danger'">
              {{ scope.row.status === 0 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="创建时间" prop="createTime" width="160" align="center" />
        <el-table-column label="操作" width="280" fixed="right" align="center">
          <template #default="scope">
            <el-button link type="primary" icon="View" @click="handleView(scope.row)">查看</el-button>
            <el-button link type="primary" icon="Edit" @click="handleEdit(scope.row)" v-if="scope.row.status === 0">编辑</el-button>
            <el-button link type="danger" icon="Delete" @click="handleDelete(scope.row)" v-if="scope.row.status === 0">删除</el-button>
            <el-button link type="warning" icon="CircleClose" @click="handleBan(scope.row)" v-if="scope.row.status === 0">禁用</el-button>
            <el-button link type="success" icon="CircleCheck" @click="handleResume(scope.row)" v-if="scope.row.status === 1">恢复</el-button>
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

    <!-- 模板表单对话框 -->
    <el-dialog
      :title="dialog.title"
      v-model="dialog.visible"
      width="800px"
      append-to-body
      @close="cancel"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="模板名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入模板名称" />
        </el-form-item>
        <el-form-item label="模板描述" prop="description">
          <el-input v-model="form.description" type="textarea" :rows="3" placeholder="请输入模板描述" />
        </el-form-item>
        <el-form-item label="适用机器人组" prop="robotGroupIds">
          <el-select v-model="form.robotGroupIds" multiple placeholder="请选择适用机器人组" style="width:100%">
            <el-option
              v-for="item in robotGroupOptions"
              :key="item.id"
              :label="item.name"
              :value="item.id"
            />
          </el-select>
          <div class="el-form-item-msg">可多选，任务只能分配给这些组内的机器人</div>
        </el-form-item>

        <!-- 表单字段定义 -->
        <el-divider content-position="left">表单字段定义</el-divider>
        <div class="fields-container">
          <el-button size="small" type="primary" @click="addField" style="margin-bottom:10px">
            <el-icon><Plus /></el-icon>添加字段
          </el-button>
          <div v-for="(field, index) in form.fields" :key="index" class="field-item">
            <el-row :gutter="10">
              <el-col :span="6">
                <el-input v-model="field.id" placeholder="字段ID（英文）" size="small" />
              </el-col>
              <el-col :span="6">
                <el-input v-model="field.label" placeholder="字段标签" size="small" />
              </el-col>
              <el-col :span="4">
                <el-select v-model="field.type" placeholder="类型" size="small" style="width:100%">
                  <el-option label="文本" value="text" />
                  <el-option label="数字" value="number" />
                  <el-option label="日期" value="date" />
                  <el-option label="时间" value="time" />
                  <el-option label="下拉" value="select" />
                  <el-option label="位置" value="location" />
                  <el-option label="图片" value="image" />
                  <el-option label="音频" value="audio" />
                  <el-option label="视频" value="video" />
                  <el-option label="文件" value="file" />
                </el-select>
              </el-col>
              <el-col :span="2">
                <el-checkbox v-model="field.required" label="必填" size="small" />
              </el-col>
              <el-col :span="4" style="text-align:right">
                <el-button type="danger" link @click="removeField(index)">删除</el-button>
              </el-col>
            </el-row>
            <!-- 文件类型额外配置 -->
            <el-row v-if="['image','audio','video','file'].includes(field.type)" :gutter="10" style="margin-top:5px">
              <el-col :span="8">
                <el-input-number v-model="field.maxCount" :min="1" :max="10" size="small" placeholder="最大数量" style="width:100%" />
              </el-col>
              <el-col :span="8">
                <el-input-number v-model="field.maxSize" :min="1" :max="100" size="small" placeholder="大小限制(MB)" style="width:100%" />
              </el-col>
              <el-col :span="8" v-if="field.type === 'file'">
                <el-select v-model="field.accept" multiple placeholder="允许格式" size="small" style="width:100%">
                  <el-option label=".pdf" value=".pdf" />
                  <el-option label=".doc/.docx" value=".doc,.docx" />
                  <el-option label=".xls/.xlsx" value=".xls,.xlsx" />
                  <el-option label=".ppt/.pptx" value=".ppt,.pptx" />
                  <el-option label=".txt" value=".txt" />
                  <el-option label=".zip/.rar" value=".zip,.rar" />
                </el-select>
              </el-col>
            </el-row>
          </div>
          <div v-if="form.fields.length === 0" class="empty-tip">暂无字段定义，请点击“添加字段”</div>
        </div>

        <!-- 流程步骤定义 -->
        <el-divider content-position="left">标准作业流程</el-divider>
        <div class="steps-container">
          <el-button size="small" type="primary" @click="addStep" style="margin-bottom:10px">
            <el-icon><Plus /></el-icon>添加步骤
          </el-button>
          <div v-for="(step, index) in form.steps" :key="index" class="step-item">
            <el-row :gutter="10">
              <el-col :span="8">
                <el-input v-model="step.name" placeholder="步骤名称" size="small" />
              </el-col>
              <el-col :span="6">
                <el-input-number v-model="step.estimatedTime" :min="1" :max="120" size="small" placeholder="预估分钟" style="width:100%" />
              </el-col>
              <el-col :span="8">
                <el-input v-model="step.description" :placeholder="'步骤描述，可使用' + step.fieldId" size="small" />
              </el-col>
              <el-col :span="2" style="text-align:right">
                <el-button type="danger" link @click="removeStep(index)">删除</el-button>
              </el-col>
            </el-row>
          </div>
          <div v-if="form.steps.length === 0" class="empty-tip">暂无步骤定义，请点击“添加步骤”</div>
        </div>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="cancel">取 消</el-button>
          <el-button type="primary" @click="submitForm" :loading="submitLoading">确 定</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 模板详情查看对话框 -->
    <el-dialog title="模板详情" v-model="viewDialog.visible" width="600px" append-to-body>
      <div v-if="viewDialog.data">
        <el-descriptions :column="1" border>
          <el-descriptions-item label="模板名称">{{ viewDialog.data.name }}</el-descriptions-item>
          <el-descriptions-item label="描述">{{ viewDialog.data.description }}</el-descriptions-item>
          <el-descriptions-item label="适用机器人组">
            <el-tag v-for="gid in viewDialog.data.robotGroupIds" :key="gid" style="margin-right:4px;">
              {{ getRobotGroupName(gid) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="viewDialog.data.status === 0 ? 'success' : 'danger'">
              {{ viewDialog.data.status === 0 ? '启用' : '禁用' }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="创建时间">{{ viewDialog.data.createTime }}</el-descriptions-item>
          <el-descriptions-item label="更新时间">{{ viewDialog.data.updateTime }}</el-descriptions-item>
        </el-descriptions>

        <el-divider />
        <h4>表单字段</h4>
        <el-table :data="viewDialog.fields" border size="small">
          <el-table-column prop="id" label="字段ID" width="120" />
          <el-table-column prop="label" label="标签" width="120" />
          <el-table-column prop="type" label="类型" width="100">
            <template #default="scope">{{ getFieldTypeText(scope.row.type) }}</template>
          </el-table-column>
          <el-table-column prop="required" label="必填" width="60">
            <template #default="scope">{{ scope.row.required ? '是' : '否' }}</template>
          </el-table-column>
          <el-table-column label="配置" min-width="150">
            <template #default="scope">
              <span v-if="['image','audio','video','file'].includes(scope.row.type)">
                最多{{ scope.row.maxCount }}个，≤{{ scope.row.maxSize }}MB
              </span>
            </template>
          </el-table-column>
        </el-table>

        <el-divider />
        <h4>作业步骤</h4>
        <el-table :data="viewDialog.steps" border size="small">
          <el-table-column prop="name" label="步骤名称" width="150" />
          <el-table-column prop="description" label="描述" min-width="200" />
          <el-table-column prop="estimatedTime" label="预估(分钟)" width="100" align="center" />
        </el-table>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import {computed, getCurrentInstance, onMounted, reactive, ref} from 'vue'
import {
  addTemplate,
  banTemplate,
  delTemplate,
  getTemplate,
  listTemplate,
  resumeTemplate,
  updateTemplate
} from '@/api/taskmgt/taskmgt'

const { proxy } = getCurrentInstance()

// ========== 查询参数 ==========
const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  name: undefined,
  robotGroupIds: [], // 修改为数组
  status: undefined
})

// ========== 数据 ==========
const loading = ref(false)
const templateList = ref([])
const total = ref(0)

// 机器人组选项（模拟，需替换为真实接口）
const robotGroupOptions = ref([
  { id: 1, name: '配送机器人组' },
  { id: 2, name: '巡检机器人组' },
  { id: 3, name: '清洁机器人组' },
  { id: 4, name: '安防机器人组' }
])

// 获取所有机器人组ID（用于默认全选）
const allGroupIds = computed(() => robotGroupOptions.value.map(g => g.id))

// 获取机器人组名称
const getRobotGroupName = (groupId) => {
  const group = robotGroupOptions.value.find(g => g.id === groupId)
  return group ? group.name : groupId
}

// 确保值为数组（兼容后端可能返回字符串的情况）
const ensureArray = (ids) => {
  if (!ids) return []
  if (Array.isArray(ids)) return ids
  if (typeof ids === 'string') return ids.split(',').map(s => Number(s.trim()))
  return [ids]
}

// 获取字段数量（从formContent解析）
const getFieldCount = (row) => {
  if (!row.formContent) return 0
  try {
    const content = JSON.parse(row.formContent)
    return content.fields ? content.fields.length : 0
  } catch {
    return 0
  }
}

// 获取步骤数量（从workflow解析）
const getStepCount = (row) => {
  if (!row.workflow) return 0
  try {
    const workflow = JSON.parse(row.workflow)
    return workflow.steps ? workflow.steps.length : 0
  } catch {
    return 0
  }
}

// 获取列表
const getList = async () => {
  loading.value = true
  try {
    // 构建请求参数，将 robotGroupIds 数组转换为逗号分隔字符串
    const params = {
      pageNum: queryParams.pageNum,
      pageSize: queryParams.pageSize,
      name: queryParams.name,
      status: queryParams.status,
    }
    if (queryParams.robotGroupIds && queryParams.robotGroupIds.length > 0) {
      params.robotGroupIds = queryParams.robotGroupIds.join(',')
    }
    const res = await listTemplate(params)
    templateList.value = res.rows || []
    total.value = res.total || 0
  } catch (error) {
    console.error(error)
    ElMessage.error('获取模板列表失败')
  } finally {
    loading.value = false
  }
}

// 搜索
const handleQuery = () => {
  queryParams.pageNum = 1
  getList()
}

// 重置
const resetQuery = () => {
  queryParams.name = undefined
  queryParams.robotGroupIds = [] // 重置为空数组
  queryParams.status = undefined
  handleQuery()
}

// ========== 表单对话框 ==========
const dialog = reactive({
  visible: false,
  title: ''
})

const formRef = ref()
const form = reactive({
  id: undefined,
  name: '',
  description: '',
  robotGroupIds: [], // 修改为数组
  fields: [],
  steps: []
})

const rules = {
  name: [{ required: true, message: '请输入模板名称', trigger: 'blur' }],
  robotGroupIds: [{ required: true, message: '请选择适用机器人组', trigger: 'change' }]
}

const submitLoading = ref(false)

// 添加字段
const addField = () => {
  form.fields.push({
    id: '',
    label: '',
    type: 'text',
    required: false,
    maxCount: 1,
    maxSize: 10,
    accept: []
  })
}

// 删除字段
const removeField = (index) => {
  form.fields.splice(index, 1)
}

// 添加步骤
const addStep = () => {
  form.steps.push({
    name: '',
    description: '',
    estimatedTime: 5
  })
}

// 删除步骤
const removeStep = (index) => {
  form.steps.splice(index, 1)
}

// 重置表单
const resetForm = () => {
  form.id = undefined
  form.name = ''
  form.description = ''
  form.robotGroupIds = []
  form.fields = []
  form.steps = []
  proxy.resetForm('formRef')
}

// 取消
const cancel = () => {
  dialog.visible = false
  resetForm()
}

// 新增 - 默认全选所有机器人组
const handleAdd = () => {
  dialog.title = '新增模板'
  // 设置默认全选
  form.robotGroupIds = [...allGroupIds.value]
  dialog.visible = true
}

// 编辑
const handleEdit = (row) => {
  dialog.title = '修改模板'
  // 反解析formContent和workflow
  let fields = [], steps = []
  try {
    if (row.formContent) {
      const content = JSON.parse(row.formContent)
      fields = content.fields || []
    }
    if (row.workflow) {
      const wf = JSON.parse(row.workflow)
      steps = wf.steps || []
    }
  } catch (e) {
    console.warn('解析模板内容失败', e)
  }

  form.id = row.id
  form.name = row.name
  form.description = row.description
  form.robotGroupIds = ensureArray(row.robotGroupIds)
  form.fields = fields
  form.steps = steps

  dialog.visible = true
}

// 提交
const submitForm = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (valid) {
      submitLoading.value = true
      try {
        // 组装DTO，robotGroupIds 直接传数组
        const dto = {
          name: form.name,
          description: form.description,
          robotGroupIds: form.robotGroupIds,
          formContent: JSON.stringify({ fields: form.fields }),
          workflow: JSON.stringify({ steps: form.steps })
        }
        if (form.id) {
          await updateTemplate(form.id, dto)
          ElMessage.success('修改成功')
        } else {
          await addTemplate(dto)
          ElMessage.success('新增成功')
        }
        dialog.visible = false
        resetForm()
        getList()
      } catch (error) {
        console.error(error)
        ElMessage.error('操作失败')
      } finally {
        submitLoading.value = false
      }
    }
  })
}

// ========== 查看详情 ==========
const viewDialog = reactive({
  visible: false,
  data: null,
  fields: [],
  steps: []
})

const handleView = async (row) => {
  try {
    const res = await getTemplate(row.id)
    viewDialog.data = res.data
    // 解析字段和步骤
    try {
      if (res.data.formContent) {
        const content = JSON.parse(res.data.formContent)
        viewDialog.fields = content.fields || []
      } else {
        viewDialog.fields = []
      }
      if (res.data.workflow) {
        const wf = JSON.parse(res.data.workflow)
        viewDialog.steps = wf.steps || []
      } else {
        viewDialog.steps = []
      }
    } catch (e) {
      viewDialog.fields = []
      viewDialog.steps = []
    }
    viewDialog.visible = true
  } catch (error) {
    ElMessage.error('获取详情失败')
  }
}

// ========== 删除 ==========
const handleDelete = (row) => {
  ElMessageBox.confirm('确认删除该模板吗？', '警告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    await delTemplate(row.id)
    ElMessage.success('删除成功')
    getList()
  }).catch(() => {})
}

// ========== 禁用/恢复 ==========
const handleBan = (row) => {
  ElMessageBox.confirm('禁用后，基于此模板的任务将无法创建，确定禁用吗？', '警告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    await banTemplate(row.id)
    ElMessage.success('禁用成功')
    getList()
  }).catch(() => {})
}

const handleResume = (row) => {
  ElMessageBox.confirm('确定恢复该模板吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'info'
  }).then(async () => {
    await resumeTemplate(row.id)
    ElMessage.success('恢复成功')
    getList()
  }).catch(() => {})
}

// 字段类型文本映射
const getFieldTypeText = (type) => {
  const map = {
    'text': '文本',
    'number': '数字',
    'date': '日期',
    'time': '时间',
    'select': '下拉',
    'location': '位置',
    'image': '图片',
    'audio': '音频',
    'video': '视频',
    'file': '文件'
  }
  return map[type] || type
}

onMounted(() => {
  getList()
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
.fields-container, .steps-container {
  border: 1px dashed #dcdfe6;
  border-radius: 4px;
  padding: 15px;
  background: #fafafa;
  margin-bottom: 20px;
}
.field-item, .step-item {
  background: white;
  padding: 10px;
  border-radius: 4px;
  margin-bottom: 10px;
  border: 1px solid #e8e8e8;
}
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
