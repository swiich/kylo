/**
 * 
 */
package com.thinkbiganalytics.metadata.api.catalog;

/*-
 * #%L
 * kylo-metadata-api
 * %%
 * Copyright (C) 2017 - 2018 ThinkBig Analytics, a Teradata Company
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import com.thinkbiganalytics.metadata.api.BaseProvider;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 *
 */
public interface DataSetProvider extends BaseProvider<DataSet, DataSet.ID> {
    
    DataSet.ID resolveId(Serializable id);

    DataSet create(DataSource.ID dataSourceId, String title);

    List<DataSet> findByDataSource(DataSource.ID dsId, DataSource.ID... otherIds);
    
    List<DataSet> findByDataSource(Collection<DataSource.ID> dsIds);

    public DataSet findByDataSourceAndTitle(DataSource.ID dataSourceId, String title);
    
    List<DataSet> findAll();
    
    Page<DataSet> findPage(Pageable page, String filter);

    Optional<DataSet> find(DataSet.ID id);
    
    void deleteById(DataSet.ID id);

}
