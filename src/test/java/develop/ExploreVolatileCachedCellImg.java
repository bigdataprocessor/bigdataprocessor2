/*-
 * #%L
 * Fiji plugin for inspection and processing of big image data
 * %%
 * Copyright (C) 2018 - 2025 EMBL
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */
package develop;

public class ExploreVolatileCachedCellImg
{
	/*
	 *
	 * import bdv.util.BdvFunctions
	 * import bdv.util.volatiles.VolatileViews
	 * import net.imglib2.Localizable
	 * import net.imglib2.cache.img.CachedCellImg
	 * import net.imglib2.cache.img.RandomAccessibleCacheLoader
	 * import net.imglib2.cache.ref.SoftRefLoaderCache
	 * import net.imglib2.img.basictypeaccess.AccessFlags
	 * import net.imglib2.img.basictypeaccess.array.DoubleArray
	 * import net.imglib2.img.basictypeaccess.volatiles.array.VolatileDoubleArray
	 * import net.imglib2.img.cell.Cell
	 * import net.imglib2.img.cell.CellGrid
	 * import net.imglib2.position.FunctionRandomAccessible
	 * import net.imglib2.type.numeric.RealType
	 * import net.imglib2.type.numeric.real.DoubleType
	 * import java.util.function.BiConsumer
	 * import java.util.function.Supplier
	 *
	 * // choose some values
	 * fun RealType<*>.pow(power: Double) = setReal(getRealDouble().pow(power))
	 *
	 * fun Localizable.prod(target: RealType<*>, power: Double, factor: Double = 500.0) {
	 *     target.setOne()
	 *     for (d in 0 until this.numDimensions())
	 *         target.mul(factor * this.getDoublePosition(d))
	 *     target.pow(power)
	 * }
	 *
	 * val nDim = 3
	 * val power = 1.0 / nDim.toDouble()
	 * val image = FunctionRandomAccessible(
	 *     nDim,
	 *     Supplier { BiConsumer { l: Localizable, t: DoubleType -> l.prod(t, power) } },
	 *     Supplier { DoubleType() })
	 * val grid = CellGrid(longArrayOf(175, 225, 200), intArrayOf(16, 16, 16))
	 * val loader = RandomAccessibleCacheLoader.get<DoubleType, DoubleArray, VolatileDoubleArray>(
	 *     grid,
	 *     image,
	 *     AccessFlags.setOf(AccessFlags.VOLATILE))
	 * val cachedImage = CachedCellImg(
	 *     grid,
	 *     DoubleType(),
	 *     SoftRefLoaderCache<Long, Cell<VolatileDoubleArray>>().withLoader(loader),
	 *     VolatileDoubleArray(1, true))
	 * BdvFunctions.show(VolatileViews.wrapAsVolatile(cachedImage), "image")
	 *
	 */


	public static void main( String[] args )
	{

	}
}
