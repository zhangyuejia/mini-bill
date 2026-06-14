-- 移除 bus_item_cost 冗余字段 family_id 和 address_id
-- 物件已属于家庭+住址，费用通过物件关联即可
ALTER TABLE bus_item_cost DROP COLUMN IF EXISTS family_id;
ALTER TABLE bus_item_cost DROP COLUMN IF EXISTS address_id;
