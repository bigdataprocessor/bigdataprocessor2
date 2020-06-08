package de.embl.cba.bdp2.save.tiff;

public class TiffUtils
{
	public static byte[] ShortToByteBigEndian( short[] input ) {
		int short_index, byte_index;
		int iterations = input.length;

		byte[] buffer = new byte[input.length * 2];

		short_index = byte_index = 0;

		for (/*NOP*/; short_index != iterations; /*NOP*/) {
			// Big Endian: store higher byte first
			buffer[byte_index] = (byte) ((input[short_index] & 0xFF00) >> 8);
			buffer[byte_index + 1] = (byte) (input[short_index] & 0x00FF);

			++short_index;
			byte_index += 2;
		}
		return buffer;
	}
}
