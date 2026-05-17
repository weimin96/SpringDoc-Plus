import { describe, expect, it } from 'vitest'
import type { OpenApiSpec, SchemaObject } from '@/types/openapi'
import {
  buildSchemaExample,
  getOpenApiRefName,
  resolveOpenApiRef,
  resolveSchemaRef,
} from './schema'

const openApiDocument = {
  openapi: '3.1.0',
  info: { title: '测试文档', version: 'v1' },
  paths: {},
  components: {
    schemas: {
      'A/B': { type: 'string', example: 'slash-name' },
      'm~n': { type: 'integer', minimum: 7 },
      NullableName: { type: ['string', 'null'], enum: [null, '启用'] },
      OnlyNull: { type: ['null'] },
      Wrapper: {
        type: 'object',
        properties: {
          id: { $ref: '#/components/schemas/m~0n' },
        },
      },
    },
    parameters: {
      Limit: {
        name: 'limit',
        in: 'query',
        schema: { type: 'integer' },
      },
    },
    responses: {
      Ok: {
        description: '成功',
      },
    },
  },
} as unknown as OpenApiSpec

describe('schema 引用解析', () => {
  it('按完整 JSON Pointer 解析本地引用和转义字符', () => {
    expect(
      resolveOpenApiRef<SchemaObject>('#/components/schemas/A~1B', openApiDocument).value,
    ).toEqual({ type: 'string', example: 'slash-name' })
    expect(
      resolveOpenApiRef<SchemaObject>('#/components/schemas/m~0n', openApiDocument).value,
    ).toEqual({ type: 'integer', minimum: 7 })
    expect(getOpenApiRefName('#/components/schemas/A~1B')).toBe('A/B')
  })

  it('支持 components 下非 schemas 节点的完整指针解析', () => {
    const parameter = resolveOpenApiRef<any>('#/components/parameters/Limit', openApiDocument)
    const response = resolveOpenApiRef<any>('#/components/responses/Ok', openApiDocument)

    expect(parameter.value?.name).toBe('limit')
    expect(response.value?.description).toBe('成功')
  })

  it('为外部引用返回可展示的降级信息', () => {
    const schema = resolveSchemaRef(
      { $ref: 'common.yaml#/components/schemas/User' },
      openApiDocument,
    )

    expect(schema?.description).toContain('外部引用未展开')
    expect(buildSchemaExample(schema)).toEqual({
      $ref: 'common.yaml#/components/schemas/User',
      note: '外部引用未展开：common.yaml#/components/schemas/User',
    })
  })

  it('兼容 OpenAPI 3.1 type array 与 nullable enum', () => {
    expect(buildSchemaExample({ $ref: '#/components/schemas/NullableName' }, openApiDocument)).toBe(
      '启用',
    )
    expect(
      buildSchemaExample({ $ref: '#/components/schemas/OnlyNull' }, openApiDocument),
    ).toBeNull()
  })
})
