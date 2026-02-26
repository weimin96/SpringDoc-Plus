import { ref, onMounted, nextTick } from 'vue';
import SettingsModal from './components/SettingsModal.vue';
import { mergeConfig, getLocalConfig, setLocalConfig, clearLocalConfig } from './composables/useConfig';
import { initSwagger } from './composables/useSwagger';
// ── State ─────────────────────────────────────────
const groups = ref([]);
const activeGroup = ref(null);
const serverConfig = ref({});
const mergedConfig = ref({});
const loading = ref(true);
const error = ref(null);
const showSettings = ref(false);
const searchKeyword = ref('');
const sidebarCollapsed = ref(false);
// ── Fetch helpers ─────────────────────────────────
async function loadGroups() {
    const res = await fetch('/springdoc-plus-gateway/openapi/groups');
    if (!res.ok)
        throw new Error(`加载文档组失败: ${res.status}`);
    const data = await res.json();
    return data.groups || [];
}
async function loadServerUiConfig() {
    try {
        const res = await fetch('/springdoc-plus-gateway/ui-config');
        if (!res.ok)
            return {};
        return await res.json();
    }
    catch {
        return {};
    }
}
// ── Swagger render ────────────────────────────────
async function renderGroup(group) {
    activeGroup.value = group;
    await nextTick();
    initSwagger('swagger-root', group.url, mergedConfig.value);
}
// ── Settings modal ────────────────────────────────
function onApply(local) {
    if (local.authPersist) {
        setLocalConfig(local);
    }
    else {
        const { authValue: _, ...rest } = local;
        setLocalConfig(rest);
    }
    mergedConfig.value = mergeConfig(serverConfig.value, getLocalConfig());
    showSettings.value = false;
    if (activeGroup.value)
        renderGroup(activeGroup.value);
}
function onClear() {
    clearLocalConfig();
    mergedConfig.value = mergeConfig(serverConfig.value, {});
    if (activeGroup.value)
        renderGroup(activeGroup.value);
}
// ── Filtered groups ───────────────────────────────
function filteredGroups() {
    const kw = searchKeyword.value.trim().toLowerCase();
    if (!kw)
        return groups.value;
    return groups.value.filter(g => g.name.toLowerCase().includes(kw));
}
// ── Init ─────────────────────────────────────────
onMounted(async () => {
    try {
        const [g, srv] = await Promise.all([loadGroups(), loadServerUiConfig()]);
        groups.value = g;
        serverConfig.value = srv;
        const local = getLocalConfig();
        mergedConfig.value = mergeConfig(srv, local);
        if (!mergedConfig.value.authPersist) {
            mergedConfig.value.authValue = '';
        }
        if (g.length) {
            await renderGroup(g[0]);
        }
    }
    catch (e) {
        error.value = e?.message ?? String(e);
    }
    finally {
        loading.value = false;
    }
});
debugger; /* PartiallyEnd: #3632/scriptSetup.vue */
const __VLS_ctx = {};
let __VLS_components;
let __VLS_directives;
/** @type {__VLS_StyleScopedClasses['sidebar-toggle']} */ ;
/** @type {__VLS_StyleScopedClasses['brand-name']} */ ;
/** @type {__VLS_StyleScopedClasses['action-btn']} */ ;
/** @type {__VLS_StyleScopedClasses['action-btn']} */ ;
/** @type {__VLS_StyleScopedClasses['action-btn']} */ ;
/** @type {__VLS_StyleScopedClasses['primary']} */ ;
/** @type {__VLS_StyleScopedClasses['layout']} */ ;
/** @type {__VLS_StyleScopedClasses['sidebar']} */ ;
/** @type {__VLS_StyleScopedClasses['search-input']} */ ;
/** @type {__VLS_StyleScopedClasses['nav-item']} */ ;
/** @type {__VLS_StyleScopedClasses['nav-item']} */ ;
/** @type {__VLS_StyleScopedClasses['nav-item']} */ ;
/** @type {__VLS_StyleScopedClasses['active']} */ ;
/** @type {__VLS_StyleScopedClasses['nav-dot']} */ ;
/** @type {__VLS_StyleScopedClasses['sidebar-footer-text']} */ ;
/** @type {__VLS_StyleScopedClasses['content-error']} */ ;
/** @type {__VLS_StyleScopedClasses['content-error']} */ ;
// CSS variable injection 
// CSS variable injection end 
__VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
    ...{ class: "app-shell" },
});
__VLS_asFunctionalElement(__VLS_intrinsicElements.header, __VLS_intrinsicElements.header)({
    ...{ class: "topbar" },
});
__VLS_asFunctionalElement(__VLS_intrinsicElements.button, __VLS_intrinsicElements.button)({
    ...{ onClick: (...[$event]) => {
            __VLS_ctx.sidebarCollapsed = !__VLS_ctx.sidebarCollapsed;
        } },
    ...{ class: "sidebar-toggle" },
    title: (__VLS_ctx.sidebarCollapsed ? '展开侧边栏' : '收起侧边栏'),
});
__VLS_asFunctionalElement(__VLS_intrinsicElements.svg, __VLS_intrinsicElements.svg)({
    ...{ class: "w-4 h-4" },
    viewBox: "0 0 24 24",
    fill: "none",
    stroke: "currentColor",
    'stroke-width': "2",
});
__VLS_asFunctionalElement(__VLS_intrinsicElements.path)({
    d: "M3 6h18M3 12h18M3 18h18",
});
__VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
    ...{ class: "brand" },
});
__VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
    ...{ class: "brand-icon" },
});
__VLS_asFunctionalElement(__VLS_intrinsicElements.svg, __VLS_intrinsicElements.svg)({
    viewBox: "0 0 24 24",
    fill: "none",
    stroke: "currentColor",
    'stroke-width': "2",
    ...{ class: "w-4 h-4" },
});
__VLS_asFunctionalElement(__VLS_intrinsicElements.path)({
    d: "M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z",
});
__VLS_asFunctionalElement(__VLS_intrinsicElements.polyline)({
    points: "14 2 14 8 20 8",
});
__VLS_asFunctionalElement(__VLS_intrinsicElements.line)({
    x1: "16",
    y1: "13",
    x2: "8",
    y2: "13",
});
__VLS_asFunctionalElement(__VLS_intrinsicElements.line)({
    x1: "16",
    y1: "17",
    x2: "8",
    y2: "17",
});
__VLS_asFunctionalElement(__VLS_intrinsicElements.polyline)({
    points: "10 9 9 9 8 9",
});
__VLS_asFunctionalElement(__VLS_intrinsicElements.span, __VLS_intrinsicElements.span)({
    ...{ class: "brand-name" },
});
__VLS_asFunctionalElement(__VLS_intrinsicElements.em, __VLS_intrinsicElements.em)({});
__VLS_asFunctionalElement(__VLS_intrinsicElements.div)({
    ...{ class: "topbar-divider" },
});
if (__VLS_ctx.activeGroup) {
    __VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
        ...{ class: "current-group" },
    });
    __VLS_asFunctionalElement(__VLS_intrinsicElements.span)({
        ...{ class: "tag-dot" },
    });
    (__VLS_ctx.activeGroup.name);
}
__VLS_asFunctionalElement(__VLS_intrinsicElements.div)({
    ...{ style: {} },
});
__VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
    ...{ class: "topbar-actions" },
});
if (__VLS_ctx.activeGroup) {
    __VLS_asFunctionalElement(__VLS_intrinsicElements.a, __VLS_intrinsicElements.a)({
        href: (__VLS_ctx.activeGroup.url),
        target: "_blank",
        ...{ class: "action-btn" },
        title: "在新窗口打开 JSON",
    });
    __VLS_asFunctionalElement(__VLS_intrinsicElements.svg, __VLS_intrinsicElements.svg)({
        ...{ class: "w-3.5 h-3.5" },
        viewBox: "0 0 24 24",
        fill: "none",
        stroke: "currentColor",
        'stroke-width': "2",
    });
    __VLS_asFunctionalElement(__VLS_intrinsicElements.path)({
        d: "M18 13v6a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V8a2 2 0 0 1 2-2h6",
    });
    __VLS_asFunctionalElement(__VLS_intrinsicElements.polyline)({
        points: "15 3 21 3 21 9",
    });
    __VLS_asFunctionalElement(__VLS_intrinsicElements.line)({
        x1: "10",
        y1: "14",
        x2: "21",
        y2: "3",
    });
    __VLS_asFunctionalElement(__VLS_intrinsicElements.span, __VLS_intrinsicElements.span)({});
}
__VLS_asFunctionalElement(__VLS_intrinsicElements.button, __VLS_intrinsicElements.button)({
    ...{ onClick: (...[$event]) => {
            __VLS_ctx.showSettings = true;
        } },
    ...{ class: "action-btn primary" },
});
__VLS_asFunctionalElement(__VLS_intrinsicElements.svg, __VLS_intrinsicElements.svg)({
    ...{ class: "w-3.5 h-3.5" },
    viewBox: "0 0 24 24",
    fill: "none",
    stroke: "currentColor",
    'stroke-width': "2",
});
__VLS_asFunctionalElement(__VLS_intrinsicElements.circle)({
    cx: "12",
    cy: "12",
    r: "3",
});
__VLS_asFunctionalElement(__VLS_intrinsicElements.path)({
    d: "M19.07 4.93a10 10 0 0 1 0 14.14M4.93 4.93a10 10 0 0 0 0 14.14",
});
__VLS_asFunctionalElement(__VLS_intrinsicElements.span, __VLS_intrinsicElements.span)({});
__VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
    ...{ class: "layout" },
    ...{ class: ({ 'sidebar-hidden': __VLS_ctx.sidebarCollapsed }) },
});
__VLS_asFunctionalElement(__VLS_intrinsicElements.aside, __VLS_intrinsicElements.aside)({
    ...{ class: "sidebar" },
});
__VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
    ...{ class: "sidebar-search" },
});
__VLS_asFunctionalElement(__VLS_intrinsicElements.svg, __VLS_intrinsicElements.svg)({
    ...{ class: "search-icon" },
    viewBox: "0 0 24 24",
    fill: "none",
    stroke: "currentColor",
    'stroke-width': "2",
});
__VLS_asFunctionalElement(__VLS_intrinsicElements.circle)({
    cx: "11",
    cy: "11",
    r: "8",
});
__VLS_asFunctionalElement(__VLS_intrinsicElements.path)({
    d: "m21 21-4.35-4.35",
});
__VLS_asFunctionalElement(__VLS_intrinsicElements.input)({
    ...{ class: "search-input" },
    placeholder: "搜索文档组...",
});
(__VLS_ctx.searchKeyword);
__VLS_asFunctionalElement(__VLS_intrinsicElements.nav, __VLS_intrinsicElements.nav)({
    ...{ class: "sidebar-nav" },
});
__VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
    ...{ class: "nav-section-title" },
});
if (__VLS_ctx.loading) {
    __VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
        ...{ class: "nav-skeleton" },
    });
    for (const [i] of __VLS_getVForSourceType((4))) {
        __VLS_asFunctionalElement(__VLS_intrinsicElements.div)({
            key: (i),
            ...{ class: "skeleton-item" },
            ...{ style: (`width: ${60 + i * 8}%`) },
        });
    }
}
else if (__VLS_ctx.error) {
    __VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
        ...{ class: "nav-error" },
    });
    __VLS_asFunctionalElement(__VLS_intrinsicElements.svg, __VLS_intrinsicElements.svg)({
        ...{ class: "w-4 h-4" },
        viewBox: "0 0 24 24",
        fill: "none",
        stroke: "currentColor",
        'stroke-width': "2",
    });
    __VLS_asFunctionalElement(__VLS_intrinsicElements.circle)({
        cx: "12",
        cy: "12",
        r: "10",
    });
    __VLS_asFunctionalElement(__VLS_intrinsicElements.path)({
        d: "M12 8v4M12 16h.01",
    });
    (__VLS_ctx.error);
}
else {
    for (const [g] of __VLS_getVForSourceType((__VLS_ctx.filteredGroups()))) {
        __VLS_asFunctionalElement(__VLS_intrinsicElements.button, __VLS_intrinsicElements.button)({
            ...{ onClick: (...[$event]) => {
                    if (!!(__VLS_ctx.loading))
                        return;
                    if (!!(__VLS_ctx.error))
                        return;
                    __VLS_ctx.renderGroup(g);
                } },
            key: (g.url),
            ...{ class: "nav-item" },
            ...{ class: ({ active: __VLS_ctx.activeGroup?.url === g.url }) },
        });
        __VLS_asFunctionalElement(__VLS_intrinsicElements.span)({
            ...{ class: "nav-dot" },
        });
        (g.name);
    }
    if (__VLS_ctx.filteredGroups().length === 0) {
        __VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
            ...{ class: "nav-empty" },
        });
    }
}
__VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
    ...{ class: "sidebar-footer" },
});
__VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
    ...{ class: "sidebar-footer-text" },
});
__VLS_asFunctionalElement(__VLS_intrinsicElements.span, __VLS_intrinsicElements.span)({});
__VLS_asFunctionalElement(__VLS_intrinsicElements.code, __VLS_intrinsicElements.code)({});
__VLS_asFunctionalElement(__VLS_intrinsicElements.main, __VLS_intrinsicElements.main)({
    ...{ class: "content" },
});
if (__VLS_ctx.loading) {
    __VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
        ...{ class: "content-loading" },
    });
    __VLS_asFunctionalElement(__VLS_intrinsicElements.div)({
        ...{ class: "spinner" },
    });
    __VLS_asFunctionalElement(__VLS_intrinsicElements.p, __VLS_intrinsicElements.p)({});
}
else if (__VLS_ctx.error) {
    __VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
        ...{ class: "content-error" },
    });
    __VLS_asFunctionalElement(__VLS_intrinsicElements.svg, __VLS_intrinsicElements.svg)({
        ...{ class: "w-8 h-8 text-red-400" },
        viewBox: "0 0 24 24",
        fill: "none",
        stroke: "currentColor",
        'stroke-width': "1.5",
    });
    __VLS_asFunctionalElement(__VLS_intrinsicElements.circle)({
        cx: "12",
        cy: "12",
        r: "10",
    });
    __VLS_asFunctionalElement(__VLS_intrinsicElements.path)({
        d: "M12 8v4M12 16h.01",
    });
    __VLS_asFunctionalElement(__VLS_intrinsicElements.h2, __VLS_intrinsicElements.h2)({});
    __VLS_asFunctionalElement(__VLS_intrinsicElements.p, __VLS_intrinsicElements.p)({});
    (__VLS_ctx.error);
}
else if (!__VLS_ctx.groups.length) {
    __VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
        ...{ class: "content-error" },
    });
    __VLS_asFunctionalElement(__VLS_intrinsicElements.svg, __VLS_intrinsicElements.svg)({
        ...{ class: "w-8 h-8 text-gray-300" },
        viewBox: "0 0 24 24",
        fill: "none",
        stroke: "currentColor",
        'stroke-width': "1.5",
    });
    __VLS_asFunctionalElement(__VLS_intrinsicElements.path)({
        d: "M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z",
    });
    __VLS_asFunctionalElement(__VLS_intrinsicElements.h2, __VLS_intrinsicElements.h2)({});
    __VLS_asFunctionalElement(__VLS_intrinsicElements.p, __VLS_intrinsicElements.p)({});
}
else {
    __VLS_asFunctionalElement(__VLS_intrinsicElements.div)({
        id: "swagger-root",
        ...{ class: "swagger-wrapper" },
    });
}
/** @type {[typeof SettingsModal, ]} */ ;
// @ts-ignore
const __VLS_0 = __VLS_asFunctionalComponent(SettingsModal, new SettingsModal({
    ...{ 'onClose': {} },
    ...{ 'onApply': {} },
    ...{ 'onClear': {} },
    visible: (__VLS_ctx.showSettings),
    config: (__VLS_ctx.mergedConfig),
    serverConfig: (__VLS_ctx.serverConfig),
}));
const __VLS_1 = __VLS_0({
    ...{ 'onClose': {} },
    ...{ 'onApply': {} },
    ...{ 'onClear': {} },
    visible: (__VLS_ctx.showSettings),
    config: (__VLS_ctx.mergedConfig),
    serverConfig: (__VLS_ctx.serverConfig),
}, ...__VLS_functionalComponentArgsRest(__VLS_0));
let __VLS_3;
let __VLS_4;
let __VLS_5;
const __VLS_6 = {
    onClose: (...[$event]) => {
        __VLS_ctx.showSettings = false;
    }
};
const __VLS_7 = {
    onApply: (__VLS_ctx.onApply)
};
const __VLS_8 = {
    onClear: (__VLS_ctx.onClear)
};
var __VLS_2;
/** @type {__VLS_StyleScopedClasses['app-shell']} */ ;
/** @type {__VLS_StyleScopedClasses['topbar']} */ ;
/** @type {__VLS_StyleScopedClasses['sidebar-toggle']} */ ;
/** @type {__VLS_StyleScopedClasses['w-4']} */ ;
/** @type {__VLS_StyleScopedClasses['h-4']} */ ;
/** @type {__VLS_StyleScopedClasses['brand']} */ ;
/** @type {__VLS_StyleScopedClasses['brand-icon']} */ ;
/** @type {__VLS_StyleScopedClasses['w-4']} */ ;
/** @type {__VLS_StyleScopedClasses['h-4']} */ ;
/** @type {__VLS_StyleScopedClasses['brand-name']} */ ;
/** @type {__VLS_StyleScopedClasses['topbar-divider']} */ ;
/** @type {__VLS_StyleScopedClasses['current-group']} */ ;
/** @type {__VLS_StyleScopedClasses['tag-dot']} */ ;
/** @type {__VLS_StyleScopedClasses['topbar-actions']} */ ;
/** @type {__VLS_StyleScopedClasses['action-btn']} */ ;
/** @type {__VLS_StyleScopedClasses['w-3.5']} */ ;
/** @type {__VLS_StyleScopedClasses['h-3.5']} */ ;
/** @type {__VLS_StyleScopedClasses['action-btn']} */ ;
/** @type {__VLS_StyleScopedClasses['primary']} */ ;
/** @type {__VLS_StyleScopedClasses['w-3.5']} */ ;
/** @type {__VLS_StyleScopedClasses['h-3.5']} */ ;
/** @type {__VLS_StyleScopedClasses['layout']} */ ;
/** @type {__VLS_StyleScopedClasses['sidebar']} */ ;
/** @type {__VLS_StyleScopedClasses['sidebar-search']} */ ;
/** @type {__VLS_StyleScopedClasses['search-icon']} */ ;
/** @type {__VLS_StyleScopedClasses['search-input']} */ ;
/** @type {__VLS_StyleScopedClasses['sidebar-nav']} */ ;
/** @type {__VLS_StyleScopedClasses['nav-section-title']} */ ;
/** @type {__VLS_StyleScopedClasses['nav-skeleton']} */ ;
/** @type {__VLS_StyleScopedClasses['skeleton-item']} */ ;
/** @type {__VLS_StyleScopedClasses['nav-error']} */ ;
/** @type {__VLS_StyleScopedClasses['w-4']} */ ;
/** @type {__VLS_StyleScopedClasses['h-4']} */ ;
/** @type {__VLS_StyleScopedClasses['nav-item']} */ ;
/** @type {__VLS_StyleScopedClasses['nav-dot']} */ ;
/** @type {__VLS_StyleScopedClasses['nav-empty']} */ ;
/** @type {__VLS_StyleScopedClasses['sidebar-footer']} */ ;
/** @type {__VLS_StyleScopedClasses['sidebar-footer-text']} */ ;
/** @type {__VLS_StyleScopedClasses['content']} */ ;
/** @type {__VLS_StyleScopedClasses['content-loading']} */ ;
/** @type {__VLS_StyleScopedClasses['spinner']} */ ;
/** @type {__VLS_StyleScopedClasses['content-error']} */ ;
/** @type {__VLS_StyleScopedClasses['w-8']} */ ;
/** @type {__VLS_StyleScopedClasses['h-8']} */ ;
/** @type {__VLS_StyleScopedClasses['text-red-400']} */ ;
/** @type {__VLS_StyleScopedClasses['content-error']} */ ;
/** @type {__VLS_StyleScopedClasses['w-8']} */ ;
/** @type {__VLS_StyleScopedClasses['h-8']} */ ;
/** @type {__VLS_StyleScopedClasses['text-gray-300']} */ ;
/** @type {__VLS_StyleScopedClasses['swagger-wrapper']} */ ;
var __VLS_dollars;
const __VLS_self = (await import('vue')).defineComponent({
    setup() {
        return {
            SettingsModal: SettingsModal,
            groups: groups,
            activeGroup: activeGroup,
            serverConfig: serverConfig,
            mergedConfig: mergedConfig,
            loading: loading,
            error: error,
            showSettings: showSettings,
            searchKeyword: searchKeyword,
            sidebarCollapsed: sidebarCollapsed,
            renderGroup: renderGroup,
            onApply: onApply,
            onClear: onClear,
            filteredGroups: filteredGroups,
        };
    },
});
export default (await import('vue')).defineComponent({
    setup() {
        return {};
    },
});
; /* PartiallyEnd: #4569/main.vue */
