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
  schemas?: Record<string, SchemaObject>,
  _visited: Set<string> = new Set()
): unknown {
  if (!schema) return {}

  // 解析 $ref，防止循环引用
  let resolved = resolveSchemaRef(schema, schemas)
  if (!resolved) return {}

  // 防止循环引用：记录已访问的 $ref
  if (schema.$ref) {
    const refKey = schema.$ref
    if (_visited.has(refKey)) return {}
    _visited = new Set(_visited).add(refKey)
  }

  // 优先使用显式示例值
  if (resolved.example !== undefined) return resolved.example
  if (resolved.default !== undefined) return resolved.default

  // allOf / anyOf / oneOf 取第一个合并
  if (resolved.allOf?.length) {
    const merged: Record<string, unknown> = {}
    for (const sub of resolved.allOf) {
      const subExample = buildSchemaExample(sub as SchemaObject, schemas, _visited)
      if (subExample && typeof subExample === 'object' && !Array.isArray(subExample)) {
        Object.assign(merged, subExample)
      }
    }
    return merged
  }
  if (resolved.anyOf?.length || resolved.oneOf?.length) {
    const first = (resolved.anyOf ?? resolved.oneOf)![0]
    return buildSchemaExample(first as SchemaObject, schemas, _visited)
  }

  // 根据类型构建示例
  switch (resolved.type) {
    case 'object': {
      const obj: Record<string, unknown> = {}
      if (resolved.properties) {
        for (const [key, val] of Object.entries(resolved.properties)) {
          const fieldVal = buildSchemaExample(val as SchemaObject, schemas, _visited)
          obj[key] = fieldVal
        }
      }
      return obj
    }
    case 'array':
      return resolved.items
        ? [buildSchemaExample(resolved.items as SchemaObject, schemas, _visited)]
        : []
    case 'string': {
      if (resolved.enum?.[0] !== undefined) return resolved.enum[0]
      if (resolved.format === 'date-time') return '2024-01-01T00:00:00Z'
      if (resolved.format === 'date') return '2024-01-01'
      if (resolved.format === 'email') return 'user@example.com'
      if (resolved.format === 'uri') return 'https://example.com'
      if (resolved.format === 'uuid') return '00000000-0000-0000-0000-000000000000'
      return 'string'
    }
    case 'integer':
      return 0
    case 'number':
      return 0.0
    case 'boolean':
      return false
    default: {
      // 没有 type 但有 properties，推断为 object
      if (resolved.properties) {
        const obj: Record<string, unknown> = {}
        for (const [key, val] of Object.entries(resolved.properties)) {
          obj[key] = buildSchemaExample(val as SchemaObject, schemas, _visited)
        }
        return obj
      }
      // 有 $ref 但未解析到，返回空对象而非 null
      return {}
    }
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