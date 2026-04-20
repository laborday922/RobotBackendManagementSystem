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
            @input="debouncedQuery"
            @clear="debouncedQuery"
          />
        </el-form-item>
        <el-form-item label="应用名称" prop="appId">
          <el-select
            v-model="queryParams.appId"
            placeholder="请选择应用"
            clearable
            style="width: 200px"
            @change="handleQuery"
            @clear="handleQuery"
          >
            <el-option
              v-for="item in appList"
              :key="item.id"
              :label="item.appName"
              :value="item.id"
            />
          </el-select>
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
        <el-table-column label="应用" prop="appName" width="150" show-overflow-tooltip>
          <template slot-scope="scope">
            <el-tag v-if="scope.row.appName" type="info" size="small">{{ scope.row.appName }}</el-tag>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column label="描述" prop="description" min-width="200" show-overflow-tooltip />
        <el-table-column label="适用机器人组" width="180">
          <template slot-scope="scope">
            <el-tag v-for="(name, index) in scope.row.robotGroupNames" :key="index" size="small" style="margin-right:4px;">
              {{ name }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="约束规则" width="100" align="center">
          <template slot-scope="scope">
            <el-tag size="small" type="danger">{{ getRuleCount(scope.row) }}</el-tag>
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
        <!-- 操作列 -->
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
      width="1100px"
      append-to-body
      @close="cancel"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="模板名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入模板名称" />
        </el-form-item>

        <!-- 选择应用 -->
        <el-form-item label="所属应用" prop="appId">
          <el-select
            v-model="form.appId"
            placeholder="请选择应用"
            style="width:100%"
            @change="onAppChange"
            :disabled="dialog.mode === 'edit'"
          >
            <el-option
              v-for="item in appList"
              :key="item.id"
              :label="item.appName"
              :value="item.id"
            >
              <span style="float: left">{{ item.appName }}</span>
              <span style="float: right; color: #8492a6; font-size: 13px">{{ item.appId }}</span>
            </el-option>
          </el-select>
          <div class="el-form-item-msg">选择应用后将自动加载该应用的能力参数和API</div>
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
              <el-col :span="5">
                <el-input v-model="field.id" placeholder="字段ID（英文）" size="small" />
              </el-col>
              <el-col :span="5">
                <el-input v-model="field.label" placeholder="字段标签" size="small" />
              </el-col>
              <el-col :span="4">
                <el-select v-model="field.type" placeholder="类型" size="small" style="width:100%">
                  <el-option label="文本" value="text" />
                  <el-option label="数字" value="number" />
                  <el-option label="日期" value="date" />
                  <el-option label="时间" value="time" />
                  <el-option label="下拉" value="select" />
                  <el-option label="动态下拉" value="dynamicSelect" />
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
            <!-- 动态下拉额外配置 -->
            <el-row v-if="field.type === 'dynamicSelect'" :gutter="10" style="margin-top:5px">
              <el-col :span="12">
                <el-select v-model="field.apiId" placeholder="选择关联API" size="small" style="width:100%">
                  <el-option
                    v-for="api in currentAppApis"
                    :key="api.id"
                    :label="api.apiName"
                    :value="api.id"
                  />
                </el-select>
              </el-col>
              <el-col :span="12">
                <el-input v-model="field.paramKey" placeholder="参数Key（用于匹配动态选项）" size="small" />
              </el-col>
            </el-row>
          </div>
          <div v-if="form.fields.length === 0" class="empty-tip">暂无字段定义，请点击"添加字段"</div>
        </div>

        <!-- 应用能力参数展示 -->
        <template v-if="currentAppParams.length > 0">
          <el-divider content-position="left">应用能力参数</el-divider>
          <div class="app-params-container">
            <el-alert
              :title="`当前应用：${currentAppName} 的能力参数配置（只读，可在步骤和约束规则中使用）`"
              type="info"
              :closable="false"
              style="margin-bottom: 10px"
            />
            <el-table :data="currentAppParams" size="small" border style="width: 100%">
              <el-table-column prop="paramKey" label="参数Key" width="150" />
              <el-table-column prop="paramName" label="参数名称" width="150" />
              <el-table-column prop="paramType" label="类型" width="100" />
              <el-table-column prop="defaultValue" label="默认值" width="150" />
              <el-table-column prop="validationRule" label="校验规则" min-width="200" />
            </el-table>
          </div>
        </template>

        <!-- 模板约束规则 -->
        <el-divider content-position="left">模板约束规则</el-divider>
        <div class="rules-container">
          <div class="rules-tip">
            <i class="el-icon-warning" style="color: #e6a23c;"></i>
            <span>配置约束规则限制表单字段取值。支持表单字段（form_data.X）、能力参数（app_param.X）和固定值的比较表达式。</span>
          </div>

          <el-button size="small" type="warning" @click="addRule" style="margin-bottom:10px; margin-top:10px;">
            <i class="el-icon-plus"></i>添加约束规则
          </el-button>

          <div v-for="(rule, index) in form.rules" :key="'rule-'+index" class="rule-item">
            <el-card shadow="hover" :body-style="{ padding: '15px' }">
              <el-row :gutter="10" type="flex" align="middle">
                <el-col :span="8">
                  <el-form-item label="规则名称" class="compact-form-item" style="margin-bottom: 0;">
                    <el-input v-model="rule.name" placeholder="如：重量限制" size="small" />
                  </el-form-item>
                </el-col>
                <el-col :span="6">
                  <el-form-item label="严重程度" class="compact-form-item" style="margin-bottom: 0;">
                    <el-select v-model="rule.severity" placeholder="严重程度" size="small" style="width:100%">
                      <el-option label="错误（阻止提交）" value="error" />
                      <el-option label="警告（提示但允许）" value="warning" />
                    </el-select>
                  </el-form-item>
                </el-col>
                <el-col :span="8" style="text-align:right">
                  <el-button type="danger" size="small" @click="removeRule(index)">删除规则</el-button>
                </el-col>
              </el-row>

              <!-- 表达式配置 -->
              <div class="expression-builder">
                <div class="section-subtitle">约束表达式配置</div>
                <el-row :gutter="10" type="flex" align="middle">
                  <el-col :span="7">
                    <el-select v-model="rule.leftType" placeholder="左侧类型" size="small" @change="onRuleLeftChange(rule)">
                      <el-option label="表单字段" value="form_field" />
                      <el-option label="能力参数" value="app_param" />
                    </el-select>
                  </el-col>
                  <el-col :span="7">
                    <!-- 左侧选择表单字段 -->
                    <el-select
                      v-if="rule.leftType === 'form_field'"
                      v-model="rule.leftValue"
                      placeholder="选择表单字段"
                      size="small"
                    >
                      <el-option
                        v-for="field in form.fields"
                        :key="field.id"
                        :label="field.label"
                        :value="field.id"
                      >
                        <span style="float: left">{{ field.label }}</span>
                        <span style="float: right; color: #8492a6; font-size: 12px">{{ field.type }}</span>
                      </el-option>
                    </el-select>
                    <!-- 左侧选择能力参数 -->
                    <el-select
                      v-else-if="rule.leftType === 'app_param'"
                      v-model="rule.leftValue"
                      placeholder="选择能力参数"
                      size="small"
                    >
                      <el-option
                        v-for="param in currentAppParams"
                        :key="param.paramKey"
                        :label="param.paramName"
                        :value="param.paramKey"
                      >
                        <span style="float: left">{{ param.paramName }}</span>
                        <span style="float: right; color: #8492a6; font-size: 12px">{{ param.paramKey }}</span>
                      </el-option>
                    </el-select>
                  </el-col>
                  <el-col :span="4">
                    <el-select v-model="rule.operator" placeholder="操作符" size="small">
                      <el-option label="=" value="==" />
                      <el-option label="!=" value="!=" />
                      <el-option label=">" value=">" />
                      <el-option label=">=" value=">=" />
                      <el-option label="<" value="<" />
                      <el-option label="<=" value="<=" />
                    </el-select>
                  </el-col>
                  <el-col :span="6">
                    <el-select v-model="rule.rightType" placeholder="右侧类型" size="small">
                      <el-option label="固定值" value="fixed" />
                      <el-option label="表单字段" value="form_field" />
                      <el-option label="能力参数" value="app_param" />
                    </el-select>
                  </el-col>
                </el-row>

                <!-- 右侧值配置 -->
                <el-row :gutter="10" style="margin-top: 10px;">
                  <el-col :span="24">
                    <!-- 固定值 -->
                    <el-input
                      v-if="rule.rightType === 'fixed'"
                      v-model="rule.rightValue"
                      placeholder="输入固定值，如：100"
                      size="small"
                    />
                    <!-- 表单字段 -->
                    <el-select
                      v-else-if="rule.rightType === 'form_field'"
                      v-model="rule.rightValue"
                      placeholder="选择表单字段"
                      size="small"
                      style="width:100%"
                    >
                      <el-option
                        v-for="field in form.fields"
                        :key="field.id"
                        :label="field.label"
                        :value="field.id"
                      />
                    </el-select>
                    <!-- 能力参数 -->
                    <el-select
                      v-else-if="rule.rightType === 'app_param'"
                      v-model="rule.rightValue"
                      placeholder="选择能力参数"
                      size="small"
                      style="width:100%"
                    >
                      <el-option
                        v-for="param in currentAppParams"
                        :key="param.paramKey"
                        :label="param.paramName"
                        :value="param.paramKey"
                      />
                    </el-select>
                  </el-col>
                </el-row>

                <!-- 生成的表达式预览 -->
                <div class="expression-preview" v-if="getRuleExpression(rule)">
                  <i class="el-icon-view"></i>
                  表达式预览：<code>{{ getRuleExpression(rule) }}</code>
                </div>
              </div>

              <!-- 错误提示信息 -->
              <el-row style="margin-top:10px">
                <el-col :span="24">
                  <el-input
                    v-model="rule.errorMessage"
                    placeholder="验证失败时的提示信息，如：重量不能超过最大负载"
                    size="small"
                  />
                </el-col>
              </el-row>
            </el-card>
          </div>

          <div v-if="form.rules.length === 0" class="empty-tip">暂无约束规则，请点击"添加约束规则"</div>
        </div>

        <!-- 流程步骤定义 -->
        <el-divider content-position="left">标准作业流程（带API配置）</el-divider>
        <div class="steps-container">
          <div class="step-tip">
            <i class="el-icon-info" style="color: #409eff;"></i>
            <span>配置步骤时，选择API并映射参数。支持从表单字段、能力参数或固定值取值。</span>
          </div>

          <el-button
            size="small"
            type="primary"
            @click="addStep"
            style="margin-bottom:10px; margin-top:10px;"
            :disabled="!form.appId"
          >
            <i class="el-icon-plus"></i>添加步骤
          </el-button>
          <div v-if="!form.appId" class="empty-tip" style="color: #f56c6c; margin-bottom: 10px;">
            请先选择所属应用以加载API列表
          </div>

          <div v-for="(step, index) in form.steps" :key="'step-'+index" class="step-item">
            <el-card shadow="hover" :body-style="{ padding: '15px' }">
              <!-- 步骤基础信息 -->
              <el-row :gutter="10">
                <el-col :span="7">
                  <el-form-item label="步骤名称" :required="true" class="compact-form-item">
                    <el-input v-model="step.name" placeholder="如：移动到取货点" size="small" />
                  </el-form-item>
                </el-col>
                <el-col :span="6">
                  <el-form-item label="预估时间" class="compact-form-item">
                    <el-input-number v-model="step.estimatedTime" :min="1" :max="120" size="small" style="width:100px" />
                    <span style="margin-left:5px; color:#999">分钟</span>
                  </el-form-item>
                </el-col>
                <el-col :span="9">
                  <el-form-item label="选择API" class="compact-form-item">
                    <el-select
                      v-model="step.apiId"
                      placeholder="选择API"
                      size="small"
                      style="width:100%"
                      @change="onApiChange(step)"
                      filterable
                    >
                      <el-option
                        v-for="api in currentAppApis"
                        :key="api.id"
                        :label="api.apiName"
                        :value="api.id"
                      >
                        <span style="float: left">{{ api.apiName }}</span>
                        <span style="float: right; color: #8492a6; font-size: 13px">{{ api.apiKey }}</span>
                      </el-option>
                    </el-select>
                  </el-form-item>
                </el-col>
                <el-col :span="2" style="text-align:right">
                  <el-button type="danger" size="small" @click="removeStep(index)">删除</el-button>
                </el-col>
              </el-row>

              <!-- API描述 -->
              <el-row v-if="step.apiId && getApi(step.apiId)">
                <el-col :span="24">
                  <div class="operation-desc">
                    <i class="el-icon-info"></i>
                    {{ getApi(step.apiId).description || '暂无描述' }}
                  </div>
                </el-col>
              </el-row>

              <!-- 参数映射配置 -->
              <div v-if="step.apiId && step.paramMappings && step.paramMappings.length > 0" class="param-mapping-section">
                <div class="section-title">参数映射配置</div>

                <el-table :data="step.paramMappings" size="small" border style="width:100%">
                  <el-table-column prop="paramName" label="参数" width="120">
                    <template slot-scope="scope">
                      <div style="font-weight:bold">{{ scope.row.paramLabel || scope.row.paramName }}</div>
                      <div style="font-size:12px; color:#999">{{ scope.row.paramName }}</div>
                    </template>
                  </el-table-column>

                  <el-table-column prop="sourceType" label="值来源" width="120">
                    <template slot-scope="scope">
                      <el-select v-model="scope.row.sourceType" size="small" style="width:100%">
                        <el-option label="表单字段" value="field" />
                        <el-option label="能力参数" value="app_param" />
                        <el-option label="固定值" value="fixed" />
                      </el-select>
                    </template>
                  </el-table-column>

                  <el-table-column prop="sourceValue" label="值配置">
                    <template slot-scope="scope">
                      <!-- 选择表单字段 -->
                      <el-select
                        v-if="scope.row.sourceType === 'field'"
                        v-model="scope.row.sourceValue"
                        placeholder="选择表单字段"
                        size="small"
                        style="width:100%"
                      >
                        <el-option
                          v-for="field in form.fields"
                          :key="field.id"
                          :label="field.label"
                          :value="field.id"
                        >
                          <span style="float: left">{{ field.label }}</span>
                          <span style="float: right; color: #8492a6; font-size: 13px">{{ field.type }}</span>
                        </el-option>
                      </el-select>

                      <!-- 选择能力参数 -->
                      <el-select
                        v-else-if="scope.row.sourceType === 'app_param'"
                        v-model="scope.row.sourceValue"
                        placeholder="选择能力参数"
                        size="small"
                        style="width:100%"
                      >
                        <el-option
                          v-for="param in currentAppParams"
                          :key="param.paramKey"
                          :label="param.paramName"
                          :value="param.paramKey"
                        >
                          <span style="float: left">{{ param.paramName }}</span>
                          <span style="float: right; color: #8492a6; font-size: 13px">{{ param.paramKey }}</span>
                        </el-option>
                      </el-select>

                      <!-- 输入固定值 -->
                      <div v-else>
                        <el-input
                          v-if="scope.row.paramType === 'string' || !scope.row.paramType"
                          v-model="scope.row.sourceValue"
                          :placeholder="'请输入' + (scope.row.paramLabel || scope.row.paramName)"
                          size="small"
                        />
                        <el-input-number
                          v-else-if="scope.row.paramType === 'number'"
                          v-model="scope.row.sourceValue"
                          size="small"
                          style="width:100%"
                        />
                        <el-select
                          v-else-if="scope.row.paramType === 'boolean'"
                          v-model="scope.row.sourceValue"
                          size="small"
                          style="width:100%"
                        >
                          <el-option label="是" :value="true" />
                          <el-option label="否" :value="false" />
                        </el-select>
                      </div>
                    </template>
                  </el-table-column>

                  <el-table-column prop="required" label="必填" width="60" align="center">
                    <template slot-scope="scope">
                      <el-tag v-if="scope.row.required" type="danger" size="mini">是</el-tag>
                      <span v-else style="color:#999">否</span>
                    </template>
                  </el-table-column>
                </el-table>
              </div>

              <!-- 步骤描述 -->
              <el-row style="margin-top:10px">
                <el-col :span="24">
                  <el-input
                    v-model="step.description"
                    type="textarea"
                    :rows="2"
                    placeholder="步骤描述（可选，支持{字段ID}占位符）"
                    size="small"
                  />
                </el-col>
              </el-row>
            </el-card>
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
    <el-dialog title="模板详情" :visible.sync="viewDialog.visible" width="800px" append-to-body>
      <div v-if="viewDialog.data">
        <el-descriptions :column="1" border>
          <el-descriptions-item label="模板名称">{{ viewDialog.data.name }}</el-descriptions-item>
          <el-descriptions-item label="所属应用">{{ viewDialog.data.appName || '-' }}</el-descriptions-item>
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

        <!-- 约束规则 -->
        <template v-if="viewDialog.rules && viewDialog.rules.length > 0">
          <el-divider />
          <h4>约束规则</h4>
          <el-table :data="viewDialog.rules" border size="small">
            <el-table-column prop="name" label="规则名称" width="150" />
            <el-table-column prop="expression" label="表达式" min-width="250">
              <template slot-scope="scope">
                <code>{{ scope.row.expression }}</code>
              </template>
            </el-table-column>
            <el-table-column prop="severity" label="严重程度" width="100">
              <template slot-scope="scope">
                <el-tag :type="scope.row.severity === 'error' ? 'danger' : 'warning'" size="small">
                  {{ scope.row.severity === 'error' ? '错误' : '警告' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="errorMessage" label="提示信息" min-width="200" />
          </el-table>
        </template>

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
          <el-table-column label="配置" min-width="200">
            <template slot-scope="scope">
              <span v-if="['image','audio','video','file'].includes(scope.row.type)">
                最多{{ scope.row.maxCount }}个，≤{{ scope.row.maxSize }}MB
              </span>
              <span v-else-if="scope.row.type === 'dynamicSelect'">
                API: {{ scope.row.apiId ? '已配置' : '未配置' }}，Key: {{ scope.row.paramKey || '-' }}
              </span>
            </template>
          </el-table-column>
        </el-table>

        <el-divider />
        <h4>作业步骤</h4>
        <el-table :data="viewDialog.steps" border size="small">
          <el-table-column prop="name" label="步骤名称" width="150" />
          <el-table-column prop="apiName" label="API" width="150">
            <template slot-scope="scope">
              <el-tag v-if="scope.row.apiName" size="small" type="info">{{ scope.row.apiName }}</el-tag>
              <span v-else>-</span>
            </template>
          </el-table-column>
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
import { listAppLibrary, getAppLibrary } from '@/api/app/app'
import { listAppApi } from '@/api/app/app'
import { listAppParam } from '@/api/app/app'
import debounce from 'lodash/debounce'

export default {
  name: 'TaskTemplate',
  data() {
    return {
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        name: undefined,
        appId: undefined,
        robotGroupIds: [],
        status: undefined
      },
      loading: false,
      templateList: [],
      total: 0,
      robotGroupOptions: [],
      appList: [],
      dialog: {
        visible: false,
        title: '',
        mode: 'create'
      },
      currentAppApis: [],
      currentAppParams: [],
      currentAppName: '',
      currentAppObj: null,
      form: {
        id: null,
        name: '',
        appId: null,
        description: '',
        robotGroupIds: [],
        fields: [],
        rules: [],
        steps: []
      },
      rules: {
        name: [{required: true, message: '请输入模板名称', trigger: 'blur'}],
        appId: [{required: true, message: '请选择所属应用', trigger: 'change'}],
        robotGroupIds: [{required: true, message: '请选择适用机器人组', trigger: 'change'}]
      },
      submitLoading: false,
      viewDialog: {
        visible: false,
        data: null,
        fields: [],
        steps: [],
        rules: []
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
    this.getAppList()
    this.getList()
    this.debouncedQuery = debounce(this.handleQuery, 500)
  },
  beforeDestroy() {
    this.debouncedQuery.cancel()
  },
  methods: {
    async getAppList() {
      try {
        const res = await listAppLibrary({pageSize: 1000, enabled: 1})
        this.appList = res.rows || []
      } catch (error) {
        console.error('获取应用列表失败', error)
        this.$message.error('获取应用列表失败')
      }
    },

    async onAppChange(appId) {
      if (!appId) {
        this.currentAppApis = []
        this.currentAppParams = []
        this.currentAppName = ''
        this.currentAppObj = null
        return
      }

      try {
        const res = await getAppLibrary(appId)
        this.currentAppObj = res.data
        this.currentAppName = res.data ? res.data.appName : ''
        const appIdStr = res.data ? res.data.appId : ''

        const apiRes = await listAppApi({appId: appIdStr, pageSize: 1000})
        this.currentAppApis = apiRes.rows || []

        const paramRes = await listAppParam({appId: appIdStr, pageSize: 1000})
        this.currentAppParams = paramRes.rows || []

        if (this.dialog.mode === 'create') {
          this.form.steps.forEach(step => {
            step.apiId = null
            step.paramMappings = []
          })
        }
      } catch (error) {
        console.error('加载应用详情失败', error)
        this.$message.error('加载应用信息失败')
      }
    },

    getApi(apiId) {
      return this.currentAppApis.find(api => api.id === apiId)
    },

    onApiChange(step) {
      const api = this.getApi(step.apiId)
      if (!api || !api.paramsSchema) {
        step.paramMappings = []
        return
      }

      try {
        const schema = JSON.parse(api.paramsSchema)
        let params = []
        if (Array.isArray(schema)) {
          params = schema
        } else if (typeof schema === 'object') {
          params = Object.keys(schema).map(key => ({
            name: key,
            label: schema[key].label || key,
            type: schema[key].type || 'string',
            required: schema[key].required || false
          }))
        }

        step.paramMappings = params.map(param => ({
          paramName: param.name || param.paramName || param.key,
          paramLabel: param.label || param.paramName || param.name,
          paramType: param.type || param.paramType || 'string',
          required: param.required || false,
          sourceType: 'field',
          sourceValue: ''
        }))
      } catch (e) {
        console.warn('解析paramsSchema失败', e)
        step.paramMappings = []
      }
    },

    addRule() {
      this.form.rules.push({
        name: '',
        expression: '',
        errorMessage: '',
        severity: 'error',
        leftType: 'form_field',
        leftValue: '',
        operator: '<=',
        rightType: 'fixed',
        rightValue: ''
      })
    },

    removeRule(index) {
      this.form.rules.splice(index, 1)
    },

    onRuleLeftChange(rule) {
      rule.leftValue = ''
    },

    getRuleExpression(rule) {
      if (!rule.leftValue || !rule.operator) return ''

      const leftPrefix = rule.leftType === 'form_field' ? 'form_data' : 'app_param'
      const leftExpr = `${leftPrefix}.${rule.leftValue}`

      let rightExpr = ''
      if (rule.rightType === 'fixed') {
        rightExpr = rule.rightValue || ''
      } else if (rule.rightType === 'form_field') {
        rightExpr = rule.rightValue ? `form_data.${rule.rightValue}` : ''
      } else if (rule.rightType === 'app_param') {
        rightExpr = rule.rightValue ? `app_param.${rule.rightValue}` : ''
      }

      if (!rightExpr) return ''

      return `${leftExpr} ${rule.operator} ${rightExpr}`
    },

    buildRuleExpression(rule) {
      const expr = this.getRuleExpression(rule)
      rule.expression = expr
      return expr
    },

    ensureArray(ids) {
      if (!ids) return []
      if (Array.isArray(ids)) return ids
      if (typeof ids === 'string') return ids.split(',').map(s => Number(s.trim()))
      return [ids]
    },

    async getRobotGroups() {
      try {
        const res = await listGroups()
        this.robotGroupOptions = res.rows || []
      } catch (error) {
        this.$message.error('获取机器人组失败')
        console.error(error)
      }
    },

    async getList() {
      this.loading = true
      try {
        const params = {...this.queryParams}
        if (params.robotGroupIds && params.robotGroupIds.length) {
          params.robotGroupIds = params.robotGroupIds.join(',')
        }
        const res = await listTemplate(params)
        this.templateList = (res.rows || []).map(tpl => {
          let robotGroupIds = []
          if (tpl.robotGroupIds) {
            if (Array.isArray(tpl.robotGroupIds)) {
              robotGroupIds = tpl.robotGroupIds
            } else if (typeof tpl.robotGroupIds === 'string') {
              robotGroupIds = tpl.robotGroupIds.split(',').map(s => Number(s.trim()))
            }
          }
          let robotGroupNames = tpl.robotGroupNames && Array.isArray(tpl.robotGroupNames)
            ? tpl.robotGroupNames
            : robotGroupIds.map(id => {
              const group = this.robotGroupOptions.find(g => g.id === id)
              return group ? group.name : id
            })

          let fields = [], steps = [], rules = []
          try {
            if (tpl.formContent) {
              const content = JSON.parse(tpl.formContent)
              fields = content.fields || []
            }
            if (tpl.workflow) {
              const wf = JSON.parse(tpl.workflow)
              steps = wf.steps || []
            }
            rules = tpl.rules || []
          } catch (e) {
            console.warn('解析模板内容失败', e)
          }

          const app = this.appList.find(a => a.id === tpl.appId)
          const appName = tpl.appName || (app ? app.appName : '')

          return {...tpl, robotGroupIds, robotGroupNames, appName, _fields: fields, _steps: steps, _rules: rules}
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

    getRuleCount(row) {
      return row._rules ? row._rules.length : 0
    },

    getFieldCount(row) {
      return row._fields ? row._fields.length : 0
    },

    getStepCount(row) {
      return row._steps ? row._steps.length : 0
    },

    getFieldTypeText(type) {
      const map = {
        'text': '文本',
        'number': '数字',
        'date': '日期',
        'time': '时间',
        'select': '下拉',
        'dynamicSelect': '动态下拉',
        'location': '位置',
        'image': '图片',
        'audio': '音频',
        'video': '视频',
        'file': '文件'
      }
      return map[type] || type
    },

    resetForm() {
      this.form = {
        id: null,
        name: '',
        appId: null,
        description: '',
        robotGroupIds: [...this.allGroupIds],
        fields: [],
        rules: [],
        steps: []
      }
      this.currentAppApis = []
      this.currentAppParams = []
      this.currentAppName = ''
      this.currentAppObj = null
      if (this.$refs.formRef) {
        this.$refs.formRef.resetFields()
      }
    },

    handleAdd() {
      this.dialog.mode = 'create'
      this.dialog.title = '新增模板'
      this.resetForm()
      this.dialog.visible = true
    },

    async handleEdit(row) {
      if (!row || !row.id) {
        this.$message.error('模板ID无效')
        return
      }

      this.dialog.mode = 'edit'
      this.dialog.title = '修改模板'
      this.submitLoading = true

      try {
        const res = await getTemplate(row.id)
        const data = res.data

        let fields = []
        if (data.formContent) {
          try {
            const content = JSON.parse(data.formContent)
            fields = content.fields || []
          } catch (e) {
            console.warn('解析formContent失败', e)
          }
        }

        let steps = []
        if (data.workflow) {
          try {
            const wf = JSON.parse(data.workflow)
            steps = (wf.steps || []).map(step => ({
              name: step.name,
              description: step.description,
              estimatedTime: step.estimatedTime || 5,
              apiId: step.apiId,
              paramMappings: step.paramMapping || []
            }))
          } catch (e) {
            console.warn('解析workflow失败', e)
          }
        }

        let rules = []
        if (data.rules && Array.isArray(data.rules)) {
          rules = data.rules.map(rule => {
            if (rule.expression && !rule.leftValue) {
              const match = rule.expression.match(/(form_data|app_param)\.(\w+)\s*(<=|>=|<|>|==|!=)\s*(.+)/)
              if (match) {
                const [, leftPrefix, leftVal, operator, rightPart] = match
                rule.leftType = leftPrefix === 'form_data' ? 'form_field' : 'app_param'
                rule.leftValue = leftVal
                rule.operator = operator
                const rightMatch = rightPart.match(/(form_data|app_param)\.(\w+)/)
                if (rightMatch) {
                  rule.rightType = rightMatch[1] === 'form_data' ? 'form_field' : 'app_param'
                  rule.rightValue = rightMatch[2]
                } else {
                  rule.rightType = 'fixed'
                  rule.rightValue = rightPart.trim()
                }
              }
            }
            return {
              name: rule.name || '',
              expression: rule.expression || '',
              errorMessage: rule.errorMessage || '',
              severity: rule.severity || 'error',
              leftType: rule.leftType || 'form_field',
              leftValue: rule.leftValue || '',
              operator: rule.operator || '<=',
              rightType: rule.rightType || 'fixed',
              rightValue: rule.rightValue || ''
            }
          })
        }

        this.form = {
          id: Number(data.id),
          name: data.name || '',
          appId: data.appId || null,
          description: data.description || '',
          robotGroupIds: this.ensureArray(data.robotGroupIds),
          fields: fields,
          rules: rules,
          steps: steps
        }

        if (this.form.appId) {
          await this.onAppChange(this.form.appId)

          for (const step of this.form.steps) {
            if (step.apiId) {
              const oldMappings = step.paramMappings || []
              this.onApiChange(step)
              step.paramMappings.forEach(newMapping => {
                const oldMapping = oldMappings.find(o => o.paramName === newMapping.paramName)
                if (oldMapping) {
                  newMapping.sourceType = oldMapping.sourceType
                  newMapping.sourceValue = oldMapping.sourceValue
                }
              })
            }
          }
        }

        this.dialog.visible = true
      } catch (error) {
        this.$message.error('获取模板详情失败')
      } finally {
        this.submitLoading = false
      }
    },

    cancel() {
      this.dialog.visible = false
      this.resetForm()
    },

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
      if (!this.form.appId) {
        this.$message.warning('请先选择所属应用')
        return
      }
      this.form.steps.push({
        name: '',
        description: '',
        estimatedTime: 5,
        apiId: null,
        paramMappings: []
      })
    },

    removeStep(index) {
      this.form.steps.splice(index, 1)
    },

    submitForm() {
      this.$refs.formRef.validate(async (valid) => {
        if (!valid) return

        for (let i = 0; i < this.form.steps.length; i++) {
          const step = this.form.steps[i]
          if (!step.name) {
            this.$message.error(`第${i + 1}步：请填写步骤名称`)
            return
          }
          if (!step.apiId) {
            this.$message.error(`第${i + 1}步 [${step.name}]：请选择API`)
            return
          }
          if (step.paramMappings) {
            for (const mapping of step.paramMappings) {
              if (mapping.required && !mapping.sourceValue) {
                this.$message.error(`第${i + 1}步 [${step.name}]：参数"${mapping.paramLabel || mapping.paramName}"必填`)
                return
              }
            }
          }
        }

        const rulesForSubmit = []
        for (let i = 0; i < this.form.rules.length; i++) {
          const rule = this.form.rules[i]
          if (!rule.name) {
            this.$message.error(`第${i + 1}条约束规则：请填写规则名称`)
            return
          }
          const expr = this.buildRuleExpression(rule)
          if (!expr) {
            this.$message.error(`第${i + 1}条约束规则 [${rule.name}]：请完善表达式配置`)
            return
          }
          if (!rule.errorMessage) {
            this.$message.error(`第${i + 1}条约束规则 [${rule.name}]：请填写错误提示信息`)
            return
          }
          rulesForSubmit.push({
            name: rule.name,
            expression: expr,
            errorMessage: rule.errorMessage,
            severity: rule.severity
          })
        }

        this.submitLoading = true
        try {
          const stepsForSubmit = this.form.steps.map(step => ({
            name: step.name,
            description: step.description,
            estimatedTime: step.estimatedTime,
            apiId: step.apiId,
            paramMapping: step.paramMappings
          }))

          const dto = {
            name: this.form.name,
            description: this.form.description,
            appId: this.form.appId,
            robotGroupIds: this.form.robotGroupIds,
            formContent: JSON.stringify({fields: this.form.fields}),
            workflow: JSON.stringify({steps: stepsForSubmit}),
            rules: rulesForSubmit
          }

          if (this.dialog.mode === 'edit') {
            await updateTemplate(this.form.id, dto)
            this.$message.success('修改成功')
          } else {
            await addTemplate(dto)
            this.$message.success('新增成功')
          }
          this.dialog.visible = false
          this.getList()
        } catch (error) {
          this.$message.error(error.response?.data?.msg || '操作失败')
        } finally {
          this.submitLoading = false
        }
      })
    },

    async handleView(row) {
      try {
        const res = await getTemplate(row.id)
        const data = res.data

        const app = this.appList.find(a => a.id === data.appId)
        data.appName = data.appName || (app ? app.appName : '')

        this.viewDialog.data = data

        try {
          if (data.formContent) {
            const content = JSON.parse(data.formContent)
            this.viewDialog.fields = content.fields || []
          } else {
            this.viewDialog.fields = []
          }
          if (data.workflow) {
            const wf = JSON.parse(data.workflow)
            this.viewDialog.steps = wf.steps || []
            for (const step of this.viewDialog.steps) {
              if (step.apiId) {
                step.apiName = step.apiName || `API(${step.apiId})`
              }
            }
          } else {
            this.viewDialog.steps = []
          }
          this.viewDialog.rules = data.rules || []
        } catch (e) {
          this.viewDialog.fields = []
          this.viewDialog.steps = []
          this.viewDialog.rules = []
        }
        this.viewDialog.visible = true
      } catch (error) {
        this.$message.error('获取详情失败')
      }
    },

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

.fields-container, .steps-container, .rules-container {
  border: 1px dashed #dcdfe6;
  border-radius: 4px;
  padding: 15px;
  background: #fafafa;
  margin-bottom: 20px;
}

.field-item, .step-item, .rule-item {
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

.el-button [class*="fas"] {
  font-size: 14px;
}

.el-table .cell .el-button + .el-button {
  margin-left: 4px;
}

.step-tip, .rules-tip {
  font-size: 13px;
  color: #606266;
  line-height: 1.5;
  margin-bottom: 5px;
}

.compact-form-item {
  margin-bottom: 0;
}

.operation-desc {
  background: #f0f9ff;
  border-left: 3px solid #409eff;
  padding: 8px 12px;
  margin: 10px 0;
  border-radius: 0 4px 4px 0;
  color: #606266;
  font-size: 13px;
}

.param-mapping-section, .expression-builder {
  background: #fafafa;
  border: 1px dashed #dcdfe6;
  border-radius: 4px;
  padding: 12px;
  margin-top: 10px;
}

.section-title, .section-subtitle {
  font-size: 13px;
  font-weight: bold;
  color: #606266;
  margin-bottom: 10px;
  padding-bottom: 8px;
  border-bottom: 1px solid #ebeef5;
}

.section-subtitle {
  font-size: 12px;
  color: #909399;
  border-bottom: none;
  margin-bottom: 8px;
}

.step-item, .rule-item {
  margin-bottom: 15px;
}

.step-item:last-child, .rule-item:last-child {
  margin-bottom: 0;
}

.app-params-container {
  border: 1px solid #ebeef5;
  border-radius: 4px;
  padding: 15px;
  background: #f5f7fa;
  margin-bottom: 20px;
}

.expression-preview {
  margin-top: 10px;
  padding: 8px 12px;
  background: #f0f9ff;
  border-radius: 4px;
  font-size: 13px;
  color: #606266;
}

.expression-preview code {
  color: #409eff;
  font-weight: bold;
  margin-left: 5px;
}
</style>
