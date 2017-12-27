
export function anyValidator(validators) {
    return (props, propName, componentName) => {
        for (const validator of validators) {
            const validationResult = validator(props, propName, componentName);

            if (validationResult) {
                return validationResult;
            }
        }
    };
}
