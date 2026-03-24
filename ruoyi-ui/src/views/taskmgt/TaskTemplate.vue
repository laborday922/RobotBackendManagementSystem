<template>
  <div class="app-container">
    <!-- 搜索栏 - 实时筛选，无重置按钮 -->
    <el-card class="search-card">
      <el-form :model="queryParams" ref="queryRef" :inline="true" label-width="80px">
        <el-form-item label="模板名称" prop="name">
          <el-input
            v-model="queryParams.name"
            placeholder="请输入模板名称"
            clearable
            style="width: 200px"
            @input="debouncedQuery"
            @clear="debouncedQuery"
          />
        </el-form-item>
        <el-form-item label="机器人组" prop="robotGroupIds">
          <el-select
            v-model="queryParams.robotGroupIds"
            multiple
            placeholder="请选择机器人组"
            clearable
            style="width: 200px"
            @change="handleQuery"
            @clear="handleQuery"
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
            @change="handleQuery"
            @clear="handleQuery"
          >
            <el-option label="启用" :value="0" />
            <el-option label="禁用" :value="1" />
          </el-select>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 操作按钮 -->
    <el-card class="table-card">
      <template #header>
        <div class="card-header">
          <span>模板列表</span>
          <el-button type="primary" icon="el-icon-plus" @click="handleAdd">新增模板</el-button>
        </div>
      </template>

      <!-- 表格 -->
      <el-table v-loading="loading" :data="templateList" border style="width: 100%">
        <el-table-column label="ID" prop="id" width="80" align="center" />
        <el-table-column label="模板名称" prop="name" min-width="150" show-overflow-tooltip />
        <el-table-column label="描述" prop="description" min-width="200" show-overflow-tooltip />
        <el-table-column label="适用机器人组" width="180">
          <template slot-scope="scope">
            <el-tag v-for="(name, index) in scope.row.robotGroupNames" :key="index" size="small" style="margin-right:4px;">
              {{ name }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="字段数量" width="100" align="center">
          <template slot-scope="scope">
            <el-tag size="small" type="success">{{ getFieldCount(scope.row) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="步骤数量" width="100" align="center">
          <template slot-scope="scope">
            <el-tag size="small" type="warning">{{ getStepCount(scope.row) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100" align="center">
          <template slot-scope="scope">
            <el-tag :type="scope.row.status === 0 ? 'success' : 'danger'">
              {{ scope.row.status === 0 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="创建时间" prop="createTime" width="160" align="center" />
        <!-- 操作列改为图标按钮 -->
        <el-table-column label="操作" width="220" fixed="right" align="center">
          <template slot-scope="scope">
            <!-- 查看 -->
            <el-button size="small" circle title="查看" @click="handleView(scope.row)">
              <i class="fas fa-eye"></i>
            </el-button>

            <!-- 编辑 -->
            <el-button size="small" type="primary" circle title="编辑" @click="handleEdit(scope.row)">
              <i class="fas fa-edit"></i>
            </el-button>

            <!-- 删除 -->
            <el-button
              v-if="scope.row.status === 1"
              size="small"
              type="danger"
              circle
              title="删除"
              @click="handleDelete(scope.row)"
            >
              <i class="fas fa-trash-alt"></i>
            </el-button>

            <!-- 禁用 -->
            <el-button
              v-if="scope.row.status === 0"
              size="small"
              type="warning"
              circle
              title="禁用"
              @click="handleBan(scope.row)"
            >
              <i class="fas fa-ban"></i>
            </el-button>

            <!-- 恢复 -->
            <el-button
              v-if="scope.row.status === 1"
              size="small"
              type="success"
              circle
              title="恢复"
              @click="handleResume(scope.row)"
            >
              <i class="fas fa-undo"></i>
            </el-button>
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

    <!-- 模板表单对话框 -->
    <el-dialog
      :title="dialog.title"
      :visible.sync="dialog.visible"
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
            <i class="el-icon-plus"></i>添加字段
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
                <el-checkbox v-model="field.required">必填</el-checkbox>
              </el-col>
              <el-col :span="4" style="text-align:right">
                <el-button type="danger" size="small" @click="removeField(index)">删除</el-button>
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
          <div v-if="form.fields.length === 0" class="empty-tip">暂无字段定义，请点击"添加字段"</div>
        </div>

        <!-- 流程步骤定义 -->
        <el-divider content-position="left">标准作业流程</el-divider>
        <div class="steps-container">
          <div class="step-tip">
            <i class="el-icon-info" style="color: #409eff;"></i>
            <span>提示：在步骤描述中可使用 <code>{字段ID}</code> 作为占位符，如"导航从{place1}到{place2}"，创建任务时将自动替换为用户输入的值</span>
          </div>
          <el-button size="small" type="primary" @click="addStep" style="margin-bottom:10px; margin-top:10px;">
            <i class="el-icon-plus"></i>添加步骤
          </el-button>
          <div v-for="(step, index) in form.steps" :key="index" class="step-item">
            <el-row :gutter="10">
              <el-col :span="6">
                <el-input v-model="step.name" placeholder="步骤名称" size="small" />
              </el-col>
              <el-col :span="6">
                <div style="display:flex; align-items:center;">
                  <span style="flex-shrink:0; margin-right:4px;">预估:</span>
                  <el-input-number v-model="step.estimatedTime" :min="1" :max="120" size="small" style="flex:1;" />
                </div>
              </el-col>
              <el-col :span="10">
                <el-input
                  v-model="step.description"
                  placeholder="步骤描述（可使用{字段ID}作为占位符）"
                  size="small"
                />
              </el-col>
              <el-col :span="2" style="text-align:right">
                <el-button type="danger" size="small" @click="removeStep(index)">删除</el-button>
              </el-col>
            </el-row>
          </div>
          <div v-if="form.steps.length === 0" class="empty-tip">暂无步骤定义，请点击"添加步骤"</div>
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
    <el-dialog title="模板详情" :visible.sync="viewDialog.visible" width="600px" append-to-body>
      <div v-if="viewDialog.data">
        <el-descriptions :column="1" border>
          <el-descriptions-item label="模板名称">{{ viewDialog.data.name }}</el-descriptions-item>
          <el-descriptions-item label="描述">{{ viewDialog.data.description }}</el-descriptions-item>
          <el-descriptions-item label="适用机器人组">
            <el-tag v-for="name in viewDialog.data.robotGroupNames" :key="name" style="margin-right:4px;">
              {{ name }}
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
            <template slot-scope="scope">{{ getFieldTypeText(scope.row.type) }}</template>
          </el-table-column>
          <el-table-column prop="required" label="必填" width="60">
            <template slot-scope="scope">{{ scope.row.required ? '是' : '否' }}</template>
          </el-table-column>
          <el-table-column label="配置" min-width="150">
            <template slot-scope="scope">
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

<script>
import {
  listTemplate,
  addTemplate,
  updateTemplate,
  getTemplate,
  delTemplate,
  banTemplate,
  resumeTemplate
} from '@/api/taskmgt/taskmgt'
import { listGroups } from '@/api/system/robots'
import debounce from 'lodash/debounce'

export default {
  name: 'TaskTemplate',
  data() {
    return {
      // 查询参数
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        name: undefined,
        robotGroupIds: [],
        status: undefined
      },
      loading: false,
      templateList: [],
      total: 0,
      robotGroupOptions: [],
      // 表单对话框
      dialog: {
        visible: false,
        title: ''
      },
      // 表单数据
      form: {
        id: null,
        name: '',
        description: '',
        robotGroupIds: [],
        fields: [],
        steps: []
      },
      rules: {
        name: [{ required: true, message: '请输入模板名称', trigger: 'blur' }],
        robotGroupIds: [{ required: true, message: '请选择适用机器人组', trigger: 'change' }]
      },
      submitLoading: false,
      // 查看详情对话框
      viewDialog: {
        visible: false,
        data: null,
        fields: [],
        steps: []
      }
    }
  },
  computed: {
    allGroupIds() {
      return this.robotGroupOptions.map(g => g.id)
    }
  },
  created() {
    this.getRobotGroups()
    this.getList()
    this.debouncedQuery = debounce(this.handleQuery, 500)
  },
  beforeDestroy() {
    this.debouncedQuery.cancel()
  },
  methods: {
    // 确保数组
    ensureArray(ids) {
      if (!ids) return []
      if (Array.isArray(ids)) return ids
      if (typeof ids === 'string') return ids.split(',').map(s => Number(s.trim()))
      return [ids]
    },
    // 获取机器人组
    async getRobotGroups() {
      try {
        const res = await listGroups()
        this.robotGroupOptions = res.rows || []
      } catch (error) {
        this.$message.error('获取机器人组失败')
        console.error(error)
      }
    },
    // 获取模板列表
    async getList() {
      this.loading = true
      try {
        const params = { ...this.queryParams }
        if (params.robotGroupIds && params.robotGroupIds.length) {
          params.robotGroupIds = params.robotGroupIds.join(',')
        }
        const res = await listTemplate(params)
        this.templateList = (res.rows || []).map(tpl => {
          // 解析 robotGroupIds
          let robotGroupIds = []
          if (tpl.robotGroupIds) {
            if (Array.isArray(tpl.robotGroupIds)) {
              robotGroupIds = tpl.robotGroupIds
            } else if (typeof tpl.robotGroupIds === 'string') {
              robotGroupIds = tpl.robotGroupIds.split(',').map(s => Number(s.trim()))
            }
          }
          // 优先使用后端返回的 robotGroupNames
          let robotGroupNames = tpl.robotGroupNames && Array.isArray(tpl.robotGroupNames)
            ? tpl.robotGroupNames
            : robotGroupIds.map(id => {
              const group = this.robotGroupOptions.find(g => g.id === id)
              return group ? group.name : id
            })

          // 解析 formContent 和 workflow
          let fields = [], steps = []
          try {
            if (tpl.formContent) {
              const content = JSON.parse(tpl.formContent)
              fields = content.fields || []
            }
            if (tpl.workflow) {
              const wf = JSON.parse(tpl.workflow)
              steps = wf.steps || []
            }
          } catch (e) {
            console.warn('解析模板内容失败', e)
          }
          return { ...tpl, robotGroupIds, robotGroupNames, _fields: fields, _steps: steps }
        })
        this.total = res.total || 0
      } catch (error) {
        this.$message.error('获取模板列表失败')
      } finally {
        this.loading = false
      }
    },
    handleQuery() {
      this.queryParams.pageNum = 1
      this.getList()
    },
    // 获取机器人组名称
    getRobotGroupName(groupId) {
      const group = this.robotGroupOptions.find(g => g.id === groupId)
      return group ? group.name : groupId
    },
    // 解析字段数量
    getFieldCount(row) {
      return row._fields ? row._fields.length : 0
    },
    // 解析步骤数量
    getStepCount(row) {
      return row._steps ? row._steps.length : 0
    },
    // 字段类型文本
    getFieldTypeText(type) {
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
    },
    // 重置表单
    resetForm() {
      this.form = {
        id: null,
        name: '',
        description: '',
        robotGroupIds: [...this.allGroupIds],
        fields: [],
        steps: []
      }
      if (this.$refs.formRef) {
        this.$refs.formRef.resetFields()
      }
    },
    // 新增模板
    handleAdd() {
      this.dialog.title = '新增模板'
      this.resetForm()
      this.dialog.visible = true
    },
    // 编辑模板 - 调用getTemplate API获取完整数据，不只读取表格
    async handleEdit(row) {
      // 严格校验 ID 是否存在且有效
      if (!row || !row.id) {
        this.$message.error('模板ID无效，无法编辑')
        return
      }

      const templateId = Number(row.id)
      if (isNaN(templateId) || templateId <= 0) {
        this.$message.error('模板ID格式错误，无法编辑')
        return
      }

      this.dialog.title = '修改模板'
      this.submitLoading = true

      try {
        // 调用API获取最新数据，而不是直接使用表格数据
        const res = await getTemplate(templateId)
        const data = res.data

        console.log('获取模板详情：', data)

        // 校验返回数据是否有有效ID
        if (!data || !data.id) {
          this.$message.error('获取模板详情失败：返回数据无效')
          return
        }

        // 解析表单字段
        let fields = []
        if (data.formContent) {
          try {
            const content = JSON.parse(data.formContent)
            fields = content.fields || []
          } catch (e) {
            console.warn('解析formContent失败', e)
          }
        }

        // 解析工作流步骤
        let steps = []
        if (data.workflow) {
          try {
            const wf = JSON.parse(data.workflow)
            steps = wf.steps || []
          } catch (e) {
            console.warn('解析workflow失败', e)
          }
        }

        // 设置表单数据 - 确保ID是数字
        this.form = {
          id: Number(data.id),
          name: data.name || '',
          description: data.description || '',
          robotGroupIds: this.ensureArray(data.robotGroupIds),
          fields: fields,
          steps: steps
        }

        console.log('编辑表单数据：', this.form)
        this.dialog.visible = true
      } catch (error) {
        this.$message.error('获取模板详情失败：' + (error.response?.data?.msg || error.message))
      } finally {
        this.submitLoading = false
      }
    },
    // 取消对话框
    cancel() {
      this.dialog.visible = false
      this.resetForm()
    },
    // 添加字段
    addField() {
      this.form.fields.push({
        id: '',
        label: '',
        type: 'text',
        required: false,
        maxCount: 1,
        maxSize: 10,
        accept: []
      })
    },
    removeField(index) {
      this.form.fields.splice(index, 1)
    },
    addStep() {
      this.form.steps.push({
        name: '',
        description: '',
        estimatedTime: 5
      })
    },
    removeStep(index) {
      this.form.steps.splice(index, 1)
    },
    // 提交表单
    submitForm() {
      this.$refs.formRef.validate(async (valid) => {
        if (!valid) return

        // 严格校验 ID（修改模式下）
        if (this.dialog.title === '修改模板') {
          const templateId = Number(this.form.id)
          if (!templateId || isNaN(templateId) || templateId <= 0) {
            this.$message.error('模板ID无效，无法保存')
            return
          }
        }

        this.submitLoading = true
        try {
          const dto = {
            name: this.form.name,
            description: this.form.description,
            robotGroupIds: this.form.robotGroupIds,
            formContent: JSON.stringify({ fields: this.form.fields }),
            workflow: JSON.stringify({ steps: this.form.steps })
          }

          console.log('提交表单：', this.form)
          console.log('提交DTO：', dto)

          if (this.dialog.title === '修改模板') {
            // 修改模板 - 确保ID是有效的数字
            const id = Number(this.form.id)
            console.log('调用updateTemplate，ID：', id)

            if (!id || isNaN(id)) {
              throw new Error('模板ID无效')
            }

            await updateTemplate(id, dto)
            this.$message.success('修改成功')
          } else {
            // 新增模板
            await addTemplate(dto)
            this.$message.success('新增成功')
          }
          this.dialog.visible = false
          this.getList()
        } catch (error) {
          console.error('提交失败：', error)
          this.$message.error(error.response?.data?.msg || error.message || '操作失败')
        } finally {
          this.submitLoading = false
        }
      })
    },
    // 查看详情
    async handleView(row) {
      try {
        const res = await getTemplate(row.id)
        this.viewDialog.data = res.data
        // 解析字段和步骤
        try {
          if (res.data.formContent) {
            const content = JSON.parse(res.data.formContent)
            this.viewDialog.fields = content.fields || []
          } else {
            this.viewDialog.fields = []
          }
          if (res.data.workflow) {
            const wf = JSON.parse(res.data.workflow)
            this.viewDialog.steps = wf.steps || []
          } else {
            this.viewDialog.steps = []
          }
        } catch (e) {
          this.viewDialog.fields = []
          this.viewDialog.steps = []
        }
        this.viewDialog.visible = true
      } catch (error) {
        this.$message.error('获取详情失败')
      }
    },
    // 删除
    handleDelete(row) {
      this.$confirm('确认删除该模板吗？', '警告', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        await delTemplate(row.id)
        this.$message.success('删除成功')
        this.getList()
      }).catch(() => {
      })
    },
    // 禁用
    handleBan(row) {
      this.$confirm('禁用后，基于此模板的任务将无法创建，确定禁用吗？', '警告', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        await banTemplate(row.id)
        this.$message.success('禁用成功')
        this.getList()
      }).catch(() => {
      })
    },
    // 恢复
    handleResume(row) {
      this.$confirm('确定恢复该模板吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'info'
      }).then(async () => {
        await resumeTemplate(row.id)
        this.$message.success('恢复成功')
        this.getList()
      }).catch(() => {
      })
    }
  }
}
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

/* 操作按钮样式优化 */
.el-button [class*="fas"] {
  font-size: 14px;
}
.el-table .cell .el-button + .el-button {
  margin-left: 4px;
}
.step-tip {
  font-size: 13px;
  color: #606266;
  line-height: 1.5;
  margin-bottom: 5px;
}
.step-tip code {
  background: #f4f4f5;
  padding: 1px 4px;
  border-radius: 3px;
  color: #409eff;
  font-family: monospace;
  font-weight: bold;
}
</style>
