<h1>Push to Business Central Failed/h1>
<p>Please review and resolve any conflicts between the git repositories repos ${gitRepo} and ${bcRepo}. The system attempted to clear the changes from Business Central, but that failed.</p>
<#if errorMessage??><p>The following error message was reported: ${errorMessage}</p></#if>
<#if userContact??> <p>When the issue is resolved, please notify ${userContact}.</p></#if>
<p>Thank you</p>