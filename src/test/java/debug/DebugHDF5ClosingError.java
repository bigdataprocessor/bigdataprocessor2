package debug;

import ch.systemsx.cisd.hdf5.HDF5Factory;
import ch.systemsx.cisd.hdf5.IHDF5Reader;

public class DebugHDF5ClosingError
{
    public static void main(String[] args) {
        String file = "path/to/file.h5";
        System.out.println("Is HDF5? " + HDF5Factory.isHDF5File(file));
        try (IHDF5Reader reader = HDF5Factory.openForReading(file)) {
            System.out.println("File opened successfully");
        }
    }
}
