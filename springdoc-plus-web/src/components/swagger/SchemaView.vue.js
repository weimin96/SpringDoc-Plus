const props = defineProps();
const depth = props.depth ?? 0;
function resolveRef(ref, schemas) {
    const name = ref.replace('#/components/schemas/', '');
    return schemas?.[name] ?? null;
}
function getSchema(s) {
    if (s.$ref && props.schemas) {
        return resolveRef(s.$ref, props.schemas) ?? s;
    }
    return s;
}
const resolved = getSchema(props.schema);
function typeLabel(s) {
    if (s.$ref)
        return s.$ref.split('/').pop() ?? '?';
    if (s.type === 'array' && s.items)
        return `array<${typeLabel(getSchema(s.items))}>`;
    return s.format ? `${s.type}(${s.format})` : s.type ?? '?';
}
function typeColor(s) {
    const t = s.type;
    if (t === 'string')
        return 'text-amber-600';
    if (t === 'integer' || t === 'number')
        return 'text-blue-600';
    if (t === 'boolean')
        return 'text-purple-600';
    if (t === 'array')
        return 'text-green-700';
    if (s.$ref)
        return 'text-[var(--c-primary)]';
    return 'text-[var(--c-muted)]';
}
const __VLS_ctx = {
    ...{},
    ...{},
    ...{},
    ...{},
};
let __VLS_components;
let __VLS_intrinsics;
let __VLS_directives;
if (__VLS_ctx.resolved.properties) {
    __VLS_asFunctionalElement1(__VLS_intrinsics.div, __VLS_intrinsics.div)({
        ...{ class: "text-xs" },
    });
    /** @type {__VLS_StyleScopedClasses['text-xs']} */ ;
    for (const [propSchema, propName] of __VLS_vFor((__VLS_ctx.resolved.properties))) {
        __VLS_asFunctionalElement1(__VLS_intrinsics.div, __VLS_intrinsics.div)({
            key: (propName),
            ...{ class: "flex items-start gap-2 border-b border-[var(--c-border)] py-1.5 last:border-0" },
            ...{ style: (`padding-left:${__VLS_ctx.depth * 16}px`) },
        });
        /** @type {__VLS_StyleScopedClasses['flex']} */ ;
        /** @type {__VLS_StyleScopedClasses['items-start']} */ ;
        /** @type {__VLS_StyleScopedClasses['gap-2']} */ ;
        /** @type {__VLS_StyleScopedClasses['border-b']} */ ;
        /** @type {__VLS_StyleScopedClasses['border-[var(--c-border)]']} */ ;
        /** @type {__VLS_StyleScopedClasses['py-1.5']} */ ;
        /** @type {__VLS_StyleScopedClasses['last:border-0']} */ ;
        __VLS_asFunctionalElement1(__VLS_intrinsics.span, __VLS_intrinsics.span)({
            ...{ class: "min-w-[120px] font-mono text-[var(--c-text)]" },
        });
        /** @type {__VLS_StyleScopedClasses['min-w-[120px]']} */ ;
        /** @type {__VLS_StyleScopedClasses['font-mono']} */ ;
        /** @type {__VLS_StyleScopedClasses['text-[var(--c-text)]']} */ ;
        (propName);
        if (__VLS_ctx.resolved.required?.includes(propName)) {
            __VLS_asFunctionalElement1(__VLS_intrinsics.span, __VLS_intrinsics.span)({
                ...{ class: "ml-0.5 text-red-500" },
            });
            /** @type {__VLS_StyleScopedClasses['ml-0.5']} */ ;
            /** @type {__VLS_StyleScopedClasses['text-red-500']} */ ;
        }
        __VLS_asFunctionalElement1(__VLS_intrinsics.span, __VLS_intrinsics.span)({
            ...{ class: "font-mono" },
            ...{ class: (__VLS_ctx.typeColor(__VLS_ctx.getSchema(propSchema))) },
        });
        /** @type {__VLS_StyleScopedClasses['font-mono']} */ ;
        (__VLS_ctx.typeLabel(__VLS_ctx.getSchema(propSchema)));
        if (propSchema.description) {
            __VLS_asFunctionalElement1(__VLS_intrinsics.span, __VLS_intrinsics.span)({
                ...{ class: "text-[var(--c-muted)]" },
            });
            /** @type {__VLS_StyleScopedClasses['text-[var(--c-muted)]']} */ ;
            (propSchema.description);
        }
        // @ts-ignore
        [resolved, resolved, resolved, depth, typeColor, getSchema, getSchema, typeLabel,];
    }
}
else if (__VLS_ctx.resolved.type === 'array' && __VLS_ctx.resolved.items) {
    __VLS_asFunctionalElement1(__VLS_intrinsics.div, __VLS_intrinsics.div)({
        ...{ class: "text-xs" },
    });
    /** @type {__VLS_StyleScopedClasses['text-xs']} */ ;
    __VLS_asFunctionalElement1(__VLS_intrinsics.span, __VLS_intrinsics.span)({
        ...{ class: "text-[var(--c-muted)]" },
    });
    /** @type {__VLS_StyleScopedClasses['text-[var(--c-muted)]']} */ ;
    let __VLS_0;
    /** @ts-ignore @type {typeof __VLS_components.SchemaView} */
    SchemaView;
    // @ts-ignore
    const __VLS_1 = __VLS_asFunctionalComponent1(__VLS_0, new __VLS_0({
        schema: (__VLS_ctx.resolved.items),
        schemas: (__VLS_ctx.schemas),
        depth: (__VLS_ctx.depth),
    }));
    const __VLS_2 = __VLS_1({
        schema: (__VLS_ctx.resolved.items),
        schemas: (__VLS_ctx.schemas),
        depth: (__VLS_ctx.depth),
    }, ...__VLS_functionalComponentArgsRest(__VLS_1));
}
else {
    __VLS_asFunctionalElement1(__VLS_intrinsics.span, __VLS_intrinsics.span)({
        ...{ class: "font-mono text-xs" },
        ...{ class: (__VLS_ctx.typeColor(__VLS_ctx.resolved)) },
    });
    /** @type {__VLS_StyleScopedClasses['font-mono']} */ ;
    /** @type {__VLS_StyleScopedClasses['text-xs']} */ ;
    (__VLS_ctx.typeLabel(__VLS_ctx.resolved));
}
// @ts-ignore
[resolved, resolved, resolved, resolved, resolved, depth, typeColor, typeLabel, schemas,];
const __VLS_export = (await import('vue')).defineComponent({
    __typeProps: {},
});
export default {};
