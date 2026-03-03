/**
 * Schema 工具模块
 * 提供 OpenAPI Schema 解析、$ref 解引用、示例生成等功能
 */

import type { SchemaObject } from '@/types/openapi'

/**
 * 解析 Schema 中的 $ref 引用
 * @param schema Schema 对象
 * @param schemas 全局 Schema 映射表（components.schemas）
 * @returns 解析后的 Schema 对象
 */
export function resolveSchemaRef(
  schema: SchemaObject | null | undefined,
  schemas?: Record<string, SchemaObject>
): SchemaObject | null {
  if (!schema?.$ref || !schemas) return schema ?? null

  const refName = schema.$ref.split('/').pop()
  return schemas[refName!] ?? schema
}

/**
 * 从 Schema 构建示例值（递归）
 * @param schema Schema 对象
 * @param schemas 全局 Schema 映射表
 * @returns 示例值（可以是任意类型）
 */
export function buildSchemaExample(
  schema: SchemaObject | null,
  schemas?: Record<string, SchemaObject>
): unknown {
  if (!schema) return null

  const resolved = resolveSchemaRef(schema, schemas)
  if (!resolved) return null

  // 优先使用显式示例值
  if (resolved.example !== undefined) return resolved.example
  if (resolved.default !== undefined) return resolved.default

  // 根据类型构建示例
  switch (resolved.type) {
    case 'object': {
      const obj: Record<string, unknown> = {}
      if (resolved.properties) {
        for (const [key, val] of Object.entries(resolved.properties)) {
          obj[key] = buildSchemaExample(val as SchemaObject, schemas)
        }
      }
      return obj
    }
    case 'array':
      return resolved.items
        ? [buildSchemaExample(resolved.items as SchemaObject, schemas)]
        : []
    case 'string':
      return resolved.enum?.[0] ?? 'string'
    case 'integer':
    case 'number':
      return 0
    case 'boolean':
      return false
    default:
      return null
  }
}

/**
 * 生成 JSON 格式的 Schema 示例
 * @param schema Schema 对象
 * @param schemas 全局 Schema 映射表
 * @returns JSON 字符串
 */
export function generateJsonSchemaExample(
  schema: SchemaObject | null,
  schemas?: Record<string, SchemaObject>
): string {
  const resolved = resolveSchemaRef(schema, schemas)
  return JSON.stringify(buildSchemaExample(resolved, schemas), null, 2)
}

/**
 * Content-Type 常量
 */
export const CONTENT_TYPE = {
  JSON: 'application/json' as const,
  FORM_URL_ENCODED: 'application/x-www-form-urlencoded' as const,
  MULTIPART_FORM_DATA: 'multipart/form-data' as const,
  XML: 'application/xml' as const,
  TEXT_PLAIN: 'text/plain' as const,
  TEXT_HTML: 'text/html' as const,
  OCTET_STREAM: 'application/octet-stream' as const,
} as const

/**
 * 判断是否为 JSON Content-Type
 */
export function isJsonContentType(contentType: string): boolean {
  return contentType.includes(CONTENT_TYPE.JSON)
}

/**
 * 判断是否为表单 Content-Type
 */
export function isFormContentType(contentType: string): boolean {
  return (
    contentType.includes(CONTENT_TYPE.FORM_URL_ENCODED) ||
    contentType.includes(CONTENT_TYPE.MULTIPART_FORM_DATA)
  )
}

/**
 * 判断是否为文件上传字段
 */
export function isBinaryField(schema: SchemaObject | null): boolean {
  if (!schema) return false
  return (
    schema.format === 'binary' ||
    (schema.type === 'array' &&
      (schema.items as SchemaObject)?.format === 'binary')
  )
}

/**
 * 参数位置样式映射
 */
const PARAM_LOCATION_CLASSES: Record<string, string> = {
  path: 'bg-blue-100 text-blue-700',
  query: 'bg-green-100 text-green-700',
  header: 'bg-purple-100 text-purple-700',
  cookie: 'bg-amber-100 text-amber-700',
} as const

/**
 * 获取参数位置的样式类
 */
export function getParamLocationClass(location: string): string {
  return PARAM_LOCATION_CLASSES[location] ?? 'bg-gray-100 text-gray-700'
}
