package develop;

import io.jhdf.HdfFile;
import io.jhdf.api.Group;
import io.jhdf.api.Node;
import org.apache.commons.lang.StringUtils;

import java.nio.file.Paths;

public class DevelopJhdfParsing
{
    public static void main(String[] args) {
        try (HdfFile hdfFile = new HdfFile( Paths.get("/Users/tischer/Documents/bigdataprocessor2/src/test/resources/test/luxendo-different-stack-size/Stack_0_Channel_1/Cam_Fused_00001.h5"))) {
            System.out.println(hdfFile.getFile().getName()); //NOSONAR - sout in example
            recursivePrintGroup(hdfFile, 0);
        }
    }

    private static void recursivePrintGroup( Group group, int level) {
        level++;
        String indent = StringUtils.repeat("    ", level);
        for (Node node : group) {
            System.out.println(indent + node.getName()); //NOSONAR - sout in example
            if (node instanceof Group) {
                recursivePrintGroup((Group) node, level);
            }
        }
    }
}
