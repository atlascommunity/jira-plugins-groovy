//@flow
import React, {Fragment} from 'react';

import memoizeOne from 'memoize-one';
import orderBy from 'lodash/orderBy';

import Spinner from '@atlaskit/spinner';

import {Binding} from './Binding';
import type {ClassDoc} from './types';

import {extractShortClassName} from '../classNames';
import {bindingService} from '../../service';


const getBindings = memoizeOne(bindingService.getGlobalBindingTypes);

type ItemType = ClassDoc & { key: string };

type State = {
    isReady: boolean,
    bindings: ?$ReadOnlyArray<ItemType>
};

export class GlobalBindings extends React.Component<{}, State> {
    state = {
        isReady: false,
        bindings: null
    };

    componentDidMount() {
        getBindings().then((bindings: {string: ClassDoc}) => {
            this.setState({
                isReady: true,
                bindings: orderBy(
                    Object.keys(bindings).map(key => ({ ...bindings[key], key })),
                    ['builtIn', 'key'], ['desc', 'asc']
                )
            });
        });
    }

    componentDidUpdate(_prevProps: *, prevState: State) {
        //force reflow after loading data
        if (!prevState.isReady && this.state.isReady) {
            window.dispatchEvent(new Event('resize'));
        }
    }

    render() {
        const {isReady, bindings} = this.state;

        if (!isReady || !bindings) {
            return <Spinner/>;
        }

        return (
            <Fragment>
                {
                    bindings.map(
                        binding =>
                            <Binding
                                key={binding.key}
                                binding={{
                                    name: binding.key,
                                    className: extractShortClassName(binding.className),
                                    fullClassName: binding.className,
                                    javaDoc: binding.href || undefined
                                }}
                            />
                    )
                }
            </Fragment>
        );
    }
}

