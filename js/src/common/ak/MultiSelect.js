import React from 'react';
import PropTypes from 'prop-types';

import {MultiSelectStateless} from '@atlaskit/multi-select';


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
                content: PropTypes.string.isRequired
            }).isRequired
        ),

        onChange: PropTypes.func.isRequired
    };

    constructor(props) {
        super(props);

        this.state = {
            lookupMap: getLookupMap(props.items),
            filter: '',
            isOpen: false
        };
    }

    componentWillReceiveProps(props) {
        this.setState({
            lookupMap: getLookupMap(props.items)
        });
    }

    _onOpenChange = ({isOpen}) => this.setState({ isOpen });

    _onFilterChange = (filter) => this.setState({ filter: filter });

    _onSelected = (sel) => this.props.onChange([...this.props.value, sel.value]);

    _onRemoved = (rem) => this.props.onChange(this.props.value.filter(e => e !== rem.value));

    render() {
        const {lookupMap} = this.state;

        return <MultiSelectStateless
            label={this.props.label}
            isRequired={this.props.isRequired}
            shouldFitContainer={true}

            filterValue={this.state.filter}
            onFilterChange={this._onFilterChange}

            isOpen={this.state.isOpen}
            onOpenChange={this._onOpenChange}

            isLoading={this.props.isLoading}
            items={this.props.items}

            selectedItems={this.props.value.map(key => lookupMap.get(key)).filter(e => e)}
            onSelected={this._onSelected}
            onRemoved={this._onRemoved}
        />;
    }
}
