import type { OpenApiSpec, SchemaObject } from '@/types/openapi'

type RefSource = Record<string, SchemaObject> | OpenApiSpec

export interface RefResolution<T = unknown> {
  status: 'resolved' | 'external' | 'invalid' | 'missing'
  value: T | null
  message?: string
}

function isRecord(value: unknown): value is Record<string, unknown> {
  return Boolean(value) && typeof value === 'object' && !Array.isArray(value)
}

function isOpenApiSpec(source: RefSource): source is OpenApiSpec {
  return 'components' in source || 'paths' in source || 'openapi' in source
}

function buildReferenceDocument(source?: RefSource): unknown {
  if (!source) return null
  return isOpenApiSpec(source) ? source : { components: { schemas: source } }
}

function decodeJsonPointerToken(token: string): RefResolution<string> {
  if (/~(?![01])/u.test(token)) {
    return {
      status: 'invalid',
      value: null,
      message: `JSON Pointer 包含非法转义：${token}`,
    }
  }

  try {
    return {
      status: 'resolved',
      value: decodeURIComponent(token).replace(/~1/g, '/').replace(/~0/g, '~'),
    }
  } catch {
    return {
      status: 'invalid',
      value: null,
      message: `JSON Pointer 包含非法 URI 编码：${token}`,
    }
  }
}

export function resolveJsonPointer<T = unknown>(
  document: unknown,
  pointer: string,
): RefResolution<T> {
  if (pointer === '') {
    return { status: 'resolved', value: document as T }
  }

  if (!pointer.startsWith('/')) {
    return {
      status: 'invalid',
      value: null,
      message: `JSON Pointer 必须以 / 开头：${pointer}`,
    }
  }

  let current = document
  const tokens = pointer.slice(1).split('/')

  for (const rawToken of tokens) {
    const decoded = decodeJsonPointerToken(rawToken)
    if (decoded.status !== 'resolved' || decoded.value === null) {
      return {
        status: decoded.status,
        value: null,
        message: decoded.message,
      }
    }

    if (
      (isRecord(current) || Array.isArray(current)) &&
      Object.prototype.hasOwnProperty.call(current, decoded.value)
    ) {
      current = (current as Record<string, unknown>)[decoded.value]
      continue
    }

    return {
      status: 'missing',
      value: null,
      message: `JSON Pointer 未找到节点：${pointer}`,
    }
  }

  return { status: 'resolved', value: current as T }
}

export function resolveOpenApiRef<T = unknown>(ref: string, source?: RefSource): RefResolution<T> {
  if (!ref.startsWith('#')) {
    return {
      status: 'external',
      value: null,
      message: `外部引用未展开：${ref}`,
    }
  }

  const document = buildReferenceDocument(source)
  if (!document) {
    return {
      status: 'missing',
      value: null,
      message: `缺少引用解析上下文：${ref}`,
    }
  }

  return resolveJsonPointer<T>(document, ref.slice(1))
}

export function getOpenApiRefName(ref: string): string {
  const fragment = ref.startsWith('#') ? ref.slice(1) : (ref.split('#').pop() ?? ref)
  if (!fragment) return ref
  const token = fragment.split('/').filter(Boolean).pop()
  if (!token) return ref
  const decoded = decodeJsonPointerToken(token)
  return decoded.value ?? token
}

function buildUnresolvedRefSchema(ref: string, message?: string): SchemaObject {
  const reason = message ?? `引用未解析：${ref}`
  return {
    type: 'object',
    description: reason,
    example: {
      $ref: ref,
      note: reason,
    },
  }
}

export function resolveSchemaRef(
  schema: SchemaObject | null | undefined,
  source?: RefSource,
): SchemaObject | null {
  if (!schema?.$ref) return schema ?? null

  const resolution = resolveOpenApiRef<SchemaObject>(schema.$ref, source)
  if (resolution.status === 'resolved' && isRecord(resolution.value)) {
    return resolution.value as SchemaObject
  }

  if (!source && schema.$ref.startsWith('#')) {
    return schema
  }

  return buildUnresolvedRefSchema(schema.$ref, resolution.message)
}

export function getSchemaTypes(schema: SchemaObject | null | undefined): string[] {
  if (!schema?.type) return []
  return Array.isArray(schema.type) ? schema.type : [schema.type]
}

export function getSchemaPrimaryType(schema: SchemaObject | null | undefined): string | undefined {
  const types = getSchemaTypes(schema)
  return types.find((type) => type !== 'null') ?? types[0]
}

export function isNullableSchema(schema: SchemaObject | null | undefined): boolean {
  return Boolean(schema?.nullable || getSchemaTypes(schema).includes('null'))
}

