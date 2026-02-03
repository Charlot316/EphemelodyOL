module.exports = {
    outputDir: 'dist',
    assetsDir:'static',
    publicPath:'/',
    parallel: false,
    lintOnSave: false,
    devServer: {
        disableHostCheck: true  // 允许所有主机访问，解决 Cloudflare Tunnel 的 "Invalid Host header" 问题
    },
    transpileDependencies: [
      'vue-router',
      'element-plus'
    ],
    configureWebpack: {
      module: {
        rules: [
          {
            test: /\.mjs$/,
            include: /node_modules/,
            type: 'javascript/auto'
          }
        ]
      }
    }
}