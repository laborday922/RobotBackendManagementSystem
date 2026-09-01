<template>
  <div class="app-container">
    <el-card class="search-card" shadow="never">
      <el-form :model="queryParams" ref="queryForm" :inline="true" v-show="showSearch" label-width="80px">
        <el-form-item label="分组名称" prop="name">
          <el-input
            v-model="queryParams.name"
            placeholder="请输入分组名称"
            clearable
            style="width: 220px"
            @keyup.enter.native="handleQuery"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">搜索</el-button>
          <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card class="table-card" shadow="never">
      <template #header>
        <div class="card-header">
          <span>机器人分组列表</span>
          <div class="card-actions">
            <el-button
              type="primary"
              icon="el-icon-plus"
              size="mini"
              @click="handleAdd"
              v-hasPermi="['robots:groups:add']"
            >新增</el-button>
            <el-button
              type="success"
              icon="el-icon-edit"
              size="mini"
              :disabled="single"
              @click="handleUpdate"
              v-hasPermi="['robots:groups:edit']"
            >修改</el-button>
            <el-button
              type="danger"
              icon="el-icon-delete"
              size="mini"
              :disabled="multiple"
              @click="handleDelete"
              v-hasPermi="['robots:groups:remove']"
            >删除</el-button>
            <el-button
              type="warning"
              icon="el-icon-download"
              size="mini"
              @click="handleExport"
              v-hasPermi="['robots:groups:export']"
            >导出</el-button>
            <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
          </div>
        </div>
      </template>

      <el-table v-loading="loading" :data="groupsList" @selection-change="handleSelectionChange" border style="width: 100%">
        <el-table-column type="selection" width="50" align="center" />
        <el-table-column label="分组ID" align="center" prop="id" width="80" show-overflow-tooltip />
        <el-table-column label="分组名称" align="center" prop="name" min-width="160" show-overflow-tooltip />
        <el-table-column label="分组描述" align="center" prop="description" min-width="220" show-overflow-tooltip />
        <el-table-column label="创建时间" align="center" prop="createdAt" width="180" show-overflow-tooltip>
          <template slot-scope="scope">
            <span>{{ parseTime(scope.row.createdAt, '{y}-{m}-{d} {h}:{i}') }}</span>
          </template>
        </el-table-column>
        <el-table-column label="更新时间" align="center" prop="updatedAt" width="180" show-overflow-tooltip>
          <template slot-scope="scope">
            <span>{{ parseTime(scope.row.updatedAt, '{y}-{m}-{d} {h}:{i}') }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" align="center" class-name="small-padding fixed-width" width="140">
          <template slot-scope="scope">
            <el-button
              size="mini"
              type="text"
              icon="el-icon-edit"
              @click="handleUpdate(scope.row)"
              v-hasPermi="['robots:groups:edit']"
            >修改</el-button>
            <el-button
              size="mini"
              type="text"
              icon="el-icon-delete"
              @click="handleDelete(scope.row)"
              v-hasPermi="['robots:groups:remove']"
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
    </el-card>

    <el-dialog :title="title" :visible.sync="open" width="550px" append-to-body @close="cancel">
      <el-form ref="form" :model="form" :rules="rules" label-width="100px" size="small">
        <el-row>
          <el-col :span="24">
            <el-form-item label="分组名称" prop="name">
              <el-input v-model="form.name" placeholder="请输入分组名称" clearable maxlength="50" show-word-limit />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="24">
            <el-form-item label="分组描述" prop="description">
              <el-input v-model="form.description" placeholder="请输入分组描述" type="textarea" rows="3" clearable maxlength="200" show-word-limit />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="cancel">取 消</el-button>
        <el-button type="primary" @click="submitForm">确 定</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { listGroups, getGroups, delGroups, addGroups, updateGroups } from "@/api/robots/groups"

export default {
  name: "Groups",
  data() {
    return {
      // 遮罩层
      loading: true,
      // 选中数组
      ids: [],
      // 非单个禁用
      single: true,
      // 非多个禁用
      multiple: true,
      // 显示搜索条件
      showSearch: true,
      // 总条数
      total: 0,
      // 机器人分组表格数据
      groupsList: [],
      // 弹出层标题
      title: "",
      // 是否显示弹出层
      open: false,
      // 查询参数
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        name: null,
      },
      // 表单参数
      form: {},
      // 表单校验
      rules: {
        name: [
          { required: true, message: "分组名称不能为空", trigger: "blur" }
        ],
        createdAt: [
          { required: true, message: "创建时间不能为空", trigger: "blur" }
        ],
        updatedAt: [
          { required: true, message: "更新时间不能为空", trigger: "blur" }
        ]
      }
    }
  },
  created() {
    this.getList()
  },
  methods: {
    /** 查询机器人分组列表 */
    getList() {
      this.loading = true
      listGroups(this.queryParams).then(response => {
        this.groupsList = response.rows
        this.total = response.total
        this.loading = false
      })
    },
    // 取消按钮 - 快速关闭对话框
    cancel() {
      this.open = false
      this.$nextTick(() => {
        this.reset()
      })
    },
    // 表单重置
    reset() {
      this.form = {
        id: null,
        name: null,
        description: null,
        createdAt: null,
        updatedAt: null
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
      this.open = true
      this.title = "添加机器人分组"
    },
    /** 修改按钮操作 */
    handleUpdate(row) {
      this.reset()
      const id = row.id || this.ids
      getGroups(id).then(response => {
        this.form = response.data
        this.open = true
        this.title = "修改机器人分组"
      })
    },
    /** 提交按钮 */
    submitForm() {
      this.$refs["form"].validate(valid => {
        if (valid) {
          if (this.form.id != null) {
            updateGroups(this.form).then(response => {
              this.$modal.msgSuccess("修改成功")
              this.open = false
              this.getList()
            })
          } else {
            addGroups(this.form).then(response => {
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
      this.$modal.confirm('是否确认删除机器人分组编号为"' + ids + '"的数据项？').then(function() {
        return delGroups(ids)
      }).then(() => {
        this.getList()
        this.$modal.msgSuccess("删除成功")
      }).catch(() => {})
    },
    /** 导出按钮操作 */
    handleExport() {
      this.download('robots/groups/export', {
        ...this.queryParams
      }, `groups_${new Date().getTime()}.xlsx`)
    }
  }
}
</script>

<style lang="scss" scoped>
.app-container { padding: 20px; }
.search-card { margin-bottom: 20px; }
.table-card { margin-bottom: 20px; }
.card-header { display: flex; justify-content: space-between; align-items: center; }
.card-actions { display: flex; align-items: center; gap: 8px; flex-wrap: wrap; }
.el-table .cell .el-button + .el-button { margin-left: 6px; }

@media (max-width: 768px) {
  .app-container { padding: 10px; }
}
</style>
