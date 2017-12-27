define('/mailru/groovy/i18n-react', function() {
    return {
        fields: {
            name: AJS.I18n.getText('ru.mail.jira.plugins.groovy.fields.name'),
            scriptCode: AJS.I18n.getText('ru.mail.jira.plugins.groovy.fields.scriptCode'),
            comment: AJS.I18n.getText('ru.mail.jira.plugins.groovy.fields.comment'),
            condition: AJS.I18n.getText('ru.mail.jira.plugins.groovy.fields.condition')
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
        },
        dialog: {
            notReady: AJS.I18n.getText('ru.mail.jira.plugins.groovy.dialog.notReady')
        }
    };
});
