import PropTypes from 'prop-types';


export const ParamModel = PropTypes.shape({
    type: PropTypes.oneOf([
        'STRING',
        'TEXT',
        'LONG',
        'DOUBLE',
        'GROUP',
        'USER',
        'CUSTOM_FIELD'
    ]),
    name: PropTypes.string,
    displayName: PropTypes.string
});

export const RegistryScriptModel = PropTypes.shape({
    id: PropTypes.number,
    name: PropTypes.string,
    params: ParamModel
});
