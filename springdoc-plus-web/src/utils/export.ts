export type ExportFormat = 'docx' | 'markdown' | 'html' | 'json' | 'yaml' | 'postman' | 'insomnia'

export interface ExportOptions {
  includeExamples: boolean
  includeSchemas: boolean
  maskSensitiveHeaders: boolean
}

export function buildMarkdown(data: any, options: ExportOptions) {
  const lines: string[] = [
    `# ${data.title || 'API 文档'}`,
    '',
    `版本：${data.version || '-'}`,
    '',
  ]
  if (data.description) {
    lines.push(data.description, '')
  }
  for (const group of data.groups ?? []) {
    lines.push(`## ${group.tag}`, '')
    if (group.description) lines.push(group.description, '')
    for (const api of group.items ?? []) {
      lines.push(`### ${api.summary || api.path}`, '')
      lines.push(`- 方法：${api.method}`)
      lines.push(`- 路径：${api.path}`)
      if (api.description) lines.push(`- 描述：${api.description}`)
      if (api.parameters?.length) {
        lines.push('', '| 参数 | 位置 | 类型 | 必填 | 说明 |', '| --- | --- | --- | --- | --- |')
        api.parameters.forEach((p: any) =>
          lines.push(`| ${p.name} | ${p.in || ''} | ${p.type || ''} | ${p.required ? '是' : '否'} | ${p.desc || ''} |`),
        )
      }
      if (options.includeSchemas && api.responseEntities?.length) {
        appendSchemaTables(lines, api)
      }
      if (options.includeExamples && api.responseContents?.length) {
        appendExamples(lines, api)
      }
      lines.push('')
    }
  }
  return lines.join('\n')
}

export function buildHtml(data: any, options: ExportOptions) {
  const body = (data.groups ?? [])
    .map(
      (group: any) => `
    <section>
      <h2>${escapeHtml(group.tag)}</h2>
      ${group.description ? `<p>${escapeHtml(group.description)}</p>` : ''}
      ${(group.items ?? [])
        .map(
          (api: any) => `
        <article>
          <h3>${escapeHtml(api.summary || api.path)}</h3>
          <p><strong>${escapeHtml(api.method)}</strong> <code>${escapeHtml(api.path)}</code></p>
          ${api.description ? `<p>${escapeHtml(api.description)}</p>` : ''}
          ${options.includeSchemas ? schemaHtml(api) : ''}
          ${options.includeExamples ? examplesHtml(api) : ''}
        </article>
      `,
        )
        .join('')}
    </section>
  `,
    )
    .join('')
  return `<!doctype html>
<html lang="zh-CN">
<head>
  <meta charset="utf-8">
  <title>${escapeHtml(data.title || 'API 文档')}</title>
  <style>body{font-family:Arial,sans-serif;max-width:960px;margin:40px auto;line-height:1.7;color:#111827}code,pre{background:#f3f4f6;padding:2px 6px;border-radius:4px}pre{padding:12px;overflow:auto}article{border-top:1px solid #e5e7eb;padding:16px 0}table{border-collapse:collapse;width:100%;margin:12px 0}th,td{border:1px solid #e5e7eb;padding:6px;text-align:left}</style>
</head>
<body>
  <h1>${escapeHtml(data.title || 'API 文档')}</h1>
  <p>版本：${escapeHtml(data.version || '-')}</p>
  ${data.description ? `<p>${escapeHtml(data.description)}</p>` : ''}
  ${body}
</body>
</html>`
}

export function buildPostmanCollection(data: any, options: ExportOptions) {
  return {
    info: {
      name: data.title || 'API 文档',
      schema: 'https://schema.getpostman.com/json/collection/v2.1.0/collection.json',
    },
    item: (data.groups ?? []).map((group: any) => ({
      name: group.tag,
      item: (group.items ?? []).map((api: any) => ({
        name: api.summary || `${api.method} ${api.path}`,
        request: {
          method: api.method,
          header: requestHeaders(options),
          url: postmanUrl(api.path),
          description: api.description || '',
        },
        response: options.includeExamples ? postmanResponses(api) : [],
      })),
    })),
    variable: [{ key: 'baseUrl', value: '' }],
  }
}

