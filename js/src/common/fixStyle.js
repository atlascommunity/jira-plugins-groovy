export function fixStyle() {

    const contentEl = document.getElementById('react-content');

    if (contentEl) {
        contentEl.parentElement.style.paddingTop = '0';
    }
}
