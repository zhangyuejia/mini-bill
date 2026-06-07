<template>
  <router-view />
</template>

<script setup>
</script>

<style>
:root {
  --primary: #409eff;
  --primary-dark: #337ecc;
  --primary-light: #ecf5ff;
  --success: #67c23a;
  --warning: #e6a23c;
  --danger: #f56c6c;
  --info: #909399;
  --text-primary: #303133;
  --text-regular: #606266;
  --text-secondary: #909399;
  --border-base: #dcdfe6;
  --border-light: #e4e7ed;
  --border-lighter: #ebeef5;
  --bg-base: #f0f2f5;
  --bg-card: #ffffff;
  --sidebar-bg: #1d1e1f;
  --sidebar-text: #bfcbd9;
  --sidebar-active: #409eff;
  --radius-sm: 6px;
  --radius-md: 8px;
  --radius-lg: 12px;
  --radius-xl: 16px;
  --shadow-sm: 0 1px 2px rgba(0,0,0,0.06);
  --shadow-md: 0 4px 12px rgba(0,0,0,0.08);
  --shadow-lg: 0 8px 24px rgba(0,0,0,0.12);
  --shadow-xl: 0 20px 60px rgba(0,0,0,0.15);
  --transition: all 0.25s cubic-bezier(0.4, 0, 0.2, 1);
}

* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

html, body, #app {
  height: 100%;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'PingFang SC', 'Microsoft YaHei', sans-serif;
  font-size: 14px;
  color: var(--text-primary);
  background: var(--bg-base);
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
}

/* Page container common */
.page-container {
  padding: 24px;
  max-width: 1400px;
  margin: 0 auto;

  @media (max-width: 768px) {
    padding: 16px;
  }
}

.page-title {
  font-size: 20px;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 24px;
  letter-spacing: 0.5px;
  position: relative;
  padding-left: 14px;

  &::before {
    content: '';
    position: absolute;
    left: 0;
    top: 50%;
    transform: translateY(-50%);
    width: 4px;
    height: 20px;
    background: var(--primary);
    border-radius: 2px;
  }
}

/* Card */
.card {
  background: var(--bg-card);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-sm);
  padding: 20px;
  transition: var(--transition);

  &:hover {
    box-shadow: var(--shadow-md);
  }
}

/* Query bar */
.query-bar {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 12px;
  margin-bottom: 20px;
  padding: 16px 20px;
  background: var(--bg-card);
  border-radius: var(--radius-md);
  box-shadow: var(--shadow-sm);
}

/* Table actions */
.table-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

/* Pagination */
.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

/* Empty tip */
.empty-tip {
  padding: 80px 0;
  display: flex;
  justify-content: center;
}

/* Dialog footer buttons gap */
.el-dialog__footer .el-button + .el-button {
  margin-left: 12px;
}

/* Table enhancements */
.el-table {
  border-radius: var(--radius-md);
  overflow: hidden;
  box-shadow: var(--shadow-sm);
}

.el-table th.el-table__cell {
  background-color: #f8f9fc !important;
  color: var(--text-regular);
  font-weight: 600;
  font-size: 13px;
}

.el-table--border .el-table__cell {
  border-color: var(--border-lighter);
}

/* Form enhancements */
.el-form-item__label {
  font-weight: 500;
  color: var(--text-regular);
}

/* Button enhancements */
.el-button--primary {
  --el-button-primary-text-color: #fff;
  --el-button-primary-bg-color: var(--primary);
  --el-button-primary-border-color: var(--primary);
}

.el-button--primary:hover {
  --el-button-primary-hover-bg-color: var(--primary-dark);
  --el-button-primary-hover-border-color: var(--primary-dark);
}

/* Tag styling */
.el-tag {
  border: none;
  font-weight: 500;
}

/* Dialog styling */
.el-dialog {
  border-radius: var(--radius-lg);
  overflow: hidden;
}

.el-dialog__header {
  padding: 20px 24px 0;
}

.el-dialog__body {
  padding: 20px 24px;
}

.el-dialog__footer {
  padding: 0 24px 20px;
}

/* Input focus */
.el-input__wrapper.is-focus {
  box-shadow: 0 0 0 1px var(--primary) inset !important;
}
</style>
