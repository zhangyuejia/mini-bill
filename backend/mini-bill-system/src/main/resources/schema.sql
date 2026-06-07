-- =============================================
-- mini-bill-system 数据库初始化脚本
-- =============================================

-- 用户表
CREATE TABLE IF NOT EXISTS sys_user (
    id BIGINT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(100),
    nickname VARCHAR(50),
    avatar VARCHAR(500),
    status INTEGER DEFAULT 0,
    remark VARCHAR(500),
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    create_by BIGINT DEFAULT 0,
    update_by BIGINT DEFAULT 0,
    del_flag INTEGER DEFAULT 0
);

-- 角色表
CREATE TABLE IF NOT EXISTS sys_role (
    id BIGINT PRIMARY KEY,
    name VARCHAR(50) NOT NULL,
    code VARCHAR(50) NOT NULL UNIQUE,
    sort INTEGER DEFAULT 0,
    status INTEGER DEFAULT 0,
    remark VARCHAR(500),
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    create_by BIGINT DEFAULT 0,
    update_by BIGINT DEFAULT 0,
    del_flag INTEGER DEFAULT 0
);

-- 菜单表
CREATE TABLE IF NOT EXISTS sys_menu (
    id BIGINT PRIMARY KEY,
    name VARCHAR(50) NOT NULL,
    path VARCHAR(200),
    component VARCHAR(200),
    permission VARCHAR(100),
    type INTEGER DEFAULT 1,
    parent_id BIGINT DEFAULT 0,
    sort INTEGER DEFAULT 0,
    icon VARCHAR(100),
    visible INTEGER DEFAULT 0,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    create_by BIGINT DEFAULT 0,
    update_by BIGINT DEFAULT 0,
    del_flag INTEGER DEFAULT 0
);

-- 用户角色关联表
CREATE TABLE IF NOT EXISTS sys_user_role (
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, role_id)
);

-- 角色菜单关联表
CREATE TABLE IF NOT EXISTS sys_role_menu (
    role_id BIGINT NOT NULL,
    menu_id BIGINT NOT NULL,
    PRIMARY KEY (role_id, menu_id)
);

-- 字典类型表
CREATE TABLE IF NOT EXISTS sys_dict_type (
    id BIGINT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    code VARCHAR(100) NOT NULL UNIQUE,
    status INTEGER DEFAULT 0,
    remark VARCHAR(500),
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    create_by BIGINT DEFAULT 0,
    update_by BIGINT DEFAULT 0,
    del_flag INTEGER DEFAULT 0
);

-- 字典数据表
CREATE TABLE IF NOT EXISTS sys_dict_data (
    id BIGINT PRIMARY KEY,
    dict_type_id BIGINT NOT NULL,
    label VARCHAR(100) NOT NULL,
    value VARCHAR(100) NOT NULL,
    sort INTEGER DEFAULT 0,
    status INTEGER DEFAULT 0,
    remark VARCHAR(500),
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    create_by BIGINT DEFAULT 0,
    update_by BIGINT DEFAULT 0,
    del_flag INTEGER DEFAULT 0
);

-- 初始化数据
INSERT INTO sys_role (id, name, code, sort, status) VALUES (1, '超级管理员', 'ADMIN', 1, 0);
INSERT INTO sys_role (id, name, code, sort, status) VALUES (2, '普通用户', 'USER', 2, 0);

-- 默认超级管理员（密码：admin123）
INSERT INTO sys_user (id, username, password, nickname, email, status)
VALUES (1, 'admin', '$2b$10$81n1/uJDgtPqK6wnJkjyJeFNWzintD2F83rQ0JVoroam4Oxb4eyke', '管理员', 'admin@minibill.com', 0);

INSERT INTO sys_user_role (user_id, role_id) VALUES (1, 1);

-- =============================================
-- 表/字段注释
-- =============================================
COMMENT ON TABLE sys_user IS '系统用户表';
COMMENT ON COLUMN sys_user.id IS '主键ID';
COMMENT ON COLUMN sys_user.username IS '用户名';
COMMENT ON COLUMN sys_user.password IS '密码（BCrypt加密）';
COMMENT ON COLUMN sys_user.email IS '邮箱';
COMMENT ON COLUMN sys_user.nickname IS '昵称';
COMMENT ON COLUMN sys_user.avatar IS '头像URL';
COMMENT ON COLUMN sys_user.status IS '状态 0-正常 1-禁用';
COMMENT ON COLUMN sys_user.remark IS '备注';
COMMENT ON COLUMN sys_user.create_time IS '创建时间';
COMMENT ON COLUMN sys_user.update_time IS '更新时间';
COMMENT ON COLUMN sys_user.create_by IS '创建人ID';
COMMENT ON COLUMN sys_user.update_by IS '更新人ID';
COMMENT ON COLUMN sys_user.del_flag IS '逻辑删除 0-正常 1-已删';

