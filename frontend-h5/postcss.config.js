import pxToViewport from 'postcss-px-to-viewport'

// 移动端适配：以 375 设计稿为基准做 vw 转换
export default {
  plugins: [
    pxToViewport({
      unitToConvert: 'px',
      viewportWidth: 375,
      unitPrecision: 5,
      propList: ['*'],
      viewportUnit: 'vw',
      fontViewportUnit: 'vw',
      selectorBlackList: ['ignore-px'],
      minPixelValue: 1,
      mediaQuery: false,
      replace: true,
      exclude: [],
      landscape: false
    })
  ]
}
