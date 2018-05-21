//@flow
import React, {type Node} from 'react';
import {Link} from 'react-router-dom';


export function LinkComponent(props: any): Node {
    return <Link {...props}/>;
}
