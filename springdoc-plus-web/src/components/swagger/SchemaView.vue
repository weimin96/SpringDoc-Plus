<script setup lang="ts">
import { computed } from 'vue'
import type { SchemaObject } from '@/types/openapi'

const props = defineProps<{
  schema: SchemaObject
  schemas?: Record<string, SchemaObject>
  depth?: number
}>()

const depth = props.depth ?? 0

function resolveRef(ref: string, schemas?: Record<string, SchemaObject>): SchemaObject | null {
  const name = ref.replace('#/components/schemas/', '')
  return schemas?.[name] ?? null
}

function getSchema(s: SchemaObject): SchemaObject {
  if (s.$ref && props.schemas) {
    return resolveRef(s.$ref, props.schemas) ?? s
  }
  return s
}

const resolved = computed(() => getSchema(props.schema))

function typeLabel(s: SchemaObject): string {
  const schema = getSchema(s)
  if (schema.$ref) return schema.$ref.split('/').pop() ?? '?'
  if (schema.type === 'array' && schema.items) return `array<${typeLabel(getSchema(schema.items))}>`
  return schema.format ? `${schema.type}(${schema.format})` : schema.type ?? '?'
}

function typeColor(s: SchemaObject): string {
  const schema = getSchema(s)
  const t = schema.type
  if (t === 'string')  return 'text-amber-600'
  if (t === 'integer' || t === 'number') return 'text-blue-600'
  if (t === 'boolean') return 'text-purple-600'
  if (t === 'array')   return 'text-green-700'
  if (schema.$ref)          return 'text-[var(--c-primary)]'
  return 'text-[var(--c-muted)]'
}
</script>

<template>
  <!-- Object with properties -->
  <div v-if="resolved.properties" class="text-xs">
    <div
      v-for="(propSchema, propName) in resolved.properties"
      :key="propName"
      class="flex items-start gap-2 border-b border-[var(--c-border)] py-1.5 last:border-0"
      :style="`padding-left:${depth * 16}px`"
    >
      <span class="min-w-[120px] font-mono text-[var(--c-text)]">
        {{ propName }}
        <span v-if="resolved.required?.includes(propName)" class="ml-0.5 text-red-500">*</span>
      </span>
      <span class="font-mono" :class="typeColor(getSchema(propSchema))">
        {{ typeLabel(getSchema(propSchema)) }}
      </span>
      <span v-if="propSchema.description" class="text-[var(--c-muted)]">— {{ propSchema.description }}</span>
    </div>
  </div>

  <!-- Array -->
  <div v-else-if="resolved.type === 'array' && resolved.items" class="text-xs">
    <span class="text-[var(--c-muted)]">array of </span>
    <SchemaView :schema="resolved.items" :schemas="schemas" :depth="depth" />
  </div>

  <!-- Primitive -->
  <span v-else class="font-mono text-xs" :class="typeColor(resolved)">
    {{ typeLabel(resolved) }}
  </span>
</template>
