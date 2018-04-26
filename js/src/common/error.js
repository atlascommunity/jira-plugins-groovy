//@flow
import type {SyntaxError} from './types';
import type {MarkerType} from './editor/types';


export function getMarkers(errors: Array<SyntaxError>): Array<MarkerType> {
    return errors.map((error: SyntaxError): MarkerType => {
        return {
            startRow: error.startLine - 1,
            endRow: error.endLine - 1,
            startCol: error.startColumn - 1,
            endCol: error.endColumn - 1,
            className: 'error-marker',
            message: error.message
        };
    });
}
