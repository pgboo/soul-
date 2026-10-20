<script setup>
import { ref, computed, onUnmounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { showToast } from 'vant'
import { authApi } from '@/api'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const phone = ref('')
const code = ref('')
const loading = ref(false)

// 登录 / 注册 模式切换
// 后端在手机号首次验证通过时会自动创建账号（AuthServiceImpl.registerNewUser），
// 因此注册与登录复用同一接口，仅交互文案不同。
const mode = ref('login')
const isRegister = computed(() => mode.value === 'register')
const submitText = computed(() => {
  if (loading.value) return isRegister.value ? '注册中...' : '登录中...'
  return isRegister.value ? '注册并进入 Soul' : '进入 Soul'
})
function switchMode(m) {
  if (loading.value || mode.value === m) return
  mode.value = m
}

/** 跳服务器设置页（内测期公网地址会变，给用户一条自助修复的路） */
function goServerSetting() {
  router.push({ name: 'server-setting', query: { redirect: '/login' } })
}

// 验证码倒计时
const countdown = ref(0)
let timer = null

const phoneValid = computed(() => /^1\d{10}$/.test(phone.value))
const canSubmit = computed(() => phoneValid.value && code.value.length > 0)
const canSendCode = computed(() => phoneValid.value && countdown.value === 0)

// ---- 动漫背景装饰元素（纯视觉，不影响逻辑）----
const stars = Array.from({ length: 28 }, (_, i) => ({
  id: 'star-' + i,
  left: Math.random() * 100,
  top: Math.random() * 58,
  size: 1.5 + Math.random() * 2.5,
  delay: Math.random() * 4,
  duration: 2 + Math.random() * 3
}))

const meteors = Array.from({ length: 3 }, (_, i) => ({
  id: 'meteor-' + i,
  top: 6 + i * 13,
  delay: 1.5 + i * 5.5
}))

const petals = Array.from({ length: 12 }, (_, i) => ({
  id: 'petal-' + i,
  left: Math.random() * 100,
  size: 8 + Math.random() * 6,
  delay: Math.random() * 8,
  duration: 9 + Math.random() * 6,
  opacity: 0.45 + Math.random() * 0.4
}))

const sparkles = [
  { id: 'sp1', left: '-14px', top: '6px', delay: 0 },
  { id: 'sp2', right: '-10px', top: '22px', delay: 1.2 },
  { id: 'sp3', left: '4px', bottom: '-8px', delay: 2.1 }
]

const spirits = [
  { id: 'ghost-1', cls: 'spirit-left', delay: 0 },
  { id: 'ghost-2', cls: 'spirit-right', delay: 1.6 }
]

/** 发送验证码 */
async function handleSendCode() {
  if (!canSendCode.value) return
  try {
    await authApi.sendCode(phone.value)
    showToast('验证码已发送，开发模式为 123456')
    startCountdown()
  } catch (e) {
    // 错误已由拦截器提示
  }
}

function startCountdown() {
  countdown.value = 60
  timer = setInterval(() => {
    countdown.value -= 1
    if (countdown.value <= 0) {
      clearInterval(timer)
      timer = null
    }
  }, 1000)
}

/** 登录 / 注册（新手机号自动注册） */
async function handleLogin() {
  if (!canSubmit.value || loading.value) return
  const registering = isRegister.value
  loading.value = true
  try {
    const data = await authApi.login(phone.value, code.value)
    userStore.setLogin(data)
    showToast(registering ? '注册成功，欢迎加入 Soul' : '登录成功')
    try {
      await userStore.fetchInitStatus()
    } catch (e) {
      // 忽略，使用登录返回的状态
    }
    const redirect = route.query.redirect
    if (userStore.nextStep !== 'square') {
      router.replace({ name: userStore.nextStep })
    } else if (redirect && typeof redirect === 'string') {
      router.replace(redirect)
    } else {
      router.replace({ name: 'square' })
    }
  } catch (e) {
    // 错误已由拦截器提示
  } finally {
    loading.value = false
  }
}

onUnmounted(() => {
  if (timer) clearInterval(timer)
})
</script>

<template>
  <div class="login-page">
    <div class="login-bg">
      <div class="nebula nebula-1"></div>
      <div class="nebula nebula-2"></div>
      <div class="nebula nebula-3"></div>
      <div class="moon"></div>

      <span
        v-for="s in stars"
        :key="s.id"
        class="star"
        :style="{
          left: s.left + '%',
          top: s.top + '%',
          width: s.size + 'px',
          height: s.size + 'px',
          animationDelay: s.delay + 's',
          animationDuration: s.duration + 's'
        }"
      ></span>

      <span
        v-for="m in meteors"
        :key="m.id"
        class="meteor"
        :style="{ top: m.top + '%', animationDelay: m.delay + 's' }"
      ></span>

      <span
        v-for="p in petals"
        :key="p.id"
        class="petal"
        :style="{
          left: p.left + '%',
          width: p.size + 'px',
          height: p.size * 0.82 + 'px',
          animationDelay: p.delay + 's',
          animationDuration: p.duration + 's',
          opacity: p.opacity
        }"
      ></span>
    </div>

    <div
      v-for="g in spirits"
      :key="g.id"
      class="spirit"
      :class="g.cls"
      :style="{ animationDelay: g.delay + 's' }"
    >
      <i class="eye eye-l"></i>
      <i class="eye eye-r"></i>
      <i class="cheek cheek-l"></i>
      <i class="cheek cheek-r"></i>
      <i class="mouth"></i>
    </div>

    <div class="login-content">
      <div class="brand">
        <div class="logo-wrap">
          <div class="logo-halo"></div>
          <div class="logo">🪐</div>
          <span
            v-for="sp in sparkles"
            :key="sp.id"
            class="sparkle"
            :style="{
              left: sp.left,
              right: sp.right,
              top: sp.top,
              bottom: sp.bottom,
              animationDelay: sp.delay + 's'
            }"
          >✦</span>
        </div>
        <h1 class="brand-title">Soul</h1>
        <p class="brand-slogan">遇见那个懂你的灵魂</p>
      </div>

      <div class="login-card">
        <div class="mode-tabs">
          <button
            class="mode-tab"
            :class="{ active: mode === 'login' }"
            type="button"
            @click="switchMode('login')"
          >登录</button>
          <button
            class="mode-tab"
            :class="{ active: mode === 'register' }"
            type="button"
            @click="switchMode('register')"
          >注册</button>
          <span class="mode-indicator" :class="{ right: isRegister }"></span>
        </div>

        <div class="field">
          <input
            v-model="phone"
            class="field-input"
            type="tel"
            maxlength="11"
            placeholder="请输入手机号"
          />
        </div>
        <div class="field field-code">
          <input
            v-model="code"
            class="field-input"
            type="tel"
            maxlength="6"
            placeholder="请输入验证码"
          />
          <button
            class="code-btn"
            :class="{ disabled: !canSendCode }"
            :disabled="!canSendCode"
            @click="handleSendCode"
          >
            {{ countdown > 0 ? countdown + 's' : '获取验证码' }}
          </button>
        </div>

        <button
          class="soul-btn login-btn"
          :disabled="!canSubmit || loading"
          @click="handleLogin"
        >
          {{ submitText }}
        </button>

        <p class="login-tip">
          {{ isRegister ? '新手机号验证通过即自动创建账号' : '未注册手机号将自动创建账号' }}
        </p>
      </div>

      <p class="agreement">
        {{ isRegister ? '注册' : '登录' }}即代表同意《用户协议》与《隐私政策》
      </p>

      <p class="server-entry" @click="goServerSetting">连不上服务器？点这里改地址</p>
    </div>
  </div>
