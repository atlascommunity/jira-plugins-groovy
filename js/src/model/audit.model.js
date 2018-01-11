import PropTypes from 'prop-types';

import {UserModel} from './user.model';


export const AuditLogEntryModel = PropTypes.shape({
    id: PropTypes.number,
    user: UserModel,
    category: PropTypes.oneOf([
        'REGISTRY_SCRIPT',
        'REGISTRY_DIRECTORY',
        'LISTENER'
    ]),
    action: PropTypes.oneOf([
        'CREATED',
        'UPDATED',
        'DELETED'
    ]),
    description: PropTypes.string
});
