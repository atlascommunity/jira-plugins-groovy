import PropTypes from 'prop-types';


export const UserModel = PropTypes.shape({
    key: PropTypes.string.isRequired,
    displayName: PropTypes.string,
    avatarUrl: PropTypes.string
});
