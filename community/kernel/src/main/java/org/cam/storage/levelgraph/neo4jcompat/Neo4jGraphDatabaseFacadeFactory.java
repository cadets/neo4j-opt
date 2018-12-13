/*
 *   Copyright (c) 2018.
 *   This file is part of NeGraph.
 *
 *  NeGraph is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  NeGraph is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with NeGraph.  If not, see <https://www.gnu.org/licenses/>.
 * @author Jyothish Soman, cl cam uk
 */

package org.cam.storage.levelgraph.neo4jcompat;

import org.neo4j.kernel.AvailabilityGuard;
import org.neo4j.kernel.configuration.Config;
import org.neo4j.kernel.impl.factory.*;
import org.neo4j.kernel.impl.query.QueryExecutionEngine;
import org.neo4j.logging.Logger;

import java.io.File;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

public class Neo4jGraphDatabaseFacadeFactory extends GraphDatabaseFacadeFactory {
    public Neo4jGraphDatabaseFacadeFactory(DatabaseInfo databaseInfo, Function<PlatformModule, EditionModule> editionFactory) {
        super(databaseInfo, editionFactory);
    }

    public GraphDatabaseFacade newFacade(File storeDir, Config config, GraphDatabaseFacadeFactory.Dependencies dependencies) {
        return this.initFacade(storeDir, config, dependencies, new GraphDatabaseFacade());
    }

    public GraphDatabaseFacade initFacade(File storeDir, Map<String, String> params, GraphDatabaseFacadeFactory.Dependencies dependencies, GraphDatabaseFacade graphDatabaseFacade) {
        return this.initFacade(storeDir, Config.defaults(params), dependencies, graphDatabaseFacade);
    }

    /*
        public GraphDatabaseFacade initFacade(File storeDir, Config config, final GraphDatabaseFacadeFactory.Dependencies dependencies, GraphDatabaseFacade graphDatabaseFacade) {
            final PlatformModule platform = this.createPlatform(storeDir, config, dependencies, graphDatabaseFacade);
            EditionModule edition = this.editionFactory.apply(platform);
            final AtomicReference<QueryExecutionEngine> queryEngine = new AtomicReference(QueryEngineProvider.noEngine());
            queryEngine.getClass();
            DataSourceModule dataSource = this.createDataSource(platform, edition, queryEngine::get);
            Logger msgLog = platform.logging.getInternalLog(this.getClass()).infoLogger();
            CoreAPIAvailabilityGuard coreAPIAvailabilityGuard = edition.coreAPIAvailabilityGuard;
            ClassicCoreSPI spi = new ClassicCoreSPI(platform, dataSource, msgLog, coreAPIAvailabilityGuard);
            graphDatabaseFacade.init(spi, dataSource.guard, dataSource.threadToTransactionBridge, platform.config);
            platform.dataSourceManager.addListener(new Listener() {
                private QueryExecutionEngine engine;

                public void registered(NeoStoreDataSource dataSource) {
                    if (this.engine == null) {
                        this.engine = QueryEngineProvider.initialize(platform.dependencies, platform.graphDatabaseFacade, dependencies.executionEngines());
                    }

                    queryEngine.set(this.engine);
                }

                public void unregistered(NeoStoreDataSource dataSource) {
                    queryEngine.set(QueryEngineProvider.noEngine());
                }
            });
            Object error = null;

            try {
                this.enableAvailabilityLogging(platform.availabilityGuard, msgLog);
                platform.life.start();
            } catch (Throwable var22) {
                error = new RuntimeException("Error starting " + this.getClass().getName() + ", " + platform.storeDir, var22);
            } finally {
                if (error != null) {
                    try {
                        graphDatabaseFacade.shutdown();
                    } catch (Throwable var21) {
                        error = Exceptions.withSuppressed(var21, (Throwable) error);
                    }
                }

            }

            if (error != null) {
                msgLog.log("Failed to start database", (Throwable) error);
                throw Exceptions.launderedException((Throwable) error);
            } else {
                return graphDatabaseFacade;
            }
        }
    */
    protected PlatformModule createPlatform(File storeDir, Config config, GraphDatabaseFacadeFactory.Dependencies dependencies, GraphDatabaseFacade graphDatabaseFacade) {
        return new PlatformModule(storeDir, config, this.databaseInfo, dependencies, graphDatabaseFacade);
    }

    protected DataSourceModule createDataSource(PlatformModule platformModule, EditionModule editionModule, Supplier<QueryExecutionEngine> queryEngine) {
        return new DataSourceModule(platformModule, editionModule, queryEngine);
    }

    private void enableAvailabilityLogging(AvailabilityGuard availabilityGuard, final Logger msgLog) {
        availabilityGuard.addListener(new AvailabilityGuard.AvailabilityListener() {
            public void available() {
                msgLog.log("Database is now ready");
            }

            public void unavailable() {
                msgLog.log("Database is now unavailable");
            }
        });
    }

}
