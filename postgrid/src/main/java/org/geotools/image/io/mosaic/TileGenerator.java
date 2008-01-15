/*
 * (C) 2008, Geomatys
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this library; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package org.geotools.image.io.mosaic;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.spi.ImageReaderSpi;

/**
 * Generates tiles for a specific raster.
 * 
 * @source $URL$
 * @author Cédric Briançon
 */
public class TileGenerator {
    /**
     * The output folder where tiles will be written.
     */
    private final File outputFolder;
    
    /**
     * Generates tiles into the default project folder.
     */
    public TileGenerator() {
        this(null);
    }
    
    /**
     * Generates tiles into the specified folder.
     * 
     * @param outputFolder The folder where tiles will be stored.
     */
    public TileGenerator(final File outputFolder) {
        this.outputFolder = outputFolder;
    }
    
    /**
     * Create tiles which have a constant size, but cover more and more superficy
     * region of the original raster as we progress into overviews level.
     * 
     * @param spi The image provider.
     * @param raster The whole raster.
     * @param tileSize The size of tiles.
     * @param step The factor by which the size of tiles is multiplicated.
     * @return A {@linkplain TileManager} that contains all tiles generated.
     */
    public TileManager createTilesConstantSize(final ImageReaderSpi spi, 
            final Rectangle raster, final Dimension tileSize, final Dimension step) 
    {
        List<Tile> tiles = new ArrayList<Tile>();
        final String extension = spi.getFileSuffixes()[0];
        Rectangle wholeRaster = raster;
        final Rectangle tileRect = new Rectangle(tileSize);
        Dimension subSampling = new Dimension(1,1);
        // Iterator used for the file name.
        int overview = 1, x = 1, y = 1;
        while (!tileRect.contains(wholeRaster)) {
            // Current values for the y coordinates
            int yMin = 0;
            int yMax = tileSize.height;
            while (yMin < wholeRaster.height) {
                /* Verify that we are not trying to generate a tile outside the original
                 * raster bounds, for the height.
                 * If it is the case, we take the width bound of the original raster as
                 * the width bound for the new tile.
                 */
                if (yMax > wholeRaster.height) {
                    yMax = wholeRaster.height;
                }
                // Current values for the x coordinates
                int xMin = 0;
                int xMax = tileSize.width;
                while (xMin < wholeRaster.width) {
                    /* Verify that we are not trying to generate a tile outside the original
                     * raster bounds, for the width.
                     * If it is the case, we take the height bound of the original raster as
                     * the height bound for the new tile.
                     */
                    if (xMax > wholeRaster.width) {
                        xMax = wholeRaster.width;
                    }
                    Rectangle currentRect = new Rectangle(xMin, yMin, xMax - xMin, yMax - yMin);
                    // Creates a specific file for this tile.
                    final File inputTile;
                    if (outputFolder != null) {
                        inputTile = new File(outputFolder, generateName(overview, x, y, extension));
                    } else {
                        inputTile = new File(generateName(overview, x, y, extension));
                    }
                    Tile tile = new Tile(spi, inputTile, 0, currentRect, subSampling);
                    tiles.add(tile);
                    x++;
                    xMin += tileSize.width;
                    xMax += tileSize.width;
                }
                y++;
                // Restart column index from the beginning, since we have change of row index.
                x = 1;
                yMin += tileSize.height;
                yMax += tileSize.height;
            }
            // Restart row index from the beginning, since we have change of overview index.
            y = 1;
            // Change to next level of overview.
            overview++;
            subSampling.setSize(subSampling.width  * step.width, 
                                subSampling.height * step.height);
            wholeRaster.setSize(wholeRaster.width  / step.width, 
                                wholeRaster.height / step.height);
        }
        final Tile[] arrayTiles = tiles.toArray(new Tile[tiles.size()]);
        TileManager tileManager = new TileManagerFactory(null).createGeneric(arrayTiles);
        return tileManager;
    }

