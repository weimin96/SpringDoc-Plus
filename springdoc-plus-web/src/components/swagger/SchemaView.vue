<script setup lang="ts">
import { computed } from 'vue'
import type { SchemaObject } from '@/types/openapi'

const props = withDefaults(defineProps<{
  schema: SchemaObject
  schemas?: Record<string, SchemaObject>
  depth?: number
  ancestorRefs?: string[]
}>(), {
  depth: 0,
  ancestorRefs: () => [],
})

function getRefName(ref: string): string {
  return ref.split('/').pop() ?? '?'
}

function resolveRef(ref: string, schemas?: Record<string, SchemaObject>): SchemaObject | null {
  return schemas?.[getRefName(ref)] ?? null
}

// 类型标签必须基于原始 schema 计算，否则引用名会在展示前退化成 object。
function resolveSchema(schema: SchemaObject): SchemaObject {
  if (!schema.$ref || !props.schemas) {
    return schema
  }
  return resolveRef(schema.$ref, props.schemas) ?? schema
}

function hasRenderableProperties(schema: SchemaObject): boolean {
  return Boolean(schema.properties && Object.keys(schema.properties).length > 0)
}

function isCircularSchema(schema: SchemaObject, ancestorRefs: string[] = props.ancestorRefs): boolean {
  if (!schema.$ref) {
    return false
  }
  return ancestorRefs.includes(schema.$ref)
}

function getNestedSchema(schema: SchemaObject): SchemaObject | null {
  const resolvedSchema = resolveSchema(schema)
  if (resolvedSchema.type === 'array') {
    return resolvedSchema.items ?? null
  }
  if (hasRenderableProperties(resolvedSchema)) {
    return schema
  }
  return null
}

function getExpandableChildSchema(schema: SchemaObject, ancestorRefs: string[] = descendantAncestorRefs.value): SchemaObject | null {
  const nestedSchema = getNestedSchema(schema)
  if (!nestedSchema || isCircularSchema(nestedSchema, ancestorRefs)) {
    return null
  }

  const resolvedNestedSchema = resolveSchema(nestedSchema)
  if (resolvedNestedSchema.type === 'array' || hasRenderableProperties(resolvedNestedSchema)) {
    return nestedSchema
  }

  return null
}

function isNestedCircular(schema: SchemaObject): boolean {
  const nestedSchema = getNestedSchema(schema)
  if (!nestedSchema) {
    return false
  }
  return isCircularSchema(nestedSchema, descendantAncestorRefs.value)
}

function typeLabel(schema: SchemaObject): string {
  if (schema.$ref) {
    return getRefName(schema.$ref)
  }

  if (schema.type === 'array' && schema.items) {
    return `array<${typeLabel(schema.items)}>`
  }

  const resolvedSchema = resolveSchema(schema)
  if (resolvedSchema.type === 'array' && resolvedSchema.items) {
    return `array<${typeLabel(resolvedSchema.items)}>`
  }
  if (hasRenderableProperties(resolvedSchema)) {
    return 'object'
  }
  return resolvedSchema.format ? `${resolvedSchema.type}(${resolvedSchema.format})` : resolvedSchema.type ?? '?'
}

function typeColor(schema: SchemaObject): string {
  if (schema.$ref) {
    return 'text-[var(--c-primary)]'
  }

  if (schema.type === 'array') {
    return 'text-green-700'
  }

  const resolvedSchema = resolveSchema(schema)
  const type = resolvedSchema.type

  if (resolvedSchema.$ref) return 'text-[var(--c-primary)]'
  if (type === 'string') return 'text-amber-600'
  if (type === 'integer' || type === 'number') return 'text-blue-600'
  if (type === 'boolean') return 'text-purple-600'
  if (type === 'array') return 'text-green-700'
  return 'text-[var(--c-muted)]'
}

const resolved = computed(() => resolveSchema(props.schema))
const currentRef = computed(() => props.schema.$ref ?? null)
const descendantAncestorRefs = computed(() => {
  if (!currentRef.value) {
    return props.ancestorRefs
  }
  return [...props.ancestorRefs, currentRef.value]
})
</script>

<template>
  <div
    v-if="!isCircularSchema(schema) && hasRenderableProperties(resolved)"
    class="text-xs"
  >
    <div
      v-for="(propSchema, propName) in resolved.properties"
      :key="propName"
      class="border-b border-[var(--c-border)] py-1.5 last:border-0"
      :style="`padding-left:${depth * 16}px`"
    >
      <div class="flex items-start gap-2">
        <span class="min-w-[120px] font-mono text-[var(--c-text)]">
          {{ propName }}
          <span v-if="resolved.required?.includes(propName)" class="ml-0.5 text-red-500">*</span>
        </span>
        <span class="font-mono" :class="typeColor(propSchema)">
          {{ typeLabel(propSchema) }}
        </span>
        <span v-if="propSchema.description" class="text-[var(--c-muted)]">- {{ propSchema.description }}</span>
        <span v-else-if="isNestedCircular(propSchema)" class="text-[var(--c-muted)]">- 循环引用，已停止展开</span>
      </div>

      <div v-if="getExpandableChildSchema(propSchema)" class="mt-1">
        <SchemaView
          :schema="getExpandableChildSchema(propSchema)!"
          :schemas="schemas"
          :depth="depth + 1"
          :ancestor-refs="descendantAncestorRefs"
        />
      </div>
    </div>
  </div>

  <div v-else-if="resolved.type === 'array' && resolved.items" class="text-xs">
    <div class="font-mono" :class="typeColor(schema)">
      {{ typeLabel(schema) }}
    </div>

    <div v-if="getExpandableChildSchema(schema)" class="mt-1">
      <SchemaView
        :schema="getExpandableChildSchema(schema)!"
        :schemas="schemas"
        :depth="depth + 1"
        :ancestor-refs="descendantAncestorRefs"
      />
    </div>
    <div v-else-if="isNestedCircular(schema)" class="mt-1 text-[var(--c-muted)]">
      循环引用，已停止展开
    </div>
  </div>

  <span v-else class="font-mono text-xs" :class="typeColor(schema)">
    {{ typeLabel(schema) }}
  </span>
</template>
