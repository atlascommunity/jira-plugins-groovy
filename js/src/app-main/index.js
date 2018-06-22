//@flow
import React from 'react';
import ReactDOM from 'react-dom';

import {BrowserRouter, Route, Switch} from 'react-router-dom';

// eslint-disable-next-line import/no-extraneous-dependencies
import AJS from 'AJS';

import {MainApp} from './MainApp';

import {getBaseUrl} from '../service/ajaxHelper';
import {fixStyle} from '../common/fixStyle';

import {RegistryRoute} from '../app-registry/RegistryRoute';
import {NotFoundPage} from '../common/script-list/NotFoundPage';
import {AdminRoute} from '../app-admin/AdminRoute';


AJS.toInit(() => {
    fixStyle();

    const element = document.getElementById('react-content');

    if (element === null) {
        alert('no element');
        return;
    }

    ReactDOM.render(
        <BrowserRouter basename={`${getBaseUrl()}/plugins/servlet/my-groovy`}>
            <Switch>
                <Route path="/" exact={true} component={MainApp}/>
                <Route path="/admin-scripts" component={AdminRoute}/>
                <Route path="/registry" component={RegistryRoute}/>
                <Route component={NotFoundPage}/>
            </Switch>
        </BrowserRouter>,
        element
    );
});
