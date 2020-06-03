package de.unistuttgart.ipvs.as.mmp.search.repository;

import de.unistuttgart.ipvs.as.mmp.common.domain.Model;
import de.unistuttgart.ipvs.as.mmp.common.domain.opcua.OpcuaObjectNode;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import org.hibernate.search.jpa.FullTextEntityManager;
import org.hibernate.search.jpa.Search;
import org.hibernate.search.query.dsl.BooleanJunction;
import org.hibernate.search.query.dsl.QueryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.LinkedList;
import java.util.List;

@Service
public class ModelSearchDao {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private boolean indexBuilt;

    @PersistenceContext
    private EntityManager entityManager;

    private void initializeHibernateSearch() {
        if (!indexBuilt) {
            try {
                log.info("Start initialize Hibernate Search");
                FullTextEntityManager fullTextEntityManager = Search.getFullTextEntityManager(entityManager);
                fullTextEntityManager.createIndexer().startAndWait();
                indexBuilt = true;
            } catch (InterruptedException e) {
                log.info("Failed to create Indexer: {}", e);
                indexBuilt = false;
                Thread.currentThread().interrupt();
            }
        }
    }

    public List<Model> getModelsByFiltersAndSearchTerms(List<String> searchTerms, List<String> algorithmNames,
                                                        List<String> machineNames, List<String> sensorNames) {
        initializeHibernateSearch();

        BooleanQuery query = createBooleanSearchQueryWithFilters(searchTerms, algorithmNames, machineNames, sensorNames);
        if (query == null) {
            throw new IllegalArgumentException();
        }
        FullTextEntityManager fullTextEntityManager = Search.getFullTextEntityManager(entityManager);
        org.hibernate.search.jpa.FullTextQuery jpaQuery
                = fullTextEntityManager.createFullTextQuery(query, Model.class);

        return jpaQuery.getResultList();

    }

    private BooleanQuery createBooleanSearchQueryWithFilters(List<String> searchTerms, List<String> algorithmNames,
                                                             List<String> machineNames, List<String> sensorNames) {

        BooleanQuery filterQuery = createBooleanQueryForFilters(algorithmNames, machineNames, sensorNames);
        BooleanQuery searchQuery = createBooleanSearchQuery(searchTerms);

        BooleanQuery.Builder finalQuery = new BooleanQuery.Builder();

        if (filterQuery == null && searchQuery == null) {
            return null;
        }

        if (filterQuery != null) {
            finalQuery.add(filterQuery, BooleanClause.Occur.MUST);
        }

        if (searchQuery != null) {
            finalQuery.add(searchQuery, BooleanClause.Occur.MUST);
        }

        return finalQuery.build();

    }

