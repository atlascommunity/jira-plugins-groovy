import React from 'react';
import PropTypes from 'prop-types';

import {Async} from 'react-select';
import './react-select-common.less';

import Avatar from 'aui-react/lib/AUIAvatar';
import Icon from 'aui-react/lib/AUIIcon';

import {ajaxGet} from '../service/ajaxHelper';


export class AsyncPicker extends React.Component {
    static propTypes = {
        src: PropTypes.string.isRequired,
        name: PropTypes.string,
        onChange: PropTypes.func,
        value: PropTypes.shape({
            value: PropTypes.any.isRequired,
            label: PropTypes.string.isRequired,
            imgSrc: PropTypes.string
        })
    };

    _getOptions = (input, callback) => {
        ajaxGet(this.props.src + (input ? `?q=${input}` : '')).then(data => callback(null, data));
    };

    _renderOption = (option) => {
        return (
            <div>
                {option.imgSrc && <Avatar size="xsmall" src={option.imgSrc}/>}
                {option.imgSrc && ' '}
                {option.label}
            </div>
        );
    };

    _renderClearer = () => {
        return <Icon icon="remove-label"/>;
    };

    render() {
        const {name, value, onChange, ...otherProps} = this.props;

        return <Async
            {...otherProps}

            clearRenderer={this._renderClearer}
            loadOptions={this._getOptions}
            optionRenderer={this._renderOption}

            value={value}
            onChange={onChange}
            name={name}
        />;
    }
}
