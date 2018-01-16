import PropTypes from 'prop-types';


export const ConditionModel = PropTypes.shape({
    type: PropTypes.string,
    className: PropTypes.string,
    projectIds: PropTypes.arrayOf(PropTypes.number),
    typeIds: PropTypes.arrayOf(PropTypes.number)
});

export const ListenerModel = PropTypes.shape({
    id: PropTypes.number.isRequired,
    uuid: PropTypes.string.isRequired,
    scriptBody: PropTypes.string.isRequired,
    condition: ConditionModel.isRequired
});

function fillConditionKeys(condition) {
    if (condition.children) {
        let i = 0;

        condition.children = condition
            .children
            .map(child => {
                fillConditionKeys(child);

                return {
                    ...child,
                    key: i++
                };
            });
    }
    return condition;
}

export function fillListenerKeys(listener) {
    return {
        ...listener,
        condition: {
            ...fillConditionKeys(listener.condition),
            key: 0
        }
    };
}
