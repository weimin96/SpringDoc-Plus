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
  /**
   * SpringDoc-Plus 扩展字段：标签排序值。
   * 对应 Java 侧 @Tag(extensions = @Extension(properties = @ExtensionProperty(name="x-order", value="1")))
   */
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
  callbacks?: Record<string, PathItemObject>
  /**
   * SpringDoc-Plus 扩展字段：接口排序值。
   * 对应 Java 侧 @Operation(extensions = @Extension(properties = @ExtensionProperty(name="x-order", value="1")))
   */
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
  headers?: Record<string, HeaderObject>
  content?: Record<string, MediaTypeObject>
  links?: Record<string, LinkObject>
}

export interface HeaderObject {
  description?: string
  required?: boolean
  schema?: SchemaObject
  example?: unknown
  examples?: Record<string, unknown>
}

export interface MediaTypeObject {
  schema?: SchemaObject
  example?: unknown
  examples?: Record<string, unknown>
}

export interface LinkObject {
  operationRef?: string
  operationId?: string
  parameters?: Record<string, unknown>
  requestBody?: unknown
  description?: string
}

export interface SchemaObject {
  type?: string | string[]
  format?: string
  description?: string
  properties?: Record<string, SchemaObject>
  additionalProperties?: boolean | SchemaObject
  items?: SchemaObject
  $ref?: string
  required?: string[]
  enum?: unknown[]
  example?: unknown
  examples?: unknown[]
  default?: unknown
  anyOf?: SchemaObject[]
  oneOf?: SchemaObject[]
  allOf?: SchemaObject[]
  discriminator?: DiscriminatorObject
  nullable?: boolean
  readOnly?: boolean
  writeOnly?: boolean
  deprecated?: boolean
  minimum?: number
  maximum?: number
  minLength?: number
  maxLength?: number
  minItems?: number
  maxItems?: number
}

export interface DiscriminatorObject {
  propertyName: string
  mapping?: Record<string, string>
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
  flows?: Record<string, unknown>
  openIdConnectUrl?: string
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