    /**
     * Create tiles which cover a constant region, but have a size that changes
     * as we progress into overviews levels.
     * The specified {@code minTileSize} parameter is the size for which we do not want to go 
     * deeper into overviews.
     * 
     * @param spi The image provider.
     * @param raster The whole raster.
     * @param minTileSize The size of tiles.
     * @param step The factor by which the size of tiles is multiplicated.
     * @return A {@linkplain TileManager} that contains all tiles generated.
     */
    public TileManager createTilesConstantRegion(final ImageReaderSpi spi, 
            final Rectangle raster, final Rectangle minTileSize, final Dimension step) 
    {
        List<Tile> tiles = new ArrayList<Tile>();
        final String extension = spi.getFileSuffixes()[0];
        //final Rectangle wholeRaster = raster;
        final Rectangle tileRect = new Rectangle(raster);
        Dimension subSampling = new Dimension(1,1);
        // Iterator used for the file name.
        int overview = 1, x = 1, y = 1;
        while (tileRect.contains(minTileSize)) {
            // Current values for the y coordinates
            int yMin = 0;
            int yMax = tileRect.height;
            // Current values for the x coordinates
            int xMin = 0;
            int xMax = tileRect.width;
            Rectangle currentRect = new Rectangle(xMin, yMin, xMax - xMin, yMax - yMin);
            // Creates a specific file for this tile.
            final File inputTile;
            if (outputFolder != null) {
                inputTile = new File(outputFolder, generateName(overview, x, y, extension));
            } else {
                inputTile = new File(generateName(overview, x, y, extension));
            }
            Tile tile = new Tile(spi, inputTile, 0, currentRect, subSampling);
            tiles.add(tile);
            overview++;
            subSampling.setSize(subSampling.width * step.width,
                    subSampling.height * step.height);
            Dimension newDim = chooseTileSizeForNextOverview(tileRect, step);
            tileRect.setSize(newDim);
        }
        final Tile[] arrayTiles = tiles.toArray(new Tile[tiles.size()]);
        TileManager tileManager = new TileManagerFactory(null).createGeneric(arrayTiles);
        return tileManager;
    }

    /**
     * Verify that the width of the tile is divisible by the step. If not, we try to add or 
     * substract 1 to the step, in order to search a right diviser for the size of the tile.
     * This function is applied on the width and the height.
     * 
     * @param tile The size of the tile.
     * @param step The step is the diviser for the tile size.
     * @return
     */
    private Dimension chooseTileSizeForNextOverview(final Rectangle tile, final Dimension step) {
        return new Dimension(chooseTileSizeForNextOverview(tile.width,  step.width),
                             chooseTileSizeForNextOverview(tile.height, step.height));
    }

    /**
     * 
     * @param size A size for the raster. It could be the width or height value.
     * @param step The step with which we will divide the size.
     * @return The new size divided by the step.
     */
    private static int chooseTileSizeForNextOverview(final int size, final int step) {
        if (size % step != 0) {
            for (int i=1; i<=5; i++) {
                int candidate = step - i;
                if (candidate > 1 && size % candidate == 0) {
                    return size / candidate;
                }
                candidate = step + i;
                if (size % candidate == 0) {
                    return size / candidate;
                }
            }
        }
        return size / step;
    }
    
    /**
     * Generate a name, for the current tile, based on the position of this tile 
     * in the raster. For example, a tile at the first level of overview, which
     * is localized on the 5th column and 2nd row will have the name "TileA52".
     * 
     * @param overview The level of overview. It should begin with 1.
     * @param x The index of columns. It should begin with 1.
     * @param y The index of rows. It should begin with 1.
     * @param extension The extension used for the tile.
     * @return A name based on the position of the tile in the whole raster.
     */
    private static String generateName(final int overview, final int x, final int y,
            final String extension) 
    {
        final StringBuilder buffer = new StringBuilder("Tile");
        buffer.append(overview).append('_');
        // Verify that we have not reached the end of the alphabet.
        // If it is the case, we add a secund letter.
        // @TODO: verifying that we do not exceed 26*26 for the x value, otherwise a third letter
        // would be necessary.
        toLetters(x, buffer);
        buffer.append(y).append('.').append(extension);
        return buffer.toString();
    }

    private static void toLetters(int column, final StringBuilder buffer) {
        if (column > 26) {
            toLetters(column / 26, buffer);
            column %= 26;
        }
        buffer.append((char)('A' + (column - 1)));
    }
    
    /**
     * Try to find a reader for the specified input file.
     * 
     * @param input The whole raster.
     * @return An {@linkplain ImageReader} for the specified raster, or null if no reader
     *         seems to be convenient.
     */
    public ImageReader findReader(final Object input) {
        ImageReader reader = null;
        Iterator<ImageReader> readers = ImageIO.getImageReaders(input);
        while (readers.hasNext()) {
            reader = readers.next();
            // TODO: do more check here in order to detect if it is a suitable reader.
            break;
        }
        return reader;
    }
}
