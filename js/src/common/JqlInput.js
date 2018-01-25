import React from 'react';
import PropTypes from 'prop-types';

// eslint-disable-next-line import/no-extraneous-dependencies
import JqlAutoComplete from 'jira/autocomplete/jql-autocomplete';

import {jiraService} from '../service/services';


export class JqlInput extends React.Component {
    static propTypes = {
        id: PropTypes.string.isRequired,
        value: PropTypes.string.isRequired,
        onChange: PropTypes.func.isRequired
    };

    el = null;

    _setEl = (el) => {
        this.el = el;
    };

    _initAutoComplete = (autoCompleteData) => {
        const {value, id} = this.props;
        //todo var hasFocus = field.length > 0 && field[0] === document.activeElement;

        this.jqlAutoComplete = JqlAutoComplete({
            fieldID: id,
            errorID: `${id}-error`,
            parser: JqlAutoComplete.MyParser(autoCompleteData.jqlReservedWords || []),
            queryDelay: 0.65,
            jqlFieldNames: autoCompleteData.visibleFieldNames || [],
            jqlFunctionNames: autoCompleteData.visibleFunctionNames || [],
            minQueryLength: 0,
            allowArrowCarousel: true,
            autoSelectFirst: false,
            maxHeight: '195'
        });

        //var jqlField = $('#' + fieldId);
        /*jqlField.unbind("keypress", Forms.submitOnEnter).keypress(function(e) {
            if (jqlAutoComplete.dropdownController === null || !jqlAutoComplete.dropdownController.displayed || jqlAutoComplete.selectedIndex < 0)
                return true;
        });*/

        this.jqlAutoComplete.buildResponseContainer();
        if (value) {
            this.jqlAutoComplete.parse(value);
        }
        this.jqlAutoComplete.updateColumnLineCount();

        //$('.atlassian-autocomplete .suggestions').css('top', '68px');

        /*
        jqlField.click(function(){
            this.jqlAutoComplete.dropdownController.hideDropdown();
        });*/

        /*todo if (hasFocus) {
            field.select();
        }*/

        // keep reference around
        //field.data('JqlAutoComplete', jqlAutoComplete);
    };

    componentDidMount() {
        jiraService
            .getAutoCompleteData()
            .then(this._initAutoComplete);
    }

    render() {
        const {id, value, onChange} = this.props;

        return (
            <div>
                <textarea
                    id={id}
                    className="textarea full-width-field"

                    ref={this._setEl}

                    value={value}
                    onChange={onChange}
                />
                <div className="error" id={`${id}-error`}/>
            </div>
        );
    }
}
