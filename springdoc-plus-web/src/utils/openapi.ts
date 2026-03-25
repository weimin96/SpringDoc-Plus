/**
 * OpenAPI 文档处理工具
 * 用于 DOCX 导出功能
 */

export function isOAS3(swagger: any): boolean {
  return typeof swagger?.openapi === 'string' && swagger.openapi.startsWith('3')
}

export function summarizeSwagger(swagger: any) {
  const info = swagger.info || {}
  const title = info.title || 'API 文档'
  const version = info.version || ''
  const description = info.description || ''
  const paths = swagger.paths || {}
  const endpointCount = Object.values(paths).reduce((acc: number, p: any) => {
    return acc + Object.keys(p || {}).length
  }, 0)
  return { title, version, description, endpointCount }
}

export function getTagsMeta(swagger: any): Record<string, string> {
  const meta: Record<string, string> = {}
  ;(swagger.tags || []).forEach((t: any) => (meta[t.name] = t.description || ''))
  return meta
}

export function buildDocData(swagger: any) {
  const info = summarizeSwagger(swagger)
  const content = composeText(swagger)
  const apis = buildApis(swagger)
  const groups = buildTagGroups(swagger)
  return {
    title: info.title,
    version: info.version,
    description: info.description,
    content,
    apis,
    groups,
  }
}

function composeText(swagger: any): string {
  const lines: string[] = []
  const info = swagger.info || {}
  lines.push(`# ${info.title || 'API 文档'}`)
  if (info.version) lines.push(`版本：${info.version}`)
  if (info.description) lines.push(info.description)
  const servers = isOAS3(swagger) ? swagger.servers || [] : []
  if (servers.length) {
    lines.push('')
    lines.push('服务器：')
    servers.forEach((s: any) =>
      lines.push(`- ${s.url} ${s.description ? '(' + s.description + ')' : ''}`)
    )
  }
  lines.push('')
  lines.push('接口列表：')

  const paths = swagger.paths || {}
  const tagsMeta = getTagsMeta(swagger)

  Object.keys(paths).forEach((path) => {
    const item = paths[path] || {}
    Object.keys(item).forEach((method) => {
      const op = item[method]
      const tag = Array.isArray(op?.tags) && op.tags[0] ? op.tags[0] : ''
      lines.push('')
      lines.push(`## [${method.toUpperCase()}] ${path}`)
      if (tag) lines.push(`标签：${tag}${tagsMeta[tag] ? '（' + tagsMeta[tag] + '）' : ''}`)
      if (op?.summary) lines.push(`摘要：${op.summary}`)
      if (op?.description) lines.push(op.description)
      const params = collectParameters(paths[path], op)
      if (params.length) {
        lines.push('参数：')
        params.forEach((p) => {
          lines.push(`- ${p.name} (${p.in})${p.required ? ' [必填]' : ''}：${p.description || ''}`)
        })
      }
      if (isOAS3(swagger)) {
        const rb = op?.requestBody
        if (rb?.content) {
          lines.push('请求体：')
          Object.keys(rb.content).forEach((ct) => {
            const schema = rb.content[ct]?.schema
            lines.push(`- ${ct}: ${schemaToText(schema)}`)
          })
        }
      }
      const responses = op?.responses || {}
      if (responses && Object.keys(responses).length) {
        lines.push('响应：')
        Object.keys(responses).forEach((code) => {
          const r = responses[code]
          if (isOAS3(swagger)) {
            const content = r?.content
            if (content) {
              Object.keys(content).forEach((ct) => {
                lines.push(`- ${code} (${ct}): ${schemaToText(content[ct]?.schema)}`)
              })
            } else {
              lines.push(`- ${code}: ${r?.description || ''}`)
            }
          } else {
            lines.push(`- ${code}: ${r?.description || ''}`)
          }
        })
      }
    })
  })

  return lines.join('\n')
}

