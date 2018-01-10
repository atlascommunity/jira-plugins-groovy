import React from 'react';
import PropTypes from 'prop-types';

import {getPluginBaseUrl} from '../service/ajaxHelper';


export class UserPicker extends React.Component {
    static propTypes = {
        onChange: PropTypes.func,
        value: PropTypes.shape({
            value: PropTypes.string.isRequired,
            label: PropTypes.string.isRequired,
            icon: PropTypes.string.isRequired
        })
    };

    render() {
        return <aui-select src={`${getPluginBaseUrl()}/jira-api/userPicker`} {...this.props}/>;
    }
}
