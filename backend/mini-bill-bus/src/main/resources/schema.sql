-- =============================================
-- mini-bill-bus 数据库初始化脚本
-- =============================================

-- 家庭表
CREATE TABLE IF NOT EXISTS bus_family (
    id BIGINT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    owner_id BIGINT NOT NULL,
    default_flag INTEGER DEFAULT 0,
    status INTEGER DEFAULT 0,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    create_by BIGINT DEFAULT 0,
    update_by BIGINT DEFAULT 0,
    del_flag INTEGER DEFAULT 0
);

-- 家庭成员表
CREATE TABLE IF NOT EXISTS bus_family_member (
    id BIGINT PRIMARY KEY,
    family_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    role VARCHAR(20) DEFAULT 'member',
    status INTEGER DEFAULT 0,
    join_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 住址表
CREATE TABLE IF NOT EXISTS bus_address (
    id BIGINT PRIMARY KEY,
    family_id BIGINT NOT NULL,
    name VARCHAR(100),
    province VARCHAR(50),
    city VARCHAR(50),
    district VARCHAR(50),
    street_number VARCHAR(200),
    address_image VARCHAR(500),
    default_rent DECIMAL(10,2),
    default_electric_price DECIMAL(10,2),
    default_water_price DECIMAL(10,2),
    status INTEGER DEFAULT 0,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    create_by BIGINT DEFAULT 0,
    update_by BIGINT DEFAULT 0,
    del_flag INTEGER DEFAULT 0
);

-- 账单表
CREATE TABLE IF NOT EXISTS bus_bill (
    id BIGINT PRIMARY KEY,
    family_id BIGINT NOT NULL,
    address_id BIGINT NOT NULL,
    period INTEGER,
    rent DECIMAL(10,2),
    water_prev_reading DECIMAL(10,2),
    water_curr_reading DECIMAL(10,2),
    water_unit_price DECIMAL(10,2),
    water_amount DECIMAL(10,2),
    electric_prev_reading DECIMAL(10,2),
    electric_curr_reading DECIMAL(10,2),
    electric_unit_price DECIMAL(10,2),
    electric_amount DECIMAL(10,2),
    other_fee DECIMAL(10,2),
    total_amount DECIMAL(10,2),
    remark TEXT,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    create_by BIGINT DEFAULT 0,
    update_by BIGINT DEFAULT 0,
    del_flag INTEGER DEFAULT 0
);

-- 账单附件表
CREATE TABLE IF NOT EXISTS bus_bill_attachment (
    id BIGINT PRIMARY KEY,
    bill_id BIGINT NOT NULL,
    file_name VARCHAR(255),
    file_url VARCHAR(500),
    file_size BIGINT
);

-- 物件表
CREATE TABLE IF NOT EXISTS bus_item (
    id BIGINT PRIMARY KEY,
    family_id BIGINT NOT NULL,
    address_id BIGINT NOT NULL,
    name VARCHAR(100) NOT NULL,
    type VARCHAR(50),
    purchase_amount DECIMAL(10,2),
    purchase_date DATE,
    deactivation_date DATE,
    remark TEXT,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    create_by BIGINT DEFAULT 0,
    update_by BIGINT DEFAULT 0,
    del_flag INTEGER DEFAULT 0
);

-- 物件附件表
CREATE TABLE IF NOT EXISTS bus_item_attachment (
    id BIGINT PRIMARY KEY,
    item_id BIGINT NOT NULL,
    file_name VARCHAR(255),
    file_url VARCHAR(500),
    file_size BIGINT
);

-- 物件费用表
CREATE TABLE IF NOT EXISTS bus_item_cost (
    id BIGINT PRIMARY KEY,
    family_id BIGINT NOT NULL,
    address_id BIGINT NOT NULL,
    item_id BIGINT NOT NULL,
    cost_date DATE,
    mileage DECIMAL(10,2),
    cost DECIMAL(10,2) NOT NULL,
    remark TEXT,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    create_by BIGINT DEFAULT 0,
    update_by BIGINT DEFAULT 0,
    del_flag INTEGER DEFAULT 0
);

-- 物件费用附件表
CREATE TABLE IF NOT EXISTS bus_item_cost_attachment (
    id BIGINT PRIMARY KEY,
    cost_id BIGINT NOT NULL,
    file_name VARCHAR(255),
    file_url VARCHAR(500),
    file_size BIGINT
);

-- 储蓄项表
CREATE TABLE IF NOT EXISTS bus_saving_item (
    id BIGINT PRIMARY KEY,
    family_id BIGINT NOT NULL,
    member_id BIGINT NOT NULL,
    name VARCHAR(100) NOT NULL,
    status INTEGER DEFAULT 0,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    create_by BIGINT DEFAULT 0,
    update_by BIGINT DEFAULT 0,
    del_flag INTEGER DEFAULT 0
);

-- 家庭储蓄表
CREATE TABLE IF NOT EXISTS bus_family_saving (
    id BIGINT PRIMARY KEY,
    family_id BIGINT NOT NULL,
    saving_date DATE,
    total_amount DECIMAL(10,2) DEFAULT 0,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    create_by BIGINT DEFAULT 0,
    update_by BIGINT DEFAULT 0,
    del_flag INTEGER DEFAULT 0
);

-- 储蓄记录表
CREATE TABLE IF NOT EXISTS bus_saving_record (
    id BIGINT PRIMARY KEY,
    saving_id BIGINT NOT NULL,
    saving_item_id BIGINT NOT NULL,
    member_id BIGINT NOT NULL,
    amount DECIMAL(10,2)
);

-- =============================================
-- 表/字段注释
-- =============================================
COMMENT ON TABLE bus_family IS '家庭表';
COMMENT ON COLUMN bus_family.id IS '主键ID';
COMMENT ON COLUMN bus_family.name IS '家庭名称';
COMMENT ON COLUMN bus_family.owner_id IS '一家之主（用户ID）';
COMMENT ON COLUMN bus_family.default_flag IS '是否默认家庭 0-非默认 1-默认';
COMMENT ON COLUMN bus_family.status IS '状态 0-正常 1-解散';

COMMENT ON TABLE bus_family_member IS '家庭成员表';
COMMENT ON COLUMN bus_family_member.id IS '主键ID';
COMMENT ON COLUMN bus_family_member.family_id IS '家庭ID';
COMMENT ON COLUMN bus_family_member.user_id IS '用户ID';
COMMENT ON COLUMN bus_family_member.role IS '角色 owner-户主 member-成员';
COMMENT ON COLUMN bus_family_member.status IS '状态 0-正常 1-已退出';
COMMENT ON COLUMN bus_family_member.join_time IS '加入时间';

COMMENT ON TABLE bus_address IS '住址表';
COMMENT ON COLUMN bus_address.id IS '主键ID';
COMMENT ON COLUMN bus_address.family_id IS '家庭ID';
COMMENT ON COLUMN bus_address.name IS '住址名称';
COMMENT ON COLUMN bus_address.province IS '省';
COMMENT ON COLUMN bus_address.city IS '市';
COMMENT ON COLUMN bus_address.district IS '区';
COMMENT ON COLUMN bus_address.street_number IS '门牌号';
COMMENT ON COLUMN bus_address.address_image IS '地址图片URL';
COMMENT ON COLUMN bus_address.default_rent IS '默认房租';
COMMENT ON COLUMN bus_address.default_electric_price IS '电费默认单价';
COMMENT ON COLUMN bus_address.default_water_price IS '水费默认单价';
COMMENT ON COLUMN bus_address.status IS '状态 0-正常 1-禁用';

COMMENT ON TABLE bus_bill IS '房租水电账单表';
COMMENT ON COLUMN bus_bill.id IS '主键ID';
COMMENT ON COLUMN bus_bill.family_id IS '家庭ID';
COMMENT ON COLUMN bus_bill.address_id IS '住址ID';
COMMENT ON COLUMN bus_bill.period IS '账期 如202605表示2026年05月';
COMMENT ON COLUMN bus_bill.rent IS '房租';
COMMENT ON COLUMN bus_bill.water_prev_reading IS '水费-上月表底';
COMMENT ON COLUMN bus_bill.water_curr_reading IS '水费-本月表底';
COMMENT ON COLUMN bus_bill.water_unit_price IS '水费-单价';
COMMENT ON COLUMN bus_bill.water_amount IS '水费-金额';
COMMENT ON COLUMN bus_bill.electric_prev_reading IS '电费-上月表底';
COMMENT ON COLUMN bus_bill.electric_curr_reading IS '电费-本月表底';
COMMENT ON COLUMN bus_bill.electric_unit_price IS '电费-单价';
COMMENT ON COLUMN bus_bill.electric_amount IS '电费-金额';
COMMENT ON COLUMN bus_bill.other_fee IS '其他费用';
COMMENT ON COLUMN bus_bill.total_amount IS '合计金额';
COMMENT ON COLUMN bus_bill.remark IS '备注';

COMMENT ON TABLE bus_bill_attachment IS '账单附件表';
COMMENT ON COLUMN bus_bill_attachment.id IS '主键ID';
COMMENT ON COLUMN bus_bill_attachment.bill_id IS '账单ID';
COMMENT ON COLUMN bus_bill_attachment.file_name IS '文件名';
COMMENT ON COLUMN bus_bill_attachment.file_url IS '文件URL';
COMMENT ON COLUMN bus_bill_attachment.file_size IS '文件大小（字节）';

COMMENT ON TABLE bus_item IS '物件表';
COMMENT ON COLUMN bus_item.id IS '主键ID';
COMMENT ON COLUMN bus_item.family_id IS '家庭ID';
COMMENT ON COLUMN bus_item.address_id IS '住址ID';
COMMENT ON COLUMN bus_item.name IS '物件名称';
COMMENT ON COLUMN bus_item.type IS '物件类型（字典编码 item_type）';
COMMENT ON COLUMN bus_item.purchase_amount IS '购买金额';
COMMENT ON COLUMN bus_item.deactivation_date IS '停用时间';
COMMENT ON COLUMN bus_item.purchase_date IS '购买日期';
COMMENT ON COLUMN bus_item.remark IS '备注';

COMMENT ON TABLE bus_item_attachment IS '物件附件表';
COMMENT ON COLUMN bus_item_attachment.id IS '主键ID';
COMMENT ON COLUMN bus_item_attachment.item_id IS '物件ID';
COMMENT ON COLUMN bus_item_attachment.file_name IS '文件名';
COMMENT ON COLUMN bus_item_attachment.file_url IS '文件URL';
COMMENT ON COLUMN bus_item_attachment.file_size IS '文件大小（字节）';

COMMENT ON TABLE bus_item_cost IS '物件费用表';
COMMENT ON COLUMN bus_item_cost.id IS '主键ID';
COMMENT ON COLUMN bus_item_cost.family_id IS '家庭ID';
COMMENT ON COLUMN bus_item_cost.address_id IS '住址ID';
COMMENT ON COLUMN bus_item_cost.item_id IS '物件ID';
COMMENT ON COLUMN bus_item_cost.cost_date IS '费用日期';
COMMENT ON COLUMN bus_item_cost.mileage IS '里程（交通工具时填写）';
COMMENT ON COLUMN bus_item_cost.cost IS '费用金额';
COMMENT ON COLUMN bus_item_cost.remark IS '备注';

COMMENT ON TABLE bus_item_cost_attachment IS '物件费用附件表';
COMMENT ON COLUMN bus_item_cost_attachment.id IS '主键ID';
COMMENT ON COLUMN bus_item_cost_attachment.cost_id IS '物件费用ID';
COMMENT ON COLUMN bus_item_cost_attachment.file_name IS '文件名';
COMMENT ON COLUMN bus_item_cost_attachment.file_url IS '文件URL';
COMMENT ON COLUMN bus_item_cost_attachment.file_size IS '文件大小（字节）';

COMMENT ON TABLE bus_saving_item IS '储蓄项表';
COMMENT ON COLUMN bus_saving_item.id IS '主键ID';
COMMENT ON COLUMN bus_saving_item.family_id IS '家庭ID';
COMMENT ON COLUMN bus_saving_item.member_id IS '成员ID（用户ID）';
COMMENT ON COLUMN bus_saving_item.name IS '储蓄项名称';
COMMENT ON COLUMN bus_saving_item.status IS '状态 0-正常 1-停用';

COMMENT ON TABLE bus_family_saving IS '家庭储蓄表';
COMMENT ON COLUMN bus_family_saving.id IS '主键ID';
COMMENT ON COLUMN bus_family_saving.family_id IS '家庭ID';
COMMENT ON COLUMN bus_family_saving.saving_date IS '储蓄日期';
COMMENT ON COLUMN bus_family_saving.total_amount IS '合计金额';

COMMENT ON TABLE bus_saving_record IS '储蓄记录明细表';
COMMENT ON COLUMN bus_saving_record.id IS '主键ID';
COMMENT ON COLUMN bus_saving_record.saving_id IS '家庭储蓄ID';
COMMENT ON COLUMN bus_saving_record.saving_item_id IS '储蓄项ID';
COMMENT ON COLUMN bus_saving_record.member_id IS '成员ID';
COMMENT ON COLUMN bus_saving_record.amount IS '金额';

-- =============================================
-- 索引
-- =============================================
CREATE UNIQUE INDEX IF NOT EXISTS idx_uq_bus_family_owner_name ON bus_family(owner_id, name);
CREATE UNIQUE INDEX IF NOT EXISTS idx_uq_bus_family_member ON bus_family_member(family_id, user_id);
CREATE UNIQUE INDEX IF NOT EXISTS idx_uq_bus_address_family_name ON bus_address(family_id, name);
CREATE UNIQUE INDEX IF NOT EXISTS idx_uq_bus_bill_family_addr_period ON bus_bill(family_id, address_id, period);
CREATE INDEX IF NOT EXISTS idx_bus_bill_attachment_bill_id ON bus_bill_attachment(bill_id);
CREATE UNIQUE INDEX IF NOT EXISTS idx_uq_bus_item_family_addr_name ON bus_item(family_id, address_id, name);
CREATE INDEX IF NOT EXISTS idx_bus_item_attachment_item_id ON bus_item_attachment(item_id);
CREATE INDEX IF NOT EXISTS idx_bus_item_cost_family_addr_item ON bus_item_cost(family_id, address_id, item_id);
CREATE INDEX IF NOT EXISTS idx_bus_item_cost_att_cost_id ON bus_item_cost_attachment(cost_id);
CREATE UNIQUE INDEX IF NOT EXISTS idx_uq_bus_saving_item_family_member_name ON bus_saving_item(family_id, member_id, name);
CREATE UNIQUE INDEX IF NOT EXISTS idx_uq_bus_saving_record_saving_item_member ON bus_saving_record(saving_id, saving_item_id, member_id);
CREATE UNIQUE INDEX IF NOT EXISTS idx_uq_bus_family_saving_family_date ON bus_family_saving(family_id, saving_date);

-- =============================================
-- 2026-06 v1.1 新增管理费字段
-- =============================================
ALTER TABLE bus_address ADD COLUMN IF NOT EXISTS default_management_fee DECIMAL(10,2);
COMMENT ON COLUMN bus_address.default_management_fee IS '默认管理费';

ALTER TABLE bus_bill ADD COLUMN IF NOT EXISTS management_fee DECIMAL(10,2);
COMMENT ON COLUMN bus_bill.management_fee IS '管理费';
nALTER TABLE bus_bill ADD COLUMN IF NOT EXISTS rounding_amount DECIMAL(10,2);
COMMENT ON COLUMN bus_bill.rounding_amount IS '抹零金额';
