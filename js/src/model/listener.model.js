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
