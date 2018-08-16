//@flow
import React, { Fragment } from 'react';
import ReactDOM from 'react-dom';

import {BrowserRouter, Route, Switch} from 'react-router-dom';

// eslint-disable-next-line import/no-extraneous-dependencies
import AJS from 'AJS';

import {MainApp} from './MainApp';
import {NavigationController} from './NavigationController';

import {getBaseUrl} from '../service';
import {fixStyle} from '../common/fixStyle';

import {ConsoleRoute} from '../app-console';
import {AdminRoute} from '../app-admin';
import {RegistryRoute} from '../app-registry';
import {ListenersRoute} from '../app-listeners';
import {RestRoute} from '../app-rest';
import {FieldsRoute} from '../app-fields';
import {ScheduledRoute} from '../app-scheduled';
import {JqlRoute} from '../app-jql';
import {AuditLogRoute} from '../app-audit';
import {ExtrasPage} from '../app-extras';

import {NotFoundPage} from '../common/script-list/NotFoundPage';

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
                    <Route path="/jql" component={JqlRoute}/>
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
