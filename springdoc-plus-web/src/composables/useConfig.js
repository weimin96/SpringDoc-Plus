import { reactive } from 'vue';
const LS_KEY = 'springdoc-plus.ui';
export function getLocalConfig() {
    try {
        return JSON.parse(localStorage.getItem(LS_KEY) || '{}');
    }
    catch {
        return {};
    }
}
export function setLocalConfig(cfg) {
    localStorage.setItem(LS_KEY, JSON.stringify(cfg));
}
export function clearLocalConfig() {
    localStorage.removeItem(LS_KEY);
}
export function mergeConfig(server, local) {
    return { ...server, ...local };
}
export function useConfig(serverCfg) {
    const state = reactive(mergeConfig(serverCfg, getLocalConfig()));
    function apply(local) {
        if (local.authPersist) {
            setLocalConfig(local);
        }
        else {
            const { authValue: _, ...rest } = local;
            setLocalConfig(rest);
        }
        const fresh = mergeConfig(serverCfg, getLocalConfig());
        Object.assign(state, fresh);
    }
    function clear() {
        clearLocalConfig();
        Object.assign(state, mergeConfig(serverCfg, {}));
    }
    return { state, apply, clear };
}
