-- =============================================
-- 附件表统一迁移
-- 将 bus_bill_attachment / bus_item_attachment / bus_item_cost_attachment
-- 三表合并为 bus_attachment 通用表，自动迁移数据
-- =============================================

-- 1. 创建通用附件表
CREATE TABLE IF NOT EXISTS bus_attachment (
    id BIGINT PRIMARY KEY,
    biz_type VARCHAR(20) NOT NULL,
    biz_id BIGINT NOT NULL,
    file_name VARCHAR(255),
    file_url VARCHAR(500),
    file_size BIGINT
);

COMMENT ON TABLE bus_attachment IS '通用附件表';
COMMENT ON COLUMN bus_attachment.id IS '主键ID';
COMMENT ON COLUMN bus_attachment.biz_type IS '业务类型：bill-账单 item-物件 item_cost-物件费用';
COMMENT ON COLUMN bus_attachment.biz_id IS '业务ID';

-- 2. 创建索引
CREATE INDEX IF NOT EXISTS idx_bus_attachment_biz ON bus_attachment(biz_type, biz_id);

-- 3. 迁移数据（从旧表导入，不丢数据）
INSERT INTO bus_attachment (id, biz_type, biz_id, file_name, file_url, file_size)
SELECT id, 'bill', bill_id, file_name, file_url, file_size FROM bus_bill_attachment;

INSERT INTO bus_attachment (id, biz_type, biz_id, file_name, file_url, file_size)
SELECT id, 'item', item_id, file_name, file_url, file_size FROM bus_item_attachment;

INSERT INTO bus_attachment (id, biz_type, biz_id, file_name, file_url, file_size)
SELECT id, 'item_cost', cost_id, file_name, file_url, file_size FROM bus_item_cost_attachment;

-- 4. 删除旧表
DROP TABLE IF EXISTS bus_bill_attachment;
DROP TABLE IF EXISTS bus_item_attachment;
DROP TABLE IF EXISTS bus_item_cost_attachment;
