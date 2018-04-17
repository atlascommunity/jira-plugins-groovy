import React from 'react';
import PropTypes from 'prop-types';

import Select from '@atlaskit/select';
import SelectWrapper from '@atlaskit/select/dist/esm/SelectWrapper';
import Avatar from '@atlaskit/avatar';
import {Label} from '@atlaskit/field-base';

import {components} from 'react-select';

import {ajaxGet} from '../../service/ajaxHelper';


function ValueImpl({data, children}) {
    return (
        <div className="flex-row">
            {data.imgSrc && <Avatar size="xsmall" src={data.imgSrc}/>}
            <span className="flex-vertical-middle" style={{marginLeft: data.imgSrc ? '5px' : ''}}>
                {children}
            </span>
        </div>
    );
}

function OptionImpl({data, children, ...props}) {
    return (
        <components.Option
            {...props}
        >
            <ValueImpl data={data}>{children}</ValueImpl>
        </components.Option>
    );
}

function SingleValueImpl({data, children, ...props}) {
    return (
        <components.SingleValue {...props}>
            {props.in && <ValueImpl data={data}>{children}</ValueImpl>}
        </components.SingleValue>
    );
}

let i = 0;

const valueShape = PropTypes.shape({
    value: PropTypes.any.isRequired,
    label: PropTypes.string.isRequired,
    imgSrc: PropTypes.string
});

export class AsyncPicker extends React.Component {
    static propTypes = {
        src: PropTypes.string.isRequired,
        label: PropTypes.string.isRequired,
        name: PropTypes.string,
        onChange: PropTypes.func,
        value: PropTypes.oneOfType([
            valueShape,
            PropTypes.arrayOf(valueShape.isRequired)
        ]),
        isMulti: PropTypes.bool,
        isLabelHidden: PropTypes.bool,
        isRequired: PropTypes.bool,
        isInvalid: PropTypes.bool,
        invalidMessage: PropTypes.string
    };

    i = i++;

    state = {
        filter: '',
        data: {
            complete: false,
            options: []
        },
        fetching: null
    };

    reqId = 0;

    _getOptions = (filter) => {
        const reqId = ++this.reqId;

        let needsFetching = !this.state.data.complete;
        this.setState({ fetching: needsFetching && this.reqId, filter });

        if (needsFetching) {
            return ajaxGet(this.props.src + (filter ? `?q=${filter}` : ''))
                .then(data => {
                    this.setState(state => {
                        if (reqId === state.fetching) {
                            return {
                                data,
                                fetching: null
                            };
                        }
                    });
                }
            );
        }
    };

    _onFilterChange = (filter) => {
        this._getOptions(filter);
    };

    componentDidMount() {
        const {value} = this.props;

        this._getOptions(value ? value.label : '');
    }

    componentWillReceiveProps(props) {
        if (this.props.value !== props.value) {
            this._getOptions(props.value ? props.value.label : '');
        }
    }

    render() {
        const {label, isRequired, isLabelHidden, isInvalid, invalidMessage} = this.props;
        const {fetching, data} = this.state;

        //todo: error display
        return (
            <div>
                <Label label={label} isRequired={isRequired} isHidden={isLabelHidden}/>
                <SelectWrapper
                    id={`async-picker-${this.i}`}

                    validationState={isInvalid && 'error'}
                    validationMessage={isInvalid && invalidMessage}
                >
                    <Select
                        {...this.props}
                        shouldFitContainer={true}

                        hasAutocomplete={true}
                        onInputChange={this._onFilterChange}
                        optionRenderer={this._renderOption}

                        isLoading={!!fetching}
                        options={data.options}

                        components={{
                            Option: OptionImpl,
                            SingleValue: SingleValueImpl
                        }}
                    />
                </SelectWrapper>
            </div>
        );
    }
}
