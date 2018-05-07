//@flow
import * as React from 'react';

import Select from '@atlaskit/select';
import SelectWrapper from '@atlaskit/select/dist/esm/SelectWrapper';
import Avatar from '@atlaskit/avatar';
import {Label} from '@atlaskit/field-base';

import {components} from 'react-select';

import type {SingleValueType} from './types';

import {ajaxGet} from '../../service/ajaxHelper';
import type {OptMutableFieldProps, FieldProps, FormFieldProps} from '../types';


function ValueImpl({data, children}: any): React.Node {
    return (
        <div className="flex-row">
            {data.imgSrc && <Avatar size="xsmall" src={data.imgSrc}/>}
            <span className="flex-vertical-middle" style={{marginLeft: data.imgSrc ? '5px' : ''}}>
                {children}
            </span>
        </div>
    );
}

function OptionImpl({data, children, ...props}: any): React.Node {
    return (
        <components.Option
            {...props}
        >
            <ValueImpl data={data}>{children}</ValueImpl>
        </components.Option>
    );
}

function SingleValueImpl({data, children, ...props}: any): React.Node {
    return (
        <components.SingleValue {...props}>
            {props.in && <ValueImpl data={data}>{children}</ValueImpl>}
        </components.SingleValue>
    );
}

let i: number = 0;

type ValueType = SingleValueType | Array<SingleValueType>;

type AsyncPickerProps = FieldProps & OptMutableFieldProps<ValueType> & FormFieldProps & {
    src: string,
    isMulti?: boolean
};

type DataType = {
    complete: boolean,
    options: Array<ValueType>
};

type AsyncPickerState = {
    filter: string,
    data: DataType,
    fetching: ?number
};

export class AsyncPicker extends React.Component<AsyncPickerProps, AsyncPickerState> {
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

    componentWillReceiveProps(props: AsyncPickerProps) {
        const value = props.value;
        if (this.props.value !== value) {
            if (!Array.isArray(value)) {
                this._getOptions(value ? value.label : '');
            }
        }
    }

    render(): React.Node {
        const {label, isRequired, isLabelHidden, isInvalid, invalidMessage} = this.props;
        const {fetching, data} = this.state;

        //todo: error display
        return (
            <div>
                <Label label={label} isRequired={isRequired} isHidden={isLabelHidden}/>
                <SelectWrapper
                    id={`async-picker-${this.i}`}

                    validationState={isInvalid ? 'error' : 'default'}
                    //$FlowFixMe
                    validationMessage={isInvalid ? invalidMessage : undefined}
                >
                    <Select
                        {...this.props}
                        shouldFitContainer={true}

                        hasAutocomplete={true}
                        onInputChange={this._onFilterChange}

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
