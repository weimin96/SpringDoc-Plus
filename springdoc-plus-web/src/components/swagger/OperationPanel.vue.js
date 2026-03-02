import { ref, computed } from 'vue';
import MethodBadge from './MethodBadge.vue';
import SchemaView from './SchemaView.vue';
import { useSimulateRequest } from '@/composables/useSimulateRequest';
const props = defineProps();
const expanded = ref(false);
const showSimulate = ref(false);
const opId = computed(() => `op-${props.item.method}-${props.item.path}`);
// 模拟请求
const simulate = useSimulateRequest(props.item);
// 本地引用，用于模板访问
const simulateParams = computed(() => simulate.params);
const simulateRequestBody = computed(() => simulate.requestBody);
const simulateResult = computed(() => simulate.result);
const statusColors = {
    '2': 'bg-green-100 text-green-800',
    '3': 'bg-blue-100 text-blue-800',
    '4': 'bg-amber-100 text-amber-800',
    '5': 'bg-red-100 text-red-800',
};
function statusColor(code) {
    return statusColors[code[0]] ?? 'bg-gray-100 text-gray-700';
}
const cardBg = {
    get: 'border-blue-200 bg-blue-50',
    post: 'border-green-200 bg-green-50',
    put: 'border-amber-200 bg-amber-50',
    delete: 'border-red-200 bg-red-50',
    patch: 'border-purple-200 bg-purple-50',
    head: 'border-yellow-200 bg-yellow-50',
    options: 'border-zinc-200 bg-zinc-50',
    trace: 'border-slate-200 bg-slate-50',
};
const bg = computed(() => cardBg[props.item.method] ?? 'border-[var(--c-border)] bg-white');
const op = computed(() => props.item.operation);
const parameters = computed(() => op.value.parameters ?? []);
const requestBodyContent = computed(() => {
    const rb = op.value.requestBody;
    if (!rb?.content)
        return null;
    return rb.content;
});
const responses = computed(() => Object.entries(op.value.responses ?? {}));
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
    id: (__VLS_ctx.opId),
    ...{ class: "mb-1.5 overflow-hidden rounded-[10px] border" },
    ...{ class: (__VLS_ctx.bg) },
});
/** @type {__VLS_StyleScopedClasses['mb-1.5']} */ ;
/** @type {__VLS_StyleScopedClasses['overflow-hidden']} */ ;
/** @type {__VLS_StyleScopedClasses['rounded-[10px]']} */ ;
/** @type {__VLS_StyleScopedClasses['border']} */ ;
__VLS_asFunctionalElement1(__VLS_intrinsics.button, __VLS_intrinsics.button)({
    ...{ onClick: (...[$event]) => {
            __VLS_ctx.expanded = !__VLS_ctx.expanded;
            // @ts-ignore
            [opId, bg, expanded, expanded,];
        } },
    ...{ class: "flex w-full cursor-pointer items-center gap-3 border-none bg-transparent px-3.5 py-2.5 text-left" },
    ...{ class: (__VLS_ctx.bg) },
});
/** @type {__VLS_StyleScopedClasses['flex']} */ ;
/** @type {__VLS_StyleScopedClasses['w-full']} */ ;
/** @type {__VLS_StyleScopedClasses['cursor-pointer']} */ ;
/** @type {__VLS_StyleScopedClasses['items-center']} */ ;
/** @type {__VLS_StyleScopedClasses['gap-3']} */ ;
/** @type {__VLS_StyleScopedClasses['border-none']} */ ;
/** @type {__VLS_StyleScopedClasses['bg-transparent']} */ ;
/** @type {__VLS_StyleScopedClasses['px-3.5']} */ ;
/** @type {__VLS_StyleScopedClasses['py-2.5']} */ ;
/** @type {__VLS_StyleScopedClasses['text-left']} */ ;
const __VLS_0 = MethodBadge;
// @ts-ignore
const __VLS_1 = __VLS_asFunctionalComponent1(__VLS_0, new __VLS_0({
    method: (__VLS_ctx.item.method),
}));
const __VLS_2 = __VLS_1({
    method: (__VLS_ctx.item.method),
}, ...__VLS_functionalComponentArgsRest(__VLS_1));
__VLS_asFunctionalElement1(__VLS_intrinsics.code, __VLS_intrinsics.code)({
    ...{ class: "flex-1 truncate font-mono text-[12.5px] font-medium text-[var(--c-text)]" },
});
/** @type {__VLS_StyleScopedClasses['flex-1']} */ ;
/** @type {__VLS_StyleScopedClasses['truncate']} */ ;
/** @type {__VLS_StyleScopedClasses['font-mono']} */ ;
/** @type {__VLS_StyleScopedClasses['text-[12.5px]']} */ ;
/** @type {__VLS_StyleScopedClasses['font-medium']} */ ;
/** @type {__VLS_StyleScopedClasses['text-[var(--c-text)]']} */ ;
(__VLS_ctx.item.path);
if (__VLS_ctx.op.deprecated) {
    __VLS_asFunctionalElement1(__VLS_intrinsics.span, __VLS_intrinsics.span)({
        ...{ class: "rounded border border-amber-300 bg-amber-100 px-1.5 py-px text-[10px] font-medium text-amber-700" },
    });
    /** @type {__VLS_StyleScopedClasses['rounded']} */ ;
    /** @type {__VLS_StyleScopedClasses['border']} */ ;
    /** @type {__VLS_StyleScopedClasses['border-amber-300']} */ ;
    /** @type {__VLS_StyleScopedClasses['bg-amber-100']} */ ;
    /** @type {__VLS_StyleScopedClasses['px-1.5']} */ ;
    /** @type {__VLS_StyleScopedClasses['py-px']} */ ;
    /** @type {__VLS_StyleScopedClasses['text-[10px]']} */ ;
    /** @type {__VLS_StyleScopedClasses['font-medium']} */ ;
    /** @type {__VLS_StyleScopedClasses['text-amber-700']} */ ;
}
if (__VLS_ctx.op.summary) {
    __VLS_asFunctionalElement1(__VLS_intrinsics.span, __VLS_intrinsics.span)({
        ...{ class: "hidden max-w-[240px] truncate text-[12.5px] text-[var(--c-muted)] md:block" },
    });
    /** @type {__VLS_StyleScopedClasses['hidden']} */ ;
    /** @type {__VLS_StyleScopedClasses['max-w-[240px]']} */ ;
    /** @type {__VLS_StyleScopedClasses['truncate']} */ ;
    /** @type {__VLS_StyleScopedClasses['text-[12.5px]']} */ ;
    /** @type {__VLS_StyleScopedClasses['text-[var(--c-muted)]']} */ ;
    /** @type {__VLS_StyleScopedClasses['md:block']} */ ;
    (__VLS_ctx.op.summary);
}
__VLS_asFunctionalElement1(__VLS_intrinsics.svg, __VLS_intrinsics.svg)({
    ...{ class: "h-4 w-4 flex-shrink-0 text-[var(--c-muted)] transition-transform duration-200" },
    ...{ class: (__VLS_ctx.expanded ? 'rotate-180' : '') },
    viewBox: "0 0 24 24",
    fill: "none",
    stroke: "currentColor",
    'stroke-width': "2",
    'stroke-linecap': "round",
});
/** @type {__VLS_StyleScopedClasses['h-4']} */ ;
/** @type {__VLS_StyleScopedClasses['w-4']} */ ;
/** @type {__VLS_StyleScopedClasses['flex-shrink-0']} */ ;
/** @type {__VLS_StyleScopedClasses['text-[var(--c-muted)]']} */ ;
/** @type {__VLS_StyleScopedClasses['transition-transform']} */ ;
/** @type {__VLS_StyleScopedClasses['duration-200']} */ ;
__VLS_asFunctionalElement1(__VLS_intrinsics.path)({
    d: "m6 9 6 6 6-6",
});
if (__VLS_ctx.expanded) {
    __VLS_asFunctionalElement1(__VLS_intrinsics.div, __VLS_intrinsics.div)({
        ...{ class: "border-t border-white/60 bg-white/80 px-4 py-4" },
        ...{ style: {} },
    });
    /** @type {__VLS_StyleScopedClasses['border-t']} */ ;
    /** @type {__VLS_StyleScopedClasses['border-white/60']} */ ;
    /** @type {__VLS_StyleScopedClasses['bg-white/80']} */ ;
    /** @type {__VLS_StyleScopedClasses['px-4']} */ ;
    /** @type {__VLS_StyleScopedClasses['py-4']} */ ;
    if (__VLS_ctx.op.description) {
        __VLS_asFunctionalElement1(__VLS_intrinsics.p, __VLS_intrinsics.p)({
            ...{ class: "mb-4 text-[13px] text-[var(--c-muted)]" },
        });
        /** @type {__VLS_StyleScopedClasses['mb-4']} */ ;
        /** @type {__VLS_StyleScopedClasses['text-[13px]']} */ ;
        /** @type {__VLS_StyleScopedClasses['text-[var(--c-muted)]']} */ ;
        (__VLS_ctx.op.description);
    }
    else if (__VLS_ctx.op.summary) {
        __VLS_asFunctionalElement1(__VLS_intrinsics.p, __VLS_intrinsics.p)({
            ...{ class: "mb-4 text-[13px] text-[var(--c-muted)]" },
        });
        /** @type {__VLS_StyleScopedClasses['mb-4']} */ ;
        /** @type {__VLS_StyleScopedClasses['text-[13px]']} */ ;
        /** @type {__VLS_StyleScopedClasses['text-[var(--c-muted)]']} */ ;
        (__VLS_ctx.op.summary);
    }
    __VLS_asFunctionalElement1(__VLS_intrinsics.div, __VLS_intrinsics.div)({
        ...{ class: "mb-4 flex items-center gap-2" },
    });
    /** @type {__VLS_StyleScopedClasses['mb-4']} */ ;
    /** @type {__VLS_StyleScopedClasses['flex']} */ ;
    /** @type {__VLS_StyleScopedClasses['items-center']} */ ;
    /** @type {__VLS_StyleScopedClasses['gap-2']} */ ;
    __VLS_asFunctionalElement1(__VLS_intrinsics.button, __VLS_intrinsics.button)({
        ...{ onClick: (...[$event]) => {
                if (!(__VLS_ctx.expanded))
                    return;
                __VLS_ctx.showSimulate = !__VLS_ctx.showSimulate;
                if (!__VLS_ctx.showSimulate)
                    __VLS_ctx.simulate.reset();
                // @ts-ignore
                [bg, expanded, expanded, item, item, op, op, op, op, op, op, op, showSimulate, showSimulate, showSimulate, simulate,];
            } },
        ...{ class: "rounded-lg bg-[var(--c-primary)] px-4 py-2 text-[13px] font-medium text-white transition-colors hover:opacity-90 disabled:cursor-not-allowed disabled:opacity-50" },
        disabled: (__VLS_ctx.simulate.loading.value),
    });
    /** @type {__VLS_StyleScopedClasses['rounded-lg']} */ ;
    /** @type {__VLS_StyleScopedClasses['bg-[var(--c-primary)]']} */ ;
    /** @type {__VLS_StyleScopedClasses['px-4']} */ ;
    /** @type {__VLS_StyleScopedClasses['py-2']} */ ;
    /** @type {__VLS_StyleScopedClasses['text-[13px]']} */ ;
    /** @type {__VLS_StyleScopedClasses['font-medium']} */ ;
    /** @type {__VLS_StyleScopedClasses['text-white']} */ ;
    /** @type {__VLS_StyleScopedClasses['transition-colors']} */ ;
    /** @type {__VLS_StyleScopedClasses['hover:opacity-90']} */ ;
    /** @type {__VLS_StyleScopedClasses['disabled:cursor-not-allowed']} */ ;
    /** @type {__VLS_StyleScopedClasses['disabled:opacity-50']} */ ;
    (__VLS_ctx.showSimulate ? '关闭模拟请求' : '模拟请求');
    if (__VLS_ctx.showSimulate) {
        __VLS_asFunctionalElement1(__VLS_intrinsics.button, __VLS_intrinsics.button)({
            ...{ onClick: (...[$event]) => {
                    if (!(__VLS_ctx.expanded))
                        return;
                    if (!(__VLS_ctx.showSimulate))
                        return;
                    __VLS_ctx.simulate.sendRequest();
                    // @ts-ignore
                    [showSimulate, showSimulate, simulate, simulate,];
                } },
            ...{ class: "rounded-lg border border-[var(--c-border)] bg-white px-4 py-2 text-[13px] text-[var(--c-text)] transition-colors hover:bg-gray-50 disabled:cursor-not-allowed disabled:opacity-50" },
            disabled: (__VLS_ctx.simulate.loading.value),
        });
        /** @type {__VLS_StyleScopedClasses['rounded-lg']} */ ;
        /** @type {__VLS_StyleScopedClasses['border']} */ ;
        /** @type {__VLS_StyleScopedClasses['border-[var(--c-border)]']} */ ;
        /** @type {__VLS_StyleScopedClasses['bg-white']} */ ;
        /** @type {__VLS_StyleScopedClasses['px-4']} */ ;
        /** @type {__VLS_StyleScopedClasses['py-2']} */ ;
        /** @type {__VLS_StyleScopedClasses['text-[13px]']} */ ;
        /** @type {__VLS_StyleScopedClasses['text-[var(--c-text)]']} */ ;
        /** @type {__VLS_StyleScopedClasses['transition-colors']} */ ;
        /** @type {__VLS_StyleScopedClasses['hover:bg-gray-50']} */ ;
        /** @type {__VLS_StyleScopedClasses['disabled:cursor-not-allowed']} */ ;
        /** @type {__VLS_StyleScopedClasses['disabled:opacity-50']} */ ;
        (__VLS_ctx.simulate.loading.value ? '发送中...' : '发送请求');
    }
    if (__VLS_ctx.showSimulate) {
        __VLS_asFunctionalElement1(__VLS_intrinsics.div, __VLS_intrinsics.div)({
            ...{ class: "mb-4 rounded-lg border border-[var(--c-border)] bg-white" },
        });
        /** @type {__VLS_StyleScopedClasses['mb-4']} */ ;
        /** @type {__VLS_StyleScopedClasses['rounded-lg']} */ ;
        /** @type {__VLS_StyleScopedClasses['border']} */ ;
        /** @type {__VLS_StyleScopedClasses['border-[var(--c-border)]']} */ ;
        /** @type {__VLS_StyleScopedClasses['bg-white']} */ ;
        if (__VLS_ctx.simulateParams.value.length) {
            __VLS_asFunctionalElement1(__VLS_intrinsics.div, __VLS_intrinsics.div)({
                ...{ class: "border-b border-[var(--c-border)] p-3" },
            });
            /** @type {__VLS_StyleScopedClasses['border-b']} */ ;
            /** @type {__VLS_StyleScopedClasses['border-[var(--c-border)]']} */ ;
            /** @type {__VLS_StyleScopedClasses['p-3']} */ ;
            __VLS_asFunctionalElement1(__VLS_intrinsics.h4, __VLS_intrinsics.h4)({
                ...{ class: "mb-2 text-[11px] font-bold uppercase tracking-wider text-[var(--c-muted)]" },
            });
            /** @type {__VLS_StyleScopedClasses['mb-2']} */ ;
            /** @type {__VLS_StyleScopedClasses['text-[11px]']} */ ;
            /** @type {__VLS_StyleScopedClasses['font-bold']} */ ;
            /** @type {__VLS_StyleScopedClasses['uppercase']} */ ;
            /** @type {__VLS_StyleScopedClasses['tracking-wider']} */ ;
            /** @type {__VLS_StyleScopedClasses['text-[var(--c-muted)]']} */ ;
            __VLS_asFunctionalElement1(__VLS_intrinsics.div, __VLS_intrinsics.div)({
                ...{ class: "space-y-2" },
            });
            /** @type {__VLS_StyleScopedClasses['space-y-2']} */ ;
            for (const [param] of __VLS_vFor((__VLS_ctx.simulateParams.value))) {
                __VLS_asFunctionalElement1(__VLS_intrinsics.div, __VLS_intrinsics.div)({
                    key: (param.name),
                    ...{ class: "flex items-center gap-2" },
                });
                /** @type {__VLS_StyleScopedClasses['flex']} */ ;
                /** @type {__VLS_StyleScopedClasses['items-center']} */ ;
                /** @type {__VLS_StyleScopedClasses['gap-2']} */ ;
                __VLS_asFunctionalElement1(__VLS_intrinsics.span, __VLS_intrinsics.span)({
                    ...{ class: "w-24 shrink-0 font-mono text-[12px]" },
                });
                /** @type {__VLS_StyleScopedClasses['w-24']} */ ;
                /** @type {__VLS_StyleScopedClasses['shrink-0']} */ ;
                /** @type {__VLS_StyleScopedClasses['font-mono']} */ ;
                /** @type {__VLS_StyleScopedClasses['text-[12px]']} */ ;
                (param.name);
                __VLS_asFunctionalElement1(__VLS_intrinsics.span, __VLS_intrinsics.span)({
                    ...{ class: "w-12 shrink-0 rounded px-1.5 py-0.5 text-[10px] font-medium text-center" },
                    ...{ class: ({
                            'bg-blue-100 text-blue-700': param.in === 'path',
                            'bg-green-100 text-green-700': param.in === 'query',
                            'bg-purple-100 text-purple-700': param.in === 'header',
                            'bg-amber-100 text-amber-700': param.in === 'cookie',
                        }) },
                });
                /** @type {__VLS_StyleScopedClasses['w-12']} */ ;
                /** @type {__VLS_StyleScopedClasses['shrink-0']} */ ;
                /** @type {__VLS_StyleScopedClasses['rounded']} */ ;
                /** @type {__VLS_StyleScopedClasses['px-1.5']} */ ;
                /** @type {__VLS_StyleScopedClasses['py-0.5']} */ ;
                /** @type {__VLS_StyleScopedClasses['text-[10px]']} */ ;
                /** @type {__VLS_StyleScopedClasses['font-medium']} */ ;
                /** @type {__VLS_StyleScopedClasses['text-center']} */ ;
                /** @type {__VLS_StyleScopedClasses['bg-blue-100']} */ ;
                /** @type {__VLS_StyleScopedClasses['text-blue-700']} */ ;
                /** @type {__VLS_StyleScopedClasses['bg-green-100']} */ ;
                /** @type {__VLS_StyleScopedClasses['text-green-700']} */ ;
                /** @type {__VLS_StyleScopedClasses['bg-purple-100']} */ ;
                /** @type {__VLS_StyleScopedClasses['text-purple-700']} */ ;
                /** @type {__VLS_StyleScopedClasses['bg-amber-100']} */ ;
                /** @type {__VLS_StyleScopedClasses['text-amber-700']} */ ;
                (param.in);
                __VLS_asFunctionalElement1(__VLS_intrinsics.input)({
                    ...{ onInput: (...[$event]) => {
                            if (!(__VLS_ctx.expanded))
                                return;
                            if (!(__VLS_ctx.showSimulate))
                                return;
                            if (!(__VLS_ctx.simulateParams.value.length))
                                return;
                            param.value = $event.target.value;
                            // @ts-ignore
                            [showSimulate, simulate, simulate, simulateParams, simulateParams,];
                        } },
                    value: (param.value),
                    ...{ class: "flex-1 rounded border border-[var(--c-border)] px-2 py-1 text-[12px] outline-none focus:border-[var(--c-primary)]" },
                    placeholder: (param.type || 'Value'),
                });
                /** @type {__VLS_StyleScopedClasses['flex-1']} */ ;
                /** @type {__VLS_StyleScopedClasses['rounded']} */ ;
                /** @type {__VLS_StyleScopedClasses['border']} */ ;
                /** @type {__VLS_StyleScopedClasses['border-[var(--c-border)]']} */ ;
                /** @type {__VLS_StyleScopedClasses['px-2']} */ ;
                /** @type {__VLS_StyleScopedClasses['py-1']} */ ;
                /** @type {__VLS_StyleScopedClasses['text-[12px]']} */ ;
                /** @type {__VLS_StyleScopedClasses['outline-none']} */ ;
                /** @type {__VLS_StyleScopedClasses['focus:border-[var(--c-primary)]']} */ ;
                if (param.required) {
                    __VLS_asFunctionalElement1(__VLS_intrinsics.span, __VLS_intrinsics.span)({
                        ...{ class: "shrink-0 text-[10px] text-red-500" },
                    });
                    /** @type {__VLS_StyleScopedClasses['shrink-0']} */ ;
                    /** @type {__VLS_StyleScopedClasses['text-[10px]']} */ ;
                    /** @type {__VLS_StyleScopedClasses['text-red-500']} */ ;
                }
                // @ts-ignore
                [];
            }
        }
        if (__VLS_ctx.simulateRequestBody.value) {
            __VLS_asFunctionalElement1(__VLS_intrinsics.div, __VLS_intrinsics.div)({
                ...{ class: "border-b border-[var(--c-border)] p-3" },
            });
            /** @type {__VLS_StyleScopedClasses['border-b']} */ ;
            /** @type {__VLS_StyleScopedClasses['border-[var(--c-border)]']} */ ;
            /** @type {__VLS_StyleScopedClasses['p-3']} */ ;
            __VLS_asFunctionalElement1(__VLS_intrinsics.h4, __VLS_intrinsics.h4)({
                ...{ class: "mb-2 text-[11px] font-bold uppercase tracking-wider text-[var(--c-muted)]" },
            });
            /** @type {__VLS_StyleScopedClasses['mb-2']} */ ;
            /** @type {__VLS_StyleScopedClasses['text-[11px]']} */ ;
            /** @type {__VLS_StyleScopedClasses['font-bold']} */ ;
            /** @type {__VLS_StyleScopedClasses['uppercase']} */ ;
            /** @type {__VLS_StyleScopedClasses['tracking-wider']} */ ;
            /** @type {__VLS_StyleScopedClasses['text-[var(--c-muted)]']} */ ;
            __VLS_asFunctionalElement1(__VLS_intrinsics.textarea)({
                ...{ onInput: (...[$event]) => {
                        if (!(__VLS_ctx.expanded))
                            return;
                        if (!(__VLS_ctx.showSimulate))
                            return;
                        if (!(__VLS_ctx.simulateRequestBody.value))
                            return;
                        __VLS_ctx.simulate.requestBody.value = $event.target.value;
                        // @ts-ignore
                        [simulate, simulateRequestBody,];
                    } },
                value: (__VLS_ctx.simulateRequestBody.value),
                rows: "6",
                ...{ class: "w-full resize-y rounded border border-[var(--c-border)] bg-gray-50 p-2 font-mono text-[11px] outline-none focus:border-[var(--c-primary)]" },
                spellcheck: "false",
            });
            /** @type {__VLS_StyleScopedClasses['w-full']} */ ;
            /** @type {__VLS_StyleScopedClasses['resize-y']} */ ;
            /** @type {__VLS_StyleScopedClasses['rounded']} */ ;
            /** @type {__VLS_StyleScopedClasses['border']} */ ;
            /** @type {__VLS_StyleScopedClasses['border-[var(--c-border)]']} */ ;
            /** @type {__VLS_StyleScopedClasses['bg-gray-50']} */ ;
            /** @type {__VLS_StyleScopedClasses['p-2']} */ ;
            /** @type {__VLS_StyleScopedClasses['font-mono']} */ ;
            /** @type {__VLS_StyleScopedClasses['text-[11px]']} */ ;
            /** @type {__VLS_StyleScopedClasses['outline-none']} */ ;
            /** @type {__VLS_StyleScopedClasses['focus:border-[var(--c-primary)]']} */ ;
        }
        if (__VLS_ctx.simulateResult.value || __VLS_ctx.simulate.error.value) {
            __VLS_asFunctionalElement1(__VLS_intrinsics.div, __VLS_intrinsics.div)({
                ...{ class: "p-3" },
            });
            /** @type {__VLS_StyleScopedClasses['p-3']} */ ;
            __VLS_asFunctionalElement1(__VLS_intrinsics.h4, __VLS_intrinsics.h4)({
                ...{ class: "mb-2 text-[11px] font-bold uppercase tracking-wider text-[var(--c-muted)]" },
            });
            /** @type {__VLS_StyleScopedClasses['mb-2']} */ ;
            /** @type {__VLS_StyleScopedClasses['text-[11px]']} */ ;
            /** @type {__VLS_StyleScopedClasses['font-bold']} */ ;
            /** @type {__VLS_StyleScopedClasses['uppercase']} */ ;
            /** @type {__VLS_StyleScopedClasses['tracking-wider']} */ ;
            /** @type {__VLS_StyleScopedClasses['text-[var(--c-muted)]']} */ ;
            if (__VLS_ctx.simulateResult.value) {
                __VLS_asFunctionalElement1(__VLS_intrinsics.span, __VLS_intrinsics.span)({
                    ...{ class: "ml-2 font-normal normal-case" },
                });
                /** @type {__VLS_StyleScopedClasses['ml-2']} */ ;
                /** @type {__VLS_StyleScopedClasses['font-normal']} */ ;
                /** @type {__VLS_StyleScopedClasses['normal-case']} */ ;
                __VLS_asFunctionalElement1(__VLS_intrinsics.span, __VLS_intrinsics.span)({
                    ...{ class: "rounded px-2 py-0.5 font-mono text-[11px] font-bold" },
                    ...{ class: ({
                            'bg-green-100 text-green-800': __VLS_ctx.simulateResult.value.status < 300,
                            'bg-blue-100 text-blue-800': __VLS_ctx.simulateResult.value.status >= 300 && __VLS_ctx.simulateResult.value.status < 400,
                            'bg-amber-100 text-amber-800': __VLS_ctx.simulateResult.value.status >= 400 && __VLS_ctx.simulateResult.value.status < 500,
                            'bg-red-100 text-red-800': __VLS_ctx.simulateResult.value.status >= 500,
                        }) },
                });
                /** @type {__VLS_StyleScopedClasses['rounded']} */ ;
                /** @type {__VLS_StyleScopedClasses['px-2']} */ ;
                /** @type {__VLS_StyleScopedClasses['py-0.5']} */ ;
                /** @type {__VLS_StyleScopedClasses['font-mono']} */ ;
                /** @type {__VLS_StyleScopedClasses['text-[11px]']} */ ;
                /** @type {__VLS_StyleScopedClasses['font-bold']} */ ;
                /** @type {__VLS_StyleScopedClasses['bg-green-100']} */ ;
                /** @type {__VLS_StyleScopedClasses['text-green-800']} */ ;
                /** @type {__VLS_StyleScopedClasses['bg-blue-100']} */ ;
                /** @type {__VLS_StyleScopedClasses['text-blue-800']} */ ;
                /** @type {__VLS_StyleScopedClasses['bg-amber-100']} */ ;
                /** @type {__VLS_StyleScopedClasses['text-amber-800']} */ ;
                /** @type {__VLS_StyleScopedClasses['bg-red-100']} */ ;
                /** @type {__VLS_StyleScopedClasses['text-red-800']} */ ;
                (__VLS_ctx.simulateResult.value.status);
                (__VLS_ctx.simulateResult.value.statusText);
                __VLS_asFunctionalElement1(__VLS_intrinsics.span, __VLS_intrinsics.span)({
                    ...{ class: "ml-2 text-[var(--c-muted)]" },
                });
                /** @type {__VLS_StyleScopedClasses['ml-2']} */ ;
                /** @type {__VLS_StyleScopedClasses['text-[var(--c-muted)]']} */ ;
                (__VLS_ctx.simulateResult.value.duration);
            }
            if (__VLS_ctx.simulate.error.value) {
                __VLS_asFunctionalElement1(__VLS_intrinsics.div, __VLS_intrinsics.div)({
                    ...{ class: "rounded-lg border border-red-200 bg-red-50 p-3 text-[12px] text-red-700" },
                });
                /** @type {__VLS_StyleScopedClasses['rounded-lg']} */ ;
                /** @type {__VLS_StyleScopedClasses['border']} */ ;
                /** @type {__VLS_StyleScopedClasses['border-red-200']} */ ;
                /** @type {__VLS_StyleScopedClasses['bg-red-50']} */ ;
                /** @type {__VLS_StyleScopedClasses['p-3']} */ ;
                /** @type {__VLS_StyleScopedClasses['text-[12px]']} */ ;
                /** @type {__VLS_StyleScopedClasses['text-red-700']} */ ;
                (__VLS_ctx.simulate.error.value);
            }
            if (__VLS_ctx.simulateResult.value?.headers) {
                __VLS_asFunctionalElement1(__VLS_intrinsics.div, __VLS_intrinsics.div)({
                    ...{ class: "mb-2 rounded-lg border border-[var(--c-border)] bg-gray-50 p-2" },
                });
                /** @type {__VLS_StyleScopedClasses['mb-2']} */ ;
                /** @type {__VLS_StyleScopedClasses['rounded-lg']} */ ;
                /** @type {__VLS_StyleScopedClasses['border']} */ ;
                /** @type {__VLS_StyleScopedClasses['border-[var(--c-border)]']} */ ;
                /** @type {__VLS_StyleScopedClasses['bg-gray-50']} */ ;
                /** @type {__VLS_StyleScopedClasses['p-2']} */ ;
                __VLS_asFunctionalElement1(__VLS_intrinsics.p, __VLS_intrinsics.p)({
                    ...{ class: "mb-1 text-[10px] font-bold uppercase tracking-wider text-[var(--c-muted)]" },
                });
                /** @type {__VLS_StyleScopedClasses['mb-1']} */ ;
                /** @type {__VLS_StyleScopedClasses['text-[10px]']} */ ;
                /** @type {__VLS_StyleScopedClasses['font-bold']} */ ;
                /** @type {__VLS_StyleScopedClasses['uppercase']} */ ;
                /** @type {__VLS_StyleScopedClasses['tracking-wider']} */ ;
                /** @type {__VLS_StyleScopedClasses['text-[var(--c-muted)]']} */ ;
                for (const [value, key] of __VLS_vFor((__VLS_ctx.simulateResult.value.headers))) {
                    __VLS_asFunctionalElement1(__VLS_intrinsics.div, __VLS_intrinsics.div)({
                        key: (key),
                        ...{ class: "flex text-[11px]" },
                    });
                    /** @type {__VLS_StyleScopedClasses['flex']} */ ;
                    /** @type {__VLS_StyleScopedClasses['text-[11px]']} */ ;
                    __VLS_asFunctionalElement1(__VLS_intrinsics.span, __VLS_intrinsics.span)({
                        ...{ class: "w-32 shrink-0 font-mono text-[var(--c-muted)]" },
                    });
                    /** @type {__VLS_StyleScopedClasses['w-32']} */ ;
                    /** @type {__VLS_StyleScopedClasses['shrink-0']} */ ;
                    /** @type {__VLS_StyleScopedClasses['font-mono']} */ ;
                    /** @type {__VLS_StyleScopedClasses['text-[var(--c-muted)]']} */ ;
                    (key);
                    __VLS_asFunctionalElement1(__VLS_intrinsics.span, __VLS_intrinsics.span)({
                        ...{ class: "flex-1 truncate font-mono" },
                    });
                    /** @type {__VLS_StyleScopedClasses['flex-1']} */ ;
                    /** @type {__VLS_StyleScopedClasses['truncate']} */ ;
                    /** @type {__VLS_StyleScopedClasses['font-mono']} */ ;
                    (value);
                    // @ts-ignore
                    [simulate, simulate, simulate, simulateRequestBody, simulateResult, simulateResult, simulateResult, simulateResult, simulateResult, simulateResult, simulateResult, simulateResult, simulateResult, simulateResult, simulateResult, simulateResult, simulateResult,];
                }
            }
            if (__VLS_ctx.simulateResult.value?.data !== undefined) {
                __VLS_asFunctionalElement1(__VLS_intrinsics.div, __VLS_intrinsics.div)({
                    ...{ class: "rounded-lg border border-[var(--c-border)] bg-gray-50 p-2" },
                });
                /** @type {__VLS_StyleScopedClasses['rounded-lg']} */ ;
                /** @type {__VLS_StyleScopedClasses['border']} */ ;
                /** @type {__VLS_StyleScopedClasses['border-[var(--c-border)]']} */ ;
                /** @type {__VLS_StyleScopedClasses['bg-gray-50']} */ ;
                /** @type {__VLS_StyleScopedClasses['p-2']} */ ;
                __VLS_asFunctionalElement1(__VLS_intrinsics.p, __VLS_intrinsics.p)({
                    ...{ class: "mb-1 text-[10px] font-bold uppercase tracking-wider text-[var(--c-muted)]" },
                });
                /** @type {__VLS_StyleScopedClasses['mb-1']} */ ;
                /** @type {__VLS_StyleScopedClasses['text-[10px]']} */ ;
                /** @type {__VLS_StyleScopedClasses['font-bold']} */ ;
                /** @type {__VLS_StyleScopedClasses['uppercase']} */ ;
                /** @type {__VLS_StyleScopedClasses['tracking-wider']} */ ;
                /** @type {__VLS_StyleScopedClasses['text-[var(--c-muted)]']} */ ;
                __VLS_asFunctionalElement1(__VLS_intrinsics.pre, __VLS_intrinsics.pre)({
                    ...{ class: "max-h-[400px] overflow-auto text-[11px]" },
                });
                /** @type {__VLS_StyleScopedClasses['max-h-[400px]']} */ ;
                /** @type {__VLS_StyleScopedClasses['overflow-auto']} */ ;
                /** @type {__VLS_StyleScopedClasses['text-[11px]']} */ ;
                (JSON.stringify(__VLS_ctx.simulateResult.value.data, null, 2));
            }
        }
    }
    if (__VLS_ctx.parameters.length) {
        __VLS_asFunctionalElement1(__VLS_intrinsics.div, __VLS_intrinsics.div)({
            ...{ class: "mb-4" },
        });
        /** @type {__VLS_StyleScopedClasses['mb-4']} */ ;
        __VLS_asFunctionalElement1(__VLS_intrinsics.h4, __VLS_intrinsics.h4)({
            ...{ class: "mb-2 text-[11px] font-bold uppercase tracking-wider text-[var(--c-muted)]" },
        });
        /** @type {__VLS_StyleScopedClasses['mb-2']} */ ;
        /** @type {__VLS_StyleScopedClasses['text-[11px]']} */ ;
        /** @type {__VLS_StyleScopedClasses['font-bold']} */ ;
        /** @type {__VLS_StyleScopedClasses['uppercase']} */ ;
        /** @type {__VLS_StyleScopedClasses['tracking-wider']} */ ;
        /** @type {__VLS_StyleScopedClasses['text-[var(--c-muted)]']} */ ;
        __VLS_asFunctionalElement1(__VLS_intrinsics.table, __VLS_intrinsics.table)({
            ...{ class: "w-full border-collapse text-xs" },
        });
        /** @type {__VLS_StyleScopedClasses['w-full']} */ ;
        /** @type {__VLS_StyleScopedClasses['border-collapse']} */ ;
        /** @type {__VLS_StyleScopedClasses['text-xs']} */ ;
        __VLS_asFunctionalElement1(__VLS_intrinsics.thead, __VLS_intrinsics.thead)({});
        __VLS_asFunctionalElement1(__VLS_intrinsics.tr, __VLS_intrinsics.tr)({
            ...{ class: "border-b-2 border-[var(--c-border)]" },
        });
        /** @type {__VLS_StyleScopedClasses['border-b-2']} */ ;
        /** @type {__VLS_StyleScopedClasses['border-[var(--c-border)]']} */ ;
        __VLS_asFunctionalElement1(__VLS_intrinsics.th, __VLS_intrinsics.th)({
            ...{ class: "py-1.5 pr-3 text-left font-semibold text-[var(--c-muted)]" },
        });
        /** @type {__VLS_StyleScopedClasses['py-1.5']} */ ;
        /** @type {__VLS_StyleScopedClasses['pr-3']} */ ;
        /** @type {__VLS_StyleScopedClasses['text-left']} */ ;
        /** @type {__VLS_StyleScopedClasses['font-semibold']} */ ;
        /** @type {__VLS_StyleScopedClasses['text-[var(--c-muted)]']} */ ;
        __VLS_asFunctionalElement1(__VLS_intrinsics.th, __VLS_intrinsics.th)({
            ...{ class: "py-1.5 pr-3 text-left font-semibold text-[var(--c-muted)]" },
        });
        /** @type {__VLS_StyleScopedClasses['py-1.5']} */ ;
        /** @type {__VLS_StyleScopedClasses['pr-3']} */ ;
        /** @type {__VLS_StyleScopedClasses['text-left']} */ ;
        /** @type {__VLS_StyleScopedClasses['font-semibold']} */ ;
        /** @type {__VLS_StyleScopedClasses['text-[var(--c-muted)]']} */ ;
        __VLS_asFunctionalElement1(__VLS_intrinsics.th, __VLS_intrinsics.th)({
            ...{ class: "py-1.5 pr-3 text-left font-semibold text-[var(--c-muted)]" },
        });
        /** @type {__VLS_StyleScopedClasses['py-1.5']} */ ;
        /** @type {__VLS_StyleScopedClasses['pr-3']} */ ;
        /** @type {__VLS_StyleScopedClasses['text-left']} */ ;
        /** @type {__VLS_StyleScopedClasses['font-semibold']} */ ;
        /** @type {__VLS_StyleScopedClasses['text-[var(--c-muted)]']} */ ;
        __VLS_asFunctionalElement1(__VLS_intrinsics.th, __VLS_intrinsics.th)({
            ...{ class: "py-1.5 pr-3 text-left font-semibold text-[var(--c-muted)]" },
        });
        /** @type {__VLS_StyleScopedClasses['py-1.5']} */ ;
        /** @type {__VLS_StyleScopedClasses['pr-3']} */ ;
        /** @type {__VLS_StyleScopedClasses['text-left']} */ ;
        /** @type {__VLS_StyleScopedClasses['font-semibold']} */ ;
        /** @type {__VLS_StyleScopedClasses['text-[var(--c-muted)]']} */ ;
        __VLS_asFunctionalElement1(__VLS_intrinsics.th, __VLS_intrinsics.th)({
            ...{ class: "py-1.5 text-left font-semibold text-[var(--c-muted)]" },
        });
        /** @type {__VLS_StyleScopedClasses['py-1.5']} */ ;
        /** @type {__VLS_StyleScopedClasses['text-left']} */ ;
        /** @type {__VLS_StyleScopedClasses['font-semibold']} */ ;
        /** @type {__VLS_StyleScopedClasses['text-[var(--c-muted)]']} */ ;
        __VLS_asFunctionalElement1(__VLS_intrinsics.tbody, __VLS_intrinsics.tbody)({});
        for (const [p] of __VLS_vFor((__VLS_ctx.parameters))) {
            __VLS_asFunctionalElement1(__VLS_intrinsics.tr, __VLS_intrinsics.tr)({
                key: (`${p.in}-${p.name}`),
                ...{ class: "border-b border-[var(--c-border)] last:border-0" },
            });
            /** @type {__VLS_StyleScopedClasses['border-b']} */ ;
            /** @type {__VLS_StyleScopedClasses['border-[var(--c-border)]']} */ ;
            /** @type {__VLS_StyleScopedClasses['last:border-0']} */ ;
            __VLS_asFunctionalElement1(__VLS_intrinsics.td, __VLS_intrinsics.td)({
                ...{ class: "py-1.5 pr-3 font-mono font-medium text-[var(--c-text)]" },
            });
            /** @type {__VLS_StyleScopedClasses['py-1.5']} */ ;
            /** @type {__VLS_StyleScopedClasses['pr-3']} */ ;
            /** @type {__VLS_StyleScopedClasses['font-mono']} */ ;
            /** @type {__VLS_StyleScopedClasses['font-medium']} */ ;
            /** @type {__VLS_StyleScopedClasses['text-[var(--c-text)]']} */ ;
            (p.name);
            __VLS_asFunctionalElement1(__VLS_intrinsics.td, __VLS_intrinsics.td)({
                ...{ class: "py-1.5 pr-3" },
            });
            /** @type {__VLS_StyleScopedClasses['py-1.5']} */ ;
            /** @type {__VLS_StyleScopedClasses['pr-3']} */ ;
            __VLS_asFunctionalElement1(__VLS_intrinsics.span, __VLS_intrinsics.span)({
                ...{ class: "rounded px-1.5 py-px text-[10px] font-medium" },
                ...{ class: ({
                        'bg-blue-100 text-blue-700': p.in === 'path',
                        'bg-green-100 text-green-700': p.in === 'query',
                        'bg-purple-100 text-purple-700': p.in === 'header',
                        'bg-amber-100 text-amber-700': p.in === 'cookie',
                    }) },
            });
            /** @type {__VLS_StyleScopedClasses['rounded']} */ ;
            /** @type {__VLS_StyleScopedClasses['px-1.5']} */ ;
            /** @type {__VLS_StyleScopedClasses['py-px']} */ ;
            /** @type {__VLS_StyleScopedClasses['text-[10px]']} */ ;
            /** @type {__VLS_StyleScopedClasses['font-medium']} */ ;
            /** @type {__VLS_StyleScopedClasses['bg-blue-100']} */ ;
            /** @type {__VLS_StyleScopedClasses['text-blue-700']} */ ;
            /** @type {__VLS_StyleScopedClasses['bg-green-100']} */ ;
            /** @type {__VLS_StyleScopedClasses['text-green-700']} */ ;
            /** @type {__VLS_StyleScopedClasses['bg-purple-100']} */ ;
            /** @type {__VLS_StyleScopedClasses['text-purple-700']} */ ;
            /** @type {__VLS_StyleScopedClasses['bg-amber-100']} */ ;
            /** @type {__VLS_StyleScopedClasses['text-amber-700']} */ ;
            (p.in);
            __VLS_asFunctionalElement1(__VLS_intrinsics.td, __VLS_intrinsics.td)({
                ...{ class: "py-1.5 pr-3 font-mono text-[var(--c-muted)]" },
            });
            /** @type {__VLS_StyleScopedClasses['py-1.5']} */ ;
            /** @type {__VLS_StyleScopedClasses['pr-3']} */ ;
            /** @type {__VLS_StyleScopedClasses['font-mono']} */ ;
            /** @type {__VLS_StyleScopedClasses['text-[var(--c-muted)]']} */ ;
            (p.schema?.type ?? '?');
            __VLS_asFunctionalElement1(__VLS_intrinsics.td, __VLS_intrinsics.td)({
                ...{ class: "py-1.5 pr-3" },
            });
            /** @type {__VLS_StyleScopedClasses['py-1.5']} */ ;
            /** @type {__VLS_StyleScopedClasses['pr-3']} */ ;
            if (p.required) {
                __VLS_asFunctionalElement1(__VLS_intrinsics.span, __VLS_intrinsics.span)({
                    ...{ class: "text-red-500" },
                });
                /** @type {__VLS_StyleScopedClasses['text-red-500']} */ ;
            }
            else {
                __VLS_asFunctionalElement1(__VLS_intrinsics.span, __VLS_intrinsics.span)({
                    ...{ class: "text-[var(--c-muted)]" },
                });
                /** @type {__VLS_StyleScopedClasses['text-[var(--c-muted)]']} */ ;
            }
            __VLS_asFunctionalElement1(__VLS_intrinsics.td, __VLS_intrinsics.td)({
                ...{ class: "py-1.5 text-[var(--c-muted)]" },
            });
            /** @type {__VLS_StyleScopedClasses['py-1.5']} */ ;
            /** @type {__VLS_StyleScopedClasses['text-[var(--c-muted)]']} */ ;
            (p.description ?? '-');
            // @ts-ignore
            [simulateResult, simulateResult, parameters, parameters,];
        }
    }
    if (__VLS_ctx.requestBodyContent) {
        __VLS_asFunctionalElement1(__VLS_intrinsics.div, __VLS_intrinsics.div)({
            ...{ class: "mb-4" },
        });
        /** @type {__VLS_StyleScopedClasses['mb-4']} */ ;
        __VLS_asFunctionalElement1(__VLS_intrinsics.h4, __VLS_intrinsics.h4)({
            ...{ class: "mb-2 text-[11px] font-bold uppercase tracking-wider text-[var(--c-muted)]" },
        });
        /** @type {__VLS_StyleScopedClasses['mb-2']} */ ;
        /** @type {__VLS_StyleScopedClasses['text-[11px]']} */ ;
        /** @type {__VLS_StyleScopedClasses['font-bold']} */ ;
        /** @type {__VLS_StyleScopedClasses['uppercase']} */ ;
        /** @type {__VLS_StyleScopedClasses['tracking-wider']} */ ;
        /** @type {__VLS_StyleScopedClasses['text-[var(--c-muted)]']} */ ;
        if (__VLS_ctx.op.requestBody?.required) {
            __VLS_asFunctionalElement1(__VLS_intrinsics.span, __VLS_intrinsics.span)({
                ...{ class: "ml-1 text-red-500 normal-case" },
            });
            /** @type {__VLS_StyleScopedClasses['ml-1']} */ ;
            /** @type {__VLS_StyleScopedClasses['text-red-500']} */ ;
            /** @type {__VLS_StyleScopedClasses['normal-case']} */ ;
        }
        for (const [media, contentType] of __VLS_vFor((__VLS_ctx.requestBodyContent))) {
            __VLS_asFunctionalElement1(__VLS_intrinsics.div, __VLS_intrinsics.div)({
                key: (contentType),
                ...{ class: "mb-2" },
            });
            /** @type {__VLS_StyleScopedClasses['mb-2']} */ ;
            __VLS_asFunctionalElement1(__VLS_intrinsics.span, __VLS_intrinsics.span)({
                ...{ class: "mb-1.5 inline-block rounded bg-gray-100 px-1.5 py-px font-mono text-[10px] text-[var(--c-muted)]" },
            });
            /** @type {__VLS_StyleScopedClasses['mb-1.5']} */ ;
            /** @type {__VLS_StyleScopedClasses['inline-block']} */ ;
            /** @type {__VLS_StyleScopedClasses['rounded']} */ ;
            /** @type {__VLS_StyleScopedClasses['bg-gray-100']} */ ;
            /** @type {__VLS_StyleScopedClasses['px-1.5']} */ ;
            /** @type {__VLS_StyleScopedClasses['py-px']} */ ;
            /** @type {__VLS_StyleScopedClasses['font-mono']} */ ;
            /** @type {__VLS_StyleScopedClasses['text-[10px]']} */ ;
            /** @type {__VLS_StyleScopedClasses['text-[var(--c-muted)]']} */ ;
            (contentType);
            __VLS_asFunctionalElement1(__VLS_intrinsics.div, __VLS_intrinsics.div)({
                ...{ class: "rounded-lg border border-[var(--c-border)] bg-gray-50 p-3" },
            });
            /** @type {__VLS_StyleScopedClasses['rounded-lg']} */ ;
            /** @type {__VLS_StyleScopedClasses['border']} */ ;
            /** @type {__VLS_StyleScopedClasses['border-[var(--c-border)]']} */ ;
            /** @type {__VLS_StyleScopedClasses['bg-gray-50']} */ ;
            /** @type {__VLS_StyleScopedClasses['p-3']} */ ;
            if (media.schema) {
                const __VLS_5 = SchemaView;
                // @ts-ignore
                const __VLS_6 = __VLS_asFunctionalComponent1(__VLS_5, new __VLS_5({
                    schema: (media.schema),
                    schemas: (__VLS_ctx.schemas),
                }));
                const __VLS_7 = __VLS_6({
                    schema: (media.schema),
                    schemas: (__VLS_ctx.schemas),
                }, ...__VLS_functionalComponentArgsRest(__VLS_6));
            }
            // @ts-ignore
            [op, requestBodyContent, requestBodyContent, schemas,];
        }
    }
    if (__VLS_ctx.responses.length) {
        __VLS_asFunctionalElement1(__VLS_intrinsics.div, __VLS_intrinsics.div)({});
        __VLS_asFunctionalElement1(__VLS_intrinsics.h4, __VLS_intrinsics.h4)({
            ...{ class: "mb-2 text-[11px] font-bold uppercase tracking-wider text-[var(--c-muted)]" },
        });
        /** @type {__VLS_StyleScopedClasses['mb-2']} */ ;
        /** @type {__VLS_StyleScopedClasses['text-[11px]']} */ ;
        /** @type {__VLS_StyleScopedClasses['font-bold']} */ ;
        /** @type {__VLS_StyleScopedClasses['uppercase']} */ ;
        /** @type {__VLS_StyleScopedClasses['tracking-wider']} */ ;
        /** @type {__VLS_StyleScopedClasses['text-[var(--c-muted)]']} */ ;
        __VLS_asFunctionalElement1(__VLS_intrinsics.div, __VLS_intrinsics.div)({
            ...{ class: "space-y-2" },
        });
        /** @type {__VLS_StyleScopedClasses['space-y-2']} */ ;
        for (const [[code, resp]] of __VLS_vFor((__VLS_ctx.responses))) {
            __VLS_asFunctionalElement1(__VLS_intrinsics.div, __VLS_intrinsics.div)({
                key: (code),
                ...{ class: "overflow-hidden rounded-lg border border-[var(--c-border)] bg-white" },
            });
            /** @type {__VLS_StyleScopedClasses['overflow-hidden']} */ ;
            /** @type {__VLS_StyleScopedClasses['rounded-lg']} */ ;
            /** @type {__VLS_StyleScopedClasses['border']} */ ;
            /** @type {__VLS_StyleScopedClasses['border-[var(--c-border)]']} */ ;
            /** @type {__VLS_StyleScopedClasses['bg-white']} */ ;
            __VLS_asFunctionalElement1(__VLS_intrinsics.div, __VLS_intrinsics.div)({
                ...{ class: "flex items-center gap-2 px-3 py-2" },
            });
            /** @type {__VLS_StyleScopedClasses['flex']} */ ;
            /** @type {__VLS_StyleScopedClasses['items-center']} */ ;
            /** @type {__VLS_StyleScopedClasses['gap-2']} */ ;
            /** @type {__VLS_StyleScopedClasses['px-3']} */ ;
            /** @type {__VLS_StyleScopedClasses['py-2']} */ ;
            __VLS_asFunctionalElement1(__VLS_intrinsics.span, __VLS_intrinsics.span)({
                ...{ class: "rounded px-2 py-0.5 font-mono text-[11px] font-bold" },
                ...{ class: (__VLS_ctx.statusColor(code)) },
            });
            /** @type {__VLS_StyleScopedClasses['rounded']} */ ;
            /** @type {__VLS_StyleScopedClasses['px-2']} */ ;
            /** @type {__VLS_StyleScopedClasses['py-0.5']} */ ;
            /** @type {__VLS_StyleScopedClasses['font-mono']} */ ;
            /** @type {__VLS_StyleScopedClasses['text-[11px]']} */ ;
            /** @type {__VLS_StyleScopedClasses['font-bold']} */ ;
            (code);
            __VLS_asFunctionalElement1(__VLS_intrinsics.span, __VLS_intrinsics.span)({
                ...{ class: "text-[13px] text-[var(--c-muted)]" },
            });
            /** @type {__VLS_StyleScopedClasses['text-[13px]']} */ ;
            /** @type {__VLS_StyleScopedClasses['text-[var(--c-muted)]']} */ ;
            (resp.description ?? '');
            if (resp.content) {
                __VLS_asFunctionalElement1(__VLS_intrinsics.div, __VLS_intrinsics.div)({
                    ...{ class: "border-t border-[var(--c-border)] px-3 pb-3 pt-2" },
                });
                /** @type {__VLS_StyleScopedClasses['border-t']} */ ;
                /** @type {__VLS_StyleScopedClasses['border-[var(--c-border)]']} */ ;
                /** @type {__VLS_StyleScopedClasses['px-3']} */ ;
                /** @type {__VLS_StyleScopedClasses['pb-3']} */ ;
                /** @type {__VLS_StyleScopedClasses['pt-2']} */ ;
                for (const [media, ct] of __VLS_vFor((resp.content))) {
                    __VLS_asFunctionalElement1(__VLS_intrinsics.div, __VLS_intrinsics.div)({
                        key: (ct),
                    });
                    __VLS_asFunctionalElement1(__VLS_intrinsics.span, __VLS_intrinsics.span)({
                        ...{ class: "mb-1 inline-block rounded bg-gray-100 px-1.5 py-px font-mono text-[10px] text-[var(--c-muted)]" },
                    });
                    /** @type {__VLS_StyleScopedClasses['mb-1']} */ ;
                    /** @type {__VLS_StyleScopedClasses['inline-block']} */ ;
                    /** @type {__VLS_StyleScopedClasses['rounded']} */ ;
                    /** @type {__VLS_StyleScopedClasses['bg-gray-100']} */ ;
                    /** @type {__VLS_StyleScopedClasses['px-1.5']} */ ;
                    /** @type {__VLS_StyleScopedClasses['py-px']} */ ;
                    /** @type {__VLS_StyleScopedClasses['font-mono']} */ ;
                    /** @type {__VLS_StyleScopedClasses['text-[10px]']} */ ;
                    /** @type {__VLS_StyleScopedClasses['text-[var(--c-muted)]']} */ ;
                    (ct);
                    __VLS_asFunctionalElement1(__VLS_intrinsics.div, __VLS_intrinsics.div)({
                        ...{ class: "rounded-lg border border-[var(--c-border)] bg-gray-50 p-2.5" },
                    });
                    /** @type {__VLS_StyleScopedClasses['rounded-lg']} */ ;
                    /** @type {__VLS_StyleScopedClasses['border']} */ ;
                    /** @type {__VLS_StyleScopedClasses['border-[var(--c-border)]']} */ ;
                    /** @type {__VLS_StyleScopedClasses['bg-gray-50']} */ ;
                    /** @type {__VLS_StyleScopedClasses['p-2.5']} */ ;
                    if (media.schema) {
                        const __VLS_10 = SchemaView;
                        // @ts-ignore
                        const __VLS_11 = __VLS_asFunctionalComponent1(__VLS_10, new __VLS_10({
                            schema: (media.schema),
                            schemas: (__VLS_ctx.schemas),
                        }));
                        const __VLS_12 = __VLS_11({
                            schema: (media.schema),
                            schemas: (__VLS_ctx.schemas),
                        }, ...__VLS_functionalComponentArgsRest(__VLS_11));
                    }
                    // @ts-ignore
                    [schemas, responses, responses, statusColor,];
                }
            }
            // @ts-ignore
            [];
        }
    }
}
// @ts-ignore
[];
const __VLS_export = (await import('vue')).defineComponent({
    __typeProps: {},
});
export default {};
