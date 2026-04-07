<template>
  <div class="card">
    <div class="card-header">
      <div class="card-title">
        <i class="fas fa-handshake"></i> 业务接待 · 话术配置
      </div>
      <div class="robot-selector">
        <span class="badge"><i class="fas fa-robot"></i> 导览机器人：</span>
        <el-select v-model="selectedRobotId" placeholder="请选择机器人" style="width:200px;" @change="loadConfig">
          <el-option v-for="r in robotList" :key="r.id" :label="r.name" :value="r.id" />
        </el-select>
      </div>
    </div>

    <div class="card-body">
      <!-- 基础话术 -->
      <div class="form-group">
        <label>首次进入欢迎语：<span class="word-limit">最多300个字</span></label>
        <el-input type="text" v-model="config.welcome" placeholder="请问关于公司：有哪些需要了解的？" maxlength="300" show-word-limit />
      </div>

      <div class="form-group">
        <label>重复唤醒播报语：<span class="word-limit">最多300个字</span></label>
        <el-input type="text" v-model="config.repeat" placeholder="您好" maxlength="300" show-word-limit />
      </div>

      <div class="form-group">
        <label>未唤醒状态播报语：<span class="word-limit">最多300个字</span></label>
        <div class="variable-container">
          <el-input type="text" v-model="config.idle" placeholder="真正的智能服务机器人 #机器人名称# 即将为您服务" maxlength="300" show-word-limit />
          <div class="inline-variables">
            <el-button size="mini" type="info" plain @click="insertVar('idle','#机器人名称#')">
              <i class="fas fa-tag"></i> 机器人名称
            </el-button>
            <el-button size="mini" type="danger" plain @click="removeTag('idle')">
              <i class="fas fa-trash-alt"></i> 删除标签
            </el-button>
          </div>
        </div>
      </div>

      <!-- VIP识别配置 -->
      <div class="config-section">
        <div class="section-header">
          <strong>VIP识别：</strong>
          <el-radio-group v-model="vipEnabled">
            <el-radio :label="'1'">开启</el-radio>
            <el-radio :label="'0'">关闭</el-radio>
          </el-radio-group>
        </div>

        <div v-if="vipEnabled === '1'" class="sub-config">
          <div class="form-group">
            <label>VIP识别语：<span class="word-limit">最多300个字</span></label>
            <div class="variable-container">
              <el-input type="text" v-model="config.vipGreeting" placeholder="尊敬的 #全名# #职称# 您好" maxlength="300" show-word-limit />
              <div class="inline-variables">
                <el-button size="mini" type="info" plain @click="insertVar('vipGreeting','#全名#')">
                  <i class="fas fa-tag"></i> 全名
                </el-button>
                <el-button size="mini" type="info" plain @click="insertVar('vipGreeting','#职称#')">
                  <i class="fas fa-tag"></i> 职称
                </el-button>
                <el-button size="mini" type="danger" plain @click="removeTag('vipGreeting')">
                  <i class="fas fa-trash-alt"></i> 删除标签
                </el-button>
              </div>
            </div>
          </div>
          <div class="form-group">
            <label>多VIP识别语：<span class="word-limit">最多20个字</span></label>
            <el-input type="text" v-model="config.vipMulti" placeholder="尊敬的贵宾们您们好" maxlength="20" show-word-limit />
          </div>
        </div>
      </div>

      <!-- 陌生人识别配置 -->
      <div class="config-section">
        <div class="section-header">
          <strong>陌生人识别：</strong>
          <el-radio-group v-model="strangerEnabled">
            <el-radio :label="'1'">开启</el-radio>
            <el-radio :label="'0'">关闭</el-radio>
          </el-radio-group>
        </div>

        <div v-if="strangerEnabled === '1'" class="sub-config">
          <div class="form-group">
            <label>陌生人识别语：<span class="word-limit">最多300个字</span></label>
            <div class="variable-container">
              <el-input type="text" v-model="config.strangerGreeting" placeholder="#性别# 您好" maxlength="300" show-word-limit />
              <div class="inline-variables">
                <el-button size="mini" type="info" plain @click="insertVar('strangerGreeting','#性别#')">
                  <i class="fas fa-tag"></i> 性别
                </el-button>
                <el-button size="mini" type="danger" plain @click="removeTag('strangerGreeting')">
                  <i class="fas fa-trash-alt"></i> 删除标签
                </el-button>
              </div>
            </div>
          </div>
          <div class="form-group">
            <label>多性别陌生人识别语：<span class="word-limit">最多20个字</span></label>
            <el-input type="text" v-model="config.strangerMulti" placeholder="先生女士们好" maxlength="20" show-word-limit />
          </div>
        </div>
      </div>

      <!-- 操作按钮 -->
      <div class="footer-actions">
        <el-button @click="resetConfig"><i class="fas fa-undo"></i> 重置</el-button>
        <el-button type="primary" @click="saveConfig"><i class="fas fa-check"></i> 确定</el-button>
      </div>
    </div>
  </div>
