//@flow
import React, {type Element} from 'react';
import {Link} from 'react-router-dom';


type Props = {
    children: Element<any>,
    className: string,
    href: string,
    onMouseEnter: (SyntheticEvent<any>) => void,
    onMouseLeave: (SyntheticEvent<any>) => void,
};

export class RouterLink extends React.PureComponent<Props, {}> {
    render() {
        const {
            children,
            className,
            href,
            onMouseEnter,
            onMouseLeave,
        } = this.props;

        return (
            <Link
                className={className}
                onMouseEnter={onMouseEnter}
                onMouseLeave={onMouseLeave}
                to={href}
            >
                {children}
            </Link>
        );
    }
}
