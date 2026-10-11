# 后端接口示例

所有接口都需要登录后将 `token` 放入请求头：

```http
X-Auth-Token: <token>
```

## 1. 药品详情

`GET /api/medications/{id}`

成功：

```json
{
  "id": 2,
  "medicationCode": "M002",
  "medicationName": "阿莫西林胶囊",
  "specification": "0.25g*24粒",
  "unit": "盒",
  "unitPrice": 22.00,
  "stockQuantity": 58,
  "safeStock": 15,
  "usageNotes": "青霉素过敏者禁用",
  "indications": "用于敏感菌所致的呼吸道、泌尿道、皮肤软组织等感染。",
  "contraindications": "对青霉素类药物过敏者禁用。",
  "sourceName": "国家药品监督管理局药品说明书",
  "sourceUrl": "https://www.nmpa.gov.cn/",
  "versionLabel": "2026-01",
  "updatedAt": "2026-01-01 00:00:00"
}
```

失败：

```json
{
  "status": 404,
  "message": "未找到该药品",
  "path": "/api/medications/99999"
}
```

## 2. 药品搜索

`GET /api/medications?keyword=阿莫&page=1&size=20`

成功：

```json
{
  "total": 1,
  "page": 1,
  "size": 20,
  "items": [
    {
      "id": 2,
      "medicationCode": "M002",
      "medicationName": "阿莫西林胶囊",
      "specification": "0.25g*24粒"
    }
  ]
}
```

失败：

```json
{
  "status": 401,
  "message": "未登录或登录已失效",
  "path": "/api/medications"
}
```

## 3. 药物冲突检查

`POST /api/medication-conflicts/check`

请求：

```json
{
  "medicationNames": ["布洛芬缓释胶囊", "阿司匹林肠溶片"]
}
```

成功：

```json
{
  "hasConflict": true,
  "conflicts": [
    {
      "leftMedicationName": "布洛芬缓释胶囊",
      "rightMedicationName": "阿司匹林肠溶片",
      "severity": "中风险",
      "guidance": "可能增加胃肠道刺激"
    }
  ],
  "modelVersion": "rule-baseline-0.1"
}
```

失败：

```json
{
  "status": 400,
  "message": "请至少提供一种药品名称",
  "path": "/api/medication-conflicts/check"
}
```

## 4. 用药计划查询

`GET /api/medication-plans?patientUserId=13`

成功返回该患者的用药计划数组，字段与 `tb_medication_plan` 对应。

失败：

```json
{
  "status": 403,
  "message": "只能访问自己的用药计划",
  "path": "/api/medication-plans"
}
```

## 5. 新建用药计划

`POST /api/medication-plans`

小程序只传药品名称时，后端会自动匹配库存并关联该患者最近一次挂号：

```json
{
  "patientUserId": 13,
  "medicationName": "奥美拉唑肠溶胶囊",
  "dosage": "1 粒 / 次",
  "quantity": 1,
  "frequencyCode": "QD",
  "frequencyLabel": "每日 1 次",
  "frequencyPerDay": 1,
  "unit": "盒",
  "instructions": "晨起空腹服用"
}
```

成功返回 `201 Created` 和新建的计划对象。失败场景：

```json
{
  "status": 400,
  "message": "未找到匹配的药品库存",
  "path": "/api/medication-plans"
}
```

## 6. 图片识别代理

`POST /api/ai/medicine-image`

请求使用 `multipart/form-data`，字段名为 `image`，支持 `jpg / jpeg / png / webp`，最大 `5 MB`。

成功：

```json
{
  "medicineName": null,
  "specification": null,
  "manufacturer": null,
  "expiryDate": null,
  "confidence": 0.0,
  "modelVersion": "mock-0.1",
  "warnings": ["当前为联调占位结果，请接入真实识别模型。"]
}
```

失败：

```json
{
  "status": 415,
  "message": "图片格式仅支持 jpg、jpeg、png、webp",
  "path": "/api/ai/medicine-image"
}
```

## 7. 知识库检索

`POST /api/knowledge/search`

请求：

```json
{
  "query": "阿莫西林",
  "topK": 5
}
```

成功：

```json
{
  "answers": [
    {
      "title": "阿莫西林胶囊说明书",
      "content": "适用于敏感菌所致的呼吸道、泌尿道等感染；对青霉素过敏者禁用。",
      "sourceName": "国家药品监督管理局",
      "sourceUrl": "https://www.nmpa.gov.cn/",
      "versionLabel": "2026-01",
      "reviewStatus": "PENDING",
      "score": 0.9
    }
  ]
}
```

失败：

```json
{
  "status": 404,
  "message": "知识库中暂未检索到相关内容，请咨询医生 / 药师",
  "path": "/api/knowledge/search"
}
```
