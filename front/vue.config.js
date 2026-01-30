module.exports = {
    outputDir: 'dist',
    assetsDir:'static',
    publicPath:'/',
    parallel: false,
    lintOnSave: false,
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