COMMENT ON TABLE sys_role IS '系统角色表';
COMMENT ON COLUMN sys_role.id IS '主键ID';
COMMENT ON COLUMN sys_role.name IS '角色名称';
COMMENT ON COLUMN sys_role.code IS '角色编码';
COMMENT ON COLUMN sys_role.sort IS '排序号';
COMMENT ON COLUMN sys_role.status IS '状态 0-正常 1-禁用';
COMMENT ON COLUMN sys_role.remark IS '备注';

COMMENT ON TABLE sys_menu IS '系统菜单表';
COMMENT ON COLUMN sys_menu.id IS '主键ID';
COMMENT ON COLUMN sys_menu.name IS '菜单名称';
COMMENT ON COLUMN sys_menu.path IS '路由路径';
COMMENT ON COLUMN sys_menu.component IS '组件路径';
COMMENT ON COLUMN sys_menu.permission IS '权限标识';
COMMENT ON COLUMN sys_menu.type IS '类型 0-目录 1-菜单 2-按钮';
COMMENT ON COLUMN sys_menu.parent_id IS '父菜单ID';
COMMENT ON COLUMN sys_menu.sort IS '排序号';
COMMENT ON COLUMN sys_menu.icon IS '图标名称';
COMMENT ON COLUMN sys_menu.visible IS '显示状态 0-显示 1-隐藏';

COMMENT ON TABLE sys_user_role IS '用户角色关联表';
COMMENT ON COLUMN sys_user_role.user_id IS '用户ID';
COMMENT ON COLUMN sys_user_role.role_id IS '角色ID';

COMMENT ON TABLE sys_role_menu IS '角色菜单关联表';
COMMENT ON COLUMN sys_role_menu.role_id IS '角色ID';
COMMENT ON COLUMN sys_role_menu.menu_id IS '菜单ID';

COMMENT ON TABLE sys_dict_type IS '字典类型表';
COMMENT ON COLUMN sys_dict_type.id IS '主键ID';
COMMENT ON COLUMN sys_dict_type.name IS '字典名称';
COMMENT ON COLUMN sys_dict_type.code IS '字典编码';
COMMENT ON COLUMN sys_dict_type.status IS '状态 0-正常 1-禁用';
COMMENT ON COLUMN sys_dict_type.remark IS '备注';

COMMENT ON TABLE sys_dict_data IS '字典数据表';
COMMENT ON COLUMN sys_dict_data.id IS '主键ID';
COMMENT ON COLUMN sys_dict_data.dict_type_id IS '字典类型ID';
COMMENT ON COLUMN sys_dict_data.label IS '字典标签（显示值）';
COMMENT ON COLUMN sys_dict_data.value IS '字典键值（实际值）';
COMMENT ON COLUMN sys_dict_data.sort IS '排序号';
COMMENT ON COLUMN sys_dict_data.status IS '状态 0-正常 1-禁用';
COMMENT ON COLUMN sys_dict_data.remark IS '备注';

-- =============================================
-- 菜单数据
-- =============================================
INSERT INTO sys_menu (id, name, path, component, permission, type, parent_id, sort, icon, visible) VALUES
(1, '系统管理', NULL, NULL, NULL, 0, 0, 1, 'Setting', 0) ON CONFLICT (id) DO NOTHING;

INSERT INTO sys_menu (id, name, path, component, permission, type, parent_id, sort, icon, visible) VALUES
(2, '用户管理', 'user', 'views/admin/UserManagement.vue', 'system:user:list', 1, 1, 1, 'User', 0) ON CONFLICT (id) DO NOTHING;

INSERT INTO sys_menu (id, name, path, component, permission, type, parent_id, sort, icon, visible) VALUES
(3, '角色管理', 'role', 'views/admin/RoleManagement.vue', 'system:role:list', 1, 1, 2, 'Avatar', 0) ON CONFLICT (id) DO NOTHING;

INSERT INTO sys_menu (id, name, path, component, permission, type, parent_id, sort, icon, visible) VALUES
(4, '菜单管理', 'menu', 'views/admin/MenuManagement.vue', 'system:menu:list', 1, 1, 3, 'Menu', 0) ON CONFLICT (id) DO NOTHING;

INSERT INTO sys_menu (id, name, path, component, permission, type, parent_id, sort, icon, visible) VALUES
(5, '字典管理', 'dict', 'views/admin/DictManagement.vue', 'system:dict:list', 1, 1, 4, 'Collection', 0) ON CONFLICT (id) DO NOTHING;

-- 一级目录：家庭管理
INSERT INTO sys_menu (id, name, path, component, permission, type, parent_id, sort, icon, visible) VALUES
(10, '家庭管理', NULL, NULL, NULL, 0, 0, 2, 'HomeFilled', 0) ON CONFLICT (id) DO NOTHING;