</template>

<script>
import { getReceptionConfig, saveReceptionConfig, resetReceptionConfig } from "@/api/func/reception";
import { listRobot } from "@/api/mode/robot";

export default {
  name: "Reception",
  data() {
    return {
      selectedRobotId: null,
      robotList: [],
      vipEnabled: '1',
      strangerEnabled: '1',
      config: {
        welcome: '',
        repeat: '',
        idle: '',
        vipGreeting: '',
        vipMulti: '',
        strangerGreeting: '',
        strangerMulti: ''
      },
      defaultConfig: {
        welcome: '请问关于公司：有哪些需要了解的？',
        repeat: '您好',
        idle: '真正的智能服务机器人 #机器人名称# 即将为您服务',
        vipGreeting: '尊敬的 #全名# #职称# 您好',
        vipMulti: '尊敬的贵宾们您们好',
        strangerGreeting: '#性别# 您好',
        strangerMulti: '先生女士们好'
      }
    };
  },
  created() {
    // 直接加载导览组（groupId=4）下的机器人
    this.loadRobotsByGroup();
  },
  methods: {
    // 加载导览组机器人（groupId=4）
    loadRobotsByGroup() {
      listRobot({ groupId: 4, pageNum: 1, pageSize: 100 }).then(response => {
        this.robotList = response.rows || [];

        if (this.robotList.length > 0) {
          this.selectedRobotId = this.robotList[0].id;
          this.loadConfig();
        } else {
          this.$message.warning('导览组下没有可用机器人');
        }
      }).catch(error => {
        console.error('获取机器人列表失败:', error);
        this.$message.error('获取机器人列表失败');
      });
    },

    loadConfig() {
      if (!this.selectedRobotId) return;
      getReceptionConfig(this.selectedRobotId).then(response => {
        if (response.data) {
          this.config = response.data;
          this.vipEnabled = response.data.vipEnabled || '1';
          this.strangerEnabled = response.data.strangerEnabled || '1';
        }
      }).catch(error => {
        console.error('加载配置失败:', error);
      });
    },

    insertVar(field, variable) {
      this.config[field] += variable;
    },

    removeTag(field) {
      const val = this.config[field];
      const match = val.match(/#[^#]+#/g);
      if (match && match.length) {
        this.config[field] = val.replace(match[match.length-1], '').trim();
        this.$message.info('已移除最后一个标签');
      } else {
        this.$message.info('没有可删除的标签');
      }
    },

    resetConfig() {
      if (!this.selectedRobotId) {
        this.$message.warning('请先选择机器人');
        return;
      }
      this.$confirm('确定要重置为默认话术吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        resetReceptionConfig(this.selectedRobotId).then(() => {
          this.config = { ...this.defaultConfig };
          this.vipEnabled = '1';
          this.strangerEnabled = '1';
          this.$message.success('重置成功');
        }).catch(error => {
          console.error('重置失败:', error);
          this.$message.error('重置失败');
        });
      }).catch(() => {});
    },

    saveConfig() {
      if (!this.selectedRobotId) {
        this.$message.warning('请先选择机器人');
        return;
      }
      const data = {
        robotId: this.selectedRobotId,
        ...this.config,
        vipEnabled: this.vipEnabled,
        strangerEnabled: this.strangerEnabled
      };
      saveReceptionConfig(data).then(() => {
        this.$message.success('配置保存成功');
      }).catch(error => {
        console.error('保存失败:', error);
        this.$message.error('保存失败');
      });
    }
  }
};
</script>