export function buildInsomniaCollection(data: any, options: ExportOptions) {
  const workspaceId = 'wrk_springdoc_plus'
  const resources: any[] = [
    {
      _id: workspaceId,
      _type: 'workspace',
      name: data.title || 'API 文档',
      description: data.description || '',
      scope: 'collection',
    },
    {
      _id: 'env_springdoc_plus',
      _type: 'environment',
      parentId: workspaceId,
      name: 'Base Environment',
      data: { baseUrl: '' },
    },
  ]
  for (const group of data.groups ?? []) {
    const groupId = `fld_${slug(group.tag)}`
    resources.push({
      _id: groupId,
      _type: 'request_group',
      parentId: workspaceId,
      name: group.tag,
      description: group.description || '',
    })
    for (const api of group.items ?? []) {
      resources.push({
        _id: `req_${slug(`${api.method}_${api.path}`)}`,
        _type: 'request',
        parentId: groupId,
        name: api.summary || `${api.method} ${api.path}`,
        method: api.method,
        url: `{{ _.baseUrl }}${api.path}`,
        headers: requestHeaders(options).map((header) => ({ name: header.key, value: header.value })),
        description: api.description || '',
      })
    }
  }
  return {
    _type: 'export',
    __export_format: 4,
    __export_date: new Date().toISOString(),
    __export_source: 'springdoc-plus',
    resources,
  }
}

function appendSchemaTables(lines: string[], api: any) {
  for (const entity of api.responseEntities ?? []) {
    if (!entity.hasFields) continue
    lines.push('', `响应结构 ${entity.code}${entity.contentType ? `（${entity.contentType}）` : ''}：`)
    lines.push('| 字段 | 类型 | 必填 | 说明 |', '| --- | --- | --- | --- |')
    entity.fields.forEach((field: any) =>
      lines.push(`| ${field.name} | ${field.type} | ${field.required ? '是' : '否'} | ${field.desc || ''} |`),
    )
  }
}

function appendExamples(lines: string[], api: any) {
  for (const content of api.responseContents ?? []) {
    if (!content.example) continue
    lines.push('', `响应示例 ${content.code}${content.contentType ? `（${content.contentType}）` : ''}：`)
    lines.push('```json', content.example, '```')
  }
}

function schemaHtml(api: any) {
  return (api.responseEntities ?? [])
    .filter((entity: any) => entity.hasFields)
    .map((entity: any) => `
      <h4>响应结构 ${escapeHtml(entity.code)}${entity.contentType ? `（${escapeHtml(entity.contentType)}）` : ''}</h4>
      <table>
        <thead><tr><th>字段</th><th>类型</th><th>必填</th><th>说明</th></tr></thead>
        <tbody>${entity.fields.map((field: any) => `<tr><td>${escapeHtml(field.name)}</td><td>${escapeHtml(field.type)}</td><td>${field.required ? '是' : '否'}</td><td>${escapeHtml(field.desc || '')}</td></tr>`).join('')}</tbody>
      </table>
    `)
    .join('')
}

function examplesHtml(api: any) {
  return (api.responseContents ?? [])
    .filter((content: any) => content.example)
    .map((content: any) => `
      <h4>响应示例 ${escapeHtml(content.code)}${content.contentType ? `（${escapeHtml(content.contentType)}）` : ''}</h4>
      <pre>${escapeHtml(content.example)}</pre>
    `)
    .join('')
}

function postmanResponses(api: any) {
  return (api.responseContents ?? [])
    .filter((content: any) => content.example)
    .map((content: any) => ({
      name: `${content.code} ${content.contentType || ''}`.trim(),
      originalRequest: {
        method: api.method,
        header: [],
        url: postmanUrl(api.path),
      },
      status: '',
      code: Number(content.code) || 200,
      header: [],
      body: content.example,
    }))
}

function requestHeaders(options: ExportOptions) {
  if (!options.maskSensitiveHeaders) return []
  return [
    { key: 'Authorization', value: '{{token}}', type: 'text' },
    { key: 'X-Token', value: '{{token}}', type: 'text' },
  ]
}

function postmanUrl(path: string) {
  const normalizedPath = path.startsWith('/') ? path.slice(1) : path
  return {
    raw: `{{baseUrl}}/${normalizedPath}`,
    host: ['{{baseUrl}}'],
    path: normalizedPath.split('/').filter(Boolean),
  }
}

function slug(value: string) {
  return value
    .toLowerCase()
    .replace(/[^a-z0-9\u4e00-\u9fa5]+/g, '_')
    .replace(/^_+|_+$/g, '')
    .slice(0, 80) || 'item'
}

function escapeHtml(value: unknown) {
  return String(value ?? '')
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
    .replace(/'/g, '&#39;')
}
