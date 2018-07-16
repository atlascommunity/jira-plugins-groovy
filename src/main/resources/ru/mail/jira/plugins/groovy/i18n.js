define('mailru/groovy/i18n-react', [], function() {
    console.log('defining i18n');

    return {
        fields: {
            name: AJS.I18n.getText('ru.mail.jira.plugins.groovy.fields.name'),
            description: AJS.I18n.getText('ru.mail.jira.plugins.groovy.fields.description'),
            scriptCode: AJS.I18n.getText('ru.mail.jira.plugins.groovy.fields.scriptCode'),
            comment: AJS.I18n.getText('ru.mail.jira.plugins.groovy.fields.comment'),
            condition: AJS.I18n.getText('ru.mail.jira.plugins.groovy.fields.condition'),
            date: AJS.I18n.getText('ru.mail.jira.plugins.groovy.fields.date'),
            time: AJS.I18n.getText('ru.mail.jira.plugins.groovy.fields.time'),
            bindings: AJS.I18n.getText('ru.mail.jira.plugins.groovy.fields.bindings'),
            successful: AJS.I18n.getText('ru.mail.jira.plugins.groovy.fields.successful'),
            httpMethods: AJS.I18n.getText('ru.mail.jira.plugins.groovy.fields.httpMethods'),
            projects: AJS.I18n.getText('ru.mail.jira.plugins.groovy.fields.projects'),
            eventTypes: AJS.I18n.getText('ru.mail.jira.plugins.groovy.fields.eventTypes'),
            type: AJS.I18n.getText('ru.mail.jira.plugins.groovy.fields.type'),
            customField: AJS.I18n.getText('ru.mail.jira.plugins.groovy.fields.customField'),
            cacheable: AJS.I18n.getText('ru.mail.jira.plugins.groovy.fields.cacheable'),
            schedule: AJS.I18n.getText('ru.mail.jira.plugins.groovy.fields.schedule'),
            issueJql: AJS.I18n.getText('ru.mail.jira.plugins.groovy.fields.issueJql'),
            workflow: AJS.I18n.getText('ru.mail.jira.plugins.groovy.fields.workflow'),
            workflowAction: AJS.I18n.getText('ru.mail.jira.plugins.groovy.fields.workflowAction'),
            parentName: AJS.I18n.getText('ru.mail.jira.plugins.groovy.fields.parentName'),
            action: AJS.I18n.getText('ru.mail.jira.plugins.groovy.fields.action'),
            order: AJS.I18n.getText('ru.mail.jira.plugins.groovy.fields.order'),
            searcher: AJS.I18n.getText('ru.mail.jira.plugins.groovy.fields.searcher'),
            groups: AJS.I18n.getText('ru.mail.jira.plugins.groovy.fields.groups'),
            issue: AJS.I18n.getText('ru.mail.jira.plugins.groovy.fields.issue'),
            options: AJS.I18n.getText('ru.mail.jira.plugins.groovy.fields.options'),
            template: AJS.I18n.getText('ru.mail.jira.plugins.groovy.fields.template')
        },
        common: {
            loading: AJS.I18n.getText('ru.mail.jira.plugins.groovy.common.loading'),
            completed: AJS.I18n.getText('ru.mail.jira.plugins.groovy.common.completed'),
            create: AJS.I18n.getText('ru.mail.jira.plugins.groovy.common.create'),
            cancel: AJS.I18n.getText('ru.mail.jira.plugins.groovy.common.cancel'),
            close: AJS.I18n.getText('ru.mail.jira.plugins.groovy.common.close'),
            update: AJS.I18n.getText('ru.mail.jira.plugins.groovy.common.update'),
            currentVersion: AJS.I18n.getText('ru.mail.jira.plugins.groovy.common.currentVersion'),
            edit: AJS.I18n.getText('ru.mail.jira.plugins.groovy.common.edit'),
            delete: AJS.I18n.getText('ru.mail.jira.plugins.groovy.common.delete'),
            showCode: AJS.I18n.getText('ru.mail.jira.plugins.groovy.common.showCode'),
            hideCode: AJS.I18n.getText('ru.mail.jira.plugins.groovy.common.hideCode'),
            prev: AJS.I18n.getText('ru.mail.jira.plugins.groovy.common.prev'),
            next: AJS.I18n.getText('ru.mail.jira.plugins.groovy.common.next'),
            of: AJS.I18n.getText('ru.mail.jira.plugins.groovy.common.of'),
            yes: AJS.I18n.getText('ru.mail.jira.plugins.groovy.common.yes'),
            no: AJS.I18n.getText('ru.mail.jira.plugins.groovy.common.no'),
            notSpecified: AJS.I18n.getText('ru.mail.jira.plugins.groovy.common.notSpecified'),
            error: AJS.I18n.getText('ru.mail.jira.plugins.groovy.common.error'),
            script: AJS.I18n.getText('ru.mail.jira.plugins.groovy.common.script'),
            switchTheme: AJS.I18n.getText('ru.mail.jira.plugins.groovy.common.switchTheme'),
            editorMode: AJS.I18n.getText('ru.mail.jira.plugins.groovy.common.editorMode'),
            clearCache: AJS.I18n.getText('ru.mail.jira.plugins.groovy.common.clearCache'),
            condition: AJS.I18n.getText('ru.mail.jira.plugins.groovy.common.condition'),
            validator: AJS.I18n.getText('ru.mail.jira.plugins.groovy.common.validator'),
            function: AJS.I18n.getText('ru.mail.jira.plugins.groovy.common.function'),
            showAll: AJS.I18n.getText('ru.mail.jira.plugins.groovy.common.showAll'),
            preview: AJS.I18n.getText('ru.mail.jira.plugins.groovy.common.preview'),
            validating: AJS.I18n.getText('ru.mail.jira.plugins.groovy.common.validating'),
            issuesFound: function(...params) {
                return AJS.I18n.getText('ru.mail.jira.plugins.groovy.common.issuesFound', ...params);
            },
            run: AJS.I18n.getText('ru.mail.jira.plugins.groovy.common.run'),
            back: AJS.I18n.getText('ru.mail.jira.plugins.groovy.common.back'),
            returnTypes: AJS.I18n.getText('ru.mail.jira.plugins.groovy.common.returnTypes'),
            confirmDelete: function(...params) {
                return AJS.I18n.getText('ru.mail.jira.plugins.groovy.common.confirmDelete', ...params);
            },
            all: AJS.I18n.getText('ru.mail.jira.plugins.groovy.common.all'),
            permalink: AJS.I18n.getText('ru.mail.jira.plugins.groovy.common.permalink')
        },
        error: {
            noValue: AJS.I18n.getText('ru.mail.jira.plugins.groovy.error.noValue'),
            notConfigured: AJS.I18n.getText('ru.mail.jira.plugins.groovy.error.notConfigured'),
            errorOccurred: AJS.I18n.getText('ru.mail.jira.plugins.groovy.error.errorOccurred'),
            incorrectConfigId: AJS.I18n.getText('ru.mail.jira.plugins.groovy.error.incorrectConfigId')
        },
        console: {
            execute: AJS.I18n.getText('ru.mail.jira.plugins.groovy.console.execute'),
            executedIn: function(...params) {
                return AJS.I18n.getText('ru.mail.jira.plugins.groovy.console.executedIn', ...params);
            }
        },
        registry: {
            addScript: AJS.I18n.getText('ru.mail.jira.plugins.groovy.registry.addScript'),
            addDirectory: AJS.I18n.getText('ru.mail.jira.plugins.groovy.registry.addDirectory'),
            editScript: AJS.I18n.getText('ru.mail.jira.plugins.groovy.registry.editScript'),
            editDirectory: AJS.I18n.getText('ru.mail.jira.plugins.groovy.registry.editDirectory'),
            deleteScript: AJS.I18n.getText('ru.mail.jira.plugins.groovy.registry.deleteScript'),
            deleteDirectory: AJS.I18n.getText('ru.mail.jira.plugins.groovy.registry.deleteDirectory'),
            noScripts: AJS.I18n.getText('ru.mail.jira.plugins.groovy.registry.noScripts'),
            findWorkflows: AJS.I18n.getText('ru.mail.jira.plugins.groovy.registry.findWorkflows'),
            onlyUnused: AJS.I18n.getText('ru.mail.jira.plugins.groovy.registry.onlyUnused')
        },
        listener: {
            addListener: AJS.I18n.getText('ru.mail.jira.plugins.groovy.listener.addListener'),
            editListener: AJS.I18n.getText('ru.mail.jira.plugins.groovy.listener.editListener'),
            createListener: AJS.I18n.getText('ru.mail.jira.plugins.groovy.listener.createListener'),
            updateListener: AJS.I18n.getText('ru.mail.jira.plugins.groovy.listener.updateListener'),
            deleteListener: AJS.I18n.getText('ru.mail.jira.plugins.groovy.listener.deleteListener'),
            addCondition: AJS.I18n.getText('ru.mail.jira.plugins.groovy.listener.addCondition'),
            noListeners: AJS.I18n.getText('ru.mail.jira.plugins.groovy.listener.noListeners'),
            type: {
                className: AJS.I18n.getText('ru.mail.jira.plugins.groovy.listener.type.className'),
                issueEvent: AJS.I18n.getText('ru.mail.jira.plugins.groovy.listener.type.issueEvent')
            }
        },
        audit: {
            user: AJS.I18n.getText('ru.mail.jira.plugins.groovy.audit.user'),
            category: AJS.I18n.getText('ru.mail.jira.plugins.groovy.audit.category'),
            action: AJS.I18n.getText('ru.mail.jira.plugins.groovy.audit.action'),
            description: AJS.I18n.getText('ru.mail.jira.plugins.groovy.audit.description'),
            script: AJS.I18n.getText('ru.mail.jira.plugins.groovy.audit.script'),
            registryScript: AJS.I18n.getText('ru.mail.jira.plugins.groovy.audit.registryScript'),
            registryDirectory: AJS.I18n.getText('ru.mail.jira.plugins.groovy.audit.registryDirectory'),
            listener: AJS.I18n.getText('ru.mail.jira.plugins.groovy.audit.listener'),
            rest: AJS.I18n.getText('ru.mail.jira.plugins.groovy.audit.rest'),
            cf: AJS.I18n.getText('ru.mail.jira.plugins.groovy.audit.cf'),
            scheduledTask: AJS.I18n.getText('ru.mail.jira.plugins.groovy.audit.scheduledTask'),
            adminScript: AJS.I18n.getText('ru.mail.jira.plugins.groovy.audit.adminScript'),
            restore: AJS.I18n.getText('ru.mail.jira.plugins.groovy.audit.restore'),
            noItems: AJS.I18n.getText('ru.mail.jira.plugins.groovy.audit.noItems')
        },
        rest: {
            nameDescription: AJS.I18n.getText('ru.mail.jira.plugins.groovy.rest.nameDescription'),
            addScript: AJS.I18n.getText('ru.mail.jira.plugins.groovy.rest.addScript'),
            updateScript: AJS.I18n.getText('ru.mail.jira.plugins.groovy.rest.updateScript'),
            createScript: AJS.I18n.getText('ru.mail.jira.plugins.groovy.rest.createScript'),
            deleteScript: AJS.I18n.getText('ru.mail.jira.plugins.groovy.rest.deleteScript'),
            noScripts: AJS.I18n.getText('ru.mail.jira.plugins.groovy.rest.noScripts'),
        },
        dialog: {
            notReady: AJS.I18n.getText('ru.mail.jira.plugins.groovy.dialog.notReady')
        },
        jira: {
            configure: AJS.I18n.getText('admin.common.words.configure'),
            edit: AJS.I18n.getText('common.words.edit')
        },
        field: {
            scriptFor: function(...params) {
                return AJS.I18n.getText('ru.mail.jira.plugins.groovy.field.scriptFor', ...params);
            },
            noFields: AJS.I18n.getText('ru.mail.jira.plugins.groovy.field.noFields')
        },
        titles: {
            console: AJS.I18n.getText('ru.mail.jira.plugins.groovy.link.console'),
            adminScripts: AJS.I18n.getText('ru.mail.jira.plugins.groovy.link.adminScripts'),
            registry: AJS.I18n.getText('ru.mail.jira.plugins.groovy.link.registry'),
            listeners: AJS.I18n.getText('ru.mail.jira.plugins.groovy.link.listeners'),
            audit: AJS.I18n.getText('ru.mail.jira.plugins.groovy.link.audit'),
            rest: AJS.I18n.getText('ru.mail.jira.plugins.groovy.link.rest'),
            fields: AJS.I18n.getText('ru.mail.jira.plugins.groovy.link.fields'),
            scheduled: AJS.I18n.getText('ru.mail.jira.plugins.groovy.link.scheduled'),
            extras: AJS.I18n.getText('ru.mail.jira.plugins.groovy.link.extras'),
        },
        scheduled: {
            noTasks: AJS.I18n.getText('ru.mail.jira.plugins.groovy.scheduled.noTasks'),
            addTask: AJS.I18n.getText('ru.mail.jira.plugins.groovy.scheduled.addTask'),
            editTask: AJS.I18n.getText('ru.mail.jira.plugins.groovy.scheduled.editTask'),
            deleteTask: AJS.I18n.getText('ru.mail.jira.plugins.groovy.scheduled.deleteTask'),
            runAs: AJS.I18n.getText('ru.mail.jira.plugins.groovy.scheduled.runAs'),
            runNow: AJS.I18n.getText('ru.mail.jira.plugins.groovy.scheduled.runNow'),
            runNowConfirm: function(...params) {
                return AJS.I18n.getText('ru.mail.jira.plugins.groovy.scheduled.runNowConfirm', ...params)
            },
            lastRun: AJS.I18n.getText('ru.mail.jira.plugins.groovy.scheduled.lastRun'),
            nextRun: AJS.I18n.getText('ru.mail.jira.plugins.groovy.scheduled.nextRun'),
            jqlLimitDescription: function(...params) {
                return AJS.I18n.getText('ru.mail.jira.plugins.groovy.scheduled.jqlLimitDescription', ...params);
            },
            jqlScriptDescription: AJS.I18n.getText('ru.mail.jira.plugins.groovy.scheduled.jqlScriptDescription'),
            scheduleDescription: AJS.I18n.getText('ru.mail.jira.plugins.groovy.scheduled.scheduleDescription'),
            transitionOptions: AJS.I18n.getText('ru.mail.jira.plugins.groovy.scheduled.transitionOptions'),
            transitionOption: {
                skipConditions: AJS.I18n.getText('ru.mail.jira.plugins.groovy.scheduled.transitionOption.skipConditions'),
                skipValidators: AJS.I18n.getText('ru.mail.jira.plugins.groovy.scheduled.transitionOption.skipValidators'),
                skipPermissions: AJS.I18n.getText('ru.mail.jira.plugins.groovy.scheduled.transitionOption.skipPermissions')
            }
        },
        adminScript: {
            noParams: AJS.I18n.getText('ru.mail.jira.plugins.groovy.adminScripts.noParams'),
            builtIn: AJS.I18n.getText('ru.mail.jira.plugins.groovy.adminScripts.builtIn')
        }
    };
});