<style scoped>
/* 样式保持不变 */
.card {
  background: white;
  border-radius: 10px;
  box-shadow: 0px 2px 2px rgba(0, 0, 0, 0.5);
  margin-bottom: 24px;
  overflow: hidden;
  position: relative;
  z-index: 1;
}

.card-header {
  padding: 16px 20px;
  border-bottom: 1px solid var(--border-light, #E5E7EB);
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 15px;
}

.card-title {
  font-size: 14px;
  font-weight: 600;
  color: #000;
  display: flex;
  align-items: center;
  gap: 8px;
}

.card-title i {
  color: var(--primary-blue, #3976E4);
}

.card-body {
  padding: 24px 20px;
}

.robot-selector {
  display: flex;
  align-items: center;
  background: #f9fafc;
  padding: 4px 4px 4px 16px;
  border-radius: 40px;
  border: 1px solid #eaeef5;
}

.badge {
  background: #e6f7ff;
  color: var(--primary-blue, #3976E4);
  padding: 5px 14px;
  border-radius: 30px;
  font-size: 13px;
  font-weight: 500;
  white-space: nowrap;
  margin-right: 8px;
}

/* 表单样式 */
.form-group {
  margin-bottom: 28px;
}

.form-group label {
  display: block;
  font-weight: 500;
  color: #1e2a3a;
  margin-bottom: 10px;
  font-size: 15px;
}

.word-limit {
  font-size: 13px;
  color: #8a9bb5;
  float: right;
  font-weight: normal;
  background: #f5f7fa;
  padding: 2px 10px;
  border-radius: 16px;
}

.variable-container {
  position: relative;
}

.inline-variables {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-top: 12px;
}

.inline-variables .el-button--mini {
  padding: 7px 14px;
  font-size: 13px;
}

/* 配置区块样式 */
.config-section {
  background: #ffffff;
  border-radius: 16px;
  padding: 24px;
  margin-bottom: 28px;
  border: 1px solid #eef2f8;
  box-shadow: 0 2px 8px rgba(0,0,0,0.02);
}

.section-header {
  display: flex;
  align-items: center;
  gap: 35px;
  margin-bottom: 6px;
}

.section-header strong {
  font-size: 16px;
  color: #1e2a3a;
  min-width: 100px;
}

.sub-config {
  margin-top: 24px;
  padding-left: 28px;
  border-left: 3px solid var(--primary-blue, #3976E4);
  background: #fafcff;
  padding: 20px 20px 20px 28px;
  border-radius: 0 16px 16px 0;
}

/* 操作按钮 */
.footer-actions {
  display: flex;
  justify-content: flex-end;
  gap: 18px;
  margin-top: 40px;
  padding-top: 28px;
  border-top: 1px solid #e5eaf2;
}

.footer-actions .el-button {
  padding: 12px 30px;
  font-size: 15px;
  border-radius: 30px;
}

/* 输入框样式调整 */
.el-input--medium .el-input__inner {
  height: 44px;
  line-height: 44px;
}

.el-textarea .el-textarea__inner {
  padding: 12px 15px;
}

/* 响应式调整 */
@media (max-width: 768px) {
  .card-header {
    flex-direction: column;
    align-items: flex-start;
  }

  .robot-selector {
    width: 100%;
    justify-content: space-between;
  }

  .section-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
  }

  .sub-config {
    padding-left: 15px;
  }

  .footer-actions {
    flex-direction: column;
  }

  .footer-actions .el-button {
    width: 100%;
  }
}
</style>
