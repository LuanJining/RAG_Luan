# API

## 创建知识空间
**POST** /api/v1/spaces
```json
{
  "name": "研发部知识库",
  "description": "研发制度与规范"
}
```
**Response**
```json
{
  "spaceId": "space_123456"
}
```
**说明**: 创建一个新的知识空间，返回空间ID。

## 创建文档
**POST** /api/v1/spaces/{spaceId}/docs
```json
{
  "title": "研发流程",
  "file": "file（支持pdf和docx）",
  "spaceId": "space_123456"
}
```
**Response**
```json
{
  "docId": "space_123456",
}
```
**说明**: 在指定知识空间中创建文档，返回文档ID。

## 编辑文档
**PUT** /api/v1/spaces/{spaceId}/docs/{docId}
```json
{
  "title": "研发流程更新",
  "file": "file（支持pdf和docx）",
  "spaceId": "space_123456",
  "docId": "doc_123456"
}
```
**Response**
```json
{
  "success": true
}
```
**说明**: 编辑指定文档（覆盖式）,不操作postgres，spaceId可填任意值。

## 删除文档
**DELETE** /api/v1/spaces/{spaceId}/docs/{docId}
```json
{
  "spaceId": "space_123456",
  "docId": "doc_123456"
}
```
**Response**
```json
{
  "success": true
}
```
**说明**: 删除指定文档（这里是真删）。

## 搜索文档
**GET** /api/v1/spaces/{spaceId}/search?query=安全管理

**Response**
```json
{
  "items": [
    {
      "docId": "doc_123456",
      "title": "研发流程"
    },
    "..."
  ]
}
```
