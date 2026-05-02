import type { SchemaObject } from '@/types/openapi'

export function resolveSchemaRef(
  schema: SchemaObject | null | undefined,
  schemas?: Record<string, SchemaObject>
): SchemaObject | null {
  if (!schema?.$ref || !schemas) return schema ?? null

  const refName = schema.$ref.split('/').pop()
  return schemas[refName!] ?? schema
}

export function buildSchemaExample(
  schema: SchemaObject | null,
  schemas?: Record<string, SchemaObject>,
  visited: Set<string> = new Set(),
  depth = 0,
): unknown {
  if (!schema) return {}
  if (depth > 8) return {}

  const resolved = resolveSchemaRef(schema, schemas)
  if (!resolved) return {}

  if (schema.$ref) {
    if (visited.has(schema.$ref)) return {}
    visited = new Set(visited).add(schema.$ref)
  }

  if (resolved.example !== undefined) return resolved.example
  if (resolved.examples?.[0] !== undefined) return resolved.examples[0]
  if (resolved.default !== undefined) return resolved.default

  if (resolved.allOf?.length) {
    const merged: Record<string, unknown> = {}
    for (const subSchema of resolved.allOf) {
      const example = buildSchemaExample(subSchema as SchemaObject, schemas, visited, depth + 1)
      if (example && typeof example === 'object' && !Array.isArray(example)) {
        Object.assign(merged, example)
      }
    }
    return merged
  }

  if (resolved.anyOf?.length || resolved.oneOf?.length) {
    const first = (resolved.anyOf ?? resolved.oneOf)![0]
    return buildSchemaExample(first as SchemaObject, schemas, visited, depth + 1)
  }

  switch (resolved.type) {
    case 'object': {
      const obj: Record<string, unknown> = {}

      if (resolved.properties) {
        for (const [key, value] of Object.entries(resolved.properties)) {
          obj[key] = buildSchemaExample(value as SchemaObject, schemas, visited, depth + 1)
        }
      }

      if (!Object.keys(obj).length && resolved.additionalProperties) {
        const additionalSchema = resolved.additionalProperties === true
          ? { type: 'string' }
          : resolved.additionalProperties
        obj.sampleKey = buildSchemaExample(additionalSchema as SchemaObject, schemas, visited, depth + 1)
      }

      return obj
    }
    case 'array':
      return resolved.items
        ? [buildSchemaExample(resolved.items as SchemaObject, schemas, visited, depth + 1)]
        : []
    case 'string':
      if (resolved.enum?.[0] !== undefined) return resolved.enum[0]
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
          obj[key] = buildSchemaExample(value as SchemaObject, schemas, visited, depth + 1)
        }
        return obj
      }

      if (resolved.additionalProperties) {
        const additionalSchema = resolved.additionalProperties === true
          ? { type: 'string' }
          : resolved.additionalProperties
        return {
          sampleKey: buildSchemaExample(additionalSchema as SchemaObject, schemas, visited, depth + 1),
        }
      }

      return {}
  }
}

export function generateJsonSchemaExample(
  schema: SchemaObject | null,
  schemas?: Record<string, SchemaObject>
): string {
  const resolved = resolveSchemaRef(schema, schemas)
  return JSON.stringify(buildSchemaExample(resolved, schemas), null, 2)
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
    (schema.type === 'array' &&
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
