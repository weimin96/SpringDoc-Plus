/* ═══════════════════════════════════════════════════
   OpenAPI 3.x spec types (minimal, for UI rendering)
═══════════════════════════════════════════════════ */

export interface OpenApiSpec {
  openapi?: string
  info?: InfoObject
  servers?: ServerObject[]
  tags?: TagObject[]
  paths?: Record<string, PathItemObject>
  components?: ComponentsObject
}

export interface InfoObject {
  title: string
  version: string
  description?: string
  contact?: { name?: string; url?: string; email?: string }
  license?: { name: string; url?: string }
}

export interface ServerObject {
  url: string
  description?: string
}

export interface TagObject {
  name: string
  description?: string
  'x-order'?: number
}

export type HttpMethod = 'get' | 'post' | 'put' | 'delete' | 'patch' | 'head' | 'options' | 'trace'

export type PathItemObject = {
  [M in HttpMethod]?: OperationObject
} & {
  parameters?: ParameterObject[]
  summary?: string
  description?: string
}

export interface OperationObject {
  operationId?: string
  summary?: string
  description?: string
  tags?: string[]
  deprecated?: boolean
  parameters?: ParameterObject[]
  requestBody?: RequestBodyObject
  responses?: Record<string, ResponseObject>
  security?: SecurityRequirementObject[]
  'x-order'?: number
}

export interface ParameterObject {
  name: string
  in: 'query' | 'header' | 'path' | 'cookie'
  description?: string
  required?: boolean
  schema?: SchemaObject
  example?: unknown
}

export interface RequestBodyObject {
  description?: string
  required?: boolean
  content?: Record<string, MediaTypeObject>
}

export interface ResponseObject {
  description?: string
  content?: Record<string, MediaTypeObject>
}

export interface MediaTypeObject {
  schema?: SchemaObject
  example?: unknown
  examples?: Record<string, unknown>
}

export interface SchemaObject {
  type?: string
  format?: string
  description?: string
  properties?: Record<string, SchemaObject>
  items?: SchemaObject
  $ref?: string
  required?: string[]
  enum?: unknown[]
  example?: unknown
  default?: unknown
  anyOf?: SchemaObject[]
  oneOf?: SchemaObject[]
  allOf?: SchemaObject[]
}

export interface ComponentsObject {
  schemas?: Record<string, SchemaObject>
  securitySchemes?: Record<string, SecuritySchemeObject>
}

export interface SecuritySchemeObject {
  type: string
  scheme?: string
  bearerFormat?: string
  in?: string
  name?: string
  description?: string
}

export type SecurityRequirementObject = Record<string, string[]>

/* Processed structures for the UI */
export interface TagGroup {
  name: string
  description?: string
  order: number
  operations: OperationItem[]
}

export interface OperationItem {
  method: HttpMethod
  path: string
  operation: OperationObject
  order: number
}
