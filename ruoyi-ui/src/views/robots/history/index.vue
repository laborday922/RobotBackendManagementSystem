<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="80px" class="demo-form-inline">
      <el-form-item label="机器人ID" prop="robotId">
        <el-input
          v-model="queryParams.robotId"
          placeholder="请输入机器人ID"
          clearable
          size="small"
          prefix-icon="el-icon-search"
          style="width: 200px"
        />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" size="small" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" size="small" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button
          type="primary"
          plain
          icon="el-icon-plus"
          size="small"
          @click="handleAdd"
          v-hasPermi="['robots:history:add']"
        >新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="warning"
          plain
          icon="el-icon-download"
          size="small"
          @click="handleExport"
          v-hasPermi="['robots:history:export']"
        >导出</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="historyList" @selection-change="handleSelectionChange" style="width: 100%" stripe highlight-current-row>
      <el-table-column type="selection" width="50" align="center" />
      <el-table-column label="机器人ID" align="center" prop="robotId" width="80" show-overflow-tooltip />
      <el-table-column label="记录时间" align="center" prop="recordTime" width="160" show-overflow-tooltip>
        <template slot-scope="scope">
          <i class="el-icon-time"></i>
          <span style="margin-left: 5px;">{{ parseTime(scope.row.recordTime, '{y}-{m}-{d} {h}:{i}') }}</span>
        </template>
      </el-table-column>
      <el-table-column label="位置区域" align="center" prop="locationArea" width="110" show-overflow-tooltip />
      <el-table-column label="具体位置" align="center" prop="specificLocation" width="140" show-overflow-tooltip />
      <el-table-column label="坐标X" align="center" prop="coordinateX" width="90" show-overflow-tooltip />
      <el-table-column label="坐标Y" align="center" prop="coordinateY" width="90" show-overflow-tooltip />
      <el-table-column label="移动速度" align="center" prop="moveSpeed" width="90" show-overflow-tooltip>
        <template slot-scope="scope">
          <el-tag type="info" size="mini">{{ scope.row.moveSpeed }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="状态描述" align="center" prop="statusDesc" width="120" show-overflow-tooltip />
    </el-table>
    
    <pagination
      v-show="total>0"
      :total="total"
      :page.sync="queryParams.pageNum"
      :limit.sync="queryParams.pageSize"
      @pagination="getList"
    />

    <!-- 添加机器人位置历史信息对话框 -->
    <el-dialog :title="title" :visible.sync="open" width="600px" append-to-body @close="cancel">
      <el-form ref="form" :model="form" :rules="rules" label-width="100px" size="small">
        <el-row>
          <el-col :span="24">
            <el-form-item label="机器人ID" prop="robotId">
              <el-input v-model="form.robotId" placeholder="请输入机器人ID" clearable />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="24">
            <el-form-item label="记录时间" prop="recordTime">
              <el-date-picker 
                clearable
                v-model="form.recordTime"
                type="datetime"
                value-format="yyyy-MM-dd HH:mm:ss"
                placeholder="请选择记录时间"
                style="width: 100%">
              </el-date-picker>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="位置区域" prop="locationArea">
              <el-input v-model="form.locationArea" placeholder="请输入位置区域" clearable />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="具体位置" prop="specificLocation">
              <el-input v-model="form.specificLocation" placeholder="请输入具体位置" clearable />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="坐标X" prop="coordinateX">
              <el-input v-model="form.coordinateX" placeholder="X坐标" type="number" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="坐标Y" prop="coordinateY">
              <el-input v-model="form.coordinateY" placeholder="Y坐标" type="number" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="移动速度" prop="moveSpeed">
              <el-input v-model="form.moveSpeed" placeholder="速度(单位)" type="number" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="24">
            <el-form-item label="状态描述" prop="statusDesc">
              <el-input v-model="form.statusDesc" placeholder="请输入状态描述" type="textarea" rows="3" clearable />
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
import { listHistory, getHistory, delHistory, addHistory, updateHistory } from "@/api/robots/history"

export default {
  name: "History",
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
      // 机器人位置历史信息表格数据
      historyList: [],
      // 弹出层标题
      title: "",
      // 是否显示弹出层
      open: false,
      // 查询参数
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        robotId: null, // 机器人ID查询条件
      },
      // 表单参数
      form: {},
      // 表单校验
      rules: {
        robotId: [
          { required: true, message: "机器人ID不能为空", trigger: "blur" },
          { type: 'number', message: '机器人ID必须为数字', trigger: 'blur' } // 新增数字校验
        ],
        recordTime: [
          { required: true, message: "记录时间不能为空", trigger: "change" } // 修改trigger为change，适配日期选择器
        ],
        locationArea: [
          { required: true, message: "位置区域不能为空", trigger: "blur" }
        ],
        specificLocation: [
          { required: true, message: "具体位置不能为空", trigger: "blur" }
        ],
        coordinateX: [
          { required: true, message: "坐标X不能为空", trigger: "blur" },
          { type: 'number', message: '坐标X必须为数字', trigger: 'blur' }
        ],
        coordinateY: [
          { required: true, message: "坐标Y不能为空", trigger: "blur" },
          { type: 'number', message: '坐标Y必须为数字', trigger: 'blur' }
        ],
        moveSpeed: [
          { required: true, message: "移动速度不能为空", trigger: "blur" },
          { type: 'number', message: '移动速度必须为数字', trigger: 'blur' }
        ],
        statusDesc: [
          { required: true, message: "状态描述不能为空", trigger: "blur" }
        ]
      }
    }
  },
  created() {
    // 检查路由查询参数中是否有robotId，如果有则自动搜索
    if (this.$route.query.robotId) {
      this.queryParams.robotId = this.$route.query.robotId
      this.queryParams.pageNum = 1
    }
    this.getList()
  },
  methods: {
    /** 查询机器人位置历史信息列表 */
    getList() {
      this.loading = true
      // 处理查询参数：如果robotId为空，传递null；否则转为数字类型
      const params = {
        ...this.queryParams,
        robotId: this.queryParams.robotId ? Number(this.queryParams.robotId) : null
      }
      listHistory(params).then(response => {
        this.historyList = response.rows
        this.total = response.total
        this.loading = false
      }).catch(error => {
        console.error('查询位置历史失败:', error)
        this.loading = false
      })
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
        robotId: null,
        recordTime: null,
        locationArea: null,
        specificLocation: null,
        coordinateX: null,
        coordinateY: null,
        moveSpeed: null,
        statusDesc: null
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
      // 手动重置robotId查询参数
      this.queryParams.robotId = null
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
      this.title = "添加机器人位置历史信息"
    },
    /** 修改按钮操作 */
    handleUpdate(row) {
      this.reset()
      const id = row.id || this.ids
      getHistory(id).then(response => {
        this.form = response.data
        // 确保时间格式正确回显
        if (this.form.recordTime) {
          this.form.recordTime = this.form.recordTime
        }
        this.open = true
        this.title = "修改机器人位置历史信息"
      }).catch(error => {
        console.error('获取位置历史详情失败:', error)
        this.$modal.msgError("获取数据失败")
      })
    },
    /** 提交按钮 */
    submitForm() {
      this.$refs["form"].validate(valid => {
        if (valid) {
          // 处理表单数据类型转换
          const submitData = {
            ...this.form,
            robotId: this.form.robotId ? Number(this.form.robotId) : null,
            coordinateX: this.form.coordinateX ? Number(this.form.coordinateX) : null,
            coordinateY: this.form.coordinateY ? Number(this.form.coordinateY) : null,
            moveSpeed: this.form.moveSpeed ? Number(this.form.moveSpeed) : null
          }
          
          if (this.form.id != null) {
            updateHistory(submitData).then(response => {
              this.$modal.msgSuccess("修改成功")
              this.open = false
              this.getList()
            }).catch(error => {
              console.error('修改位置历史失败:', error)
              this.$modal.msgError("修改失败")
            })
          } else {
            addHistory(submitData).then(response => {
              this.$modal.msgSuccess("新增成功")
              this.open = false
              this.getList()
            }).catch(error => {
              console.error('新增位置历史失败:', error)
              this.$modal.msgError("新增失败")
            })
          }
        }
      })
    },
    /** 删除按钮操作 */
    handleDelete(row) {
      const ids = row.id || this.ids
      this.$modal.confirm('是否确认删除机器人位置历史信息编号为"' + ids + '"的数据项？').then(() => {
        return delHistory(ids)
      }).then(() => {
        this.getList()
        this.$modal.msgSuccess("删除成功")
      }).catch(() => {})
    },
    /** 导出按钮操作 */
    handleExport() {
      // 处理导出参数类型
      const exportParams = {
        ...this.queryParams,
        robotId: this.queryParams.robotId ? Number(this.queryParams.robotId) : null
      }
      this.download('robots/history/export', exportParams, `history_${new Date().getTime()}.xlsx`)
    }
  }
}
</script>