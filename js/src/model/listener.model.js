import PropTypes from 'prop-types';


const ConditionShape = {
    key: PropTypes.number.isRequired, //must have key
    type: PropTypes.string,
    className: PropTypes.string,
    entityIds: PropTypes.arrayOf(PropTypes.number)
};
ConditionShape.children = PropTypes.arrayOf(PropTypes.shape(ConditionShape));

export const ConditionModel = PropTypes.shape(ConditionShape);

export const ListenerModel = PropTypes.shape({
    id: PropTypes.number.isRequired,
    uuid: PropTypes.string.isRequired,
    script: PropTypes.string.isRequired,
    condition: ConditionModel.isRequired
});


export const conditions = {
    'AND': {
        id: 'AND',
        name: 'And',
        requiresChildren: true
    },
    'OR': {
        id: 'OR',
        name: 'Or',
        requiresChildren: true
    },
    'CLASS_NAME': {
        id: 'CLASS_NAME',
        name: 'Class name',
        requiresChildren: false
    },
    'ISSUE_PROJECT': {
        id: 'ISSUE_PROJECT',
        name: 'Issue project',
        requiresChildren: false
    },
    'ISSUE_EVENT_TYPE': {
        id: 'ISSUE_EVENT_TYPE',
        name: 'IssueEvent type',
        requiresChildren: false
    }
};

export const conditionList = Object.values(conditions);

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
