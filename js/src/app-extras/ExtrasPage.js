import React from 'react';

import Button from 'aui-react/lib/AUIButton';

import {extrasService} from '../service/services';


export class ExtrasPage extends React.Component {
    _clearCache = () => {
        extrasService.clearCache().then(() => alert('done'));
    };

    render() {
        return <Button type="primary" onClick={this._clearCache}>{'Kill it with fire'}</Button>;
    }
}
