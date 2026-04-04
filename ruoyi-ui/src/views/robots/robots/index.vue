<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="在线状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="请选择在线状态" clearable>
          <el-option label="离线" value="0"></el-option>
          <el-option label="在线" value="1"></el-option>
          <el-option label="待激活" value="2"></el-option>
        </el-select>
      </el-form-item>
      <el-form-item label="所属分组" prop="groupId">
        <el-select v-model="queryParams.groupId" placeholder="请选择所属分组" clearable>
          <el-option
            v-for="group in groupsList"
            :key="group.id"
            :label="group.name"
            :value="group.id"
          ></el-option>
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button
          type="primary"
          plain
          icon="el-icon-plus"
          size="mini"
          @click="handleAdd"
          v-hasPermi="['robots:robots:add']"
        >新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="success"
          plain
          icon="el-icon-edit"
          size="mini"
          :disabled="single"
          @click="handleUpdate"
          v-hasPermi="['robots:robots:edit']"
        >修改</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="danger"
          plain
          icon="el-icon-delete"
          size="mini"
          :disabled="multiple"
          @click="handleDelete"
          v-hasPermi="['robots:robots:remove']"
        >删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="warning"
          plain
          icon="el-icon-download"
          size="mini"
          @click="handleExport"
          v-hasPermi="['robots:robots:export']"
        >导出</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="robotsList" @selection-change="handleSelectionChange" style="width: 100%">
      <el-table-column type="selection" width="50" align="center" />
      <el-table-column label="ID" align="center" prop="id" width="60" show-overflow-tooltip />
      <el-table-column label="编号" align="center" prop="code" width="100" show-overflow-tooltip />
      <el-table-column label="名称" align="center" prop="name" width="120" show-overflow-tooltip />
      <el-table-column label="分组" align="center" prop="groupId" width="100" show-overflow-tooltip>
        <template slot-scope="scope">
          <span>{{ getGroupName(scope.row.groupId) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="厂家" align="center" prop="manufacturer" width="100" show-overflow-tooltip />
      <el-table-column label="生产日期" align="center" prop="productionDate" width="110" show-overflow-tooltip>
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.productionDate, '{y}-{m}-{d}') }}</span>
        </template>
      </el-table-column>
      <el-table-column label="区域" align="center" prop="area" width="100" show-overflow-tooltip />
      <el-table-column label="在线" align="center" prop="status" width="70">
        <template slot-scope="scope">
          <el-tag :type="getStatusTagType(scope.row.status)">{{ getStatusText(scope.row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="硬件" align="center" prop="hardwareStatus" width="70">
        <template slot-scope="scope">
          <el-tag :type="getHardwareTagType(scope.row.hardwareStatus)">{{ getHardwareStatusText(scope.row.hardwareStatus) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="任务" align="center" prop="taskStatus" width="70">
        <template slot-scope="scope">
          <el-tag :type="getTaskTagType(scope.row.taskStatus)">{{ getTaskStatusText(scope.row.taskStatus) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="电量" align="center" prop="battery" width="60" />
      <el-table-column label="创建时间" align="center" prop="createdAt" width="110" show-overflow-tooltip>
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.createdAt, '{y}-{m}-{d}') }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width" width="150">
        <template slot-scope="scope">
          <el-button
            size="mini"
            type="text"
            icon="el-icon-view"
            @click="handleViewHistory(scope.row)"
          >查看</el-button>
          <el-button
            size="mini"
            type="text"
            icon="el-icon-edit"
            @click="handleUpdate(scope.row)"
            v-hasPermi="['robots:robots:edit']"
          >修改</el-button>
          <el-button
            size="mini"
            type="text"
            icon="el-icon-delete"
            @click="handleDelete(scope.row)"
            v-hasPermi="['robots:robots:remove']"
          >删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    
    <pagination
      v-show="total>0"
      :total="total"
      :page.sync="queryParams.pageNum"
      :limit.sync="queryParams.pageSize"
      @pagination="getList"
    />

    <el-dialog :title="title" :visible.sync="open" width="500px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="机器人编号" prop="code">
          <el-input v-model="form.code" placeholder="请输入机器人编号" />
        </el-form-item>
        <el-form-item label="机器人名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入机器人名称" />
        </el-form-item>
        <el-form-item label="所属分组" prop="groupId">
          <el-select v-model="form.groupId" placeholder="请选择所属分组" clearable>
            <el-option
              v-for="group in groupsList"
              :key="group.id"
              :label="group.name"
              :value="group.id"
            ></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="生产厂家" prop="manufacturer">
          <el-input v-model="form.manufacturer" placeholder="请输入生产厂家" />
        </el-form-item>
        <el-form-item label="生产日期" prop="productionDate">
          <el-date-picker clearable
            v-model="form.productionDate"
            type="date"
            value-format="yyyy-MM-dd"
            placeholder="请选择生产日期">
          </el-date-picker>
        </el-form-item>
        <el-form-item label="所属区域" prop="area">
          <el-input v-model="form.area" placeholder="请输入所属区域" />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitForm">确 定</el-button>
        <el-button @click="cancel">取 消</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { listRobots, getRobots, delRobots, addRobots, updateRobots } from "@/api/robots/robots"
import { listGroups } from "@/api/robots/groups"

export default {
  name: "Robots",
  data() {
    return {
      loading: true,
      ids: [],
      single: true,
      multiple: true,
      showSearch: true,
      total: 0,
      robotsList: [],
      groupsList: [], // 分组列表缓存
      groupsMap: {}, // 分组ID-名称映射表
      title: "",
      open: false,
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        groupId: null,
        status: null,
        hardwareStatus: null,
        taskStatus: null,
      },
      form: {},
      statusMap: {
        '0': '离线',
        '1': '在线',
        '2': '待激活'
      },
      hardwareStatusMap: {
        '0': '正常',
        '1': '警告',
        '2': '故障'
      },
      taskStatusMap: {
        '0': '执行中',
        '1': '充电中',
        '2': '闲置',
        '3': '维护'
      },
      rules: {
        code: [
          { required: true, message: "机器人编号不能为空", trigger: "blur" }
        ],
        name: [
          { required: true, message: "机器人名称不能为空", trigger: "blur" }
        ],
        groupId: [
          { required: true, message: "所属分组不能为空", trigger: "change" }
        ],
        manufacturer: [
          { required: true, message: "生产厂家不能为空", trigger: "blur" }
        ],
        productionDate: [
          { required: true, message: "生产日期不能为空", trigger: "blur" }
        ],
        area: [
          { required: true, message: "所属区域不能为空", trigger: "blur" }
        ]
      },
      groupsLoaded: false // 分组数据加载标识
    }
  },
  created() {
    this.getList()
    this.getGroupsList() // 初始化加载分组列表
  },
  methods: {
    /** 查询机器人基础信息列表 */
    getList() {
      this.loading = true
      listRobots(this.queryParams).then(response => {
        this.robotsList = response.rows
        this.total = response.total
        this.loading = false
      })
    },

    /** 查询分组列表（带缓存） */
    getGroupsList() {
      // 如果已经加载过分组数据，直接返回缓存
      if (this.groupsLoaded && this.groupsList.length > 0) {
        return Promise.resolve(this.groupsList)
      }
      
      return listGroups({}).then(response => {
        const groups = response.rows || response
        this.groupsList = groups
        // 构建分组ID-名称映射表，优化查询性能
        this.groupsMap = groups.reduce((map, group) => {
          // 确保ID类型统一（避免数字/字符串类型不匹配导致的回显问题）
          map[String(group.id)] = group.name
          return map
        }, {})
        this.groupsLoaded = true // 标记为已加载
        return groups
      }).catch(error => {
        console.error('获取分组列表失败:', error)
        return []
      })
    },

    /** 根据分组ID获取分组名称 */
    getGroupName(groupId) {
      if (!groupId) return '未分组'
      // 统一转换为字符串，避免类型不匹配问题
      return this.groupsMap[String(groupId)] || `未知分组(${groupId})`
    },

    /** 获取在线状态文本 */
    getStatusText(status) {
      return this.statusMap[status] || '未知状态'
    },

    /** 获取硬件状态文本 */
    getHardwareStatusText(hardwareStatus) {
      return this.hardwareStatusMap[hardwareStatus] || '未知状态'
    },

    /** 获取任务状态文本 */
    getTaskStatusText(taskStatus) {
      return this.taskStatusMap[taskStatus] || '未知状态'
    },

    /** 获取在线状态标签类型 */
    getStatusTagType(status) {
      // 0: 离线(红), 1: 在线(绿), 2: 待激活(蓝)
      switch (String(status)) {
        case '0':
          return 'danger'
        case '1':
          return 'success'
        case '2':
          return 'info'
        default:
          return 'warning'
      }
    },

    /** 获取硬件状态标签类型 */
    getHardwareTagType(hardwareStatus) {
      // 0: 正常(绿), 1: 警告(黄), 2: 故障(红)
      switch (String(hardwareStatus)) {
        case '0':
          return 'success'
        case '1':
          return 'warning'
        case '2':
          return 'danger'
        default:
          return 'info'
      }
    },

    /** 获取任务状态标签类型 */
    getTaskTagType(taskStatus) {
      // 0: 执行中(蓝), 1: 充电中(橙), 2: 闲置(灰), 3: 维护(红)
      switch (String(taskStatus)) {
        case '0':
          return 'primary'
        case '1':
          return 'warning'
        case '2':
          return 'info'
        case '3':
          return 'danger'
        default:
          return 'info'
      }
    },

    // 取消按钮
    cancel() {
      this.open = false
      this.reset()
    },

    // 表单重置
    reset() {
      this.form = {
        id: null,
        code: null,
        name: null,
        groupId: null,
        manufacturer: null,
        productionDate: null,
        area: null,
        status: null,
        hardwareStatus: null,
        taskStatus: null,
        battery: null
      }
      this.resetForm("form")
    },

    /** 搜索按钮操作 */
    handleQuery() {
      this.queryParams.pageNum = 1
      this.getList()
    },

    /** 重置按钮操作 */
    resetQuery() {
      this.resetForm("queryForm")
      this.handleQuery()
    },

    // 多选框选中数据
    handleSelectionChange(selection) {
      this.ids = selection.map(item => item.id)
      this.single = selection.length!==1
      this.multiple = !selection.length
    },

    /** 新增按钮操作 */
    handleAdd() {
      this.reset()
      this.getGroupsList().then(() => {
        this.open = true
        this.title = "添加机器人基础信息"
      })
    },

    /** 修改按钮操作 - 核心修复 */
    handleUpdate(row) {
      this.reset()
      // 先加载分组列表（使用缓存），确保下拉框有数据后再回显
      this.getGroupsList().then(() => {
        const id = row.id || this.ids
        getRobots(id).then(response => {
          // 确保groupId的类型与下拉框的value类型一致（统一为字符串）
          this.form = {
            ...response.data,
            groupId: response.data.groupId ? String(response.data.groupId) : null
          }
          this.open = true
          this.title = "修改机器人基础信息"
        })
      })
    },

    /** 提交按钮 */
    submitForm() {
      this.$refs["form"].validate(valid => {
        if (valid) {
          const submitData = {
            id: this.form.id,
            code: this.form.code,
            name: this.form.name,
            // 提交时可根据后端需求转换类型（如果需要数字类型）
            groupId: this.form.groupId ? (isNaN(Number(this.form.groupId)) ? this.form.groupId : Number(this.form.groupId)) : null,
            manufacturer: this.form.manufacturer,
            productionDate: this.form.productionDate,
            area: this.form.area,
            status: this.form.status,
            hardwareStatus: this.form.hardwareStatus,
            taskStatus: this.form.taskStatus,
            battery: this.form.battery
          }
          
          if (this.form.id != null) {
            updateRobots(submitData).then(response => {
              this.$modal.msgSuccess("修改成功")
              this.open = false
              this.getList()
            })
          } else {
            addRobots(submitData).then(response => {
              this.$modal.msgSuccess("新增成功")
              this.open = false
              this.getList()
            })
          }
        }
      })
    },

    /** 删除按钮操作 */
    handleDelete(row) {
      const ids = row.id || this.ids
      this.$modal.confirm('是否确认删除机器人基础信息编号为"' + ids + '"的数据项？').then(function() {
        return delRobots(ids)
      }).then(() => {
        this.getList()
        this.$modal.msgSuccess("删除成功")
      }).catch(() => {})
    },

    /** 导出按钮操作 */
    handleExport() {
      this.download('robots/robots/export', {
        ...this.queryParams
      }, `robots_${new Date().getTime()}.xlsx`)
    },

    /** 查看运行历史 */
    handleViewHistory(row) {
      this.$router.push({
        name: 'RobotHistory',
        query: { robotId: row.id }
      })
    }
  }
}
</script>