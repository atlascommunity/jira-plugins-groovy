import PropTypes from 'prop-types';


export const ExecutionModel = PropTypes.shape({
    id: PropTypes.number,
    scriptId: PropTypes.string,
    time: PropTypes.number,
    success: PropTypes.bool,
    date: PropTypes.string,
    error: PropTypes.string,
    extraParams: PropTypes.string
});
