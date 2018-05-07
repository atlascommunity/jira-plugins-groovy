import com.atlassian.jira.user.ApplicationUser

@WithParam(displayName="User", type=ParamType.USER)
ApplicationUser user

Thread.sleep(5000L)

return user.displayName
