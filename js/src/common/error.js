export function getMarkers(errors) {
    return errors.map(error => {
        return {
            startRow: error.startLine - 1,
            endRow: error.endLine - 1,
            startCol: error.startColumn - 1,
            endCol: error.endColumn - 1,
            className: 'error-marker',
            type: 'background'
        };
    });
}
