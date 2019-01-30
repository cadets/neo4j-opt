package org.cam.storage.levelgraph.storage;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.neo4j.io.fs.FileSystemAbstraction;
import org.neo4j.io.pagecache.PageCache;
import org.neo4j.io.pagecache.impl.muninn.MuninnPageCache;
import org.neo4j.kernel.impl.store.NeoStores;
import org.neo4j.kernel.impl.store.id.IdGeneratorFactory;
import org.neo4j.logging.Log;
import org.neo4j.logging.LogProvider;

import java.io.File;
import java.io.IOException;

import static org.mockito.Mockito.spy;
import static org.neo4j.kernel.impl.store.NodeStoreTest.pageCacheRule;

public class SkiplistRelationshipStoreTest {

    private NeoStores neoStores;
    private SkiplistRelationshipStore skiplistRelationshipStore;
    private IdGeneratorFactory idGeneratorFactory;

    @Before
    public void setUp() throws Exception {
        skiplistRelationshipStore =new SkiplistRelationshipStore("testFile", new MuninnPageCache(null, 1000, 4096, null, null), new LogProvider() {
            @Override
            public Log getLog(Class loggingClass) {
                return null;
            }

            @Override
            public Log getLog(String name) {
                return null;
            }
        });
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
     private SkiplistRelationshipStore newNodeStore(FileSystemAbstraction fs ) throws IOException
    {
        return newNodeStore( fs, pageCacheRule.getPageCache( fs ) );
    }

    private SkiplistRelationshipStore newNodeStore(FileSystemAbstraction fs, PageCache pageCache ) throws IOException
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
//        skiplistRelationshipStore = neoStores.getRelationshipStore();
*/
        return skiplistRelationshipStore;
    }
}