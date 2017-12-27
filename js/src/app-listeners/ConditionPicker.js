import React from 'react';
import PropTypes from 'prop-types';

import Button from 'aui-react/lib/AUIButton';
import Icon from 'aui-react/lib/AUIIcon';

import {jiraService} from '../service/services';
import {ConditionModel} from '../model/listener.model';
import {SingleSelect} from '../common/SingleSelect';
import {CommonMessages} from '../i18n/common.i18n';

import './ConditionPicker.less';
import {AsyncLoadingMultiSelect} from '../common/AsyncLoadingMultiSelect';


const nestingLimit = 2;
const listLimit = 5;

const conditions = {
    'AND': {
        id: 'AND',
        name: 'And',
        requiresChildren: true
    },
    'OR': {
        id: 'OR',
        name: 'Or',
        requiresChildren: true
    },
    'CLASS_NAME': {
        id: 'CLASS_NAME',
        name: 'Class name',
        requiresChildren: false
    },
    'ISSUE_PROJECT': {
        id: 'ISSUE_PROJECT',
        name: 'Issue project',
        requiresChildren: false
    },
    'ISSUE_EVENT_TYPE': {
        id: 'ISSUE_EVENT_TYPE',
        name: 'IssueEvent type',
        requiresChildren: false
    }
};

const projectsLoader = () => jiraService
    .getAllProjects()
    .then(projects => projects.map(project => {
        return {
            value: parseInt(project.id, 10),
            name: `${project.key} - ${project.name}`
        };
    }));

const eventTypeLoader = () => jiraService
    .getEventTypes()
    .then(types => types.map(type => {
        return {
            value: type.id,
            name: type.name
        };
    }));

const conditionList = Object.values(conditions);

export class ConditionPicker extends React.Component {
    static propTypes = {
        value: ConditionModel.isRequired,
        onChange: PropTypes.func.isRequired,
        onDelete: function(props, propName) {
            if (props.isDeletable && !props[propName]) {
                return Error(`${propName} must be specified if component is deletable`);
            }
        },
        isDeletable: PropTypes.bool.isRequired
    };

    static defaultProps = {
        isDeletable: false
    };

    constructor(props) {
        super(props);

        console.log(props.value);
    }

    _onChange = property => {
        return val => {
            const {value, onChange} = this.props;

            onChange({
                ...value,
                [property]: val
            });
        };
    };

    _onInputChange = property => {
        return e => {
            const {value, onChange} = this.props;

            onChange({
                ...value,
                [property]: e.target.value
            });
        };
    };

    _onDelete = e => {
        if (e) {
            e.preventDefault();
        }

        this.props.onDelete();
    };

    _onReset = e => {
        if (e) {
            e.preventDefault();
        }

        const {value, onChange} = this.props;

        onChange({
            key: value.key
        });
    };

    render() {
        const {value, isDeletable} = this.props;

        console.log(value);

        let typeEl = null;
        let paramEl = null;

        if (value.type) {
            typeEl =
                <div>
                    <strong>{conditions[value.type].name}</strong>
                    <a href="#edit" onClick={this._onReset}>
                        <Icon icon="edit"/>
                    </a>
                </div>;

            switch (value.type) {
                case 'AND':
                case 'OR':
                    paramEl = <ConditionList
                        value={value.children || []}
                        onChange={this._onChange('children')}
                    />;
                    break;
                case 'CLASS_NAME':
                    paramEl = <input
                        type="text"
                        className="text long-field"

                        value={value.className || ''}
                        onChange={this._onInputChange('className')}
                    />;
                    break;
                case 'ISSUE_PROJECT':
                    paramEl = <AsyncLoadingMultiSelect
                        value={value.entityIds || []}
                        onChange={this._onChange('entityIds')}
                        loader={projectsLoader}
                    />;
                    break;
                case 'ISSUE_EVENT_TYPE':
                    paramEl = <AsyncLoadingMultiSelect
                        value={value.entityIds || []}
                        onChange={this._onChange('entityIds')}
                        loader={eventTypeLoader}
                    />;
                    break;
                default:
                    paramEl = 'not implemented'; //todo
            }
        } else {
            typeEl = <SingleSelect
                options={conditionList.map(type => { return {value: type.id, name: type.name}; })}
                onChange={this._onInputChange('type')}
            />;
        }

        return <div className="ConditionPicker">
            <div className="flex-row">
                <div className="flex-grow">
                    {typeEl}
                </div>
                {isDeletable && <div className="flex-none">
                    <Button type="subtle" icon="delete" onClick={this._onDelete}>{CommonMessages.delete}</Button>
                </div>}
            </div>
            {paramEl}
        </div>;
    }
}

class ConditionList extends React.Component {
    static propTypes = {
        value: PropTypes.arrayOf(ConditionModel).isRequired,
        onChange: PropTypes.func.isRequired
    };

    constructor(props) {
        super(props);

        this._genNextKey(props);
    }

    _genNextKey = props => {
        const {value} = props;

        if (!value.length) {
            this.nextKey = 0;
            return;
        }

        this.nextKey = Math.max(...value.map(condition => condition.key)) + 1;
    };

    _addItem = (e) => {
        if (e) {
            e.preventDefault();
        }

        this.props.onChange([...this.props.value, {key: this.nextKey++}]);
    };

    _onModified = (key) => {
        return val => {
            const {value, onChange} = this.props;

            onChange(value.map(condition => {
                if (condition.key === key) {
                    return val;
                } else {
                    return condition;
                }
            }));
        };
    };

    _onDelete = (key) => {
        return () => {
            const {value, onChange} = this.props;

            onChange(value.filter(condition => condition.key !== key));
        };
    };

    componentWillReceiveProps(nextProps) {
        this._genNextKey(nextProps);
    }

    render() {
        const {value} = this.props;

        return (
            <div className="flex-column">
                {value.map(condition =>
                    <ConditionPicker
                        key={condition.key}
                        onChange={this._onModified(condition.key)}
                        onDelete={this._onDelete(condition.key)}
                        value={condition}

                        isDeletable={true}
                    />
                )}
                <Button icon="add" type="subtle" onClick={this._addItem}>Add condition</Button>
            </div>
        );
    }
}
