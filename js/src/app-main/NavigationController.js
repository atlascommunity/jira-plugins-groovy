//@flow
import React from 'react';
import withRouter from 'react-router-dom/es/withRouter';


const routeMap = {
    'mailru-groovy-console-link': '/console',
    'mailru-groovy-admin-scripts-link': '/admin-scripts',
    'mailru-groovy-registry-link': '/registry',
    'mailru-groovy-listeners-link': '/listeners',
    'mailru-groovy-rest-link': '/rest',
    'mailru-groovy-fields-link': '/fields',
    'mailru-groovy-scheduled-link': '/scheduled',
    'mailru-groovy-audit-link': '/audit',
    'mailru-groovy-extras-link': '/extras'
};

type Props = {
    history: any,
    location: any
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

    _onClick = (event: Event) => {
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

        for (const el of parent.children) {
            const link = el.children[0];
            if (link) {
                const route = routeMap[link.id];

                const parent = link.parentElement;
                if (parent) {
                    if (route && location.pathname.startsWith(route)) {
                        parent.classList.add('aui-nav-selected');
                    } else {
                        parent.classList.remove('aui-nav-selected');
                    }
                }
            }
        }
    };

    render() {
        return null;
    }
}

export const NavigationController = withRouter(NavigationControllerInternal);
