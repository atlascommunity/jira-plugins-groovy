import PropTypes from 'prop-types';

import {anyValidator} from '../common/prop-utils';

/*
function requiredForType(type) {
    return function(props, propName) {
        if (props['type'] === type && !props[propName]) {
            return new Error(`className is required for type ${type}`);
        }
    };
}
*/

const ConditionShape = PropTypes.shape({
    key: PropTypes.number.isRequired, //must have key
    type: PropTypes.string,
    className: PropTypes.string,
    entityIds: PropTypes.arrayOf(PropTypes.number)
});
ConditionShape.children = PropTypes.arrayOf(PropTypes.shape(ConditionShape));

export const ConditionModel = PropTypes.shape(ConditionShape);

export const ListenerModel = PropTypes.shape({
    id: PropTypes.number.isRequired,
    uuid: PropTypes.string.isRequired,
    script: PropTypes.string.isRequired,
    condition: ConditionModel.isRequired
});
