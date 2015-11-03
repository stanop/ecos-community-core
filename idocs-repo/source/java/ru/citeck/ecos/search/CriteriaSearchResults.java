/*
 * Copyright (C) 2008-2015 Citeck LLC.
 *
 * This file is part of Citeck EcoS
 *
 * Citeck EcoS is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Citeck EcoS is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Citeck EcoS. If not, see <http://www.gnu.org/licenses/>.
 */
package ru.citeck.ecos.search;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.alfresco.service.cmr.repository.NodeRef;

public class CriteriaSearchResults {
    
    private final SearchCriteria criteria;
    private final List<NodeRef> results;
    private Long totalCount = 0L;
    private boolean more = false;
    
    private CriteriaSearchResults(SearchCriteria criteria, List<NodeRef> results) {
        this.criteria = criteria;
        this.results = Collections.unmodifiableList(results);
    }
    
    /*package*/ static class Builder {
        
        private SearchCriteria criteria;
        private List<NodeRef> results;
        private Long totalCount;
        private Boolean more;
        
        public Builder criteria(SearchCriteria criteria) {
            this.criteria = criteria;
            return this;
        }
        
        public Builder results(List<NodeRef> results) {
            this.results = new ArrayList<NodeRef>(results);
            return this;
        }
        
        public Builder totalCount(long totalCount) {
            this.totalCount = totalCount;
            return this;
        }
        
        public Builder hasMore(boolean more) {
            this.more = more;
            return this;
        }
        
        public CriteriaSearchResults build() {
            if(criteria == null) {
                throw new IllegalStateException("criteria should be set");
            }
            if(results == null) {
                throw new IllegalStateException("results should be set");
            }
            
            CriteriaSearchResults result = new CriteriaSearchResults(criteria, results);
            if(totalCount != null) {
                result.setTotalCount(totalCount);
            }
            if(more != null) {
                result.setMore(more);
            }
            return result;
        }
    }

    public SearchCriteria getCriteria() {
        return criteria;
    }
    
    public List<NodeRef> getResults() {
        return results;
    }
    
    public boolean hasMore() {
        return this.more;
    }
    
    private void setMore(boolean more) {
        this.more = more;
    }
    
    public Long getTotalCount() {
        return totalCount;
    }
    
    private void setTotalCount(long totalCount) {
        this.totalCount = totalCount;
    }
}
