export class PreferenceService {
    get(key) {
        return localStorage.getItem(key);
    }

    put(key, value) {
        try {
            localStorage.setItem(key, value);
        } catch (e) {
            console.error(`unable to setItem ${key} in localStorage`, e);
        }
    }

    remove(key) {
        localStorage.removeItem(key);
    }
}
