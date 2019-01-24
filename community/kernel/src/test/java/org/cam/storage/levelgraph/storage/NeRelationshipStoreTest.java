package org.cam.storage.levelgraph.storage;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.neo4j.io.fs.FileSystemAbstraction;
import org.neo4j.io.pagecache.PageCache;
import org.neo4j.kernel.configuration.Config;
import org.neo4j.kernel.impl.store.NeoStores;
import org.neo4j.kernel.impl.store.StoreFactory;
import org.neo4j.kernel.impl.store.id.DefaultIdGeneratorFactory;
import org.neo4j.kernel.impl.store.id.IdGeneratorFactory;
import org.neo4j.logging.NullLogProvider;

import java.io.File;
import java.io.IOException;
import java.util.function.Supplier;

import static org.junit.Assert.*;
import static org.mockito.Mockito.spy;
import static org.neo4j.kernel.impl.store.NodeStoreTest.pageCacheRule;

public class NeRelationshipStoreTest {

    private NeoStores neoStores;
    private NeRelationshipStore neRelationshipStore;
    private IdGeneratorFactory idGeneratorFactory;

    @Before
    public void setUp() throws Exception {

    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void readerTest(){

    }
    @Test
    public void writerTest(){

    }
     private NeRelationshipStore newNodeStore( FileSystemAbstraction fs ) throws IOException
    {
        return newNodeStore( fs, pageCacheRule.getPageCache( fs ) );
    }

    private NeRelationshipStore newNodeStore( FileSystemAbstraction fs, PageCache pageCache ) throws IOException
    {
        File storeDir = new File( "dir" );
        fs.mkdirs( storeDir );
/*        idGeneratorFactory = spy( new DefaultIdGeneratorFactory( fs )
        {
            @Override
            protected IdGenerator instantiate( FileSystemAbstraction fs, File fileName, int grabSize, long maxValue,
                    boolean aggressiveReuse, Supplier<Long> highId )
            {
                return spy( super.instantiate( fs, fileName, grabSize, maxValue, aggressiveReuse, highId ) );
            }
        } );
        StoreFactory factory = new StoreFactory( storeDir, Config.defaults(), idGeneratorFactory, pageCache, fs,
                NullLogProvider.getInstance() );
        neoStores = factory.openAllNeoStores( true );
//        neRelationshipStore = neoStores.getRelationshipStore();
*/
        return neRelationshipStore;
    }
}