    private BooleanQuery createBooleanSearchQuery(List<String> searchTerms) {
        searchTerms = removeEmptyStringsFromList(searchTerms);

        if (searchTerms == null) {
            return null;
        }

        FullTextEntityManager fullTextEntityManager = Search.getFullTextEntityManager(entityManager);
        QueryBuilder queryBuilder = fullTextEntityManager.getSearchFactory()
                .buildQueryBuilder()
                .forEntity(Model.class)
                .get();


        List<Query> queryList = new LinkedList<>();
        Query query;
        BooleanJunction booleanJunction;

        for (String searchTerm : searchTerms) {
            searchTerm = QueryParser.escape(searchTerm).toLowerCase();
            searchTerm = "*" + searchTerm + "*";

            booleanJunction = queryBuilder.bool()
                    .should(queryBuilder.keyword().wildcard().onField("modelMetadata.ALL")
                            .matching(searchTerm).createQuery())
                    .should(queryBuilder.keyword().wildcard().onField("modelMetadata.status")
                            .ignoreFieldBridge().matching(searchTerm).createQuery())

                    .should(queryBuilder.keyword().wildcard().onField("modelMetadata.pmmlMetadata.ALL")
                            .matching(searchTerm).createQuery())
                    .should(queryBuilder.keyword().wildcard().onField("modelMetadata.pmmlMetadata.inputAttributes.ALL")
                            .matching(searchTerm).createQuery())
                    .should(queryBuilder.keyword().wildcard().onField("modelMetadata.pmmlMetadata.outputAttributes.ALL")
                            .matching(searchTerm).createQuery())

                    .should(queryBuilder.keyword().wildcard().onField("modelMetadata.author.ALL")
                            .matching(searchTerm).createQuery())

                    .should(queryBuilder.keyword().wildcard().onField("modelMetadata.hyperparameters.ALL")
                            .matching(searchTerm).createQuery())

                    .should(queryBuilder.keyword().wildcard().onField("modelMetadata.customFields.ALL")
                            .matching(searchTerm).createQuery())

                    .should(queryBuilder.keyword().wildcard().onField("modelMetadata.trainingRuns.ALL")
                            .matching(searchTerm).createQuery())
                    .should(queryBuilder.keyword().wildcard().onField("modelMetadata.trainingRuns.currentScore.ALL")
                            .matching(searchTerm).createQuery())

                    .should(queryBuilder.keyword().wildcard().onField("modelMetadata.transformations.ALL")
                            .matching(searchTerm).createQuery())
                    .should(queryBuilder.keyword().wildcard().onField("modelMetadata.transformations.input.ALL")
                            .matching(searchTerm).createQuery())
                    .should(queryBuilder.keyword().wildcard().onField("modelMetadata.transformations.output.ALL")
                            .matching(searchTerm).createQuery())
                    .should(queryBuilder.keyword().wildcard().onField("modelMetadata.transformations.attributeToTransform.ALL")
                            .matching(searchTerm).createQuery())

                    .should(queryBuilder.keyword().wildcard().onField("modelMetadata.predictionMetadata.ALL")
                            .matching(searchTerm).createQuery())
                    .should(queryBuilder.keyword().wildcard().onField("modelMetadata.predictionMetadata.evaluation.ALL")
                            .matching(searchTerm).createQuery())
                    .should(queryBuilder.keyword().wildcard().onField("modelMetadata.predictionMetadata.evaluation.scores.ALL")
                            .matching(searchTerm).createQuery())

                    .should(queryBuilder.keyword().wildcard().onField("project.ALL")
                            .matching(searchTerm).createQuery())
                    .should(queryBuilder.keyword().wildcard().onField("project.status")
                            .ignoreFieldBridge().matching(searchTerm).createQuery())
                    .should(queryBuilder.keyword().wildcard().onField("project.editors.ALL")
                            .matching(searchTerm).createQuery())

                    .should(queryBuilder.keyword().wildcard().onField("opcuaInformationModels.ALL")
                            .matching(searchTerm).createQuery())
                    .should(queryBuilder.keyword().wildcard().onField("opcuaInformationModels.dbFile.ALL")
                            .matching(searchTerm).createQuery())
                    .should(queryBuilder.keyword().wildcard().onField("opcuaInformationModels.opcuaMetadata")
                            .matching(searchTerm).createQuery())
                    .should(queryBuilder.keyword().wildcard().onField("opcuaInformationModels.opcuaMetadata.machine.ALL")
                            .matching(searchTerm).createQuery())
                    .should(queryBuilder.keyword().wildcard().onField("opcuaInformationModels.opcuaMetadata.sensors.ALL")
                            .matching(searchTerm).createQuery())
                    .should(queryBuilder.keyword().wildcard().onField("relationalDBInformation.ALL")
                            .matching(searchTerm).createQuery())

                    .should(queryBuilder.keyword().wildcard().onField("modelFile.dbFile.ALL")
                            .matching(searchTerm).createQuery());


            Query opcuaMachienQuery = createOPCUASearchQuery(queryBuilder,
                    "opcuaInformationModels.opcuaMetadata.machine", searchTerm);
            Query opcuaSensorsQuery = createOPCUASearchQuery(queryBuilder,
                    "opcuaInformationModels.opcuaMetadata.sensors", searchTerm);
            query = booleanJunction.should(opcuaMachienQuery).should(opcuaSensorsQuery).createQuery();

            queryList.add(query);

        }

        BooleanQuery.Builder queryFinal = new BooleanQuery.Builder();

        for (Query q : queryList) {
            queryFinal.add(q, BooleanClause.Occur.MUST);
        }

        return queryFinal.build();
    }

    // Create recursive a deeper search query for the opcua tree.
    private Query createOPCUASearchQuery(QueryBuilder queryBuilder, String opcuaField, String searchTerm) {
        String components = opcuaField + ".components";
        String variables = opcuaField + ".variables";
        String properties = opcuaField + ".properties";

        BooleanJunction booleanJunction = queryBuilder.bool()
                .should(queryBuilder.keyword().wildcard().onField(components + ".ALL")
                        .matching(searchTerm).createQuery())
                .should(queryBuilder.keyword().wildcard().onField(variables + ".ALL")
                        .matching(searchTerm).createQuery())
                .should(queryBuilder.keyword().wildcard().onField(properties + ".ALL")
                        .matching(searchTerm).createQuery());

        // +2 because of opcuainformationsmodels.opcuaMetadata
        if (opcuaField.split("[.]").length < OpcuaObjectNode.MAXIMUM_DEPTH + 2) {
            booleanJunction.should(createOPCUASearchQuery(queryBuilder, components, searchTerm))
                    .should(createOPCUASearchQuery(queryBuilder, variables, searchTerm))
                    .should(createOPCUASearchQuery(queryBuilder, properties, searchTerm));
        }

        return booleanJunction.createQuery();
    }

