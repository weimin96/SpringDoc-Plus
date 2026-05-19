import { describe, expect, it } from 'vitest'
import {
  buildHtml,
  buildInsomniaCollection,
  buildMarkdown,
  buildPostmanCollection,
  type ExportOptions,
} from './export'

const options: ExportOptions = {
  includeExamples: true,
  includeSchemas: true,
  maskSensitiveHeaders: true,
}

const docData = {
  title: '用户服务',
  version: '1.0.0',
  description: '<script>alert(1)</script>',
  groups: [
    {
      tag: '用户',
      description: '用户接口',
      items: [
        {
          method: 'GET',
          path: '/users/{id}',
          summary: '查询用户',
          description: '返回用户详情',
          parameters: [{ name: 'id', in: 'path', type: 'string', required: true, desc: '用户 ID' }],
          responseContents: [{ code: '200', contentType: 'application/json', example: '{"id":"1"}' }],
          responseEntities: [
            {
              code: '200',
              contentType: 'application/json',
              hasFields: true,
              fields: [{ name: 'id', type: 'string', required: true, desc: '用户 ID' }],
            },
          ],
        },
      ],
    },
  ],
}

describe('export utils', () => {
  it('生成 Markdown 并按选项包含结构与示例', () => {
    const markdown = buildMarkdown(docData, options)

    expect(markdown).toContain('## 用户')
    expect(markdown).toContain('| id | string | 是 | 用户 ID |')
    expect(markdown).toContain('```json')
  })

  it('生成转义后的 HTML 文档', () => {
    const html = buildHtml(docData, options)

    expect(html).toContain('&lt;script&gt;alert(1)&lt;/script&gt;')
    expect(html).toContain('<table>')
  })

  it('生成 Postman Collection', () => {
    const collection = buildPostmanCollection(docData, options)

    expect(collection.info.schema).toContain('collection/v2.1.0')
    expect(collection.item[0].item[0].request.url.raw).toBe('{{baseUrl}}/users/{id}')
    expect(collection.item[0].item[0].request.header[0].key).toBe('Authorization')
  })

  it('生成 Insomnia Collection', () => {
    const collection = buildInsomniaCollection(docData, options)

    expect(collection.__export_format).toBe(4)
    expect(collection.resources.some((item: any) => item._type === 'request')).toBe(true)
  })
})
