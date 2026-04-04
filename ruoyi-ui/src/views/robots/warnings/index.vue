<template>
  <div class="app-container">
    <!-- 顶部状态切换 -->
    <el-row :gutter="10" class="mb8">
      <el-col :span="6">
        <el-radio-group v-model="statusType" @change="handleStatusChange" size="medium">
          <el-radio-button label="0">
            <i class="el-icon-tickets"></i> 未处理预警
          </el-radio-button>
          <el-radio-button label="1">
            <i class="el-icon-circle-check"></i> 已处理预警
          </el-radio-button>
        </el-radio-group>
      </el-col>
      <!-- 处理预警按钮：仅未处理状态显示 -->
      <el-col v-if="statusType === '0'" :span="1.5">
        <el-button
          type="success"
          plain
          icon="el-icon-check"
          size="small"
          :disabled="multiple"
          @click="handleDeal"
          v-hasPermi="['robots:warnings:deal']"
        >处理预警</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="warning"
          plain
          icon="el-icon-download"
          size="small"
          @click="handleExport"
          v-hasPermi="['robots:warnings:export']"
        >导出</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="warningsList" @selection-change="handleSelectionChange" style="width: 100%" stripe highlight-current-row>
      <el-table-column type="selection" width="50" align="center" />
      <el-table-column label="预警ID" align="center" prop="id" width="70" show-overflow-tooltip />
      <el-table-column label="机器人ID" align="center" prop="robotId" width="90" show-overflow-tooltip />
      <el-table-column label="预警类型" align="center" prop="warningType" width="110" show-overflow-tooltip>
        <template slot-scope="scope">
          <el-tag v-if="scope.row.warningType === '0'" type="warning" effect="dark">低电量</el-tag>
          <el-tag v-else-if="scope.row.warningType === '1'" type="danger" effect="dark">硬件故障</el-tag>
          <el-tag v-else-if="scope.row.warningType === '2'" type="danger" effect="light">硬件异常</el-tag>
          <el-tag v-else-if="scope.row.warningType === '3'" type="info" effect="dark">离线</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="预警描述" align="center" prop="warningContent" width="150" show-overflow-tooltip />
      <el-table-column label="预警级别" align="center" prop="warningLevel" width="100" show-overflow-tooltip>
        <template slot-scope="scope">
          <el-tag v-if="scope.row.warningLevel === '0'" type="success">提示</el-tag>
          <el-tag v-else-if="scope.row.warningLevel === '1'" type="warning">警告</el-tag>
          <el-tag v-else-if="scope.row.warningLevel === '2'" type="danger">错误</el-tag>
        </template>
      </el-table-column>
      <!-- 未处理状态不显示已处理相关字段 -->
      <el-table-column v-if="statusType === '1'" label="处理完成时间" align="center" prop="resolveTime" width="160" show-overflow-tooltip>
        <template slot-scope="scope">
          <i class="el-icon-time"></i>
          <span style="margin-left: 5px;">{{ parseTime(scope.row.resolveTime, '{y}-{m}-{d} {h}:{i}') }}</span>
        </template>
      </el-table-column>
      <el-table-column v-if="statusType === '1'" label="处理人" align="center" prop="resolveUser" width="100" show-overflow-tooltip />
      <el-table-column v-if="statusType === '1'" label="处理备注" align="center" prop="resolveNote" width="140" show-overflow-tooltip />
      <el-table-column label="预警创建时间" align="center" prop="createdAt" width="160" show-overflow-tooltip>
        <template slot-scope="scope">
          <i class="el-icon-time"></i>
          <span style="margin-left: 5px;">{{ parseTime(scope.row.createdAt, '{y}-{m}-{d} {h}:{i}') }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width" width="100">
        <template slot-scope="scope">
          <!-- 仅未处理状态显示处理按钮 -->
          <el-button
            v-if="statusType === '0'"
            size="mini"
            type="text"
            icon="el-icon-check"
            @click="handleDeal(scope.row)"
            v-hasPermi="['robots:warnings:deal']"
          >处理</el-button>
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

    <!-- 处理机器人预警对话框 -->
    <el-dialog :title="title" :visible.sync="open" width="550px" append-to-body @close="cancel">
      <el-form ref="form" :model="form" :rules="rules" label-width="100px" size="small">
        <el-row>
          <el-col :span="24">
            <el-form-item label="处理人" prop="resolveUser">
              <el-input v-model="form.resolveUser" placeholder="请输入处理人" clearable />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="24">
            <el-form-item label="处理备注" prop="resolveNote">
              <el-input v-model="form.resolveNote" placeholder="请输入处理备注" type="textarea" rows="4" clearable maxlength="500" show-word-limit />
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
// 移除 addWarnings 导入（新增功能相关）
import { listWarningsNo, listWarningsYes, getWarnings, dealWarnings } from "@/api/robots/warnings"

export default {
  name: "Warnings",
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
      // 显示搜索条件（保留但隐藏搜索栏，不影响右侧工具栏）
      showSearch: false,
      // 总条数
      total: 0,
      // 机器人状态预警表格数据
      warningsList: [],
      // 弹出层标题
      title: "",
      // 是否显示弹出层
      open: false,
      // 状态切换标识：0-未处理 1-已处理
      statusType: "0",
      // 查询参数
      queryParams: {
        pageNum: 1,
        pageSize: 10
      },
      // 表单参数（移除新增相关的默认值）
      form: {
        id: null,        // 预警ID
        resolveTime: null, // 处理时间（后端自动赋值，前端无需传）
        resolveUser: null, // 处理人
        resolveNote: null  // 处理备注
      },
      // 表单校验
      rules: {
        resolveUser: [
          { required: true, message: "处理人不能为空", trigger: "blur" }
        ],
        resolveNote: [
          { required: true, message: "处理备注不能为空", trigger: "blur" }
        ]
      }
    }
  },
  created() {
    this.getList()
  },
  methods: {
    /** 查询机器人状态预警列表 */
    getList() {
      this.loading = true
      // 根据状态调用不同接口
      const request = this.statusType === "0" ? listWarningsNo : listWarningsYes
      request(this.queryParams).then(response => {
        this.warningsList = response.rows
        this.total = response.total
        this.loading = false
      }).catch(() => {
        this.loading = false
      })
    },
    // 状态切换处理
    handleStatusChange() {
      this.queryParams.pageNum = 1 // 切换状态重置页码
      this.getList()
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
        resolveTime: null,
        resolveUser: null,
        resolveNote: null
      }
      this.resetForm("form")
    },
    // 多选框选中数据
    handleSelectionChange(selection) {
      this.ids = selection.map(item => item.id)
      this.single = selection.length !== 1
      this.multiple = !selection.length
    },
    /** 已删除 handleAdd 新增按钮点击事件 */
    /** 处理预警按钮操作（修复容错问题） */
    handleDeal(row) {
      this.reset()
      // 修复：用可选链避免row为undefined时报错
      const id = row?.id || this.ids[0]
      if (!id) {
        this.$modal.msgWarning("请选择需要处理的预警")
        return
      }
      // 回显预警ID（处理接口需要）
      this.form.id = id
      this.open = true
      this.title = "处理机器人状态预警"
    },
    /** 提交按钮（移除新增逻辑） */
    submitForm() {
      this.$refs["form"].validate(valid => {
        if (valid) {
          // 仅保留处理预警逻辑
          dealWarnings(this.form)
            .then(response => {
              this.$modal.msgSuccess("处理成功")
              this.open = false
              this.getList() // 刷新列表
            })
            // 报错捕获，方便排查
            .catch(error => {
              this.$modal.msgError("处理失败：" + (error.msg || "接口请求异常"))
              console.error("处理预警报错：", error)
            })
        }
      })
    },
    /** 删除按钮操作（后端接口注释，暂禁用） */
    handleDelete(row) {
      this.$modal.msgInfo("删除接口暂未开放")
    },
    /** 导出按钮操作 */
    handleExport() {
      this.download('robots/warnings/export', {
        ...this.queryParams
      }, `warnings_${new Date().getTime()}.xlsx`)
    }
  }
}
</script>