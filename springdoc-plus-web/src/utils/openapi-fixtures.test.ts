import { afterEach, describe, expect, it, vi } from 'vitest'
import { useOpenApi } from '@/composables/useOpenApi'
import type { MergedConfig } from '@/types'
import { buildLargeOpenApiDocument, complexOpenApiDocument } from '@/fixtures/openapi/complex'
import { buildDocData } from './openapi'
import { buildSchemaExample, generateJsonSchemaExample, resolveOpenApiRef } from './schema'

function config(): MergedConfig {
  return {
    authEnabled: false,
    tagsSorter: 'order',
    operationsSorter: 'alpha',
  }
}

function mockFetch(document: unknown) {
  vi.stubGlobal(
    'fetch',
    vi.fn(() =>
      Promise.resolve(
        new Response(JSON.stringify(document), {
          status: 200,
          headers: { 'content-type': 'application/json' },
        }),
      ),
    ),
  )
}

afterEach(() => {
  vi.unstubAllGlobals()
})

describe('OpenAPI 复杂 fixture', () => {
  it('解析基础 REST 接口、OAuth2、callbacks、links 和响应 headers', async () => {
    mockFetch(complexOpenApiDocument)
    const openApi = useOpenApi(config())

    await openApi.load('/v3/api-docs')

    expect(openApi.error.value).toBeNull()
    expect(openApi.tagGroups.value.map((group) => group.name)).toEqual(['用户', '宠物'])
    expect(openApi.tagGroups.value[0].operations).toHaveLength(2)
    expect(openApi.spec.value?.components?.securitySchemes?.OAuth2?.type).toBe('oauth2')
    expect(openApi.spec.value?.paths?.['/users']?.post?.callbacks?.userCreated).toBeTruthy()
    expect(openApi.spec.value?.paths?.['/users/{id}']?.get?.responses?.['200']?.headers?.['X-Rate-Limit']).toBeTruthy()
    expect(openApi.spec.value?.paths?.['/users/{id}']?.get?.responses?.['200']?.links?.userOrders).toBeTruthy()
  })

  it('生成组合 Schema、循环引用和错误 Schema 的示例', () => {
    const userExample = buildSchemaExample(
      { $ref: '#/components/schemas/User' },
      complexOpenApiDocument,
    ) as Record<string, unknown>
    const petExample = buildSchemaExample(
      { $ref: '#/components/schemas/Pet' },
      complexOpenApiDocument,
    ) as Record<string, unknown>
    const invalidExample = buildSchemaExample(
      { $ref: '#/components/schemas/InvalidButRenderable' },
      complexOpenApiDocument,
    )

    expect(userExample.id).toBe('00000000-0000-0000-0000-000000000000')
    expect(userExample.name).toBe('string')
    expect(petExample.petType).toBe('cat')
    expect(invalidExample).toEqual({ value: 'string' })
    expect(generateJsonSchemaExample({ $ref: '#/components/schemas/UserNode' }, complexOpenApiDocument)).toContain('child')
  })

  it('保留多 content-type 和 multiple examples 的导出数据', () => {
    const data = buildDocData(complexOpenApiDocument)

    expect(data.content).toContain('application/json')
    expect(data.content).toContain('multipart/form-data')
    expect(data.apis.some((api: any) => api.responseContents.some((content: any) => content.contentType === 'application/problem+json'))).toBe(true)
    expect(data.apis.some((api: any) => api.responseContents.some((content: any) => content.example.includes('张三')))).toBe(true)
  })

  it('解析超大 OpenAPI 文档时保持分组完整', async () => {
    mockFetch(buildLargeOpenApiDocument(220))
    const openApi = useOpenApi(config())

    await openApi.load('/v3/api-docs-large')

    expect(openApi.error.value).toBeNull()
    expect(openApi.tagGroups.value).toHaveLength(1)
    expect(openApi.tagGroups.value[0].operations).toHaveLength(220)
  })

  it('对缺失引用返回 missing 状态', () => {
    const result = resolveOpenApiRef('#/components/schemas/Missing', complexOpenApiDocument)

    expect(result.status).toBe('missing')
  })
})
