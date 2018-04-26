//@flow
import * as React from 'react';

import {Controlled} from 'react-codemirror2';


/*export class CodeMirror extends React.Component {
    cm = null;

    _setEditor = (editor) => {
        const {editorDidMount} = this.props;

        this.cm = editor;

        if (editorDidMount) {
            editorDidMount(editor);
        }
    };

    componentWillReceiveProps(props) {
        if (props.value !== this.cm.getValue()) {
            //this.cm.setValue(props.value);
            console.log(props.value);
            console.log(this.cm.getValue());
        }
    }

    render() {
        const {editorDidMount, value, ...props} = this.props;

        return <CM {...props} editorDidMount={this._setEditor}/>;
    }
}*/

export class CodeMirror extends React.PureComponent<any> {
    render(): React.Node {
        return <Controlled {...this.props}/>;
    }
}
