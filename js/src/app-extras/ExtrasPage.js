import React from 'react';

import Button from 'aui-react/lib/AUIButton';

import {extrasService} from '../service/services';
import {TitleMessages} from '../i18n/common.i18n';


export class ExtrasPage extends React.Component {
    _clearCache = () => {
        extrasService.clearCache().then(() => alert('done'));
    };

    render() {
        return <div>
            <header className="aui-page-header">
                <div className="aui-page-header-inner">
                    <div className="aui-page-header-main">
                        <h2>{TitleMessages.extras}</h2>
                    </div>
                </div>
            </header>
            <div className="page-content">
                Clear cache:
                <Button type="primary" onClick={this._clearCache}>{'Kill it with fire'}</Button>
            </div>
        </div>;
    }
}
