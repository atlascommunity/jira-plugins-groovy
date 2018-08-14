//@flow
import React, {type Node} from 'react';

import Avatar from '@atlaskit/avatar';

import memoize from 'lodash/memoize';

import {LargeSelect} from './LargeSelect';
import {FormField} from './FormField';
import type {SingleValueType} from './types';

import {ajaxGet} from '../../service';
import type {OptMutableFieldProps, FieldProps, FormFieldProps, SelectProps, AkFormFieldProps} from '../types';


const formatValueFactory = memoize(
    (displayValue: boolean): ((SingleValueType) => Node) => {
        return function formatValue(data: SingleValueType): Node {
            return (
                <div className="flex-row">
                    {data.imgSrc && <Avatar size="xsmall" src={data.imgSrc}/>}
                        <span className="flex-vertical-middle" style={{marginLeft: data.imgSrc ? '5px' : ''}}>
                        {data.label}{displayValue ? ` (${data.value})` : null}
                    </span>
                </div>
            );
        };
    }
);

let i: number = 0;

type ValueType = SingleValueType | $ReadOnlyArray<SingleValueType>;

type Props<T: ValueType> = FieldProps & FormFieldProps & OptMutableFieldProps<T> & AkFormFieldProps & SelectProps & {
    src: string,
    displayValue: boolean
};

type DataType = {
    complete: boolean,
    options: $ReadOnlyArray<ValueType>
};

type AsyncPickerState = {
    filter: string,
    data: DataType,
    fetching: ?number
};

export class AsyncPicker<T: ValueType> extends React.PureComponent<Props<T>, AsyncPickerState> {
    static defaultProps = {
        isValidationHidden: false,
        displayValue: false
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

    _getOptions = (filter: string) => {
        const reqId = ++this.reqId;

        let needsFetching: boolean = !this.state.data.complete;
        this.setState({ fetching: needsFetching ? this.reqId : null, filter });

        if (needsFetching) {
            ajaxGet(this.props.src + (filter ? `?q=${filter}` : ''))
                .then((data: DataType) => {
                    this.setState((state: AsyncPickerState): any => {
                        if (reqId === state.fetching) {
                            return {
                                data,
                                fetching: null
                            };
                        } else {
                            return {};
                        }
                    });
                }
            );
        }
    };

    _onFilterChange = (filter: string) => {
        this._getOptions(filter);
    };

    componentDidMount() {
        const {value} = this.props;

        if (!Array.isArray(value)) {
            this._getOptions(value ? value.label : '');
        }
    }

    componentDidUpdate(prevProps: Props<T>) {
        const {value} = this.props;

        if (prevProps.value !== value) {
            if (!Array.isArray(value)) {
                this._getOptions(value ? value.label : '');
            }
        }
    }

    render() {
        const {label, isRequired, isLabelHidden, isInvalid, invalidMessage, isValidationHidden, displayValue} = this.props;
        const {fetching, data} = this.state;

        if (isValidationHidden) {
            return (
                <LargeSelect
                    {...this.props}
                    shouldFitContainer={true}

                    hasAutocomplete={true}
                    onInputChange={this._onFilterChange}

                    isLoading={!!fetching}
                    options={data.options}

                    validationState={isInvalid ? 'error' : 'default'}

                    formatOptionLabel={formatValueFactory(displayValue)}
                />
            );
        }

        return (
            <FormField
                label={label || ''}
                isLabelHidden={isLabelHidden}
                isRequired={isRequired}

                isInvalid={isInvalid}
                invalidMessage={invalidMessage}
            >
                <LargeSelect
                    {...this.props}
                    shouldFitContainer={true}

                    hasAutocomplete={true}
                    onInputChange={this._onFilterChange}

                    isLoading={!!fetching}
                    options={data.options}

                    formatOptionLabel={formatValueFactory(displayValue)}
                />
            </FormField>
        );
    }
}
