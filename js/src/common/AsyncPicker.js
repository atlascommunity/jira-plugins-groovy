import React from 'react';
import PropTypes from 'prop-types';

import {Async} from 'react-select';
import './react-select-common.less';

import Avatar from 'aui-react/lib/AUIAvatar';

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

    render() {
        const {name, value, onChange, ...otherProps} = this.props;

        return <Async
            {...otherProps}
            value={value}
            onChange={onChange}
            loadOptions={this._getOptions}
            optionRenderer={this._renderOption}
            name={name}
        />;
    }
}
