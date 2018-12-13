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

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.graphdb.factory.GraphDatabaseFactoryState;
import org.neo4j.kernel.configuration.Config;
import org.neo4j.kernel.impl.factory.CommunityEditionModule;
import org.neo4j.kernel.impl.factory.DatabaseInfo;
import org.neo4j.kernel.impl.factory.GraphDatabaseFacadeFactory;

import javax.annotation.Nonnull;
import java.io.File;
import java.util.Map;

public class Neo4jGraphDatabaseFactory extends GraphDatabaseFactory {
    @Override
    public Neo4jGraphDatabaseBuilder newEmbeddedDatabaseBuilder(File storeDir) {
        GraphDatabaseFactoryState state = this.getStateCopy();
        Neo4jGraphDatabaseBuilder.DatabaseCreator creator = this.createDatabaseCreator(storeDir, state);
        Neo4jGraphDatabaseBuilder builder = this.createGraphDatabaseBuilder(creator);
        this.configure(builder);
        return builder;
    }

    protected Neo4jGraphDatabaseBuilder createGraphDatabaseBuilder(Neo4jGraphDatabaseBuilder.DatabaseCreator creator) {
        return new Neo4jGraphDatabaseBuilder(creator);
    }

    @Override
    protected GraphDatabaseService newEmbeddedDatabase(File storeDir, Config config, GraphDatabaseFacadeFactory.Dependencies dependencies) {
        return this.newDatabase(storeDir, config, dependencies);
    }

    @Override
    protected GraphDatabaseService newDatabase(File storeDir, Config config, GraphDatabaseFacadeFactory.Dependencies dependencies) {
        return (new Neo4jGraphDatabaseFacadeFactory(DatabaseInfo.COMMUNITY, CommunityEditionModule::new)).newFacade(storeDir, config, dependencies);
    }

    protected Neo4jGraphDatabaseBuilder.DatabaseCreator createDatabaseCreator(final File storeDir, final GraphDatabaseFactoryState state) {
        return new Neo4jGraphDatabaseBuilder.DatabaseCreator() {
            public GraphDatabaseService newDatabase(Map<String, String> config) {
                return this.newDatabase(Config.defaults(config));
            }

            public GraphDatabaseService newDatabase(@Nonnull Config config) {
                config.augment(GraphDatabaseFacadeFactory.Configuration.ephemeral, "false");
                return Neo4jGraphDatabaseFactory.this.newEmbeddedDatabase(storeDir, config, state.databaseDependencies());
            }
        };
    }

}
