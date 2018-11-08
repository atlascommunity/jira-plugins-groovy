//@flow
import React, {Fragment} from 'react';

import memoizeOne from 'memoize-one';
import memoize from 'lodash/memoize';
import orderBy from 'lodash/orderBy';

import Spinner from '@atlaskit/spinner';

import {Binding} from './Binding';
import type {ClassDoc} from './types';

import {extractShortClassName} from '../classNames';
import {bindingService} from '../../service';


const getBindings = memoizeOne(bindingService.getGlobalBindingTypes);

type ItemType = ClassDoc & { key: string, classDoc: ClassDoc };

type Props = {
    onOpenDoc: (ClassDoc) => void
};

type State = {
    isReady: boolean,
    bindings: ?$ReadOnlyArray<ItemType>
};

export class GlobalBindings extends React.PureComponent<Props, State> {
    state = {
        isReady: false,
        bindings: null
    };

    _openDoc = memoize(doc => () => this.props.onOpenDoc(doc));

    componentDidMount() {
        getBindings().then((bindings: {string: ClassDoc}) => {
            this.setState({
                isReady: true,
                bindings: orderBy(
                    Object
                        .keys(bindings)
                        .map(key => ({
                            ...bindings[key],
                            ...(
                                bindings[key].builtIn
                                ? {}
                                : {classDoc: bindings[key]}
                            ),
                            key
                        })),
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
                        ({key, className, href, classDoc}) =>
                            <Binding
                                key={key}
                                onOpenDoc={classDoc ? this._openDoc(classDoc) : undefined}
                                binding={{
                                    name: key,
                                    className: extractShortClassName(className),
                                    fullClassName: className,
                                    javaDoc: href || undefined,
                                    classDoc
                                }}
                            />
                    )
                }
            </Fragment>
        );
    }
}

