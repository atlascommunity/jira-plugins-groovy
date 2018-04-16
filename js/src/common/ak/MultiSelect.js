import React from 'react';
import PropTypes from 'prop-types';

import Select from '@atlaskit/select';
import SelectWrapper from '@atlaskit/select/dist/esm/SelectWrapper';
import {Label} from '@atlaskit/field-base';


function getLookupMap(items) {
    const lookupMap = new Map();
    for (const item of items) {
        lookupMap.set(item.value, item);
    }

    return lookupMap;
}

const ValueType = PropTypes.oneOfType([
    PropTypes.number.isRequired,
    PropTypes.string.isRequired
]);

let i = 0;

export class MultiSelect extends React.Component {
    static propTypes = {
        label: PropTypes.string.isRequired,

        isRequired: PropTypes.bool,
        isLoading: PropTypes.bool,

        isInvalid: PropTypes.bool,
        invalidMessage: PropTypes.string,

        value: PropTypes.arrayOf(ValueType.isRequired).isRequired,
        items: PropTypes.arrayOf(
            PropTypes.shape({
                value: ValueType.isRequired,
                label: PropTypes.string.isRequired
            }).isRequired
        ),

        onChange: PropTypes.func.isRequired
    };

    i = i++;

    _onChange = (val) => {
        let newVal = [];

        if (val) {
            newVal = val.map(item => item.value);
        }

        this.props.onChange(newVal);
    };

    constructor(props) {
        super(props);

        this.state = {
            lookupMap: getLookupMap(props.items),
            isOpen: false
        };
    }

    componentWillReceiveProps(props) {
        this.setState({
            lookupMap: getLookupMap(props.items)
        });
    }

    render() {
        const {isInvalid, invalidMessage} = this.props;
        const {lookupMap} = this.state;

        return (
            <div>
                <Label
                    label={this.props.label}
                    isRequired={this.props.isRequired}
                />
                <SelectWrapper
                    id={`multi-select-${this.i}`}

                    validationState={isInvalid && 'error'}
                    validationMessage={isInvalid && invalidMessage}
                >
                    <Select
                        shouldFitContainer={true}
                        isMulti={true}

                        isLoading={this.props.isLoading}
                        options={this.props.items}

                        value={this.props.value.map(key => lookupMap.get(key)).filter(e => e)}
                        onChange={this._onChange}
                    />
                </SelectWrapper>
            </div>
        );
    }
}
