<template>
  <div class="app-container">
    <el-row :gutter="12">
      <el-col :span="8">
        <el-card shadow="never">
          <div slot="header">
            <span>机器人请求模拟</span>
          </div>
          <el-form :model="form" label-width="110px">
            <el-form-item label="robotId">
              <el-select v-model="form.robotId" placeholder="请选择机器人" filterable style="width: 100%">
                <el-option
                  v-for="item in robotOptions"
                  :key="item.id"
                  :label="robotLabel(item)"
                  :value="String(item.id)"
                />
              </el-select>
            </el-form-item>
            <el-form-item label="conversationId">
              <el-input v-model="form.conversationId" placeholder="为空表示新对话" />
            </el-form-item>
            <el-form-item label="query">
              <el-input v-model="form.query" type="textarea" :rows="6" placeholder="输入用户消息" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" :loading="streaming" @click="send">发送并流式接收</el-button>
              <el-button :disabled="!streaming" @click="stop">停止</el-button>
              <el-button @click="clearAll">清空</el-button>
            </el-form-item>
            <el-form-item label="接口">
              <el-input :value="endpoint" readonly />
            </el-form-item>
          </el-form>
        </el-card>
      </el-col>
      <el-col :span="16">
        <el-card shadow="never" class="chat-card">
          <div slot="header" class="chat-header">
            <span>流式输出</span>
            <span class="status" :class="{ on: streaming }">{{ streaming ? 'Streaming' : 'Idle' }}</span>
          </div>
          <div class="chat-body" ref="chatBody">
            <div v-for="(m, idx) in messages" :key="idx" class="msg" :class="m.role">
              <div class="meta">{{ m.role === 'user' ? 'User' : 'Assistant' }}</div>
              <div class="content">{{ m.content }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script>
import { getToken } from "@/utils/auth"
import { listRobots } from "@/api/robots/robots"

export default {
  name: "RobotChat",
  data() {
    return {
      form: {
        robotId: "",
        conversationId: "",
        query: ""
      },
      streaming: false,
      abortController: null,
      messages: [],
      robotOptions: []
    }
  },
  created() {
    this.loadRobots()
  },
  computed: {
    endpoint() {
      return process.env.VUE_APP_BASE_API + "/qa/chat/robot/chat"
    }
  },
  methods: {
    loadRobots() {
      listRobots({ pageNum: 1, pageSize: 1000 }).then(response => {
        this.robotOptions = response.rows || []
        if (!this.form.robotId && this.robotOptions.length > 0) {
          this.form.robotId = String(this.robotOptions[0].id)
        }
      })
    },
    async send() {
      if (this.streaming) return
      if (!this.form.robotId) {
        this.$message.error("robotId 不能为空")
        return
      }
      if (!this.form.query) {
        this.$message.error("query 不能为空")
        return
      }

      const userText = String(this.form.query)
      this.messages.push({ role: "user", content: userText })
      const assistant = { role: "assistant", content: "" }
      this.messages.push(assistant)

      this.streaming = true
      this.abortController = new AbortController()

      try {
        const headers = {
          "Content-Type": "application/json",
          "Accept": "text/event-stream"
        }
        const token = getToken()
        if (token) {
          headers["Authorization"] = "Bearer " + token
        }

        const resp = await fetch(this.endpoint, {
          method: "POST",
          headers,
          body: JSON.stringify({
            robotId: this.form.robotId,
            conversationId: this.form.conversationId || "",
            query: this.form.query
          }),
          signal: this.abortController.signal
        })

        if (!resp.ok) {
          const text = await resp.text()
          throw new Error("HTTP " + resp.status + ": " + text)
        }
        if (!resp.body) {
          throw new Error("浏览器不支持流式读取 response.body")
        }

        const reader = resp.body.getReader()
        const decoder = new TextDecoder("utf-8")
        let buffer = ""

        const handleChunk = (chunk) => {
          const lines = String(chunk).split("\n")
          for (let i = 0; i < lines.length; i++) {
            const line = lines[i]
            if (!line || !line.startsWith("data:")) continue
            const raw = line.slice(5).trim()
            if (!raw) continue
            try {
              const obj = JSON.parse(raw)
              if (obj && typeof obj.answer === "string") {
                assistant.content += obj.answer
              } else if (obj && obj.event === "error") {
                assistant.content += "\n[ERROR] " + (obj.message || "")
              }
            } catch (e) {
              assistant.content += raw
            }
          }
          this.$nextTick(() => this.scrollToBottom())
        }

        const drainBuffer = (final) => {
          let idx
          while ((idx = buffer.indexOf("\n\n")) !== -1) {
            const part = buffer.slice(0, idx)
            buffer = buffer.slice(idx + 2)
            if (part.trim()) handleChunk(part)
          }
          if (final && buffer.trim()) {
            handleChunk(buffer)
            buffer = ""
          }
        }

        while (true) {
          const { value, done } = await reader.read()
          if (done) break
          buffer += decoder.decode(value, { stream: true })
          drainBuffer(false)
        }

        buffer += decoder.decode()
        drainBuffer(true)
      } catch (e) {
        const msg = e && e.name === "AbortError" ? "已停止" : (e && e.message ? e.message : String(e))
        assistant.content += "\n[ERROR] " + msg
      } finally {
        this.streaming = false
        this.abortController = null
        this.form.query = ""
        this.$nextTick(() => this.scrollToBottom())
      }
    },
    stop() {
      if (this.abortController) {
        this.abortController.abort()
      }
    },
    clearAll() {
      this.stop()
      this.messages = []
    },
    robotLabel(item) {
      const name = item.name || ("机器人" + item.id)
      return item.code ? `${name}（${item.code}）` : name
    },
    scrollToBottom() {
      const el = this.$refs.chatBody
      if (!el) return
      el.scrollTop = el.scrollHeight
    }
  }
}
</script>

<style scoped>
.chat-card .chat-body {
  height: calc(100vh - 210px);
  overflow: auto;
  background: #0f172a;
  color: #e2e8f0;
  padding: 12px;
  border-radius: 4px;
  white-space: pre-wrap;
}
.msg {
  margin-bottom: 12px;
}
.msg .meta {
  font-size: 12px;
  opacity: 0.9;
  margin-bottom: 4px;
}
.msg.user .meta {
  color: #93c5fd;
}
.msg.assistant .meta {
  color: #a7f3d0;
}
.msg .content {
  line-height: 1.6;
}
.chat-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.status {
  font-size: 12px;
  color: #909399;
}
.status.on {
  color: #67c23a;
}
</style>
