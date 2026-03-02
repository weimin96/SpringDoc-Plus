const __VLS_props = defineProps();
const colorMap = {
    get: 'bg-blue-600',
    post: 'bg-green-700',
    put: 'bg-amber-600',
    delete: 'bg-red-600',
    patch: 'bg-purple-600',
    head: 'bg-yellow-600',
    options: 'bg-zinc-500',
    trace: 'bg-slate-500',
};
const bgMap = {
    get: 'bg-blue-50 border-blue-200',
    post: 'bg-green-50 border-green-200',
    put: 'bg-amber-50 border-amber-200',
    delete: 'bg-red-50 border-red-200',
    patch: 'bg-purple-50 border-purple-200',
    head: 'bg-yellow-50 border-yellow-200',
    options: 'bg-zinc-50 border-zinc-200',
    trace: 'bg-slate-50 border-slate-200',
};
const __VLS_ctx = {
    ...{},
    ...{},
    ...{},
    ...{},
};
let __VLS_components;
let __VLS_intrinsics;
let __VLS_directives;
__VLS_asFunctionalElement1(__VLS_intrinsics.div, __VLS_intrinsics.div)({
    ...{ class: "flex items-center gap-2" },
});
/** @type {__VLS_StyleScopedClasses['flex']} */ ;
/** @type {__VLS_StyleScopedClasses['items-center']} */ ;
/** @type {__VLS_StyleScopedClasses['gap-2']} */ ;
__VLS_asFunctionalElement1(__VLS_intrinsics.span, __VLS_intrinsics.span)({
    ...{ class: "min-w-[65px] rounded-md px-2 py-[3px] text-center font-mono text-[11px] font-bold uppercase tracking-wide text-white" },
    ...{ class: (__VLS_ctx.colorMap[__VLS_ctx.method]) },
});
/** @type {__VLS_StyleScopedClasses['min-w-[65px]']} */ ;
/** @type {__VLS_StyleScopedClasses['rounded-md']} */ ;
/** @type {__VLS_StyleScopedClasses['px-2']} */ ;
/** @type {__VLS_StyleScopedClasses['py-[3px]']} */ ;
/** @type {__VLS_StyleScopedClasses['text-center']} */ ;
/** @type {__VLS_StyleScopedClasses['font-mono']} */ ;
/** @type {__VLS_StyleScopedClasses['text-[11px]']} */ ;
/** @type {__VLS_StyleScopedClasses['font-bold']} */ ;
/** @type {__VLS_StyleScopedClasses['uppercase']} */ ;
/** @type {__VLS_StyleScopedClasses['tracking-wide']} */ ;
/** @type {__VLS_StyleScopedClasses['text-white']} */ ;
(__VLS_ctx.method);
var __VLS_0 = {
    bg: (__VLS_ctx.bgMap[__VLS_ctx.method]),
};
// @ts-ignore
var __VLS_1 = __VLS_0;
// @ts-ignore
[colorMap, method, method, method, bgMap,];
const __VLS_base = (await import('vue')).defineComponent({
    __typeProps: {},
});
const __VLS_export = {};
export default {};
