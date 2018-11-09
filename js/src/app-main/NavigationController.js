//@flow
import React from 'react';
import {withRouter, type RouterHistory, type Location} from 'react-router-dom';

import AJS from 'AJS';

import {TitleMessages} from '../i18n/common.i18n';


const routeMap = {
    'mailru-groovy-console-link': '/console',
    'mailru-groovy-admin-scripts-link': '/admin-scripts',
    'mailru-groovy-registry-link': '/registry',
    'mailru-groovy-listeners-link': '/listeners',
    'mailru-groovy-rest-link': '/rest',
    'mailru-groovy-fields-link': '/fields',
    'mailru-groovy-scheduled-link': '/scheduled',
    'mailru-groovy-jql-link': '/jql',
    'mailru-groovy-audit-link': '/audit'
};

const titleMap = {
    '/console': TitleMessages.console,
    '/admin-scripts': TitleMessages.adminScripts,
    '/registry': TitleMessages.registry,
    '/listeners': TitleMessages.listeners,
    '/rest': TitleMessages.rest,
    '/fields': TitleMessages.fields,
    '/scheduled': TitleMessages.scheduled,
    '/jql': TitleMessages.jql,
    '/audit': TitleMessages.audit
};

type Props = {
    history: RouterHistory,
    location: Location
};

export class NavigationControllerInternal extends React.PureComponent<Props> {
    componentDidMount() {
        for (const elementId of Object.keys(routeMap)) {
            const element = document.getElementById(elementId);

            if (element) {
                element.addEventListener('click', this._onClick);
            }
        }

        this._updateLinks();
    }

    componentWillUnmount() {
        for (const elementId of Object.keys(routeMap)) {
            const element = document.getElementById(elementId);

            if (element) {
                element.removeEventListener('click', this._onClick);
            }
        }
    }

    componentDidUpdate() {
        this._updateLinks();
    }

    _onClick = (event: MouseEvent) => {
        if (event.metaKey || event.altKey || event.ctrlKey || event.shiftKey || event.button !== 0 || event.defaultPrevented) {
            return;
        }

        //$FlowFixMe
        const {id} = event.currentTarget;

        if (id) {
            const route = routeMap[id];
            if (route) {
                event.preventDefault();

                this.props.history.push(route);
            }
        }
    };

    _updateLinks = () => {
        const {location} = this.props;

        const firstLink = document.getElementById(Object.keys(routeMap)[0]);

        if (!firstLink) {
            return;
        }

        const parent = firstLink.closest('.aui-nav');

        if (!parent) {
            return;
        }

        let updatedTitle: boolean = false;

        for (const el of parent.children) {
            const link = el.children[0];
            if (link) {
                const route = routeMap[link.id];

                const parent = link.parentElement;
                if (parent) {
                    if (route && location.pathname.startsWith(route)) {
                        parent.classList.add('aui-nav-selected');
                        document.title = `${titleMap[route]} - ${AJS.Meta.get('app-title')}`;
                        updatedTitle = true;
                    } else {
                        parent.classList.remove('aui-nav-selected');
                    }
                }
            }
        }

        if (!updatedTitle) {
            document.title = `MyGroovy - ${AJS.Meta.get('app-title')}`;
        }
    };

    render() {
        return null;
    }
}

export const NavigationController = withRouter(NavigationControllerInternal);
