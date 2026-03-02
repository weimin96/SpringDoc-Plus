import { ref, onMounted, onUnmounted } from 'vue';
import OperationPanel from './OperationPanel.vue';
const props = defineProps();
const open = ref(true);
// 监听全局事件，展开并滚动到指定接口
function handleScrollToOp(event) {
    const detail = event.detail;
    if (!detail)
        return;
    // 检查是否属于当前 tag
    const found = props.group.operations.find((op) => op.method === detail.method && op.path === detail.path);
    if (found) {
        // 等待 DOM 更新后滚动
        setTimeout(() => {
            const el = document.getElementById(`op-${detail.method}-${detail.path}`);
            if (el) {
                el.scrollIntoView({ behavior: 'smooth', block: 'center' });
                // 高亮闪烁效果
                el.classList.add('ring-2', 'ring-blue-400');
                setTimeout(() => el.classList.remove('ring-2', 'ring-blue-400'), 1500);
            }
        }, 100);
    }
}
onMounted(() => {
    window.addEventListener('scroll-to-operation', handleScrollToOp);
});
onUnmounted(() => {
    window.removeEventListener('scroll-to-operation', handleScrollToOp);
});
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
    ...{ class: "mb-4" },
});
/** @type {__VLS_StyleScopedClasses['mb-4']} */ ;
__VLS_asFunctionalElement1(__VLS_intrinsics.button, __VLS_intrinsics.button)({
    ...{ onClick: (...[$event]) => {
            __VLS_ctx.open = !__VLS_ctx.open;
            // @ts-ignore
            [open, open,];
        } },
    ...{ class: "flex w-full cursor-pointer items-center gap-2 rounded-[10px] border-none bg-transparent px-1 py-2.5 text-left transition-colors hover:bg-[var(--c-primary-light)]" },
});
/** @type {__VLS_StyleScopedClasses['flex']} */ ;
/** @type {__VLS_StyleScopedClasses['w-full']} */ ;
/** @type {__VLS_StyleScopedClasses['cursor-pointer']} */ ;
/** @type {__VLS_StyleScopedClasses['items-center']} */ ;
/** @type {__VLS_StyleScopedClasses['gap-2']} */ ;
/** @type {__VLS_StyleScopedClasses['rounded-[10px]']} */ ;
/** @type {__VLS_StyleScopedClasses['border-none']} */ ;
/** @type {__VLS_StyleScopedClasses['bg-transparent']} */ ;
/** @type {__VLS_StyleScopedClasses['px-1']} */ ;
/** @type {__VLS_StyleScopedClasses['py-2.5']} */ ;
/** @type {__VLS_StyleScopedClasses['text-left']} */ ;
/** @type {__VLS_StyleScopedClasses['transition-colors']} */ ;
/** @type {__VLS_StyleScopedClasses['hover:bg-[var(--c-primary-light)]']} */ ;
__VLS_asFunctionalElement1(__VLS_intrinsics.svg, __VLS_intrinsics.svg)({
    ...{ class: "h-4 w-4 flex-shrink-0 text-[var(--c-muted)] transition-transform duration-200" },
    ...{ class: (__VLS_ctx.open ? 'rotate-0' : '-rotate-90') },
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
__VLS_asFunctionalElement1(__VLS_intrinsics.span, __VLS_intrinsics.span)({
    ...{ class: "flex-1 text-[13.5px] font-semibold text-[var(--c-text)]" },
});
/** @type {__VLS_StyleScopedClasses['flex-1']} */ ;
/** @type {__VLS_StyleScopedClasses['text-[13.5px]']} */ ;
/** @type {__VLS_StyleScopedClasses['font-semibold']} */ ;
/** @type {__VLS_StyleScopedClasses['text-[var(--c-text)]']} */ ;
(__VLS_ctx.group.name);
__VLS_asFunctionalElement1(__VLS_intrinsics.span, __VLS_intrinsics.span)({
    ...{ class: "rounded-full bg-gray-100 px-2 py-px text-[11px] text-[var(--c-muted)]" },
});
/** @type {__VLS_StyleScopedClasses['rounded-full']} */ ;
/** @type {__VLS_StyleScopedClasses['bg-gray-100']} */ ;
/** @type {__VLS_StyleScopedClasses['px-2']} */ ;
/** @type {__VLS_StyleScopedClasses['py-px']} */ ;
/** @type {__VLS_StyleScopedClasses['text-[11px]']} */ ;
/** @type {__VLS_StyleScopedClasses['text-[var(--c-muted)]']} */ ;
(__VLS_ctx.group.operations.length);
if (__VLS_ctx.group.description) {
    __VLS_asFunctionalElement1(__VLS_intrinsics.p, __VLS_intrinsics.p)({
        ...{ class: "mb-2 ml-6 text-[12px] text-[var(--c-muted)]" },
    });
    /** @type {__VLS_StyleScopedClasses['mb-2']} */ ;
    /** @type {__VLS_StyleScopedClasses['ml-6']} */ ;
    /** @type {__VLS_StyleScopedClasses['text-[12px]']} */ ;
    /** @type {__VLS_StyleScopedClasses['text-[var(--c-muted)]']} */ ;
    (__VLS_ctx.group.description);
}
if (__VLS_ctx.open) {
    __VLS_asFunctionalElement1(__VLS_intrinsics.div, __VLS_intrinsics.div)({
        ...{ class: "mt-1 ml-1" },
        ...{ style: {} },
    });
    /** @type {__VLS_StyleScopedClasses['mt-1']} */ ;
    /** @type {__VLS_StyleScopedClasses['ml-1']} */ ;
    for (const [item] of __VLS_vFor((__VLS_ctx.group.operations))) {
        const __VLS_0 = OperationPanel;
        // @ts-ignore
        const __VLS_1 = __VLS_asFunctionalComponent1(__VLS_0, new __VLS_0({
            key: (`${item.method}-${item.path}`),
            item: (item),
            schemas: (__VLS_ctx.schemas),
        }));
        const __VLS_2 = __VLS_1({
            key: (`${item.method}-${item.path}`),
            item: (item),
            schemas: (__VLS_ctx.schemas),
        }, ...__VLS_functionalComponentArgsRest(__VLS_1));
        // @ts-ignore
        [open, open, group, group, group, group, group, schemas,];
    }
}
// @ts-ignore
[];
const __VLS_export = (await import('vue')).defineComponent({
    __typeProps: {},
});
export default {};
