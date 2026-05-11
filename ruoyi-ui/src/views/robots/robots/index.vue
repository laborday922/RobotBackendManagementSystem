<template>
  <div class="app-container">

    <!-- 搜索栏：卡片化美化 + 逻辑整洁 -->
    <el-card class="search-panel" shadow="hover">
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
    </el-card>

    <!-- 工具栏 -->
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

    <!-- 表格卡片 -->
    <el-card class="table-card" shadow="never">
      <div slot="header" class="clearfix card-header">
        <span><i class="el-icon-robot"></i> 机器人管理列表</span>
      </div>
      <el-table v-loading="loading" :data="robotsList" @selection-change="handleSelectionChange" style="width: 100%" border stripe>
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
            <el-tag :type="getStatusTagType(scope.row.status)" effect="light">{{ getStatusText(scope.row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="硬件" align="center" prop="hardwareStatus" width="70">
          <template slot-scope="scope">
            <el-tag :type="getHardwareTagType(scope.row.hardwareStatus)" effect="light">{{ getHardwareStatusText(scope.row.hardwareStatus) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="任务" align="center" prop="taskStatus" width="70">
          <template slot-scope="scope">
            <el-tag :type="getTaskTagType(scope.row.taskStatus)" effect="light">{{ getTaskStatusText(scope.row.taskStatus) }}</el-tag>
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
    </el-card>

    <!-- 分页 -->
    <div class="pagination-box">
      <pagination
        v-show="total>0"
        :total="total"
        :page.sync="queryParams.pageNum"
        :limit.sync="queryParams.pageSize"
        @pagination="getList"
      />
    </div>

    <!-- 弹窗：统一美化 -->
    <el-dialog :title="title" :visible.sync="open" width="500px" append-to-body class="global-dialog">
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
              :value="String(group.id)"
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
      groupsList: [],
      groupsMap: {},
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
        code: [{ required: true, message: "机器人编号不能为空", trigger: "blur" }],
        name: [{ required: true, message: "机器人名称不能为空", trigger: "blur" }],
        groupId: [{ required: true, message: "所属分组不能为空", trigger: "change" }],
        manufacturer: [{ required: true, message: "生产厂家不能为空", trigger: "blur" }],
        productionDate: [{ required: true, message: "生产日期不能为空", trigger: "blur" }],
        area: [{ required: true, message: "所属区域不能为空", trigger: "blur" }]
      },
      groupsLoaded: false
    }
  },
  created() {
    this.getList()
    this.getGroupsList()
  },
  methods: {
    getList() {
      this.loading = true
      listRobots(this.queryParams).then(response => {
        this.robotsList = response.rows
        this.total = response.total
        this.loading = false
      })
    },
    getGroupsList() {
      if (this.groupsLoaded && this.groupsList.length > 0) {
        return Promise.resolve(this.groupsList)
      }
      return listGroups({}).then(response => {
        const groups = response.rows || response
        this.groupsList = groups
        this.groupsMap = groups.reduce((map, group) => {
          map[String(group.id)] = group.name
          return map
        }, {})
        this.groupsLoaded = true
        return groups
      }).catch(() => [])
    },
    getGroupName(groupId) {
      if (!groupId) return '未分组'
      return this.groupsMap[String(groupId)] || `未知分组(${groupId})`
    },
    getStatusText(status) { return this.statusMap[status] || '未知状态' },
    getHardwareStatusText(hardwareStatus) { return this.hardwareStatusMap[hardwareStatus] || '未知状态' },
    getTaskStatusText(taskStatus) { return this.taskStatusMap[taskStatus] || '未知状态' },
    getStatusTagType(status) {
      switch (String(status)) {
        case '0': return 'danger'
        case '1': return 'success'
        case '2': return 'info'
        default: return 'warning'
      }
    },
    getHardwareTagType(hardwareStatus) {
      switch (String(hardwareStatus)) {
        case '0': return 'success'
        case '1': return 'warning'
        case '2': return 'danger'
        default: return 'info'
      }
    },
    getTaskTagType(taskStatus) {
      switch (String(taskStatus)) {
        case '0': return 'primary'
        case '1': return 'warning'
        case '2': return 'info'
        case '3': return 'danger'
        default: return 'info'
      }
    },
    cancel() { this.open = false; this.reset() },
    reset() {
      this.form = {
        id: null, code: null, name: null, groupId: null, manufacturer: null,
        productionDate: null, area: null, status: null, hardwareStatus: null,
        taskStatus: null, battery: null
      }
      this.resetForm("form")
    },
    handleQuery() { this.queryParams.pageNum = 1; this.getList() },
    resetQuery() { this.resetForm("queryForm"); this.handleQuery() },
    handleSelectionChange(selection) {
      this.ids = selection.map(item => item.id)
      this.single = selection.length!==1
      this.multiple = !selection.length
    },
    handleAdd() {
      this.reset()
      this.getGroupsList().then(() => {
        this.open = true
        this.title = "添加机器人基础信息"
      })
    },
    handleUpdate(row) {
      this.reset()
      this.getGroupsList().then(() => {
        const id = row.id || this.ids
        getRobots(id).then(response => {
          const groupId = response.data.groupId === null || response.data.groupId === undefined
            ? null
            : String(response.data.groupId)
          this.form = { ...response.data, groupId }
          this.open = true
          this.title = "修改机器人基础信息"
        })
      })
    },
    submitForm() {
      this.$refs["form"].validate(valid => {
        if (valid) {
          const d = this.form
          const submitData = {
            id: d.id, code:d.code, name:d.name, groupId: Number(d.groupId)||null,
            manufacturer:d.manufacturer, productionDate:d.productionDate, area:d.area,
            status:d.status, hardwareStatus:d.hardwareStatus, taskStatus:d.taskStatus, battery:d.battery
          }
          if (this.form.id) {
            updateRobots(submitData).then(() => {
              this.$modal.msgSuccess("修改成功")
              this.open = false
              this.getList()
            })
          } else {
            addRobots(submitData).then(() => {
              this.$modal.msgSuccess("新增成功")
              this.open = false
              this.getList()
            })
          }
        }
      })
    },
    handleDelete(row) {
      const ids = row.id || this.ids
      this.$modal.confirm('是否确认删除所选机器人数据？').then(() => delRobots(ids))
        .then(() => { this.getList(); this.$modal.msgSuccess("删除成功") })
    },
    handleExport() {
      this.download('robots/robots/export', {...this.queryParams}, `robots_${new Date().getTime()}.xlsx`)
    },
    handleViewHistory(row) {
      this.$router.push({ path: "/robots/history", query: { robotId: row.id } });
    }
  }
}
</script>

<style lang="scss" scoped>
.app-container {
  padding: 20px;
  background-color: #f5f7fa;
  min-height: calc(100vh - 84px);
}

/* 搜索栏卡片美化 */
.search-panel {
  margin-bottom: 16px;
  border-radius: 8px;
  overflow: hidden;
}

/* 表格卡片 */
.table-card {
  border-radius: 8px;
  overflow: hidden;
  margin-bottom: 15px;

  ::v-deep .el-card__header {
    padding: 12px 20px;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    color: #fff;
    font-weight: 500;
  }

  ::v-deep .el-card__body {
    padding: 0;
  }
}

/* 分页 */
.pagination-box {
  text-align: right;
  margin-top: 10px;
}

/* 表格样式 */
::v-deep .el-table {
  border: none;
  --el-table-row-hover-bg-color: #ecf5ff;
}
::v-deep .el-table th {
  background-color: #f5f7fa !important;
  font-weight: 600;
}

/* 全局弹窗美化：所有弹窗统一渐变头部 */
::v-deep .global-dialog .el-dialog__header {
  background: linear-gradient(135deg, #409eff 0%, #66b1ff 100%);
  color: #fff;
  border-top-left-radius: 8px;
  border-top-right-radius: 8px;
}
::v-deep .global-dialog .el-dialog__title {
  color: #fff;
  font-weight: 500;
}
::v-deep .global-dialog .el-dialog__close {
  color: #fff !important;
}
::v-deep .global-dialog .el-dialog {
  border-radius: 8px;
  overflow: hidden;
}
</style>
