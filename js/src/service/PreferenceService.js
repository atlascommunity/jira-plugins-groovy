export class PreferenceService {
    get(key) {
        return localStorage.getItem(key);
    }

    put(key, value) {
        localStorage.setItem(key, value);
    }

    remove(key) {
        localStorage.removeItem(key);
    }
}