</template>

<style scoped lang="scss">
.login-page {
  min-height: 100vh;
  position: relative;
  overflow: hidden;
  background: linear-gradient(180deg, #171038 0%, #2a1b52 34%, #4a2364 66%, #1d3262 100%);
  background-size: 100% 220%;
}
@keyframes bgDrift {
  from { background-position: 0 0; }
  to { background-position: 0 100%; }
}
/* 背景动画必须放在子元素上：组件根节点挂无限动画会让
   App.vue 的 <transition> 一直等待动画结束，路由跳转会卡住 */
.login-bg {
  position: absolute;
  inset: 0;
  overflow: hidden;
  background: linear-gradient(180deg, #171038 0%, #2a1b52 34%, #4a2364 66%, #1d3262 100%);
  background-size: 100% 220%;
  animation: bgDrift 14s ease-in-out infinite alternate;
}
.nebula { position: absolute; border-radius: 50%; filter: blur(60px); }
.nebula-1 {
  width: 300px; height: 300px;
  background: rgba(150, 96, 255, 0.5);
  top: -70px; left: -80px;
  animation: nebulaFloat 11s ease-in-out infinite alternate;
}
.nebula-2 {
  width: 260px; height: 260px;
  background: rgba(255, 111, 165, 0.38);
  top: 110px; right: -90px;
  animation: nebulaFloat 13s ease-in-out infinite alternate-reverse;
}
.nebula-3 {
  width: 240px; height: 240px;
  background: rgba(96, 180, 255, 0.32);
  bottom: 30px; left: -60px;
  animation: nebulaFloat 15s ease-in-out infinite alternate;
}
@keyframes nebulaFloat {
  from { transform: translate3d(0, 0, 0) scale(1); }
  to { transform: translate3d(24px, -18px, 0) scale(1.12); }
}
.moon {
  position: absolute;
  top: 46px; right: 42px;
  width: 58px; height: 58px;
  border-radius: 50%;
  background: radial-gradient(circle at 34% 34%, #fff8e3 0%, #ffe9b8 55%, #f7cf7f 100%);
  box-shadow: 0 0 24px rgba(255, 232, 170, 0.75), 0 0 70px rgba(255, 214, 140, 0.4);
  animation: moonGlow 4s ease-in-out infinite alternate;
}
.moon::after {
  content: '';
  position: absolute;
  top: 12px; left: 14px;
  width: 12px; height: 12px;
  border-radius: 50%;
  background: rgba(230, 190, 120, 0.35);
  box-shadow: 16px 14px 0 -3px rgba(230, 190, 120, 0.3), 6px 24px 0 -4px rgba(230, 190, 120, 0.28);
}
@keyframes moonGlow {
  from { box-shadow: 0 0 24px rgba(255, 232, 170, 0.75), 0 0 70px rgba(255, 214, 140, 0.4); }
  to { box-shadow: 0 0 34px rgba(255, 232, 170, 0.95), 0 0 100px rgba(255, 214, 140, 0.55); }
}
.star {
  position: absolute;
  border-radius: 50%;
  background: #fff;
  box-shadow: 0 0 6px rgba(255, 255, 255, 0.9);
  animation: twinkle linear infinite;
}
@keyframes twinkle {
  0%, 100% { opacity: 0.15; transform: scale(0.7); }
  50% { opacity: 1; transform: scale(1.25); }
}
.meteor {
  position: absolute;
  right: -130px;
  width: 130px; height: 2px;
  border-radius: 2px;
  background: linear-gradient(90deg, rgba(255, 255, 255, 0.95), rgba(255, 255, 255, 0));
  opacity: 0;
  animation: meteorFly 11s linear infinite;
}
@keyframes meteorFly {
  0% { transform: translate3d(0, 0, 0) rotate(150deg); opacity: 0; }
  2% { opacity: 1; }
  10% { transform: translate3d(-72vw, 44vh, 0) rotate(150deg); opacity: 0; }
  100% { transform: translate3d(-72vw, 44vh, 0) rotate(150deg); opacity: 0; }
}
.petal {
  position: absolute;
  top: -4vh;
  background: linear-gradient(135deg, #ffd9ec 0%, #ff9ecb 100%);
  border-radius: 85% 0 85% 0;
  box-shadow: 0 0 6px rgba(255, 158, 203, 0.35);
  animation: petalFall linear infinite;
}
@keyframes petalFall {
  0% { transform: translate3d(0, -5vh, 0) rotate(12deg); }
  25% { transform: translate3d(4vw, 25vh, 0) rotate(130deg); }
  50% { transform: translate3d(-2.5vw, 52vh, 0) rotate(215deg); }
  75% { transform: translate3d(3vw, 78vh, 0) rotate(310deg); }
  100% { transform: translate3d(-1.5vw, 106vh, 0) rotate(395deg); }
}
.spirit {
  position: absolute;
  z-index: 1;
  width: 46px; height: 54px;
  background: linear-gradient(180deg, #ffffff 0%, #efe8ff 100%);
  border-radius: 50% 50% 42% 42% / 58% 58% 38% 38%;
  box-shadow: 0 8px 22px rgba(124, 92, 255, 0.4);
  animation: spiritFloat 4.5s ease-in-out infinite;
}
.spirit::after {
  content: '';
  position: absolute;
  left: 0; right: 0; bottom: -5px;
  height: 11px;
  background: radial-gradient(circle at 8px 0, #efe8ff 8px, transparent 8.5px);
  background-size: 16px 11px;
  background-position: left bottom;
  background-repeat: repeat-x;
}
.spirit-left { left: 7%; bottom: 19vh; transform: rotate(-6deg); }
.spirit-right { right: 8%; top: 30vh; width: 38px; height: 45px; animation-duration: 5.4s; }
@keyframes spiritFloat {
  0%, 100% { transform: translateY(0) rotate(-4deg); }
  50% { transform: translateY(-14px) rotate(5deg); }
}
.eye {
  position: absolute;
  top: 38%;
  width: 6px; height: 8px;
  border-radius: 50%;
  background: #3d2b63;
}
.eye-l { left: 11px; }
.eye-r { right: 11px; }
.cheek {
  position: absolute;
  top: 54%;
  width: 8px; height: 5px;
  border-radius: 50%;
  background: #ffb3d1;
  opacity: 0.85;
}
.cheek-l { left: 5px; }
.cheek-r { right: 5px; }
.mouth {
  position: absolute;
  left: 50%; top: 50%;
  width: 8px; height: 5px;
  transform: translateX(-50%);
  border-bottom: 2px solid #3d2b63;
  border-radius: 0 0 8px 8px;
}
.login-content {
  position: relative;
  z-index: 2;
  padding: 0 28px;
  padding-top: 13vh;
}
.brand {
  text-align: center;
  margin-bottom: 32px;
  animation: fadeUp 0.8s ease-out both;
}
.logo-wrap {
  position: relative;
  width: 96px; height: 96px;
  margin: 0 auto 12px;
}
.logo-halo {
  position: absolute;
  inset: -14px;
  border-radius: 50%;
  background: radial-gradient(circle, rgba(255, 190, 222, 0.5) 0%, rgba(150, 96, 255, 0.25) 45%, transparent 70%);
  animation: haloPulse 3.2s ease-in-out infinite;
}
@keyframes haloPulse {
  0%, 100% { transform: scale(1); opacity: 0.8; }
  50% { transform: scale(1.16); opacity: 1; }
}
.logo {
  position: absolute;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 58px;
  margin: 0;
  filter: drop-shadow(0 6px 14px rgba(255, 170, 90, 0.45));
  animation: planetFloat 4s ease-in-out infinite;
}
@keyframes planetFloat {
  0%, 100% { transform: translateY(0) rotate(-8deg); }
  50% { transform: translateY(-9px) rotate(8deg); }
}
.sparkle {
  position: absolute;
  color: #ffe9a8;
  font-size: 15px;
  text-shadow: 0 0 8px rgba(255, 233, 168, 0.9);
  animation: twinkle 2.4s linear infinite;
}
.brand-title {
  font-size: 42px;
  font-weight: 800;
  margin: 0;
  letter-spacing: 3px;
  background: linear-gradient(90deg, #d3b3ff 0%, #ff9ecb 30%, #8fd4ff 60%, #d3b3ff 100%);
  background-size: 300% 100%;
  -webkit-background-clip: text;
  background-clip: text;
  color: transparent;
  animation: gradientMove 6s linear infinite;
}
@keyframes gradientMove {
  from { background-position: 0% 50%; }
  to { background-position: 100% 50%; }
}
.brand-slogan {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.78);
  margin: 10px 0 0;
  letter-spacing: 3px;
  text-shadow: 0 0 14px rgba(211, 179, 255, 0.6);
}
@keyframes fadeUp {
  from { opacity: 0; transform: translateY(18px); }
  to { opacity: 1; transform: translateY(0); }
}
.login-card {
  background: rgba(255, 255, 255, 0.92);
  backdrop-filter: blur(18px);
  border-radius: 26px;
  padding: 22px 22px 26px;
  box-shadow: 0 20px 55px rgba(14, 6, 46, 0.5), 0 0 0 1px rgba(255, 255, 255, 0.55) inset;
  animation: cardIn 0.9s cubic-bezier(0.22, 1.35, 0.36, 1) 0.3s both;
}
@keyframes cardIn {
  from { opacity: 0; transform: translateY(34px) scale(0.94); }
  to { opacity: 1; transform: translateY(0) scale(1); }
}
.mode-tabs {
  position: relative;
  display: flex;
  background: #f0ebfa;
  border-radius: 14px;
  padding: 4px;
  margin-bottom: 20px;
}
.mode-tab {
  position: relative;
  z-index: 2;
  flex: 1;
  height: 40px;
  border: none;
  background: transparent;
  border-radius: 11px;
  font-size: 14px;
  font-weight: 600;
  color: var(--soul-text-sub);
  transition: color 0.25s;
  &.active { color: #fff; }
}
.mode-indicator {
  position: absolute;
  z-index: 1;
  top: 4px; left: 4px;
  width: calc(50% - 4px);
  height: 40px;
  border-radius: 11px;
  background: linear-gradient(90deg, #9660ff, #ff6fa5);
  box-shadow: 0 4px 14px rgba(150, 96, 255, 0.35);
  transition: transform 0.3s cubic-bezier(0.22, 1.2, 0.36, 1);
  &.right { transform: translateX(100%); }
}
.field { margin-bottom: 16px; }
.field-input {
  width: 100%;
  height: 48px;
  border: 1.5px solid transparent;
  outline: none;
  background: #f4f1fb;
  border-radius: 14px;
  padding: 0 16px;
  font-size: 15px;
  color: var(--soul-text);
  transition: border-color 0.25s, box-shadow 0.25s, background 0.25s;
  &::placeholder { color: var(--soul-text-hint); }
  &:focus {
    border-color: #b18cff;
    background: #fbf9ff;
    box-shadow: 0 0 0 4px rgba(150, 96, 255, 0.16), 0 0 18px rgba(150, 96, 255, 0.25);
  }
}
.field-code {
  display: flex;
  gap: 10px;
  align-items: center;
  .field-input { flex: 1; }
}
.code-btn {
  flex-shrink: 0;
  height: 48px;
  padding: 0 14px;
  border: none;
  border-radius: 14px;
  background: linear-gradient(135deg, rgba(150, 96, 255, 0.16), rgba(255, 158, 203, 0.2));
  color: var(--soul-primary);
  font-size: 13px;
  font-weight: 600;
  transition: transform 0.15s, box-shadow 0.25s;
  &:active { transform: scale(0.95); }
  &:not(.disabled) {
    box-shadow: 0 4px 14px rgba(150, 96, 255, 0.22);
    animation: codeBtnGlow 2.6s ease-in-out infinite;
  }
  &.disabled {
    color: var(--soul-text-hint);
    background: #f4f1fb;
    animation: none;
    box-shadow: none;
  }
}
@keyframes codeBtnGlow {
  0%, 100% { box-shadow: 0 4px 14px rgba(150, 96, 255, 0.18); }
  50% { box-shadow: 0 4px 20px rgba(255, 158, 203, 0.4); }
}
.login-btn {
  margin-top: 8px;
  position: relative;
  overflow: hidden;
  background: linear-gradient(90deg, #9660ff 0%, #ff6fa5 50%, #9660ff 100%);
  background-size: 200% 100%;
  animation: btnGradient 4s linear infinite;
  transition: transform 0.15s;
  &:not(:disabled):active { transform: scale(0.97); }
  &::after {
    content: '';
    position: absolute;
    top: 0; left: -70%;
    width: 45%; height: 100%;
    background: linear-gradient(100deg, transparent, rgba(255, 255, 255, 0.55), transparent);
    transform: skewX(-20deg);
    animation: shineSweep 2.8s ease-in-out infinite;
  }
  &:disabled { opacity: 0.55; }
}
@keyframes btnGradient {
  from { background-position: 0% 50%; }
  to { background-position: 200% 50%; }
}
@keyframes shineSweep {
  0% { left: -70%; }
  55%, 100% { left: 130%; }
}
.login-tip {
  text-align: center;
  font-size: 12px;
  color: var(--soul-text-hint);
  margin: 16px 0 0;
}
.agreement {
  position: fixed;
  bottom: 24px;
  left: 0; right: 0;
  text-align: center;
  font-size: 12px;
  color: rgba(255, 255, 255, 0.55);
  z-index: 2;
}
/* 服务器设置入口：内测期公网地址会变，让用户能自助改，不必等重新发包 */
.server-entry {
  position: fixed;
  bottom: 48px;
  left: 0; right: 0;
  text-align: center;
  font-size: 12px;
  color: rgba(255, 255, 255, 0.42);
  text-decoration: underline;
  text-underline-offset: 3px;
  z-index: 2;
  cursor: pointer;

  &:active {
    color: rgba(255, 255, 255, 0.75);
  }
}
@media (prefers-reduced-motion: reduce) {
  .login-bg, .nebula, .moon, .star, .meteor, .petal, .spirit,
  .logo, .logo-halo, .sparkle, .brand-title, .login-card, .brand,
  .login-btn, .login-btn::after, .code-btn {
    animation: none !important;
  }
  .meteor, .petal { display: none; }
}
</style>
