/**
 * For use with Jira 6 and 7.  Jira 8 had breaking API changes.
 **/

import com.atlassian.jira.component.ComponentAccessor
import com.atlassian.jira.issue.search.SearchRequestManager
import com.atlassian.jira.mail.TemplateIssue
import com.atlassian.jira.web.bean.PagerFilter
import com.atlassian.jira.issue.search.SearchProvider
import org.apache.log4j.Logger

jqlSearch = new JqlSearchTool(user: user)

class JqlSearchTool {
    def log = Logger.getLogger(this.getClass())

    private user

    /**
     * Returns the issues found by executing the passed JQL
     * (or null in case of failure).
     */
    def searchByJql(def jql) {
        def clazz = ComponentAccessor.class.classLoader.loadClass("com.atlassian.jira.jql.parser.JqlQueryParser")
        def jqlQueryParser = ComponentAccessor.getComponentOfType(clazz)

        def query = jqlQueryParser.parseQuery(jql)
        if (query == null) {
            log.debug("<{$query.queryString}> could not be parsed")
            return null
        }
        log.debug("<{$query.queryString}> is parsed")

        return search(query)
    }

    /**
     * Returns the issues found by executing the saved filter with the passed ID
     * (or null in case of failure).
     */
    def searchBySavedFilter(def savedFilterId) {
        def searchRequest = ComponentAccessor.getComponentOfType(SearchRequestManager.class).getSearchRequestById(user, savedFilterId)
        if (searchRequest == null) {
            log.debug("Filter #${savedFilterId} not found")
            return null
        }
        log.debug("Filter #${savedFilterId} found: \"${searchRequest.name}\"")

        return search(searchRequest.query)
    }

    private search(def query) {
        def searchResults = ComponentAccessor.getComponentOfType(SearchProvider.class).search(query, user, PagerFilter.getUnlimitedFilter())
        if (searchResults == null) {
            return null
        }
        log.debug("<{$query.queryString}> found ${searchResults.total} issues")

        return searchResults.issues.collect { new TemplateIssue(it, ComponentAccessor.fieldLayoutManager, ComponentAccessor.rendererManager, ComponentAccessor.customFieldManager, null, null) }
    }
}