    private BooleanQuery createBooleanQueryForFilters(List<String> algorithmNames,
                                                      List<String> machineNames, List<String> sensorNames) {

        algorithmNames = removeEmptyStringsFromList(algorithmNames);
        machineNames = removeEmptyStringsFromList(machineNames);
        sensorNames = removeEmptyStringsFromList(sensorNames);

        if (algorithmNames == null && machineNames == null && sensorNames == null) {
            return null;
        }

        FullTextEntityManager fullTextEntityManager = Search.getFullTextEntityManager(entityManager);
        QueryBuilder queryBuilder = fullTextEntityManager.getSearchFactory()
                .buildQueryBuilder()
                .forEntity(Model.class)
                .get();


        List<Query> queryListAlgorithm = null;
        List<Query> queryListMachine = null;
        List<Query> queryListSensors = null;

        BooleanQuery.Builder booleanQueryAlgorithm = null;
        BooleanQuery.Builder booleanQueryMachine = null;
        BooleanQuery.Builder booleanQuerySensors = null;

        Query query;

        if (algorithmNames != null) {
            queryListAlgorithm = new LinkedList<>();
            booleanQueryAlgorithm = new BooleanQuery.Builder();

            for (String algorithmName : algorithmNames) {
                algorithmName = QueryParser.escape(algorithmName);
                query = queryBuilder.phrase().withSlop(0).onField("modelMetadata.algorithm")
                        .sentence(algorithmName).createQuery();
                queryListAlgorithm.add(query);
            }
        }

        if (machineNames != null) {
            queryListMachine = new LinkedList<>();
            booleanQueryMachine = new BooleanQuery.Builder();

            for (String machineName : machineNames) {
                machineName = QueryParser.escape(machineName);
                query = queryBuilder.bool()
                        .should(queryBuilder.phrase().withSlop(0).onField("opcuaInformationModels.opcuaMetadata.machine.displayName")
                                .sentence(machineName).createQuery())

                        // If machine node name is not defined and no relational db information is set and the filter
                        // term contains the null token.
                        .should(
                                queryBuilder.bool()
                                        .must(queryBuilder.keyword().onField("relationalDBInformation").matching("_null_").createQuery())
                                        .must(queryBuilder.bool()
                                                .should(queryBuilder.phrase().withSlop(0).onField("opcuaInformationModels")
                                                        .sentence(machineName).createQuery())
                                                .should(queryBuilder.phrase().withSlop(0).onField("opcuaInformationModels.opcuaMetadata")
                                                        .sentence(machineName).createQuery())
                                                .should(queryBuilder.phrase().withSlop(0).onField("opcuaInformationModels.opcuaMetadata.machine")
                                                        .sentence(machineName).createQuery())

                                                .createQuery()
                                        )
                                        .createQuery()
                        )
                        .createQuery();
                queryListMachine.add(query);
            }
        }

        if (sensorNames != null) {
            queryListSensors = new LinkedList<>();
            booleanQuerySensors = new BooleanQuery.Builder();

            for (String sensorName : sensorNames) {
                sensorName = QueryParser.escape(sensorName);
                query = queryBuilder.bool()
                        .should(queryBuilder.phrase().withSlop(0).onField("opcuaInformationModels.opcuaMetadata.sensors.displayName")
                                .sentence(sensorName)
                                .createQuery())
                        // If sensor node name is not defined and no relational db information is set and the filter
                        // term contains the null token.
                        .should(
                                queryBuilder.bool()
                                        .must(queryBuilder.keyword().onField("relationalDBInformation").matching("_null_").createQuery())
                                        .must(queryBuilder.bool()
                                                .should(queryBuilder.phrase().withSlop(0).onField("opcuaInformationModels")
                                                        .sentence(sensorName).createQuery())
                                                .should(queryBuilder.phrase().withSlop(0).onField("opcuaInformationModels.opcuaMetadata")
                                                        .sentence(sensorName).createQuery())
                                                .should(queryBuilder.phrase().withSlop(0).onField("opcuaInformationModels.opcuaMetadata.sensors")
                                                        .sentence(sensorName).createQuery())

                                                .createQuery()
                                        )
                                        .createQuery()
                        )
                        .createQuery();
                queryListSensors.add(query);
            }
        }

        BooleanQuery.Builder queryFinal = new BooleanQuery.Builder();

        if (queryListAlgorithm != null) {
            for (Query q : queryListAlgorithm) {
                booleanQueryAlgorithm.add(q, BooleanClause.Occur.SHOULD);
            }
            queryFinal.add(booleanQueryAlgorithm.build(), BooleanClause.Occur.MUST);
        }

        if (queryListMachine != null) {
            for (Query q : queryListMachine) {
                booleanQueryMachine.add(q, BooleanClause.Occur.SHOULD);
            }
            queryFinal.add(booleanQueryMachine.build(), BooleanClause.Occur.MUST);
        }

        if (queryListSensors != null) {
            for (Query q : queryListSensors) {
                booleanQuerySensors.add(q, BooleanClause.Occur.SHOULD);
            }
            queryFinal.add(booleanQuerySensors.build(), BooleanClause.Occur.MUST);
        }

        return queryFinal.build();

    }

    private List<String> removeEmptyStringsFromList(List<String> list) {
        if (list == null) {
            return null;
        }

        List<String> newList = new LinkedList<>();
        for (String str : list) {
            if (str != null && !str.equals("")) {
                newList.add(str);
            }
        }

        if (newList.isEmpty()) {
            return null;
        }
        return newList;
    }

}
