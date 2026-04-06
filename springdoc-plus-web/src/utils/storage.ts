/**
 * storage.ts — 带版本号和 TTL 的 localStorage 工具
 *
 * 解决问题：原 useSimulateRequest.ts 和 SimulatePanel.vue 直接读写
 * localStorage，没有版本控制也没有过期机制，随着接口变更会积累大量脏数据。
 *
 * 策略：
 * - STORAGE_VERSION 升级时，旧前缀的缓存自动失效
 * - 每条缓存写入时记录 timestamp，超过 TTL_MS 视为过期
 * - 应用启动时执行一次 GC，清理所有过期和旧版本条目
 */

/** 当前存储版本。接口协议发生不兼容变更时递增此值。 */
const STORAGE_VERSION = 'v2'

/** 缓存条目有效期：7 天 */
const TTL_MS = 7 * 24 * 60 * 60 * 1000

interface StorageEnvelope<T> {
  ver: string
  ts: number
  data: T
}

/**
 * 读取一条缓存。
 * 返回 null 表示：不存在 / 版本不匹配 / 已过期 / 反序列化失败。
 */
export function readStorage<T>(key: string): T | null {
  try {
    const raw = window.localStorage.getItem(key)
    if (!raw) return null

    const envelope = JSON.parse(raw) as StorageEnvelope<T>
    if (envelope.ver !== STORAGE_VERSION) return null
    if (Date.now() - envelope.ts > TTL_MS) {
      window.localStorage.removeItem(key)
      return null
    }

    return envelope.data
  } catch {
    return null
  }
}

/** 写入一条缓存，自动附加版本和时间戳。 */
export function writeStorage<T>(key: string, data: T): void {
  try {
    const envelope: StorageEnvelope<T> = {
      ver: STORAGE_VERSION,
      ts: Date.now(),
      data,
    }
    window.localStorage.setItem(key, JSON.stringify(envelope))
  } catch {
    // 静默处理：隐私模式或配额超限
  }
}

/** 删除一条缓存。 */
export function removeStorage(key: string): void {
  try {
    window.localStorage.removeItem(key)
  } catch {
    // 静默处理
  }
}

/**
 * 垃圾回收：清理所有带已知前缀但版本不匹配或已过期的条目。
 * 建议在应用启动时调用一次。
 */
export function gcStorage(prefixes: string[]): void {
  try {
    const keysToDelete: string[] = []
    for (let i = 0; i < window.localStorage.length; i++) {
      const key = window.localStorage.key(i)
      if (!key) continue

      const belongsToUs = prefixes.some(p => key.startsWith(p))
      if (!belongsToUs) continue

      const raw = window.localStorage.getItem(key)
      if (!raw) continue

      try {
        const envelope = JSON.parse(raw) as StorageEnvelope<unknown>
        const isStale = envelope.ver !== STORAGE_VERSION || Date.now() - envelope.ts > TTL_MS
        if (isStale) keysToDelete.push(key)
      } catch {
        // 非 JSON 格式，属于旧版本数据，直接删除
        keysToDelete.push(key)
      }
    }

    for (const key of keysToDelete) {
      window.localStorage.removeItem(key)
    }
  } catch {
    // 静默处理
  }
}
