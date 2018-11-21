package classes;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class DiskSchedulerTest {
    @Deployment
    public static JavaArchive createDeployment() {
        return ShrinkWrap.create(JavaArchive.class)
                .addClass(DiskScheduler.class)
                .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");
    }

    @Test
    public void executeAllMethods() {
        DiskScheduler scheduler = new DiskScheduler();
        for (int i = 1; i <= 4; i++) {
            scheduler.changeInputData("input/Arquivo " + i + ".in");
            scheduler.executeAllMethods("Arquivo " + i + ".out");
            assertEquals("The files differ!",
                    FileUtils.readFileToString(file1, "utf-8"),
                    FileUtils.readFileToString(file2, "utf-8"));
        }

    }
}
