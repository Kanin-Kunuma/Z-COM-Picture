<template>
  <div id="userRegisterPage">
    <h2 class="title">新的柚子厨注册</h2>
    <div class="desc">企业级智能协同云图库</div>
    <a-form
      :model="formState"
      name="basic"
      label-align="left"
      autocomplete="off"
      @finish="handleSubmit"
      :label-col="{ span: 7 }"
    >
      <a-form-item
        name="userAccount"
        label="注册账号"
        :rules="[{ required: true, message: '请输入账号' }]"
      >
        <a-input v-model:value="formState.userAccount" placeholder="请输入账号" />
      </a-form-item>
      <a-form-item
        name="userPassword"
        label="注册密码"
        :rules="[
          { required: true, message: '请输入密码' },
          { min: 8, message: '密码不能小于 8 位' },
        ]"
      >
        <a-input-password v-model:value="formState.userPassword" placeholder="请输入密码" />
        <div v-if="formState.userPassword" class="password-strength">
          <div class="strength-bar">
            <a-progress
              :percent="passwordStrength.percent"
              :stroke-color="passwordStrength.color"
              :show-info="false"
              size="small"
            />
          </div>
          <div class="strength-text" :style="{ color: passwordStrength.color }">
            {{ passwordStrength.text }}
          </div>
        </div>
      </a-form-item>
      <a-form-item
        name="checkPassword"
        label="确认密码"
        :rules="[
          { required: true, message: '请输入确认密码' },
          { min: 8, message: '确认密码不能小于 8 位' },
        ]"
      >
        <a-input-password v-model:value="formState.checkPassword" placeholder="请输入确认密码" />
      </a-form-item>

      <a-form-item name="userAvatar" label="头像">
        <a-upload
          v-model:file-list="fileList"
          name="file"
          list-type="picture-card"
          class="avatar-uploader"
          :show-upload-list="false"
          action="/api/user/avatar/upload"
          :before-upload="beforeUpload"
          @change="handleChange"
        >
          <img v-if="imageUrl" :src="imageUrl" alt="avatar" />
          <div v-else>
            <loading-outlined v-if="loading"></loading-outlined>
            <plus-outlined v-else></plus-outlined>
            <div class="ant-upload-text">Upload</div>
          </div>
        </a-upload>
        <a-input
          v-model:value="formState.userAvatar"
          placeholder="若无原图，可上传原图的链接"
          style="margin-top: 8px"
        />
      </a-form-item>

      <div class="tips">
        已有账号？
        <RouterLink to="/user/login">去登录</RouterLink>
      </div>
      <a-form-item style="text-align: right">
        <a-button type="primary" html-type="submit">注册</a-button>
      </a-form-item>
    </a-form>
  </div>
</template>

<script lang="ts" setup>
import { computed, reactive, ref } from 'vue'
import { userRegisterUsingPost } from '@/api/userController'
import type { UploadChangeParam, UploadProps } from 'ant-design-vue'
import { message } from 'ant-design-vue'
import { useRouter } from 'vue-router'
import { LoadingOutlined, PlusOutlined } from '@ant-design/icons-vue'

const passwordStrength = computed(() => {
  const password = formState.userPassword
  if (!password) {
    return { percent: 0, color: '#d9d9d9', text: '' }
  }
  let score = 0
  if (password.length >= 8) score += 20
  if (password.length >= 12) score += 10
  if (/[a-z]/.test(password)) score += 20
  if (/[A-Z]/.test(password)) score += 20
  if (/\d/.test(password)) score += 20
  if (/[!@#$%^&*()_+\-=\[\]{};':"\\|,.<>\/?]/.test(password)) score += 10
  let color, text
  if (score < 40) {
    color = '#ff4d4f'
    text = 'Week'
  } else if (score < 70) {
    color = '#faad14'
    text = 'Medium'
  } else {
    color = '#52c41a'
    text = 'Strong'
  }
  return {
    percent: Math.min(score, 100),
    color,
    text: text,
  }
})

const formState = reactive<API.UserRegisterRequest>({
  userAccount: '',
  userPassword: '',
  checkPassword: '',
  userAvatar: ''
})

const router = useRouter()
const fileList = ref([])
const loading = ref<boolean>(false)
const imageUrl = ref<string>('')

const beforeUpload = (file: File) => {
  const isImage = file.type.startsWith('image/')
  if (!isImage) {
    message.error('只能上传图片文件！')
    return false
  }
  const isLt2M = file.size / 1024 / 1024 < 2
  if (!isLt2M) {
    message.error('图片不能超过2MB！')
    return false
  }
  return isImage && isLt2M
}

const handleChange = (info: UploadChangeParam) => {
  if (info.file.status === 'uploading') {
    loading.value = true
    return
  }
  if (info.file.status === 'done') {
    const res = info.file.response
    if (res && res.code === 0 && res.data) {
      imageUrl.value = res.data // 只做预览，不赋值formState.userAvatar
      message.success('头像上传成功！')
    } else {
      message.error('头像上传失败！')
    }
    loading.value = false
  }
  if (info.file.status === 'error') {
    loading.value = false
    message.error('头像上传失败！')
  }
}

const handleSubmit = async (values: any) => {
  if (formState.userPassword !== formState.checkPassword) {
    message.error('两次输入的密码不一致')
    return
  }
  // 优先用用户手动输入的URL，否则用上传图片的URL
  if (!formState.userAvatar && imageUrl.value) {
    values.userAvatar = imageUrl.value
  }
  const res = await userRegisterUsingPost(values)
  if (res.data.code === 0 && res.data.data) {
    message.success('注册成功')
    router.push({
      path: '/user/login',
      replace: true,
    })
  } else {
    message.error('注册失败，' + res.data.message)
  }
}
</script>

<style scoped>
#userRegisterPage {
  max-width: 360px;
  margin: 60px auto 0 auto;
  display: flex;
  flex-direction: column;
  align-items: center;
}
.title {
  text-align: center;
  margin-bottom: 16px;
}
.desc {
  text-align: center;
  color: #bbb;
  margin-bottom: 16px;
}
.tips {
  color: #bbb;
  text-align: right;
  font-size: 13px;
  margin-bottom: 16px;
}
:deep(.ant-input),
:deep(.ant-input-password) {
  width: 260px;
  text-align: left;
  margin: 0 auto;
}
:deep(.ant-form-item) {
  display: flex;
  flex-direction: column;
  align-items: center;
}
:deep(.ant-btn) {
  width: 260px !important;
}
.avatar-uploader > .ant-upload {
  width: 80px;
  height: 80px;
}
.avatar-uploader > .ant-upload img {
  width: 80px;
  height: 80px;
  object-fit: cover;
  border-radius: 8px;
}
.ant-upload-select-picture-card i {
  font-size: 32px;
  color: #999;
}
.ant-upload-select-picture-card .ant-upload-text {
  margin-top: 8px;
  color: #666;
}
</style>
