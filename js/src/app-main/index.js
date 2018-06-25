//@flow
import React, { Fragment } from 'react';
import ReactDOM from 'react-dom';

import {BrowserRouter, Route, Switch} from 'react-router-dom';

// eslint-disable-next-line import/no-extraneous-dependencies
import AJS from 'AJS';

import {MainApp} from './MainApp';
import {NavigationController} from './NavigationController';

import {getBaseUrl} from '../service/ajaxHelper';
import {fixStyle} from '../common/fixStyle';

import {ConsoleRoute} from '../app-console/ConsoleRoute';
import {AdminRoute} from '../app-admin/AdminRoute';
import {RegistryRoute} from '../app-registry/RegistryRoute';
import {ListenersRoute} from '../app-listeners/ListenersRoute';
import {RestRoute} from '../app-rest/RestRoute';
import {FieldsRoute} from '../app-fields/FieldsRoute';
import {ScheduledRoute} from '../app-scheduled/ScheduledRoute';
import {AuditLogRoute} from '../app-audit/AuditLogRoute';

import {NotFoundPage} from '../common/script-list/NotFoundPage';
import {ExtrasPage} from '../app-extras/ExtrasPage';

import '../flex.less';


AJS.toInit(() => {
    fixStyle();

    const element = document.getElementById('react-content');

    if (element === null) {
        alert('no element');
        return;
    }

    ReactDOM.render(
        <BrowserRouter basename={`${getBaseUrl()}/plugins/servlet/my-groovy`}>
            <Fragment>
                <Switch>
                    <Route path="/" exact={true} component={MainApp}/>
                    <Route path="/console" component={ConsoleRoute}/>
                    <Route path="/admin-scripts" component={AdminRoute}/>
                    <Route path="/registry" component={RegistryRoute}/>
                    <Route path="/listeners" component={ListenersRoute}/>
                    <Route path="/rest" component={RestRoute}/>
                    <Route path="/fields" component={FieldsRoute}/>
                    <Route path="/scheduled" component={ScheduledRoute}/>
                    <Route path="/audit" component={AuditLogRoute}/>
                    <Route path="/extras" component={ExtrasPage}/>
                    <Route component={NotFoundPage}/>
                </Switch>
                <NavigationController/>
            </Fragment>
        </BrowserRouter>,
        element
    );
});