function buildApis(swagger: any) {
  const paths = swagger.paths || {}
  const apis: any[] = []
  Object.keys(paths).forEach((path) => {
    const item = paths[path] || {}
    Object.keys(item).forEach((method) => {
      const op = item[method] || {}
      const tag = Array.isArray(op?.tags) && op.tags[0] ? op.tags[0] : ''
      const params = collectParameters(item, op).map((p: any) => ({
        name: p.name || '',
        in: p.in || '',
        required: !!p.required,
        type: isOAS3(swagger) ? p.schema?.type || p.schema?.$ref || p.type || '' : p.type || '',
        desc: p.description || '',
      }))
      const responsesObj = op.responses || {}
      const responses = Object.keys(responsesObj).map((code) => ({
        code,
        description: responsesObj[code]?.description || '',
      }))
      const responseContents: any[] = []
      const responseEntities: any[] = []
      if (isOAS3(swagger)) {
        Object.keys(responsesObj).forEach((code) => {
          const r = responsesObj[code]
          const content = r?.content || {}
          const cts = Object.keys(content)
          if (cts.length) {
            cts.forEach((ct) => {
              const schema = content[ct]?.schema
              const example = extractExampleFromOAS3(content[ct], schema)
              responseContents.push({
                code,
                contentType: ct,
                schema: schemaToText(schema),
                description: r?.description || '',
                example,
                hasExample: !!example && String(code) === '200',
              })
              const fields = schemaToFields(swagger, schema)
              responseEntities.push({ code, contentType: ct, fields, hasFields: fields.length > 0 })
            })
          } else {
            responseContents.push({
              code,
              contentType: '',
              schema: '',
              description: r?.description || '',
              example: '',
              hasExample: false,
            })
            responseEntities.push({ code, contentType: '', fields: [], hasFields: false })
          }
        })
      } else {
        Object.keys(responsesObj).forEach((code) => {
          const r = responsesObj[code]
          const example = extractExampleFromOAS2(r)
          responseContents.push({
            code,
            contentType: '',
            schema: '',
            description: r?.description || '',
            example,
            hasExample: !!example && String(code) === '200',
          })
          const schema = r?.schema
          const fields = schemaToFields(swagger, schema)
          responseEntities.push({ code, contentType: '', fields, hasFields: fields.length > 0 })
        })
      }
      apis.push({
        method: String(method).toUpperCase(),
        path,
        summary: op.summary || '',
        description: op.description || '',
        tag,
        parameters: params,
        responses,
        responseContents,
        responseEntities,
        hasResponseEntities: responseEntities.some((e) => e.fields && e.fields.length > 0),
      })
    })
  })
  return apis
}

function collectParameters(pathItem: any, op: any) {
  const params = [...(pathItem?.parameters || []), ...(op?.parameters || [])]
  const seen = new Set<string>()
  const result: any[] = []
  params.forEach((p) => {
    const key = `${p.in}:${p.name}`
    if (!seen.has(key)) {
      seen.add(key)
      result.push(p)
    }
  })
  return result
}

function schemaToText(schema: any): string {
  if (!schema) return '未定义'
  if (schema.$ref) return schema.$ref
  if (schema.type === 'object') {
    const props = schema.properties || {}
    const required = new Set(schema.required || [])
    const items = Object.keys(props).map((k) => {
      const p = props[k]
      const req = required.has(k) ? '[必填]' : ''
      return `${k}: ${p.type || ''} ${req} ${p.description || ''}`.trim()
    })
    return `{ ${items.join('; ')} }`
  }
  if (schema.type === 'array') {
    return `Array<${schemaToText(schema.items)}>`
  }
  return schema.type || 'schema'
}

export function buildTagGroups(swagger: any) {
  const apis = buildApis(swagger)
  const tagsMeta = getTagsMeta(swagger)
  const order = (swagger.tags || []).map((t: any) => t.name)
  const groupsMap: Record<string, { tag: string; description: string; items: any[] }> = {}
  apis.forEach((api) => {
    const t = api.tag || '未分组'
    if (!groupsMap[t]) groupsMap[t] = { tag: t, description: tagsMeta[t] || '', items: [] }
    groupsMap[t].items.push(api)
  })
  const result: any[] = []
  order.forEach((t: any) => {
    if (groupsMap[t]) result.push(groupsMap[t])
  })
  if (groupsMap['未分组']) result.push(groupsMap['未分组'])
  Object.keys(groupsMap).forEach((t) => {
    if (t !== '未分组' && !order.includes(t)) result.push(groupsMap[t])
  })
  return result
}

