<template>
  <div class="card">
    <div class="card-header">
      <div class="card-title">
        <i class="fas fa-edit"></i> 编辑模式
      </div>
      <div>
        <el-button type="primary" size="small" @click="handleAdd" v-hasPermi="['system:mode:add']">
          <i class="fas fa-plus"></i> 新增
        </el-button>
      </div>
    </div>

    <div class="card-body">
      <!-- 搜索栏 -->
      <div class="filter-bar">
        <el-input
          v-model="queryParams.modeName"
          placeholder="搜索模式名称"
          style="width:200px;"
          size="small"
          clearable
          @keyup.enter.native="handleQuery"
        >
          <template #prefix><i class="fas fa-search"></i></template>
        </el-input>
        <el-select v-model="queryParams.modeType" placeholder="模式类型" clearable size="small" style="width:150px;">
          <el-option label="系统模式" value="system" />
          <el-option label="自定义模式" value="custom" />
        </el-select>
        <el-select v-model="queryParams.enabled" placeholder="状态" clearable size="small" style="width:150px;">
          <el-option label="已启用" value="1" />
          <el-option label="已禁用" value="0" />
        </el-select>
        <el-button type="primary" size="small" @click="handleQuery">
          <i class="fas fa-search"></i> 查询
        </el-button>
        <el-button size="small" @click="resetQuery">
          <i class="fas fa-undo"></i> 重置
        </el-button>
        <div style="margin-left: auto; display: flex; gap: 8px;">
          <el-button size="small" @click="handleSort">
            <i class="fas fa-sort-amount-down"></i> 排序
          </el-button>
          <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
        </div>
      </div>

      <!-- 模式卡片列表 -->
      <div class="mode-grid">
        <div class="mode-cards-container">
          <div
            v-for="mode in modeList"
            :key="mode.modeId"
            class="mode-card-item"
          >
            <el-card :body-style="{ padding: '0px' }" shadow="hover" class="mode-card" :style="{ borderLeft: `4px solid ${mode.modeColor}` }">
              <div class="mode-card-header">
                <div class="mode-icon" :style="{ backgroundColor: mode.modeColor }">
                  <i :class="mode.modeIcon"></i>
                </div>
                <div class="mode-info">
                  <div class="mode-name">{{ mode.modeName }}</div>
                  <div class="mode-tags">
                    <el-tag size="mini" type="info">{{ getCategoryName(mode.categoryId) }}</el-tag>
                    <span class="mode-type">{{ mode.modeType === 'system' ? '系统内置' : '用户自定义' }}</span>
                  </div>
                </div>
              </div>

              <div class="mode-card-body">
                <div class="mode-description">
                  {{ mode.description || '暂无描述' }}
                </div>

                <div class="mode-stats">
                  <div class="stat-item">
                    <div class="stat-value">{{ mode.usageCount || 0 }}</div>
                    <div class="stat-label">使用次数</div>
                  </div>
                  <div class="stat-item">
                    <div class="stat-value">{{ mode.robotCount || 0 }}</div>
                    <div class="stat-label">关联机器人</div>
                  </div>
                  <div class="stat-item">
                    <div class="stat-value">{{ mode.modeParams ? mode.modeParams.length : 0 }}</div>
                    <div class="stat-label">配置参数</div>
                  </div>
                </div>

                <div class="mode-params" v-if="mode.modeParams && mode.modeParams.length > 0">
                  <div class="mode-params-title">
                    <i class="fas fa-cog"></i> 主要参数：
                  </div>
                  <div class="mode-params-list">
                    <el-tag size="mini" v-for="param in mode.modeParams.slice(0, 3)" :key="param.paramId" class="param-tag">
                      {{ param.paramName }}
                    </el-tag>
                    <el-tag size="mini" v-if="mode.modeParams.length > 3" class="param-tag more-tag">
                      +{{ mode.modeParams.length - 3 }}
                    </el-tag>
                  </div>
                </div>
              </div>

              <div class="mode-card-footer">
                <div class="mode-status">
                  <span class="mode-status-dot" :class="mode.enabled === '1' ? 'status-active' : 'status-inactive'"></span>
                  <span>{{ mode.enabled === '1' ? '已启用' : '已禁用' }}</span>
                </div>
                <div class="mode-actions">
                  <el-button size="mini" type="text" @click="handleUpdate(mode)" v-hasPermi="['system:mode:edit']">
                    <i class="fas fa-edit"></i> 编辑
                  </el-button>
                  <el-button
                    size="mini"
                    :type="mode.enabled === '1' ? 'warning' : 'success'"
                    @click="handleChangeStatus(mode)"
                    v-hasPermi="['system:mode:edit']"
                  >
                    <i :class="mode.enabled === '1' ? 'fas fa-ban' : 'fas fa-check'"></i>
                    {{ mode.enabled === '1' ? '禁用' : '启用' }}
                  </el-button>
                  <el-button
                    size="mini"
                    type="danger"
                    @click="handleDelete(mode)"
                    v-if="mode.modeType !== 'system'"
                    v-hasPermi="['system:mode:remove']"
                  >
                    <i class="fas fa-trash"></i> 删除
                  </el-button>
                </div>
              </div>
            </el-card>
          </div>
        </div>

        <!-- 空状态 -->
        <el-empty v-if="modeList.length === 0" description="暂无模式数据"></el-empty>
      </div>

      <!-- 分页 -->
      <div class="pagination-wrap">
        <pagination
          v-show="total>0"
          :total="total"
          :page.sync="queryParams.pageNum"
          :limit.sync="queryParams.pageSize"
          @pagination="getList"
        />
      </div>
    </div>

    <!-- 添加或修改模式对话框 -->
    <el-dialog :title="title" :visible.sync="open" width="800px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="80px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="模式名称" prop="modeName">
              <el-input v-model="form.modeName" placeholder="请输入模式名称" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="模式类型" prop="modeType">
              <el-select v-model="form.modeType" placeholder="请选择模式类型">
                <el-option label="系统模式" value="system" />
                <el-option label="自定义模式" value="custom" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="模式颜色" prop="modeColor">
              <el-color-picker v-model="form.modeColor" show-alpha></el-color-picker>
              <span style="margin-left: 10px;">{{ form.modeColor }}</span>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="模式图标" prop="modeIcon">
              <el-select v-model="form.modeIcon" placeholder="请选择图标">
                <el-option label="待机图标" value="fa fa-pause-circle" />
                <el-option label="维护图标" value="fa fa-tools" />
                <el-option label="充电图标" value="fa fa-bolt" />
                <el-option label="自定义图标" value="fa fa-cog" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="模式描述" prop="description">
          <el-input type="textarea" v-model="form.description" placeholder="请输入内容" :rows="3" />
        </el-form-item>

        <el-divider content-position="left">
          <i class="fas fa-sliders-h"></i> 配置参数
          <el-tooltip content="充电模式建议添加【充电策略】参数" placement="top">
            <i class="el-icon-question" style="margin-left: 8px; color: #909399; cursor: pointer;"></i>
          </el-tooltip>
        </el-divider>

        <div class="params-config">
          <div v-if="form.modeParams && form.modeParams.length > 0" style="margin-bottom: 10px; color: #67c23a; font-size: 12px;">
            <i class="el-icon-info"></i> 当前共有 {{ form.modeParams.length }} 个参数
          </div>
          <div v-else style="margin-bottom: 10px; color: #909399; font-size: 12px;">
            <i class="el-icon-info"></i> 暂无参数，请点击下方按钮添加
          </div>

          <div v-for="(param, index) in form.modeParams" :key="index" class="param-item">
            <div class="param-row">
              <div class="param-field param-name">
                <el-input v-model="param.paramName" placeholder="参数名称" size="small" />
              </div>
              <div class="param-field param-type">
                <el-select v-model="param.paramType" placeholder="参数类型" size="small" @change="onParamTypeChange(param)">
                  <el-option label="开关" value="boolean" />
                  <el-option label="数值" value="number" />
                  <el-option label="下拉选择" value="select" />
                  <el-option label="文本" value="string" />
                  <el-option label="滑块" value="range" />
                </el-select>
              </div>
              <div class="param-field param-value">
                <!-- 根据类型显示不同的输入控件 -->
                <div v-if="param.paramType === 'boolean'">
                  <el-switch v-model="param.paramValue" active-text="开启" inactive-text="关闭"></el-switch>
                </div>
                <div v-else-if="param.paramType === 'number'">
                  <el-input-number v-model="param.paramValue" :min="param.paramMin || 0" :max="param.paramMax || 100" size="small" controls-position="right"></el-input-number>
                </div>
                <div v-else-if="param.paramType === 'select'">
                  <el-select v-model="param.paramValue" placeholder="请选择" size="small">
                    <el-option v-for="opt in getParamOptions(param)" :key="opt.value" :label="opt.label" :value="opt.value"></el-option>
                  </el-select>
                </div>
                <div v-else>
                  <el-input v-model="param.paramValue" placeholder="参数值" size="small" />
                </div>
              </div>
              <div class="param-actions">
                <el-button type="primary" icon="el-icon-edit" size="mini" circle @click="editParam(index)"></el-button>
                <el-button type="danger" icon="el-icon-delete" size="mini" circle @click="removeParam(index)"></el-button>
              </div>
            </div>
          </div>

          <div style="display: flex; gap: 10px; margin-top: 8px;">
            <el-button type="primary" plain icon="el-icon-plus" size="small" @click="addParam" class="add-param-btn">
              添加参数
            </el-button>
            <el-button
              type="success"
              plain
              icon="el-icon-setting"
              size="small"
              @click="addChargeStrategyParam"
              v-if="form.modeName === '充电模式' || form.modeId === 3">
              快速添加充电策略
            </el-button>
          </div>
        </div>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitForm">确 定</el-button>
        <el-button @click="cancel">取 消</el-button>
      </div>
    </el-dialog>

    <!-- 添加参数配置对话框 -->
    <el-dialog title="参数配置" :visible.sync="paramDialogOpen" width="550px" append-to-body>
      <el-form ref="paramForm" :model="currentParam" label-width="90px">
        <el-form-item label="参数名称" required>
          <el-input v-model="currentParam.paramName" placeholder="请输入参数名称" />
        </el-form-item>
        <el-form-item label="参数类型" required>
          <el-select v-model="currentParam.paramType" placeholder="请选择参数类型" @change="onParamTypeChange(currentParam)" style="width: 100%;">
            <el-option label="开关" value="boolean" />
            <el-option label="数值" value="number" />
            <el-option label="下拉选择" value="select" />
            <el-option label="文本" value="string" />
            <el-option label="滑块" value="range" />
          </el-select>
        </el-form-item>
        <el-form-item label="参数描述">
          <el-input v-model="currentParam.paramDescription" placeholder="请输入参数描述" type="textarea" :rows="2" />
        </el-form-item>

        <!-- 数值类型配置 -->
        <div v-if="currentParam.paramType === 'number' || currentParam.paramType === 'range'">
          <el-row :gutter="15">
            <el-col :span="12">
              <el-form-item label="最小值">
                <el-input-number v-model="currentParam.paramMin" :min="0" style="width: 100%;"></el-input-number>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="最大值">
                <el-input-number v-model="currentParam.paramMax" :min="0" style="width: 100%;"></el-input-number>
              </el-form-item>
            </el-col>
          </el-row>
          <el-form-item label="单位">
            <el-input v-model="currentParam.paramUnit" placeholder="例如：%、秒、次" />
          </el-form-item>
          <el-form-item label="默认值">
            <el-input-number v-model="currentParam.paramValue" :min="currentParam.paramMin || 0" :max="currentParam.paramMax || 100" style="width: 100%;"></el-input-number>
          </el-form-item>
        </div>

        <!-- 下拉选择类型配置 -->
        <div v-if="currentParam.paramType === 'select'">
          <el-form-item label="选项配置">
            <div v-for="(option, idx) in currentParam.paramOptions" :key="idx" class="option-item">
              <el-input v-model="option.label" placeholder="显示文本" size="small" />
              <el-input v-model="option.value" placeholder="值" size="small" />
              <el-button type="danger" icon="el-icon-delete" circle size="mini" @click="removeOption(idx)"></el-button>
            </div>
            <el-button type="primary" plain size="small" icon="el-icon-plus" @click="addOption">添加选项</el-button>
          </el-form-item>
          <el-form-item label="默认选项">
            <el-select v-model="currentParam.paramValue" placeholder="请选择默认选项" style="width: 100%;">
              <el-option v-for="opt in currentParam.paramOptions" :key="opt.value" :label="opt.label" :value="opt.value"></el-option>
            </el-select>
          </el-form-item>
        </div>

        <!-- 开关类型配置 -->
        <div v-if="currentParam.paramType === 'boolean'">
          <el-form-item label="默认值">
            <el-switch v-model="currentParam.paramValue" active-text="开启" inactive-text="关闭"></el-switch>
          </el-form-item>
        </div>

        <!-- 文本类型配置 -->
        <div v-if="currentParam.paramType === 'string'">
          <el-form-item label="默认值">
            <el-input v-model="currentParam.paramValue" placeholder="请输入默认值" />
          </el-form-item>
        </div>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="saveParam">确 定</el-button>
        <el-button @click="paramDialogOpen = false">取 消</el-button>
      </div>
    </el-dialog>

    <!-- 排序对话框 -->
    <el-dialog title="模式排序" :visible.sync="sortDialogOpen" width="650px" append-to-body>
      <el-table :data="sortList" row-key="modeId" border>
        <el-table-column label="排序" width="100" align="center">
          <template slot-scope="scope">
            <el-input-number
              v-model="scope.row.orderNum"
              :min="1"
              :max="sortList.length"
              size="small"
              controls-position="right"
              @change="handleOrderNumChange(scope.row)"
              style="width: 100%;"
            ></el-input-number>
          </template>
        </el-table-column>
        <el-table-column label="模式名称" prop="modeName" min-width="150"></el-table-column>
        <el-table-column label="模式类型" prop="modeType" width="100">
          <template slot-scope="scope">
            <el-tag size="small" :type="scope.row.modeType === 'system' ? 'primary' : 'success'">
              {{ scope.row.modeType === 'system' ? '系统' : '自定义' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="80" align="center">
          <template slot-scope="scope">
            <el-tag size="small" :type="scope.row.enabled === '1' ? 'success' : 'danger'">
              {{ scope.row.enabled === '1' ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
      </el-table>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="saveSort">保 存</el-button>
        <el-button @click="sortDialogOpen = false">取 消</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { listMode, getMode, delMode, addMode, updateMode, changeModeStatus, getChargeStrategyParam } from "@/api/mode/mode";

export default {
  name: "Mode",
  data() {
    return {
      loading: true,
      ids: [],
      single: true,
      multiple: true,
      showSearch: true,
      total: 0,
      modeList: [],
      title: "",
      open: false,
      paramDialogOpen: false,
      sortDialogOpen: false,
      sortList: [],
      currentParam: {
        paramId: null,
        paramName: '',
        paramType: 'string',
        paramDescription: '',
        paramValue: '',
        paramOptions: [{ label: '选项1', value: 'option1' }],
        paramMin: 0,
        paramMax: 100,
        paramUnit: '',
        _tempId: null
      },
      queryParams: {
        pageNum: 1,
        pageSize: 12,
        modeName: null,
        modeType: null,
        enabled: null
      },
      form: {
        modeId: null,
        modeName: null,
        modeType: 'custom',
        categoryId: 2,
        modeColor: '#1890ff',
        modeIcon: 'fa fa-cog',
        description: null,
        enabled: '1',
        usageCount: 0,
        robotCount: 0,
        orderNum: 0,
        modeParams: []
      },
      rules: {
        modeName: [
          { required: true, message: "模式名称不能为空", trigger: "blur" }
        ]
      },
      categoryMap: {
        1: '系统模式',
        2: '自定义模式'
      }
    };
  },
  created() {
    this.getList();
  },
  methods: {
    /** 查询模式列表 */
    getList() {
      this.loading = true;
      console.log('开始查询模式列表，查询参数:', this.queryParams);

      listMode(this.queryParams).then(response => {
        console.log('查询返回结果:', response);

        const rows = response.rows || [];
        console.log('返回数据条数:', rows.length);

        // 处理每个模式的参数
        rows.forEach(mode => {
          if (mode.modeParams && mode.modeParams.length > 0) {
            mode.modeParams = this.processParamsData(mode.modeParams);
          }
        });

        this.modeList = rows.sort((a, b) => (a.orderNum || 0) - (b.orderNum || 0));
        this.total = response.total;
        this.loading = false;

        console.log('最终显示的模式列表数量:', this.modeList.length);
      }).catch(error => {
        console.error('查询模式列表失败:', error);
        this.loading = false;
        this.$modal.msgError("查询模式列表失败");
      });
    },

    /** 处理参数数据，将JSON字符串转换为对象 */
    processParamsData(params) {
      if (!params || !Array.isArray(params)) {
        return [];
      }

      return params.map(param => {
        const processedParam = JSON.parse(JSON.stringify(param));

        if (processedParam.paramOptions && typeof processedParam.paramOptions === 'string') {
          try {
            processedParam.paramOptions = JSON.parse(processedParam.paramOptions);
          } catch (e) {
            processedParam.paramOptions = [];
          }
        }

        if (!processedParam.paramOptions || !Array.isArray(processedParam.paramOptions)) {
          processedParam.paramOptions = [];
        }

        if (processedParam.paramType === 'boolean') {
          processedParam.paramValue = processedParam.paramValue === 'true' || processedParam.paramValue === true;
        } else if (processedParam.paramType === 'number' || processedParam.paramType === 'range') {
          processedParam.paramValue = Number(processedParam.paramValue) || 0;
        } else if (processedParam.paramType === 'select') {
          if (!processedParam.paramValue && processedParam.paramOptions.length > 0) {
            processedParam.paramValue = processedParam.paramOptions[0]?.value || '';
          }
        }

        return processedParam;
      });
    },

    /** 获取参数选项（确保返回数组） */
    getParamOptions(param) {
      if (!param.paramOptions) return [];
      if (Array.isArray(param.paramOptions)) return param.paramOptions;
      if (typeof param.paramOptions === 'string') {
        try {
          const parsed = JSON.parse(param.paramOptions);
          return Array.isArray(parsed) ? parsed : [];
        } catch (e) {
          return [];
        }
      }
      return [];
    },

    /** 搜索按钮操作 */
    handleQuery() {
      this.queryParams.pageNum = 1;
      this.getList();
    },

    /** 重置按钮操作 */
    resetQuery() {
      this.resetForm("queryForm");
      this.handleQuery();
    },

    /** 新增按钮操作 */
    handleAdd() {
      this.reset();

      // 自动计算排序值：取现有模式中最大的 orderNum + 1
      let maxOrderNum = 0;
      if (this.modeList && this.modeList.length > 0) {
        const orderNums = this.modeList.map(mode => mode.orderNum || 0);
        maxOrderNum = Math.max(...orderNums);
      }
      this.form.orderNum = maxOrderNum + 1;

      this.open = true;
      this.title = "添加模式";
    },

    /** 快速添加充电策略参数 */
    addChargeStrategyParam() {
      const chargeStrategyParam = getChargeStrategyParam();
      chargeStrategyParam._tempId = Date.now();
      this.form.modeParams.push(chargeStrategyParam);
      this.$message.success('已添加充电策略参数');
    },

    /** 修改按钮操作 */
    handleUpdate(row) {
      this.reset();
      const modeId = row.modeId;

      getMode(modeId).then(response => {
        this.form = response.data;

        if (this.form.params && !this.form.modeParams) {
          this.form.modeParams = this.form.params;
        }

        if (this.form.modeParams && this.form.modeParams.length > 0) {
          this.form.modeParams = this.processParamsData(this.form.modeParams);
        } else {
          this.form.modeParams = [];
        }

        this.open = true;
        this.title = "修改模式";
      }).catch(error => {
        console.error('获取模式详情失败:', error);
        this.$modal.msgError("获取模式详情失败");
      });
    },

    /** 删除按钮操作 */
    handleDelete(row) {
      const modeIds = row.modeId || this.ids;
      const modeName = row.modeName || '该模式';

      console.log('准备删除模式:', { modeIds, modeName });

      this.$modal.confirm(`是否确认删除模式"${modeName}"？删除后将无法恢复！`, '警告', {
        confirmButtonText: '确定删除',
        cancelButtonText: '取消',
        type: 'warning',
        dangerouslyUseHTMLString: true
      }).then(() => {
        const loading = this.$loading({
          lock: true,
          text: '正在删除中...',
          spinner: 'el-icon-loading',
          background: 'rgba(0, 0, 0, 0.7)'
        });

        console.log('调用删除API，参数:', modeIds);

        return delMode(modeIds).then(response => {
          loading.close();
          console.log('删除API返回:', response);

          if (response && response.code === 200) {
            this.$modal.msgSuccess(`删除"${modeName}"成功`);
            console.log('重新加载模式列表...');
            this.getList();
          } else {
            this.$modal.msgError(response.msg || "删除失败");
          }
        }).catch(error => {
          loading.close();
          console.error('删除失败详情:', error);
          console.error('错误响应:', error.response);

          let errorMsg = "删除失败，请稍后重试";
          if (error.response && error.response.data) {
            errorMsg = error.response.data.msg || errorMsg;
          } else if (error.message) {
            errorMsg = error.message;
          }

          this.$modal.msgError(errorMsg);
          throw error;
        });
      }).catch(() => {
        console.log('用户取消了删除操作');
      });
    },

    /** 启用/禁用模式 */
    handleChangeStatus(row) {
      let text = row.enabled === '1' ? "禁用" : "启用";
      this.$modal.confirm('确认要' + text + '"' + row.modeName + '"模式吗？').then(() => {
        const loading = this.$loading({
          lock: true,
          text: '正在处理中...',
          spinner: 'el-icon-loading',
          background: 'rgba(0, 0, 0, 0.7)'
        });

        const data = {
          modeId: row.modeId,
          enabled: row.enabled === '1' ? '0' : '1'
        };

        return changeModeStatus(data).then(response => {
          loading.close();
          if (response && response.code === 200) {
            this.$modal.msgSuccess(text + "成功");
            this.getList();
          } else {
            this.$modal.msgError(response.msg || text + "失败");
          }
        }).catch(error => {
          loading.close();
          console.error('状态变更失败:', error);
          this.$modal.msgError(text + "失败");
          throw error;
        });
      }).catch(() => {});
    },

    /** 打开排序对话框 */
    handleSort() {
      this.sortList = JSON.parse(JSON.stringify(this.modeList));
      this.sortDialogOpen = true;
    },

    /** 排序值变化 */
    handleOrderNumChange(row) {
      const otherItems = this.sortList.filter(item => item.modeId !== row.modeId);
      const exists = otherItems.some(item => item.orderNum === row.orderNum);
      if (exists) {
        this.$modal.msgWarning("排序值不能重复");
        const original = this.modeList.find(m => m.modeId === row.modeId);
        row.orderNum = original ? original.orderNum : 0;
      }
    },

    /** 保存排序 */
    saveSort() {
      const orderNums = this.sortList.map(item => item.orderNum);
      const hasDuplicate = orderNums.some((num, index) => orderNums.indexOf(num) !== index);
      if (hasDuplicate) {
        this.$modal.msgError("排序值不能重复，请检查后重试");
        return;
      }

      const loading = this.$loading({
        lock: true,
        text: '正在保存排序...',
        spinner: 'el-icon-loading',
        background: 'rgba(0, 0, 0, 0.7)'
      });

      const updatePromises = this.sortList.map(item => {
        return updateMode({ modeId: item.modeId, orderNum: item.orderNum });
      });

      Promise.all(updatePromises).then(() => {
        loading.close();
        this.$modal.msgSuccess("排序保存成功");
        this.sortDialogOpen = false;
        this.getList();
      }).catch(() => {
        loading.close();
        this.$modal.msgError("排序保存失败");
      });
    },

    /** 添加参数 */
    addParam() {
      this.currentParam = {
        paramId: null,
        paramName: '',
        paramType: 'string',
        paramDescription: '',
        paramValue: '',
        paramOptions: [{ label: '选项1', value: 'option1' }],
        paramMin: 0,
        paramMax: 100,
        paramUnit: '',
        _tempId: null
      };
      this.paramDialogOpen = true;
    },

    /** 编辑参数 */
    editParam(index) {
      const param = JSON.parse(JSON.stringify(this.form.modeParams[index]));

      if (param.paramOptions && typeof param.paramOptions === 'string') {
        try {
          param.paramOptions = JSON.parse(param.paramOptions);
        } catch (e) {
          param.paramOptions = [];
        }
      }
      if (!param.paramOptions || !Array.isArray(param.paramOptions)) {
        param.paramOptions = [];
      }

      if (param.paramType === 'boolean') {
        param.paramValue = param.paramValue === 'true' || param.paramValue === true;
      }

      this.currentParam = param;
      this.paramDialogOpen = true;
    },

    /** 移除参数 */
    removeParam(index) {
      this.form.modeParams.splice(index, 1);
    },

    /** 保存参数 */
    saveParam() {
      if (!this.currentParam.paramName) {
        this.$modal.msgWarning("请输入参数名称");
        return;
      }

      const paramToSave = JSON.parse(JSON.stringify(this.currentParam));

      if (paramToSave.paramType === 'boolean') {
        paramToSave.paramValue = paramToSave.paramValue === true ? 'true' : 'false';
      } else if (paramToSave.paramType === 'number' || paramToSave.paramType === 'range') {
        paramToSave.paramValue = String(paramToSave.paramValue || 0);
      } else if (paramToSave.paramType === 'select') {
        if (!paramToSave.paramValue && paramToSave.paramOptions && paramToSave.paramOptions.length > 0) {
          paramToSave.paramValue = paramToSave.paramOptions[0]?.value || '';
        }
      } else {
        paramToSave.paramValue = paramToSave.paramValue || '';
      }

      if (paramToSave.paramType === 'select' && paramToSave.paramOptions) {
        if (typeof paramToSave.paramOptions === 'string') {
          try {
            paramToSave.paramOptions = JSON.parse(paramToSave.paramOptions);
          } catch(e) {
            paramToSave.paramOptions = [];
          }
        }
      } else {
        paramToSave.paramOptions = null;
      }

      if (this.currentParam._tempId || this.currentParam.paramId) {
        const index = this.form.modeParams.findIndex(p =>
          (p._tempId && p._tempId === this.currentParam._tempId) ||
          (p.paramId && p.paramId === this.currentParam.paramId)
        );
        if (index !== -1) {
          if (this.currentParam.paramId) {
            paramToSave.paramId = this.currentParam.paramId;
          }
          this.$set(this.form.modeParams, index, paramToSave);
        } else {
          paramToSave._tempId = Date.now();
          this.form.modeParams.push(paramToSave);
        }
      } else {
        paramToSave._tempId = Date.now();
        this.form.modeParams.push(paramToSave);
      }

      this.paramDialogOpen = false;
    },

    /** 参数类型变化 */
    onParamTypeChange(param) {
      if (param.paramType === 'boolean') {
        param.paramValue = param.paramValue === 'true' ? true : false;
      } else if (param.paramType === 'number' || param.paramType === 'range') {
        param.paramValue = Number(param.paramValue) || 0;
      } else if (param.paramType === 'select') {
        if (typeof param.paramOptions === 'string') {
          try {
            param.paramOptions = JSON.parse(param.paramOptions);
          } catch(e) {
            param.paramOptions = [{ label: '选项1', value: 'option1' }];
          }
        }
        if (!param.paramOptions || param.paramOptions.length === 0) {
          param.paramOptions = [{ label: '选项1', value: 'option1' }];
        }
        if (!param.paramValue || param.paramValue === '') {
          param.paramValue = param.paramOptions[0]?.value || '';
        }
      }
    },

    /** 添加选项 */
    addOption() {
      if (!this.currentParam.paramOptions) {
        this.currentParam.paramOptions = [];
      }
      if (typeof this.currentParam.paramOptions === 'string') {
        try {
          this.currentParam.paramOptions = JSON.parse(this.currentParam.paramOptions);
        } catch(e) {
          this.currentParam.paramOptions = [];
        }
      }
      this.currentParam.paramOptions.push({
        label: `选项${this.currentParam.paramOptions.length + 1}`,
        value: `option${this.currentParam.paramOptions.length + 1}`
      });
    },

    /** 删除选项 */
    removeOption(index) {
      this.currentParam.paramOptions.splice(index, 1);
    },

    /** 获取分类名称 */
    getCategoryName(categoryId) {
      return this.categoryMap[categoryId] || '未知分类';
    },

    /** 表单重置 */
    reset() {
      this.form = {
        modeId: null,
        modeName: null,
        modeType: 'custom',
        categoryId: 2,
        modeColor: '#1890ff',
        modeIcon: 'fa fa-cog',
        description: null,
        enabled: '1',
        usageCount: 0,
        robotCount: 0,
        orderNum: 0,
        modeParams: []
      };
      this.resetForm("form");
    },

    /** 取消按钮 */
    cancel() {
      this.open = false;
      this.reset();
    },

    /** 提交按钮 */
    submitForm() {
      this.$refs["form"].validate(valid => {
        if (valid) {
          const loading = this.$loading({
            lock: true,
            text: '正在保存中...',
            spinner: 'el-icon-loading',
            background: 'rgba(0, 0, 0, 0.7)'
          });

          const submitData = JSON.parse(JSON.stringify(this.form));

          if (submitData.modeParams && submitData.modeParams.length > 0) {
            submitData.modeParams.forEach(param => {
              delete param._tempId;

              if (param.paramType === 'boolean') {
                param.paramValue = param.paramValue === true ? 'true' : 'false';
              } else if (param.paramType === 'number' || param.paramType === 'range') {
                param.paramValue = String(param.paramValue || 0);
              } else if (param.paramType === 'select') {
                if (param.paramOptions && typeof param.paramOptions === 'object') {
                  param.paramOptions = JSON.stringify(param.paramOptions);
                }
                if (!param.paramValue && param.paramOptions) {
                  try {
                    const options = JSON.parse(param.paramOptions);
                    param.paramValue = options[0]?.value || '';
                  } catch(e) {}
                }
              } else {
                param.paramValue = param.paramValue || '';
              }

              if (param.paramOptions && typeof param.paramOptions !== 'string') {
                param.paramOptions = JSON.stringify(param.paramOptions);
              }
            });
          }

          const savePromise = this.form.modeId != null
            ? updateMode(submitData)
            : addMode(submitData);

          savePromise.then(response => {
            loading.close();
            if (response && response.code === 200) {
              this.$modal.msgSuccess(this.form.modeId != null ? "修改成功" : "新增成功");
              this.open = false;
              this.getList();
            } else {
              this.$modal.msgError(response.msg || (this.form.modeId != null ? "修改失败" : "新增失败"));
            }
          }).catch(error => {
            loading.close();
            console.error('保存失败:', error);
            this.$modal.msgError(this.form.modeId != null ? "修改失败" : "新增失败");
          });
        }
      });
    }
  }
};
</script>

<style scoped>
.card {
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  margin-bottom: 24px;
  overflow: hidden;
}

.card-header {
  padding: 20px 24px;
  border-bottom: 1px solid #E5E7EB;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-title {
  font-size: 16px;
  font-weight: 600;
  color: #1F2937;
  display: flex;
  align-items: center;
  gap: 8px;
}

.card-title i {
  color: #3B82F6;
  font-size: 18px;
}

.card-body {
  padding: 24px;
}

.filter-bar {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 24px;
  flex-wrap: wrap;
  background-color: #F9FAFB;
  padding: 16px;
  border-radius: 8px;
}

.filter-bar i {
  color: #3B82F6;
  margin-right: 4px;
}

.pagination-wrap {
  display: flex;
  justify-content: flex-end;
  margin-top: 24px;
}

.mode-grid {
  margin-top: 8px;
}

.mode-cards-container {
  display: flex;
  flex-wrap: wrap;
  margin: -12px;
}

.mode-card-item {
  width: calc(33.333% - 24px);
  margin: 12px;
  display: flex;
  flex-direction: column;
}

@media (max-width: 1200px) {
  .mode-card-item {
    width: calc(50% - 24px);
  }
}

@media (max-width: 768px) {
  .mode-card-item {
    width: calc(100% - 24px);
  }

  .filter-bar {
    flex-direction: column;
    align-items: stretch;
  }

  .filter-bar .el-input,
  .filter-bar .el-select {
    width: 100% !important;
  }

  .mode-actions {
    flex-wrap: wrap;
    justify-content: flex-end;
  }
}

.mode-card {
  transition: all 0.3s ease;
  border: 1px solid #E5E7EB;
  height: 100%;
  display: flex;
  flex-direction: column;
  border-radius: 12px;
  overflow: hidden;
}

.mode-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
}

.mode-card-header {
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 12px;
  border-bottom: 1px solid #F3F4F6;
}

.mode-icon {
  width: 52px;
  height: 52px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  color: white;
  flex-shrink: 0;
}

.mode-info {
  flex: 1;
  min-width: 0;
}

.mode-name {
  font-size: 16px;
  font-weight: 600;
  color: #1F2937;
  margin-bottom: 6px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.mode-tags {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.mode-type {
  font-size: 11px;
  color: #9CA3AF;
}

.mode-card-body {
  padding: 16px 20px;
  flex: 1;
}

.mode-description {
  font-size: 13px;
  color: #6B7280;
  line-height: 1.5;
  margin-bottom: 16px;
  min-height: 60px;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
}

.mode-stats {
  display: flex;
  gap: 12px;
  margin-bottom: 16px;
  justify-content: space-around;
  background-color: #F9FAFB;
  padding: 12px;
  border-radius: 8px;
}

.stat-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  flex: 1;
}

.stat-value {
  font-size: 20px;
  font-weight: 700;
  color: #1F2937;
}

.stat-label {
  font-size: 11px;
  color: #9CA3AF;
  margin-top: 4px;
}

.mode-params-title {
  font-size: 12px;
  font-weight: 500;
  margin-bottom: 10px;
  color: #6B7280;
  display: flex;
  align-items: center;
  gap: 6px;
}

.mode-params-title i {
  font-size: 12px;
}

.mode-params-list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.param-tag {
  margin: 0;
}

.more-tag {
  background-color: #F3F4F6;
  border-color: #E5E7EB;
  color: #6B7280;
}

.mode-card-footer {
  padding: 16px 20px;
  border-top: 1px solid #F3F4F6;
  display: flex;
  justify-content: space-between;
  align-items: center;
  background-color: #FAFAFA;
}

.mode-status {
  font-size: 12px;
  display: flex;
  align-items: center;
  gap: 6px;
}

.mode-status-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
}

.status-active {
  background-color: #10B981;
  box-shadow: 0 0 0 2px rgba(16, 185, 129, 0.2);
}

.status-inactive {
  background-color: #EF4444;
  box-shadow: 0 0 0 2px rgba(239, 68, 68, 0.2);
}

.mode-actions {
  display: flex;
  gap: 4px;
}

.mode-actions .el-button--text {
  color: #6B7280;
}

.mode-actions .el-button--text:hover {
  color: #3B82F6;
}

.params-config {
  margin-top: 12px;
  max-height: 450px;
  overflow-y: auto;
  padding: 8px;
  background-color: #F9FAFB;
  border-radius: 8px;
}

.param-item {
  margin-bottom: 12px;
  padding: 12px;
  background-color: white;
  border-radius: 8px;
  border: 1px solid #E5E7EB;
}

.param-row {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.param-field {
  flex: 1;
}

.param-name {
  flex: 2;
  min-width: 120px;
}

.param-type {
  flex: 1.5;
  min-width: 100px;
}

.param-value {
  flex: 2;
  min-width: 150px;
}

.param-actions {
  display: flex;
  gap: 8px;
  flex-shrink: 0;
}

.add-param-btn {
  margin-top: 8px;
  width: 100%;
}

.option-item {
  display: flex;
  gap: 10px;
  margin-bottom: 10px;
  align-items: center;
}

.option-item .el-input {
  flex: 1;
}

.el-dialog__header {
  border-bottom: 1px solid #E5E7EB;
  padding: 20px 24px;
}

.el-dialog__title {
  font-size: 16px;
  font-weight: 600;
  color: #1F2937;
}

.el-dialog__body {
  padding: 20px 24px;
}

.el-dialog__footer {
  border-top: 1px solid #E5E7EB;
  padding: 16px 24px;
}

.el-form-item__label {
  font-weight: 500;
  color: #374151;
}

.el-divider__text {
  font-weight: 500;
  color: #374151;
}
</style>
