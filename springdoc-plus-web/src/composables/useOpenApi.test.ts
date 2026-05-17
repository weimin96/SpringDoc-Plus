import { describe, expect, it, vi, afterEach } from 'vitest'
import type { MergedConfig } from '@/types'
import { useOpenApi } from './useOpenApi'

interface PendingResponse {
  signal?: AbortSignal
  resolve: (value: Response) => void
  promise: Promise<Response>
}

function createConfig(): MergedConfig {
  return {
    authEnabled: false,
    tagsSorter: 'alpha',
    operationsSorter: 'alpha',
  }
}

function createPendingResponse(signal?: AbortSignal): PendingResponse {
  let resolve!: (value: Response) => void
  const promise = new Promise<Response>((done) => {
    resolve = done
  })
  return { signal, resolve, promise }
}

function createOpenApiResponse(title: string): Response {
  return new Response(
    JSON.stringify({
      openapi: '3.1.0',
      info: { title, version: 'v1' },
      paths: {},
    }),
    {
      status: 200,
      headers: { 'content-type': 'application/json' },
    },
  )
}

afterEach(() => {
  vi.unstubAllGlobals()
})

describe('useOpenApi', () => {
  it('快速切换分组时只保留最后一次请求结果', async () => {
    const requests: PendingResponse[] = []

    vi.stubGlobal(
      'fetch',
      vi.fn((_url: string, init?: RequestInit) => {
        const pending = createPendingResponse(init?.signal)
        requests.push(pending)
        return pending.promise
      }),
    )

    const openApi = useOpenApi(createConfig())

    const firstLoad = openApi.load('/v3/api-docs-a')
    const secondLoad = openApi.load('/v3/api-docs-b')

    expect(requests).toHaveLength(2)
    expect(requests[0].signal?.aborted).toBe(true)

    requests[1].resolve(createOpenApiResponse('服务 B'))
    await secondLoad

    expect(openApi.spec.value?.info?.title).toBe('服务 B')
    expect(openApi.loading.value).toBe(false)

    requests[0].resolve(createOpenApiResponse('服务 A'))
    await firstLoad

    expect(openApi.spec.value?.info?.title).toBe('服务 B')
    expect(openApi.error.value).toBeNull()
  })
})