export function buildSchemaExample(
  schema: SchemaObject | null,
  source?: RefSource,
  visited: Set<string> = new Set(),
  depth = 0,
): unknown {
  if (!schema) return {}
  if (depth > 8) return {}

  const resolved = resolveSchemaRef(schema, source)
  if (!resolved) return {}

  if (schema.$ref) {
    if (visited.has(schema.$ref)) return {}
    visited = new Set(visited).add(schema.$ref)
  }

  if (resolved.example !== undefined) return resolved.example
  if (resolved.examples?.[0] !== undefined) return resolved.examples[0]
  if (resolved.default !== undefined) return resolved.default
  if (resolved.enum?.length) {
    const enumValue = resolved.enum.find((value) => value !== null)
    return enumValue !== undefined ? enumValue : resolved.enum[0]
  }

  if (resolved.allOf?.length) {
    const merged: Record<string, unknown> = {}
    for (const subSchema of resolved.allOf) {
      const example = buildSchemaExample(subSchema as SchemaObject, source, visited, depth + 1)
      if (example && typeof example === 'object' && !Array.isArray(example)) {
        Object.assign(merged, example)
      }
    }
    return merged
  }

  if (resolved.anyOf?.length || resolved.oneOf?.length) {
    const first = (resolved.anyOf ?? resolved.oneOf)![0]
    return buildSchemaExample(first as SchemaObject, source, visited, depth + 1)
  }

  const type = getSchemaPrimaryType(resolved)
  if (type === 'null') return null

  switch (type) {
    case 'object': {
      const obj: Record<string, unknown> = {}

      if (resolved.properties) {
        for (const [key, value] of Object.entries(resolved.properties)) {
          obj[key] = buildSchemaExample(value as SchemaObject, source, visited, depth + 1)
        }
      }

      if (!Object.keys(obj).length && resolved.additionalProperties) {
        const additionalSchema =
          resolved.additionalProperties === true
            ? { type: 'string' }
            : resolved.additionalProperties
        obj.sampleKey = buildSchemaExample(
          additionalSchema as SchemaObject,
          source,
          visited,
          depth + 1,
        )
      }

      return obj
    }
    case 'array':
      return resolved.items
        ? [buildSchemaExample(resolved.items as SchemaObject, source, visited, depth + 1)]
        : []
    case 'string':
      if (resolved.format === 'date-time') return '2024-01-01T00:00:00Z'
      if (resolved.format === 'date') return '2024-01-01'
      if (resolved.format === 'email') return 'user@example.com'
      if (resolved.format === 'uri') return 'https://example.com'
      if (resolved.format === 'uuid') return '00000000-0000-0000-0000-000000000000'
      if (resolved.format === 'binary') return ''
      if (resolved.minLength && resolved.minLength > 6) return 'x'.repeat(resolved.minLength)
      return 'string'
    case 'integer':
      return resolved.minimum ?? 0
    case 'number':
      return resolved.minimum ?? 0
    case 'boolean':
      return false
    default:
      if (resolved.properties) {
        const obj: Record<string, unknown> = {}
        for (const [key, value] of Object.entries(resolved.properties)) {
          obj[key] = buildSchemaExample(value as SchemaObject, source, visited, depth + 1)
        }
        return obj
      }

      if (resolved.additionalProperties) {
        const additionalSchema =
          resolved.additionalProperties === true
            ? { type: 'string' }
            : resolved.additionalProperties
        return {
          sampleKey: buildSchemaExample(
            additionalSchema as SchemaObject,
            source,
            visited,
            depth + 1,
          ),
        }
      }

      return isNullableSchema(resolved) ? null : {}
  }
}

export function generateJsonSchemaExample(schema: SchemaObject | null, source?: RefSource): string {
  const resolved = resolveSchemaRef(schema, source)
  return JSON.stringify(buildSchemaExample(resolved, source), null, 2)
}

export const CONTENT_TYPE = {
  JSON: 'application/json' as const,
  FORM_URL_ENCODED: 'application/x-www-form-urlencoded' as const,
  MULTIPART_FORM_DATA: 'multipart/form-data' as const,
  XML: 'application/xml' as const,
  TEXT_PLAIN: 'text/plain' as const,
  TEXT_HTML: 'text/html' as const,
  OCTET_STREAM: 'application/octet-stream' as const,
} as const

export function isJsonContentType(contentType: string): boolean {
  return contentType.includes(CONTENT_TYPE.JSON) || contentType.includes('+json')
}

export function isFormContentType(contentType: string): boolean {
  return (
    contentType.includes(CONTENT_TYPE.FORM_URL_ENCODED) ||
    contentType.includes(CONTENT_TYPE.MULTIPART_FORM_DATA)
  )
}

export function isBinaryField(schema: SchemaObject | null): boolean {
  if (!schema) return false

  return (
    schema.format === 'binary' ||
    (getSchemaPrimaryType(schema) === 'array' &&
      (schema.items as SchemaObject)?.format === 'binary')
  )
}

const PARAM_LOCATION_CLASSES: Record<string, string> = {
  path: 'bg-blue-100 text-blue-700',
  query: 'bg-green-100 text-green-700',
  header: 'bg-purple-100 text-purple-700',
  cookie: 'bg-amber-100 text-amber-700',
}

export function getParamLocationClass(location: string): string {
  return PARAM_LOCATION_CLASSES[location] ?? 'bg-gray-100 text-gray-700'
}
