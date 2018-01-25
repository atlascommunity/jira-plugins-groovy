define('/mailru/groovy/i18n-react', function() {
    return {
        fields: {
            name: AJS.I18n.getText('ru.mail.jira.plugins.groovy.fields.name'),
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
        },
        common: {
            loading: AJS.I18n.getText('ru.mail.jira.plugins.groovy.common.loading'),
            create: AJS.I18n.getText('ru.mail.jira.plugins.groovy.common.create'),
            cancel: AJS.I18n.getText('ru.mail.jira.plugins.groovy.common.cancel'),
            update: AJS.I18n.getText('ru.mail.jira.plugins.groovy.common.update'),
            currentVersion: AJS.I18n.getText('ru.mail.jira.plugins.groovy.common.currentVersion'),
            edit: AJS.I18n.getText('ru.mail.jira.plugins.groovy.common.edit'),
            delete: AJS.I18n.getText('ru.mail.jira.plugins.groovy.common.delete'),
            showCode: AJS.I18n.getText('ru.mail.jira.plugins.groovy.common.showCode'),
            prev: AJS.I18n.getText('ru.mail.jira.plugins.groovy.common.prev'),
            next: AJS.I18n.getText('ru.mail.jira.plugins.groovy.common.next'),
            of: AJS.I18n.getText('ru.mail.jira.plugins.groovy.common.of'),
            yes: AJS.I18n.getText('ru.mail.jira.plugins.groovy.common.yes'),
            no: AJS.I18n.getText('ru.mail.jira.plugins.groovy.common.no'),
            error: AJS.I18n.getText('ru.mail.jira.plugins.groovy.common.error'),
            script: AJS.I18n.getText('ru.mail.jira.plugins.groovy.common.script'),
            switchTheme: AJS.I18n.getText('ru.mail.jira.plugins.groovy.common.switchTheme'),
            editorMode: AJS.I18n.getText('ru.mail.jira.plugins.groovy.common.editorMode'),
        },
        error: {
            noValue: AJS.I18n.getText('ru.mail.jira.plugins.groovy.error.noValue'),
            notConfigured: AJS.I18n.getText('ru.mail.jira.plugins.groovy.error.notConfigured'),
            errorOccurred: AJS.I18n.getText('ru.mail.jira.plugins.groovy.error.errorOccurred'),
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
            noScripts: AJS.I18n.getText('ru.mail.jira.plugins.groovy.registry.noScripts')
        },
        listener: {
            addListener: AJS.I18n.getText('ru.mail.jira.plugins.groovy.listener.addListener'),
            createListener: AJS.I18n.getText('ru.mail.jira.plugins.groovy.listener.createListener'),
            updateListener: AJS.I18n.getText('ru.mail.jira.plugins.groovy.listener.updateListener'),
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
        },
        rest: {
            nameDescription: AJS.I18n.getText('ru.mail.jira.plugins.groovy.rest.nameDescription'),
            addScript: AJS.I18n.getText('ru.mail.jira.plugins.groovy.rest.addScript'),
            updateScript: AJS.I18n.getText('ru.mail.jira.plugins.groovy.rest.updateScript'),
            createScript: AJS.I18n.getText('ru.mail.jira.plugins.groovy.rest.createScript'),
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
            runAs: AJS.I18n.getText('ru.mail.jira.plugins.groovy.scheduled.runAs'),
            jqlLimitDescription: function(...params) {
                return AJS.I18n.getText('ru.mail.jira.plugins.groovy.scheduled.jqlLimitDescription', ...params);
            }
        }
    };
});