export function groupApisByTag(apis: any[], tagsMeta: Record<string, string>) {
  const groupsMap: Record<string, { tag: string; description: string; items: any[] }> = {}
  apis.forEach((api) => {
    const t = api.tag || '未分组'
    if (!groupsMap[t]) groupsMap[t] = { tag: t, description: tagsMeta[t] || '', items: [] }
    groupsMap[t].items.push(api)
  })
  return Object.values(groupsMap)
}

function stringifyExample(val: any): string {
  try {
    if (val === undefined || val === null) return ''
    if (typeof val === 'string') return val
    return JSON.stringify(val, null, 2)
  } catch {
    return ''
  }
}

function extractExampleFromOAS3(media: any, schema: any): string {
  if (!media) return stringifyExample(schema?.example)
  if (media.example !== undefined) return stringifyExample(media.example)
  const examples = media.examples || {}
  const firstKey = Object.keys(examples)[0]
  if (firstKey && examples[firstKey]?.value !== undefined)
    return stringifyExample(examples[firstKey].value)
  return stringifyExample(schema?.example)
}

function extractExampleFromOAS2(response: any): string {
  if (!response) return ''
  const examples = response.examples || {}
  const firstKey = Object.keys(examples)[0]
  if (firstKey && examples[firstKey] !== undefined) return stringifyExample(examples[firstKey])
  return stringifyExample(response.schema?.example)
}

function resolveRef(swagger: any, ref?: string): any | null {
  if (!ref || typeof ref !== 'string') return null
  if (!ref.startsWith('#/')) return null
  const parts = ref.replace(/^#\//, '').split('/')
  let cur: any = swagger
  for (const p of parts) {
    if (cur && typeof cur === 'object' && p in cur) cur = cur[p]
    else return null
  }
  return cur || null
}

function fieldTypeText(schema: any): string {
  if (!schema) return '未定义'
  if (schema.$ref) return schema.$ref
  if (schema.type === 'array') return `Array<${fieldTypeText(schema.items)}>`
  if (schema.type === 'object') return 'object'
  return schema.type || 'schema'
}

function schemaToFields(
  swagger: any,
  schema: any,
  parent: string = '',
  required: Set<string> = new Set(),
  visitedRefs: Set<string> = new Set()
): Array<{ name: string; type: string; required: boolean; desc: string }> {
  const out: Array<{ name: string; type: string; required: boolean; desc: string }> = []
  if (!schema) return out
  if (schema.$ref) {
    // 检测循环引用
    if (visitedRefs.has(schema.$ref)) {
      return parent ? [{ name: parent, type: 'Circular Reference', required: false, desc: '循环引用' }] : []
    }
    const resolved = resolveRef(swagger, schema.$ref)
    if (!resolved) return out
    const newVisitedRefs = new Set(visitedRefs)
    newVisitedRefs.add(schema.$ref)
    return schemaToFields(swagger, resolved, parent, required, newVisitedRefs)
  }
  if (schema.type === 'object') {
    const props = schema.properties || {}
    const reqSet = new Set([...(schema.required || []), ...required])
    Object.keys(props).forEach((k) => {
      const p = props[k]
      const name = parent ? `${parent}.${k}` : k
      const type = fieldTypeText(p)
      const isReq = reqSet.has(k)
      const desc = p.description || ''
      out.push({ name, type, required: isReq, desc })
      if (p.$ref || p.type === 'object' || p.type === 'array') {
        const nextSchema = p.$ref ? resolveRef(swagger, p.$ref) : p.type === 'array' ? p.items : p
        out.push(
          ...schemaToFields(
            swagger,
            nextSchema,
            name + (p.type === 'array' ? '[]' : ''),
            new Set(p.required || []),
            visitedRefs
          )
        )
      }
    })
    return out
  }
  if (schema.type === 'array') {
    const name = parent ? `${parent}[]` : 'items'
    const type = fieldTypeText(schema)
    out.push({ name, type, required: false, desc: '' })
    if (schema.items) {
      out.push(
        ...schemaToFields(
          swagger,
          schema.items,
          name,
          new Set(),
          visitedRefs
        )
      )
    }
    return out
  }
  const type = fieldTypeText(schema)
  out.push({ name: parent || '', type, required: false, desc: schema.description || '' })
  return out
}