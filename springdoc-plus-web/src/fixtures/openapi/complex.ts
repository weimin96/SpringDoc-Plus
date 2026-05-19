import type { OpenApiSpec } from '@/types/openapi'

export const complexOpenApiDocument: OpenApiSpec = {
  openapi: '3.1.0',
  info: {
    title: '复杂 OpenAPI 文档',
    version: '2026.1',
    description: '覆盖组合 Schema、循环引用、多内容类型和 OAuth2。',
  },
  servers: [{ url: 'https://api.example.com', description: '生产环境' }],
  tags: [
    { name: '用户', description: '用户接口', 'x-order': 1 },
    { name: '宠物', description: '宠物接口', 'x-order': 2 },
  ],
  security: [{ OAuth2: ['user:read'] }] as any,
  paths: {
    '/users/{id}': {
      parameters: [
        {
          name: 'traceId',
          in: 'header',
          schema: { type: 'string' },
          description: '链路 ID',
        },
      ],
      get: {
        tags: ['用户'],
        summary: '查询用户',
        operationId: 'getUser',
        security: [{ OAuth2: ['user:read'] }],
        parameters: [
          {
            name: 'id',
            in: 'path',
            required: true,
            schema: { type: 'string', format: 'uuid' },
            description: '用户 ID',
          },
        ],
        responses: {
          '200': {
            description: '成功',
            headers: {
              'X-Rate-Limit': {
                description: '剩余限流次数',
                schema: { type: 'integer' },
              },
            },
            links: {
              userOrders: {
                operationId: 'listOrders',
                parameters: { userId: '$response.body#/id' },
              },
            },
            content: {
              'application/json': {
                schema: { $ref: '#/components/schemas/User' },
                examples: {
                  normal: {
                    value: { id: '00000000-0000-0000-0000-000000000000', name: '张三' },
                  },
                },
              },
              'application/problem+json': {
                schema: { $ref: '#/components/schemas/ErrorResponse' },
              },
            },
          },
        },
      },
    },
    '/users': {
      post: {
        tags: ['用户'],
        summary: '创建用户',
        requestBody: {
          required: true,
          content: {
            'application/json': { schema: { $ref: '#/components/schemas/CreateUserRequest' } },
            'multipart/form-data': { schema: { $ref: '#/components/schemas/UserAvatarForm' } },
          },
        },
        callbacks: {
          userCreated: {
            '{$request.body#/callbackUrl}': {
              post: {
                summary: '用户创建回调',
                responses: { '204': { description: '已接收' } },
              },
            },
          },
        },
        responses: {
          '201': {
            description: '已创建',
            content: {
              'application/json': { schema: { $ref: '#/components/schemas/User' } },
            },
          },
        },
      },
    },
    '/pets': {
      post: {
        tags: ['宠物'],
        summary: '创建宠物',
        requestBody: {
          content: {
            'application/json': { schema: { $ref: '#/components/schemas/Pet' } },
          },
        },
        responses: {
          '200': {
            description: '成功',
            content: {
              'application/json': { schema: { $ref: '#/components/schemas/SearchResult' } },
            },
          },
        },
      },
    },
  },
  components: {
    securitySchemes: {
      OAuth2: {
        type: 'oauth2',
        flows: {
          authorizationCode: {
            authorizationUrl: 'https://auth.example.com/oauth2/authorize',
            tokenUrl: 'https://auth.example.com/oauth2/token',
            scopes: { 'user:read': '读取用户' },
          },
        },
      },
    },
    schemas: {
      BaseEntity: {
        type: 'object',
        required: ['id'],
        properties: {
          id: { type: 'string', format: 'uuid', readOnly: true },
        },
      },
      UserProfile: {
        type: 'object',
        properties: {
          displayName: { type: ['string', 'null'], nullable: true },
          password: { type: 'string', writeOnly: true },
        },
      },
      User: {
        allOf: [
          { $ref: '#/components/schemas/BaseEntity' },
          {
            type: 'object',
            required: ['name'],
            properties: {
              name: { type: 'string' },
              profile: { $ref: '#/components/schemas/UserProfile' },
              manager: { $ref: '#/components/schemas/UserNode' },
            },
          },
        ],
      },
      UserNode: {
        type: 'object',
        properties: {
          name: { type: 'string' },
          child: { $ref: '#/components/schemas/UserNode' },
        },
      },
      CreateUserRequest: {
        type: 'object',
        required: ['name'],
        properties: {
          name: { type: 'string', minLength: 2 },
          callbackUrl: { type: 'string', format: 'uri' },
        },
      },
      UserAvatarForm: {
        type: 'object',
        properties: {
          avatar: { type: 'string', format: 'binary' },
          metadata: { type: 'string' },
        },
      },
      Pet: {
        oneOf: [{ $ref: '#/components/schemas/Cat' }, { $ref: '#/components/schemas/Dog' }],
        discriminator: {
          propertyName: 'petType',
          mapping: {
            cat: '#/components/schemas/Cat',
            dog: '#/components/schemas/Dog',
          },
        },
      },
      Cat: {
        type: 'object',
        required: ['petType'],
        properties: {
          petType: { type: 'string', enum: ['cat'] },
          huntingSkill: { type: 'string', deprecated: true },
        },
      },
      Dog: {
        type: 'object',
        required: ['petType'],
        properties: {
          petType: { type: 'string', enum: ['dog'] },
          packSize: { type: 'integer', minimum: 1 },
        },
      },
      SearchResult: {
        anyOf: [
          { $ref: '#/components/schemas/User' },
          {
            type: 'object',
            properties: {
              total: { type: 'integer' },
              items: {
                type: 'array',
                items: { $ref: '#/components/schemas/User' },
              },
            },
          },
        ],
      },
      ErrorResponse: {
        type: 'object',
        additionalProperties: { type: 'string' },
      },
      InvalidButRenderable: {
        properties: {
          value: { type: 'string' },
        },
      },
    },
  },
}

export function buildLargeOpenApiDocument(count = 180): OpenApiSpec {
  const paths: NonNullable<OpenApiSpec['paths']> = {}
  for (let i = 0; i < count; i += 1) {
    paths[`/bulk/items/${i}`] = {
      get: {
        tags: ['批量'],
        summary: `批量接口 ${i}`,
        responses: {
          '200': {
            description: '成功',
            content: {
              'application/json': {
                schema: { type: 'object', properties: { id: { type: 'integer', example: i } } },
              },
            },
          },
        },
      },
    }
  }
  return {
    openapi: '3.1.0',
    info: { title: '超大文档', version: '1.0.0' },
    tags: [{ name: '批量' }],
    paths,
  }
}
