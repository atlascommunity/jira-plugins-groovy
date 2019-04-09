package ru.mail.jira.plugins.groovy.api.entity;

import net.java.ao.OneToMany;
import net.java.ao.Preload;
import net.java.ao.schema.Indexed;
import net.java.ao.schema.NotNull;
import net.java.ao.schema.StringLength;

//https://ecosystem.atlassian.net/browse/AO-3454 FK column directory_id isn't preloaded by default, so we must specify all columns
@Preload({"ID", "UUID", "NAME", "TYPES", "DESCRIPTION", "DIRECTORY_ID", "SCRIPT_BODY", "DELETED", "PARAMETERS"})
public interface Script extends AbstractScript {
    @Indexed
    void setUuid(String uuid);
    String getUuid();

    @NotNull
    @StringLength(StringLength.UNLIMITED)
    void setScriptBody(String scriptBody);
    String getScriptBody();

    @StringLength(StringLength.UNLIMITED)
    void setParameters(String parameters);
    String getParameters();

    @NotNull
    void setDirectory(ScriptDirectory directory);
    ScriptDirectory getDirectory();

    @OneToMany(reverse = "getScript")
    Changelog[] getChangelogs();

    void setTypes(String types);
    String getTypes();
}