INSERT INTO sys_menu (id, name, path, component, permission, type, parent_id, sort, icon, visible) VALUES
(11, '家庭管理', 'family', 'views/business/family/FamilyManagement.vue', 'bus:family:list', 1, 10, 1, 'HomeFilled', 0) ON CONFLICT (id) DO NOTHING;

INSERT INTO sys_menu (id, name, path, component, permission, type, parent_id, sort, icon, visible) VALUES
(12, '住址管理', 'address', 'views/business/address/AddressManagement.vue', 'bus:address:list', 1, 10, 2, 'MapLocation', 0) ON CONFLICT (id) DO NOTHING;

INSERT INTO sys_menu (id, name, path, component, permission, type, parent_id, sort, icon, visible) VALUES
(14, '物件管理', 'item', 'views/business/item/ItemManagement.vue', 'bus:item:list', 1, 10, 3, 'Goods', 0) ON CONFLICT (id) DO NOTHING;

-- 一级目录：账单管理
INSERT INTO sys_menu (id, name, path, component, permission, type, parent_id, sort, icon, visible) VALUES
(20, '账单管理', NULL, NULL, NULL, 0, 0, 3, 'Ticket', 0) ON CONFLICT (id) DO NOTHING;

INSERT INTO sys_menu (id, name, path, component, permission, type, parent_id, sort, icon, visible) VALUES
(13, '房租水电', 'bill', 'views/business/bill/BillManagement.vue', 'bus:bill:list', 1, 20, 1, 'Ticket', 0) ON CONFLICT (id) DO NOTHING;

INSERT INTO sys_menu (id, name, path, component, permission, type, parent_id, sort, icon, visible) VALUES
(15, '物件费用', 'item-cost', 'views/business/item/ItemCostManagement.vue', 'bus:itemcost:list', 1, 20, 2, 'Coin', 0) ON CONFLICT (id) DO NOTHING;

-- 一级目录：财富管理
INSERT INTO sys_menu (id, name, path, component, permission, type, parent_id, sort, icon, visible) VALUES
(30, '财富管理', NULL, NULL, NULL, 0, 0, 4, 'Wallet', 0) ON CONFLICT (id) DO NOTHING;

INSERT INTO sys_menu (id, name, path, component, permission, type, parent_id, sort, icon, visible) VALUES
(16, '储蓄项管理', 'saving-item', 'views/business/saving/SavingItemManagement.vue', 'bus:savingitem:list', 1, 30, 1, 'Coin', 0) ON CONFLICT (id) DO NOTHING;

INSERT INTO sys_menu (id, name, path, component, permission, type, parent_id, sort, icon, visible) VALUES
(40, '家庭储蓄', 'saving', 'views/business/saving/SavingManagement.vue', 'bus:saving:list', 1, 30, 2, 'Wallet', 0) ON CONFLICT (id) DO NOTHING;

-- ADMIN 角色拥有所有菜单
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 1, id FROM sys_menu ON CONFLICT DO NOTHING;

-- USER 角色拥有业务菜单（排除系统管理及其子菜单）
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 2, id FROM sys_menu WHERE id NOT IN (1, 2, 3, 4, 5) ON CONFLICT DO NOTHING;

-- =============================================
-- 字典类型数据
-- =============================================
INSERT INTO sys_dict_type (id, name, code, status, remark) VALUES
(1, '物件类型', 'item_type', 0, '物件管理的类型分类，如家用电器、电子设备、交通工具等') ON CONFLICT (id) DO NOTHING;

INSERT INTO sys_dict_type (id, name, code, status, remark) VALUES
(2, '省市区', 'region', 0, '地址级联选择：省-市-区') ON CONFLICT (id) DO NOTHING;

-- 字典数据：物件类型
INSERT INTO sys_dict_data (id, dict_type_id, label, value, sort, status) VALUES
(1, 1, '家用电器', 'home_appliance', 1, 0) ON CONFLICT (id) DO NOTHING;

INSERT INTO sys_dict_data (id, dict_type_id, label, value, sort, status) VALUES
(2, 1, '电子设备', 'electronic', 2, 0) ON CONFLICT (id) DO NOTHING;

INSERT INTO sys_dict_data (id, dict_type_id, label, value, sort, status) VALUES
(3, 1, '交通工具', 'vehicle', 3, 0) ON CONFLICT (id) DO NOTHING;

INSERT INTO sys_dict_data (id, dict_type_id, label, value, sort, status) VALUES
(4, 1, '家具', 'furniture', 4, 0) ON CONFLICT (id) DO NOTHING;

INSERT INTO sys_dict_data (id, dict_type_id, label, value, sort, status) VALUES
(5, 1, '其他', 'other', 99, 0) ON CONFLICT (id) DO NOTHING;
